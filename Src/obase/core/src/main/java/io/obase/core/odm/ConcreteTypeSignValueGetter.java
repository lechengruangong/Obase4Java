/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：具体判别标记的取值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 17:22:20
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.util.HashMap;

/**
 * 具体判别标记的取值器
 */
public class ConcreteTypeSignValueGetter implements IValueGetter {

    /**
     * 判别标识集合1 内存代理类型
     */
    private final HashMap<Class<?>, Object> rebuildingTypeValues;

    /**
     * 判别标识集合1 内存Clr类型
     */
    private final HashMap<Class<?>, Object> clrTypeValues;

    /**
     * 具体判别标记的取值器
     *
     * @param values1 判别标识集合1 内存代理类型
     * @param values2 判别标识集合1 内存Clr类型
     */
    public ConcreteTypeSignValueGetter(HashMap<Class<?>, Object> values1, HashMap<Class<?>, Object> values2) {
        this.rebuildingTypeValues = values1;
        this.clrTypeValues = values2;
    }

    /**
     * 从指定对象取值
     *
     * @param obj 目标对象
     * @return 值
     */
    @Override
    public Object getValue(Object obj) {
        if (this.rebuildingTypeValues.containsKey(obj.getClass()))
            return this.rebuildingTypeValues.get(obj.getClass());
        if (this.clrTypeValues.containsKey(obj.getClass()))
            return this.clrTypeValues.get(obj.getClass());
        return null;
    }
}
