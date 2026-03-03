/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示集运算操作数.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 17:02:06
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.List;

/**
 * 表示集运算操作数
 */
public interface ISetOperand {

    /**
     * 使用参数化的方式 和 默认的数据源 将Sql对象表示为Sql字符串
     *
     * @param parameters 返回字符串中的参数及其值的集合
     * @param creator    参数构造器
     * @return Sql字符串
     */
    String toSql(ObjectReferencePack<List<DataParameter>> parameters, IParameterCreator creator);

    /**
     * 对指定的数据源类型，根据查询Sql语句的对象表示法生成Sql语句。
     *
     * @param sourceType 数据源类型
     * @return Sql字符串
     */
    String toSql(EDataSource sourceType);

    /**
     * 使用参数化的方式 和 指定的数据源 将Sql对象表示为Sql字符串
     *
     * @param sourceType 指定的数据源
     * @param parameters 参数
     * @param creator    参数构造器
     * @return Sql字符串
     */
    String toSql(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> parameters, IParameterCreator creator);

}
