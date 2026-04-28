/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：默认的具体类型区分器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-18 11:48:05
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.util.HashMap;

/**
 * 默认的具体类型区分器，根据类型代码选择一个具体类型。
 * 如果没有配置具体类型区分器，则使用本类作为默认的区分器。
 */
public class ConcreteTypeDiscriminator implements IConcreteTypeDiscriminator {

    /**
     * 具体类型区分字典
     */
    private final HashMap<String, StructuralType> dictionary;

    /**
     * 初始化默认的具体类型区分器
     *
     * @param dictionary 具体类型区分字典
     */
    public ConcreteTypeDiscriminator(HashMap<String, StructuralType> dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * 根据类型代码选择一个具体类型
     *
     * @param typeCode 类型代码
     * @return 具体的结构化类型
     */
    @Override
    public StructuralType discriminate(Object typeCode) {

        //如果没有配置具体类型区分字典 则返回null 表示无法区分具体类型
        if (this.dictionary == null)
            return null;
        //根据类型代码在具体类型区分字典中查找对应的具体类型 如果找到则返回 否则返回null
        if (this.dictionary.containsKey(typeCode.toString())) {
            return this.dictionary.get(typeCode.toString());
        }
        return null;
    }
}
