/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：字段设值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 11:18:17
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Utils;
import io.obase.providers.sql.EDataSource;
import org.apache.commons.lang3.StringUtils;

/**
 * 字段设值器
 *
 * @param <TValue> 字段类型
 */
public abstract class FieldSetter<TValue> implements IFieldSetter {

    /**
     * 字段
     */
    protected final Field field;

    /**
     * 值
     */
    protected final TValue value;

    /**
     * 构造字段设值器
     *
     * @param field 字段名称
     * @param value 字段值
     */
    protected FieldSetter(String field, TValue value) {
        this.field = new Field(field);
        this.value = value;
    }

    /**
     * 构造字段设值器
     *
     * @param source 源
     * @param field  字段名称
     * @param value  字段值
     */
    protected FieldSetter(String source, String field, TValue value) {
        this.field = Utils.getStringIsEmpty(source) ? new Field(field) : new Field(source, field);
        this.value = value;
    }

    /**
     * 构造字段设值器
     *
     * @param field 字段
     * @param value 字段值
     */
    protected FieldSetter(Field field, TValue value) {
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
     * 获取值
     *
     * @return 值
     */
    public TValue getValue() {
        return this.value;
    }

    /**
     * 将字段设值器实例转换成字符串表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    public abstract String toString(EDataSource sourceType);

    /**
     * 将字段设值器实例转换成字符串表示形式，该字符串将用于Insert语句的Values字句，同时返回字段名称，用于Insert语句的字段列表。
     *
     * @param field 返回字段名称
     * @return 字符串表示形式
     */
    public abstract String toString(ObjectReferencePack<String> field);

    /**
     * 将字段设值器实例转换成字符串表示形式
     *
     * @param field      返回字段名称
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    public abstract String toString(ObjectReferencePack<String> field, EDataSource sourceType);

    /**
     * 将字段设值器实例转换成字符串表示形式
     *
     * @param parameters 参数
     * @param creator    参数对象构造器
     * @return 字符串表示形式
     */
    public abstract String toString(ObjectReferencePack<DataParameter> parameters, IParameterCreator creator);

    /**
     * 将字段设值器实例转换成参数化的字符串表示形式，该字符串将用于Insert语句的Values字句，同时返回字段名称，用于Insert语句的字段列表。
     *
     * @param parameters 参数
     * @param sourceType 数据源
     * @param creator    参数构造器
     * @return 字符串表示形式
     */
    public abstract String toString(ObjectReferencePack<DataParameter> parameters, EDataSource sourceType, IParameterCreator creator);

    /**
     * 将字段设值器实例转换成字符串表示形式
     *
     * @param field      返回字段名称
     * @param parameters 返回字符串中的参数及其值
     * @param creator    参数构造器
     * @return 字符串表示形式
     */
    public abstract String toString(ObjectReferencePack<String> field, ObjectReferencePack<DataParameter> parameters, IParameterCreator creator);

    /**
     * 将字段设值器实例转换成参数化的字符串表示形式，该字符串将用于Insert语句的Values字句，同时返回字段名称，用于Insert语句的字段列表。
     *
     * @param parameters 参数
     * @param field      字段
     * @param sourceType 数据源
     * @param creator    参数构造器
     * @return 字符串表示形式
     */
    public abstract String toString(ObjectReferencePack<DataParameter> parameters, ObjectReferencePack<String> field, EDataSource sourceType, IParameterCreator creator);

    /**
     * 根据不同的数据源返回字段字符串
     *
     * @param sourceType 数据源
     * @return 字段字符串
     */
    protected String getFiledString(EDataSource sourceType) {
        //SqlServer [字段] MySql `字段`
        String field;
        switch (sourceType) {
            case SqlServer: {
                field = "[" + this.field.getName() + "]";
                break;
            }
            case PostgreSql: {
                field = "\"" + StringUtils.capitalize(this.field.getName()) + "\"";
                break;
            }
            case MySql:
            case Oracle:
            case Sqlite: {
                field = "`" + this.field.getName() + "`";
                break;
            }
            default:
                throw new IllegalArgumentException("不支持的数据源: " + sourceType);
        }

        return field;
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
    protected String getParameters(ObjectReferencePack<DataParameter> parameters, EDataSource sourceType, Object valueStr,
                                   IParameterCreator creator) {
        //参数名
        String parameter = "?";

        parameters.realValue = creator.create();
        parameters.realValue.Index = 1;
        //非空 加入参数
        parameters.realValue.Value = !valueStr.toString().trim().equalsIgnoreCase("null") ? valueStr : null;

        return parameter;
    }
}
