/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示比较运算的表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 17:10:40
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示比较运算的表达式
 */
public class ComparisonExpression extends BinaryExpression {

    /**
     * 创建ComparisonExpression的实例，并设置Left属性和Right属性的值
     *
     * @param left  左操作数
     * @param right 右操作数
     */
    public ComparisonExpression(Expression left, Expression right) {
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
        boolean isNull = false;
        String valueStr = "";
        if (this.getRight() instanceof ConstantExpression) {
            ConstantExpression constant = (ConstantExpression) this.getRight();
            if (constant.getValue() == null) {
                isNull = true;
            } else {
                //MySql中对于True和False不含单引号
                //SqlServer中True和False必须包含单引号
                //故分开处理
                if (constant.getValue() instanceof Boolean && (sourceType == EDataSource.MySql ||
                        sourceType == EDataSource.Oracle || sourceType == EDataSource.PostgreSql ||
                        sourceType == EDataSource.Sqlite))
                    valueStr = constant.toString(sourceType);
                else
                    valueStr = "'" + constant.toString(sourceType) + "'";
            }
        } else {
            FieldExpression fieId = (FieldExpression) this.getRight();
            valueStr = "(" + fieId.toString(sourceType) + ")";
        }

        switch (this.getNodeType()) {
            case Equal:
                if (isNull)
                    return this.getLeft().toString(sourceType) + " IS NULL";
                else
                    return this.getLeft().toString(sourceType) + " = " + valueStr;
            case NotEqual:
                if (isNull)
                    return this.getLeft().toString(sourceType) + " IS NOT NULL";
                else
                    return this.getLeft().toString(sourceType) + " <> " + valueStr;
            case LessThan:
                return this.getLeft().toString(sourceType) + " < " + valueStr;
            case LessThanOrEqual:
                return this.getLeft().toString(sourceType) + " <= " + valueStr;
            case GreaterThan:
                return this.getLeft().toString(sourceType) + " > " + valueStr;
            case GreaterThanOrEqual:
                return this.getLeft().toString(sourceType) + " >= " + valueStr;

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
        //参数列表
        sqlParameters.realValue = new ArrayList<>();

        boolean isLeftNull = false;
        boolean isRightNull = false;
        //比较表达式可能为空 为空时特殊翻译
        if (this.getLeft() instanceof ConstantExpression) {
            ConstantExpression leftConstantExpression = (ConstantExpression) this.getLeft();
            if (leftConstantExpression.getValue() == null)
                isLeftNull = true;
        }
        if (this.getRight() instanceof ConstantExpression) {
            ConstantExpression rightConstantExpression = (ConstantExpression) this.getRight();
            if (rightConstantExpression.getValue() == null)
                isRightNull = true;
        }

        //结果 左侧表达式参数
        String result;
        //每个部分的参数集合
        List<ObjectReferencePack<List<DataParameter>>> resultParameter = this.genBinaryDataParameter();
        ObjectReferencePack<List<DataParameter>> leftSqlParameter = resultParameter.get(0);
        ObjectReferencePack<List<DataParameter>> rightSqlParameter = resultParameter.get(1);

        switch (this.getNodeType()) {
            case Equal: {
                if (isLeftNull || isRightNull) {
                    ObjectReferencePack<List<DataParameter>> sqlParameter = new ObjectReferencePack<>();
                    if (isLeftNull) {
                        result = this.getRight().toString(sourceType, sqlParameter, creator) + " IS NULL";
                        sqlParameters.realValue.addAll(sqlParameter.realValue);
                        break;
                    }

                    result = this.getLeft().toString(sourceType, sqlParameter, creator) + " IS NULL";
                    sqlParameters.realValue.addAll(sqlParameter.realValue);
                } else {

                    if (sourceType == EDataSource.SqlServer) {
                        if (this.getRight() instanceof ConstantExpression) {
                            ConstantExpression rightConstant = (ConstantExpression) this.getRight();
                            if (rightConstant.getValue() instanceof Boolean) {
                                Boolean rightBoolValue = (Boolean) rightConstant.getValue();

                                if (this.getLeft() instanceof LikeExpression) {

                                    result = rightBoolValue ? "" : " NOT " + this.getLeft().toString(sourceType, leftSqlParameter, creator);
                                    sqlParameters.realValue.addAll(leftSqlParameter.realValue);
                                    break;
                                }

                                if (this.getLeft() instanceof UnaryExpression) {
                                    UnaryExpression unaryExpression = (UnaryExpression) this.getLeft();
                                    if (unaryExpression.getNodeType() == EExpressionType.Not) {
                                        result = "Not " + unaryExpression.getOperand().toString(sourceType, leftSqlParameter, creator) + " = " + this.getRight().toString(sourceType, rightSqlParameter, creator);
                                        sqlParameters.realValue.addAll(leftSqlParameter.realValue);
                                        sqlParameters.realValue.addAll(rightSqlParameter.realValue);
                                        break;
                                    }
                                }
                            }
                        }

                        if (this.getLeft() instanceof ConstantExpression) {
                            ConstantExpression leftConstant = (ConstantExpression) this.getLeft();
                            if (leftConstant.getValue() instanceof Boolean) {
                                Boolean leftBoolValue = (Boolean) leftConstant.getValue();

                                if (this.getRight() instanceof LikeExpression) {

                                    result = leftBoolValue ? "" : " NOT " + this.getRight().toString(sourceType, rightSqlParameter, creator);
                                    sqlParameters.realValue.addAll(rightSqlParameter.realValue);
                                    break;
                                }

                                if (this.getRight() instanceof UnaryExpression) {
                                    UnaryExpression unaryExpression = (UnaryExpression) this.getRight();
                                    if (unaryExpression.getNodeType() == EExpressionType.Not) {
                                        result = "Not " + this.getLeft().toString(sourceType, leftSqlParameter, creator) + " = " + unaryExpression.getOperand().toString(sourceType, rightSqlParameter, creator);
                                        sqlParameters.realValue.addAll(leftSqlParameter.realValue);
                                        sqlParameters.realValue.addAll(rightSqlParameter.realValue);
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    result = this.getLeft().toString(sourceType, leftSqlParameter, creator) + " = " + this.getRight().toString(sourceType, rightSqlParameter, creator);
                    sqlParameters.realValue.addAll(leftSqlParameter.realValue);
                    sqlParameters.realValue.addAll(rightSqlParameter.realValue);
                }
                break;
            }

            case NotEqual: {
                if (isLeftNull || isRightNull) {
                    ObjectReferencePack<List<DataParameter>> sqlParameter = new ObjectReferencePack<>();
                    if (isLeftNull) {
                        result = this.getRight().toString(sourceType, sqlParameter, creator) + " IS NOT NULL";
                        sqlParameters.realValue.addAll(sqlParameter.realValue);
                        break;
                    }

                    result = this.getLeft().toString(sourceType, sqlParameter, creator) + " IS NOT NULL";
                    sqlParameters.realValue.addAll(sqlParameter.realValue);
                } else {

                    if (sourceType == EDataSource.SqlServer) {
                        if (this.getRight() instanceof ConstantExpression) {
                            ConstantExpression rightConstant = (ConstantExpression) this.getRight();
                            if (rightConstant.getValue() instanceof Boolean) {
                                Boolean rightBoolValue = (Boolean) rightConstant.getValue();

                                if (this.getLeft() instanceof LikeExpression) {

                                    result = rightBoolValue ? " NOT " : "" + this.getLeft().toString(sourceType, leftSqlParameter, creator);
                                    sqlParameters.realValue.addAll(leftSqlParameter.realValue);
                                    break;
                                }

                                if (this.getLeft() instanceof UnaryExpression) {
                                    UnaryExpression unaryExpression = (UnaryExpression) this.getLeft();
                                    if (unaryExpression.getNodeType() == EExpressionType.Not) {
                                        result = "Not " + unaryExpression.getOperand().toString(sourceType, leftSqlParameter, creator) + " <> " + this.getRight().toString(sourceType, rightSqlParameter, creator);
                                        sqlParameters.realValue.addAll(leftSqlParameter.realValue);
                                        sqlParameters.realValue.addAll(rightSqlParameter.realValue);
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (this.getLeft() instanceof ConstantExpression) {
                        ConstantExpression leftConstant = (ConstantExpression) this.getLeft();
                        if (leftConstant.getValue() instanceof Boolean) {
                            Boolean leftBoolValue = (Boolean) leftConstant.getValue();

                            if (this.getRight() instanceof LikeExpression) {

                                result = leftBoolValue ? " NOT " : "" + this.getRight().toString(sourceType, rightSqlParameter, creator);
                                sqlParameters.realValue.addAll(rightSqlParameter.realValue);
                                break;
                            }

                            if (this.getRight() instanceof UnaryExpression) {
                                UnaryExpression unaryExpression = (UnaryExpression) this.getRight();
                                if (unaryExpression.getNodeType() == EExpressionType.Not) {
                                    result = "Not " + this.getLeft().toString(sourceType, leftSqlParameter, creator) + " <> " + unaryExpression.getOperand().toString(sourceType, rightSqlParameter, creator);
                                    sqlParameters.realValue.addAll(leftSqlParameter.realValue);
                                    sqlParameters.realValue.addAll(rightSqlParameter.realValue);
                                    break;
                                }
                            }
                        }
                    }

                    result = this.getLeft().toString(sourceType, leftSqlParameter, creator) + " <> " + this.getRight().toString(sourceType, rightSqlParameter, creator);
                    sqlParameters.realValue.addAll(leftSqlParameter.realValue);
                    sqlParameters.realValue.addAll(rightSqlParameter.realValue);
                }
                break;
            }
            case LessThan: {
                result = this.getLeft().toString(sourceType, leftSqlParameter, creator) + " < " + this.getRight().toString(sourceType, rightSqlParameter, creator);
                sqlParameters.realValue.addAll(leftSqlParameter.realValue);
                sqlParameters.realValue.addAll(rightSqlParameter.realValue);
                break;
            }
            case LessThanOrEqual: {
                result = this.getLeft().toString(sourceType, leftSqlParameter, creator) + " <= " + this.getRight().toString(sourceType, rightSqlParameter, creator);
                sqlParameters.realValue.addAll(leftSqlParameter.realValue);
                sqlParameters.realValue.addAll(rightSqlParameter.realValue);
                break;
            }
            case GreaterThan: {
                result = this.getLeft().toString(sourceType, leftSqlParameter, creator) + " > " + this.getRight().toString(sourceType, rightSqlParameter, creator);
                sqlParameters.realValue.addAll(leftSqlParameter.realValue);
                sqlParameters.realValue.addAll(rightSqlParameter.realValue);
                break;
            }
            case GreaterThanOrEqual: {
                result = this.getLeft().toString(sourceType, leftSqlParameter, creator) + " >= " + this.getRight().toString(sourceType, rightSqlParameter, creator);
                sqlParameters.realValue.addAll(leftSqlParameter.realValue);
                sqlParameters.realValue.addAll(rightSqlParameter.realValue);
                break;
            }
            default:
                throw new IllegalArgumentException("未知的表达式类型: " + this.getNodeType());
        }

        DataParameterSorter.sort(sqlParameters.realValue);

        return result;
    }
}
