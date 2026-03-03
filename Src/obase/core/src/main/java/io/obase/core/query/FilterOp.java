/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：基于断言函数对元素进行筛选的运算基类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 17:24:43
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;


import io.obase.core.expression.LambdaExpression;
import io.obase.core.odm.ObjectDataModel;

/**
 * 为基于断言函数对元素进行筛选的运算提供基础实现
 */
public abstract class FilterOp extends QueryOp {

    /**
     * 指示当未选中任何元素时是否返回默认值
     */
    private final boolean returnDefault;
    /**
     * 断言函数，用于测试每个元素是否满足条件。不指定表示恒为真，即任何元素都满足条件。
     */
    private LambdaExpression predicate;

    /**
     * 创建FilterOp实例
     *
     * @param name          运算名称
     * @param predicate     对元素进行筛选的断言函数
     * @param returnDefault 指示未选中任何元素时是否返回默认值
     */
    protected FilterOp(EQueryOpName name, LambdaExpression predicate, boolean returnDefault, ObjectDataModel model) {
        this(name, QueryOp.getParameterHostType(predicate), returnDefault);

        this.predicate = predicate;
        this.model = model;
    }

    /**
     * 创建FilterOp实例
     *
     * @param name          运算名称
     * @param sourceType    查询源类型
     * @param returnDefault 指示未选中任何元素时是否返回默认值
     */
    protected FilterOp(EQueryOpName name, Class<?> sourceType, boolean returnDefault) {
        super(name, sourceType);

        this.returnDefault = returnDefault;
        this.predicate = null;
    }

    /**
     * 获取断言函数，该函数用于测试每个元素是否满足条件。不指定表示恒为真，即任何元素都满足条件。
     *
     * @return 断言函数
     */
    public LambdaExpression getPredicate() {
        return this.predicate;
    }

    /**
     * 获取一个值，该值指示未选中任何元素时是否返回默认值
     *
     * @return 未选中任何元素时是否返回默认值
     */
    public boolean getReturnDefault() {
        return this.returnDefault;
    }

    /**
     * 获取一个值该值指示断言函数是否将元素在序列中的索引作为（第二个）参数
     *
     * @return 断言函数是否将元素在序列中的索引作为（第二个）参数
     */
    public boolean getIndexReferred() {
        //没有断言函数 返回false
        if (this.predicate == null)
            return false;
        //参数不足两个 返回false
        if (this.predicate.getParameters().length < 2)
            return false;
        //判断第二个参数是不是整数
        return this.predicate.getParameters()[1].getType() == int.class || this.predicate.getParameters()[1].getType() == short.class ||
                this.predicate.getParameters()[1].getType() == long.class;
    }
}
