package jvm;

/**
 * @author likelong
 * @date 2023/10/29 22:14
 * @description
 */
public class ClassLoaderTest {
    public static void main(String[] args) {
        // 根类加载器，代码层面无法获取到，返回null
        System.out.println(String.class.getClassLoader());

        // 获取应用类加载器
        System.out.println(ClassLoader.getSystemClassLoader());
    }
}
