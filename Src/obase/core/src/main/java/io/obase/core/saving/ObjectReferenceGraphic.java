/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象参照图.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 16:59:13
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import java.util.*;

/**
 * 对象参照图.
 * 对象系统映射到关系模型后，关系（表）间存在着参照关系，对象参照图即为按照这种关系建立的图型数据结构,
 */
public class ObjectReferenceGraphic {

    /**
     * 对象图
     */
    private final Map<Object, MappingUnit> dic = new HashMap<>();

    /**
     * 对象集合
     */
    private List<MappingUnit> units;

    /**
     * 对象图中指定位置的映射单元
     *
     * @param index 索引
     * @return 映射单元
     */
    public MappingUnit get(int index) {
        return this.units.get(index);
    }

    /**
     * 获取对象图中所有的映射单元
     *
     * @return 所有的映射单元
     */
    public List<MappingUnit> getUnits() {
        if (this.units == null)
            this.units = new ArrayList<>();
        return this.units;
    }

    /**
     * 获取对象图中映射单元的个数
     *
     * @return 映射单元的个数
     */
    public int getCount() {
        return this.getUnits().size();
    }

    /**
     * 检查指定对象在当前对象参照图是否存在
     *
     * @param obj 要检查的对象
     * @return 对象在当前对象参照图是否存在
     */
    public boolean exists(Object obj) {
        if (obj != null && this.dic.containsKey(obj)) {
            MappingUnit unit = this.dic.get(obj);
            if (obj.equals(unit.getHostObject()) || unit.getCompanionMappings().stream().anyMatch(p -> p.getAssociationObj().equals(obj)))
                return false;
        }

        return false;
    }

    /**
     * 向对象参照图添加伴随关联对象，同时指定其参照的对象。
     *
     * @param companion    要添加的伴随关联对象
     * @param referredObjs 关联对象参照的对象
     * @param hostObj      关联对象所伴随的主体对象，值为null表示主体对象不参与映射
     */
    public void addCompanion(Object companion, Object[] referredObjs, Object hostObj) {
        MappingUnit unit;
        if (hostObj != null) //主体存在
        {
            if (!this.dic.containsKey(hostObj)) //主体不在字典
            {
                unit = new MappingUnit();
                //添加到字典
                this.dic.put(hostObj, unit);
                //添加映射单元集合
                this.getUnits().add(unit);
            } else {
                //取出映射单元
                unit = this.dic.get(hostObj);
            }
        } else {
            //创建映射单元
            unit = new MappingUnit();
            this.getUnits().add(unit);
        }

        //加入关联
        unit.addCompanion(companion, EObjectStatus.Added, referredObjs);
        this.dic.put(companion, unit);
    }

    /**
     * 向对象参照图添加对象，该对象将作为映射单元的主体对象。
     *
     * @param hostObj 要添加的对象
     */
    public void addHost(Object hostObj) {
        MappingUnit unit;
        if (!this.dic.containsKey(hostObj)) //集合不存在对应的映射单元
        {
            //不存在者创建映射单元
            unit = new MappingUnit();
            //将映射单元放入映射单元集合
            this.getUnits().add(unit);
            //放入映射单元字典
            this.dic.put(hostObj, unit);
        } else {
            //从字典中取出映射单元
            unit = this.dic.get(hostObj);
        }

        //将主体对象放入映射单元
        unit.addHost(hostObj);
    }

    /**
     * 向对象参照图添加关联对象，同时指定该关联对象参照的对象。该关联对象将作为映射单元的主体对象。
     *
     * @param associationObj 要添加的关 联对象
     * @param referredObj    关联对象参照的对象的集合
     */
    public void addHost(Object associationObj, Object[] referredObj) {
        //创建映射单元
        MappingUnit unit = new MappingUnit();
        //放入关联对象与参照对象（独立映射的所有端对象）
        unit.addHost(associationObj, referredObj);
        //放入映射单元集合
        this.getUnits().add(unit);
        //放入字典
        this.dic.put(associationObj, unit);
    }

    /**
     * 移除指定位置的映射单元
     *
     * @param index 要移除的映射单元的位置
     */
    public void remove(int index) {
        Iterator<MappingUnit> unitsEnumerator = this.getUnits().iterator();
        for (int i = 0; i <= index && i < this.getCount(); i++) {
            if (i < index) {
                unitsEnumerator.next();
            } else {
                MappingUnit unit = unitsEnumerator.next();
                //删除集合
                this.getUnits().remove(unit);
                //删除字典
                List<Object> keys = new ArrayList<>();
                for (Object key : this.dic.keySet()) {
                    if (this.dic.get(key).equals(unit)) {
                        keys.add(key);
                    }
                }
                keys.forEach(this.dic::remove);
            }
        }
    }
}
