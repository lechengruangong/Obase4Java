/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：公开构造类型视图的方法.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 11:25:29
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

import io.obase.core.expression.Expression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.StructuralType;
import io.obase.core.odm.objectSys.ParameterBinding;
import io.obase.core.odm.typeviews.TypeView;

/**
 * 公开构造类型视图的方法
 */
public interface ITypeViewBuilder {

    /**
     * 构造类型视图
     *
     * @param viewExp      视图表达式
     * @param source       视图源
     * @param model        对象数据模型
     * @param sourcePara   视图表达式中代表视图源的形式参数
     * @param paraBindings 形参绑定
     * @return 类型视图
     */
    TypeView build(Expression viewExp, StructuralType source, ObjectDataModel model, ParameterExpression sourcePara, ParameterBinding... paraBindings);
}
