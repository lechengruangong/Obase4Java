/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：值读取器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 11:35:12
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Utils;
import io.obase.providers.sql.ISqlExecutor;

import java.sql.ResultSet;

/**
 * 值读取器，负责从结果集读取值
 *
 * @param <T> 值类型
 */
public class ValueReader<T> extends ResultReader<T> {
    /**
     * 构造ValueReader的新实例
     *
     * @param dataReader  数据读取器，负责从数据库读取数据。
     * @param type        值类型
     * @param sqlExecutor Sql执行器
     */
    public ValueReader(ResultSet dataReader, Class<T> type, ISqlExecutor sqlExecutor) {
        super(dataReader, type, sqlExecutor);
    }

    /**
     * 从结果集读取下一个元素（值或对象）
     *
     * @param result 返回读取结果
     * @return 读取成功返回true，否则返回false
     */
    @Override
    public boolean read(ObjectReferencePack<T> result) {
        //读取数据行
        DataRow dataRow = this.nextRow();
        //为空 则返回默认值
        if (dataRow == null) {
            result.realValue = null;
            return false;
        }

        //读取第一行
        Object obj = dataRow.getValue(0);
        if (obj == null) {
            result.realValue = null;
            return true;
        }
        obj = Utils.convertDbValue(obj, this.type);
        result.realValue = (T) obj;
        return true;
    }
}
