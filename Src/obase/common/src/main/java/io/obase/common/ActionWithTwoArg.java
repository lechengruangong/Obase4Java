/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：有两个参数的委托接口
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-11 15:08:35
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.common;

/**
 * 有两个参数的无返回值委托接口 用于实现传入两个对象并对其做操作
 */
@FunctionalInterface
public interface ActionWithTwoArg<T1, T2> {

    /**
     * 实际操作方法
     *
     * @param paramObj1 参数1
     * @param paramObj2 参数2
     */
    void invoke(T1 paramObj1, T2 paramObj2);
}
