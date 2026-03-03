/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：属性路径,属性树中某一节点相对于根节点的寻址结构.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:44:16
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 表示属性路径。
 * 将对象型视为根节点，该对象型直接包含的所有属性视为一级节点。这些属性中有一部分为复杂属性，复杂属性的类型所具有的属性视为该属性的子属性。依此类推，可以将对象的属
 * 性体系视为一个树型结构，称为该对象或对象类型的属性树。
 * 属性路径是指属性树中某一个节点相对于根节点的寻址结构，形式上可以表示成：/一级属性/二级属性/.../目标属性。
 */
public class AttributePath implements Iterable<Attribute> {

    /**
     * 表示属性路径的各个节点。0个节点表示当前路径指向属性树的根。
     */
    private final List<Attribute> attributes = new ArrayList<>();

    /**
     * 属性树的类型
     */
    private final StructuralType modelType;

    /**
     * 枚举器
     */
    private Iterator<Attribute> enumerator;

    /**
     * 父级
     */
    private AttributePath parent;

    /**
     * 创建指定类型的AttributePath实例
     *
     * @param modelType 类型
     */
    public AttributePath(StructuralType modelType) {
        this.modelType = modelType;
    }

    /**
     * 获取属性树的类型
     *
     * @return 属性树的类型
     */
    public StructuralType getModelType() {
        return this.modelType;
    }

    /**
     * 获取属性路径指向的属性
     *
     * @return 属性路径指向的属性
     */
    public Attribute getNext() {
        return this.enumerator.next();
    }

    /**
     * 获取属性路径指向的属性的父属性
     *
     * @return 指向的属性的父属性
     */
    public AttributePath getParent() {
        return this.parent;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Attribute> iterator() {
        if (this.enumerator == null)
            this.enumerator = this.attributes.iterator();
        return this.enumerator;
    }

    /**
     * 将属性路径表示成字符串形式。格式为：/一级属性/二级属性/.../目标属性。
     *
     * @return 字符串形式
     */
    @Override
    public String toString() {
        StringBuilder pathBuilder = new StringBuilder("/");

        for (int i = 0; i < this.attributes.size(); i++)
            pathBuilder.append(i == 0 ? this.attributes.get(i).getName() : "/" + this.attributes.get(i).getName());

        return pathBuilder.toString();
    }

    /**
     * 沿属性树向下，将属性路径向下延伸到指定节点（属性）
     *
     * @param attribute 目标属性
     * @return 下一个属性
     */
    public AttributePath goDown(Attribute attribute) {
        AttributePath parentPath = new AttributePath(this.modelType);
        parentPath.attributes.addAll(this.attributes);
        this.parent = parentPath;

        this.attributes.add(attribute);
        return this;
    }
}
