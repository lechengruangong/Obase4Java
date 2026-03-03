/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：默认的代理类型生成器,判断是否需要生成代理类型和生成代理类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-24 11:38:36
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.ObjectReferencePack;
import io.obase.common.TwoTuple;
import io.obase.core.common.Utils;
import io.obase.core.odm.*;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认的代理类型生成器
 */
public class DefaultProxyTypeGenerator implements IProxyTypeGenerator {

    /**
     * 代理类型生成管道中的下一个生成器
     */
    private final IProxyTypeGenerator next;

    /**
     * 构造默认的代理类型生成器
     */
    public DefaultProxyTypeGenerator(IProxyTypeGenerator next) {
        this.next = next;
    }

    /**
     * 获取代理类型生成管道中的下一个生成器
     *
     * @return 下一个类型生成器
     */
    @Override
    public IProxyTypeGenerator getNext() {
        return this.next;
    }

    /**
     * 为指定类型的代理类型定义成员
     *
     * @param typeBuilder  一个类型建造器，用于定义代理类型
     * @param objType      要为其定义代理类的类型，即代理类的基类
     * @param configurator 上述类型的配置器
     */
    @Override
    public DynamicType.Builder<?> defineMembers(DynamicType.Builder<?> typeBuilder, ObjectType objType, IObjectTypeConfigurator configurator) {
        //处理新实例对象的构造函数
        if (objType.getNewInstanceConstructor() != null) {
            //参数
            Class<?>[] paraObjs = ((InstanceConstructor) objType.getNewInstanceConstructor()).getParameterTypes().toArray(new Class<?>[0]);

            Constructor<?> ctor;
            try {
                ctor = objType.getClrType().getDeclaredConstructor(paraObjs);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("无法获取" + objType.getName() + "的构造函数.", e);
            }

            MethodCall call = MethodCall.invoke(ctor)
                    .withAllArguments();

            typeBuilder = typeBuilder.defineConstructor(Visibility.PUBLIC)
                    .withParameters(paraObjs)
                    .intercept(call);
        }

        //生成外键
        StructuralTypeConfiguration.ForeignKeyAdder foreignKeyAdder = new StructuralTypeConfiguration.ForeignKeyAdder(objType, typeBuilder);
        foreignKeyAdder.guarantee(objType, null);

        if (configurator instanceof StructuralTypeConfiguration) {
            StructuralTypeConfiguration<?> structuralTypeConfiguration = (StructuralTypeConfiguration<?>) configurator;
            structuralTypeConfiguration.setForeignKeyAdder(foreignKeyAdder);
        }
        Visibility fieldAttributes = Visibility.PRIVATE;
        Visibility methodAttributes = Visibility.PUBLIC;

        typeBuilder = foreignKeyAdder.getProxyTypeBuilder();
        //加入介入者接口字段
        typeBuilder = typeBuilder.defineField("intervener", IIntervener.class, fieldAttributes);
        //加入注册介入者接口方法
        DynamicType.Builder.MethodDefinition.ParameterDefinition.Initial<?> methodBuilder = typeBuilder.defineMethod("registerIntervener", void.class, methodAttributes);
        //定义参数
        typeBuilder = methodBuilder.withParameter(IIntervener.class, "intervener").intercept(FieldAccessor.ofField("intervener").setsArgumentAt(0));
        //是否延迟加载的字段
        typeBuilder = typeBuilder.defineField("forbidLazyLoading", boolean.class, fieldAttributes);
        //禁用延迟加载方法
        DynamicType.Builder.MethodDefinition.ParameterDefinition.Initial<?> forbidLazyLoadingMethod = typeBuilder.defineMethod("forbidLazyLoading", void.class, methodAttributes);
        typeBuilder = forbidLazyLoadingMethod.intercept(FieldAccessor.ofField("forbidLazyLoading").setsValue(true));
        //启用延迟加载方法
        DynamicType.Builder.MethodDefinition.ParameterDefinition.Initial<?> enableLazyLoadingMethod = typeBuilder.defineMethod("enableLazyLoading", void.class, methodAttributes);
        typeBuilder = enableLazyLoadingMethod.intercept(FieldAccessor.ofField("forbidLazyLoading").setsValue(false));

        HashSet<String> hasDef = new HashSet<>();
        //遍历触发器重写属性或方法
        for (IBehaviorTrigger tri : configurator.getBehaviorTriggersI()) {
            Method method = tri.override(objType.getClrType());
            List<TypeElementConfiguration> elements = Arrays.stream(configurator.getBehaviorElementsI(tri)).map(p -> (TypeElementConfiguration) p).collect(Collectors.toList());
            for (TypeElementConfiguration elem : elements) {
                if (elem instanceof ILazyLoadingConfiguration) {
                    ILazyLoadingConfiguration re = (ILazyLoadingConfiguration) elem;
                    if (re.getEnableLazyLoading() && !hasDef.contains(elem.getName() + "HasCalled")) {
                        //定义一个HasCalled字段
                        typeBuilder = typeBuilder.defineField(elem.getName() + "HasCalled", boolean.class, fieldAttributes);
                        typeBuilder = typeBuilder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(AssociationGetMethodInterceptor.class));
                        hasDef.add(elem.getName() + "HasCalled");
                    }
                }
            }
            for (TypeElementConfiguration elem : elements) {
                if (elem.getElementType().equals(EElementType.Attribute)) {
                    typeBuilder = typeBuilder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(AttributeGetMethodInterceptor.class));
                }
            }

        }

        TwoTuple<String, Object> signAttr = Utils.getDerivedConcreteTypeSign(objType);
        if (signAttr != null) {
            //生成一个字段obase_gen_ct 作为补充管道属性的承载
            typeBuilder = typeBuilder.defineField("obase_gen_ct", signAttr.getItem2().getClass(), Visibility.PUBLIC);
            typeBuilder = typeBuilder.defineMethod("getObase_gen_ct", signAttr.getItem2().getClass(), Visibility.PUBLIC).intercept(FieldAccessor.ofField("obase_gen_ct"));
        }

        return typeBuilder;
    }

    /**
     * 判定指定的类型是否需要生成代理类型
     *
     * @param objType      要判定的类型
     * @param configurator 上述类型的配置器
     * @return 是否需要生成代理类型
     */
    @Override
    public boolean should(ObjectType objType, IObjectTypeConfigurator configurator) {
        //获取定义的外键
        List<Attribute> attrs = Utils.getDefinedForeignAttributes(objType, null, new ObjectReferencePack<>());
        //获取顶级父类的类型判别标记对应的属性
        TwoTuple<String, Object> signAttr = Utils.getDerivedConcreteTypeSign(objType);
        //默认的判断条件 1.有触发器 2.有定义的外键 3.没有标记属性
        return (long) configurator.getBehaviorTriggersI().size() > 0 || attrs.size() > 0 || signAttr != null;
    }
}
