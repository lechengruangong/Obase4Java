/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：适用于Sql服务器的存储结构映射提供程序.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-13 11:23:33
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

import io.obase.common.ObjectReferencePack;
import io.obase.core.Field;
import io.obase.core.IStorageStructMappingProvider;
import io.obase.providers.sql.common.SqlUtils;
import io.obase.providers.sql.sqlobject.DataParameter;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 */
public class SqlStorageStructMappingProvider implements IStorageStructMappingProvider {

    /**
     * Sql执行器
     */
    private final ISqlExecutor executor;

    /**
     * 构造适用于Sql服务器的存储结构映射提供程序
     *
     * @param executor Sql执行器
     */
    public SqlStorageStructMappingProvider(ISqlExecutor executor) {
        if (executor == null)
            throw new IllegalArgumentException("存储结构映射的SQL执行器不可为空");
        this.executor = executor;
    }

    /**
     * 向指定的表追加字段
     *
     * @param tableName 表名
     * @param fields    要追加的字段
     */
    @Override
    public void appendField(String tableName, Field[] fields) {
        for (Field field : fields) {
            String sql;
            //是否可空
            boolean nullable = field.getNullable();
            switch (this.executor.getSourceType()) {
                case Oracle:
                case Oledb:
                case Other:
                    throw new IllegalArgumentException("结构映射暂不支持" + this.executor.getSourceType());
                case SqlServer:
                    sql = "ALTER TABLE [" + tableName + "] ADD [" + field.getName() + "] " + SqlUtils.getSqlServerDbType(field.getDataType().getClrType()) + " " + (nullable ? "NULL" : "NOT NULL");
                    //实际上只有字符串类型和decimal类型需要长度 其他类型的长度与具体可以存储的长度无关
                    ObjectReferencePack<String> sqlServerFieldText = new ObjectReferencePack<>();
                    if (this.isTypeNeedLength(field.getDataType().getClrType(), field, sqlServerFieldText)) {
                        sql = "ALTER TABLE [" + tableName + "] ADD [" + field.getName() + "] " + sqlServerFieldText.realValue + " " + (nullable ? "NULL" : "NOT NULL");
                    }
                    break;
                case Sqlite:
                    sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `" + field.getName() + "` " + SqlUtils.getSqliteDbType(field.getDataType().getClrType()) + " " + (nullable ? "NULL" : "NOT NULL");
                    break;
                case MySql:
                    sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `" + field.getName() + "` " + SqlUtils.getMySqlDbType(field.getDataType().getClrType()) + " " + (nullable ? "NULL" : "NOT NULL");
                    //实际上只有字符串类型和decimal类型需要长度  其他类型的长度与具体可以存储的长度无关
                    ObjectReferencePack<String> mysqlFieldText = new ObjectReferencePack<>();
                    if (this.isTypeNeedLength(field.getDataType().getClrType(), field, mysqlFieldText)) {
                        sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `" + field.getName() + "` " + mysqlFieldText.realValue + " " + (nullable ? "NULL" : "NOT NULL");
                    }
                    break;
                case PostgreSql:
                    sql = "ALTER TABLE \"" + tableName + "\" ADD \"" + field.getName() + "\" " + SqlUtils.getPostgreSqlDbType(field.getDataType().getClrType()) + " " + (nullable ? "DEFAULT  NULL" : "NOT NULL");
                    //实际上只有字符串类型和decimal类型需要长度  其他类型的长度与具体可以存储的长度无关
                    ObjectReferencePack<String> postgreSqlServerFieldText = new ObjectReferencePack<>();
                    if (this.isTypeNeedLength(field.getDataType().getClrType(), field, postgreSqlServerFieldText)) {
                        sql = "ALTER TABLE \"" + tableName + "\" ADD  \"" + field.getName() + "\" " + postgreSqlServerFieldText.realValue + " " + (nullable ? "DEFAULT NULL" : "NOT NULL");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("未知的数据源类型" + this.executor.getSourceType());
            }

            this.executor.executeScalarNoResult(sql, new DataParameter[0]);
        }
    }

    /**
     * 索引一致性检查，确认表的既有索引与指定索引一致。
     *
     * @param tableName 表名
     * @param keyFields 标识属性
     * @return 既有索引是否与指定索引一致
     */
    @Override
    public boolean checkKey(String tableName, String[] keyFields) {
        boolean[] results = this.indexExist(tableName, keyFields);
        for (boolean result : results) {
            if (!result)
                return false;
        }
        return true;
    }

    /**
     * 创建索引
     *
     * @param tableName 表名
     * @param fields    索引字段的名称序列
     */
    @Override
    public void createIndex(String tableName, String[] fields) {
        String sql;
        String name = "ogi_" + tableName + "_" + String.join("_", fields);
        if (name.length() > 64)
            name = name.substring(0, 64);

        switch (this.executor.getSourceType()) {
            case Oracle:
            case Oledb:
            case Other:
                throw new IllegalArgumentException("结构映射暂不支持" + this.executor.getSourceType());
            case SqlServer:
                sql = "CREATE INDEX " + name + " ON [" + tableName + "](" + Arrays.stream(fields).map(p -> "[" + p + "]").collect(Collectors.joining(",")) + ")";
                break;
            case Sqlite:
                sql = "CREATE INDEX '" + name + "' ON `" + tableName + "`(" + Arrays.stream(fields).map(p -> "`" + p + "`").collect(Collectors.joining(",")) + ")";
                break;
            case MySql:
                sql = "CREATE INDEX " + name + " ON `" + tableName + "`(" + Arrays.stream(fields).map(p -> "`" + p + "`").collect(Collectors.joining(",")) + ")";
                break;
            case PostgreSql:
                sql = "CREATE INDEX " + name + " ON \"" + tableName + "\"(" + Arrays.stream(fields).map(p -> "\"" + p + "\"").collect(Collectors.joining(",")) + ")";
                break;
            default:
                throw new IllegalArgumentException("未知的数据源类型" + this.executor.getSourceType());
        }

        this.executor.executeScalarNoResult(sql, new DataParameter[0]);
    }

    /**
     * 创建表
     *
     * @param name      表名
     * @param fields    表的字段
     * @param keyFields 标识字段的名称序列
     */
    @Override
    public void createTable(String name, Field[] fields, String[] keyFields) {
        StringBuilder sqlBuilder = new StringBuilder();
        StringBuilder sqliteBuilder = new StringBuilder();
        switch (this.executor.getSourceType()) {
            case Oracle:
            case Oledb:
            case Other:
                throw new IllegalArgumentException("结构映射暂不支持" + this.executor.getSourceType());
            case SqlServer: {
                sqlBuilder.append("CREATE TABLE [").append(name).append("](");
                //联合主键
                if (keyFields.length > 1) {
                    for (Field field : fields) {
                        //是否可空
                        boolean nullable = this.getNullable(keyFields, field);
                        //实际上只有字符串类型和decimal类型需要长度 其他类型的长度与具体可以存储的长度无关
                        ObjectReferencePack<String> mysqlFieldText = new ObjectReferencePack<>();
                        String filedText = this.isTypeNeedLength(field.getDataType().getClrType(), field, mysqlFieldText)
                                ? mysqlFieldText.realValue
                                : "[" + SqlUtils.getSqlServerDbType(field.getDataType().getClrType()) + "]";

                        if (Arrays.stream(keyFields).anyMatch(p -> p.equalsIgnoreCase(field.getName()))) {
                            sqlBuilder.append("[").append(field.getName()).append("] ").append(filedText);
                            if (field.getSelfIncreasing()) {
                                sqlBuilder.append("IDENTITY(1,1)");
                            }
                            sqlBuilder.append(nullable ? " NULL," : " NOT NULL,");
                        } else {
                            sqlBuilder.append("[").append(field.getName()).append("] ").append(filedText);
                            sqlBuilder.append(nullable ? " NULL," : " NOT NULL,");
                        }
                    }
                    sqlBuilder.append("PRIMARY KEY (").append(String.join(",", keyFields)).append(")");
                } else {
                    for (Field field : fields) {
                        //是否可空
                        boolean nullable = this.getNullable(keyFields, field);
                        //实际上只有字符串类型和decimal类型需要长度 其他类型的长度与具体可以存储的长度无关
                        ObjectReferencePack<String> mysqlFieldText = new ObjectReferencePack<>();
                        String filedText = this.isTypeNeedLength(field.getDataType().getClrType(), field, mysqlFieldText)
                                ? mysqlFieldText.realValue
                                : "[" + SqlUtils.getSqlServerDbType(field.getDataType().getClrType()) + "]";
                        if (Arrays.stream(keyFields).anyMatch(p -> p.equalsIgnoreCase(field.getName()))) {
                            sqlBuilder.append("[").append(field.getName()).append("] ").append(filedText);
                            sqlBuilder.append(" PRIMARY KEY ");
                            if (field.getSelfIncreasing()) {
                                sqlBuilder.append("IDENTITY(1,1)");
                            }
                            sqlBuilder.append(nullable ? " NULL," : " NOT NULL,");
                        } else {
                            sqlBuilder.append("[").append(field.getName()).append("] ").append(filedText);
                            sqlBuilder.append(nullable ? " NULL," : " NOT NULL,");
                        }
                    }
                    sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
                }

                sqlBuilder.append(")");
            }
            break;
            case Sqlite: {
                sqlBuilder.append("CREATE TABLE `").append(name).append("`(");
                //联合主键
                if (keyFields.length > 1) {
                    for (Field field : fields) {
                        //是否可空
                        boolean nullable = this.getNullable(keyFields, field);
                        if (Arrays.stream(keyFields).anyMatch(p -> p.equalsIgnoreCase(field.getName()))) {
                            sqlBuilder.append("`").append(field.getName()).append("` ").append(SqlUtils.getSqliteDbType(field.getDataType().getClrType()));
                            if (field.getSelfIncreasing()) {
                                sqlBuilder.append("AUTOINCREMENT");
                            }
                            sqlBuilder.append(nullable ? " NULL," : " NOT NULL,");
                        } else {
                            sqlBuilder.append("`").append(field.getName()).append("` ").append(SqlUtils.getSqliteDbType(field.getDataType().getClrType())).append(",");
                        }
                    }
                    sqlBuilder.append("PRIMARY KEY (").append(String.join(",", keyFields)).append(")");
                } else {
                    for (Field field : fields) {
                        //是否可空
                        boolean nullable = this.getNullable(keyFields, field);
                        if (Arrays.stream(keyFields).anyMatch(p -> p.equalsIgnoreCase(field.getName()))) {
                            sqlBuilder.append("`").append(field.getName()).append("` ").append(SqlUtils.getSqliteDbType(field.getDataType().getClrType()));
                            sqlBuilder.append(" PRIMARY KEY ");
                            if (field.getSelfIncreasing()) {
                                sqlBuilder.append(" AUTOINCREMENT ");
                            }
                            sqlBuilder.append(nullable ? " NULL," : " NOT NULL,");
                        } else {
                            sqlBuilder.append("`").append(field.getName()).append("` ").append(SqlUtils.getSqliteDbType(field.getDataType().getClrType())).append(",");
                        }
                    }
                    sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
                }
                sqlBuilder.append(");");
                sqliteBuilder.append("CREATE INDEX 'ogi_").append(name).append("_").append(String.join(",", keyFields)).append("' ON `").append(name).append("` (").append(String.join(",", keyFields)).append(")");
            }
            break;
            case MySql: {
                sqlBuilder.append("CREATE TABLE `").append(name).append("`(");
                for (Field field : fields) {
                    //是否可空
                    boolean nullable = this.getNullable(keyFields, field);
                    //实际上只有字符串类型和decimal类型需要长度 其他类型的长度与具体可以存储的长度无关
                    ObjectReferencePack<String> mysqlFieldText = new ObjectReferencePack<>();
                    String filedText = this.isTypeNeedLength(field.getDataType().getClrType(), field, mysqlFieldText)
                            ? mysqlFieldText.realValue
                            : SqlUtils.getMySqlDbType(field.getDataType().getClrType());
                    sqlBuilder.append("`").append(field.getName()).append("`");
                    sqlBuilder.append(filedText);
                    if (nullable) {
                        sqlBuilder.append(" DEFAULT  NULL");
                    } else {
                        sqlBuilder.append(" NOT NULL");
                    }
                    if (field.getSelfIncreasing()) {
                        sqlBuilder.append(" AUTO_INCREMENT");
                    }
                    sqlBuilder.append(",");
                }

                sqlBuilder.append("PRIMARY KEY(");
                for (Field field : fields) {
                    if (Arrays.stream(keyFields).anyMatch(p -> p.equalsIgnoreCase(field.getName()))) {
                        sqlBuilder.append("`").append(field.getName()).append("`,");
                    }
                }
                sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
                sqlBuilder.append(")");
                sqlBuilder.append(") ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;");
            }
            break;
            case PostgreSql: {
                sqlBuilder.append("CREATE TABLE \"").append(name).append("\"(");
                for (Field field : fields) {
                    //是否可空
                    boolean nullable = this.getNullable(keyFields, field);

                    sqlBuilder.append("\"").append(field.getName()).append("\"");

                    String filedText;
                    if (field.getSelfIncreasing()) {
                        filedText = SqlUtils.getPostgreSqlAutoIncreaseDbType(field.getDataType().getClrType());
                    } else {
                        //实际上只有字符串类型和decimal类型需要长度 其他类型的长度与具体可以存储的长度无关
                        ObjectReferencePack<String> postgresqlFieldText = new ObjectReferencePack<>();
                        filedText = this.isTypeNeedLength(field.getDataType().getClrType(), field, postgresqlFieldText)
                                ? postgresqlFieldText.realValue
                                : SqlUtils.getPostgreSqlDbType(field.getDataType().getClrType());
                    }
                    sqlBuilder.append(filedText);
                    if (nullable) {
                        sqlBuilder.append(" DEFAULT  NULL");
                    } else {
                        sqlBuilder.append(" NOT NULL");
                    }
                    sqlBuilder.append(",");
                }

                sqlBuilder.append("PRIMARY KEY(");
                for (Field field : fields) {
                    if (Arrays.stream(keyFields).anyMatch(p -> p.equalsIgnoreCase(field.getName()))) {
                        sqlBuilder.append("\"").append(field.getName()).append("\",");
                    }
                }
                sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
                sqlBuilder.append(")").append(")");
            }
            break;
            default:
                throw new IllegalArgumentException("未知的数据源类型" + this.executor.getSourceType());
        }

        this.executor.executeScalarNoResult(sqlBuilder.toString(), new DataParameter[0]);
        if (this.executor.getSourceType() == EDataSource.Sqlite) {
            this.executor.executeScalarNoResult(sqliteBuilder.toString(), new DataParameter[0]);
        }
    }

    /**
     * 扩大指定字段的长度
     *
     * @param tableName 表名
     * @param fields    要增加宽度的字段
     */
    @Override
    public void expandField(String tableName, Field[] fields) {
        //Sqlite 无字段长度
        if (this.executor.getSourceType() != EDataSource.Sqlite) {
            for (Field field : fields) {
                //实际上只有字符串类型需要拓展 其他类型的长度与具体可以存储的长度无关
                ObjectReferencePack<String> fieldText = new ObjectReferencePack<>();
                if (this.isTypeNeedLength(field.getDataType().getClrType(), field, fieldText)) {
                    String sql;
                    switch (this.executor.getSourceType()) {
                        case Oracle:
                        case Oledb:
                        case Other:
                            throw new IllegalArgumentException("结构映射暂不支持" + this.executor.getSourceType());
                        case SqlServer:
                            sql = "ALTER TABLE [" + tableName + "] ALTER COLUMN [" + field.getName() + "] " + fieldText.realValue;
                            break;
                        case MySql:
                            sql = "ALTER TABLE `" + tableName + "` MODIFY COLUMN `" + field.getName() + "` " + fieldText.realValue;
                            break;
                        case PostgreSql:
                            sql = "ALTER TABLE \"" + tableName + "\" MODIFY COLUMN \"" + field.getName() + "\" " + fieldText.realValue;
                            break;
                        default:
                            throw new IllegalArgumentException("未知的数据源类型" + this.executor.getSourceType());
                    }

                    this.executor.executeScalarNoResult(sql, new DataParameter[0]);
                }
            }
        }
    }

    /**
     * 探测指定的字段是否已存在
     *
     * @param tableName   表名
     * @param fields      待检测的字段
     * @param lackOnes    返回缺少的字段
     * @param shorterOnes 返回长度不足的字段
     */
    @Override
    public void fieldExist(String tableName, Field[] fields, ObjectReferencePack<Field[]> lackOnes, ObjectReferencePack<Field[]> shorterOnes) {
        List<Field> lack = new ArrayList<>();
        //List<Field> shorter = new ArrayList<>();

        for (Field field : fields) {
            String sql;
            switch (this.executor.getSourceType()) {
                case Oracle:
                case Oledb:
                case Other:
                    throw new IllegalArgumentException("结构映射暂不支持" + this.executor.getSourceType());
                case SqlServer:
                    sql = "SELECT TOP 1 [" + tableName + "].[" + field.getName() + "] FROM [" + tableName + "]";
                    break;
                case MySql:
                    sql = "SELECT `" + tableName + "`.`" + field.getName() + "` FROM `" + tableName + "` LIMIT 0,1";
                    break;
                case PostgreSql:
                    sql = "SELECT \"" + tableName + "\".\"" + field.getName() + "\" FROM \"" + tableName + "\" LIMIT 0 OFFSET 1";
                    break;
                case Sqlite:
                    sql = "SELECT  `" + tableName + "`.`" + field.getName() + "` FROM `" + tableName + "` LIMIT 0,1";
                    break;
                default:
                    throw new IllegalArgumentException("未知的数据源类型" + this.executor.getSourceType());
            }

            try (ResultSet reader = this.executor.executeReader(sql, new DataParameter[0])) {
                reader.next();//字段长度无法查询
                //shorter.Add(filed)
            } catch (Exception ignore) {
                lack.add(field);
            } finally {
                this.executor.closeConnection();
            }
        }

        lackOnes.realValue = lack.toArray(new Field[0]);
        shorterOnes.realValue = new Field[0];
    }

    /**
     * 探测指定的索引是否已存在
     *
     * @param tableName 表名
     * @param fields    索引字段的名称序列
     * @return 索引是否已存在
     */
    @Override
    public boolean[] indexExist(String tableName, String[] fields) {
        boolean[] result = new boolean[fields.length];
        int i = 0;
        for (String field : fields) {
            String sql;
            switch (this.executor.getSourceType()) {
                case Oracle:
                case Oledb:
                case Other:
                    throw new IllegalArgumentException("结构映射暂不支持" + this.executor.getSourceType());
                case SqlServer:
                    sql = "sp_helpindex '" + tableName + "'";
                    break;
                case MySql:
                    sql = "SHOW INDEX FROM `" + tableName + "` WHERE column_name = '" + field + "'";
                    break;
                case PostgreSql:
                    sql = "Select indexdef FROM pg_indexes Where  tablename = '" + tableName + "'";
                    break;
                case Sqlite:
                    sql = "select * From sqlite_master where type = 'index' and tbl_name like '" + tableName + "' and sql like '%" + field + "%'";
                    break;
                default:
                    throw new IllegalArgumentException("未知的数据源类型" + this.executor.getSourceType());
            }

            try (ResultSet reader = this.executor.executeReader(sql, new DataParameter[0])) {
                if (this.executor.getSourceType() == EDataSource.SqlServer) {
                    while (reader.next()) {
                        Object key = reader.getObject("index_keys");
                        if (key != null && key.toString().toLowerCase().contains(field.toLowerCase()))
                            result[i] = true;
                    }
                } else if (this.executor.getSourceType() == EDataSource.PostgreSql) {
                    while (reader.next()) {
                        Object key = reader.getObject("indexdef");
                        if (key != null && key.toString().toLowerCase().contains(field.toLowerCase()))
                            result[i] = true;
                    }
                } else {
                    if (reader.next())
                        result[i] = true;
                }
            } catch (SQLException e) {
                throw new RuntimeException("执行探测指定的索引是否已存在错误,请参照内部异常.", e);
            } finally {
                this.executor.closeConnection();
            }

            i++;
        }

        return result;
    }

    /**
     * 探测指定的表是否已存在
     *
     * @param name 表名
     * @return 表是否已存在
     */
    @Override
    public boolean tableExist(String name) {
        String sql;
        switch (this.executor.getSourceType()) {
            case Oracle:
            case Oledb:
            case Other:
                throw new IllegalArgumentException("结构映射暂不支持" + this.executor.getSourceType());
            case SqlServer:
                sql = "SELECT TOP 1 1 FROM [" + name + "]";
                break;
            case PostgreSql:
                sql = "SELECT 1 FROM \"" + name + "\"  LIMIT 0 OFFSET 1";
                break;
            case MySql:
                sql = "SELECT 1 FROM `" + name + "`  LIMIT 0,1";
                break;
            case Sqlite:
                sql = "SELECT 1 FROM `" + name + "` LIMIT 0,1";
                break;
            default:
                throw new IllegalArgumentException("未知的数据源类型" + this.executor.getSourceType());
        }

        try (ResultSet ignored1 = this.executor.executeReader(sql, new DataParameter[0])) {
            return true;
        } catch (Exception ignored) {
            return false;
        } finally {
            this.executor.closeConnection();
        }
    }

    /**
     * 是否可空
     *
     * @param keyFields 主键
     * @param field     当前字段
     * @return 是否可空
     */
    private boolean getNullable(String[] keyFields, Field field) {
        //是否可空
        boolean nullable = field.getNullable();

        //如果是主键 必然不可空
        if (Arrays.stream(keyFields).map(String::toLowerCase).collect(Collectors.toList()).contains(field.getName().toLowerCase()))
            nullable = false;

        return nullable;
    }

    /**
     * 判断类型是否需要长度
     * 目前仅有映射为string的类型 和 decimal类型 需要长度
     *
     * @param type      类型
     * @param field     字段
     * @param fieldText 字段类型和长度文本
     * @return 是否需要长度
     */
    private boolean isTypeNeedLength(Class<?> type, Field field, ObjectReferencePack<String> fieldText) {
        EDataSource dataSource = this.executor.getSourceType();
        //字符串类型
        if (type == String.class || type == UUID.class) {
            int length = field.getLength() / 8 == 0 ? 40 : field.getLength() / 8;
            switch (dataSource) {
                case Oracle:
                case Other:
                case Oledb:
                    throw new IllegalArgumentException("结构映射暂不支持" + dataSource);
                case MySql:
                    fieldText.realValue = length > 255
                            ? "Text"
                            : SqlUtils.getMySqlDbType(field.getDataType().getClrType()) + "(" + length + ")";
                    break;
                case PostgreSql:
                    fieldText.realValue = length > 255
                            ? "Text"
                            : SqlUtils.getPostgreSqlDbType(field.getDataType().getClrType()) + "(" + length + ")";
                    break;
                case Sqlite:
                    fieldText.realValue = SqlUtils.getSqliteDbType(field.getDataType().getClrType());
                    break;
                case SqlServer:
                    fieldText.realValue = length > 500
                            ? "[Text]"
                            : SqlUtils.getSqlServerDbType(field.getDataType().getClrType()) + "(" + length + ")";
                    break;
                default:
                    throw new IllegalArgumentException("未知的数据源类型");
            }

            return true;
        }

        //十进制数类型
        if (type == BigDecimal.class) {
            byte precision = field.getPrecision();
            switch (dataSource) {
                case Oracle:
                case Other:
                case Oledb:
                    throw new IllegalArgumentException("结构映射暂不支持" + dataSource);
                case MySql:
                    fieldText.realValue = SqlUtils.getMySqlDbType(field.getDataType().getClrType()) + "(65," + precision + ")";
                    break;
                case PostgreSql:
                    fieldText.realValue = SqlUtils.getPostgreSqlDbType(field.getDataType().getClrType()) + "(65," + precision + ")";
                    break;
                case Sqlite:
                    fieldText.realValue = SqlUtils.getSqliteDbType(field.getDataType().getClrType());
                    break;
                case SqlServer:
                    fieldText.realValue = SqlUtils.getSqlServerDbType(field.getDataType().getClrType()) + "(38," + precision + ")";
                    break;
                default:
                    throw new IllegalArgumentException("未知的数据源类型");
            }

            return true;
        }

        //char类型
        if (type == byte.class || type == Byte.class || type == char.class || type == Character.class) {
            int length = field.getLength() / 8 == 0 ? 2 : field.getLength() / 8;
            switch (dataSource) {
                case Oracle:
                case Other:
                case Oledb:
                    throw new IllegalArgumentException("结构映射暂不支持" + dataSource);
                case PostgreSql:
                    fieldText.realValue = SqlUtils.getPostgreSqlDbType(field.getDataType().getClrType()) + "(" + length + ")";
                    return true;
                case MySql:
                case Sqlite:
                case SqlServer:
                    return false;
                default:
                    throw new IllegalArgumentException("未知的数据源类型");
            }
        }

        fieldText.realValue = "";
        return false;
    }
}
