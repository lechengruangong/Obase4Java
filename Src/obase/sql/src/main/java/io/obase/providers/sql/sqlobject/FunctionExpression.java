/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示调用函数的表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 15:34:00
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表示调用函数的表达式
 */
public class FunctionExpression extends Expression {

    /**
     * 实参集合
     */
    private final Expression[] arguments;

    /**
     * 函数的名称
     */
    private final String functionName;

    /**
     * 作用于函数表达式的over子句
     */
    private OverClause over;

    /**
     * 创建FunctionExpression的实例，并指定Arguments属性的值
     *
     * @param functionName 方法名称
     * @param arguments    参数集合
     */
    public FunctionExpression(String functionName, Expression[] arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String getFunctionName() {
        return this.functionName;
    }

    /**
     * 获取实参集合
     *
     * @return 实参集合
     */
    public Expression[] getArguments() {
        return this.arguments;
    }

    /**
     * 获取作用于函数表达式的Over子句
     *
     * @return 函数表达式的Over子句
     */
    public OverClause getOver() {
        return this.over;
    }

    /**
     * 设置作用于函数表达式的Over子句
     *
     * @param over 函数表达式的Over子句
     */
    public void setOver(OverClause over) {
        this.over = over;
    }

    /**
     * 派生类实现此方法以判定具体类型的表达式对象是否相等
     *
     * @param other 要与当前表达式进行比较的表达式
     * @return 是否相等
     */
    @Override
    protected boolean concreteEquals(Expression other) {
        if (other instanceof FunctionExpression) {
            FunctionExpression funcOther = (FunctionExpression) other;
            return Objects.equals(funcOther.getFunctionName(), this.getFunctionName()) && Arrays.equals(funcOther.getArguments(), this.getArguments()) && funcOther.getOver() == this.getOver();
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
        String result;

        //识别是否为空
        String isNullStr;
        switch (sourceType) {
            case SqlServer: {
                isNullStr = "isnull";
                break;
            }
            case PostgreSql: {
                isNullStr = "COALESCE";
                break;
            }
            case Oledb:
            case MySql:
            case Oracle:
            case Sqlite: {
                isNullStr = "ifnull";
                break;
            }
            default:
                throw new IllegalArgumentException("不支持的数据源类型: " + sourceType);
        }

        switch (this.getFunctionName().toUpperCase()) {
            case "CONVERT": {
                switch (sourceType) {

                    case SqlServer:
                    case PostgreSql:
                        result = this.getFunctionName() + "(" + Arrays.stream(this.getArguments()).map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) + ")";
                        break;
                    case Oracle:
                    case MySql: {
                        //MySql的Convert函数类型仅支持以下几类BINARY,CHAR,DATE,TIME,DATETIME,DECIMAL,SIGNED,UNSIGNED
                        if (this.arguments.length > 0 && this.arguments[0] instanceof ConstantExpression) {
                            ConstantExpression constant = (ConstantExpression) this.arguments[0];
                            switch (constant.getValue().toString().toLowerCase()) {
                                case "smallint":
                                case "int":
                                case "bigint":
                                case "bit": {
                                    constant = new ConstantExpression("SIGNED");
                                    this.arguments[0] = constant;
                                    break;
                                }
                                case "varchar":
                                case "char": {
                                    constant = new ConstantExpression("CHAR");
                                    this.arguments[0] = constant;
                                    break;
                                }
                                case "real":
                                case "float":
                                case "numeric": {
                                    constant = new ConstantExpression("DECIMAL");
                                    this.arguments[0] = constant;
                                    break;
                                }
                            }
                        }


                        List<Expression> reserved = Arrays.stream(this.getArguments()).collect(Collectors.toList());
                        Collections.reverse(reserved);
                        result = this.getFunctionName() + "(" + reserved.stream().map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) + ")";
                        break;
                    }
                    case Sqlite: {
                        //Sqlite翻译为Cast(xx as xx)
                        //Sqlite的Cast函数类型仅支持以下几类TEXT,REAL,INTEGER
                        if (this.arguments.length > 0 && this.arguments[0] instanceof ConstantExpression) {
                            ConstantExpression constant = (ConstantExpression) this.arguments[0];
                            switch (constant.getValue().toString().toLowerCase()) {
                                case "smallint":
                                case "int":
                                case "bigint":
                                case "bit": {
                                    constant = new ConstantExpression("INTEGER");
                                    this.arguments[0] = constant;
                                    break;
                                }
                                case "varchar":
                                case "char": {
                                    constant = new ConstantExpression("TEXT");
                                    this.arguments[0] = constant;
                                    break;
                                }
                                case "real":
                                case "float":
                                case "numeric": {
                                    constant = new ConstantExpression("REAL");
                                    this.arguments[0] = constant;
                                    break;
                                }
                            }
                        }
                        List<Expression> reserved = Arrays.stream(this.getArguments()).collect(Collectors.toList());
                        Collections.reverse(reserved);

                        result = "CAST(" + reserved.stream().map(p -> p.toString(sourceType)).collect(Collectors.joining(" AS ")) + ")";
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("不支持的数据源: " + sourceType);
                }
                break;
            }
            case "AVERAGE": {
                if (Arrays.stream(this.arguments).allMatch(Objects::isNull)) {
                    result = isNullStr + "(AVG(CAST(1 as decimal(10,2))),0)";
                } else {
                    result = isNullStr + "(AVG(CAST(" + Arrays.stream(this.getArguments()).map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) + "as decimal(10,2))),0)";
                }
                break;
            }
            case "MAX": {
                if (Arrays.stream(this.arguments).allMatch(Objects::isNull)) {
                    result = isNullStr + "(MAX(1),0)";
                } else {
                    result = isNullStr + "(MAX(" + Arrays.stream(this.getArguments()).map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) + "),0)";
                }
                break;
            }
            case "MIN": {
                if (Arrays.stream(this.arguments).allMatch(Objects::isNull)) {
                    result = isNullStr + "(MIN(1),0)";
                } else {
                    result = isNullStr + "(MIN(" + Arrays.stream(this.getArguments()).map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) + "),0)";
                }
                break;
            }
            case "SUM": {
                if (Arrays.stream(this.arguments).allMatch(Objects::isNull)) {
                    result = isNullStr + "(SUM(1),0)";
                } else {
                    result = isNullStr + "(SUM(" + Arrays.stream(this.getArguments()).map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) + "),0)";
                }
                break;
            }
            case "CONCAT": {

                if (sourceType == EDataSource.Sqlite || sourceType == EDataSource.PostgreSql) {
                    List<String> args = new ArrayList<>();
                    if (this.getArguments() != null && this.getArguments().length > 0)
                        for (Expression expression : this.getArguments()) {
                            if (expression instanceof FieldExpression) {
                                FieldExpression fieldExpression = (FieldExpression) expression;
                                args.add(fieldExpression.getField().getSource().toString(sourceType) + "." + fieldExpression.toString(sourceType));
                            } else {
                                args.add(expression.toString(sourceType));
                            }
                        }

                    result = String.join("||", args);
                } else {
                    result = this.getFunctionName() + "(" + Arrays.stream(this.getArguments()).map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) + ")";
                }

                break;
            }
            default: {
                result = this.getFunctionName() + "(" + Arrays.stream(this.getArguments()).map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) + ")";
                break;
            }
        }

        if (this.getOver() != null) result += this.getOver().toString(sourceType);

        return result;
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
        //函数表达式没有参数化
        sqlParameters.realValue = new ArrayList<>();

        return this.toString(sourceType);
    }
}
