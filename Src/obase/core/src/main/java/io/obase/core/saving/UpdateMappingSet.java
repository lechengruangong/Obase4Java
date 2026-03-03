/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：更新映射集.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 17:10:50
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.odm.AssociationType;
import io.obase.core.odm.ObjectKey;
import io.obase.core.odm.ObjectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 更新映射集，负责将待执行更新映射的对象划分为一组映射单元，划分依据为：实体对象和独立关联取其键值、伴随关联取其伴随端的键值，键值相等者为一个单元。
 */
public class UpdateMappingSet {

    /**
     * 映射单元组
     */
    private final List<MappingUnit> mappingUnits = new ArrayList<>();


    /**
     * 映射单元检索字典
     */
    private final Map<ObjectKey, MappingUnit> selectDic = new HashMap<>();

    /**
     * 获取更新映射集中映射单元的数量
     *
     * @return 获取更新映射集中映射单元的数量
     */
    public int getCount() {
        return this.mappingUnits.size();
    }

    /**
     * 获取指定索引处的映射单元
     *
     * @param index 从零开始的索引
     * @return 获取指定索引处的映射单元
     */
    public MappingUnit get(int index) {
        if (index >= this.mappingUnits.size() || index < 0)
            throw new IllegalArgumentException("映射单元索引超出数组界限");
        return this.mappingUnits.get(index);
    }

    /**
     * 设置指定索引处的映射单元
     *
     * @param index 从零开始的索引
     * @param unit  映射单元
     */
    public void set(int index, MappingUnit unit) {
        if (index >= this.mappingUnits.size() || index < 0)
            throw new IllegalArgumentException("映射单元索引超出数组界限");
        this.mappingUnits.set(index, unit);
    }

    /**
     * 向映射集添加伴随关联
     *
     * @param companion       要添加的伴随关联
     * @param associationType 伴随关联的类型
     * @param status          伴随关联对象的状态
     */
    public void addCompanion(Object companion, AssociationType associationType, EObjectStatus status) {
        ObjectKey key = ObjectSystemVisitor.getObjectKey(companion, associationType, associationType.getCompanionEnd());
        MappingUnit unit = this.selectDic.getOrDefault(key, null);

        if (unit == null) {
            unit = new MappingUnit();
            this.mappingUnits.add(unit);
            this.selectDic.put(key, unit);
        }

        unit.addCompanion(companion, status);
    }

    /**
     * 向映射集中添加一个对象，该对象将作为映射单元的主体对象。注：只有实体对象和独立关联可作为主体对象。
     *
     * @param hostObj    要添加的对象
     * @param objectType 对象的类型
     */
    public void addHost(Object hostObj, ObjectType objectType) {
        if (objectType instanceof AssociationType) {
            AssociationType assoc = (AssociationType) objectType;
            if (assoc.getIndependent()) {
                MappingUnit unit = new MappingUnit();
                unit.addHost(hostObj);
                this.mappingUnits.add(unit);
                ObjectKey key = ObjectSystemVisitor.getObjectKey(hostObj, objectType);
                this.selectDic.put(key, unit);
            }
        } else {
            MappingUnit unit = new MappingUnit();
            unit.addHost(hostObj);
            this.mappingUnits.add(unit);
            ObjectKey key = ObjectSystemVisitor.getObjectKey(hostObj, objectType);
            this.selectDic.put(key, unit);
        }
    }
}
