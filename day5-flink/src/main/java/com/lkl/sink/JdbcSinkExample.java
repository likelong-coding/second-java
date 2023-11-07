package com.lkl.sink;

import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class JdbcSinkExample {

    static class Book {
        public Book(Long id, String title, String authors, Integer year) {
            this.id = id;
            this.title = title;
            this.authors = authors;
            this.year = year;
        }

        final Long id;
        final String title;
        final String authors;
        final Integer year;
    }

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        /*
            写入 mysql
            1、只能用老的 sink 写法： addSink
            2、JDBCSink 的 4 个参数:
                第一个参数： 执行的 sql，一般就是 insert into
                第二个参数： 预编译 sql， 对占位符填充值
                第三个参数： 执行选项 ---》 攒批、重试
                第四个参数： 连接选项 ---》 url、用户名、密码
         */
        env.fromElements(
                new Book(101L, "Stream Processing with Apache Flink", "Fabian Hueske, Vasiliki Kalavri", 2019),
                new Book(102L, "Streaming Systems", "Tyler Akidau, Slava Chernyak, Reuven Lax", 2018),
                new Book(103L, "Designing Data-Intensive Applications", "Martin Kleppmann", 2017),
                new Book(104L, "Kafka: The Definitive Guide", "Gwen Shapira, Neha Narkhede, Todd Palino", 2017)
        ).addSink(
                JdbcSink.sink(
                        "insert into books (id, title, authors, year) values (?, ?, ?, ?)",
                        (statement, book) -> {
                            statement.setLong(1, book.id);
                            statement.setString(2, book.title);
                            statement.setString(3, book.authors);
                            statement.setInt(4, book.year);
                        },
                        JdbcExecutionOptions.builder()
                                .withBatchSize(1000) // 批次的大小：条数
                                .withBatchIntervalMs(200) // 批次的时间
                                .withMaxRetries(5) // 重试次数
                                .build(),
                        new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                                .withUrl("jdbc:mysql://localhost:3306/test?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF8")
                                .withUsername("root")
                                .withPassword("root")
                                .withConnectionCheckTimeoutSeconds(60) // 重试的超时时间
                                .build()
                ));

        env.execute();
    }
}