/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：视图查询解析器基类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 10:59:57
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

import io.obase.core.IdentityArray;
import io.obase.core.odm.ImpliedTypeManager;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.StructuralType;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.query.QueryOp;

/**
 * 为视图查询解析器提供基础实现。
 * 视图查询是指一种查询运算，在运算过程中会将源类型投影（Select）成视图，它可能是投影运算,也可能是逻辑蕴涵投影运算的其它运算。
 * 视图查询解析器的用途是分析查询运算从中解析出视图类型。
 */
public abstract class ViewQueryParser {

    /**
     * 创建视图实例
     *
     * @param queryOp    要解析的查询运算
     * @param viewType   视图的CLR类型
     * @param sourceType 视图源的CLR类型
     * @param model      对象数据模型
     * @return 创建的类型视图
     */
    protected abstract TypeView createView(QueryOp queryOp, Class<?> viewType, Class<?> sourceType, ObjectDataModel model);

    /**
     * 从查询运算抽取视图源的CLR类型
     *
     * @param queryOp 要解析的查询运算
     * @return 抽取的CLR类型
     */
    protected abstract Class<?> extractSourceType(QueryOp queryOp);

    /**
     * 从查询运算抽取视图的CLR类型
     *
     * @param queryOp 要解析的查询运算
     * @return 抽取的视图CLR类型
     */
    protected abstract Class<?> extractViewType(QueryOp queryOp);

    /**
     * 执行解析操作
     *
     * @param queryOp 要解析的查询运算
     * @param model   对象数据模型
     * @return 解析出的类型视图
     */
    public TypeView parse(QueryOp queryOp, ObjectDataModel model) {
        Class<?> candidateType = this.extractViewType(queryOp);
        Class<?> sourceType = this.extractSourceType(queryOp);
        StructuralType modelType = model.getStructuralType(candidateType);
        Class<?> derivedType = null;

        if (modelType instanceof TypeView) {
            TypeView typeView = (TypeView) modelType;

            if (typeView.getSource().getClrType() == sourceType) return typeView;

            //从缓存中获取派生类型
            derivedType =
                    ImpliedTypeManager.getCurrent().applyType(candidateType,
                            new IdentityArray(typeView.getSource().getFullName()), null);
            //从模型中获取模型视图
            TypeView modelTypeView = model.getTypeView(derivedType);
            if (modelTypeView != null) return modelTypeView;
        }
        //创建视图。
        TypeView view = this.createView(queryOp, candidateType, sourceType, model);
        view.setModel(model);
        //缓存生成的派生类型
        if (derivedType != null) view.setProxyType(derivedType);
        //将视图添加到模型
        model.addType(view);
        return view;
    }
}
