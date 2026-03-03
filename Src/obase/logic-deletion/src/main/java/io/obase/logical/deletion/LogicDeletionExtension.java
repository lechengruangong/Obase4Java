/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：逻辑删除扩展.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 10:54:55
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.logical.deletion;

import io.obase.core.odm.TypeExtension;

/**
 * 逻辑删除扩展
 */
public class LogicDeletionExtension extends TypeExtension {

    /**
     * 逻辑删除标记的属性的名称
     */
    private String deletionMark;

    /**
     * 删除标记的映射字段
     */
    private String deletionField;

    /**
     * 获取逻辑删除标记的属性的名称
     *
     * @return 逻辑删除标记的属性的名称
     */
    public String getDeletionMark() {
        return this.deletionMark;
    }

    /**
     * 设置逻辑删除标记的属性的名称
     *
     * @param deletionMark 逻辑删除标记的属性的名称
     */
    public void setDeletionMark(String deletionMark) {
        this.deletionMark = deletionMark;
    }

    /**
     * 获取删除标记的映射字段
     *
     * @return 删除标记的映射字段
     */
    public String getDeletionField() {
        return this.deletionField;
    }

    /**
     * 设置删除标记的映射字段
     *
     * @param deletionField 删除标记的映射字段
     */
    public void setDeletionField(String deletionField) {
        this.deletionField = deletionField;
    }
}
