/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：字段设值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 11:35:29
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Utils;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示字段设值器
 */
public class ExpressionFieldSetter implements IFieldSetter {

    /**
     * 要设置其值的字段
     */
    private final Field field;

    /**
     * 表示字段值的表达式
     */
    private final Expression value;

    /**
     * 使用字段名称和值表达式创建FieldSetter实例
     *
     * @param field 字段名称
     * @param value 值表达式
     */
    public ExpressionFieldSetter(String field, Expression value) {
        this.field = new Field(field);
        this.value = value;
    }

    /**
     * 使用源名称、字段名称和值表达式创建FieldSetter实例
     *
     * @param source 源名称
     * @param field  字段名称
     * @param value  值表达式
     */
    public ExpressionFieldSetter(String source, String field, Expression value) {
        this.field = Utils.getStringIsEmpty(source) ? new Field(field) : new Field(source, field);
        this.value = value;
    }

    /**
     * 使用字段实例和值表达式创建FieldSetter实例
     *
     * @param field 字段
     * @param value 值表达式
     */
    public ExpressionFieldSetter(Field field, Expression value) {
        this.field = field;
        this.value = value;
    }

    /**
     * 获取要为其设置值的字段
     *
     * @return 要为其设置值的字段
     */
    @Override
    public Field getField() {
        return this.field;
    }

    /**
     * 获取字段的值表达式
     *
     * @return 值表达式
     */
    public Expression getValue() {
        return this.value;
    }

    /**
     * 将字段设值器实例转换成字符串表示形式，该字符串将用于Update Sql的Set字句
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {
        return this.field.toString(sourceType) + " = " + (this.value == null ? "null" : "'" + this.value.toString(sourceType)) + "'";
    }

    /**
     * 将字段设值器实例转换成字符串表示形式，该字符串将用于Insert语句的Values字句，同时返回字段名称，用于Insert语句的字段列表
     *
     * @param field 返回字段名称
     * @return 字符串表示形式
     */
    @Override
    public String toString(ObjectReferencePack<String> field) {
        return this.toString(field, EDataSource.SqlServer);
    }

    /**
     * 将字段设值器实例转换成参数化的字符串表示形式，该字符串将用于Insert语句的Values字句，同时返回字段名称，用于Insert语句的字段列表。
     *
     * @param field      返回字段名称
     * @param parameters 返回字符串中的参数及其值
     * @param creator    参数构造器
     * @return 字符串表示形式
     */
    @Override
    public String toString(ObjectReferencePack<String> field, ObjectReferencePack<DataParameter> parameters, IParameterCreator creator) {
        return this.toString(parameters, field, EDataSource.SqlServer, creator);
    }

    /**
     * 将字段设值器实例转换成字符串表示形式，该字符串将用于Update Sql的Set字句
     *
     * @param field      返回字段名称
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(ObjectReferencePack<String> field, EDataSource sourceType) {
        switch (sourceType) {
            case SqlServer:
                field.realValue = "[" + this.field.getName() + "]";
                break;
            case PostgreSql:
                field.realValue = "\"" + this.field.getName() + "\"";
                break;
            case MySql:
            case Oracle:
            case Sqlite:
                field.realValue = "`" + this.field.getName() + "`";
                break;
            default:
                throw new IllegalArgumentException("不支持的数据源: " + sourceType);
        }

        return this.getValue().toString(sourceType);
    }

    /**
     * 将字段设值器实例转换成参数化的字符串表示形式，该字符串将用于Insert语句的Values字句，同时返回字段名称，用于Insert语句的字段列表。
     *
     * @param parameters 参数
     * @param creator    参数对象构造器
     * @return 字符串表示形式
     */
    @Override
    public String toString(ObjectReferencePack<DataParameter> parameters, IParameterCreator creator) {
        return this.toString(parameters, EDataSource.SqlServer, creator);
    }

    /**
     * 将字段设值器实例转换成参数化的字符串表示形式，该字符串将用于Insert语句的Values字句，同时返回字段名称，用于Insert语句的字段列表。
     *
     * @param parameters 参数
     * @param sourceType 数据源
     * @param creator    参数构造器
     * @return 字符串表示形式
     */
    @Override
    public String toString(ObjectReferencePack<DataParameter> parameters, EDataSource sourceType, IParameterCreator creator) {
        String valueStr;
        if (this.value == null) {
            valueStr = "null";
            parameters.realValue = null;
        } else {
            ObjectReferencePack<List<DataParameter>> sqlParameters = new ObjectReferencePack<>();
            sqlParameters.realValue = new ArrayList<>();
            valueStr = this.value.toString(sourceType, sqlParameters, creator);
            parameters.realValue = sqlParameters.realValue.size() > 0 ? sqlParameters.realValue.get(0) : null;
        }

        return this.field.toString(sourceType) + " = " + valueStr;
    }

    /**
     * 将字段设值器实例转换成参数化的字符串表示形式，该字符串将用于Insert语句的Values字句，同时返回字段名称，用于Insert语句的字段列表。
     *
     * @param parameters 参数
     * @param field      字段
     * @param sourceType 数据源
     * @param creator    参数构造器
     * @return 字符串表示形式
     */
    @Override
    public String toString(ObjectReferencePack<DataParameter> parameters, ObjectReferencePack<String> field, EDataSource sourceType, IParameterCreator creator) {
        switch (sourceType) {
            case MySql:
            case Oracle:
            case Sqlite:
                field.realValue = "`" + this.field.getName() + "`";
                break;
            case SqlServer:
                field.realValue = "[" + this.field.getName() + "]";
                break;
            case PostgreSql:
                field.realValue = "\"" + this.field.getName() + "\"";
                break;
            default:
                throw new IllegalArgumentException("不支持的数据源: " + sourceType);
        }

        ObjectReferencePack<List<DataParameter>> sqlParameters = new ObjectReferencePack<>();
        sqlParameters.realValue = new ArrayList<>();
        String valueStr = this.value.toString(sourceType, sqlParameters, creator);
        parameters.realValue = sqlParameters.realValue.size() > 0 ? sqlParameters.realValue.get(0) : null;

        return valueStr;
    }
}
