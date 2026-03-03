/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示对投影结果实施合并的多重投影运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 11:51:22
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

/**
 * 表示对投影结果实施合并的多重投影运算
 */
public class CombiningSelectOp extends SelectOp {

    /**
     * 最终结果类型，即合并后的序列元素的类型
     */
    private final Class<?> resultType;

    /**
     * 根据指定的投影函数创建CombiningSelectOp实例
     *
     * @param resultSelector 应用于每个元素的投影函数
     * @param resultType     最终结果类型，即合并后的序列元素的类型
     */
    CombiningSelectOp(LambdaExpression resultSelector, Class<?> resultType, ObjectDataModel model) {
        super(resultSelector, model);
        this.resultType = resultType;
    }

    /**
     * 获取一个值，该值指示投影运算是否为多重投影。
     * 多重投影是指投影到一个具有多重性的引用元素或其下级元素（下级元素不要求多重性）的运算。
     * 下级元素是指关联树中代表当前元素的节点的后代所代表的元素，或者是当前节点或其后代所含属性树节点所代表的属性。
     *
     * @return 指示投影运算是否为多重投影
     */
    @Override
    public boolean getIsMultiple() {
        return true;
    }

    /**
     * 获取一个值，该值指示投影运算是否为实例化投影。
     *
     * @return 指示投影运算是否为实例化投影
     */
    @Override
    public boolean getIsNew() {
        return false;
    }

    /**
     * 获取投影结果类型
     *
     * @return 投影结果类型
     */
    @Override
    public Class<?> getResultType() {
        return this.resultType;
    }
}
