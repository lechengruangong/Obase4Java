/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：标识的数组.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 17:02:53
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 可以作为标识的数组
 * 标识是一个值序列，该序列可以在同类事物中唯一标识一个事物。该序列中的项称为标识成员。
 */
public class IdentityArray extends ArrayList<Object> {

    /**
     * 哈希生成器
     */
    private final IArrayHashGenerator hashGenerator;

    /**
     * 用指定的标识成员创建IdentityArray实例。
     *
     * @param identity 标识成员序列
     */
    public IdentityArray(Object... identity) {
        super(Arrays.asList(identity));
        this.hashGenerator = new DefaultArrayHashGenerator();
    }

    /**
     * 用指定的标识成员创建IdentityArray实例，并指定用于为标识生成哈希代码的方法。
     *
     * @param hashGenerator 用于为标识数组生成哈希代码的方法
     * @param identity      标识成员序列
     */
    public IdentityArray(IArrayHashGenerator hashGenerator, Object... identity) {
        super(Arrays.asList(identity));
        this.hashGenerator = hashGenerator;
    }

    /**
     * 向标识数组追加子标识。
     * 在主标识不能唯一标识事物的情况下可以使用子标识进一步标识。
     *
     * @param subIdentity 子标识的成员序列
     */
    public void append(Object... subIdentity) {
        this.addAll(Arrays.asList(subIdentity));
    }

    /**
     * 重写相等比较方法
     *
     * @param o 另外一个对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        IdentityArray objects = (IdentityArray) o;


        //比较序列
        Object[] selfArray = this.toArray();
        Object[] otherArray = objects.toArray();

        if (!Arrays.equals(selfArray, otherArray))
            return false;
        //比较生成器
        //因为生成器没有属性访问器 故比较生成的哈希码
        return o.hashCode() == this.hashCode();
    }

    /**
     * 重写返回哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return this.hashGenerator.generator(this.toArray());
    }
}
