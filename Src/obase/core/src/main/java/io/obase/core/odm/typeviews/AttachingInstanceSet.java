/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：附加视图实例集.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-8 14:39:14
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.typeviews;

import io.obase.common.ObjectReferencePack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 附加视图实例集，存储某一附加视图的一个或多个实例。
 */
public class AttachingInstanceSet {

    /**
     * 附加视图的视图引用
     */
    private final ViewReference attachingRef;

    /**
     * 作为集中视图实例的类型的附加视图
     */
    private final TypeView attachingView;

    /**
     * 附加视图的实例
     */
    private Object[] instances;

    /**
     * 创建AttachingInstanceSet实例
     *
     * @param attachingView 作为集中视图实例的类型的附加视图
     * @param instances     一个或多个视图实例
     */
    public AttachingInstanceSet(TypeView attachingView, ViewReference attachingRef, Object[] instances) {
        this.attachingView = attachingView;
        this.attachingRef = attachingRef;
        this.instances = instances;
    }

    /**
     * 获取视图实例集包含的视图实例
     *
     * @return 视图实例集包含的视图实例
     */
    public Object[] getInstances() {
        return this.instances;
    }

    /**
     * 获取作为集中视图实例的类型的附加视图
     *
     * @return 作为集中视图实例的类型的附加视图
     */
    public TypeView getAttachingView() {
        return this.attachingView;
    }

    /**
     * 根据平展鍵对视图实例分组，每一组构成一个新的实例集。
     *
     * @return 视图实例分组后的附加视图实例集
     */
    public AttachingInstanceSet[] groupByFlatteningKey() {
        ViewAttribute[] flatteningAttrs = this.attachingView.getFlatteningKey();
        if (flatteningAttrs == null) {
            AttachingInstanceSet[] result = new AttachingInstanceSet[1];
            result[0] = this;
            return result;
        }
        //用flattening方法分组
        Map<Object[], List<Object>> collect = Arrays.stream(this.instances).collect(Collectors.groupingBy(p -> this.flattening(flatteningAttrs, p)));
        return collect.keySet().stream().map(p -> new AttachingInstanceSet(this.attachingView, this.attachingRef, p)).toArray(AttachingInstanceSet[]::new);
    }

    /**
     * 从附加实例中筛选出一个子集，该子集由指定的基础实例引用。
     *
     * @param baseInstance 基础实例
     * @param removing     指示是否移除筛选出的子集。默认不移除。
     * @return 筛选后的附加视图实例集
     */
    public AttachingInstanceSet filterByBaseInstance(Object baseInstance, boolean removing) {
        Object[] objects = this.instances;
        ObjectReferencePack<Object[]> pack = new ObjectReferencePack<>();
        pack.realValue = objects;
        Object[] subSet = this.attachingRef.filterTarget(pack, baseInstance, this.attachingView, removing);
        this.instances = pack.realValue;

        return new AttachingInstanceSet(this.attachingView, this.attachingRef, subSet);
    }

    /**
     * 从附加实例中筛选出一个子集，该子集由指定的基础实例引用。
     *
     * @param baseInstance 基础实例
     * @return 筛选后的附加视图实例集
     */
    public AttachingInstanceSet filterByBaseInstance(Object baseInstance) {
        return this.filterByBaseInstance(baseInstance, false);
    }

    /**
     * 平展视图属性的方法
     *
     * @param flatteningAttrs 视图属性
     * @param o               视图实例
     * @return 视图的值集合
     */
    private Object[] flattening(ViewAttribute[] flatteningAttrs, Object o) {

        int len = flatteningAttrs.length;
        Object[] key = new Object[len];
        for (int i = 0; i < len; i++)
            key[i] = flatteningAttrs[i].getValue(o);
        return key;
    }
}
