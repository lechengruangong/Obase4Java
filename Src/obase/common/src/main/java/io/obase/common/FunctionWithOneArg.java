/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：有一个参数参数的有返回值委托接口
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-11 16:01:26
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.common;

/**
 * 有一个参数参数的有返回值委托接口 用于实现传入一个对象并对其做操作
 */
@FunctionalInterface
public interface FunctionWithOneArg<T, TObject> {

    /**
     * 实际操作方法
     *
     * @param paramObj 参数
     */
    TObject invoke(T paramObj);
}
