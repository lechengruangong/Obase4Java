/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：介入接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-2 16:12:26
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 介入接口
 * 对象实现此接口以允许第三方介入其行为
 */
public interface IIntervene {

    /**
     * 向指定的对象注册介入者以实施介入
     *
     * @param intervener 介入者
     */
    void registerIntervener(IIntervener intervener);

    /**
     * 禁用延迟加载
     */
    void forbidLazyLoading();

    /**
     * 启用延迟加载
     */
    void enableLazyLoading();
}
