/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Over子句.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 15:34:44
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.providers.sql.EDataSource;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 表示Over子句
 */
public class OverClause {

    /**
     * 作为排序依据的排序规则序列
     */
    private Order[] orderBy;

    /**
     * 作为分区依据的表达式序列
     */
    private Expression[] partitionBy;

    /**
     * 创建OverClause实例，同时指定排序依据
     *
     * @param orderBy 排序规则
     */
    public OverClause(Order orderBy) {
        this.orderBy = new Order[1];
        this.orderBy[0] = orderBy;
    }

    /**
     * 创建OverClause实例，同时指定排序依据
     *
     * @param orderBy 一个作为排序依据的排序规则序列
     */
    public OverClause(Order[] orderBy) {
        this.orderBy = orderBy;
    }

    /**
     * 创建OverClause实例，同时指定分区表达式
     *
     * @param partitionBy 分区表达式
     */
    public OverClause(Expression partitionBy) {
        this.partitionBy = new Expression[1];
        this.partitionBy[0] = partitionBy;
    }

    /**
     * 创建OverClause实例，同时指定分区表达式
     *
     * @param partitionBy 一个作为分区依据的表达式序列
     */
    public OverClause(Expression[] partitionBy) {
        this.partitionBy = partitionBy;
    }

    /**
     * 创建OverClause实例，同时指定分区表达式和排序依据
     *
     * @param partitionBy 分区表达式
     * @param orderBy     排序规则
     */
    public OverClause(Expression partitionBy, Order orderBy) {
        this.orderBy = new Order[1];
        this.orderBy[0] = orderBy;
        this.partitionBy = new Expression[1];
        this.partitionBy[0] = partitionBy;
    }

    /**
     * 创建OverClause实例，同时指定分区表达式和排序依据
     *
     * @param partitionBy 作为分区依据的表达式序列
     * @param orderBy     作为排序依据的排序规则序列
     */
    public OverClause(Expression[] partitionBy, Order[] orderBy) {
        this.orderBy = orderBy;
        this.partitionBy = partitionBy;
    }

    /**
     * 获取作为分区依据的表达式序列
     *
     * @return 作为分区依据的表达式序列
     */
    public Expression[] getPartitionBy() {
        if (this.partitionBy == null)
            this.partitionBy = new Expression[0];
        return this.partitionBy;
    }

    /**
     * 获取作为排序依据的排序规则序列
     *
     * @return 作为排序依据的排序规则序列
     */
    public Order[] getOrderBy() {
        if (this.orderBy == null)
            this.orderBy = new Order[0];
        return this.orderBy;
    }

    /**
     * 重写转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return this.toString(EDataSource.SqlServer);
    }

    /**
     * 转换为字符串表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    public String toString(EDataSource sourceType) {
        return "OVER (" + (this.getPartitionBy().length > 0 ? "PARTITION BY " + Arrays.stream(this.getPartitionBy()).map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) : "") +
                (this.getOrderBy().length > 0 ? "ORDER BY " + Arrays.stream(this.getOrderBy()).map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) : "") + ") ";
    }

    /**
     * 重写相等比较方法
     *
     * @param o 另一个对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        OverClause that = (OverClause) o;
        return Arrays.equals(this.orderBy, that.orderBy) && Arrays.equals(this.partitionBy, that.partitionBy);
    }

    /**
     * 重写获取哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        int result = Arrays.hashCode(this.orderBy);
        result = 31 * result + Arrays.hashCode(this.partitionBy);
        return result;
    }
}
