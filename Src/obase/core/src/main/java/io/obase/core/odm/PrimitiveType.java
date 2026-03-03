/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示基元类型.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 16:37:44
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * 表示基元类型
 */
public class PrimitiveType extends TypeBase {

    /**
     * 构造TypeBase实例
     *
     * @param clrType 运行时类型
     */
    private PrimitiveType(Class<?> clrType) {
        super(clrType);
        this.typeName.IsAssociation = false;
        this.typeName.IsEntity = false;
    }

    /**
     * 创建基元类型
     *
     * @param clrType 运行时类型
     * @return 基元类型
     */
    public static PrimitiveType fromType(Class<?> clrType) {
        return new PrimitiveType(clrType);
    }

    /**
     * 是否为obase定义的基元类型
     *
     * @param type 要判断的类型
     * @return 是否是obase定义的基元类型
     */
    public static boolean isObasePrimitive(Class<?> type) {
        //基础类型 + 十进制精确数 + 包装类型 + 各种时间 + UUID + 枚举
        return type.isPrimitive() || type == String.class || type == java.util.Date.class || type == LocalDateTime.class || type == LocalDate.class || type == LocalTime.class
                || type == Integer.class || type == Short.class || type == Long.class || type == Byte.class || type == Character.class || type == Double.class || type == Float.class
                || type == Boolean.class || type == BigDecimal.class || type == UUID.class || type.isEnum();
    }
}
