package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hmdp.constants.RedisConstants.LOCK_ORDER_KEY;
import static com.hmdp.constants.RedisConstants.QUEUE_STREAM_ORDERS;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    /**
     * 当前类初始化完毕之后执行，只要有任务就执行
     */
    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    private class VoucherOrderHandler implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    // 1.获取消息队列中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS stream.orders >
                    // （> 下一条未消费的消息开始）
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(QUEUE_STREAM_ORDERS, ReadOffset.lastConsumed())
                    );
                    // 2.判断订单信息是否为空
                    if (list == null || list.isEmpty()) {
                        // 如果为null，说明没有消息，继续下一次循环
                        continue;
                    }
                    // 解析消息中订单信息
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);
                    // 3.创建订单
                    proxy.createVoucherOrder(voucherOrder);
                    // 4.确认消息 XACK stream.orders g1 id
                    stringRedisTemplate.opsForStream().acknowledge(QUEUE_STREAM_ORDERS, "g1", record.getId());
                } catch (Exception e) {
                    log.error("创建订单异常", e);
                    // 处理异常消息
                    handlePendingList();
                }
            }
        }
    }

    private void handlePendingList() {
        while (true) {
            try {
                // 1.获取pending-list中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 1000 STREAMS stream.orders 0
                // （0 pending-list中第一条消息开始）
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from("g1", "c1"),
                        StreamReadOptions.empty().count(1).block(Duration.ofSeconds(1)),
                        StreamOffset.create(QUEUE_STREAM_ORDERS, ReadOffset.from("0"))
                );
                // 2.判断订单信息是否为空
                if (list == null || list.isEmpty()) {
                    // 如果为null，说明pending-list没有异常消息，结束循环
                    break;
                }
                // 解析数据
                MapRecord<String, Object, Object> record = list.get(0);
                Map<Object, Object> value = record.getValue();
                VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);
                // 3.创建订单
                proxy.createVoucherOrder(voucherOrder);
                // 4.确认消息 XACK
                stringRedisTemplate.opsForStream().acknowledge(QUEUE_STREAM_ORDERS, "g1", record.getId());
            } catch (Exception e) {
                log.error("处理pending-list订单异常", e);
            }
        }
    }

//    private final BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);
//    private class VoucherOrderHandler implements Runnable {
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    // 1.获取队列中订单信息，没订单阻塞，不会损耗性能
//                    VoucherOrder voucherOrder = orderTasks.take();
//                    // 2.创建订单
//                    handleVoucherOrder(voucherOrder);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    log.error("创建订单异常", e);
//                }
//            }
//        }
//    }

    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        RLock lock = redissonClient.getLock(LOCK_ORDER_KEY + voucherOrder.getUserId());
        // 获取锁 无参方法，默认不等待失败立即返回，超过30s还未释放锁就自动释放锁
        boolean isLock = lock.tryLock();
        // 判断是否获取锁成功
        if (!isLock) {
            // 获取锁失败，返回错误或者重试
            // 一个人只能下一单，直接返回错误即可
            log.error("一个用户只能购买一单！");
            return;
        }
        try {
            // 在子线程中ThreadLocal中无法获取代理对象，提高作用域在主线程中获取
            proxy.createVoucherOrder(voucherOrder);
        } finally {
            // 无论怎样一定要释放锁，写在finally代码块里
            lock.unlock();
        }
    }

    private IVoucherOrderService proxy;

    @Override
    public Result seckillVoucher(Long voucherId) {
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀尚未开始！");
        }
        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀已经结束！");
        }
        if (seckillVoucher.getStock() < 1) {
            return Result.fail("库存不足！");
        }

        // 一、判断秒杀资格 & 发消息到队列
        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("order");
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(), String.valueOf(orderId)
        );

        int r = result.intValue();
        // 1.判断结果是否为0
        if (r != 0) {
            // 1.1.不为0 ，代表没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        // 2.获取代理对象
        proxy = (IVoucherOrderService) AopContext.currentProxy();
        // 3.返回订单id
        return Result.ok(orderId);
    }

//    @Override
//    public Result seckillVoucher(Long voucherId) {
//        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
//        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
//            return Result.fail("秒杀尚未开始！");
//        }
//        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
//            return Result.fail("秒杀已经结束！");
//        }
//        if (seckillVoucher.getStock() < 1) {
//            return Result.fail("库存不足！");
//        }
//
//        // 一、判断秒杀资格
//        Long userId = UserHolder.getUser().getId();
//        Long result = stringRedisTemplate.execute(
//                SECKILL_SCRIPT,
//                Arrays.asList(SECKILL_STOCK_KEY + voucherId, SECKILL_ORDER_KEY + voucherId),
//                userId.toString()
//        );
//
//        int r = result.intValue();
//        // 1.判断结果是否为0
//        if (r != 0) {
//            // 1.1.不为0 ，代表没有购买资格
//            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
//        }
//
//        // 二、将信息存入阻塞队列
//        VoucherOrder voucherOrder = new VoucherOrder();
//        // 2.1.订单id
//        long orderId = redisIdWorker.nextId("order");
//        voucherOrder.setId(orderId);
//        // 2.2.用户id
//        voucherOrder.setUserId(userId);
//        // 2.3.代金券id
//        voucherOrder.setVoucherId(voucherId);
//        orderTasks.add(voucherOrder);
//        // 3.获取代理对象
//        proxy = (IVoucherOrderService) AopContext.currentProxy();
//        // 4.返回订单id
//        return Result.ok(orderId);
//    }

//    @Override
//    public Result seckillVoucher(Long voucherId) {
//
//        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
//
//        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
//            return Result.fail("秒杀尚未开始！");
//        }
//
//        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
//            return Result.fail("秒杀已经结束！");
//        }
//
//        if (seckillVoucher.getStock() < 1) {
//            return Result.fail("库存不足！");
//        }
//
//        Long userId = UserHolder.getUser().getId();
//
//        // SimpleRedisLock simpleRedisLock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
//        RLock lock = redissonClient.getLock("lock:order:" + userId);
//
//        // 获取锁 无参方法，默认不等待失败立即返回，超过30s还未释放锁就自动释放锁
//        boolean isLock = lock.tryLock();
//        // 判断是否获取锁成功
//        if(!isLock){
//            // 获取锁失败，返回错误或者重试
//            // 一个人只能下一单，直接返回错误即可
//            return Result.fail("一个用户只能购买一单！");
//        }
//        try {
//            // 获取代理对象（事务）
//            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
//            return proxy.createVoucherOrder(voucherId);
//        } finally {
//            // 无论怎样一定要释放锁，写在finally代码块里
//            lock.unlock();
//        }
//    }

    @Override
    @Transactional
    public Result createVoucherOrder(Long voucherId) {
        // 查询订单 保证 user_id + voucher_id 唯一性
        Long userId = UserHolder.getUser().getId();
        int count = query().eq("user_id", userId)
                .eq("voucher_id", voucherId).count();
        // 判断是否已存在
        if (count > 0) {
            // 用户已经购买过了
            return Result.fail("用户已经购买过一次！");
        }
        // 1.扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                // CAS 存在成功率低的问题
                // .eq("stock", seckillVoucher.getStock())
                // 实际上只要 stock > 0 就能扣减
                .gt("stock", 0)
                .update();

        if (!success) {
            return Result.fail("库存不足！");
        }

        // 2.创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 2.1.订单id
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        // 2.2.用户id
        voucherOrder.setUserId(userId);
        // 2.3.代金券id
        voucherOrder.setVoucherId(voucherId);
        save(voucherOrder);
        return Result.ok(orderId);
    }

    @Override
    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        // 查询订单 保证 user_id + voucher_id 唯一性
        Long userId = voucherOrder.getUserId();
        int count = query().eq("user_id", userId)
                .eq("voucher_id", voucherOrder.getVoucherId()).count();
        // 判断是否已存在
        if (count > 0) {
            // 用户已经购买过了
            log.error("用户已经购买过一次！");
            return;
        }
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherOrder.getVoucherId())
                // CAS 存在成功率低的问题
                // .eq("stock", seckillVoucher.getStock())
                // 实际上只要 stock > 0 就能扣减
                .gt("stock", 0)
                .update();

        if (!success) {
            log.error("库存不足！");
            return;
        }
        save(voucherOrder);
    }
}
