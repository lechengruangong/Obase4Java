/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：通配列.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 16:18:55
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.core.common.Utils;
import io.obase.providers.sql.EDataSource;

import java.util.Objects;

/**
 * 通配列，即以一个通配符指定查询结果列。
 */
public class WildcardColumn extends SelectionColumn {

    /**
     * 指定一个源，该源界定通配范围
     */
    private MonomerSource source;

    /**
     * 获取界定通配范围的源
     *
     * @return 源
     */
    public MonomerSource getSource() {
        return this.source;
    }

    /**
     * 设置界定通配范围的源
     *
     * @param source 源
     */
    public void setSource(MonomerSource source) {
        this.source = source;
    }

    /**
     * 获取哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return this.source.hashCode();
    }

    /**
     * 确定指定的投影列是否与当前投影列相等。注：两个投影列相等的充要条件是表达式和别名均相等。
     *
     * @param other 要与当前投影列进行比较的投影列
     * @return 是否相等
     */
    @Override
    public boolean equals(SelectionColumn other) {
        WildcardColumn newOther = (WildcardColumn) other;
        if (newOther == null)
            return false;
        return newOther.getSource() == this.source;
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
        if (this.source == null || Utils.getStringIsEmpty(this.source.getSymbol()))
            return " * ";
        switch (sourceType) {
            case SqlServer:
                return "[" + this.source.getSymbol() + "].*";
            case PostgreSql:
                return "" + this.source.getSymbol() + ".*";
            case Oracle:
                return this.source.getSymbol() + ".*";
            case Oledb:
            case MySql:
            case Sqlite:
                return "`" + this.source.getSymbol() + "`.*";
            default:
                throw new IllegalArgumentException("不支持的数据源: " + sourceType);
        }
    }

    /**
     * 为投影列涉及到的源的别名设置前缀
     *
     * @param prefix 别名前缀
     */
    @Override
    public void setSourceAliasPrefix(String prefix) {
        //如果为MonomerSource简单源则设置别名
        if (this.getSource() != null) {
            this.getSource().setSymbolPrefix(prefix);
        }
    }

    /**
     * 确定当前通配列是否逻辑蕴含指定的投影列
     * 判定规则：
     * （1）如果目标列为表达式列但表达式不为字段表达式，判为不蕴含；
     * （2）如果当前列未指定通配范围，则蕴含所有字段表达式列和通配列；
     * （3）如果当前列指定了通配范围，其通配范围为S0，目标列为通配列，其通配范围为S1，当S0.ToString(Select-Clause)==S1.
     * ToString(Select-Clause)时，判定为蕴含；
     * （4）如果当前列指定了通配范围，其通配范围为S0，目标列为字段表达式列，字段所属的源为S1，当S0.ToString(Select-Clause)==S1.
     * ToString(Select-Clause)时，判定为蕴含。
     *
     * @param other 目标投影列
     * @return 果蕴含返回true，否则返回false
     */
    public boolean implies(SelectionColumn other) {

        if (other instanceof ExpressionColumn) {
            //如果为表达式列 不为字段表达式
            ExpressionColumn otherExpression = (ExpressionColumn) other;
            if (!(otherExpression.getExpression() instanceof FieldExpression)) return false;
        }
        //如果当前列未指定通配范围
        if (this.source == null) return true;

        if (other instanceof WildcardColumn) {
            WildcardColumn otherWild = (WildcardColumn) other;
            //目标列为通配列
            if (Objects.equals(this.source.toString(EDataSource.SqlServer), otherWild.getSource().toString(EDataSource.SqlServer)))
                return true;
        }

        if (other instanceof ExpressionColumn) {
            //如果为表达式列 不为字段表达式
            ExpressionColumn otherExpression = (ExpressionColumn) other;
            //目标为表达式列
            FieldExpression filedExp = (FieldExpression) otherExpression.getExpression();
            return filedExp != null && Objects.equals(this.source.toString(EDataSource.SqlServer), filedExp.getField().getSource().toString(EDataSource.SqlServer));
        }
        return false;
    }

    /**
     * 确定当前通配列是否逻辑蕴含指定表达式构建的投影列。
     * 判定规则：
     * （1）如果目标表达式不为字段表达式，判为不蕴含；
     * （2）如果当前列未指定通配范围，则蕴含所有字段表达式列；
     * （3）如果当前列指定了通配范围，其通配范围为S0，目标字段所属的源为S1，当S0.ToString(Select-Clause)==S1.
     * ToString(Select-Clause)时，判定为蕴含。
     *
     * @param otherExp 作为目标投影列的表达式。
     * @return 如果蕴含返回true，否则返回false。
     */
    public boolean implies(Expression otherExp) {
        if (!(otherExp instanceof FieldExpression)) return false;

        FieldExpression otherField = (FieldExpression) otherExp;

        //如果当前列未指定通配范围
        if (this.source == null) return true;

        return Objects.equals(this.source.toString(EDataSource.SqlServer), otherField.getField().getSource().toString(EDataSource.SqlServer));
    }
}
