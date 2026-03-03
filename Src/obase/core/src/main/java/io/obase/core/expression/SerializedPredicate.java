/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：可序列化的判断传入参数是否符合条件接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-18 14:15:31
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * 一个可序列化的函数式接口 继承自Predicate<T>和Serializable
 * 可以实现判断传入参数是否符合条件
 *
 * @param <T> 传入参数 被测试者
 */
@FunctionalInterface
public interface SerializedPredicate<T> extends Predicate<T>, Serializable {
}
