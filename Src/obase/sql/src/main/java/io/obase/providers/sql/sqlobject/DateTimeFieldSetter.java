/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：日期时间字段设置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 11:53:56
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间字段设置器
 */
public class DateTimeFieldSetter extends FieldSetter<LocalDateTime> {
    /**
     * 构造字段设值器
     *
     * @param field    字段名称
     * @param dateTime 字段值
     */
    public DateTimeFieldSetter(String field, LocalDateTime dateTime) {
        super(field, dateTime);
    }

    /**
     * 构造字段设值器
     *
     * @param source   源
     * @param field    字段名称
     * @param dateTime 字段值
     */
    public DateTimeFieldSetter(String source, String field, LocalDateTime dateTime) {
        super(source, field, dateTime);
    }

    /**
     * 构造字段设值器
     *
     * @param field    字段
     * @param dateTime 字段值
     */
    public DateTimeFieldSetter(Field field, LocalDateTime dateTime) {
        super(field, dateTime);
    }

    /**
     * 将字段设值器实例转换成字符串表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {
        LocalDateTime dateTime = this.getValue();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        if (dateTime.isAfter(LocalDateTime.parse("1753-01-01 00:00:00", format)) && dateTime.isBefore(LocalDateTime.parse("9999-12-31 00:00:00", format))) {
            return this.field.toString(sourceType) + "='" + format.format(dateTime) + "'";
        }
        return this.field.toString(sourceType) + "=null";
    }

    /**
     * 将字段设值器实例转换成字符串表示形式，该字符串将用于Insert语句的Values字句，同时返回字段名称，用于Insert语句的字段列表。
     *
     * @param field 返回字段名称
     * @return 字符串表示形式
     */
    @Override
    public String toString(ObjectReferencePack<String> field) {
        return this.toString(field, EDataSource.SqlServer);
    }

    /**
     * 将字段设值器实例转换成字符串表示形式
     *
     * @param field      返回字段名称
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(ObjectReferencePack<String> field, EDataSource sourceType) {
        LocalDateTime dateTime = this.getValue();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        field.realValue = this.getFiledString(sourceType);
        if (dateTime.isAfter(LocalDateTime.parse("1753-01-01 00:00:00", format)) && dateTime.isBefore(LocalDateTime.parse("9999-12-31 00:00:00", format))) {
            return format.format(dateTime);
        }
        return "null";
    }

    /**
     * 将字段设值器实例转换成字符串表示形式
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
        LocalDateTime dateTime = this.getValue();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String valueStr;
        if (dateTime.isAfter(LocalDateTime.parse("1753-01-01 00:00:00", format)) && dateTime.isBefore(LocalDateTime.parse("9999-12-31 00:00:00", format))) {
            valueStr = format.format(dateTime);
        } else {
            valueStr = "null";
        }

        String parameter = this.getParameters(parameters, sourceType, valueStr, creator);

        return this.field.toString(sourceType) + "=" + parameter + "";
    }

    /**
     * 将字段设值器实例转换成字符串表示形式
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

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime dateTime = this.getValue();
        String valueStr;
        if (dateTime.isAfter(LocalDateTime.parse("1753-01-01 00:00:00.000", format)) && dateTime.isBefore(LocalDateTime.parse("9999-12-31 00:00:00.000", format))) {
            valueStr = format.format(dateTime);
        } else {
            valueStr = "null";
        }

        return this.getParameters(parameters, sourceType, valueStr, creator);
    }

    /**
     * 根据不同的数据源返回参数和参数名字符串
     *
     * @param parameters 参数化参数集合
     * @param sourceType 数据源类型
     * @param valueStr   值字符串表示
     * @param creator    参数化参数建造器
     * @return 参数指代
     */
    @Override
    protected String getParameters(ObjectReferencePack<DataParameter> parameters, EDataSource sourceType, Object valueStr,
                                   IParameterCreator creator) {
        //参数名
        String parameter = "?";

        parameters.realValue = creator.create();
        parameters.realValue.Index = 1;
        //非空 加入参数
        boolean aNull = !valueStr.toString().trim().equalsIgnoreCase("null");
        parameters.realValue.Value = aNull ? valueStr : null;
        if (sourceType == EDataSource.PostgreSql && parameters.realValue.Value != null) {
            if (parameters.realValue.Value.toString().lastIndexOf("00:00:00.000") > 0) {
                parameters.realValue.Value = java.sql.Date.valueOf(parameters.realValue.Value.toString().replace(" 00:00:00.000", ""));
            } else {
                parameters.realValue.Value = java.sql.Timestamp.valueOf(parameters.realValue.Value.toString());
            }
        }
        return parameter;
    }
}
