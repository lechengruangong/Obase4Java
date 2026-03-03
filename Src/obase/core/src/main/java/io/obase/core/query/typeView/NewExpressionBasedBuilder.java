/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：特定于实例化投影运算的视图查询解析器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 11:27:58
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

import io.obase.core.common.Utils;
import io.obase.core.expression.Expression;
import io.obase.core.expression.NewExpression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.InstanceConstructor;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.ReflectionConstructor;
import io.obase.core.odm.StructuralType;
import io.obase.core.odm.objectSys.ParameterBinding;
import io.obase.core.odm.typeviews.TypeView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;
import java.util.Objects;

/**
 * 基于NewExpression的视图构造器
 */
public class NewExpressionBasedBuilder implements ITypeViewBuilder {
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
        TypeView typeView = new TypeView(source, viewExp.getType(), sourcePara);

        NewExpression newExp = (NewExpression) viewExp;

        Constructor<?> constructorInfo = newExp.getConstructor();
        Parameter[] paraInfos = constructorInfo.getParameters();

        InstanceConstructor constructor = new ReflectionConstructor(constructorInfo);
        typeView.setConstructor(constructor);

        ViewElementAdder adder = new ViewElementAdder(typeView, model);

        if (newExp.getMembers() != null && newExp.getMembers().length > 0) {
            for (int i = 0; i < newExp.getMembers().length; i++) {
                Member member = newExp.getMembers().length > i ? newExp.getMembers()[i] : null;
                Expression arg = newExp.getArgument().length > i ? newExp.getArgument()[i] : null;
                Parameter paraInfo = paraInfos.length > i ? paraInfos[i] : null;
                if (member == null || arg == null || paraInfo == null) continue;
                adder.addElement(newExp.getMembers()[i], newExp.getArgument()[i], paraBindings);

                constructor.setParameter(newExp.getMembers()[i].getName(), paraInfos[i].getName(), value -> {
                    Class<?> tValueType = arg.getType();
                    value = Utils.convertDbValue(value, tValueType);

                    return value;
                }, null);
            }
        }
        if (paraInfos.length > 0) {
            //加入构造函数参数
            for (int i = 0; i < paraInfos.length; i++) {
                Parameter arg = paraInfos[i];
                if (constructor.getParameters() != null) {
                    if (constructor.getParameters().stream().anyMatch(p -> Objects.equals(p.getName(), arg.getName())))
                        continue;
                }
                constructor.setParameter(paraInfos[i].getName(), paraInfos[i].getName(), value -> {
                    Class<?> tValueType = arg.getType();
                    value = Utils.convertDbValue(value, tValueType);

                    return value;
                }, newExp.getArgument()[i]);
            }
        }

        typeView.addParameterBinding(paraBindings);
        return typeView;
    }
}
