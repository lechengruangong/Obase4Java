/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示算术运算的表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 16:54:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示算术运算的表达式
 */
public class ArithmeticExpression extends BinaryExpression {

    /**
     * 创建ArithmeticExpression的实例，并设置Left属性和Right属性的值
     *
     * @param left  左操作数
     * @param right 右操作数
     */
    public ArithmeticExpression(Expression left, Expression right) {
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
        switch (this.getNodeType()) {
            case Add:
                return this.getLeft().toString(sourceType) + "+(" + this.getRight().toString(sourceType) + ")";
            case Subtract:
                return this.getLeft().toString(sourceType) + "-(" + this.getRight().toString(sourceType) + ")";
            case Multiply:
                return this.getLeft().toString(sourceType) + "*(" + this.getRight().toString(sourceType) + ")";
            case Divide:
                return this.getLeft().toString(sourceType) + "/(" + this.getRight().toString(sourceType) + ")";
            case Power:
                return this.getLeft().toString(sourceType) + "^(" + this.getRight().toString(sourceType) + ")";
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
        //每个部分的参数集合
        List<ObjectReferencePack<List<DataParameter>>> resultParameter = this.genBinaryDataParameter();
        ObjectReferencePack<List<DataParameter>> leftSqlParameter = resultParameter.get(0);
        ObjectReferencePack<List<DataParameter>> rightSqlParameter = resultParameter.get(1);

        //字符串
        String resultStr;
        switch (this.getNodeType()) {
            case Add:
                resultStr = this.getLeft().toString(sourceType, leftSqlParameter, creator) + "+(" + this.getRight().toString(sourceType, rightSqlParameter, creator) + ")";
                break;
            case Subtract:
                resultStr = this.getLeft().toString(sourceType, leftSqlParameter, creator) + "-(" + this.getRight().toString(sourceType, rightSqlParameter, creator) + ")";
                break;
            case Multiply:
                resultStr = this.getLeft().toString(sourceType, leftSqlParameter, creator) + "*(" + this.getRight().toString(sourceType, rightSqlParameter, creator) + ")";
                break;
            case Divide:
                resultStr = this.getLeft().toString(sourceType, leftSqlParameter, creator) + "/(" + this.getRight().toString(sourceType, rightSqlParameter, creator) + ")";
                break;
            case Power:
                resultStr = this.getLeft().toString(sourceType, leftSqlParameter, creator) + "^(" + this.getRight().toString(sourceType, rightSqlParameter, creator) + ")";
                break;
            default:
                throw new IllegalArgumentException("未知的表达式类型: " + this.getNodeType());
        }

        sqlParameters.realValue = new ArrayList<>();
        sqlParameters.realValue.addAll(leftSqlParameter.realValue);
        sqlParameters.realValue.addAll(rightSqlParameter.realValue);

        DataParameterSorter.sort(sqlParameters.realValue);

        return resultStr;
    }
}
