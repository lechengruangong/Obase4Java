/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：有三个参数参数的有返回值委托接口
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-11 16:13:57
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.common;

/**
 * 有三个参数参数的有返回值委托接口 用于实现传入三个对象并对其做操作
 */
@FunctionalInterface
public interface FunctionWithThreeArgs<T1, T2, T3, TObject> {

    /**
     * 实际操作方法
     *
     * @param paramObj1 参数1
     * @param paramObj2 参数2
     * @param paramObj3 参数3
     * @return 返回值
     */
    TObject invoke(T1 paramObj1, T2 paramObj2, T3 paramObj3);
}
