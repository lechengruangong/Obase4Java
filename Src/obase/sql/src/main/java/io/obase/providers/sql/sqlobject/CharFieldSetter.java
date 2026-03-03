/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：字符字段设置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 11:50:45
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

/**
 * 字符字段设置器
 */
public class CharFieldSetter extends FieldSetter<Character> {

    /**
     * 构造字段设值器
     *
     * @param field     字段名称
     * @param character 字段值
     */
    public CharFieldSetter(String field, Character character) {
        super(field, character);
    }

    /**
     * 构造字段设值器
     *
     * @param source    源
     * @param field     字段名称
     * @param character 字段值
     */
    public CharFieldSetter(String source, String field, Character character) {
        super(source, field, character);
    }

    /**
     * 构造字段设值器
     *
     * @param field     字段
     * @param character 字段值
     */
    public CharFieldSetter(Field field, Character character) {
        super(field, character);
    }

    /**
     * 将字段设值器实例转换成字符串表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {
        return this.field.toString(sourceType) + "='" + this.value + "'";
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
        field.realValue = this.getFiledString(sourceType);
        return "'" + this.value + "'";
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
        String valueStr = this.value.toString();

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

        String valueStr = this.value.toString();

        return this.getParameters(parameters, sourceType, valueStr, creator);
    }
}
