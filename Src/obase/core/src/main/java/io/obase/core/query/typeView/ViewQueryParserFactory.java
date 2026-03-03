/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：视图查询解析器工厂.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 12:24:37
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

import io.obase.core.query.GroupAggregationOp;
import io.obase.core.query.GroupOp;
import io.obase.core.query.QueryOp;
import io.obase.core.query.SelectOp;

/**
 * 视图查询解析器工厂
 */
public class ViewQueryParserFactory {

    /**
     * 针对指定的查询运算创建视图查询解析器实例
     *
     * @param queryOp 被解析的查询运算
     * @return 返回解析器实例。如果指定的查询运算不是视图查询返回null
     */
    public ViewQueryParser create(QueryOp queryOp) {
        ViewQueryParser parser = null;

        switch (queryOp.getName()) {
            case Select: {
                SelectOp op = (SelectOp) queryOp;
                //投影运算，如果ResultType是IEnumerable，且从投影表达式中抽取的关联树有子节点，实例化MultipleParser。
                if (op.getIsMultiple())
                    parser = new MultipleSelectionParser();
                    // 对于投影运算，如果IsNew==true，实例化NewSelectionParser。
                else if (op.getIsNew())
                    parser = new NewSelectionParser();
            }
            break;

            case Group: {
                //分组（聚合）运算
                if (queryOp instanceof GroupAggregationOp) {
                    GroupAggregationOp groupAggregationOp = (GroupAggregationOp) queryOp;
                    if (groupAggregationOp.getComparator() == null && groupAggregationOp.getIsNew())
                        parser = new GroupingAggregationParser();
                }
                //分组（普通）运算
                else if (queryOp instanceof GroupOp) {
                    GroupOp groupOp = (GroupOp) queryOp;
                    if (groupOp.getComparator() == null && groupOp.getElementSelector() != null)
                        parser = new GroupingParser();
                }
            }
            break;
        }

        return parser;
    }
}
