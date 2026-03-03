/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：一种外键保证机制的具体实现,使用派生类型定义缺失的外键属性.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 15:07:10
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.common.Utils;
import io.obase.core.expression.Expression;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * 提供一种外键保证机制的具体实现，该机制使用派生类型定义缺失的外键属性。
 * 说明
 * 定义属性的方式是定义一个派生类型，将所需属性定义在该派生类型上，并将该类型作为代理类型（ProxyType）。
 * 警告
 * 如果某一类型缺少外键属性，但其代理类型已存在，将引发ForeignKeyGuarantingException异常（代理类型已存在，无法通过定义派生类的方式追加
 * 定义外键属性）。
 * 实施说明
 * 每一属性（Attribute）定义一个公有字段，字段名称为属性名。调用ImpliedTypeManager.ApplyType(baseType,
 * fields)，其中：
 * 1）baseType的实参为objType.ClrType；
 * 2）fields的实参依据要定义的外鍵属性生成。
 * 为每一属性（Attribute）设置取值器和设置器，使用委托取/设值器。委托可基于访问上述字段的MemberExpression生成。
 * 将生成的代理类型赋予ObjectType的ProxyType属性。
 * 更改类型的构造器，以确保反持久化过程中创建派生类型的实例。更改构造器的方法请参见顺序图“Odm.Builder/生成模型”。
 */
public class DerivingBasedForeignKeyGuarantor extends ForeignKeyGuarantor {

    /**
     * 在外键属性缺失的情况下定义所缺的属性
     *
     * @param attrs   要定义的外键属性
     * @param objType 要定义属性的类型
     */
    @Override
    protected void defineMissing(Attribute[] attrs, ObjectType objType) {
        //字段们
        List<FieldDescriptor> fields = new ArrayList<>();
        for (Attribute att : attrs) {
            FieldDescriptor fieldDescriptor = new FieldDescriptor(att.getDataType(), att.getName());
            fieldDescriptor.setHasGetter(true);
            fieldDescriptor.setHasSetter(true);
            fields.add(fieldDescriptor);
        }

        //定义隐含类型
        Class<?> proxyType = ImpliedTypeManager.getCurrent().applyType(objType.getClrType(), fields.toArray(new FieldDescriptor[0]), null);

        //为每个字段弄一个设值器一个取值器
        for (int i = 0; i < attrs.length; i++) {

            //设值器
            try {
                Method getMethod = proxyType.getDeclaredMethod("get" + fields.get(i).getPropertyName());
                IValueGetter getter = Utils.makeDelegateValueGetter(getMethod);

                attrs[i].setValueGetter(getter);
            } catch (Exception exception) {
                throw new RuntimeException("无法创建取值器,请参考内部异常.", exception);
            }

            //设值器
            try {
                Method setMethod = proxyType.getDeclaredMethod("set" + fields.get(i).getPropertyName(), attrs[i].getDataType());
                attrs[i].setValueSetter(ValueSetter.create(setMethod, EValueSettingMode.Assignment));
            } catch (Exception exception) {
                throw new RuntimeException("无法创建设值器,请参考内部异常.", exception);
            }

            //构造器
            try {
                //处理参数
                List<Parameter> parameterList = objType.constructor.getParameters();
                Constructor<?> constructor;
                if (parameterList != null && parameterList.size() > 0) {
                    Object[] types = parameterList.stream().map(Parameter::getExpression).toArray();
                    List<Class<?>> list = new ArrayList<>();
                    for (Object o : types) {
                        if (o instanceof Expression) {
                            Expression t = (Expression) o;
                            list.add(t.getType());
                        }
                    }
                    constructor = proxyType.getConstructor(list.toArray(new Class<?>[0]));
                } else {
                    constructor = proxyType.getConstructor();
                }
                constructor.setAccessible(true);
                ReflectionConstructor proxyCon = new ReflectionConstructor(constructor);

                if (parameterList != null) {
                    for (Parameter p : parameterList) {
                        proxyCon.setParameter(p.getName(), p.getElementName(), p.getValueConverter(), p.getExpression());
                    }
                }

                //对象构造器
                objType.setConstructor(proxyCon);

            } catch (Exception exception) {
                throw new RuntimeException("无法创建构造器,请参考内部异常.", exception);
            }

            //生成代理类对象
            objType.setProxyType(proxyType);
        }
    }
}
