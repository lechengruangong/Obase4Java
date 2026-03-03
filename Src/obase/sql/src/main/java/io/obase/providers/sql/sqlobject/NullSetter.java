/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Null设值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 11:28:07
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;
import org.apache.commons.lang3.StringUtils;

/**
 * Null设值器，用于将字段的值设值为NULL。
 */
public class NullSetter implements IFieldSetter {

    /**
     * 字段
     */
    private final Field field;

    /**
     * 创建NullSetter实例
     *
     * @param field 表示要为其设置值的字段
     */
    public NullSetter(Field field) {
        this.field = field;
    }

    /**
     * 创建NullSetter实例
     *
     * @param fieldName 表示要为其设置值的字段的名称
     */
    public NullSetter(String fieldName) {
        this.field = new Field(fieldName);
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
     * 将字段设值器实例转换成字符串表示形式，该字符串将用于Update Sql的Set字句
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {
        switch (sourceType) {
            case SqlServer:
                return " [" + this.getField().getName() + "] = null";
            case PostgreSql:
                return " \"" + this.getField().getName() + "\" = null";
            case MySql:
            case Sqlite:
            case Oracle:
                return " `" + this.getField().getName() + "` = null";
            default:
                throw new IllegalArgumentException("不支持的数据源: " + sourceType);
        }
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
                field.realValue = " [" + this.getField().getName() + "]";
                break;
            case PostgreSql:
                field.realValue = " \"" + this.getField().getName() + "\"";
                break;
            case MySql:
            case Sqlite:
            case Oracle:
                field.realValue = " `" + this.getField().getName() + "`";
                break;
            default:
                throw new IllegalArgumentException("不支持的数据源: " + sourceType);
        }

        return " null ";
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
        String parameter = this.getParameters(parameters, creator);

        return this.field.toString(sourceType) + " = " + parameter;
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
        field.realValue = this.getFiledString(sourceType);
        return this.getParameters(parameters, creator);
    }

    /**
     * 根据不同的数据源返回字段字符串
     *
     * @param sourceType 数据源类型
     * @return 字段字符串
     */
    private String getFiledString(EDataSource sourceType) {
        //SqlServer [字段] MySql `字段`
        String field;
        switch (sourceType) {
            case SqlServer: {
                field = "[" + this.getField().getName() + "]";
                break;
            }
            case PostgreSql: {
                field = "\"" + StringUtils.capitalize(this.getField().getName()) + "\"";
                break;
            }
            case MySql:
            case Oracle:
            case Sqlite: {
                field = "`" + this.getField().getName() + "`";
                break;
            }
            default: {
                throw new IllegalArgumentException("不支持的数据源: " + sourceType);
            }
        }

        return field;
    }

    /**
     * 根据不同的数据源返回参数和参数名字符串
     *
     * @param parameters 参数化参数集合
     * @param creator    参数化参数建造器
     * @return 参数指代
     */
    private String getParameters(ObjectReferencePack<DataParameter> parameters, IParameterCreator creator) {
        parameters.realValue = creator.create();
        parameters.realValue.Index = 1;
        parameters.realValue.Value = null;

        return "?";
    }
}
