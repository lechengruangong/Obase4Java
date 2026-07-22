/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：简单查询源.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 17:35:07
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Utils;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 简单查询源，一般是表或者视图。
 */
public class SimpleSource extends MonomerSource {

    /**
     * 名称
     */
    private String name;

    /**
     * 指代符，该指代符用于在Sql语句的其它部分引用源
     */
    private String alias;

    /**
     * 排序顺序
     */
    private List<Order> storingOrder;

    /**
     * 创建简单查询源实例
     *
     * @param name 名称
     */
    public SimpleSource(String name) {
        this.name = name;
    }

    /**
     * 创建简单查询源实例
     *
     * @param name  名称
     * @param alias 别名
     */
    public SimpleSource(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    /**
     * 获取排序顺序
     *
     * @return 排序顺序
     */
    public List<Order> getStoringOrder() {
        return this.storingOrder;
    }

    /**
     * 设置排序顺序
     *
     * @param storingOrder 排序顺序
     */
    public void setStoringOrder(List<Order> storingOrder) {
        this.storingOrder = storingOrder;
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置名称
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取一个值，该值指示源是否支持排序冒泡
     *
     * @return 是否支持排序冒泡
     */
    @Override
    public boolean getCanBubbleOrder() {
        return true;
    }

    /**
     * 获取指代符，该指代符用于在Sql语句的其它部分引用源
     *
     * @return 指代符
     */
    @Override
    public String getSymbol() {
        if (this.alias == null) {
            return this.name;
        }
        return this.alias;
    }

    /**
     * 获取源的别名
     *
     * @return 源的别名
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * 将当前查询源的排序规则提升为指定查询的排序规则
     *
     * @param query 指定的查询
     */
    @Override
    public void bubbleOrder(QuerySql query) {
        if (query.getOrders() != null && query.getOrders().size() > 0)
            throw new RuntimeException("查询Sql语句已设置排序规则");
        for (Order order : this.getStoringOrder()) {
            if (query.getOrders() != null)
                query.getOrders().add(order);
        }
    }

    /**
     * 仅供Sqlite使用的无指代符ToSting 用于Delete语句
     *
     * @param sourceType 数据源类型
     * @return 无指代符的字符串
     */
    public String toNoSymbolString(EDataSource sourceType) {
        if (sourceType != EDataSource.Sqlite && sourceType != EDataSource.PostgreSql)
            throw new IllegalArgumentException("此方法仅供Sqlite和PostgreSQL使用");

        if (sourceType == EDataSource.Sqlite)
            return "`" + this.name + "`";
        else
            return "\"" + this.name + "\" ";
    }

    /**
     * 针对指定的数据源类型，生成数据源实例的字符串表示形式，该字符串可用于From子句、Update子句和Insert Into子句。
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {
        if (!Utils.getStringIsEmpty(this.getSymbol()))
            switch (sourceType) {
                case SqlServer: {
                    return "[" + this.name + "]  [" + this.getSymbol() + "]";
                }
                case PostgreSql: {
                    return "\"" + this.name + "\"  \"" + this.getSymbol() + "\"";
                }
                case Oracle: {
                    return this.name + "  " + this.getSymbol();
                }
                case MySql:
                case Sqlite: {
                    return "`" + this.name + "` `" + this.getSymbol() + "`";
                }
                default:
                    throw new IllegalArgumentException("不支持的数据源类型: " + sourceType);
            }

        switch (sourceType) {
            case SqlServer: {
                return "[" + this.name + "]";
            }
            case PostgreSql: {
                return "\"" + this.name + "\"";
            }
            case Oracle: {
                return this.name;
            }
            case MySql:
            case Sqlite: {
                return "`" + this.name + "`";
            }
            default: {
                throw new IllegalArgumentException("不支持的数据源类型: " + sourceType);
            }
        }
    }

    /**
     * 使用参数化的方式 默认的用途 和 指定的数据源 将Sql对象表示为Sql字符串
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        //简单源 无参数化
        sqlParameters.realValue = new ArrayList<>();
        return this.toString(sourceType);
    }

    /**
     * 为源的指代符设置前缀，设置前缀后源的指代符变更为该前缀串联原指代符。
     *
     * @param prefix 前缀
     */
    @Override
    public void setSymbolPrefix(String prefix) {
        //设置指代符前缀即在别名前加上前缀。
        if (this.alias == null)
            this.alias = prefix;
        else
            this.alias = prefix + this.alias;
    }

    /**
     * 别称设为NULL
     */
    @Override
    public void resetSymbol() {
        this.alias = null;
    }

    /**
     * 清除别称
     */
    @Override
    public void clearSymbol() {
        this.alias = "";
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
        SimpleSource that = (SimpleSource) o;
        return Objects.equals(this.name, that.name) && Objects.equals(this.alias, that.alias);
    }

    /**
     * 重写获取哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.alias, this.storingOrder);
    }
}
