/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表达式委托库.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 16:25:56
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.oop;

import io.obase.core.expression.Expression;
import io.obase.core.expression.IFunc;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.GlobalDelegateValueGetterCache;

import java.util.HashMap;
import java.util.Map;

/**
 * 表达式委托库，存储表达式经编译生成的委托，可以根据表达式获取委托
 */
public class ExpressionDelegates {

    /**
     * 获取当前应用程序域中的ExpressionDelegates实例
     */
    private static volatile ExpressionDelegates instance;

    /**
     * 表达式委托字典
     */
    private final Map<Expression, IFunc> delegates = new HashMap<>();

    /**
     * 初始化ExpressionDelegates的新实例
     */
    private ExpressionDelegates() {
    }

    /**
     * 获取当前应用程序域中的ExpressionDelegates实例
     *
     * @return 当前应用程序域中的ExpressionDelegates实例
     */
    public static ExpressionDelegates getInstance() {
        if (instance == null) {
            synchronized (ExpressionDelegates.class) {
                instance = new ExpressionDelegates();
            }
        }
        return instance;
    }

    /**
     * 获取指定表达式经编译生成的委托
     *
     * @param expression 表达式
     * @return 表达式委托
     */
    public IFunc get(Expression expression) {
        synchronized (GlobalDelegateValueGetterCache.class) {
            if (!this.delegates.containsKey(expression)) {
                this.delegates.put(expression, ((LambdaExpression) expression).compile());
            }
            return this.delegates.get(expression);
        }
    }
}

