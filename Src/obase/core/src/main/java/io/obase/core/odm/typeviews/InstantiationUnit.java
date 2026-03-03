/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：异构视图实例化单元.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-8 15:12:58
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.typeviews;

import io.obase.core.odm.TypeElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 异构视图实例化单元，包含一个基础视图实例及一个与之配对的附加视图实例集。附加视图实例集中的实例与极限分解得到的附加视图一一对应。
 */
public class InstantiationUnit {

    /**
     * 基础视图实例
     */
    private final Object baseInstance;

    /**
     * 基础视图
     */
    private final TypeView baseView;

    /**
     * 存储附加视图实例的字典，其中鍵为附加视图，值为该视图的实例（一个或多个）
     */
    private Map<TypeView, AttachingInstanceSet> attachingInstances;

    /**
     * 创建InstantiationUnit实例。
     *
     * @param baseInstance 基础视图实例
     * @param baseView     基础视图
     */
    public InstantiationUnit(Object baseInstance, TypeView baseView) {
        this.baseInstance = baseInstance;
        this.baseView = baseView;
    }

    /**
     * 向实例化单元添加附加视图实例集
     *
     * @param instanceSet 附加视图实例集
     */
    public void addAttachingInstance(AttachingInstanceSet instanceSet) {
        if (this.attachingInstances == null) this.attachingInstances = new HashMap<>();
        this.attachingInstances.put(instanceSet.getAttachingView(), instanceSet.filterByBaseInstance(this.baseInstance, true));
    }

    /**
     * 为实例化单元生成指定个数的复本
     *
     * @param count 要复制的副本数
     * @return 生成的实例化单元复本
     */
    public InstantiationUnit[] clone(int count) {
        InstantiationUnit[] clones = new InstantiationUnit[count];
        for (int i = 0; i < count; i++) clones[i] = new InstantiationUnit(this.baseInstance, this.baseView);
        return clones;
    }

    /**
     * 从实例化单元获取指定元素的值
     *
     * @param element 要获取其值的元素
     * @return 元素的值
     */
    public Object getValue(TypeElement element) {
        //取元素宿主
        TypeView typeView = (TypeView) element.getHostType();
        if (typeView == this.baseView) return element.getValue(this.baseInstance);

        List<Object> result = new ArrayList<>();
        //获取实例集中的实例
        Object[] instances = this.attachingInstances.get(typeView).getInstances();

        if (instances == null || instances.length == 0)
            return null;

        for (Object instance : instances) {
            if (instance == null) {
                result.add(null);
            } else {
                //从附加实例取元素值，并添加到结果集。
                result.add(element.getValue(instance));
            }

        }
        return result.size() == 1 ? result.get(0) : result.toArray();
    }
}
