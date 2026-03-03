/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：多方关联的元组标准化函数及其反函数.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-24 17:39:30
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.implicitAssociationConfigor;

import io.obase.common.ObjectReferencePack;
import io.obase.common.Tuple;
import io.obase.common.TupleUtils;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 适用于多方关联的元组标准化函数及其反函数。
 */
public class MultiAssociationTupleStandardizer implements ITupleStandardizer {

    /**
     * 关联引用的属性类型
     */
    private final Property property;

    /**
     * 构造适用于多方关联的元组标准化函数及其反函数
     *
     * @param property 关联引用的属性
     */
    public MultiAssociationTupleStandardizer(Property property) {
        this.property = property;
    }

    /**
     * 元组标准化函数的反函数，将标准元组转换成被引对象元组。
     *
     * @param tupleItems 被引对象组成的元组（不限定元组的数据类型，只要逻辑上为元组即可）
     * @return 标准化元组的项序列
     */
    @Override
    public Object revert(Object[] tupleItems) {
        //多方关联 传入的对象即为关联型对象集合
        List<Object> result = new ArrayList<>();

        //要把关联型对象中每个对象都转成元组
        for (Object item : tupleItems) {
            ObjectReferencePack<Class<?>> type = new ObjectReferencePack<>();
            Utils.getIsMultiple(this.property, type);
            //元组泛型参数
            Class<?>[] tupleTypeList = Utils.getTupleGenericTypeArguments(this.property.getField());

            //每个值都是一组对象
            if (item instanceof Object[]) {
                Object[] endObjects = (Object[]) item;
                List<Object> realObjects = new ArrayList<>();
                //按照元组泛型参数顺序加入
                for (Class<?> tupleType : tupleTypeList) {
                    //可能没有值 用null代替
                    Object endObj = Arrays.stream(endObjects).filter(p -> p != null && (tupleType == p.getClass() || tupleType.isAssignableFrom(p.getClass()))).findFirst().orElse(null);
                    realObjects.add(endObj);
                }

                //创建对象
                try {
                    Object tuple = type.realValue.getConstructor(tupleTypeList).newInstance(realObjects);
                    //加入元组
                    result.add(tuple);
                } catch (Exception e) {
                    result.add(TupleUtils.of(realObjects.toArray(new Object[0])));
                }

            }
        }

        //返回数组
        return result.toArray();
    }

    /**
     * 元组标准化函数，将被引对象元组转换成标准元组。
     *
     * @param referredTuple 表示标准化元组的对象数组
     * @return 被引对象组成的元组（不限定元组的数据类型，只要逻辑上为元组即可）。被引对象是指关联引用指向的对象，如果关联引用是多重性的，它是指其中的一个。
     */
    @Override
    public Object[] standardize(Object referredTuple) {
        //多方关联 传入的对象即为端对象
        List<Object> result = new ArrayList<>();
        //如果是集合 就取出每一个
        if (referredTuple instanceof Iterable) {
            Iterable<?> iEnumerable = (Iterable<?>) referredTuple;
            for (Object o : iEnumerable) {
                result.add(o);
            }
        } else {
            result.add(referredTuple);
        }

        //处理每个元组 变换成每个值的数组
        List<Object> tupleResult = new ArrayList<>();
        for (Object r : result) {
            if (r instanceof Tuple) {
                Tuple tuple = (Tuple) r;
                tupleResult.add(tuple.getItems());
            }
        }
        return tupleResult.toArray();
    }
}
