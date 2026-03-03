/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：存储ObjectCreation实例的容器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 15:10:15
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.core.odm.ObjectKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存储ObjectCreation实例的容器。
 * ObjectCreation是一个关联。在创建对象系统过程中我们将新建的对象临时“挂靠”在关联树各节点上，并在挂靠于父子节点的两组对象间建立配属关系（挂靠子节点
 * 的一个或多个对象配属于一个挂靠父节点的对象），根据挂靠及配属关系可以在对象间建立引用关系，从而最终生成对象系统。ObjectCreation关联即用于描述上述挂
 * 靠及配属关系。
 * 于对象标识建立上述配属关系。如果对象A配属于对象B,则称对象B的标识为A的父标识。
 */
public class ObjectCreationSet {

    /**
     * 存储ObjectCreation实例的集合，其键为挂靠节点，值为存储新建对象的字典，该字典的键为对象父标识，值为对象自身。
     */
    private final Map<AssociationTreeNode, Map<ObjectKey, List<Object>>> dict = new HashMap<>();

    /**
     * 用于寄存根对象的寄存器
     */
    private Object rootObject;

    /**
     * 获取根对象
     *
     * @return 根对象
     */
    public Object getRootObject() {
        return this.rootObject;
    }

    /**
     * 添加一个ObjectCreation实例。
     *
     * @param treeNode  挂靠节点。
     * @param obj       挂靠的对象。
     * @param parentKey 父标识。
     */
    public void add(AssociationTreeNode treeNode, Object obj, ObjectKey parentKey) {
        if (parentKey == null) {
            this.rootObject = obj;
        } else {
            if (this.dict.containsKey(treeNode)) {
                Map<ObjectKey, List<Object>> nodeVars = this.dict.get(treeNode);
                if (nodeVars.containsKey(parentKey)) nodeVars.get(parentKey).add(obj);
                else {
                    List<Object> objs = new ArrayList<>();
                    objs.add(obj);
                    nodeVars.put(parentKey, objs);
                }
            } else {

                Map<ObjectKey, List<Object>> hashMap = new HashMap<>();
                List<Object> objs = new ArrayList<>();
                objs.add(obj);
                hashMap.put(parentKey, objs);
                this.dict.put(treeNode, hashMap);
            }
        }
    }

    /**
     * 获取挂靠在指定节点并配属于指定对象的对象集
     *
     * @param treeNode  关联树节点
     * @param parentKey 父级对象键
     * @return 指定对象的对象集
     */
    public Object[] getObjects(AssociationTreeNode treeNode, ObjectKey parentKey) {
        if (this.dict.containsKey(treeNode) && this.dict.get(treeNode).containsKey(parentKey))
            return this.dict.get(treeNode).get(parentKey).toArray();
        return new Object[0];
    }

    /**
     * 清空ObjectCreation集合。
     */
    public void clear() {
        this.dict.clear();
    }
}
