/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Having子句.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 17:00:50
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.providers.sql.EDataSource;

/**
 * 表示Having子句
 */
public class Having {

    /**
     * 作为过滤依据的表达式
     */
    private final Expression expression;

    /**
     * 创建表示依据指定的表达式进行筛选的Having实例
     *
     * @param expression 筛选条件的表达式
     */
    public Having(Expression expression) {
        this.expression = expression;
    }

    /**
     * 获取作为筛选条件的表达式
     *
     * @return 筛选条件的表达式
     */
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * Having的字符串表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    public String toString(EDataSource sourceType) {
        return " having " + this.expression.toString(sourceType);
    }
}
