/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：别名根设置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 16:29:36
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.common.Utils;
import io.obase.providers.sql.sqlobject.Expression;
import io.obase.providers.sql.sqlobject.ExpressionVisitor;
import io.obase.providers.sql.sqlobject.FieldExpression;
import io.obase.providers.sql.sqlobject.SimpleSource;

/**
 * 别名根设置器，用于为SQL表达式中的源设置别名根，即在现有别名前加上前缀。
 */
public class SourceAliasRootSetter extends ExpressionVisitor {

    /**
     * 别名根，即要作为现有别名前缀的字符串
     */
    private final String aliasRoot;

    /**
     * 构造SourceAliasRootSetter的新实例
     *
     * @param aliasRoot 别名根，即要作为现有别名的前缀的字符串
     */
    public SourceAliasRootSetter(String aliasRoot) {
        if (Utils.getStringIsEmpty(aliasRoot))
            this.aliasRoot = null;
        else
            this.aliasRoot = aliasRoot;
    }

    /**
     * 访问字段表达式
     *
     * @param field 要访问的表达式
     * @return Expression 如果修改了该表达式或任何子表达式，则为修改后的表达式；否则返回原始表达式
     */
    @Override
    protected Expression visitField(FieldExpression field) {
        if (!Utils.getStringIsEmpty(this.aliasRoot) && field.getField().getSource() instanceof SimpleSource) {
            SimpleSource simpleSource = (SimpleSource) field.getField().getSource();
            simpleSource.setSymbolPrefix(this.aliasRoot);
        }

        return field;
    }
}
