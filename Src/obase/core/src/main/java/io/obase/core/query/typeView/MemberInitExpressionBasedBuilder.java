/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：基于MemberInitExpression的视图构造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 11:30:02
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

import io.obase.core.expression.*;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.StructuralType;
import io.obase.core.odm.objectSys.ParameterBinding;
import io.obase.core.odm.typeviews.TypeView;

/**
 * 基于MemberInitExpression的视图构造器
 */
public class MemberInitExpressionBasedBuilder implements ITypeViewBuilder {
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
    @Override
    public TypeView build(Expression viewExp, StructuralType source, ObjectDataModel model, ParameterExpression sourcePara, ParameterBinding... paraBindings) {
        if (viewExp instanceof MemberInitExpression) {
            MemberInitExpression initExp = (MemberInitExpression) viewExp;
            NewExpression newExp = initExp.getNewExpression();
            TypeView typeView = new NewExpressionBasedBuilder().build(newExp, source, model, sourcePara, paraBindings);
            ViewElementAdder adder = new ViewElementAdder(typeView, model);
            MemberBinding[] bindings = initExp.getMemberBindings();

            for (MemberBinding binding : bindings) {
                if (binding instanceof MemberAssignment) {
                    MemberAssignment assignment = (MemberAssignment) binding;
                    adder.addElement(assignment.getField(), assignment.getExpression(), paraBindings);
                }
            }
            return typeView;
        }

        throw new IllegalArgumentException("表达式不合法");
    }
}
