/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：复杂属性建造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 14:51:34
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.typeView;

import io.obase.common.ObjectReferencePack;
import io.obase.core.expression.Expression;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.odm.objectSys.AttributeTreeNode;
import io.obase.core.odm.objectSys.ParameterBinding;
import io.obase.core.odm.typeviews.ViewComplexAttribute;

import java.lang.reflect.Member;

/**
 * 复杂属性建造器
 */
public class ViewComplexAttributeBuilder extends ViewElementBuilder {

    /**
     * 创建ViewElementBuilder实例
     *
     * @param model 对象数据模型
     */
    public ViewComplexAttributeBuilder(ObjectDataModel model) {
        super(model);
    }

    /**
     * 实例化类型元素，同时根据需要扩展视图源
     *
     * @param member          与元素对应的类成员
     * @param expression      类成员绑定的表达式
     * @param sourceExtension 视图源扩展树
     * @param paraBindings    形参绑定
     */
    @Override
    public void instantiate(Member member, Expression expression, AssociationTree sourceExtension, ParameterBinding[] paraBindings) {
        ObjectReferencePack<AttributeTreeNode> lastAattrNode = new ObjectReferencePack<>();
        AssociationTreeNode lastAssocNode = expression.growAssociationTree(sourceExtension, this.model,
                lastAattrNode, paraBindings);
        this.element = new ViewComplexAttribute(member.getName(), lastAssocNode, lastAattrNode.realValue);
    }
}
