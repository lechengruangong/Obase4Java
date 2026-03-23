/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示二元逻辑运算的表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 15:21:17
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示二元逻辑运算的表达式
 */
public class BinaryLogicExpression extends BinaryExpression {

    /**
     * 创建BinaryLogicExpression的实例，并设置Left属性和Right属性的值
     *
     * @param left  左操作数
     * @param right 右操作数
     */
    public BinaryLogicExpression(Expression left, Expression right) {
        super(left, right);
    }

    /**
     * 针对指定的数据源类型，返回表达式的文本表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {

        //由于SQL Server不支持布尔类型字段作为条件，故需要将布尔类型字段转换为位类型字段进行处理
        if (sourceType == EDataSource.SqlServer) {
            this.replaceBoolField();
        }

        //操作数
        String operatorStr;

        switch (this.getNodeType()) {
            case OrElse:
                operatorStr = " OR ";
                break;
            case AndAlso:
                operatorStr = " AND ";
                break;
            default:
                throw new IllegalArgumentException("未知的表达式类型: " + this.getNodeType());
        }
        return "(" + this.getLeft().toString(sourceType) + ")" + operatorStr + "(" + this.getRight().toString(sourceType) + ")";
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

        //由于SQL Server不支持布尔类型字段作为条件，故需要将布尔类型字段转换为位类型字段进行处理
        if (sourceType == EDataSource.SqlServer) {
            this.replaceBoolField();
        }

        //每个部分的参数集合
        List<ObjectReferencePack<List<DataParameter>>> resultParameter = this.genBinaryDataParameter();

        //操作数
        String operatorStr;
        switch (this.getNodeType()) {
            case AndAlso:
                operatorStr = " AND ";
                break;
            case OrElse:
                operatorStr = " OR ";
                break;
            default:
                throw new IllegalArgumentException("未知的表达式类型: " + this.getNodeType());
        }

        ObjectReferencePack<List<DataParameter>> leftSqlParameter = resultParameter.get(0);
        ObjectReferencePack<List<DataParameter>> rightSqlParameter = resultParameter.get(1);

        //字符串
        String resultStr = this.getResultString(sourceType, creator, operatorStr, leftSqlParameter, rightSqlParameter);

        List<DataParameter> realSqlParameters = new ArrayList<>();
        realSqlParameters.addAll(leftSqlParameter.realValue);
        realSqlParameters.addAll(rightSqlParameter.realValue);

        //最终的集合
        sqlParameters.realValue = realSqlParameters;

        DataParameterSorter.sort(sqlParameters.realValue);

        return resultStr;
    }

    /**
     * 获取结果字符串
     *
     * @param sourceType         数据源类型
     * @param creator            参数构造器
     * @param operatorStr        操作符
     * @param leftSqlParameter   左操作数的参数集合
     * @param rightSqlParameters 右操作数的参数集合
     * @return 结果字符串
     */
    private String getResultString(EDataSource sourceType, IParameterCreator creator, String operatorStr, ObjectReferencePack<List<DataParameter>> leftSqlParameter, ObjectReferencePack<List<DataParameter>> rightSqlParameters) {
        String resultStr;
        if (this.getLeft() instanceof ConstantExpression && ((ConstantExpression) this.getLeft()).getValue() instanceof Boolean) {
            leftSqlParameter.realValue = new ArrayList<>();
            resultStr = "(1=1)" + operatorStr + "(" + this.getRight().toString(sourceType, rightSqlParameters, creator) + ")";
            if (this.getRight() instanceof FieldExpression) {
                FieldExpression fieldExpression = (FieldExpression) this.getRight();
                Expression exp = Expression.equal(fieldExpression, new ConstantExpression(true));
                resultStr = "(1=1)" + operatorStr + "(" + exp.toString(sourceType, rightSqlParameters, creator) + ")";
            }
        } else if (this.getRight() instanceof ConstantExpression && ((ConstantExpression) this.getRight()).getValue() instanceof Boolean) {
            rightSqlParameters.realValue = new ArrayList<>();
            resultStr = "(" + this.getLeft().toString(sourceType, leftSqlParameter, creator) + ")" + operatorStr + "(1=1)";
            if (this.getLeft() instanceof FieldExpression) {
                FieldExpression fieldExpression = (FieldExpression) this.getLeft();
                Expression exp = Expression.equal(fieldExpression, new ConstantExpression(true));
                resultStr = "(" + exp.toString(sourceType, leftSqlParameter, creator) + ")" + operatorStr + "(1=1)";
            }
        } else {
            resultStr = "(" + this.getLeft().toString(sourceType, leftSqlParameter, creator) + ")" + operatorStr + "(" + this.getRight().toString(sourceType, rightSqlParameters, creator) + ")";
            if (this.getLeft() instanceof FieldExpression) {
                FieldExpression fieldExpression = (FieldExpression) this.getLeft();
                Expression exp = Expression.equal(fieldExpression, new ConstantExpression(true));
                resultStr = "(" + exp.toString(sourceType, leftSqlParameter, creator) + ")" + operatorStr + "(" + this.getRight().toString(sourceType, rightSqlParameters, creator) + ")";
            }
            if (this.getRight() instanceof FieldExpression) {
                FieldExpression fieldExpression = (FieldExpression) this.getRight();
                Expression exp = Expression.equal(fieldExpression, new ConstantExpression(true));
                resultStr = "(" + this.getLeft().toString(sourceType, leftSqlParameter, creator) + ")" + operatorStr + "(" + exp.toString(sourceType, rightSqlParameters, creator) + ")";
            }
        }
        return resultStr;
    }
}
