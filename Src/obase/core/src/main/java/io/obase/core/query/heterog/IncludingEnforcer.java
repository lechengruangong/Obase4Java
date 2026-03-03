/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：强制包含执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 15:46:44
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.heterog;

import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.IAssociationTreeDownwardVisitor;
import io.obase.core.odm.objectSys.ObjectTypeNode;

import java.util.List;

/**
 * 作为关联树向下访问者，执行强制包含
 */
public class IncludingEnforcer implements IAssociationTreeDownwardVisitor {
    /**
     * 前置访问，即在访问子级前执行操作。
     *
     * @param subTree          被访问的关联树子树
     * @param parentState      访问父级时产生的状态数据
     * @param outParentState   返回一个状态数据，在遍历到子级时该数据将被视为父级状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     * @return 是否继续访问
     */
    @Override
    public boolean preVisit(AssociationTree subTree, Object parentState, ObjectReferencePack<Object> outParentState, ObjectReferencePack<Object> outPreVisitState) {
        //强制包含构造函数参数
        List<Parameter> parameters = subTree.getRepresentedType().getConstructor().getParameters();

        if (parameters != null && parameters.size() > 0) {
            for (Parameter parameter : parameters) {
                TypeElement paraElement = parameter.getElement();
                if (!(paraElement instanceof ReferenceElement))
                    continue;
                if (subTree.getNode().hasChild(paraElement.getName()))
                    continue;
                subTree.grow(paraElement.getName());
            }
        }

        //强制包含关联端
        if (subTree.getRepresentedType() instanceof AssociationType) {
            AssociationType associationType = (AssociationType) subTree.getRepresentedType();

            List<AssociationEnd> assoEnds = associationType.getAssociationEnds();
            for (AssociationEnd end : assoEnds) {
                if (end.getEnableLazyLoading())
                    continue;
                if (subTree.getNode().hasChild(end.getName()))
                    continue;
                if (subTree.getNode() instanceof ObjectTypeNode) {
                    ObjectTypeNode objectTypeNode = (ObjectTypeNode) subTree.getNode();
                    if (objectTypeNode.getElement() instanceof AssociationReference) {
                        AssociationReference assocRef = (AssociationReference) objectTypeNode.getElement();
                        AssociationEnd left = assocRef.gotLeftEnd();
                        if (end.equals(left))
                            continue;
                    }
                }

                boolean hasAttribute = true;
                for (AssociationEndMapping mapping : end.getMappings()) {
                    Attribute targetField = associationType.findAttributeByTargetField(mapping.getTargetField());
                    if (targetField == null) {
                        hasAttribute = false;
                        break;
                    }
                }

                if (hasAttribute)
                    continue;
                subTree.grow(end.getName());
            }
        }

        outParentState.realValue = null;
        outPreVisitState.realValue = null;
        return false;
    }

    /**
     * 后置访问，即在访问子级后执行操作
     *
     * @param subTree       被访问的关联树子树
     * @param parentState   访问父级时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     */
    @Override
    public void postVisit(AssociationTree subTree, Object parentState, Object preVisitState) {
        //Nothing to do
    }

    /**
     * 重置访问者
     */
    @Override
    public void reset() {
        //Nothing to do
    }
}
