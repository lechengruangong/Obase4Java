/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：向上遍历属性树过程中对子树实施访问的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-8 17:27:40
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

/**
 * 定义在向上遍历属性树过程中对子树实施访问的规范，该遍历操作会返回一个结果
 */
public interface IAttributeTreeUpwardVisitorWithResult<TResult> extends IAttributeTreeUpwardVisitor {

    /**
     * 获取遍历属性树的结果
     *
     * @return 获取遍历属性树的结果
     */
    TResult getResult();
}
