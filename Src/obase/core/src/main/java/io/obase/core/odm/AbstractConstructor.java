/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：基类的构造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-26 16:12:18
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.util.Arrays;
import java.util.List;

/**
 * 适用于基类的构造器，它根据类型代码（作为构造函数）选择一个具体类型，构造该具体类型的实例。
 */
public class AbstractConstructor extends InstanceConstructor {

    /**
     * 具体类型判别器
     */
    private final IConcreteTypeDiscriminator typeDiscriminator;

    /**
     * 指示派生类型的属性的名称
     */
    private final String typeAttributeName;

    /**
     * 初始化AbstractConstructor类的实例
     *
     * @param parameters        派生类型的构造函数参数
     * @param typeDiscriminator 派生类型判别器
     * @param typeAttributeName 指示派生类型的属性的名称
     */
    public AbstractConstructor(List<Parameter> parameters, IConcreteTypeDiscriminator typeDiscriminator,
                               String typeAttributeName) {
        //复制参数
        if (parameters != null && parameters.size() > 0) {
            for (Parameter parameter : parameters) {
                this.setParameter(parameter.getName(), parameter.getElementName(), parameter.getValueConverter());
            }
        }
        //加入判断区别的参数
        this.setParameter("obase_gen_typeCode", typeAttributeName);
        //判别器
        this.typeDiscriminator = typeDiscriminator;
        //名称
        this.typeAttributeName = typeAttributeName;
    }

    /**
     * 获取派生类型的属性的名称
     *
     * @return 派生类型的属性的名称
     */
    public String getTypeAttributeName() {
        return this.typeAttributeName;
    }

    /**
     * 构造对象
     *
     * @param arguments 构造函数参数
     * @return 构造出的对象
     */
    @Override
    public Object construct(Object[] arguments) {
        //判别类型
        StructuralType structuralType = this.getDiscriminateType(arguments);

        //是自己 用基础类型构造器构造 否则用自己的构造器
        IInstanceConstructor constructor = structuralType.equals(this.getInstanceType()) ? structuralType.getBaseTypeConstructor() : structuralType.getConstructor();

        //还是基类的构造器 继续传递
        if (constructor instanceof AbstractConstructor) {
            AbstractConstructor abstractConstructor = (AbstractConstructor) constructor;
            return abstractConstructor.construct(arguments);
        }

        //去掉判断字段
        Object[] realValues = Arrays.stream(arguments).limit(arguments.length - 1).toArray();
        //构造具体值
        return constructor.construct(realValues);
    }

    /**
     * 根据字段获取判别类型
     *
     * @param arguments 构造函数的参数集合
     * @return 具体的结构化类型
     */
    public StructuralType getDiscriminateType(Object[] arguments) {
        if (arguments == null || arguments.length < 1)
            throw new IllegalArgumentException("无法获取用于判别类型的属性" + this.typeAttributeName + ".");
        //获取判别用值
        Object value = arguments[arguments.length - 1];

        if (value == null)
            throw new IllegalArgumentException("用于判别类型的属性" + this.typeAttributeName + "值不能为空.");
        String typeCode = value.toString();
        //获取判别的类型
        StructuralType discriminate = this.typeDiscriminator.discriminate(typeCode);
        //如果是空 没找到
        if (discriminate == null)
            throw new IllegalArgumentException("判别器" + this.typeDiscriminator.getClass() + "无法使用值" + typeCode + "获取到" + this.getInstanceType().getName() + "类型的具体类型.");
        //判断当前类型是否是基类的派生类型
        if (!this.getInstanceType().getClrType().isAssignableFrom(discriminate.getClrType()))
            throw new IllegalArgumentException("判别器" + this.typeDiscriminator.getClass() + "使用值" + typeCode + "获取的具体类型" + discriminate.getClrType().getName() + "不是" + this.getInstanceType().getName() + "的派生类型.");
        //此类型配置的判别标记代码值
        String discriminatedTypeCode = discriminate.getConcreteTypeSign().getItem2().toString();
        //判断值是否一致
        if (!discriminatedTypeCode.equalsIgnoreCase(typeCode))
            throw new IllegalArgumentException(discriminate.getClrType().getName() + "类型配置的类型判别值为" + discriminatedTypeCode + ",而类型判别器" + this.typeDiscriminator.getClass() + "获取到具体类型" + discriminate.getClrType().getName() + "时使用的值为" + typeCode + "而不是" + discriminatedTypeCode + ".");
        return discriminate;
    }

}
