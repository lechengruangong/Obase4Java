/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：存储DataRowAssignment关联实例.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 11:20:41
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.odm.ObjectKey;
import io.obase.core.odm.objectSys.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存储DataRowAssignment关联实例。
 * 从数据库查询关联树所代表的对象系统时，结果数据集的结构是由关联树平展而来，即将树型结构平展为线性结构。如果关联树中某一节点代表的类型有一个具多重性的引用元素，那
 * 么结果数据集在该节点上将表现出重复性。也就是说，结果集中至少存在两个数据行，它们在该节点上将创建出同一对象的两个相同副本，我们把这样的数据行称为等效数据行，或者
 * 说它们在该节点上是等效的。
 * 为了过滤等效数据行，在正式创建对象系统前需要先对数据行实施分派操作，保证分派至同一关联树节点的数据行两两不等效。分派算法见活动图“分派数据行”。
 * 在创建对象系统过程中，需要在挂靠于父、子节点的两组对象间建立配属关系，因而分派至关联树节点的数据行还需要明确其在该节点上的父标识。
 * DataRowAssignment就是用于描述上述分派及配属关系的关联。
 */
public class DataRowAssignmentSet implements IObjectDataSet {

    /**
     * 用于存储DataRowAssignment实例的字典，其键为数据行的属主节点，值为存储所属数据行的字典，该字典的键为配属关系父标识，值为数据行序列
     */
    private final Map<AssociationTreeNode, Map<ObjectKey, List<DataRow>>> dict = new HashMap<>();

    /**
     * 寄存分派至根节点的数据行的变量
     */
    private DataRow rootRow;

    /**
     * 获取一个值，该值指示是否不存在任何DataRowAssignment实例，包括分派至根节点的数据行
     *
     * @return 是否不存在任何DataRowAssignment实例
     */
    public boolean IsEmpty() {
        return this.dict.size() == 0 && this.rootRow == null;
    }

    /**
     * 获取挂靠在指定关联树节点上的对象数据集合。
     *
     * @param assNode 关联树节点。
     * @return 对象数据集合
     */
    @Override
    public Iterable<ObjectDataSetItem> get(AssociationTreeNode assNode) {
        if (assNode == null || !this.dict.containsKey(assNode)) {
            ObjectDataSetItem item = new ObjectDataSetItem();
            item.ObjectData = new DataRow.NodeSpecializedView(this.rootRow, assNode);
            List<ObjectDataSetItem> result = new ArrayList<>();
            result.add(item);
            return result;
        }

        List<ObjectDataSetItem> result = new ArrayList<>();

        Map<ObjectKey, List<DataRow>> rowData = this.dict.get(assNode);
        for (List<DataRow> value : rowData.values()) {
            for (DataRow row : value) {
                ObjectDataSetItem temp = new ObjectDataSetItem();
                temp.ObjectData = new DataRow.NodeSpecializedView(row, assNode);
                temp.ParentKey = this.getParentKey(row, assNode);
                result.add(temp);
            }
        }

        return result;
    }

    /**
     * 委托转写方法
     *
     * @return 对象键
     */
    private ObjectKey getParentKey(DataRow dataRow, AssociationTreeNode assocNode) {
        ObjectKey parentKey = null;
        AssociationTreeNode parentNode = null;
        if (assocNode instanceof ObjectTypeNode) {
            ObjectTypeNode objectTypeNode = (ObjectTypeNode) assocNode;
            parentNode = objectTypeNode.getParent();
        }
        if (parentNode != null) {
            parentKey = dataRow.getObjectKey(parentNode);
        }
        return parentKey;
    }

    /**
     * 添加一个DataRowAssignment实例
     *
     * @param treeNode 属主节点
     * @param dataRow  数据行
     */
    public void add(AssociationTreeNode treeNode, DataRow dataRow) {

        //父节点
        AssociationTreeNode parent = null;
        if (treeNode instanceof ObjectTypeNode) {
            ObjectTypeNode objectTypeNode = (ObjectTypeNode) treeNode;
            parent = objectTypeNode.getParent();
        }
        //当前即为根 暂存DataRow
        if (parent == null) this.rootRow = dataRow;

        ObjectKey parentKey = parent == null ? null : dataRow.getObjectKey(parent);

        if (parentKey != null) {
            if (!this.dict.containsKey(treeNode)) this.dict.put(treeNode, new HashMap<>());
            Map<ObjectKey, List<DataRow>> parentRow = this.dict.get(treeNode);
            if (parentRow.containsKey(parentKey)) {
                ObjectTypeNode objectTypeNode = (ObjectTypeNode) treeNode;
                //如果是多重的关联引用 或者 是单个的关联引用但是没有添加过
                if (objectTypeNode.getElement().getIsMultiple() || parentRow.get(parentKey).size() < 1)
                    parentRow.get(parentKey).add(dataRow);
            } else {
                List<DataRow> temp = new ArrayList<>();
                temp.add(dataRow);
                parentRow.put(parentKey, temp);
            }
        }
    }

    /**
     * 清除所有DataRowAssignment实例，包括分派至根节点的数据行
     */
    public void clear() {
        this.dict.clear();
        this.rootRow = null;
    }

    /**
     * 等效检查，即检查指定关联树节点是否已分派了一个与指定数据行等效的数据行
     *
     * @param treeNode 属主节点
     * @param dataRow  待检测的数据行
     * @return 是否等效
     */
    public boolean containEquivalent(AssociationTreeNode treeNode, DataRow dataRow) {
        if (treeNode instanceof ObjectTypeNode) {
            ObjectTypeNode objectTypeNode = (ObjectTypeNode) treeNode;
            if (objectTypeNode.getParent() == null) {
                if (this.rootRow == null) return false;
                //比较根
                ObjectKey rootId = this.rootRow.getObjectKey(treeNode);
                ObjectKey currId = dataRow.getObjectKey(treeNode);
                return rootId.equals(currId);
            }
        }

        if (treeNode instanceof TypeViewNode) {
            if (this.rootRow == null) return false;
            //比较根
            ObjectKey rootId = this.rootRow.getObjectKey(treeNode);
            ObjectKey currId = dataRow.getObjectKey(treeNode);
            return rootId.equals(currId);
        }

        if (!this.dict.containsKey(treeNode)) return false; //不存在此节点 肯定不等效

        if (treeNode instanceof ObjectTypeNode) {
            ObjectTypeNode objectTypeNode = (ObjectTypeNode) treeNode;
            //取父级
            AssociationTreeNode parent = objectTypeNode.getParent();
            //有父级
            ObjectKey parentKey = parent == null ? null : dataRow.getObjectKey(parent);
            if (parentKey == null) return false;
            if (this.dict.get(treeNode).containsKey(parentKey)) {
                ObjectKey currentRowKey = dataRow.getObjectKey(treeNode);
                List<DataRow> rows = this.dict.get(treeNode).get(parentKey);
                for (DataRow row : rows) {
                    if (dataRow.equals(row))
                        return true;
                    ObjectKey currentKey = row.getObjectKey(treeNode);
                    if (currentRowKey.equals(currentKey))
                        return true;
                }
            } else {
                return false;
            }
        }


        return false;
    }
}
