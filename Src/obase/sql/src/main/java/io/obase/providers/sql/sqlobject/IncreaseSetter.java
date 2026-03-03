/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：增量设值器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 14:36:02
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

/**
 * 增量设值器，即在现值上增加指定值
 *
 * @param <TIncrease> 增量类型
 */
public class IncreaseSetter<TIncrease> extends FieldSetter<TIncrease> {
    /**
     * 构造字段设值器
     *
     * @param field     字段名称
     * @param tIncrease 字段值
     */
    public IncreaseSetter(String field, TIncrease tIncrease) {
        super(field, tIncrease);
    }

    /**
     * 构造字段设值器
     *
     * @param source    源
     * @param field     字段名称
     * @param tIncrease 字段值
     */
    public IncreaseSetter(String source, String field, TIncrease tIncrease) {
        super(source, field, tIncrease);
    }

    /**
     * 构造字段设值器
     *
     * @param field     字段
     * @param tIncrease 字段值
     */
    public IncreaseSetter(Field field, TIncrease tIncrease) {
        super(field, tIncrease);
    }

    /**
     * 将字段设值器实例转换成字符串表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {
        return this.field.toString(sourceType) + "=" + this.field.toString(sourceType) + " + " + this.value;
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
        return this.field.toString(sourceType) + " + " + this.value;
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
        String valueStr = "?";

        String icreasValue = this.field.toString(sourceType) + "=" + this.field.toString(sourceType) + " + " + valueStr;
        parameters.realValue = creator.create();
        parameters.realValue.Value = this.value.toString();

        return icreasValue;
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

        String increaseValue = this.field.toString(sourceType) + "=" + this.field.toString(sourceType) + " + " + valueStr;
        parameters.realValue = creator.create();

        return increaseValue;
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
    protected String getParameters(ObjectReferencePack<DataParameter> parameters, EDataSource sourceType, Object valueStr, IParameterCreator creator) {
        parameters.realValue = creator.create();

        //形如 参数名+值
        Object increaseValue = (this.field.toString(sourceType) + "=" + this.field.toString(sourceType) + " + " + valueStr);

        parameters.realValue.Index = 1;
        parameters.realValue.Value = increaseValue;

        return "?";
    }
}
