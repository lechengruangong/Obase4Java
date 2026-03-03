/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示具有常量值的表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 11:59:24
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 表示具有常量值的表达式
 */
public class ConstantExpression extends Expression {

    /**
     * 常量表达式的值
     */
    private final Object value;

    /**
     * 创建ConstantExpression的实例，并设置其Value属性值
     *
     * @param value 常量值
     */
    public ConstantExpression(Object value) {
        this.value = value;
    }

    /**
     * 获取常量表达式的值
     *
     * @return 常量表达式的值
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * 派生类实现此方法以判定具体类型的表达式对象是否相等
     *
     * @param other 要与当前表达式进行比较的表达式
     * @return 是否相等
     */
    @Override
    protected boolean concreteEquals(Expression other) {
        if (other instanceof ConstantExpression) {
            ConstantExpression constOther = (ConstantExpression) other;
            return constOther.getValue() == this.getValue();
        }
        return false;
    }

    /**
     * 针对指定的数据源类型，返回表达式的文本表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {
        if (this.value == null) return "NULL";

        if (this.value instanceof Date) {

            Date dateTime = (Date) this.value;
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            return ft.format(dateTime);
        } else if (this.value instanceof LocalDateTime) {
            LocalDateTime dateTime = (LocalDateTime) this.value;
            DateTimeFormatter ft = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            return ft.format(dateTime);
        } else if (this.value instanceof LocalDate) {
            LocalDate dateTime = (LocalDate) this.value;
            DateTimeFormatter ft = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return ft.format(dateTime);
        } else if (this.value instanceof LocalTime) {
            LocalTime dateTime = (LocalTime) this.value;
            DateTimeFormatter ft = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
            return ft.format(dateTime);
        } else if (this.value instanceof Boolean) {
            if (sourceType == EDataSource.SqlServer) {
                Boolean boolValue = (Boolean) this.value;
                return boolValue ? "1" : "0";
            } else {
                return this.value.toString();
            }
        } else if (this.value instanceof UUID) {
            UUID uuid = (UUID) this.value;
            return uuid.toString().toUpperCase();
        } else if (this.value.getClass().isEnum()) {
            return String.valueOf(((Enum<?>) this.value).ordinal());
        }


        return this.value.toString();
    }

    /**
     * 使用参数化的方式 和 指定的数据源 将表达式表示为字符串形式
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        //参数值
        sqlParameters.realValue = new ArrayList<>();

        DataParameter dataParameter = creator.create();
        if (this.value == null) {
            dataParameter.Value = null;
        } else {
            if (this.value instanceof Date) {
                Date dateTime = (Date) this.value;
                SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                if (sourceType == EDataSource.PostgreSql) {
                    dataParameter.Value = java.sql.Timestamp.valueOf(ft.format(dateTime));
                } else {
                    dataParameter.Value = ft.format(dateTime);
                }

            } else if (this.value instanceof LocalDateTime) {
                LocalDateTime dateTime = (LocalDateTime) this.value;
                DateTimeFormatter ft = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                if (sourceType == EDataSource.PostgreSql) {
                    dataParameter.Value = java.sql.Timestamp.valueOf(dateTime);
                } else {
                    dataParameter.Value = ft.format(dateTime);
                }
            } else if (this.value instanceof LocalDate) {
                LocalDate dateTime = (LocalDate) this.value;
                DateTimeFormatter ft = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                if (sourceType == EDataSource.PostgreSql) {
                    dataParameter.Value = java.sql.Date.valueOf(dateTime);
                } else {
                    dataParameter.Value = ft.format(dateTime);
                }
            } else if (this.value instanceof LocalTime) {
                LocalTime dateTime = (LocalTime) this.value;
                DateTimeFormatter ft = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
                if (sourceType == EDataSource.PostgreSql) {
                    dataParameter.Value = java.sql.Time.valueOf(dateTime);
                } else {
                    dataParameter.Value = ft.format(dateTime);
                }
            } else if (this.value instanceof Boolean) {
                if (sourceType == EDataSource.SqlServer) {
                    Boolean boolValue = (Boolean) this.value;
                    dataParameter.Value = boolValue ? 1 : 0;
                } else if (sourceType == EDataSource.PostgreSql) {
                    dataParameter.Value = this.value;
                } else {
                    dataParameter.Value = this.value;
                }
            } else if (this.value.getClass().isEnum()) {
                Enum<?>[] cons = (Enum<?>[]) this.value.getClass().getEnumConstants();
                for (Enum<?> con : cons) {
                    if (Objects.equals(con.ordinal(), this.value)) {
                        dataParameter.Value = con;
                    }
                }
            } else if (this.value instanceof UUID) {
                UUID uuid = (UUID) this.value;
                dataParameter.Value = uuid.toString().toUpperCase();
            } else {
                dataParameter.Value = this.value;
            }
        }

        sqlParameters.realValue.add(dataParameter);
        DataParameterSorter.sort(sqlParameters.realValue);

        return "?";
    }
}
