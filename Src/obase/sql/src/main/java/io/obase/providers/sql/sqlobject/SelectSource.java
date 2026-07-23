/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：子查询源.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 17:13:42
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Utils;
import io.obase.providers.sql.EDataSource;

import java.util.List;

/**
 * Select源，即用一条查询Sql语句作为源
 */
public class SelectSource extends MonomerSource {

    /**
     * 源的名称
     */
    private String name;

    /**
     * 查询Sql语句的对象表示法
     */
    private QuerySql querySql;


    /**
     * 创建Select源的实例，并指定其查询Sql语句
     *
     * @param querySql 查询Sql语句的对象表示法
     */
    public SelectSource(QuerySql querySql) {
        this.querySql = querySql;
    }

    /**
     * 创建Select源的实例，并指定其查询Sql语句和名称
     *
     * @param querySql 查询Sql语句的对象表示法
     * @param name     源的名称
     */
    public SelectSource(QuerySql querySql, String name) {
        this(querySql);
        this.name = name;
    }

    /**
     * 获取源的名称
     *
     * @return 源的名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置源的名称
     *
     * @param name 源的名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取查询Sql语句
     *
     * @return 查询Sql语句
     */
    public QuerySql getQuerySql() {
        return this.querySql;
    }

    /**
     * 设置查询Sql语句
     *
     * @param querySql 查询Sql语句
     */
    public void setQuerySql(QuerySql querySql) {
        this.querySql = querySql;
    }

    /**
     * 将当前查询源的排序规则提升为指定查询的排序规则
     *
     * @param query 指定的查询
     */
    @Override
    public void bubbleOrder(QuerySql query) {
        if (this.getQuerySql().getOrders().size() == 0 && this.getQuerySql().getSource().getCanBubbleOrder())
            this.getQuerySql().bubbleOrder();

        int i = 0;
        for (Order order : this.getQuerySql().getOrders()) {
            Expression orderExp = order.getExpression();
            ObjectReferencePack<String> alias = new ObjectReferencePack<>();
            if (!this.getQuerySql().getSelectionSet().contains(orderExp, alias) || (Utils.getStringIsEmpty(alias.realValue))) {
                alias.realValue = this.name + "_obaseOrderCol" + i;
                this.getQuerySql().getSelectionSet().add(orderExp, alias.realValue);
                i++;
            }

            Order bubbledOrder = new Order(this, alias.realValue, order.getDirection());
            query.getOrders().add(bubbledOrder);
        }

        if (this.getQuerySql().getTakeNumber() == 0)
            this.getQuerySql().clearOrder();
    }

    /**
     * 获取指代符，该指代符用于在Sql语句的其它部分引用源
     *
     * @return 指代符
     */
    @Override
    public String getSymbol() {
        return this.name;
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
     * 针对指定的数据源类型，生成数据源实例的字符串表示形式，该字符串可用于From子句、Update子句和Insert Into子句。
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {
        if (Utils.getStringIsEmpty(this.getSymbol()))
            return "(" + this.getQuerySql().toSql(sourceType) + ")";

        switch (sourceType) {
            case SqlServer: {
                return "(" + this.getQuerySql().toSql(sourceType) + ") [" + this.getSymbol() + "]";
            }
            case PostgreSql:
            case Oracle: {
                return "(" + this.getQuerySql().toSql(sourceType) + ") " + this.getSymbol() + "";
            }
            case MySql:
            case Sqlite: {
                return "(" + this.getQuerySql().toSql(sourceType) + ") `" + this.getSymbol() + "`";
            }
            default:
                throw new IllegalArgumentException("不支持的数据源类型: " + sourceType);
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
        if (Utils.getStringIsEmpty(this.getSymbol()))
            return "(" + this.getQuerySql().toSql(sourceType, sqlParameters, creator) + ")";
        switch (sourceType) {
            case SqlServer: {
                DataParameterSorter.sort(sqlParameters.realValue);
                return "(" + this.getQuerySql().toSql(sourceType, sqlParameters, creator) + ") [" + this.getSymbol() + "]";
            }
            case PostgreSql:
            case Oracle: {
                DataParameterSorter.sort(sqlParameters.realValue);
                return "(" + this.getQuerySql().toSql(sourceType, sqlParameters, creator) + ") \"" + this.getSymbol() + "\"";
            }
            case MySql:
            case Sqlite: {
                DataParameterSorter.sort(sqlParameters.realValue);
                return "(" + this.getQuerySql().toSql(sourceType, sqlParameters, creator) + ") `" + this.getSymbol() + "`";
            }
            default:
                throw new IllegalArgumentException("不支持的数据源类型: " + sourceType);
        }
    }

    /**
     * 为源的指代符设置前缀，设置前缀后源的指代符变更为该前缀串联原指代符。
     *
     * @param prefix 前缀
     */
    @Override
    public void setSymbolPrefix(String prefix) {
        //设置指代符前缀即在名称前加上前缀。
        if (this.name == null)
            this.name = prefix;
        this.name = prefix + this.name;
    }

    /**
     * 别称设为NULL
     */
    @Override
    public void resetSymbol() {
        //无需操作
    }

    /**
     * 清除别称
     */
    @Override
    public void clearSymbol() {
        //无需操作
    }
}
