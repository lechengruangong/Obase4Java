/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：运算结果为long的函数式接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 11:58:38
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import java.io.Serializable;

/**
 * 运算结果为long的函数式接口
 * 用于聚合操作 如求和 求平均值 求最小值 求最大值
 *
 * @param <T> 源对象类型
 */
@FunctionalInterface
public interface SerializedLongResult<T> extends Serializable {

    /**
     * 运算结果为long的值
     *
     * @param t 源对象
     * @return 运算结果
     */
    long apply(T t);
}
