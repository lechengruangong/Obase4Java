/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：为类型视图的映射源提供默认的遗传映射机制.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 12:19:20
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

import io.obase.core.common.Utils;
import io.obase.core.odm.ReferenceElement;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.TypeViewNode;
import io.obase.core.odm.typeviews.ViewReference;
import io.obase.core.saving.IHeredityMapper;

/**
 * 为类型视图的映射源提供默认的遗传映射机制
 */
public class TypeViewHeredityMapper implements IHeredityMapper {

    /**
     * 别名生成器
     */
    private final AliasGenerator aliasGenerator = new AliasGenerator();
    /**
     * 作为联接依据的视图引用
     */
    private ViewReference joinReference;

    /**
     * 作为联接依据的视图引用
     *
     * @return 视图引用
     */
    public ViewReference getJoinReference() {
        return this.joinReference;
    }

    /**
     * 作为联接依据的视图引用
     *
     * @param joinReference 视图引用
     */
    public void setJoinReference(ViewReference joinReference) {
        this.joinReference = joinReference;
    }

    /**
     * 根据字段在母源中名称推断其在衍生源中的名称
     *
     * @param fieldName 字段在母源中的名称
     * @return 名称
     */
    @Override
    public String map(String fieldName) {
        return this.map(this.getJoinReference(), fieldName);
    }

    /**
     * 推断其在衍生源中的名称
     *
     * @param joinRef   视图引用
     * @param fieldName 字段名称
     * @return 衍生源中的名称
     */
    private String map(ViewReference joinRef, String fieldName) {
        //获取锚点
        AssociationTreeNode anchor = joinRef.getAnchor();
        if (anchor instanceof TypeViewNode) {
            //获取视图绑定
            ReferenceElement binding = joinRef.getBinding();
            ViewReference reference = (ViewReference) binding;
            return this.map(reference, fieldName);
        }

        //转换为关联树
        AssociationTree tree = anchor.asTree();
        String result = tree.accept(this.aliasGenerator, fieldName);
        if (Utils.getStringIsEmpty(result))
            return fieldName;
        return result;
    }
}
