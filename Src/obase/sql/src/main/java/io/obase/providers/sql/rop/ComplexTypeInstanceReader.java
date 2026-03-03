/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：复杂类型实例读取器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 11:26:19
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.providers.sql.rop;

import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.ComplexType;
import io.obase.providers.sql.ISqlExecutor;

import java.sql.ResultSet;

/**
 * 复杂类型实例读取器
 *
 * @param <T> 复杂类型
 */
public class ComplexTypeInstanceReader<T> extends ResultReader<T> {

    /**
     * 要读取其实例的复杂类型
     */
    private final ComplexType complexType;

    /**
     * 构造ResultReader的新实例
     *
     * @param complexType 复杂类型
     * @param dataReader  数据读取器，负责从数据库读取数据。
     * @param type        泛型T的类型
     * @param executor    SQL执行器
     */
    public ComplexTypeInstanceReader(ComplexType complexType, ResultSet dataReader, Class<T> type, ISqlExecutor executor) {
        super(dataReader, type, executor);
        this.complexType = complexType;
    }

    /**
     * 获取要读取其实例的复杂类型
     *
     * @return 复杂类型
     */
    public ComplexType getComplexType() {
        return this.complexType;
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
        //没有 返回空
        if (dataRow == null) {
            result.realValue = null;
            return false;
        }

        result.realValue = (T) this.complexType.instantiate(simpleAttributeNode -> dataRow.getValue(simpleAttributeNode, null));

        return true;
    }
}
