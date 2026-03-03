/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：属性树节点表达式生成器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-8 17:25:41
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.expression.Expression;
import io.obase.core.expression.LambdaExpression;

import java.util.Objects;

/**
 * 用于生成指向属性节点的表达式
 */
public class AttributeExpressionGenerator implements IAttributeTreeUpwardVisitorWithResult<LambdaExpression> {

    /**
     * 宿主Lambda表达式
     */
    private final LambdaExpression hostExp;

    /**
     * 结果表达式
     */
    private Expression resultExp;

    /**
     * 创建AttributeExpressionGenerator实例
     *
     * @param hostExp 一个Lambda表达式，其主体（Body）的Type为定义属性树根节点代表的属性的类型
     */
    public AttributeExpressionGenerator(LambdaExpression hostExp) {
        this.hostExp = hostExp;
    }

    /**
     * 前置访问，即在访问父级前执行操作
     *
     * @param subTree          被访问的子树
     * @param childState       访问子级时产生的状态数据
     * @param outChildState    返回一个状态数据，在遍历到父级时该数据将被视为子级状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     * @return 是否继续访问
     */
    @Override
    public boolean preVisit(AttributeTree subTree, Object childState, ObjectReferencePack<Object> outChildState, ObjectReferencePack<Object> outPreVisitState) {
        outChildState.realValue = outPreVisitState.realValue = null;
        return false;
    }

    /**
     * 后置访问，即在访问父级后执行操作
     *
     * @param subTree       被访问的子树
     * @param childState    访问子级时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     */
    @Override
    public void postVisit(AttributeTree subTree, Object childState, Object preVisitState) {
        //顶级节点 为_resultExp赋值
        if (subTree.getParent() == null) {
            this.resultExp = this.hostExp.getBody();
        } else {
            //取resultExp的Type
            Class<?> hostType = this.resultExp.getType();
            ObaseIntrospector.getObaseBeanProperties(hostType).stream().filter(p -> Objects.equals(p.getName(), subTree.getAttributeName())).findFirst()
                    .ifPresent(property -> this.resultExp = Expression.member(this.resultExp, property.getGetterMethod(), this.resultExp, hostType));
        }
    }

    /**
     * 重置访问者
     */
    @Override
    public void reset() {
        this.resultExp = null;
    }

    /**
     * 获取遍历属性树的结果
     *
     * @return 获取遍历属性树的结果
     */
    @Override
    public LambdaExpression getResult() {
        return Expression.lambda(this.hostExp.getParameters(), this.resultExp);
    }
}
