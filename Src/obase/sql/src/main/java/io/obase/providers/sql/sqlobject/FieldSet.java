/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：字段集.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 16:43:11
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Utils;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 表示字段集。字段集由字段所属的查询源和名称列表组成。同一字段集中的字段必须属于同一个源
 */
public class FieldSet implements ISelectionSet {

    /**
     * 字段别名列表。别名列表为空表示不设置别名
     */
    private List<String> aliases;

    /**
     * 投影列集合
     */
    private List<SelectionColumn> columns;

    /**
     * 字段名称列表。名称列表为空表示该源下的所有字段
     */
    private List<String> names;

    /**
     * 源
     */
    private ISource source;

    /**
     * 创建字段集实例。该字段集表示指定源下的所有字段
     *
     * @param source 字段所属的源
     */
    public FieldSet(ISource source) {
        this.source = source;
    }

    /**
     * 创建字段集实例
     *
     * @param source 字段所属源的名称
     * @param names  字段的名称列表
     */
    public FieldSet(String source, List<String> names) {
        this(new SimpleSource(source), names);
    }

    /**
     * 创建字段集实例。该字段集表示指定源下的所有字段
     *
     * @param source 字段所属源的名称
     */
    public FieldSet(String source) {
        this(new SimpleSource(source));
    }

    /**
     * 创建字段集实例
     *
     * @param source 字段所属的源
     * @param names  字段的名称列表
     */
    public FieldSet(ISource source, List<String> names) {
        this(source);
        this.names = names;
    }

    /**
     * 获取字段的名称列表。名称列表为空表示该源下的所有字段
     *
     * @return 字段的名称列表
     */
    public List<String> getNames() {
        return this.names;
    }

    /**
     * 设置字段的名称列表。名称列表为空表示该源下的所有字段
     *
     * @param names 字段的名称列表
     */
    public void setNames(List<String> names) {
        this.names = names;
    }

    /**
     * 获取字段所属的源
     *
     * @return 源
     */
    public ISource getSource() {
        return this.source;
    }

    /**
     * 设置字段所属的源
     *
     * @param source 源
     */
    public void setSource(ISource source) {
        if (!(source instanceof SimpleSource))
            throw new IllegalArgumentException("字段的源只能为SimpleSource");
        this.source = source;
    }

    /**
     * 获取字段别名列表。别名列表为空表示不设置别名
     *
     * @return 字段别名列表
     */
    public List<String> getAliases() {
        return this.aliases;
    }

    /**
     * 设置字段别名列表。别名列表为空表示不设置别名
     *
     * @param aliases 字段别名列表
     */
    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    /**
     * 获取投影集中的投影列集合
     *
     * @return 投影列集合
     */
    @Override
    public List<SelectionColumn> getColumns() {
        if (this.columns != null) return this.columns;
        if (this.names != null) {
            List<SelectionColumn> returnColumns = new ArrayList<>();

            for (int i = 0; i < this.names.size(); i++) {
                String strAlias = "";
                if (this.aliases != null && this.aliases.size() > 0)
                    if (!Utils.getStringIsEmpty(this.aliases.get(i)))
                        strAlias = this.aliases.get(i);
                ExpressionColumn expressionColumn = new ExpressionColumn();
                expressionColumn.setExpression(Expression.field(new Field(this.names.get(i))));
                expressionColumn.setAlias(strAlias);
                returnColumns.add(expressionColumn);
            }

            return returnColumns;
        }

        WildcardColumn wildcardColumn = new WildcardColumn();
        wildcardColumn.setSource((MonomerSource) this.source);
        return new ArrayList<>(Collections.singleton(wildcardColumn));
    }

    /**
     * 向投影集中添加一列。注：如果列已存在则不执行任何操作。
     *
     * @param column 生成列的表达式
     */
    @Override
    public void add(SelectionColumn column) {
        if (column instanceof ExpressionColumn) {
            ExpressionColumn expressionColum = (ExpressionColumn) column;
            FieldExpression filedExp = (FieldExpression) expressionColum.getExpression();

            if (this.names == null) this.names = new ArrayList<>();

            if (this.aliases == null) this.aliases = new ArrayList<>();

            if (this.columns == null)
                this.columns = new ArrayList<>();

            WildcardColumn wildcardColumn = new WildcardColumn();
            wildcardColumn.setSource((MonomerSource) this.source);
            this.columns.add(wildcardColumn);

            this.names.add(filedExp.getField().getName());
            this.columns.add(expressionColum);
            this.aliases.add(expressionColum.getAlias());
        } else {
            this.names = null;
            this.aliases = null;
            this.columns = null;
        }
    }

    /**
     * 向投影集中添加一个不界定通配范围的通配列。注：如果列已存在则不执行任何操作。
     */
    @Override
    public void add() {
        this.names = null;
        this.aliases = null;
        this.columns = null;
    }

    /**
     * 向投影集中添加一个通配列，并界定其通配范围。注：如果列已存在则不执行任何操作。
     *
     * @param source 界定通配范围的源
     */
    @Override
    public void add(ISource source) {
        this.source = source;
        this.names = null;
        this.aliases = null;
        this.columns = null;
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
        column.setExpression(expression);
        column.setAlias(alias);

        if (this.names == null) {
            if (this.columns == null) this.columns = new ArrayList<>();

            WildcardColumn wildcardColumn = new WildcardColumn();
            wildcardColumn.setSource((MonomerSource) this.source);
            this.columns.add(wildcardColumn);
        }

        if (!this.getColumns().contains(column))
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
        column.setExpression(Expression.field(field));
        column.setAlias(alias);
        if (!this.getColumns().contains(column))
            this.add(column);
    }

    /**
     * 向投影集中添加一组列。注：如果某一列已存在则忽略它。
     *
     * @param columns 生成列的表达式构成的集合
     */
    @Override
    public void addRange(SelectionColumn[] columns) {
        for (SelectionColumn item : columns)
            if (!this.getColumns().contains(item))
                this.add(item);
    }

    /**
     * 向投影集中添加一组列。注：如果某一列已存在则忽略它。
     *
     * @param columns 生成列的表达式构成的集合
     * @param alias   列的别名构成的集合
     */
    @Override
    public void addRange(SelectionColumn[] columns, String[] alias) {
        for (SelectionColumn item : columns)
            if (!this.getColumns().contains(item))
                this.add(item);

        if (this.aliases == null || this.aliases.size() == 0) return;

        for (String item : alias)
            if (!this.aliases.contains(item))
                this.aliases.add(item);
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
        boolean result = false;

        if (expression instanceof FieldExpression) {
            FieldExpression filedExp = (FieldExpression) expression;
            if (this.names != null && this.names.size() > 0) {
                for (int i = 0; i < this.names.size(); i++)
                    if (Objects.equals(this.names.get(i), filedExp.getField().getName())) {
                        alias.realValue = this.aliases.get(i);
                        result = true;
                    }

                if (result) return true;
            }
        }

        //增加通配符列的判断
        for (SelectionColumn col : this.getColumns()) {
            if (!(col instanceof WildcardColumn))
                continue;
            WildcardColumn wild = (WildcardColumn) col;
            result = wild.implies(expression);
            if (result) break; //判断任意包含
        }

        return result;
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
        for (SelectionColumn col : this.getColumns()) {
            if (col instanceof ExpressionColumn) {
                ExpressionColumn expCol = (ExpressionColumn) col;
                result = expCol.equals(column);
                if (result) break; //判断任意包含
            }

            if (col instanceof WildcardColumn) {
                WildcardColumn wildCol = (WildcardColumn) col;
                result = wildCol.implies(column);
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
        //如果为MonomerSource简单源则设置别名
        if (this.getSource() instanceof MonomerSource) {
            MonomerSource source = (MonomerSource) this.getSource();
            source.setSymbolPrefix(prefix);
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
        if (this.source instanceof SimpleSource) {
            SimpleSource simpleSource = (SimpleSource) this.source;
            String result;

            if (this.columns != null) {
                List<String> names = this.columns.stream().map(p -> p.toString(sourceType)).collect(Collectors.toList());
                return String.join(",", names);
            }

            if (this.names != null) {
                List<String> names = this.names.stream().map(s -> {
                    if (this.getAliases() != null && this.getAliases().size() == this.getNames().size()) {
                        if (this.source != null)
                            return ((Utils.getStringIsEmpty(simpleSource.getSymbol()))
                                    ? this.source.toString(sourceType)
                                    : simpleSource.getSymbol()) + "." + s + " as " + this.getAliases().get(this.getNames().indexOf(s));
                        return s + " as " + this.getAliases().get(this.getNames().indexOf(s));
                    }

                    if (this.source != null)
                        return ((Utils.getStringIsEmpty(simpleSource.getSymbol()))
                                ? this.source.toString(sourceType)
                                : simpleSource.getSymbol()) + "." + s;
                    return s;

                }).collect(Collectors.toList());

                result = String.join(",", names);
            } else {
                result = (Utils.getStringIsEmpty(simpleSource.getSymbol())
                        ? this.source.toString(sourceType)
                        : simpleSource.getSymbol()) + ".*";

            }

            return result;
        }

        return null;
    }
}
