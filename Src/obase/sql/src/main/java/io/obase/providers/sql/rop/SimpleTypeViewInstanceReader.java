/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：简单视图实例读取器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 11:46:42
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.providers.sql.ISqlExecutor;

import java.sql.ResultSet;

/**
 * 简单视图实例读取器
 *
 * @param <T> 视图的类型
 */
public class SimpleTypeViewInstanceReader<T> extends ResultReader<T> {

    /**
     * 要读取其实例的视图
     */
    private final TypeView typeView;

    /**
     * 创建SimpleTypeViewInstanceReader实例
     *
     * @param typeView    要读取其实例的类型视图
     * @param dataReader  数据集阅读器
     * @param type        结果类型
     * @param sqlExecutor Sql执行器
     */
    public SimpleTypeViewInstanceReader(TypeView typeView, ResultSet dataReader, Class<T> type, ISqlExecutor sqlExecutor) {
        super(dataReader, type, sqlExecutor);
        this.typeView = typeView;
    }

    /**
     * 获取要读取其实例的视图
     *
     * @return 实例的视图
     */
    public TypeView getTypeView() {
        return this.typeView;
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

        result.realValue = (T) this.typeView.instantiate(simpleAttributeNode -> dataRow.getValue(simpleAttributeNode, null));

        return true;
    }
}
