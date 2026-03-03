/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：使用存储标记判断的异构存储断言提供器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-25 17:29:57
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.HeterogeneityPredicationProvider;
import io.obase.core.odm.typeviews.TypeView;

/**
 * 断言当前节点与根节点是否为存储异构的。如果节点代表的类型未定义异构存储扩展，使用模型默认的存储标记。
 */
public class StorageHeterogeneityPredicationProvider extends HeterogeneityPredicationProvider {

    /**
     * 根节点代表类型的存储标记
     */
    private StorageSymbol rootSymbol = StorageSymbols.getCurrent().getDefault();

    /**
     * 获取根节点代表类型的存储标记
     *
     * @return 根节点代表类型的存储标记
     */
    public StorageSymbol getRootSymbol() {
        return this.rootSymbol;
    }

    /**
     * 比较当前节点与根节点在关注特性上的异同
     *
     * @param currentNode 当前节点
     * @return 相等返回true，否则返回false。
     */
    @Override
    public boolean compare(AssociationTreeNode currentNode) {
        return this.rootSymbol == this.getStorageSymbol(currentNode.getRepresentedType());
    }

    /**
     * 寄存根节点的关注特性
     *
     * @param rootNode 根节点
     */
    @Override
    public void registerRoot(AssociationTreeNode rootNode) {
        if (rootNode.getRepresentedType() == null)
            return;
        TypeExtension extension = rootNode.getRepresentedType().getExtension(HeterogStorageExtension.class);
        if (extension != null) {
            HeterogStorageExtension h = (HeterogStorageExtension) extension;
            this.rootSymbol = h.getStorageSymbol();
        }
    }

    /**
     * 重写比较方法
     *
     * @param o the object to be compared.
     * @return 是否相等
     */
    @Override
    public int compareTo(HeterogeneityPredicationProvider o) {
        return this.equals(o) ? 0 : 1;
    }

    /**
     * 重写Object的Equal
     *
     * @param o 另一个对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o instanceof StorageHeterogeneityPredicationProvider) {
            StorageHeterogeneityPredicationProvider predicationProvider = (StorageHeterogeneityPredicationProvider) o;
            return predicationProvider.getClass().getName().equals(this.getClass().getName());
        }

        return false;
    }

    /**
     * 重写GetHashCode
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return this.getClass().getName().hashCode();
    }

    /**
     * 获取类型的存储标记。
     *
     * @param modelType 结构化类型
     * @return 存储标记
     */
    private StorageSymbol getStorageSymbol(StructuralType modelType) {
        if (modelType instanceof TypeView) {
            TypeView typeView = (TypeView) modelType;
            return this.getStorageSymbol(typeView.getSource());
        }

        if (modelType instanceof ObjectType) {
            TypeExtension extension = modelType.getExtension(HeterogStorageExtension.class);
            if (extension != null) {
                HeterogStorageExtension h = (HeterogStorageExtension) extension;
                return h.getStorageSymbol();
            }
            StorageSymbol symbol = modelType.getModel().getStorageSymbol();
            if (symbol == null)
                symbol = StorageSymbols.getCurrent().getDefault();
            return symbol;
        }

        return null;
    }
}
