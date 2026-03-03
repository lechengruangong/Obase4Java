/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Sql语句中的字段.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 17:13:38
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.core.common.Utils;
import io.obase.providers.sql.EDataSource;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 表示Sql语句中的字段
 */
public class Field {

    /**
     * 字段名称
     */
    private String name;

    /**
     * 字段的源
     */
    private MonomerSource source;

    /**
     * 构造字段
     *
     * @param name 字段名称
     */
    public Field(String name) {
        if (name.contains("*")) throw new IllegalArgumentException("列内不允许包含*号");
        this.name = name;
    }

    /**
     * 构造字段
     *
     * @param source 源
     * @param name   名称
     */
    public Field(String source, String name) {
        if (name.contains("*")) throw new IllegalArgumentException("列内不允许包含*号");
        if (!Utils.getStringIsEmpty(source))
            this.source = new SimpleSource(source);
        this.name = name;
    }

    /**
     * 构造字段
     *
     * @param source 源
     * @param name   名称
     */
    public Field(MonomerSource source, String name) {
        if (name.contains("*")) throw new IllegalArgumentException("列内不允许包含*号");
        this.source = source;
        this.name = name;
    }

    /**
     * 获取字段名称
     *
     * @return 字段名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 字段名称
     *
     * @param name 字段名称
     */
    public void setName(String name) {
        if (name.contains("*")) throw new IllegalArgumentException("列内不允许包含*号");
        this.name = name;
    }

    /**
     * 获取字段的源
     *
     * @return 字段的源
     */
    public MonomerSource getSource() {
        return this.source;
    }

    /**
     * 设置字段的源
     *
     * @param source 字段的源
     */
    public void setSource(MonomerSource source) {
        this.source = source;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return this.toString(EDataSource.SqlServer);
    }

    /**
     * 针对指定的数据源类型，返回字段的字符串表示形式。
     * 如果源为空，则返回字段名；否则返回“源名.字段名”。
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    public String toString(EDataSource sourceType) {
        switch (sourceType) {
            case SqlServer: {
                if (this.getSource() != null && !Utils.getStringIsEmpty(this.getSource().getSymbol()))
                    return "[" + this.getSource().getSymbol() + "]" + ".[" + this.name + "]";
                return "[" + this.name + "]";
            }
            case PostgreSql: {
                if (this.getSource() != null && !Utils.getStringIsEmpty(this.getSource().getSymbol())) {
                    if (this.name.contains("OTB")) {
                        //当使用OTB生成时 此处的字段不应使用限定符
                        return this.getSource().getSymbol() + "." + StringUtils.capitalize(this.name) + "";
                    } else {
                        return this.getSource().getSymbol() + ".\"" + StringUtils.capitalize(this.name) + "\"";
                    }
                }
                return "\"" + StringUtils.capitalize(this.name) + "\"";
            }
            case MySql:
            case Sqlite: {
                if (this.getSource() != null && !Utils.getStringIsEmpty(this.getSource().getSymbol()))
                    return "`" + this.getSource().getSymbol() + "`" + ".`" + this.name + "`";
                return "`" + this.name + "`";
            }
            case Oracle: {
                if (this.getSource() != null && !Utils.getStringIsEmpty(this.getSource().getSymbol()))
                    return this.getSource().getSymbol() + this.name;
                return this.name;
            }
            default:
                throw new IllegalArgumentException("不支持的数据源类型: " + sourceType);
        }
    }

    /**
     * 重写相等比较方法
     *
     * @param o 另一个对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return Objects.equals(this.name, field.name) && Objects.equals(this.source, field.source);
    }

    /**
     * 重写获取哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.source);
    }
}
