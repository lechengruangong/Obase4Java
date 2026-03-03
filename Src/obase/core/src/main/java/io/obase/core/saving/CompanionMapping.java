/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示参与伴随映射的关联对象及其状态.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 17:00:50
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

/**
 * 表示参与伴随映射的关联对象及其状态
 */
public class CompanionMapping {

    /**
     * 伴随关联对象
     */
    private final Object associationObj;

    /**
     * 伴随关联对象的状态
     */
    private final EObjectStatus status;

    /**
     * 创建CompanionMapping实例
     *
     * @param associationObj 伴随关联对象
     * @param status         伴随关联对象的状态
     */
    public CompanionMapping(Object associationObj, EObjectStatus status) {

        this.associationObj = associationObj;
        this.status = status;
    }

    /**
     * 获取伴随关联对象
     *
     * @return 获取伴随关联对象
     */
    public Object getAssociationObj() {
        return this.associationObj;
    }

    /**
     * 获取伴随关联对象的状态
     *
     * @return 获取伴随关联对象的状态
     */
    public EObjectStatus getStatus() {
        return this.status;
    }
}
