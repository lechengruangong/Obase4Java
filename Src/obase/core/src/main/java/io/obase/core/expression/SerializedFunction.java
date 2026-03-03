/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：可序列化的按照某种方式转换传入对象接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-18 14:17:10
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 一个函数式接口 继承自Function<T,R>和Serializable
 * 可以实现按照某种方式转换传入对象
 *
 * @param <T> 传入对象
 * @param <R> 结果对象
 */
@FunctionalInterface
public interface SerializedFunction<T, R> extends Function<T, R>, Serializable {
}

