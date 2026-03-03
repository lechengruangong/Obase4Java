/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Sql通用工具.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-17 15:03:56
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.common;

import io.obase.core.odm.*;
import io.obase.core.saving.ObjectSystemVisitor;
import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.sqlobject.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SqlUtils {

    /**
     * Mysql默认映射
     */
    private static final Map<Class<?>, String> MySqlValueTypeDictionary = new HashMap<>();

    /**
     * Sqlite默认映射
     */
    private static final Map<Class<?>, String> SqliteValueTypeDictionary = new HashMap<>();

    /**
     * SqlServer默认映射
     */
    private static final Map<Class<?>, String> SqlServerValueTypeDictionary = new HashMap<>();

    /**
     * PostgreSql默认映射
     */
    private static final Map<Class<?>, String> PostgreSqlValueTypeDictionary = new HashMap<>();

    static {
        //初始化一些内部使用的工具
        MySqlValueTypeDictionary.put(byte.class, "tinyint");
        MySqlValueTypeDictionary.put(Byte.class, "tinyint");
        MySqlValueTypeDictionary.put(short.class, "smallint");
        MySqlValueTypeDictionary.put(Short.class, "smallint");
        MySqlValueTypeDictionary.put(int.class, "int");
        MySqlValueTypeDictionary.put(Integer.class, "int");
        MySqlValueTypeDictionary.put(long.class, "bigint");
        MySqlValueTypeDictionary.put(Long.class, "bigint");
        MySqlValueTypeDictionary.put(char.class, "char");
        MySqlValueTypeDictionary.put(Character.class, "char");
        MySqlValueTypeDictionary.put(boolean.class, "tinyint");
        MySqlValueTypeDictionary.put(Boolean.class, "tinyint");
        MySqlValueTypeDictionary.put(float.class, "float");
        MySqlValueTypeDictionary.put(Float.class, "float");
        MySqlValueTypeDictionary.put(double.class, "double");
        MySqlValueTypeDictionary.put(Double.class, "double");
        MySqlValueTypeDictionary.put(BigDecimal.class, "decimal");
        MySqlValueTypeDictionary.put(String.class, "varchar");
        MySqlValueTypeDictionary.put(Date.class, "datetime");
        MySqlValueTypeDictionary.put(LocalDate.class, "datetime");
        MySqlValueTypeDictionary.put(LocalDateTime.class, "datetime");
        MySqlValueTypeDictionary.put(Timestamp.class, "datetime");
        MySqlValueTypeDictionary.put(LocalTime.class, "time");
        MySqlValueTypeDictionary.put(UUID.class, "varchar");

        SqliteValueTypeDictionary.put(byte.class, "INTEGER");
        SqliteValueTypeDictionary.put(Byte.class, "INTEGER");
        SqliteValueTypeDictionary.put(short.class, "INTEGER");
        SqliteValueTypeDictionary.put(Short.class, "INTEGER");
        SqliteValueTypeDictionary.put(int.class, "INTEGER");
        SqliteValueTypeDictionary.put(Integer.class, "INTEGER");
        SqliteValueTypeDictionary.put(long.class, "INTEGER");
        SqliteValueTypeDictionary.put(Long.class, "INTEGER");
        SqliteValueTypeDictionary.put(char.class, "INTEGER");
        SqliteValueTypeDictionary.put(Character.class, "INTEGER");
        SqliteValueTypeDictionary.put(boolean.class, "INTEGER");
        SqliteValueTypeDictionary.put(Boolean.class, "INTEGER");
        SqliteValueTypeDictionary.put(float.class, "REAL");
        SqliteValueTypeDictionary.put(Float.class, "REAL");
        SqliteValueTypeDictionary.put(double.class, "REAL");
        SqliteValueTypeDictionary.put(Double.class, "REAL");
        SqliteValueTypeDictionary.put(BigDecimal.class, "REAL");
        SqliteValueTypeDictionary.put(String.class, "TEXT");
        SqliteValueTypeDictionary.put(Date.class, "TEXT");
        SqliteValueTypeDictionary.put(LocalDate.class, "TEXT");
        SqliteValueTypeDictionary.put(LocalDateTime.class, "TEXT");
        SqliteValueTypeDictionary.put(Timestamp.class, "TEXT");
        SqliteValueTypeDictionary.put(LocalTime.class, "TEXT");
        SqliteValueTypeDictionary.put(UUID.class, "TEXT");

        SqlServerValueTypeDictionary.put(byte.class, "tinyint");
        SqlServerValueTypeDictionary.put(Byte.class, "tinyint");
        SqlServerValueTypeDictionary.put(short.class, "smallint");
        SqlServerValueTypeDictionary.put(Short.class, "smallint");
        SqlServerValueTypeDictionary.put(int.class, "int");
        SqlServerValueTypeDictionary.put(Integer.class, "int");
        SqlServerValueTypeDictionary.put(long.class, "bigint");
        SqlServerValueTypeDictionary.put(Long.class, "bigint");
        SqlServerValueTypeDictionary.put(char.class, "char");
        SqlServerValueTypeDictionary.put(Character.class, "char");
        SqlServerValueTypeDictionary.put(boolean.class, "tinyint");
        SqlServerValueTypeDictionary.put(Boolean.class, "tinyint");
        SqlServerValueTypeDictionary.put(float.class, "float");
        SqlServerValueTypeDictionary.put(Float.class, "float");
        SqlServerValueTypeDictionary.put(double.class, "real");
        SqlServerValueTypeDictionary.put(Double.class, "real");
        SqlServerValueTypeDictionary.put(BigDecimal.class, "decimal");
        SqlServerValueTypeDictionary.put(String.class, "nvarchar");
        SqlServerValueTypeDictionary.put(Date.class, "datetime");
        SqlServerValueTypeDictionary.put(LocalDate.class, "datetime");
        SqlServerValueTypeDictionary.put(LocalDateTime.class, "datetime");
        SqlServerValueTypeDictionary.put(Timestamp.class, "datetime");
        SqlServerValueTypeDictionary.put(LocalTime.class, "time");
        SqlServerValueTypeDictionary.put(UUID.class, "nvarchar");

        PostgreSqlValueTypeDictionary.put(byte.class, "character");
        PostgreSqlValueTypeDictionary.put(Byte.class, "character");
        PostgreSqlValueTypeDictionary.put(short.class, "smallint");
        PostgreSqlValueTypeDictionary.put(Short.class, "smallint");
        PostgreSqlValueTypeDictionary.put(int.class, "int");
        PostgreSqlValueTypeDictionary.put(Integer.class, "int");
        PostgreSqlValueTypeDictionary.put(long.class, "bigint");
        PostgreSqlValueTypeDictionary.put(Long.class, "bigint");
        PostgreSqlValueTypeDictionary.put(char.class, "character");
        PostgreSqlValueTypeDictionary.put(Character.class, "character");
        PostgreSqlValueTypeDictionary.put(boolean.class, "boolean");
        PostgreSqlValueTypeDictionary.put(Boolean.class, "boolean");
        PostgreSqlValueTypeDictionary.put(float.class, "real");
        PostgreSqlValueTypeDictionary.put(Float.class, "real");
        PostgreSqlValueTypeDictionary.put(double.class, "double precision");
        PostgreSqlValueTypeDictionary.put(Double.class, "double precision");
        PostgreSqlValueTypeDictionary.put(BigDecimal.class, "decimal");
        PostgreSqlValueTypeDictionary.put(String.class, "varchar");
        PostgreSqlValueTypeDictionary.put(Date.class, "timestamp");
        PostgreSqlValueTypeDictionary.put(LocalDate.class, "date");
        PostgreSqlValueTypeDictionary.put(LocalDateTime.class, "timestamp");
        PostgreSqlValueTypeDictionary.put(Timestamp.class, "timestamp");
        PostgreSqlValueTypeDictionary.put(LocalTime.class, "time");
        PostgreSqlValueTypeDictionary.put(UUID.class, "varchar");
    }

    /**
     * 获取MySql默认映射
     *
     * @param type 类型
     * @return 字段名称
     */
    public static String getMySqlDbType(Class<?> type) {
        //枚举 tinyint
        if (type.isEnum())
            return "tinyint";

        if (MySqlValueTypeDictionary.containsKey(type))
            return MySqlValueTypeDictionary.get(type);
        return "varchar";
    }

    /**
     * 获取Sqlite默认映射
     *
     * @param type 类型
     * @return 字段名称
     */
    public static String getSqliteDbType(Class<?> type) {
        //枚举 tinyint
        if (type.isEnum())
            return "INTEGER";

        if (SqliteValueTypeDictionary.containsKey(type))
            return SqliteValueTypeDictionary.get(type);
        return "varchar";
    }

    /**
     * 获取SqlServer默认映射
     *
     * @param type 类型
     * @return 字段名称
     */
    public static String getSqlServerDbType(Class<?> type) {
        //枚举 tinyint
        if (type.isEnum())
            return "tinyint";

        if (SqlServerValueTypeDictionary.containsKey(type))
            return SqlServerValueTypeDictionary.get(type);
        return "nvarchar";
    }

    /**
     * 获取PostgreSql默认映射
     *
     * @param type 类型
     * @return 字段名称
     */
    public static String getPostgreSqlDbType(Class<?> type) {
        //枚举 tinyint
        if (type.isEnum())
            return "smallint";

        if (PostgreSqlValueTypeDictionary.containsKey(type))
            return PostgreSqlValueTypeDictionary.get(type);
        return "varchar";
    }

    /**
     * 获取PostgreSql默认自增字段的映射
     *
     * @param type 类型
     * @return 字段名称
     */
    public static String getPostgreSqlAutoIncreaseDbType(Class<?> type) {
        if (type == short.class || type == Short.class) {
            return "SMALLSERIAL";
        }
        if (type == int.class || type == Integer.class) {
            return "SERIAL";
        }
        if (type == long.class || type == Long.class) {
            return "BIGSERIAL";
        }
        return "SERIAL";
    }

    /**
     * 根据属性类型创建设值器
     *
     * @param dataType   数据类型
     * @param fieId      字段名
     * @param value      值
     * @param isIncrease 是否是增量设值
     * @param source     源名称
     * @return 设值器
     */
    public static IFieldSetter getFieldSetter(Class<?> dataType, String fieId, Object value, boolean isIncrease, String source) {

        try {
            if (isIncrease) {
                Constructor<?> constructor = IncreaseSetter.class.getConstructor(String.class, String.class, Object.class);
                return (IFieldSetter) constructor.newInstance(source, fieId, value);
            }
            if (dataType == null)
                return new NullSetter(fieId);
            if (dataType == String.class)
                return new StringFieldSetter(source, fieId, value == null ? "" : value.toString());
            if (dataType == Character.class)
                return new CharFieldSetter(source, fieId, value == null ? '0' : value.toString().charAt(0));
            if (dataType == Boolean.class)
                return new BoolFieldSetter(source, fieId, Boolean.parseBoolean(value.toString()));
            if (dataType == Date.class) {
                Date date = (Date) value;
                return new DateTimeFieldSetter(source, fieId, date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            if (dataType == LocalDateTime.class) {
                LocalDateTime localDateTime = (LocalDateTime) value;
                return new DateTimeFieldSetter(source, fieId, localDateTime);
            }
            if (dataType == LocalDate.class) {
                LocalDate localDate = (LocalDate) value;
                return new DateTimeFieldSetter(source, fieId, localDate.atStartOfDay());
            }
            if (dataType == LocalTime.class) {
                LocalTime localTime = (LocalTime) value;
                return new TimeSpanFieldSetter(source, fieId, localTime);
            }
            if (dataType == Timestamp.class) {
                Timestamp timestamp = (Timestamp) value;
                return new DateTimeFieldSetter(source, fieId, timestamp.toLocalDateTime());
            }
            if (dataType == UUID.class) {
                UUID uuid = (UUID) value;
                return new UUIDFieldSetter(source, fieId, uuid);
            }
            if (dataType.isEnum()) {
                value = ((Enum<?>) value).ordinal();
                dataType = value.getClass();
            }

            if (PrimitiveType.isObasePrimitive(dataType)) {
                Constructor<?> constructor = NumericFieldSetter.class.getConstructor(String.class, String.class, Object.class);
                return (IFieldSetter) constructor.newInstance(source, fieId, value);
            }
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException("无法为此类型" + dataType.getName() + "设置设值器,请参照内部异常.", e);
        }


        throw new IllegalArgumentException("无法为此类型" + dataType.getName() + "设置设值器,请检查此属性的配置属性类型和取值器设值器.");
    }

    /**
     * 生成筛选条件：筛选单个实体对象或关联对象
     *
     * @param obj       目标对象
     * @param modelType 对象的模型类型
     * @return 条件
     */
    public static ICriteria generateCriteria(Object obj, StructuralType modelType) {
        if (modelType instanceof EntityType) {
            EntityType entityType = (EntityType) modelType;
            return generateCriteria(obj, entityType);
        }

        if (modelType instanceof AssociationType) {
            AssociationType associationType = (AssociationType) modelType;
            return generateCriteria(obj, associationType);
        }

        return null;
    }

    /**
     * 生成筛选条件：筛选单个实体对象
     *
     * @param entityObj  目标实体对象
     * @param entityType 对象的实体型
     * @return 条件
     */
    private static ICriteria generateCriteria(Object entityObj, EntityType entityType) {
        ICriteria result = null;

        for (String attr : entityType.getKeyAttributes()) {
            Attribute att = entityType.getAttribute(attr);
            Object value = ObjectSystemVisitor.getValue(entityObj, att);
            ICriteria segment = getCriteria(att.getDataType(), att.getTargetField(), value,
                    entityType.getTargetTable());

            result = result == null ? segment : result.and(segment);
        }

        return result;
    }

    /**
     * 生成筛选条件：筛选单个关联对象
     *
     * @param associationObj  目标关联对象
     * @param associationType 对象的关联型
     * @return 条件
     */
    private static ICriteria generateCriteria(Object associationObj, AssociationType associationType) {
        ICriteria result = null;
        for (AssociationEnd end : associationType.getAssociationEnds()) {
            for (AssociationEndMapping mapping : end.getMappings()) {
                Attribute attr = associationType.findAttributeByTargetField(mapping.getTargetField());
                Object value;
                ICriteria segment;

                if (attr != null) {
                    value = ObjectSystemVisitor.getValue(associationObj, attr);
                    segment = getCriteria(attr.getDataType(), mapping.getTargetField(), value,
                            associationType.getTargetTable());
                } else {
                    Object endObj = ObjectSystemVisitor.getValue(associationObj, end);
                    value = ObjectSystemVisitor.getValue(endObj, end.getEntityType(), mapping.getKeyAttribute());
                    segment = getCriteria(end.getEntityType().getAttribute(mapping.getKeyAttribute()).getDataType(),
                            mapping.getTargetField(), value, associationType.getTargetTable());
                }

                result = result == null ? segment : result.and(segment);
            }
        }

        return result;
    }

    /**
     * 根据属性类型创建条件
     *
     * @param dataType    字段类型
     * @param targetField 目标字段
     * @param value       值
     * @param source      源
     * @return 条件
     */
    private static ICriteria getCriteria(Class<?> dataType, String targetField, Object value, String source) {
        if (dataType == String.class)
            return new StringCriteria(source, targetField, ERelationOperator.Equal, value == null ? "" : value.toString());
        if (dataType == Character.class)
            return new CharCriteria(source, targetField, ERelationOperator.Equal, value == null ? '0' : value.toString().charAt(0));
        if (dataType == Boolean.class)
            return new BoolCriteria(source, targetField, ERelationOperator.Equal, Boolean.parseBoolean(value.toString()));
        if (dataType == Date.class) {
            Date date = (Date) value;
            return new DateTimeCriteria(source, targetField, ERelationOperator.Equal, date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (dataType == LocalDateTime.class) {
            LocalDateTime localDateTime = (LocalDateTime) value;
            return new DateTimeCriteria(source, targetField, ERelationOperator.Equal, localDateTime);
        }
        if (dataType == LocalDate.class) {
            LocalDate localDate = (LocalDate) value;
            return new DateTimeCriteria(source, targetField, ERelationOperator.Equal, LocalDateTime.parse(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        }
        if (dataType == LocalTime.class) {
            LocalTime localTime = (LocalTime) value;
            return new DateTimeCriteria(source, targetField, ERelationOperator.Equal, LocalDateTime.parse(localTime.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))));
        }
        if (dataType == Timestamp.class) {
            Timestamp timestamp = (Timestamp) value;
            return new DateTimeCriteria(source, targetField, ERelationOperator.Equal, timestamp.toLocalDateTime());
        }

        if (dataType.isEnum()) {
            value = ((Enum<?>) value).ordinal();
            dataType = value.getClass();
        }

        if (dataType.isPrimitive()) {
            try {
                Constructor<?> constructor = NumericCriteria.class.getConstructor(String.class, String.class, ERelationOperator.class, Object.class);
                return (ICriteria) constructor.newInstance(source, targetField, ERelationOperator.Equal, value);
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException |
                     NoSuchMethodException e) {
                throw new RuntimeException("无法创建条件,请参照内部异常.", e);
            }
        }

        throw new IllegalArgumentException("无法为此类型" + dataType.getName() + "创建条件,请检查此属性的配置属性类型和取值器设值器.");
    }

    /**
     * 排序字段去重
     *
     * @param orders 排序字段列表
     * @return 去重后的排序字段
     */
    public static List<Order> distinctOrders(List<Order> orders) {
        //如果存在同一个Field的仅保留一个
        HashSet<String> orderSet = new HashSet<>();
        List<Order> list = new ArrayList<>();
        for (Order order : orders) {
            String orderStr = order.toString(EDataSource.SqlServer).replace("Desc", "").replace("Asc", "");
            //使用HashSet去重
            if (orderSet.add(orderStr))
                list.add(order);
        }

        return list;
    }
}
