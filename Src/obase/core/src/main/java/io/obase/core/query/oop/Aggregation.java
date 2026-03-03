/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：聚合接口实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 16:44:52
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.oop;

import io.obase.core.expression.IAggregation;

import java.util.List;

/**
 * 聚合接口实现
 *
 * @param <T> 要聚合的元素类型
 */
public class Aggregation<T> implements IAggregation<T> {

    /**
     * 内容容器
     */
    private final List<T> list;

    /**
     * 构造聚合接口实现
     *
     * @param list 要聚合的目标
     */
    public Aggregation(List<T> list) {
        this.list = list;
    }

    /**
     * 转换为数组
     *
     * @return 数组
     */
    @Override
    public T[] toArray() {
        return (T[]) this.list.toArray(new Object[0]);
    }

    /**
     * 转换为列表
     *
     * @return 列表
     */
    @Override
    public List<T> toList() {
        return this.list;
    }

    /**
     * 求计数
     *
     * @return 长整型计数
     */
    @Override
    public long countLong() {
        return this.list.size();
    }

    /**
     * 求和
     *
     * @return 长整型和
     */
    @Override
    public long sumLong() {
        return this.list.stream().mapToLong(p -> Long.parseLong(p.toString())).sum();
    }

    /**
     * 求和
     *
     * @return 双精度和
     */
    @Override
    public double sumDouble() {
        return this.list.stream().mapToDouble(p -> Double.parseDouble(p.toString())).sum();
    }

    /**
     * 求最小值
     *
     * @return 长整型最小值
     */
    @Override
    public long minLong() {
        return this.list.stream().mapToLong(p -> Long.parseLong(p.toString())).min().orElse(0);
    }

    /**
     * 求最小值
     *
     * @return 双精度最小值
     */
    @Override
    public double minDouble() {
        return this.list.stream().mapToDouble(p -> Double.parseDouble(p.toString())).min().orElse(0);
    }

    /**
     * 求最大值
     *
     * @return 长整型最大值
     */
    @Override
    public long maxLong() {
        return this.list.stream().mapToLong(p -> Long.parseLong(p.toString())).max().orElse(0);
    }

    /**
     * 求最大值
     *
     * @return 双精度最大值
     */
    @Override
    public double maxDouble() {
        return this.list.stream().mapToDouble(p -> Double.parseDouble(p.toString())).max().orElse(0);
    }


    /**
     * 求平均值
     *
     * @return 双精度平均值
     */
    @Override
    public double avgDouble() {
        return this.list.stream().mapToDouble(p -> Double.parseDouble(p.toString())).average().orElse(0);
    }
}
