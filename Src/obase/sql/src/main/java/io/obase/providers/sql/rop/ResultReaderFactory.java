/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：结果读取器工厂.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 11:31:09
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.IAttachObject;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.providers.sql.ISqlExecutor;

import java.sql.ResultSet;

/**
 * 结果读取器工厂
 */
public class ResultReaderFactory {

    /**
     * 创建具体的结果读取器
     *
     * @param dataReader    数据集阅读器
     * @param resultType    结果的类型
     * @param includingTree 包含树
     * @param attachObj     对象附加委托
     * @return 结果读取器
     */
    public Iterable<?> create(ResultSet dataReader, TypeBase resultType, AssociationTree includingTree, IAttachObject attachObj, boolean attachRoot, ISqlExecutor sqlExecutor) {
        Iterable<?> reader = null;
        if (resultType instanceof PrimitiveType) {
            reader = new ValueReader<>(dataReader, resultType.getClrType(), sqlExecutor);
        } else if (resultType instanceof ObjectType) {
            ObjectType objectType = (ObjectType) resultType;
            reader = new ObjectReader<>(objectType, includingTree, dataReader, attachObj, resultType.getClrType(), attachRoot, sqlExecutor);
        } else if (resultType instanceof ComplexType) {
            ComplexType complexType = (ComplexType) resultType;
            reader = new ComplexTypeInstanceReader<>(complexType, dataReader, resultType.getClrType(), sqlExecutor);
        } else if (resultType instanceof TypeView) {
            TypeView typeView = (TypeView) resultType;
            if (typeView.getViewReferences().length > 0) {
                reader = new ObjectReader<>((ReferringType) resultType, includingTree, dataReader, attachObj, resultType.getClrType(), attachRoot, sqlExecutor);
            } else {
                reader = new SimpleTypeViewInstanceReader<>(typeView, dataReader, resultType.getClrType(), sqlExecutor);
            }
        }

        if (reader == null) throw new IllegalArgumentException("没有合适的结果读取器");
        return reader;
    }
}
