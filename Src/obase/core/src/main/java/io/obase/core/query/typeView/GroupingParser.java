/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：特定于（普通）分组运算的视图查询解析器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 11:46:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

import io.obase.common.ObjectReferencePack;
import io.obase.core.IdentityArray;
import io.obase.core.expression.*;
import io.obase.core.odm.FieldDescriptor;
import io.obase.core.odm.ImpliedTypeManager;
import io.obase.core.odm.objectSys.ParameterBinding;
import io.obase.core.query.GroupOp;
import io.obase.core.query.QueryOp;

import java.lang.reflect.Method;

/**
 * 特定于（普通）分组运算的视图查询解析器。
 * 分组运算是指GroupBy(keySelector, elementSelector)，其中：
 * （1）keySelector为键选择器，类型为Func`2[TSource, TKey];
 * （2）elementSelector为组元素选择器，类型为Func`2[TSource, TElement]。
 */
public class GroupingParser extends ExpressionBasedViewQueryParser {
    /**
     * 从查询运算抽取代表平展点的表达式
     *
     * @param queryOp        要解析的查询运算
     * @param flatteningPara 返回平展形参
     * @return 代表平展点的表达式
     */
    @Override
    protected LambdaExpression extractFlatteningExpression(QueryOp queryOp, ObjectReferencePack<ParameterExpression> flatteningPara) {
        flatteningPara.realValue = null;
        return null;
    }

    /**
     * 从查询运算抽取视图的标识属性
     *
     * @param queryOp 要解析的查询运算
     * @return 标识属性
     */
    @Override
    protected String[] extractKeyAttributes(QueryOp queryOp) {
        return new String[0];
    }

    /**
     * 从查询运算抽取视图表达式涉及的形参绑定
     *
     * @param queryOp 要解析的查询运算
     * @return 形参绑定
     */
    @Override
    protected ParameterBinding[] extractParameterBinding(QueryOp queryOp) {
        return new ParameterBinding[0];
    }

    /**
     * 从查询运算抽取描述视图结构的Lambda表达式（简称视图表达式），后续将据此表达式构造TypeView实例。
     *
     * @param queryOp  要解析的查询运算
     * @param viewType 视图的CLR类型
     * @return 视图表达式
     */
    @Override
    protected LambdaExpression extractViewExpression(QueryOp queryOp, Class<?> viewType) {
        if (!(queryOp instanceof GroupOp))
            throw new IllegalArgumentException("GroupingParser从查询运算抽取视图表达式失败,查询操作不是GroupOp");

        GroupOp groupOp = (GroupOp) queryOp;

        //创建隐含视图
        Class<?> baseType = SelectionResult.class;
        ParameterBinding[] bindings = this.extractParameterBinding(queryOp);
        FieldDescriptor viewAttr = new FieldDescriptor(groupOp.getKeySelector(), bindings);
        viewAttr.setHasGetter(true);
        viewAttr.setHasSetter(true);
        viewAttr.setCreateConstructorParameter(queryOp.getHeterogeneous(null));
        FieldDescriptor viewRef = new FieldDescriptor(groupOp.getElementSelector(), bindings);
        viewRef.setHasGetter(true);
        viewRef.setHasSetter(true);
        viewRef.setCreateConstructorParameter(queryOp.getHeterogeneous(null));

        FieldDescriptor[] fields = new FieldDescriptor[2];
        fields[0] = viewAttr;
        fields[1] = viewRef;

        IdentityArray subIdentity = new IdentityArray(groupOp.getSourceType().getName() + "[" + viewAttr.getValueExpression().getType().getName() + "]" + "[" + viewRef.getValueExpression().getType().getName() + "]");
        Class<?> typeImpliedView = ImpliedTypeManager.getCurrent().applyType(baseType, fields, subIdentity, null);

        //源
        Class<?>[] parameterTypes = new Class<?>[]{viewAttr.getType(), viewRef.getType()};

        //统一表达式参数
        ParameterExpression parameterExpression = groupOp.getKeySelector().getParameters()[0];
        ParameterVisitor visitor = new ParameterVisitor(parameterExpression);
        LambdaExpression newElementSelector = (LambdaExpression) visitor.visit(groupOp.getElementSelector());
        NewExpression newExpression;
        if (queryOp.getHeterogeneous(null)) {
            newExpression = Expression.news(typeImpliedView);
            try {
                newExpression.setConstructor(typeImpliedView.getConstructor(parameterTypes));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("无法找到视图的构造函数", e);
            }
        } else {
            newExpression = Expression.news(typeImpliedView);
            newExpression.setConstructor(typeImpliedView.getConstructors()[0]);
        }


        final int[] index = {0};

        //视图属性
        viewAttr.getName(() -> {
            FieldDescriptor field = fields[index[0]];
            //字段前半部分
            String filedStart = (field.getHasGetter() || field.getHasSetter()) ? "_field_" : "Field_";
            return filedStart + (++index[0]);
        });

        String artPropertyName = viewAttr.getPropertyName();
        Method attrMethod;
        try {
            attrMethod = typeImpliedView.getMethod("set" + artPropertyName, viewAttr.getType());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("无法获取视图的属性", e);
        }
        MemberAssignment attrBindExp = Expression.bind(attrMethod, groupOp.getKeySelector().getBody());

        //视图引用
        MemberAssignment refBindExp = Expression.bind(getViewRefMethod(viewRef, fields, typeImpliedView, index), newElementSelector.getBody());
        MemberInitExpression memberInitExp = Expression.memberInit(newExpression, new MemberBinding[]{attrBindExp, refBindExp});

        return Expression.lambda(new ParameterExpression[]{parameterExpression}, memberInitExp);
    }

    /**
     * 从查询运算抽取视图源的CLR类型
     *
     * @param queryOp 要解析的查询运算
     * @return 抽取的CLR类型
     */
    @Override
    protected Class<?> extractSourceType(QueryOp queryOp) {
        return queryOp.getSourceType();
    }

    /**
     * 从查询运算抽取视图的CLR类型
     *
     * @param queryOp 要解析的查询运算
     * @return 抽取的视图CLR类型
     */
    @Override
    protected Class<?> extractViewType(QueryOp queryOp) {
        if (queryOp instanceof GroupOp) {
            GroupOp groupOp = (GroupOp) queryOp;
            return groupOp.getElementType();
        }
        return null;
    }

    /**
     * 参数访问器
     * 替换表达式参数为指定参数
     */
    private static class ParameterVisitor extends ExpressionVisitor {

        /**
         * 替换的参数表达式
         */
        private final ParameterExpression parExp;

        /**
         * 通过指定参数实例化ParameterVisitor
         *
         * @param parExp 指定参数
         */
        ParameterVisitor(ParameterExpression parExp) {
            this.parExp = parExp;
        }

        /**
         * 默认的访问参数表达式
         * 直接返回自身
         *
         * @param parameterExpression 参数表达式
         * @return 常量表达式自身
         */
        @Override
        protected Expression visitParameter(ParameterExpression parameterExpression) {
            return this.parExp;
        }
    }
}
