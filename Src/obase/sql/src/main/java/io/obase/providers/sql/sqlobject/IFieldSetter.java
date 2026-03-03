/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：字段设值器接口.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 11:15:38
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

/**
 * 字段设值器接口。
 * 字段设值器用于指定字段的值，如：字段名=值，一般用于Update和Insert语句
 */
public interface IFieldSetter {

    /**
     * 获取要为其设置值的字段
     *
     * @return 要为其设置值的字段
     */
    Field getField();

    /**
     * 将字段设值器实例转换成字符串表示形式，该字符串将用于Update Sql的Set字句
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    String toString(EDataSource sourceType);

    /**
     * 将字段设值器实例转换成字符串表示形式，该字符串将用于Insert语句的Values字句，同时返回字段名称，用于Insert语句的字段列表
     *
     * @param field 返回字段名称
     * @return 字符串表示形式
     */
    String toString(ObjectReferencePack<String> field);

    /**
     * 将字段设值器实例转换成参数化的字符串表示形式，该字符串将用于Insert语句的Values字句，同时返回字段名称，用于Insert语句的字段列表。
     *
     * @param field      返回字段名称
     * @param parameters 返回字符串中的参数及其值
     * @param creator    参数构造器
     * @return 字符串表示形式
     */
    String toString(ObjectReferencePack<String> field, ObjectReferencePack<DataParameter> parameters, IParameterCreator creator);

    /**
     * 将字段设值器实例转换成字符串表示形式，该字符串将用于Update Sql的Set字句
     *
     * @param field      返回字段名称
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    String toString(ObjectReferencePack<String> field, EDataSource sourceType);

    /**
     * 将字段设值器实例转换成参数化的字符串表示形式，该字符串将用于Insert语句的Values字句，同时返回字段名称，用于Insert语句的字段列表。
     *
     * @param parameters 参数
     * @param creator    参数对象构造器
     * @return 字符串表示形式
     */
    String toString(ObjectReferencePack<DataParameter> parameters, IParameterCreator creator);

    /**
     * 将字段设值器实例转换成参数化的字符串表示形式，该字符串将用于Insert语句的Values字句，同时返回字段名称，用于Insert语句的字段列表。
     *
     * @param parameters 参数
     * @param sourceType 数据源
     * @param creator    参数构造器
     * @return 字符串表示形式
     */
    String toString(ObjectReferencePack<DataParameter> parameters, EDataSource sourceType, IParameterCreator creator);

    /**
     * 将字段设值器实例转换成参数化的字符串表示形式，该字符串将用于Insert语句的Values字句，同时返回字段名称，用于Insert语句的字段列表。
     *
     * @param parameters 参数
     * @param field      字段
     * @param sourceType 数据源
     * @param creator    参数构造器
     * @return 字符串表示形式
     */
    String toString(ObjectReferencePack<DataParameter> parameters, ObjectReferencePack<String> field, EDataSource sourceType, IParameterCreator creator);
}
