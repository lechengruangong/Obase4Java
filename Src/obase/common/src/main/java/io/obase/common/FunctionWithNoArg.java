/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：没有参数的有返回值委托接口
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-11 15:55:11
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.common;

/**
 * 没有参数的有返回值委托接口 用于实现没有参数的调用
 *
 * @param <TObject>
 */
@FunctionalInterface
public interface FunctionWithNoArg<TObject> {

    /**
     * 实际操作方法
     *
     * @return 返回值
     */
    TObject invoke();
}
