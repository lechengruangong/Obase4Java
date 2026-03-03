/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：基于表达式的视图查询解析器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-6-30 10:32:03
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

import io.obase.common.ObjectReferencePack;
import io.obase.core.expression.Expression;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.FieldDescriptor;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.StructuralType;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.ParameterBinding;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.query.QueryOp;

import java.lang.reflect.Method;

/**
 * 为基于表达式的视图查询解析器提供基础实现
 */
public abstract class ExpressionBasedViewQueryParser extends ViewQueryParser {

    /**
     * 抽取到的类型视图实例
     */
    private TypeView typeView;

    /**
     * 获取视图引用的方法
     *
     * @param viewRef         视图引用
     * @param fields          字段描述符
     * @param typeImpliedView 视图
     * @param index           索引
     * @return 视图引用的方法
     */
    protected static Method getViewRefMethod(FieldDescriptor viewRef, FieldDescriptor[] fields, Class<?> typeImpliedView, int[] index) {
        viewRef.getName(() -> {
            FieldDescriptor field = fields[index[0]];
            //字段前半部分
            String filedStart = field.getHasGetter() || field.getHasSetter() ? "_field_" : "Field_";
            return filedStart + (++index[0]);
        });

        String refPropertyName = viewRef.getPropertyName();
        try {
            return typeImpliedView.getMethod("set" + refPropertyName, viewRef.getType());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("无法获取视图的引用", e);
        }
    }

    /**
     * 创建视图实例
     *
     * @param queryOp    要解析的查询运算
     * @param viewType   视图的CLR类型
     * @param sourceType 视图源的CLR类型
     * @param model      对象数据模型
     * @return 创建的类型视图
     */
    @Override
    protected TypeView createView(QueryOp queryOp, Class<?> viewType, Class<?> sourceType, ObjectDataModel model) {
        if (this.typeView != null) return this.typeView;
        StructuralType source = model.getStructuralType(this.extractSourceType(queryOp));
        LambdaExpression lambda = this.extractViewExpression(queryOp, viewType);
        ParameterBinding[] paraBindings = this.extractParameterBinding(queryOp);

        ITypeViewBuilder builder = new TypeViewBuilderFactory().create(lambda);
        TypeView typeView = builder.build(lambda.getBody(), source, model, Expression.parameter("", sourceType), paraBindings);
        typeView.setKeyAttributes(this.extractKeyAttributes(queryOp));

        LambdaExpression lambda1 = this.extractFlatteningExpression(queryOp, new ObjectReferencePack<>());
        if (lambda1 != null) {
            ObjectReferencePack<AssociationTreeNode> assoTail = new ObjectReferencePack<>();
            lambda1.getBody().extractAssociation(model, assoTail, paraBindings);
            if (assoTail.realValue != null) typeView.addFlatteningPoint(assoTail.realValue, false);
        }

        return this.typeView == null ? (this.typeView = typeView) : this.typeView;
    }

    /**
     * 从查询运算抽取代表平展点的表达式
     *
     * @param queryOp        要解析的查询运算
     * @param flatteningPara 返回平展形参
     * @return 代表平展点的表达式
     */
    protected abstract LambdaExpression extractFlatteningExpression(QueryOp queryOp, ObjectReferencePack<ParameterExpression> flatteningPara);

    /**
     * 从查询运算抽取视图的标识属性
     *
     * @param queryOp 要解析的查询运算
     * @return 标识属性
     */
    protected abstract String[] extractKeyAttributes(QueryOp queryOp);

    /**
     * 从查询运算抽取视图表达式涉及的形参绑定
     *
     * @param queryOp 要解析的查询运算
     * @return 形参绑定
     */
    protected abstract ParameterBinding[] extractParameterBinding(QueryOp queryOp);

    /**
     * 从查询运算抽取描述视图结构的Lambda表达式（简称视图表达式），后续将据此表达式构造TypeView实例。
     *
     * @param queryOp  要解析的查询运算
     * @param viewType 视图的CLR类型
     * @return 视图表达式
     */
    protected abstract LambdaExpression extractViewExpression(QueryOp queryOp, Class<?> viewType);
}
