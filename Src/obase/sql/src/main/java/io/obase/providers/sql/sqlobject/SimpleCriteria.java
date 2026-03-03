/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示简单条件.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 16:24:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 表示简单条件，形如：[源名].字段名=值、[源名1].字段名1=[源名2].字段名2
 *
 * @param <TValue>
 */
public abstract class SimpleCriteria<TValue> extends ExpressionCriteria {

    /**
     * 创建简单条件实例
     *
     * @param field            字段名
     * @param relationOperator 关系运算符
     * @param value            参考值
     */
    protected SimpleCriteria(String field, ERelationOperator relationOperator, TValue value) {
        super(createExpression(new Field(field), relationOperator, value));
    }

    /**
     * 创建简单条件实例
     *
     * @param field            字段
     * @param relationOperator 关系运算符
     * @param value            参考值
     */
    protected SimpleCriteria(Field field, ERelationOperator relationOperator, TValue value) {
        super(createExpression(field, relationOperator, value));
    }

    /**
     * 创建简单条件实例
     *
     * @param source           源名称
     * @param field            字段名
     * @param relationOperator 关系运算符
     * @param value            参考值
     */
    protected SimpleCriteria(String source, String field, ERelationOperator relationOperator, TValue value) {
        super(createExpression(new Field(source, field), relationOperator, value));
    }

    /**
     * 创建表达式
     *
     * @param field            字段
     * @param relationOperator 操作符
     * @param value            值
     * @param <TValue>         值类型
     * @return 表达式
     */
    private static <TValue> Expression createExpression(Field field, ERelationOperator relationOperator, TValue value) {
        Expression valueExp;
        if (value instanceof Field) {
            Field valueField = (Field) value;
            valueExp = Expression.field(valueField);
        } else {
            valueExp = Expression.constant(value);
        }

        switch (relationOperator) {

            case Equal:
                return Expression.equal(Expression.field(field), valueExp);
            case Unequal:
                return Expression.notEqual(Expression.field(field), valueExp);
            case LessThanOrEqual:
                return Expression.lessThanOrEqual(Expression.field(field), valueExp);
            case LessThan:
                return Expression.lessThan(Expression.field(field), valueExp);
            case GreaterThan:
                return Expression.greaterThan(Expression.field(field), valueExp);
            case GreaterThanOrEqual:
                return Expression.greaterThanOrEqual(Expression.field(field), valueExp);
            case Like:
                return Expression.like(Expression.field(field), value.toString());
            case In:
                return Expression.in(Expression.field(field), (Object[]) value);
            case NotIn:
                return Expression.notIn(Expression.field(field), (Iterable<Object>) value);
        }

        return null;
    }

    /**
     * 获取参考值，可以为一个实际值或另外一个字段。
     *
     * @return 参考值
     */
    protected TValue getValue() {
        switch (this.getOperator()) {
            case Equal:
            case Unequal:
            case LessThanOrEqual:
            case LessThan:
            case GreaterThan:
            case GreaterThanOrEqual: {
                Expression exp = ((BinaryExpression) this.getExpression()).getRight();
                if (exp.getNodeType() == EExpressionType.Constant) {
                    return (TValue) ((ConstantExpression) exp).getValue();
                } else {
                    return (TValue) ((FieldExpression) exp).getField();
                }
            }
            case Like: {
                return (TValue) ((LikeExpression) ((BinaryExpression) this.getExpression()).getRight()).getPattern();
            }
            case In:
            case NotIn: {
                return (TValue) ((InExpression) ((BinaryExpression) this.getExpression()).getRight()).getValueSet();
            }
        }

        return null;
    }

    /**
     * 设置参考值，可以为一个实际值或另外一个字段
     *
     * @param value 参考值
     */
    protected void setValue(TValue value) {
        this.expression = createExpression(this.getField(), this.getOperator(), value);
    }

    /**
     * 获取字段
     *
     * @return 字段
     */
    protected Field getField() {
        return ((FieldExpression) ((BinaryExpression) this.getExpression()).getLeft()).getField();
    }

    /**
     * 设置字段
     *
     * @param value 字段
     */
    protected void setField(Field value) {
        this.expression = createExpression(value, this.getOperator(), this.getValue());
    }

    /**
     * 获取关系运算符
     *
     * @return 关系运算符
     */
    public ERelationOperator getOperator() {
        return this.getERelationOperator(this.getExpression().getNodeType());
    }

    /**
     * 设置关系运算符
     *
     * @param operator 关系运算符
     */
    public void setOperator(ERelationOperator operator) {
        this.expression = createExpression(this.getField(), operator, this.getValue());
    }

    /**
     * 关系运算符映射
     *
     * @param eExpressionType 表达式类型
     * @return 关系运算符
     */
    private ERelationOperator getERelationOperator(EExpressionType eExpressionType) {
        switch (eExpressionType) {
            case Equal:
                return ERelationOperator.Equal;
            case NotEqual:
                return ERelationOperator.Unequal;
            case LessThan:
                return ERelationOperator.LessThan;
            case LessThanOrEqual:
                return ERelationOperator.LessThanOrEqual;
            case GreaterThan:
                return ERelationOperator.GreaterThan;
            case GreaterThanOrEqual:
                return ERelationOperator.GreaterThanOrEqual;
            case Like:
                return ERelationOperator.Like;
            case In:
                return ERelationOperator.In;
            case NotIn:
                return ERelationOperator.NotIn;
        }

        return null;
    }

    /**
     * 生成value对应数据中的值
     *
     * @return 数据中的值
     */
    protected String generateSqlValue(EDataSource source) {
        String matchValue;
        Object value = this.getValue();

        if (value instanceof Field) {
            value = ((Field) value).toString(source);
        }

        if (this.getValue() == null) return null;

        if (value instanceof Date) {
            Date dateTime = (Date) value;
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            matchValue = ft.format(dateTime);
        } else if (value instanceof LocalDateTime) {
            LocalDateTime dateTime = (LocalDateTime) value;
            DateTimeFormatter ft = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            matchValue = ft.format(dateTime);
        } else if (value instanceof LocalDate) {
            LocalDate dateTime = (LocalDate) value;
            DateTimeFormatter ft = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            matchValue = ft.format(dateTime);
        } else if (value instanceof LocalTime) {
            LocalTime dateTime = (LocalTime) value;
            DateTimeFormatter ft = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
            matchValue = ft.format(dateTime);
        } else if (value instanceof Boolean) {
            if (source == EDataSource.SqlServer) {
                Boolean boolValue = (Boolean) value;
                matchValue = boolValue ? "1" : "0";
            } else {
                matchValue = value.toString();
            }
        } else if (value.getClass().isEnum()) {
            matchValue = String.valueOf(((Enum<?>) value).ordinal());
        } else if (value instanceof UUID) {
            UUID uuid = (UUID) value;
            matchValue = uuid.toString();
        } else {
            matchValue = value.toString();
        }

        return matchValue;
    }

    /**
     * 生成value对应数据中的值并返回参数
     *
     * @param sourceType    数据源
     * @param sqlParameters 参数
     * @param creator       参数对象构造器
     * @return 数据的值
     */
    protected String generateSqlValue(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        sqlParameters.realValue = new ArrayList<>();

        //生成值
        Object value = this.getValue();
        Object paValue;

        if (value instanceof Field) {
            value = ((Field) value).toString(sourceType);
        }

        if (value instanceof Date) {
            Date dateTime = (Date) value;
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            paValue = java.sql.Timestamp.valueOf(ft.format(dateTime));
        } else if (value instanceof LocalDateTime) {
            LocalDateTime dateTime = (LocalDateTime) value;
            paValue = java.sql.Timestamp.valueOf(dateTime);
        } else if (value instanceof LocalDate) {
            LocalDate dateTime = (LocalDate) value;
            paValue = java.sql.Date.valueOf(dateTime);
        } else if (value instanceof LocalTime) {
            LocalTime dateTime = (LocalTime) value;
            paValue = java.sql.Time.valueOf(dateTime);
        } else if (value instanceof Boolean) {
            if (sourceType == EDataSource.SqlServer) {
                Boolean boolValue = (Boolean) value;
                paValue = boolValue ? 1 : 0;
            } else if (sourceType == EDataSource.PostgreSql) {
                paValue = value;
            } else {
                paValue = value.toString();
            }
        } else if (value.getClass().isEnum()) {
            paValue = String.valueOf(((Enum<?>) value).ordinal());
            if (sourceType == EDataSource.PostgreSql) {
                paValue = ((Enum<?>) value).ordinal();
            }
        } else if (value instanceof UUID) {
            UUID uuid = (UUID) value;
            paValue = uuid.toString();
        } else {
            paValue = value;
        }

        DataParameter dataParameter = creator.create();
        dataParameter.Index = 1;
        dataParameter.Value = paValue;

        sqlParameters.realValue.add(dataParameter);

        DataParameterSorter.sort(sqlParameters.realValue);

        return "?";
    }

    /**
     * 针对指定的数据源类型，生成条件实例的字符串表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {
        //字段
        String result = this.getField().toString(sourceType);
        //值
        String matchValue = this.generateSqlValue(sourceType);

        return this.getReturnString(result, matchValue);
    }

    /**
     * 使用参数化的方式 和 指定的数据源 将Sql对象表示为Sql字符串
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        //字段
        String result = this.getField().toString(sourceType);
        ObjectReferencePack<List<DataParameter>> matchValueParameters = new ObjectReferencePack<>();
        //值
        String matchValue = this.generateSqlValue(sourceType, matchValueParameters, creator);

        String returnValue = this.getReturnString(result, matchValue);

        sqlParameters.realValue = new ArrayList<>();
        sqlParameters.realValue.addAll(matchValueParameters.realValue);

        DataParameterSorter.sort(sqlParameters.realValue);

        return returnValue;
    }

    /**
     * 使用默认数据源和参数化的方式将Sql对象表示为Sql字符串
     *
     * @param sqlParameters 参数
     * @param creator       参数构造器
     * @return 字符串表示形式
     */
    @Override
    public String toString(ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        return this.toString(EDataSource.SqlServer, sqlParameters, creator);
    }

    /**
     * 获取表达式的返回值字符串
     *
     * @param result     要处理的结果
     * @param matchValue 取出的值结果
     * @return 拼接后的结果
     */
    private String getReturnString(String result, String matchValue) {
        String returnValue = "";
        switch (this.getOperator()) {
            case Equal:
                returnValue = matchValue != null ? result + " = " + matchValue : result + " is null";
                break;
            case GreaterThan:
                returnValue = result + " > " + matchValue;
                break;
            case GreaterThanOrEqual:
                returnValue = result + " >= " + matchValue;
                break;
            case In:
                returnValue = result + " in (" + matchValue + ")";
                break;
            case LessThan:
                returnValue = result + " < " + matchValue;
                break;
            case LessThanOrEqual:
                returnValue = result + " <= " + matchValue;
                break;
            case Like:
                returnValue = result + "like '%" + matchValue.replaceAll("[" + "%" + "]+$", "").replaceAll("^[" + "%" + "]+", "") + "%'";
                break;
            case NotIn:
                returnValue = result + " not in (" + matchValue + ")";
                break;
            case Unequal:
                returnValue = matchValue != null ? result + " <> " + matchValue : result + " is not null";
                break;
        }
        return returnValue;
    }
}
