/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：组件工具.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-24 15:00:06
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.common;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class AssemblyUtil {

    /**
     * 获取某个包下的所有类
     *
     * @param packageName 包名
     * @return 忽略掉匿名类 或者非公开类 的软件包下的类集合
     */
    public static Set<Class<?>> getAllClassByPackageName(String packageName) {
        return getClasses(packageName).stream().filter(p -> !p.isAnonymousClass()).filter(p -> Modifier.isPublic(p.getModifiers())).collect(Collectors.toSet());
    }

    /**
     * 从包package中获取所有的Class
     *
     * @param pack 包名
     * @return 软件包下的类集合
     */
    public static Set<Class<?>> getClasses(String pack) {

        // 第一个class类的集合
        Set<Class<?>> classes = new LinkedHashSet<>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                // 如果是一个.class文件 而且不是目录
                                if (name.endsWith(".class") && !entry.isDirectory()) {
                                    // 去掉后面的".class" 获取真正的类名
                                    String className = name.substring(packageName.length() + 1, name.length() - 6);
                                    try {
                                        // 添加到classes
                                        classes.add(Class.forName(packageName + '.' + className));
                                    } catch (ClassNotFoundException e) {
                                        // 没有对应的Class文件 忽略掉即可
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        // 出现IO异常 忽略掉即可
                    }
                }
            }
        } catch (IOException e) {
            // 出现IO异常 忽略掉即可
        }

        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName 包名
     * @param packagePath 包路径
     * @param recursive   是否地柜
     * @param classes     结果集合
     */
    public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, Set<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
        File[] files = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));

        if (files != null) {
            // 循环所有文件
            for (File file : files) {
                // 如果是目录 则继续扫描
                if (file.isDirectory()) {
                    findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive,
                            classes);
                } else {
                    // 如果是java类文件 去掉后面的.class 只留下类名
                    String className = file.getName().substring(0, file.getName().length() - 6);
                    try {
                        // 用Loader载入
                        classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                    } catch (ClassNotFoundException e) {
                        // 没有对应的Class文件 忽略掉即可
                    }
                }
            }
        }
    }
}
