/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：默认的补充配置器,补充关联引用左右端和继承相关配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-24 12:20:53
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.TwoTuple;
import io.obase.core.common.Utils;
import io.obase.core.odm.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 默认的补充配置器
 */
public class DefaultComplementConfigurator implements IComplementConfigurator {

    /**
     * 补充配置管道中的下一个配置器
     */
    private final IComplementConfigurator next;

    /**
     * 默认的补充配置器
     *
     * @param next 补充配置管道中的下一个配置器
     */
    public DefaultComplementConfigurator(IComplementConfigurator next) {
        this.next = next;
    }

    /**
     * 补充配置管道中的下一个配置器
     *
     * @return 下一个补充配置器
     */
    @Override
    public IComplementConfigurator getNext() {
        return this.next;
    }

    /**
     * 根据类型配置项中的元数据配置指定的类型
     *
     * @param targetType    要配置的类型
     * @param configuration 包含配置元数据的类型配置项
     */
    @Override
    public void configure(StructuralType targetType, StructuralTypeConfiguration<?> configuration) {
        //配置关联引用的右端
        if (targetType instanceof EntityType) {
            EntityType entity = (EntityType) targetType;
            for (ReferenceElement referenceElement : entity.getReferenceElements()) {
                if (referenceElement instanceof AssociationReference) {
                    AssociationReference associationReference = (AssociationReference) referenceElement;
                    AssociationType associationType = associationReference.getAssociationType();
                    //如果当前的关联引用只有两个端的才需要配置
                    if (associationType.getAssociationEnds().size() == 2)
                        //左端一般都会侦测配置 尝试配置右端即可
                        //只配置有左端没右端的即可
                        if (!Utils.getStringIsEmpty(associationReference.getLeftEnd()) && Utils.getStringIsEmpty(associationReference.getRightEnd())) {
                            associationType.getAssociationEnds().stream().filter(p -> !p.getName().equals(associationReference.getLeftEnd())).findFirst()
                                    .ifPresent(end -> associationReference.setRightEnd(StringUtils.capitalize(end.getName())));
                        }
                }
            }
        }

        //是个基类 补充自己的具体类型属性和继承类的虚拟属性
        if (targetType.getDerivedTypes().size() > 0) {
            //增加自己的具体类型虚拟属性
            this.addConcreteTypeAttr(targetType, true);

            //将子类中与自己不同的属性补进来

            //将子类中与自己不同的属性补进来
            List<Attribute> needAdded = new ArrayList<>();
            this.createDerivedTypeAttr(targetType, needAdded);


            //补充虚拟属性
            for (Attribute add : needAdded) {
                //这些虚拟属性实际上不会用到 只是为了查询
                Attribute attr = new Attribute(add.getDataType(), add.getName());
                attr.setTargetField(add.getTargetField());
                attr.setNullable(add.getNullable());
                attr.setValueSetter(new ConcreteTypeSignValueSetter(new HashMap<>(), new HashMap<>(), null));
                attr.setValueGetter(new ConcreteTypeSignValueGetter(new HashMap<>(), new HashMap<>()));
                targetType.addAttribute(attr);
            }
        }

        //是继承类 补充自己的具体类型属
        if (targetType.getDerivingFrom() != null)
            this.addConcreteTypeAttr(targetType, false);
    }

    /**
     * 创建继承类在父类中的虚拟属性
     *
     * @param targetType 目标类型
     * @param needAdded  需要增加的虚拟属性
     */
    private void createDerivedTypeAttr(StructuralType targetType, List<Attribute> needAdded) {
        //循环所有继承类
        for (StructuralType derivedType : targetType.getDerivedTypes()) {
            //添加不是从父类继承而来的属性
            for (Attribute attr : derivedType.getAttributes())
                if (targetType.getAttributes().stream().noneMatch(p -> p.getTargetField().equalsIgnoreCase(attr.getTargetField())))
                    needAdded.add(attr);
            //递归处理子类
            this.createDerivedTypeAttr(derivedType, needAdded);
        }
    }

    /**
     * 增加自己的具体类型虚拟属性
     *
     * @param targetType 目标类型
     * @param isBase     是否只包装基类
     */
    private void addConcreteTypeAttr(StructuralType targetType, boolean isBase) {
        //取自己的类型判别标记
        TwoTuple<String, Object> sign = targetType.getConcreteTypeSign();
        //没配置 就不处理 之后会检查出来
        if (sign != null) {
            //获取继承链 构造字典
            HashMap<Class<?>, Object> dict1 = new HashMap<>();
            HashMap<Class<?>, Object> dict2 = new HashMap<>();
            List<StructuralType> chains = Utils.getDerivingChain(targetType);
            //沿着继承链处理每一级
            for (StructuralType structural : chains) {
                this.createConcreteTypeSignValueSetterDict(structural, dict1, dict2);
                for (StructuralType derivedType1 : structural.getDerivedTypes()) {
                    this.createConcreteTypeSignValueSetterDict(derivedType1, dict1, dict2);
                    for (StructuralType derivedType2 : derivedType1.getDerivedTypes()) {
                        this.createConcreteTypeSignValueSetterDict(derivedType2, dict1, dict2);
                    }
                }
            }

            Attribute attribute = targetType.findAttributeByTargetField(sign.getItem1());
            //如果这个属性没有定义
            if (attribute == null) {
                //补充一个类型判别的虚拟属性 固定为obase_gen_ct
                Attribute attr = new Attribute(sign.getItem2().getClass(), "Obase_gen_ct");
                attr.setTargetField(sign.getItem1());
                attr.setNullable(false);
                attr.setValueSetter(new ConcreteTypeSignValueSetter(dict1, dict2, new ConcreteTypeSignFiledSetter("obase_gen_ct")));
                attr.setValueGetter(new ConcreteTypeSignValueGetter(dict1, dict2));
                targetType.addAttribute(attr);
            } else {
                //定义了 只包装基类的
                if (isBase) {
                    //包装已有的属性即可
                    attribute.setValueSetter(new ConcreteTypeSignValueSetter(dict1, dict2, attribute.getValueSetter()));
                    attribute.setValueGetter(new ConcreteTypeSignValueGetter(dict1, dict2));
                    attribute.setNullable(false);
                }
            }
        }
    }

    /**
     * 组合ConcreteTypeSignValueSetter的字典参数
     *
     * @param targetType 目标类型
     * @param dict1      代理类的类型和具体类型判别标识的映射
     * @param dict2      原类的类型和具体类型判别标识的映射
     */
    private void createConcreteTypeSignValueSetterDict(StructuralType targetType, HashMap<Class<?>, Object> dict1,
                                                       HashMap<Class<?>, Object> dict2) {
        //保存代理类的类型和具体类型判别标识的映射
        if (!dict1.containsKey(targetType.getRebuildingType()) && targetType.getConcreteTypeSign() != null)
            dict1.put(targetType.getRebuildingType(), targetType.getConcreteTypeSign().getItem2());
        //保存原类的类型和具体类型判别标识的映射
        if (!dict2.containsKey(targetType.getClrType()) && targetType.getConcreteTypeSign() != null)
            dict2.put(targetType.getClrType(), targetType.getConcreteTypeSign().getItem2());
    }
}
