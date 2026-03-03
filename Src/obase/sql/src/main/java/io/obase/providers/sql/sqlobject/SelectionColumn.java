/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示投影集中的一个列.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 16:16:44
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.providers.sql.EDataSource;

/**
 * 表示投影集中的一个列
 */
public abstract class SelectionColumn {

    /**
     * 获取哈希码
     *
     * @return 哈希码
     */
    @Override
    public abstract int hashCode();

    /**
     * 确定指定的投影列是否与当前投影列相等。注：两个投影列相等的充要条件是表达式和别名均相等。
     *
     * @param other 要与当前投影列进行比较的投影列
     * @return 是否相等
     */
    public abstract boolean equals(SelectionColumn other);

    /**
     * 确定指定的对象与当前投影列是否相等。（重写Object.Equals）
     *
     * @param o 要与当前投影列进行比较的对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof SelectionColumn) {
            return this.equals((SelectionColumn) o);
        }
        return false;
    }

    /**
     * 转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public abstract String toString();

    /**
     * 转换为字符串表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    public abstract String toString(EDataSource sourceType);

    /**
     * 为投影列涉及到的源的别名设置前缀
     *
     * @param prefix 别名前缀
     */
    public abstract void setSourceAliasPrefix(String prefix);
}
