/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：结果读取器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 10:47:58
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Utils;
import io.obase.providers.sql.AliasGenerator;
import io.obase.providers.sql.EConnectionMode;
import io.obase.providers.sql.ISqlExecutor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 结果读取器。用于从结果集中读取一个元素（值或对象）
 *
 * @param <T> 值类型
 */
public abstract class ResultReader<T> implements Iterable<T> {

    /**
     * 数据读取器，负责从数据库读取数据
     */
    protected final ResultSet dataReader;
    /**
     * 泛型T的类型
     */
    protected final Class<T> type;
    /**
     * SQL执行器
     */
    private final ISqlExecutor sqlExecutor;
    /**
     * 别名生成器
     */
    protected AliasGenerator aliasGenerator = new AliasGenerator();
    /**
     * 映射字段生成器
     */
    protected TargetFieldGenerator targetFieldGenerator = new TargetFieldGenerator();

    /**
     * 构造ResultReader的新实例
     *
     * @param dataReader  数据读取器，负责从数据库读取数据。
     * @param type        泛型T的类型
     * @param sqlExecutor SQL执行器
     */
    protected ResultReader(ResultSet dataReader, Class<T> type, ISqlExecutor sqlExecutor) {
        this.dataReader = dataReader;
        this.sqlExecutor = sqlExecutor;
        this.aliasGenerator.setEnableCache(true);
        this.targetFieldGenerator.setEnableCache(true);
        this.type = type;
    }

    /**
     * 获取迭代器
     *
     * @return 迭代器
     */
    public Iterator<T> iterator() {
        //一般情况下使用List
        List<T> resultList = new ArrayList<>();
        return this.getIterator(resultList);
    }

    /**
     * 使用容器替换当前结果集 获取迭代器
     *
     * @param resultList 要使用的容器
     * @return 迭代器
     */
    protected Iterator<T> getIterator(Collection<T> resultList) {
        try {
            ObjectReferencePack<T> result = new ObjectReferencePack<>();
            while (this.read(result)) {
                resultList.add(result.realValue);
            }
        } finally {
            this.close();
        }
        return resultList.iterator();
    }

    /**
     * 从结果集读取下一个元素（值或对象）
     *
     * @param result 返回读取结果
     * @return 读取成功返回true，否则返回false
     */
    public abstract boolean read(ObjectReferencePack<T> result);

    /**
     * 关闭读取器
     */
    protected void close() {
        if (this.dataReader != null) {
            try {
                this.dataReader.close();
            } catch (SQLException e) {
                throw new RuntimeException("关闭数据读取器失败,请参考内部异常", e);
            } finally {
                //如果是执行模式 那就是由执行器开启的连接 此处需要关闭
                if (this.sqlExecutor != null && this.sqlExecutor.getConnectionMode() == EConnectionMode.Execution)
                    this.sqlExecutor.closeConnection();
            }
        }
    }

    /**
     * 将数据读取器移动到下一行
     *
     * @return 数据行
     */
    protected DataRow nextRow() {
        try {
            if (this.dataReader == null) {
                this.close();
                return null;
            }
            boolean closed = this.dataReader.isClosed();
            if (closed)
                return null;

            //没有数据的时候
            if (!this.dataReader.next()) {
                this.close();
                return null;
            }
            //数据行
            DataRow dataRow = new DataRow(this.aliasGenerator, this.targetFieldGenerator);
            ResultSetMetaData metaData = this.dataReader.getMetaData();
            //获取列数据
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String name = metaData.getColumnName(i);
                String label = metaData.getColumnLabel(i);
                Object obj = this.dataReader.getObject(i);

                if (!Utils.getStringIsEmpty(label))
                    dataRow.add(label, obj, i);
                else
                    dataRow.add(name, obj, i);
            }

            return dataRow;
        } catch (SQLException sqlException) {
            throw new RuntimeException("读取数据行失败, 请参照内部异常.", sqlException);
        }
    }
}
