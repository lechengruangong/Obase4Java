/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表达式投影列.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 16:25:51
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.rop.SourceAliasRootSetter;

import java.util.Objects;

/**
 * 表达式投影列，即以一个表达式指定查询结果列
 */
public class ExpressionColumn extends SelectionColumn {

    /**
     * 列的别名
     */
    private String alias;

    /**
     * 生成列的表达式
     */
    private Expression expression;

    /**
     * 获取投影列的别名
     *
     * @return 投影列的别名
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * 设置投影列的别名
     *
     * @param alias 投影列的别名
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * 获取生成列的表达式
     *
     * @return 生成列的表达式
     */
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * 设置生成列的表达式
     *
     * @param expression 生成列的表达式
     */
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     * 获取哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.alias, this.expression);
    }

    /**
     * 确定指定的投影列是否与当前投影列相等。注：两个投影列相等的充要条件是表达式和别名均相等。
     *
     * @param other 要与当前投影列进行比较的投影列
     * @return 是否相等
     */
    @Override
    public boolean equals(SelectionColumn other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        ExpressionColumn that = (ExpressionColumn) other;
        return Objects.equals(this.alias, that.alias) && Objects.equals(this.expression, that.expression);
    }

    /**
     * 转换为字符串表示形式
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
    @Override
    public String toString(EDataSource sourceType) {
        return this.expression.toString(sourceType) + " " + (this.alias == null ? "" : this.alias);
    }

    /**
     * 为投影列涉及到的源的别名设置前缀
     *
     * @param prefix 别名前缀
     */
    @Override
    public void setSourceAliasPrefix(String prefix) {
        SourceAliasRootSetter setter = new SourceAliasRootSetter(prefix);
        this.getExpression().accept(setter);
    }
}
