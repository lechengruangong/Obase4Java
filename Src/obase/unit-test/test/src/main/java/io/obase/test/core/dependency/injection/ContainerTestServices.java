/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：依赖注入容器、作用域测试用的服务类型.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.test.core.dependency.injection;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 依赖注入容器与作用域测试用的服务类型
 * <p>
 * 这些服务在ConfigSetUp中注册,测试用例只负责从已建好的容器中获取.
 * 放在本包而不是{@code io.obase.test.domain}下,是因为该包会被整体注册进对象数据模型.
 */
public final class ContainerTestServices {

    private ContainerTestServices() {
    }

    /**
     * 释放顺序记录器
     */
    public static final class DisposalLog {

        private static final Queue<String> ENTRIES = new ConcurrentLinkedQueue<>();

        private DisposalLog() {
        }

        /**
         * 记录一次释放
         *
         * @param name 名称
         */
        static void record(String name) {
            ENTRIES.add(name);
        }

        /**
         * 已记录的顺序
         *
         * @return 以逗号分隔的顺序
         */
        public static String joined() {
            return String.join(",", ENTRIES);
        }

        /**
         * 清空记录
         */
        public static void reset() {
            ENTRIES.clear();
        }
    }

    /**
     * 服务接口
     */
    public interface IService {
        /**
         * 标识
         *
         * @return 标识
         */
        String getCode();
    }

    /**
     * 服务实现一
     */
    public static final class ServiceA implements IService {
        @Override
        public String getCode() {
            return "A";
        }
    }

    /**
     * 服务实现二
     */
    public static final class ServiceB implements IService {
        @Override
        public String getCode() {
            return "B";
        }
    }

    /**
     * 用于构造函数注入的依赖
     */
    public static final class Dependency {
    }

    /**
     * 需要构造函数参数的服务
     */
    public static final class NeedsDependency {

        private final Dependency dependency;

        private NeedsDependency(Dependency dependency) {
            this.dependency = dependency;
        }

        /**
         * 依赖
         *
         * @return 依赖
         */
        public Dependency getDependency() {
            return this.dependency;
        }
    }

    /**
     * 未注册的接口 用于验证构造函数参数无法解析时的行为
     */
    public interface INotRegistered {
    }

    /**
     * 构造函数依赖未注册接口的服务
     */
    public static final class NeedsUnregistered {
        private NeedsUnregistered(INotRegistered service) {
        }
    }

    /**
     * 仅由委托注册的接口
     */
    public interface IFactoryOnly {
    }

    /**
     * 仅由委托注册的接口的实现
     */
    public static final class FactoryOnly implements IFactoryOnly {
    }

    /**
     * 循环依赖的一方
     */
    public static final class CyclicA {
        private CyclicA(CyclicB b) {
        }
    }

    /**
     * 循环依赖的另一方
     */
    public static final class CyclicB {
        private CyclicB(CyclicA a) {
        }
    }

    /**
     * 慢速构造的单例 用于并发测试
     */
    public static final class SlowSingleton {

        private static final AtomicInteger CREATED = new AtomicInteger(0);

        private SlowSingleton() {
            CREATED.incrementAndGet();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * 创建次数
         *
         * @return 创建次数
         */
        public static int getCreated() {
            return CREATED.get();
        }
    }

    /**
     * 泛型服务接口 用于验证泛型注册
     *
     * @param <T> 元素类型
     */
    public interface IRepository<T> {
    }

    /**
     * 泛型服务接口的实现
     *
     * @param <T> 元素类型
     */
    public static final class Repository<T> implements IRepository<T> {
    }

    /**
     * 记录创建与释放次数的可释放服务
     */
    public static final class Tracked implements AutoCloseable {

        private static final AtomicInteger CREATED = new AtomicInteger(0);

        private static final AtomicInteger DISPOSED = new AtomicInteger(0);

        private Tracked() {
            CREATED.incrementAndGet();
        }

        @Override
        public void close() {
            DISPOSED.incrementAndGet();
        }

        /**
         * 创建次数
         *
         * @return 创建次数
         */
        public static int getCreated() {
            return CREATED.get();
        }

        /**
         * 释放次数
         *
         * @return 释放次数
         */
        public static int getDisposed() {
            return DISPOSED.get();
        }

        /**
         * 重置计数
         */
        public static void reset() {
            CREATED.set(0);
            DISPOSED.set(0);
        }
    }

    /**
     * 记录释放顺序的服务 无依赖
     */
    public static final class LoggedA implements AutoCloseable {
        @Override
        public void close() {
            DisposalLog.record("A");
        }
    }

    /**
     * 记录释放顺序的服务 依赖LoggedA
     */
    public static final class LoggedB implements AutoCloseable {

        private LoggedB(LoggedA a) {
        }

        @Override
        public void close() {
            DisposalLog.record("B");
        }
    }

    /**
     * 父作用域中的可释放服务
     */
    public static final class OuterItem implements AutoCloseable {
        @Override
        public void close() {
            DisposalLog.record("outer");
        }
    }

    /**
     * 子作用域中的可释放服务
     */
    public static final class InnerItem implements AutoCloseable {
        @Override
        public void close() {
            DisposalLog.record("inner");
        }
    }

    /**
     * 孙作用域中的可释放服务
     */
    public static final class GrandchildItem implements AutoCloseable {
        @Override
        public void close() {
            DisposalLog.record("grandchild");
        }
    }

    /**
     * 作用域内单例 记录创建次数
     */
    public static final class ScopedOnly implements AutoCloseable {

        private static final AtomicInteger CREATED = new AtomicInteger(0);

        private ScopedOnly() {
            CREATED.incrementAndGet();
        }

        @Override
        public void close() {
        }

        /**
         * 创建次数
         *
         * @return 创建次数
         */
        public static int getCreated() {
            return CREATED.get();
        }

        /**
         * 重置计数
         */
        public static void reset() {
            CREATED.set(0);
        }
    }

    /**
     * 记录释放次数的作用域内单例
     */
    public static final class ScopedDisposable implements AutoCloseable {

        private static final AtomicInteger DISPOSED = new AtomicInteger(0);

        @Override
        public void close() {
            DISPOSED.incrementAndGet();
        }

        /**
         * 释放次数
         *
         * @return 释放次数
         */
        public static int getDisposed() {
            return DISPOSED.get();
        }

        /**
         * 重置计数
         */
        public static void reset() {
            DISPOSED.set(0);
        }
    }

    /**
     * 作用域内单例 依赖一个多例 用于验证释放顺序
     */
    public static final class ScopedWithTransient implements AutoCloseable {

        private final LoggedA dependency;

        private ScopedWithTransient(LoggedA dependency) {
            this.dependency = dependency;
        }

        /**
         * 多例依赖
         *
         * @return 多例依赖
         */
        public LoggedA getDependency() {
            return this.dependency;
        }

        @Override
        public void close() {
            DisposalLog.record("scoped");
        }
    }

    /**
     * 单例 依赖作用域内单例 获取时应当失败
     */
    public static final class SingletonNeedsScoped {
        private SingletonNeedsScoped(ScopedOnly service) {
        }
    }
}
