/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：定义查询投影集规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 16:38:57
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.List;

/**
 * 定义查询投影集规范
 */
public interface ISelectionSet {

    /**
     * 获取投影集中的投影列集合
     *
     * @return 投影列集合
     */
    List<SelectionColumn> getColumns();

    /**
     * 向投影集中添加一列。注：如果列已存在则不执行任何操作。
     *
     * @param column 生成列的表达式
     */
    void add(SelectionColumn column);

    /**
     * 向投影集中添加一个不界定通配范围的通配列。注：如果列已存在则不执行任何操作。
     */
    void add();

    /**
     * 向投影集中添加一个通配列，并界定其通配范围。注：如果列已存在则不执行任何操作。
     *
     * @param source 界定通配范围的源
     */
    void add(ISource source);

    /**
     * 向投影集添加投影列，该列以指定的表达式作为投影表达式。注：如果列已存在则不执行任何操作
     *
     * @param expression 投影表达式
     */
    void add(Expression expression);

    /**
     * 向投影集添加投影列，该列以指定的表达式作为投影表达式。注：如果列已存在则不执行任何操作。
     *
     * @param expression 投影表达式
     * @param alias      投影列的别名
     */
    void add(Expression expression, String alias);

    /**
     * 向投影集添加投影列，该列为指定的字段。注：如果列已存在则不执行任何操作。
     *
     * @param field 作为投影列的字段
     */
    void add(Field field);

    /**
     * 向投影集添加投影列，该列为指定的字段。注：如果列已存在则不执行任何操作。
     *
     * @param field 作为投影列的字段
     * @param alias 投影列的别名
     */
    void add(Field field, String alias);

    /**
     * 向投影集中添加一组列。注：如果某一列已存在则忽略它。
     *
     * @param columns 生成列的表达式构成的集合
     */
    void addRange(SelectionColumn[] columns);

    /**
     * 向投影集中添加一组列。注：如果某一列已存在则忽略它。
     *
     * @param columns 生成列的表达式构成的集合
     * @param alias   列的别名构成的集合
     */
    void addRange(SelectionColumn[] columns, String[] alias);

    /**
     * 确定投影集是否包含与指定的表达式相对应的列，同时返回该列的别名
     *
     * @param expression 指定的表达式
     * @param alias      返回相应列的别名
     * @return 是否包含
     */
    boolean contains(Expression expression, ObjectReferencePack<String> alias);

    /**
     * 确定投影集是否包含指定的列
     *
     * @param column 指定的表达式
     * @return 是否包含
     */
    boolean contains(SelectionColumn column);

    /**
     * 为各投影列涉及到的源的别名设置前缀
     *
     * @param prefix 别名前缀
     */
    void setSourceAliasPrefix(String prefix);

    /**
     * 生成投影集的文本表示形式，该文本可直接用于Select子句。
     *
     * @return 文本表示形式
     */
    String toString();

    /**
     * 针对指定的数据源类型，生成投影集的文本表示形式，该文本可直接用于Select子句。
     *
     * @param sourceType 数据源类型
     * @return 文本表示形式
     */
    String toString(EDataSource sourceType);
}
