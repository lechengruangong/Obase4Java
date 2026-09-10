/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Sql别名收集器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-9-2 09:56:43
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.common;

import io.obase.core.common.Utils;
import io.obase.providers.sql.sqlobject.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Sql别名收集器。
 * 遍历Sql对象表示法（查询源树、投影集及嵌套子查询），收集其中出现的所有别名（表别名、列别名、派生表符号），
 * 供Sql别名替换器在生成Sql字符串后统一替换。收集不改变任何对象的发射逻辑。
 */
public class SqlAliasCollector {

    /**
     * 私有构造 防止外部初始化
     */
    private SqlAliasCollector() {
    }

    /**
     * 收集指定查询Sql对象表示法中出现的所有别名。
     *
     * @param querySql 查询Sql对象
     * @return 别名集合
     */
    public static Set<String> collect(QuerySql querySql) {
        Set<String> aliases = new HashSet<>();
        collect(querySql, aliases);
        return aliases;
    }

    /**
     * 收集指定修改Sql对象表示法中出现的所有别名。
     *
     * @param changeSql 修改Sql对象
     * @return 别名集合
     */
    public static Set<String> collect(ChangeSql changeSql) {
        Set<String> aliases = new HashSet<>();
        if (changeSql == null) return aliases;
        collectSource(changeSql.getSource(), aliases);
        collectSource(changeSql.getTargetSource(), aliases);
        return aliases;
    }

    /**
     * 收集查询Sql对象表示法中的别名。
     *
     * @param querySql 查询Sql对象
     * @param aliases  别名集合
     */
    private static void collect(QuerySql querySql, Set<String> aliases) {
        if (querySql == null) return;
        //查询源（含嵌套子查询与集运算）
        collectSource(querySql.getSource(), aliases);
        //投影集
        collectSelectionSet(querySql.getSelectionSet(), aliases);
    }

    /**
     * 收集查询源及其嵌套子查询中的别名。
     *
     * @param source  查询源
     * @param aliases 别名集合
     */
    private static void collectSource(ISource source, Set<String> aliases) {
        if (source == null) return;
        if (source instanceof SimpleSource) {
            SimpleSource simpleSource = (SimpleSource) source;
            if (!Utils.getStringIsEmpty(simpleSource.getAlias())) aliases.add(simpleSource.getAlias());
            return;
        }
        if (source instanceof SelectSource) {
            SelectSource selectSource = (SelectSource) source;
            if (!Utils.getStringIsEmpty(selectSource.getSymbol())) aliases.add(selectSource.getSymbol());
            collect(selectSource.getQuerySql(), aliases);
            return;
        }
        if (source instanceof SetSource) {
            SetSource setSource = (SetSource) source;
            if (!Utils.getStringIsEmpty(setSource.getSymbol())) aliases.add(setSource.getSymbol());
            collect(setSource.getQuerySet(), aliases);
            return;
        }
        if (source instanceof JoinedSource) {
            JoinedSource joinedSource = (JoinedSource) source;
            for (ISource subSource : joinedSource.getSources())
                collectSource(subSource, aliases);
        }
    }

    /**
     * 收集集运算操作数中的别名。
     *
     * @param operand 集运算操作数
     * @param aliases 别名集合
     */
    private static void collect(ISetOperand operand, Set<String> aliases) {
        if (operand == null) return;
        if (operand instanceof QuerySql) {
            collect((QuerySql) operand, aliases);
            return;
        }
        if (operand instanceof QuerySet) {
            collect((QuerySet) operand, aliases);
        }
    }

    /**
     * 收集集运算中的别名。
     *
     * @param querySet 集运算
     * @param aliases  别名集合
     */
    private static void collect(QuerySet querySet, Set<String> aliases) {
        if (querySet == null) return;
        collect(querySet.getLeft(), aliases);
        collect(querySet.getRight(), aliases);
    }

    /**
     * 收集投影集中的别名。
     *
     * @param selectionSet 投影集
     * @param aliases      别名集合
     */
    private static void collectSelectionSet(ISelectionSet selectionSet, Set<String> aliases) {
        if (selectionSet == null) return;
        if (selectionSet instanceof FieldSet) {
            FieldSet fieldSet = (FieldSet) selectionSet;
            if (fieldSet.getAliases() != null)
                for (String alias : fieldSet.getAliases())
                    if (!Utils.getStringIsEmpty(alias))
                        aliases.add(alias);
            collectSource(fieldSet.getSource(), aliases);
            return;
        }
        for (SelectionColumn column : selectionSet.getColumns()) {
            if (column instanceof ExpressionColumn) {
                ExpressionColumn expressionColumn = (ExpressionColumn) column;
                if (!Utils.getStringIsEmpty(expressionColumn.getAlias()))
                    aliases.add(expressionColumn.getAlias());
            } else if (column instanceof WildcardColumn) {
                collectSource(((WildcardColumn) column).getSource(), aliases);
            }
        }
    }
}
