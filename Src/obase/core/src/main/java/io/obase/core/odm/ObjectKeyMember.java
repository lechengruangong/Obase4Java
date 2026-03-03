/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象标识的成员.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-2 16:08:31
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.io.Serializable;
import java.util.Objects;

/**
 * 对象标识的成员
 */
public class ObjectKeyMember implements Serializable {


    /**
     * 属性名
     */
    private final String attribute;

    /**
     * 属性值
     */
    private final Object value;

    /**
     * 创建对象标识成员实例
     *
     * @param attribute 属性名
     * @param value     属性值
     */
    public ObjectKeyMember(String attribute, Object value) {
        this.attribute = attribute;
        this.value = value;
    }

    /**
     * 根据按既定规则编码的字符串生成ObjectKeyMember实例
     *
     * @param memberString 对象键成员字符串
     * @return 对象键成员
     */
    public static ObjectKeyMember FromString(String memberString) {
        //切分字符串
        String[] splits = memberString.split(":");
        return splits.length == 2 ? new ObjectKeyMember(splits[0], splits[1]) : null;
    }

    /**
     * 获取属性名
     *
     * @return 获取属性名
     */
    public String getAttribute() {
        return this.attribute;
    }

    /**
     * 获取属性值
     *
     * @return 获取属性值
     */
    public Object getValue() {
        return this.value;
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
        ObjectKeyMember that = (ObjectKeyMember) o;
        return this.attribute.equals(that.attribute) && this.value.equals(that.value);
    }

    /**
     * 重写哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.attribute, this.value);
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return this.getAttribute() + ":" + this.getValue();
    }
}
