/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：指示是否使用延迟加载的接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-26 15:51:13
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

/**
 * 指示是否使用延迟加载的接口
 */
public interface ILazyLoadingConfiguration {

    /**
     * 是否启用延迟加载
     *
     * @return 是否启用延迟加载
     */
    boolean getEnableLazyLoading();

    /**
     * 指定关联或关联端的加载优先级，数值小者先加载。
     *
     * @return 优先级
     */
    int getLoadingPriority();
}
