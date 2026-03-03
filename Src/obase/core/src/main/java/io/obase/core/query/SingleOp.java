/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Single运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 14:26:55
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

/**
 * 表示Single运算
 */
public class SingleOp extends CriteriaContainOp {

    /**
     * 指示不满足条件时是否返回默认值
     */
    private final boolean returnDefault;

    /**
     * 创建SingleOp实例
     *
     * @param predicate     断言函数，用于测试元素是否满足条件
     * @param returnDefault 指示不满足条件时是否返回默认值
     */
    SingleOp(LambdaExpression predicate, boolean returnDefault, ObjectDataModel model) {
        super(EQueryOpName.Single, predicate, model);
        this.returnDefault = returnDefault;
    }

    /**
     * 创建SingleOp实例
     *
     * @param sourceType    查询源类型
     * @param returnDefault 指示不满足条件时是否返回默认值
     */
    SingleOp(Class<?> sourceType, boolean returnDefault) {
        super(EQueryOpName.Single, sourceType);

        this.returnDefault = returnDefault;
    }

    /**
     * 获取一个值，该值指示不满足条件时是否返回默认值
     *
     * @return 不满足条件时是否返回默认值
     */
    public boolean getReturnDefault() {
        return this.returnDefault;
    }

    /**
     * 结果类型
     *
     * @return 结果类型
     */
    @Override
    public Class<?> getResultType() {
        return this.getSourceType();
    }
}

