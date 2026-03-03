/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：提供构造Sql语句参数的方法.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 12:03:09
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.providers.sql.sqlobject;

/**
 * 提供构造Sql语句参数的方法
 */
public interface IParameterCreator {

    /**
     * 构造一个Sql语句参数
     *
     * @return Sql语句参数
     */
    DataParameter create();
}
