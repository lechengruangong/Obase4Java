/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联端映射,存储关联端的实体主键和映射字段.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-2 16:22:52
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.util.Objects;

public class AssociationEndMapping implements IOrderBy {

    /**
     * 关联端的标识属性
     */
    private String keyAttribute;

    /**
     * 关联表的映射字段
     */
    private String targetField;

    /**
     * 获取关联端的标识属性
     *
     * @return 关联端的标识属性
     */
    public String getKeyAttribute() {
        return this.keyAttribute;
    }

    /**
     * 设置关联端的标识属性
     *
     * @param keyAttribute 关联端的标识属性
     */
    public void setKeyAttribute(String keyAttribute) {
        this.keyAttribute = keyAttribute;
    }

    /**
     * 获取关联表的映射字段
     *
     * @return 关联表的映射字段
     */
    @Override
    public String getTargetField() {
        return this.targetField;
    }

    /**
     * 设置关联表的映射字段
     *
     * @param targetField 关联表的映射字段
     */
    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    /**
     * 重写相等方法
     *
     * @param o 另一个对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        AssociationEndMapping that = (AssociationEndMapping) o;
        return this.keyAttribute.equals(that.keyAttribute) && this.targetField.equals(that.targetField);
    }

    /**
     * 重写获取哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.keyAttribute, this.targetField);
    }
}
