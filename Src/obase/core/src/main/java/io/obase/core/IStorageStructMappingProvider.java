/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：为存储结构映射提供程序定义规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 16:41:04
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.common.ObjectReferencePack;

/**
 * 为存储结构映射提供程序定义规范，该提供程序实现在存储服务（如数据库）中建立存储结构的系列方法。
 */
public interface IStorageStructMappingProvider {

    /**
     * 向指定的表追加字段
     *
     * @param tableName 表名
     * @param fields    要追加的字段
     */
    void appendField(String tableName, Field[] fields);

    /**
     * 索引一致性检查，确认表的既有索引与指定索引一致。
     *
     * @param tableName 表名
     * @param keyFields 标识属性
     * @return 既有索引是否与指定索引一致
     */
    boolean checkKey(String tableName, String[] keyFields);

    /**
     * 创建索引
     *
     * @param tableName 表名
     * @param fields    索引字段的名称序列
     */
    void createIndex(String tableName, String[] fields);

    /**
     * 创建表
     *
     * @param name      表名
     * @param fields    表的字段
     * @param keyFields 标识字段的名称序列
     */
    void createTable(String name, Field[] fields, String[] keyFields);

    /**
     * 扩大指定字段的长度
     *
     * @param tableName 表名
     * @param fields    要增加宽度的字段
     */
    void expandField(String tableName, Field[] fields);

    /**
     * 探测指定的字段是否已存在
     *
     * @param tableName   表名
     * @param fields      待检测的字段
     * @param lackOnes    返回缺少的字段
     * @param shorterOnes 返回长度不足的字段
     */
    void fieldExist(String tableName, Field[] fields, ObjectReferencePack<Field[]> lackOnes, ObjectReferencePack<Field[]> shorterOnes);

    /**
     * 探测指定的索引是否已存在
     *
     * @param tableName 表名
     * @param fields    索引字段的名称序列
     * @return 索引是否已存在
     */
    boolean[] indexExist(String tableName, String[] fields);

    /**
     * 探测指定的表是否已存在
     *
     * @param name 表名
     * @return 表是否已存在
     */
    boolean tableExist(String name);
}
