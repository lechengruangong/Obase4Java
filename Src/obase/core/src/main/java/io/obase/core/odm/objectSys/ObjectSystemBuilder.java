/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象系统建造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 17:47:07
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.*;
import io.obase.core.odm.typeviews.SelfReference;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.odm.typeviews.ViewReference;

import java.util.*;

/**
 * 对象建造器
 */
public class ObjectSystemBuilder implements IAssociationTreeDownwardVisitorWithResult<Object> {

    /**
     * 用于将创建的对象附加到对象上下文的委托 不指定将不执行附加操作
     */
    private final IAttachObject attachObject;
    /**
     * 指示是否附加根对象
     */
    private final boolean attachRoot;
    /**
     * 用于寄存新建对象的容器
     */
    private final ObjectCreationSet objectCreationSet = new ObjectCreationSet();

    /**
     * 已创建对象
     */
    private final Map<ObjectKey, Object> createdObjs = new HashMap<>();
    /**
     * 作为对象数据源的对象数据集
     */
    private final IObjectDataSet objectDataSet;

    /**
     * 存储结果
     */
    private Object result;

    /**
     * 创建ObjectBuilder实例
     *
     * @param objDataSet   用于创建对象系统的对象数据集
     * @param attachObject 用于将创建的对象附加到对象上下文的委托 不指定将不执行附加操作
     */
    public ObjectSystemBuilder(IObjectDataSet objDataSet, IAttachObject attachObject, boolean attachRoot) {
        this.objectDataSet = objDataSet;
        this.attachObject = attachObject;
        this.attachRoot = attachRoot;
    }

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
        return true;
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
        //获取节点的代表类型
        ReferringType nodeType = subTree.getRepresentedType();
        AssociationTreeNode assNode = subTree.getNode();
        //获取对象数据
        Iterable<ObjectDataSetItem> objectDataSetItems = this.objectDataSet.get(assNode);

        for (ObjectDataSetItem objectDataSetItem : objectDataSetItems) {
            ObjectKey currentKey = objectDataSetItem.ObjectData.getObjectKey();
            if (currentKey == null) continue;

            //构造对象
            Object obj = nodeType.instantiateWithRefGetter(
                    simpleAttributeNode -> {
                        try {
                            return objectDataSetItem.ObjectData.getValue(simpleAttributeNode);
                        } catch (Exception e) {
                            throw new IllegalArgumentException("无法获取简单属性的值" + e.getMessage(), e);
                        }
                    }, referenceElement -> {
                        if (referenceElement.getNavigationUse() == ENavigationUse.EmittingReference && !(referenceElement.getHostType() instanceof TypeView))
                            return new Object[0];
                        AssociationTree tempTree = subTree.getSubTree(referenceElement.getName());
                        if (tempTree == null)
                            return new Object[0];
                        Object[] objs = this.objectCreationSet.getObjects(tempTree.getNode(), currentKey);
                        if (referenceElement.getNavigationUse() == ENavigationUse.DirectlyReference) {
                            return Arrays.stream(objs).map(p -> {
                                try {
                                    return referenceElement.getNavigation().getTargetEnd().getValue(p);
                                } catch (Exception e) {
                                    throw new IllegalArgumentException("获取关联端值错误" + e.getMessage(), e);
                                }
                            }).toArray();
                        } else {
                            return objs;
                        }
                    }, referenceElement -> {
                        //是否使用Include进行了加载 如果进行了 则可以获取到关联树
                        return subTree.getSubTree(referenceElement.getName()) != null;
                    });
            if (!this.checkAssociation(obj, subTree.getNode())) continue;

            /* 1.如果根对象是TypeView，整棵树上的对象均不附加。
             * 2.关联对象由补充操作附加，此处不附加。*/
            if (!(nodeType instanceof TypeView) && !(nodeType instanceof AssociationType && !subTree.getIsRoot()))
                obj = this.attachAndDeduplicate(obj, (ObjectType) nodeType, subTree.getIsRoot());
            //创建并寄存ObjectCreation
            this.objectCreationSet.add(assNode, obj, objectDataSetItem.ParentKey);
            //执行补偿操作
            if (nodeType instanceof EntityType) {
                this.complement(assNode, obj, currentKey);
            }
            this.result = obj;
        }
    }

    /**
     * 重置访问者
     */
    @Override
    public void reset() {
        if (this.attachRoot) {
            return;
        }
        this.result = null;
    }

    /**
     * 获取遍历关联树的结果
     *
     * @return 获取遍历关联树的结果
     */
    @Override
    public Object getResult() {
        return this.result;
    }

    /**
     * 对象附加与去重
     *
     * @param obj     要附加并去重的对象
     * @param objType 对象类型
     * @param asRoot  是否作为根对象
     * @return 对象
     */
    private Object attachAndDeduplicate(Object obj, ObjectType objType, boolean asRoot) {
        if (this.attachObject != null) {
            ObjectReferencePack<Object> attachedObj = new ObjectReferencePack<>();
            attachedObj.realValue = obj;

            this.attachObject.attachObject(attachedObj, asRoot);
            if (obj != attachedObj.realValue) {
                obj = attachedObj.realValue;
            }
        } else {
            ObjectKey objectKey = objType.getObjectKey(obj);
            boolean created = this.createdObjs.containsKey(objectKey);
            if (created) {
                obj = this.createdObjs.get(objectKey);
            } else {
                this.createdObjs.put(objectKey, obj);
            }
        }

        return obj;
    }

    /**
     * 执行补充操作
     *
     * @param currentNode 当前关联树节点
     * @param currentObj  当前对象
     * @param currentKey  当前对象的标识
     */
    private void complement(AssociationTreeNode currentNode, Object currentObj, ObjectKey currentKey) {

        for (ObjectTypeNode child : currentNode.getChildren()) {
            //获取子节点上的对象(实例)
            Object[] assObjs = this.objectCreationSet.getObjects(child, currentKey);

            //获取子节点代表的元素
            AssociationReference childRepresentRef = (AssociationReference)
                    currentNode.getRepresentedType().getReferenceElement(child.getElementName());


            //获取左端名
            String leftEndName = childRepresentRef == null ? "" : childRepresentRef.getLeftEnd();
            AssociationEnd leftEnd = null;
            //获取关联型的左端
            if (leftEndName != null && child.getRepresentedType() instanceof AssociationType) {
                AssociationType at = (AssociationType) child.getRepresentedType();
                leftEnd = at.getAssociationEnd(leftEndName);
            }
            if (leftEnd == null) throw new IllegalArgumentException("关联型的左端不存在");

            if (child.getRepresentedType() instanceof ObjectType) {
                ObjectType objectType = (ObjectType) child.getRepresentedType();
                List<Object> objectList = new ArrayList<>(Arrays.asList(assObjs));
                //去重set
                HashSet<ObjectKey> set = new HashSet<>();
                List<Object> list = new ArrayList<>();
                for (Object obj : objectList) {
                    ObjectKey key = objectType.getObjectKey(obj);
                    //不存在时加入List
                    if (!set.contains(key)) {
                        set.add(key);
                        list.add(obj);
                    }
                }
                assObjs = list.toArray();
            }

            //遍历关联实例
            for (int i = 0; i < assObjs.length; i++) {
                //设置关联实例的左端值
                leftEnd.setValue(assObjs[i], currentObj);
                assObjs[i] = this.attachAndDeduplicate(assObjs[i], (ObjectType) currentNode.getRepresentedType(), false);
            }

            if (!(childRepresentRef != null && childRepresentRef.getAssociationType().getVisible())) continue;

            if (assObjs.length == 0) continue;
            Object value = childRepresentRef.getIsMultiple() ? assObjs : assObjs[0];
            if (childRepresentRef.getIsMultiple())
                value = new ArrayList<>(Arrays.asList((Object[]) value));
            childRepresentRef.setValue(currentObj, value);
        }
    }

    /**
     * 审核创建的关联型实例是否符合规范。
     *
     * @param instance 要检查的关联型实例
     * @param assNode  该关联实例挂靠的关联树节点
     * @return 符合规范返回true，否则返回false。</
     */
    private boolean checkAssociation(Object instance, AssociationTreeNode assNode) {

        //实施说明:
        //关联型实例的所有端都必须有值，除非该端（assEnd）满足以下条件中的至少一个：
        //（1）启用了延迟加载，忽略；
        //（2）关联型实例的挂靠节点代表的引用元素为视图引用或反身引用；
        //（3）上述引用元素是关联引用，且该端是该关联引用的左端（assNode.Element.GetLeftEnd().Equals(assEnd)）；
        //（4）所有映射目标字段（assEnd.Mappings[].TargetField）都能在关联型上找到映射到它的属性。

        if (!(assNode.getRepresentedType() instanceof AssociationType)) return true; //节点代表类型不是关联型。
        if (!(assNode instanceof ObjectTypeNode)) return false;
        AssociationType associationType = (AssociationType) assNode.getRepresentedType();
        ObjectTypeNode objectTypeNode = (ObjectTypeNode) assNode;
        if (objectTypeNode.getElement() instanceof ViewReference || objectTypeNode.getElement() instanceof SelfReference)
            return true; // 关联型实例的挂靠节点代表的引用元素为视图引用或反身引用；
        for (AssociationEnd assEnd : associationType.getAssociationEnds())//遍历关联端
        {
            if (assEnd.getEnableLazyLoading()) continue; //启用了延迟加载，忽略；
            if (objectTypeNode.getElement() instanceof AssociationReference) {
                AssociationReference ar = (AssociationReference) objectTypeNode.getElement();
                if (ar.gotLeftEnd().equals(assEnd))
                    continue; //是关联型实例的挂靠节点代表的关联引用的左端
            }
            boolean allExist = true; //所有映射目标字段（assEnd.Mappings[].TargetField）都能在关联型上找到映射到它的属性
            for (AssociationEndMapping mapping : assEnd.getMappings())
                if (associationType.getAttribute(mapping.getTargetField()) == null && associationType.findAttributeByTargetField(mapping.getTargetField()) == null)
                    allExist = false;
            if (allExist) continue;

            Object endValue = assEnd.getValue(instance);
            if (endValue == null) return false;
        }

        return true;
    }
}
