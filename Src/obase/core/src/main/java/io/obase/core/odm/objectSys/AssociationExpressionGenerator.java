/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：用于生成指向关联节点的表达式的生成器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-8 17:23:16
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.expression.Expression;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.ENavigationType;
import io.obase.core.odm.ENavigationUse;
import io.obase.core.odm.ReferenceElement;
import io.obase.core.odm.ReferringType;

import java.util.Arrays;
import java.util.Objects;

/**
 * 用于生成指向关联节点的表达式
 */
public class AssociationExpressionGenerator implements IAssociationTreeUpwardVisitorWithResult<LambdaExpression> {

    /**
     * 平展形参获取委托。
     */
    private final FunctionWithOneArg<AssociationTreeNode, ParameterExpression> flatteningParaGetter;

    /**
     * 参数
     */
    private final ParameterExpression[] parameters = new ParameterExpression[2];

    /**
     * 代表查询源的形参
     */
    private final ParameterExpression sourceParameter;

    /**
     * 元素名称
     */
    private String memberName;

    /**
     * 结果表达式
     */
    private Expression resultExp;

    /**
     * 创建AssociationExpressionGenerator实例。
     *
     * @param sourcePara           代表查询源的形参。
     * @param flatteningParaGetter 平展形参获取委托。
     */
    public AssociationExpressionGenerator(ParameterExpression sourcePara,
                                          FunctionWithOneArg<AssociationTreeNode, ParameterExpression> flatteningParaGetter) {
        this.sourceParameter = sourcePara;
        this.flatteningParaGetter = flatteningParaGetter;
        this.parameters[0] = this.sourceParameter;
    }

    /**
     * 获取遍历关联树的结果
     *
     * @return 遍历操作返回结果的类型
     */
    @Override
    public LambdaExpression getResult() {
        return this.resultExp != null
                ? Expression.lambda(Arrays.stream(this.parameters).filter(Objects::nonNull).toArray(ParameterExpression[]::new), this.resultExp)
                : null;
    }

    /**
     * 前置访问，即在访问父级前执行操作
     *
     * @param subTree          被访问的子树
     * @param childState       访问子级时产生的状态数据
     * @param outChildState    返回一个状态数据，在遍历到父级时该数据将被视为子级状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     * @return 是否继续访问父级
     */
    @Override
    public boolean preVisit(AssociationTree subTree, Object childState, ObjectReferencePack<Object> outChildState, ObjectReferencePack<Object> outPreVisitState) {
        outChildState.realValue = null;
        outPreVisitState.realValue = null;
        if (subTree.getIsRoot()) {
            Object[] value = new Object[1];
            value[0] = this.sourceParameter;
            outPreVisitState.realValue = value;
            return false;
        }

        if (this.constraint((subTree))) {
            ParameterExpression flatteningPara = this.flatteningParaGetter.invoke(subTree.getNode());

            outPreVisitState.realValue = this.parameters[1] = flatteningPara;
            return false;
        }

        return true;
    }

    /**
     * 后置访问，即在访问父级后执行操作
     *
     * @param subTree       被访问的子树
     * @param childState    访问子级时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     */
    @Override
    public void postVisit(AssociationTree subTree, Object childState, Object preVisitState) {
        if (preVisitState != null) {
            Object[] objs = (Object[]) preVisitState;
            this.resultExp = (Expression) objs[0];
        } else {
            ReferringType parentType = subTree.getParent().getRepresentedType();
            ReferenceElement representedRef = parentType.getReferenceElement(subTree.getElementName());
            //对应图中蓝色部分
            ENavigationType navType = representedRef.getNavigation().getNavigationType();
            ENavigationUse navUse = representedRef.getNavigationUse();

            if (navType == ENavigationType.Indirectly || navUse == ENavigationUse.DirectlyReference ||
                    subTree.getParent().getParent() == null)
                this.memberName = subTree.getElementName();

            if (navType == ENavigationType.Indirectly || navUse == ENavigationUse.ArrivingReference) {
                //取resultExp的Type
                Class<?> hostType = this.resultExp.getType();
                ObaseIntrospector.getObaseBeanProperties(hostType).stream().filter(p -> Objects.equals(p.getName(), this.memberName)).findFirst()
                        .ifPresent(property -> this.resultExp = Expression.member(this.resultExp, property.getGetterMethod(), this.resultExp, this.resultExp.getType()));
            }
        }
    }

    /**
     * 重置访问者
     */
    @Override
    public void reset() {
        this.resultExp = null;
        this.memberName = null;
    }

    /**
     * 测试subTree方法
     *
     * @param subTree 关联树
     * @return 是否符合条件跳出条件
     */
    private boolean constraint(AssociationTree subTree) {


        //获取父级
        ReferringType parentType = subTree.getParent().getRepresentedType();
        //获取当前节点代表的元素
        ReferenceElement representedRef = parentType.getReferenceElement(subTree.getElementName());
        //当前节点属性
        ENavigationUse navUse = representedRef.getNavigationUse();
        boolean isMultiple = representedRef.getIsMultiple();
        ENavigationType navType = representedRef.getNavigation().getNavigationType();
        //父级是否为一对多
        boolean parentIsMultiple = false;
        if (subTree.getParent() != null) {
            if (subTree.getParent().getElement() != null) {
                parentIsMultiple = subTree.getParent().getElement().getIsMultiple();
            }
        }
        //当前子树个数
        int subCount = subTree.getSubCount();

        return (navUse == ENavigationUse.EmittingReference && isMultiple ||
                navUse == ENavigationUse.ArrivingReference && navType == ENavigationType.Directly &&
                        parentIsMultiple) && subCount > 0;
    }
}
