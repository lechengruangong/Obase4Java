/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对某个类型聚合接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-18 15:31:11
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import java.util.List;

/**
 * 表示对某个类型聚合
 * 用于分组函数中的聚合操作
 *
 * @param <T> 类型
 */
public interface IAggregation<T> {

    /**
     * 转换为数组
     *
     * @return 数组
     */
    T[] toArray();

    /**
     * 转换为列表
     *
     * @return 列表
     */
    List<T> toList();

    /**
     * 求计数
     *
     * @return 长整型计数
     */
    long countLong();

    /**
     * 求和
     *
     * @return 长整型和
     */
    long sumLong();

    /**
     * 求和
     *
     * @return 双精度和
     */
    double sumDouble();

    /**
     * 求最小值
     *
     * @return 长整型最小值
     */
    long minLong();

    /**
     * 求最小值
     *
     * @return 双精度最小值
     */
    double minDouble();

    /**
     * 求最大值
     *
     * @return 长整型最大值
     */
    long maxLong();

    /**
     * 求最大值
     *
     * @return 双精度最大值
     */
    double maxDouble();

    /**
     * 求平均值
     *
     * @return 双精度平均值
     */
    double avgDouble();
}
