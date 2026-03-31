/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化实体类型.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-30 14:29:05
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.serialization;

import io.obase.core.common.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 序列化实体类型
 */
public class SerializationEntity {

    /**
     * 类型对应的对象系统类型
     */
    private final Class<?> clrType;

    /**
     * 所有的元素集合 包含属性和构造参数
     */
    private final List<SerializationElement> elements = new ArrayList<>();

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 构造器
     */
    private SerializationConstructor constructor;

    /**
     * 初始化序列化实体类型
     *
     * @param clrType 类型对应的对象系统类型
     */
    public SerializationEntity(Class<?> clrType) {
        this.clrType = clrType;
        this.name = clrType.getName();
    }

    /**
     * 获取所有的元素集合 包含属性和引用
     *
     * @return 所有的元素集合 包含属性和引用
     */
    public List<SerializationElement> getElements() {
        return this.elements;
    }

    /**
     * 获取所有的属性集合
     *
     * @return 所有的属性集合
     */
    public List<SerializationAttribute> getAttributes() {
        return this.elements.stream().filter(p -> p instanceof SerializationAttribute).map(p -> (SerializationAttribute) p).collect(Collectors.toList());
    }

    /**
     * 获取所有的构造函数参数集合
     *
     * @return 所有的构造函数参数集合
     */
    public List<SerializationConstructorParameter> getConstructorParameters() {
        if (this.constructor == null)
            return null;
        return new ArrayList<>(this.constructor.getParameters().values());
    }

    /**
     * 获取所有的引用集合
     *
     * @return 所有的引用集合
     */
    public List<SerializationReference> getReferences() {
        return this.elements.stream().filter(p -> p instanceof SerializationReference).map(p -> (SerializationReference) p).collect(Collectors.toList());
    }

    /**
     * 获取类型对应的对象系统类型
     *
     * @return 类型对应的对象系统类型
     */
    public Class<?> getClrType() {
        return this.clrType;
    }

    /**
     * 获取类型名称
     *
     * @return 类型名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取构造器
     *
     * @return 构造器
     */
    public SerializationConstructor getConstructor() {
        return this.constructor;
    }

    /**
     * 设置构造器
     *
     * @param constructor 构造器
     */
    public void setConstructor(SerializationConstructor constructor) {
        this.constructor = constructor;
    }

    /**
     * 完整性检查
     *
     * @param errDictionary 错误信息字典
     */
    public void integrityCheck(Map<String, List<String>> errDictionary) {
        //错误消息
        List<String> message = new ArrayList<>();
        //检查属性
        for (SerializationAttribute attribute : this.getAttributes()) {
            if (Utils.getStringIsEmpty(attribute.getName()))
                message.add("序列化实体的属性名称不能为空.");
            if (attribute.getValueGetter() == null)
                message.add(this.getName() + "的属性" + attribute.getName() + "没有取值器.");
            if (attribute.getValueSetter() == null)
                message.add(this.getName() + "的属性" + attribute.getName() + "没有设值器.");
        }

        //检查构造器
        if (this.getConstructor() == null) {
            message.add(this.getName() + "没有构造器.");
        } else {
            if (this.getConstructor().getRealParameterCount() != this.getConstructorParameters().size())
                message.add(this.getName() + "的构造器应有" + this.getConstructor().getRealParameterCount() + "参数,实际上仅配置了" + this.getConstructorParameters().size() + "个.");
        }

        //检查引用
        for (SerializationReference reference : this.getReferences()) {
            if (Utils.getStringIsEmpty(reference.getName()))
                message.add("序列化实体的引用名称不能为空.");
            if (reference.getValueGetter() == null)
                message.add(this.getName() + "的引用" + reference.getName() + "没有取值器.");
            if (reference.getValueSetter() == null)
                message.add(this.getName() + "的引用" + reference.getName() + "没有设值器.");
        }

        //如果有检查失败消息
        if (message.size() > 0) {
            //就与现有的问题合并
            if (errDictionary.containsKey(this.name))
                errDictionary.get(this.name).addAll(message);
            else
                errDictionary.put(this.name, message);
        }
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "SerializationEntity{" +
                "name=" + this.name +
                ", clrType='" + this.clrType + '\'' +
                '}';
    }
}
