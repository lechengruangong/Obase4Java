/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Where运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 17:25:41
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.*;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.ReferringType;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 表示Where运算
 */
public class WhereOp extends FilterOp {

    /**
     * 分解出来的或因子
     */
    private OrFactor[] orFactors;

    public WhereOp(LambdaExpression predicate, ObjectDataModel model) {
        super(EQueryOpName.Where, predicate, false, model);
    }


    /**
     * 执行或因子分解
     *
     * @param model 对象数据模型
     * @return 或因子
     */
    public OrFactor[] Decompose(ObjectDataModel model) {
        //没有断言函数 不分解
        if (this.getPredicate() == null)
            return null;

        if (this.orFactors == null) {
            ReferringType referringType = model.getReferringType(this.getSourceType());
            CriteriaFlattener flattener = new CriteriaFlattener(referringType, this.getPredicate().getParameters()[0]);
            //直接访问内容 参数绑定已传入
            this.orFactors = flattener.flatt(this.getPredicate().getBody());
        }

        return this.orFactors;
    }


    /**
     * 结果类型
     *
     * @return 结果类型
     */
    @Override
    public Class<?> getResultType() {
        return this.getSourceType();
    }

    /**
     * 作为一个表达式访问者对表达式表示的筛选条件实施平展
     */
    private static class CriteriaFlattener extends ExpressionVisitor {

        /**
         * 主引类型
         */
        private final ReferringType referringType;

        /**
         * 参数表达式
         */
        private final ParameterExpression sourceParameter;

        /**
         * 存放当前层级分解出来的或因子
         */
        private final Stack<OrFactor> tempFactors = new Stack<>();

        /**
         * 构造一个条件平展器 将判断函数条件进行平展
         *
         * @param referringType   主引类型
         * @param sourceParameter 参数表达式
         */
        public CriteriaFlattener(ReferringType referringType, ParameterExpression sourceParameter) {
            this.referringType = referringType;
            this.sourceParameter = sourceParameter;
        }

        /**
         * 平展方法
         *
         * @param expression 要平展的表达式
         * @return 平展后的或因子
         */
        public OrFactor[] flatt(Expression expression) {
            expression.accept(this);
            return this.tempFactors.toArray(new OrFactor[0]);
        }

        /**
         * 是否是逻辑运算
         *
         * @param type 运算类型
         * @return 是否是逻辑运算
         */
        private boolean getIsLogicOp(EExpressionType type) {
            return type == EExpressionType.Not || type == EExpressionType.AndAlso || type == EExpressionType.OrElse;
        }

        /**
         * 是否是关系运算
         *
         * @param type 运算类型
         * @return 是否是关系运算
         */
        private boolean getIsRelationOp(EExpressionType type) {
            return type == EExpressionType.Equal || type == EExpressionType.NotEqual ||
                    type == EExpressionType.GreaterThan
                    || type == EExpressionType.GreaterThanOrEqual || type == EExpressionType.LessThan
                    || type == EExpressionType.LessThanOrEqual;
        }

        /**
         * 访问表达式树方法
         *
         * @param node 表达式树节点
         * @return 访问结果
         */
        @Override
        public Expression visit(Expression node) {
            EExpressionType nodeType = node.getExpressionType();

            if (this.getIsLogicOp(nodeType) && node.getType() == boolean.class)
                return nodeType == EExpressionType.Not
                        ? this.visitUnary((UnaryExpression) node)
                        : this.visitBinary((BinaryExpression) node);

            //不是一元或二元表达式 直接放入临时栈
            this.tempFactors.push(new OrFactor(new Expression[]{node}, this.referringType, this.sourceParameter));
            return node;
        }

        /**
         * 默认的访问二元表达式
         * 先访问左端 然后访问右端 最后返回右端的访问结果
         *
         * @param binaryExpression 二元表达式
         * @return 二元表达式的右端
         */
        @Override
        protected Expression visitBinary(BinaryExpression binaryExpression) {

            this.visit(binaryExpression.getLeft());
            this.visit(binaryExpression.getLeft());

            //是与 组合
            if (binaryExpression.getExpressionType() == EExpressionType.AndAlso) {
                OrFactor left = this.tempFactors.pop();
                OrFactor right = this.tempFactors.pop();
                OrFactor result = left.and(right);
                this.tempFactors.push(result);
            }

            //是或 无需处理
            if (binaryExpression.getExpressionType() == EExpressionType.OrElse) {
                //ignore
            }
            return binaryExpression;
        }

        /**
         * 默认访问一元表达式
         * 先访问Operand 先后返回自身
         *
         * @param unaryExpression 一元表达式
         * @return 一元表达式自身
         */
        @Override
        protected Expression visitUnary(UnaryExpression unaryExpression) {

            //处理非运算 变为挨个表达式取反 翻转与或
            if (unaryExpression.getExpressionType() == EExpressionType.Not) {
                int current = this.tempFactors.size();
                this.visit(unaryExpression.getOperand());
                current = this.tempFactors.size() - current;

                //存放需要改为与的表达式
                List<OrFactor> tempList = new ArrayList<>();
                for (int i = 0; i < current; i++) {
                    OrFactor needRevers = this.tempFactors.pop();
                    for (Expression exp : needRevers.getItems()) {
                        tempList.add(new OrFactor(new Expression[]{Expression.not(exp)}, this.referringType, this.sourceParameter));
                    }
                }
                //求最终与
                OrFactor result = tempList.stream().findFirst().orElse(null);
                for (int i = 1; i < tempList.size(); i++) {
                    result.and(tempList.get(i));
                }
                this.tempFactors.push(result);
            } else {
                this.tempFactors.push(new OrFactor(new Expression[]{unaryExpression}, this.referringType, this.sourceParameter));
            }

            return unaryExpression;
        }
    }
}

