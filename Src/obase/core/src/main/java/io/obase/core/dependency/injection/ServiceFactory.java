/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：服务构造接口,函数式接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-13 15:50:27
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.dependency.injection;

/**
 * 服务构造工厂
 *
 * @param <T> 服务类型
 */
@FunctionalInterface
public interface ServiceFactory<T> {
    /**
     * 获取服务对象
     *
     * @param container 服务注入容器
     * @return 服务对象
     */
    T getServiceInstance(ServiceContainer container);
}
