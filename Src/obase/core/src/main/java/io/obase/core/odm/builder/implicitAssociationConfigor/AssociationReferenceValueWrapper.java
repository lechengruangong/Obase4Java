/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：排序依据接口,提供排序字段访问器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-24 17:56:20
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.implicitAssociationConfigor;

import io.obase.core.common.Utils;
import io.obase.core.odm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 在隐式关联的显式化配置中，将关联引用的值包装成关联实例。
 */
public class AssociationReferenceValueWrapper implements IValueGetter, IValueSetter {

    /**
     * 被包装的关联引用所在关联端在关联型上的索引号（从1开始计数）
     */
    private final byte associationEndIndex;

    /**
     * 关联引用的关联类型
     */
    private final AssociationType associationType;

    /**
     * 基础取值器，用于取出当前关联引用的原始值
     */
    private final IValueGetter foundationGetter;

    /**
     * 基础设值器，用于为当前关联引用设置原始值。
     */
    private final IValueSetter foundationSetter;

    /**
     * 是否是多方隐式关联
     */
    private final boolean isMultiAssociation;

    /**
     * 指示被包装的关联引用是否是多重的
     */
    private final boolean isMultiple;

    /**
     * 元组标准化函数及其反函数
     */
    private final ITupleStandardizer tupleStandardizer;

    /**
     * 初始化AssociationReferenceValueWrapper类的新实例
     *
     * @param associationEndIndex 关联引用所在关联端在关联型上的索引号（从1开始计数）
     * @param associationType     关联引用的关联类型
     * @param foundationGetter    关联引用的基础取值器
     * @param foundationSetter    关联引用的基础设值器
     * @param isMultiAssociation  是否是多方隐式关联
     * @param isMultiple          指示关联引用是否是多重性的
     * @param tupleStandardizer   元组标准化函数及其反函数
     */
    public AssociationReferenceValueWrapper(byte associationEndIndex, AssociationType associationType, IValueGetter foundationGetter, IValueSetter foundationSetter, boolean isMultiAssociation, boolean isMultiple, ITupleStandardizer tupleStandardizer) {
        this.associationEndIndex = associationEndIndex;
        this.associationType = associationType;
        this.foundationGetter = foundationGetter;
        this.foundationSetter = foundationSetter;
        this.isMultiAssociation = isMultiAssociation;
        this.isMultiple = isMultiple;
        this.tupleStandardizer = tupleStandardizer;
    }

    /**
     * 从指定对象取值
     *
     * @param obj 目标对象
     * @return 值
     */
    @Override
    public Object getValue(Object obj) {
        //原始值
        Object originalValue = this.foundationGetter.getValue(obj);

        //多方关联才加入处理
        if (!this.isMultiAssociation)
            //两方关联 直接返回第1个
            return this.tupleStandardizer.standardize(originalValue)[0];

        //如果是空 直接返回
        if (originalValue == null)
            return null;

        //最终结果
        List<Object> resultList = new ArrayList<>();

        //隐式关联型的构造函数
        IInstanceConstructor assConstructor = this.associationType.getConstructor();

        //原始值 即端对象
        //对原始值进行处理 每个圆度都是 将每个端都展开成关联端对象数组[End1,End2 ... End] 无一定的顺序
        Object[] standardTuples = this.tupleStandardizer.standardize(originalValue);

        //所有的关联端
        List<AssociationEnd> ends = this.associationType.getAssociationEnds();

        //每个对象构造一个隐式关联型对象
        for (Object standard : standardTuples) {
            //构造关联型
            Object result = assConstructor.construct(null);
            //每个值都是一组对象
            if (standard instanceof Object[]) {
                Object[] endObjects = (Object[]) standard;
                //设值 每个端
                for (AssociationEnd end : ends) {
                    //自己这一端
                    if (end.getName().equals("End" + this.associationEndIndex))
                        end.setValue(result, obj);
                    else {
                        //其他端
                        //每个端对象
                        for (Object endObject : endObjects) {
                            if (endObject.getClass().equals(end.getEntityType().getClrType()))
                                end.setValue(result, endObject);
                        }

                        resultList.add(result);
                    }
                }
            }
        }

        //根据多重性 处理为单值和集合
        if (!this.isMultiple)
            return resultList.get(0);

        return resultList;
    }

    /**
     * 获取设值模式
     *
     * @return 设值模式
     */
    @Override
    public EValueSettingMode getMode() {
        return this.foundationSetter.getMode();
    }

    /**
     * 为对象设值
     *
     * @param obj   目标对象
     * @param value 值对象
     */
    @Override
    public void setValue(Object obj, Object value) {
        //多方关联才加入处理
        if (!this.isMultiAssociation) {
            //两方关联 直接设置值
            this.setValueCore(obj, this.tupleStandardizer.revert(new Object[]{value}));
            return;
        }

        //关联型对象集合
        List<Object> assTypeObj = Utils.getObjectList(value);

        //所有的关联端
        List<AssociationEnd> ends = this.associationType.getAssociationEnds();
        List<Object> endObjects = new ArrayList<>();
        //处理每个关联对象
        for (Object assObj : assTypeObj) {
            //每个处理成object[]
            List<Object> endObjs = new ArrayList<>();
            for (AssociationEnd end : ends) {
                if (end.getName().equals("End" + this.associationEndIndex))
                    continue;
                Object endObj = end.getValue(assObj);
                if (endObj != null)
                    endObjs.add(endObj);
            }

            //如果不够数 加一些空值
            if (endObjs.size() < ends.size() - 1) {
                int end = ends.size() - 1 - endObjs.size();
                int start = 0;
                while (start < end) {
                    endObjs.add(null);
                    start++;
                }
            }

            endObjects.add(endObjs.toArray());
        }

        //获取真正要设值的结果
        Object revertObj = this.tupleStandardizer.revert(endObjects.toArray());

        if (this.isMultiple) {
            if (revertObj instanceof Iterable) {
                Iterable<Object> enumerable = (Iterable<Object>) revertObj;
                //有值 直接设置
                if (enumerable.iterator().hasNext())
                    this.setValueCore(obj, enumerable);
            } else if (revertObj instanceof Object[]) {
                Object[] objs = (Object[]) revertObj;
                List<Object> objList = Arrays.asList(objs);
                this.setValueCore(obj, objList);
            }

        } else {
            //应只有一个值
            for (Object item : (Iterable<Object>) revertObj)
                this.setValueCore(obj, item);
        }
    }

    /**
     * 具体的设置值方法
     *
     * @param targetObj 目标
     * @param value     值
     */
    private void setValueCore(Object targetObj, Iterable<?> value) {
        EValueSettingMode settingMode = this.foundationSetter.getMode();
        switch (settingMode) {
            case Assignment:
                this.foundationSetter.setValue(targetObj, value);
                break;
            case Appending:
                if (value == null) return;
                for (Object valueItem : value)
                    this.foundationSetter.setValue(targetObj, valueItem);
                break;
        }
    }

    /**
     * 具体的设置值方法
     *
     * @param targetObj 目标
     * @param value     值
     */
    private void setValueCore(Object targetObj, Object value) {
        //前置过滤，如果value实现了IEnumerable或IEnumerable<>，调用另一重载。
        Class<?> valueType = value.getClass();
        if (!valueType.equals(String.class) && Iterable.class.isAssignableFrom(valueType)) {
            Iterable<?> iEnumerableValue = (Iterable<?>) value;
            this.setValueCore(targetObj, iEnumerableValue);
        } else {
            this.foundationSetter.setValue(targetObj, value);
        }
    }
}
