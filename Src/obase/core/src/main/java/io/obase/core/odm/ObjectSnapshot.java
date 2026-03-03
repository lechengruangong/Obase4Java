/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象快照,用于记录对象的当前状态，包含属性值和引用元素值.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 15:16:42
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.objectSys.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 对象快照，用于记录对象的当前状态，包含属性值和引用元素值。
 */
public class ObjectSnapshot implements Serializable {

    /**
     * 存储复杂属性值的字典，键为属性名，值为属性值的快照
     */
    private final Map<String, ObjectSnapshot> complexAttributes = new HashMap<>();

    /**
     * 存储引用元素值的字典，键为元素名，值为被引用对象的标识的集合。
     */
    private final Map<String, List<ObjectKey>> referenceKeys = new HashMap<>();

    /**
     * 存储简单属性值的字典，键为属性名，值为属性值
     */
    private final Map<String, Object> simpleAttributes = new HashMap<>();
    /**
     * 被快照的对象的类型
     */
    private final StructuralType structuralType;
    /**
     * 全局引用字典，存储被当前对象直接和间接引用的对象，键为对象标识，值为对象的快照
     */
    private Map<ObjectKey, ObjectSnapshot> allReferences = new HashMap<>();

    /**
     * 创建ObjectSnapshot实例
     *
     * @param structuralType 对象的类型
     */
    public ObjectSnapshot(StructuralType structuralType) {
        this.structuralType = structuralType;
    }

    /**
     * 获取全局引用字典
     *
     * @return 全局引用字典
     */
    public Map<ObjectKey, ObjectSnapshot> getAllReferences() {
        return this.allReferences;
    }

    /**
     * 设置全局引用字典
     *
     * @param allReferences 全局引用字典
     */
    public void setAllReferences(Map<ObjectKey, ObjectSnapshot> allReferences) {
        this.allReferences = allReferences;
    }

    /**
     * 获取对象的所有属性，包括简单属性和复杂属性
     *
     * @return 所有属性
     */
    public String[] getAttributes() {
        List<String> list = new ArrayList<>();

        list.addAll(this.simpleAttributes.keySet());
        list.addAll(this.complexAttributes.keySet());

        return list.toArray(new String[0]);
    }

    /**
     * 获取对象的所有引用元素
     *
     * @return 所有引用元素
     */
    public String[] getReferences() {

        List<String> list = new ArrayList<>(this.referenceKeys.keySet());

        return list.toArray(new String[0]);
    }

    /**
     * 设置简单属性的值
     *
     * @param attrName 属性名称
     * @param value    属性值
     */
    public void setAttribute(String attrName, Object value) {
        this.simpleAttributes.put(attrName, value);
    }

    /**
     * 设置复杂属性的值
     *
     * @param attrName 属性名称
     * @param value    属性值的快照
     */
    public void setAttribute(String attrName, ObjectSnapshot value) {
        this.complexAttributes.put(attrName, value);
    }

    /**
     * 设置子属性（即复杂属性的值的属性）的值
     *
     * @param attribute    属性名称
     * @param subAttribute 子属性的名称
     * @param value        子属性的值
     */
    public void setAttribute(String attribute, String subAttribute, Object value) {
        if (this.complexAttributes.containsKey(attribute)) {
            this.complexAttributes.get(attribute).setAttribute(subAttribute, value);
        }
    }

    /**
     * 为指定的引用元素添加一个目标对象
     *
     * @param elementName 元素名称
     * @param objectKey   被引用对象的标识
     */
    public void addReference(String elementName, ObjectKey objectKey) {
        if (this.referenceKeys.containsKey(elementName)) {
            if (this.referenceKeys.get(elementName) != null)
                this.referenceKeys.get(elementName).add(objectKey);
            else {
                List<ObjectKey> objectKeys = new ArrayList<>();
                objectKeys.add(objectKey);
                this.referenceKeys.put(elementName, objectKeys);
            }

        } else {
            List<ObjectKey> objectKeys = new ArrayList<>();
            objectKeys.add(objectKey);
            this.referenceKeys.put(elementName, objectKeys);
        }
    }

    /**
     * 获取指定元素（简单属性、复杂属性、引用元素）的值
     *
     * @param attributeName 属性名
     * @return 元素的值，对于简单属性、复杂属性和引用元素，值类型分别为基元类型、对象快照和对象标识集合。
     */
    public Object getElement(String attributeName) throws ElementNotFoundException {
        //查找简单属性
        Object simpleValue = this.simpleAttributes.getOrDefault(attributeName, null);
        if (simpleValue != null) return simpleValue;
        //查找复合属性
        Object complexValue = this.complexAttributes.getOrDefault(attributeName, null);
        if (complexValue != null) return complexValue;
        //查找引用
        Object referenceValue = this.referenceKeys.getOrDefault(attributeName, null);
        if (referenceValue != null) return referenceValue;

        throw new ElementNotFoundException(attributeName);
    }

    /**
     * 获取指定子属性的值
     *
     * @param attribute    属性名
     * @param subAttribute 子属性名
     * @return 子属性的值
     * @throws ElementNotFoundException 指定的属性不存在
     */
    public Object getAttribute(String attribute, String subAttribute) throws ElementNotFoundException {
        if (this.complexAttributes.containsKey(attribute))
            return this.complexAttributes.get(attribute).getElement(subAttribute);
        throw new ElementNotFoundException(attribute);
    }

    /**
     * 获取对象标识
     *
     * @return 对象标识
     */
    public ObjectKey getKey() {

        if (this.structuralType instanceof EntityType) {
            EntityType entityType = (EntityType) this.structuralType;
            List<ObjectKeyMember> keyMembers = new ArrayList<>();
            for (String keyMemberName : entityType.getKeyFields()) {
                Object keyMemberValue = this.getElement(keyMemberName);
                ObjectKeyMember member = new ObjectKeyMember(entityType.getClrType().getName() + "-" + keyMemberName, keyMemberValue);
                keyMembers.add(member);
            }

            return new ObjectKey(entityType, keyMembers);
        }

        if (this.structuralType instanceof AssociationType) {
            AssociationType associationType = (AssociationType) this.structuralType;
            List<ObjectKeyMember> keyMembers = new ArrayList<>();
            for (AssociationEnd end : associationType.getAssociationEnds()) {
                ObjectSnapshot endObj = null;
                Object tempEndObj = this.getElement(end.getName());
                if (tempEndObj instanceof ObjectSnapshot) {
                    endObj = (ObjectSnapshot) tempEndObj;
                }
                //关联端有快照
                if (endObj != null) {
                    ObjectKey endKey = endObj.getKey();
                    keyMembers.addAll(endKey.getMembers());
                } else {
                    for (AssociationEndMapping mapping : end.getMappings()) {
                        Object val = this.getElement(mapping.getTargetField());
                        ObjectKeyMember member = new ObjectKeyMember(mapping.getTargetField(), val);
                        keyMembers.add(member);
                    }
                }
            }
            return new ObjectKey(associationType, keyMembers);
        }

        throw new IllegalArgumentException("当前对象不是ObjectType,没有对象标识。");
    }

    /**
     * 获取指定的引用元素的值
     *
     * @param element 引用元素名
     * @return 对象标识序列
     * @throws ElementNotFoundException 指定的元素不存在
     */
    public Iterable<ObjectKey> getReference(String element) throws ElementNotFoundException {
        if (this.referenceKeys.containsKey(element)) return this.referenceKeys.get(element);

        throw new ElementNotFoundException(element);
    }

    /**
     * 检测指定的元素（简单属性、复杂属性、引用元素）是否存在。
     *
     * @param attributeName 属性名
     * @return 是否包含
     */
    public boolean containsElement(String attributeName) {
        //查找简单属性
        Object simpleValue = this.simpleAttributes.getOrDefault(attributeName, null);
        if (simpleValue != null) return true;
        //查找复合属性
        ObjectSnapshot complexValue = this.complexAttributes.getOrDefault(attributeName, null);
        if (complexValue != null) return true;
        //查找引用
        List<ObjectKey> referenceValue = this.referenceKeys.getOrDefault(attributeName, null);
        return referenceValue != null;
        //都没有
    }

    /**
     * 依据全局引用字典生成关联树，并返回用于重建对象系统的数据集。
     *
     * @param dataSet 对象数据集合
     * @return 关联树
     */
    public AssociationTree generateTree(ObjectReferencePack<IObjectDataSet> dataSet) {
        // 实施说明
        //在为对象建立快照时，对于引用元素，只记录被引对象的标识，
        //而将对象本身放入全局引用字典（AllReferences）——仍然是以快照形式——
        //对象沿关联关系直接或间接引用的对象都被放入了全局引用字典，因此我们通过分析字典中的对象及其引用关系即可重建对象的关联树。

        if (this.structuralType instanceof ObjectType) {
            ObjectType objectType = (ObjectType) this.structuralType;

            AssociationTreeGrower grower = new AssociationTreeGrower(this);
            AssociationTree associationTree = new AssociationTree(objectType);
            ObjectReferencePack<ObjectAssignmentSet> outData = new ObjectReferencePack<>();
            associationTree.accept(grower, outData);
            //最终结果
            dataSet.realValue = outData.realValue;
            return grower.getResult();
        }

        throw new IllegalArgumentException("当前对象不是ObjectType,无法生成关联树");
    }

    /**
     * 作为关联树向下访问者，依据对象快照的全局引用字典生成关联树。
     */
    private static class AssociationTreeGrower implements IAssociationTreeDownwardVisitorWithOutArg<AssociationTree, ObjectAssignmentSet> {

        /**
         * 对象快照
         */
        private final ObjectSnapshot obj;

        /**
         * 一并返回值
         */
        private ObjectAssignmentSet outSet;

        /**
         * 结果
         */
        private AssociationTree result;

        /**
         * 构造关联树访问者
         *
         * @param obj 快照
         */
        public AssociationTreeGrower(ObjectSnapshot obj) {
            this.obj = obj;
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
            if (parentState == null) {
                //创建并存储分派关系
                AssociationTreeNode node = subTree.getNode();
                this.outSet = new ObjectAssignmentSet();
                this.outSet.add(new ObjectAssignment(this.obj, node, null));
                //生长关联树
                this.growSub(subTree, node);

                ObjectSnapshot[] snapshots = new ObjectSnapshot[1];
                snapshots[0] = this.obj;
                outParentState.realValue = snapshots;
                this.result = subTree;
            } else {
                if (parentState instanceof ObjectSnapshot[]) {
                    ObjectSnapshot[] snapshots = (ObjectSnapshot[]) parentState;
                    List<ObjectSnapshot> objList = new ArrayList<>();
                    for (ObjectSnapshot snapshot : snapshots) {
                        Iterable<ObjectKey> keys = snapshot.getReference(subTree.getElementName());

                        for (ObjectKey key : keys) {
                            //创建并存储分派关系
                            AssociationTreeNode node = subTree.getNode();
                            //创建并存储分派关系
                            if (this.obj.getAllReferences().containsKey(key)) {
                                ObjectSnapshot objectSnapshot = this.obj.getAllReferences().get(key);
                                this.outSet.add(new ObjectAssignment(objectSnapshot, node, key));
                                objList.add(objectSnapshot);
                            }
                            this.outSet.add(new ObjectAssignment(this.obj, node, key));
                            //生长关联树
                            this.growSub(subTree, node);
                        }
                    }

                    outParentState.realValue = objList.toArray(new ObjectSnapshot[0]);
                } else {
                    outParentState.realValue = null;
                }
            }

            outPreVisitState.realValue = null;

            return true;
        }

        /**
         * 生长关联树
         *
         * @param subTree 关联树
         * @param node    关联树节点
         */
        private void growSub(AssociationTree subTree, AssociationTreeNode node) {
            ReferenceElement[] refElements = subTree.getRepresentedType().getReferenceElements();
            for (ReferenceElement reference : refElements) {
                if (node.hasChild(reference.getName()))
                    continue;
                Iterable<ObjectKey> subKey;
                subKey = this.obj.getReference(reference.getName());
                if (subKey.iterator().hasNext()) {
                    subTree.addSubTree(new AssociationTree(reference), reference.getName());
                }
            }
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

        /**
         * 获取输出参数的值
         *
         * @return 获取输出参数的值
         */
        @Override
        public ObjectAssignmentSet getOutArgument() {
            return this.outSet;
        }

        /**
         * 获取遍历关联树的结果
         *
         * @return 获取遍历关联树的结果
         */
        @Override
        public AssociationTree getResult() {
            return this.result;
        }
    }

    /**
     * 存储对象分派关系的实例。
     */
    private static class ObjectAssignmentSet implements IObjectDataSet {

        /**
         * 添加对象分派关系集合
         */
        private final List<ObjectAssignment> assignments = new ArrayList<>();

        /**
         * 获取挂靠在指定关联树节点上的对象数据集合。
         *
         * @param asoNode 关联树节点。
         * @return 对象数据集合
         */
        @Override
        public Iterable<ObjectDataSetItem> get(AssociationTreeNode asoNode) {
            List<ObjectDataSetItem> resultList = new ArrayList<>();
            List<ObjectAssignment> assignments = this.assignments.stream().filter(p -> p.getAnchorNode().equals(asoNode)).collect(Collectors.toList());
            if (assignments.size() > 0) {
                for (ObjectAssignment assignment : assignments) {
                    ObjectDataSetItem item = new ObjectDataSetItem();
                    item.ObjectData = new ObjectData(assignment.getObj());
                    item.ParentKey = assignment.getParentKey();
                    resultList.add(item);
                }
            }

            return resultList;
        }

        /**
         * 添加对象分派关系实例
         *
         * @param assignment 分派关系实例
         */
        public void add(ObjectAssignment assignment) {
            this.assignments.add(assignment);
        }
    }

    /**
     * 对象快照面向IObjectData的适配器
     */
    private static class ObjectData implements IObjectData {

        /**
         * 对象快照
         */
        private final ObjectSnapshot obj;

        /**
         * 初始化ObjectData
         *
         * @param obj 快照
         */
        private ObjectData(ObjectSnapshot obj) {
            this.obj = obj;
        }


        /**
         * 获取指定属性树节点代表的简单属性的值
         *
         * @param attrNode 属性树节点
         * @return 值
         */
        @Override
        public Object getValue(SimpleAttributeNode attrNode) {
            return this.obj.getElement(attrNode.getAttributeName());
        }

        /**
         * 获取对象标识
         *
         * @return 对象标识
         */
        @Override
        public ObjectKey getObjectKey() {
            return this.obj.getKey();
        }
    }

    /**
     * 描述对象分派关系。对象分派关系是指执行对象分派过程中，在对象快照与关联树节点间建立的对应关系。
     */
    private static class ObjectAssignment {

        /**
         * 锚点节点
         */
        private final AssociationTreeNode anchorNode;

        /**
         * 快照
         */
        private final ObjectSnapshot obj;

        /**
         * 父级对象键
         */
        private final ObjectKey parentKey;

        /**
         * 构造描述对象分派关系
         *
         * @param obj        快照
         * @param anchorNode 锚点节点
         * @param parentKey  父级对象键
         */
        public ObjectAssignment(ObjectSnapshot obj, AssociationTreeNode anchorNode, ObjectKey parentKey) {
            this.obj = obj;
            this.anchorNode = anchorNode;
            this.parentKey = parentKey;
        }

        /**
         * 获取锚点节点
         *
         * @return 锚点节点
         */
        public AssociationTreeNode getAnchorNode() {
            return this.anchorNode;
        }

        /**
         * 获取快照
         *
         * @return 快照
         */
        public ObjectKey getParentKey() {
            return this.parentKey;
        }

        /**
         * 获取父级对象键
         *
         * @return 父级对象键
         */
        public ObjectSnapshot getObj() {
            return this.obj;
        }
    }
}
