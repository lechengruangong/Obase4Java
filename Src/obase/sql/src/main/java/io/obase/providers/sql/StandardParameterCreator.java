/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：标准的参数构造器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-13 11:09:33
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

import io.obase.providers.sql.sqlobject.DataParameter;
import io.obase.providers.sql.sqlobject.IParameterCreator;

/**
 * 标准的参数构造器
 */
public class StandardParameterCreator implements IParameterCreator {
    /**
     * 构造一个Sql语句参数
     *
     * @return Sql语句参数
     */
    @Override
    public DataParameter create() {
        return new DataParameter();
    }
}
