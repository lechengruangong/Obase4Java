/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联树异构断言提供程序.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-9 11:34:30
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

/**
 * 关联树异构断言提供程序。
 * 异构断言是指判定关联树是否为异构的，只要存在一个节点，该节点与根节点某种特性上不相同，则认为该关联树是异构的，或称该关联树为异构关联树。所述“特性”称为“关注特性”。
 * 断言算法的基本思想是，先寄存根节点的关注特性，然后遍历其它节点，如果找到一个节点在关注特性上与根节点不同，则判定关联树为异构的。
 */
public abstract class HeterogeneityPredicationProvider implements Comparable<HeterogeneityPredicationProvider> {

    /**
     * 比较当前节点与根节点在关注特性上的异同
     *
     * @param currentNode 当前节点
     * @return 相等返回true，否则返回false。
     */
    public abstract boolean compare(AssociationTreeNode currentNode);

    /**
     * 寄存根节点的关注特性
     *
     * @param rootNode 根节点
     */
    public abstract void registerRoot(AssociationTreeNode rootNode);

    /**
     * 重写比较方法
     *
     * @param o the object to be compared.
     * @return 是否相等
     */
    @Override
    public abstract int compareTo(HeterogeneityPredicationProvider o);

    /**
     * 重写Object的Equal
     *
     * @param o 另一个对象
     * @return 是否相等
     */
    @Override
    public abstract boolean equals(Object o);

    /**
     * 重写GetHashCode
     *
     * @return 哈希码
     */
    @Override
    public abstract int hashCode();

}
