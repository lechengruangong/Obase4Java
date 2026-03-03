/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示一元运算的表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 11:16:44
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示一元运算的表达式，具体可以表示Increment（递减）、Decrement（递增）、Negate（算术取反）、UnaryPlus（一元加法）、Not（逻辑求反）、BitNot（按位取反）六种运算。
 */
public class UnaryExpression extends Expression {

    /**
     * 操作数
     */
    private final Expression operand;

    /**
     * 创建UnaryExpression的实例，并设置Operand属性的值。
     *
     * @param operand 操作数
     */
    public UnaryExpression(Expression operand) {
        this.operand = operand;
    }

    /**
     * 获取操作数
     *
     * @return 操作数
     */
    public Expression getOperand() {
        return this.operand;
    }

    /**
     * 派生类实现此方法以判定具体类型的表达式对象是否相等
     *
     * @param other 要与当前表达式进行比较的表达式
     * @return 是否相等
     */
    @Override
    protected boolean concreteEquals(Expression other) {
        if (other instanceof UnaryExpression) {
            UnaryExpression unaryExpression = (UnaryExpression) other;
            return unaryExpression.getOperand() == this.getOperand();
        }
        return false;
    }

    /**
     * 针对指定的数据源类型，返回表达式的文本表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {
        switch (this.getNodeType()) {
            case Increment:
                return "(" + this.getOperand().toString(sourceType) + "+1)";
            case Decrement:
                return "(" + this.getOperand().toString(sourceType) + "-1)";
            case Negate:
                return "(-" + this.getOperand().toString(sourceType) + ")";
            case UnaryPlus:
                return "(+" + this.getOperand().toString(sourceType) + ")";
            case Not:
                return "(!" + this.getOperand().toString(sourceType) + ")";
            case BitNot:
                return "~(" + this.getOperand().toString(sourceType) + ")";
            default:
                throw new IllegalArgumentException("未知的表达式类型: " + this.getNodeType());
        }
    }

    /**
     * 使用参数化的方式 和 指定的数据源 将表达式表示为字符串形式
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        switch (this.getNodeType()) {
            case Increment:
                DataParameterSorter.sort(sqlParameters.realValue);
                return "(" + this.getOperand().toString(sourceType, sqlParameters, creator) + "+1)";
            case Decrement:
                DataParameterSorter.sort(sqlParameters.realValue);
                return "(" + this.getOperand().toString(sourceType, sqlParameters, creator) + "-1)";
            case Negate:
                DataParameterSorter.sort(sqlParameters.realValue);
                return "(-" + this.getOperand().toString(sourceType, sqlParameters, creator) + ")";
            case UnaryPlus:
                DataParameterSorter.sort(sqlParameters.realValue);
                return "(+" + this.getOperand().toString(sourceType, sqlParameters, creator) + ")";
            case Not:
                DataParameterSorter.sort(sqlParameters.realValue);
                if (sourceType == EDataSource.SqlServer || sourceType == EDataSource.Sqlite || sourceType == EDataSource.PostgreSql) {

                    if (this.getOperand() instanceof FieldExpression) {
                        Expression exp = Expression.equal(this.getOperand(), new ConstantExpression(true));
                        return " not " + exp.toString(sourceType, sqlParameters, creator);
                    }

                    if (this.getOperand() instanceof ConstantExpression) {
                        ConstantExpression constantExpression = (ConstantExpression) this.getOperand();
                        if (constantExpression.getValue() instanceof Boolean) {
                            sqlParameters.realValue = new ArrayList<>();
                            return "1<>1";
                        }
                    }
                }
                return "(!" + this.getOperand().toString(sourceType, sqlParameters, creator) + ")";
            case BitNot:
                DataParameterSorter.sort(sqlParameters.realValue);
                return "~(" + this.getOperand().toString(sourceType, sqlParameters, creator) + ")";
            default:
                throw new IllegalArgumentException("未知的表达式类型: " + this.getNodeType());
        }
    }
}
