/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：投影集.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 16:53:28
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表示集，即Select子句所确定的结果列
 */
public class SelectionSet implements ISelectionSet {

    /**
     * 投影列集合
     */
    private final List<SelectionColumn> columns;

    /**
     * 创建SelectionSet的实例
     */
    public SelectionSet() {
        this.columns = new ArrayList<>();
    }

    /**
     * 使用指定的投影列创建SelectionSet的实例
     *
     * @param column 投影集包含的投影列
     */
    public SelectionSet(SelectionColumn column) {
        this.columns = new ArrayList<>();
        this.columns.add(column);
    }

    /**
     * 使用指定的投影列集合创建SelectionSet的实例
     *
     * @param columns 投影集包含的投影列的集合
     */
    public SelectionSet(List<SelectionColumn> columns) {
        this.columns = columns;
    }

    /**
     * 获取投影集中的投影列集合
     *
     * @return 投影列集合
     */
    @Override
    public List<SelectionColumn> getColumns() {
        return this.columns;
    }

    /**
     * 向投影集中添加一列。注：如果列已存在则不执行任何操作。
     *
     * @param column 生成列的表达式
     */
    @Override
    public void add(SelectionColumn column) {
        if (!this.columns.contains(column))
            this.columns.add(column);
    }

    /**
     * 向投影集中添加一个不界定通配范围的通配列。注：如果列已存在则不执行任何操作。
     */
    @Override
    public void add() {
        this.columns.add(new WildcardColumn());
    }

    /**
     * 向投影集中添加一个通配列，并界定其通配范围。注：如果列已存在则不执行任何操作。
     *
     * @param source 界定通配范围的源
     */
    @Override
    public void add(ISource source) {
        WildcardColumn wildcardColumn = new WildcardColumn();
        wildcardColumn.setSource((MonomerSource) source);
        this.columns.add(wildcardColumn);
    }

    /**
     * 向投影集添加投影列，该列以指定的表达式作为投影表达式。注：如果列已存在则不执行任何操作
     *
     * @param expression 投影表达式
     */
    @Override
    public void add(Expression expression) {
        this.add(expression, null);
    }

    /**
     * 向投影集添加投影列，该列以指定的表达式作为投影表达式。注：如果列已存在则不执行任何操作。
     *
     * @param expression 投影表达式
     * @param alias      投影列的别名
     */
    @Override
    public void add(Expression expression, String alias) {
        ExpressionColumn column = new ExpressionColumn();
        column.setAlias(alias);
        column.setExpression(expression);
        this.add(column);
    }

    /**
     * 向投影集添加投影列，该列为指定的字段。注：如果列已存在则不执行任何操作。
     *
     * @param field 作为投影列的字段
     */
    @Override
    public void add(Field field) {
        this.add(field, null);
    }

    /**
     * 向投影集添加投影列，该列为指定的字段。注：如果列已存在则不执行任何操作。
     *
     * @param field 作为投影列的字段
     * @param alias 投影列的别名
     */
    @Override
    public void add(Field field, String alias) {
        ExpressionColumn column = new ExpressionColumn();
        column.setAlias(alias);
        column.setExpression(Expression.field(field));
        this.add(column);
    }

    /**
     * 向投影集中添加一组列。注：如果某一列已存在则忽略它。
     *
     * @param columns 生成列的表达式构成的集合
     */
    @Override
    public void addRange(SelectionColumn[] columns) {
        for (SelectionColumn item : columns) {
            this.add(item);
        }
    }

    /**
     * 向投影集中添加一组列。注：如果某一列已存在则忽略它。
     *
     * @param columns 生成列的表达式构成的集合
     * @param alias   列的别名构成的集合
     */
    @Override
    public void addRange(SelectionColumn[] columns, String[] alias) {
        for (SelectionColumn item : columns) {
            this.add(item);
        }
    }

    /**
     * 确定投影集是否包含与指定的表达式相对应的列，同时返回该列的别名
     *
     * @param expression 指定的表达式
     * @param alias      返回相应列的别名
     * @return 是否包含
     */
    @Override
    public boolean contains(Expression expression, ObjectReferencePack<String> alias) {
        alias.realValue = null;

        for (SelectionColumn item : this.columns) {
            if (item instanceof ExpressionColumn) {
                ExpressionColumn expressionColumn = (ExpressionColumn) item;
                if (expression == expressionColumn.getExpression()) {
                    alias.realValue = expressionColumn.getAlias();
                    return true;
                }
            }

            if (item instanceof WildcardColumn) {
                WildcardColumn wildCol = (WildcardColumn) item;
                if (wildCol.implies(expression)) {
                    if (expression instanceof FieldExpression) {
                        //表示已包含在通配符列
                        FieldExpression fieldExp = (FieldExpression) expression;
                        alias.realValue = fieldExp.getField().getName();
                        return true;
                    }

                }
            }
        }

        return false;
    }

    /**
     * 确定投影集是否包含指定的列
     *
     * @param column 指定的表达式
     * @return 是否包含
     */
    @Override
    public boolean contains(SelectionColumn column) {
        boolean result = false;

        for (SelectionColumn item : this.getColumns()) {
            if (item instanceof WildcardColumn) {
                WildcardColumn wildCol = (WildcardColumn) item;
                result = wildCol.implies(column);
                if (result) break; //判断任意包含
            }

            if (item instanceof ExpressionColumn) {
                ExpressionColumn expCol = (ExpressionColumn) item;
                result = expCol.equals(column);
                if (result) break; //判断任意包含
            }
        }
        return result;
    }

    /**
     * 为各投影列涉及到的源的别名设置前缀
     *
     * @param prefix 别名前缀
     */
    @Override
    public void setSourceAliasPrefix(String prefix) {
        for (SelectionColumn item : this.getColumns()) {
            item.setSourceAliasPrefix(prefix);
        }
    }

    /**
     * 生成投影集的文本表示形式，该文本可直接用于Select子句。
     *
     * @return 文本表示形式
     */
    @Override
    public String toString() {
        return this.toString(EDataSource.SqlServer);
    }

    /**
     * 针对指定的数据源类型，生成投影集的文本表示形式，该文本可直接用于Select子句。
     *
     * @param sourceType 数据源类型
     * @return 文本表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {
        return this.getColumns().stream().map(p -> p.toString(sourceType)).collect(Collectors.joining(","));
    }
}
