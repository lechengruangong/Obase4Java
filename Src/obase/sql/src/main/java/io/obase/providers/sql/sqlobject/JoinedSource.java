/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：连接查询源.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 17:23:09
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 连接查询源，即两个查询源通过Join运算得出的新源。
 */
public class JoinedSource implements ISource {

    /**
     * 连接条件
     */
    private final ICriteria joinCriteria;

    /**
     * 连接源列表
     */
    private final List<ISource> sources;

    /**
     * 连接方式，即左连接、内连接或右连接
     */
    private ESourceJoinType joinType;

    /**
     * 创建连接查询源的实例，该源由两个源通过内连接运算得到
     *
     * @param source1  第一个查询源
     * @param source2  第一个查询源
     * @param criteria 连接条件
     */
    public JoinedSource(ISource source1, ISource source2, ICriteria criteria) {
        this(source1, source2, criteria, ESourceJoinType.Inner);
    }

    /**
     * 创建连接查询源的实例
     *
     * @param source1  第一个查询源
     * @param source2  第二个查询源
     * @param criteria 连接条件
     * @param joinType 连接方式
     */
    public JoinedSource(ISource source1, ISource source2, ICriteria criteria, ESourceJoinType joinType) {
        this.sources = new ArrayList<>();
        this.sources.add(source1);
        this.sources.add(source2);
        this.joinType = joinType;
        this.joinCriteria = criteria;
    }

    /**
     * 获取连接方式，即左连接、内连接或右连接。
     *
     * @return 连接方式
     */
    public ESourceJoinType getJoinType() {
        return this.joinType;
    }

    /**
     * 获取连接运算包含的源列表。
     *
     * @return 源列表
     */
    public List<ISource> getSources() {
        return this.sources;
    }

    /**
     * 设置连接方式，即左连接、内连接或右连接
     *
     * @param joinType 连接方式
     */
    public void setJoinType(ESourceJoinType joinType) {
        this.joinType = joinType;
    }

    /**
     * 获取一个值，该值指示源是否支持排序冒泡
     *
     * @return 是否支持排序冒泡
     */
    @Override
    public boolean getCanBubbleOrder() {
        return false;
    }

    /**
     * 将当前源与另一源执行左连接运算，得出一个新源
     *
     * @param other    另一个源
     * @param criteria 连接条件
     * @return 左连接后的源
     */
    @Override
    public ISource leftJoin(ISource other, ICriteria criteria) {
        return new JoinedSource(this, other, criteria, ESourceJoinType.Left);
    }

    /**
     * 将当前源与另一源执行右连接运算，得出一个新源
     *
     * @param other    另一个源
     * @param criteria 连接条件
     * @return 右连接后的源
     */
    @Override
    public ISource rightJoin(ISource other, ICriteria criteria) {
        return new JoinedSource(this, other, criteria, ESourceJoinType.Right);
    }

    /**
     * 将当前源与另一源内执行连接运算，得出一个新源。
     *
     * @param other    另一个源
     * @param criteria 连接条件
     * @return 内连接后的源
     */
    @Override
    public ISource innerJoin(ISource other, ICriteria criteria) {
        return new JoinedSource(this, other, criteria);
    }

    /**
     * 将当前查询源的排序规则提升为指定查询的排序规则
     *
     * @param query 指定的查询
     */
    @Override
    public void bubbleOrder(QuerySql query) {
        throw new OrderBubblingUnSupportedException(this);
    }

    /**
     * 针对指定的数据源类型，生成数据源实例的字符串表示形式，该字符串可用于From子句、Update子句和Insert Into子句。
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {
        String joinTypeStr;
        switch (this.getJoinType()) {
            case Inner:
                joinTypeStr = " inner join ";
                break;
            case Left:
                joinTypeStr = " left join ";
                break;
            case Right:
                joinTypeStr = " right join ";
                break;
            default:
                throw new IllegalArgumentException("不支持的连接方法: " + this.getJoinType());
        }

        return this.sources.get(0).toString(sourceType) +
                joinTypeStr +
                this.sources.get(1).toString(sourceType) +
                " on " +
                this.joinCriteria.toString(sourceType);
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
        String joinTypeStr;
        switch (this.getJoinType()) {
            case Left:
                joinTypeStr = " left join ";
                break;
            case Right:
                joinTypeStr = " right join ";
                break;
            case Inner:
                joinTypeStr = " inner join ";
                break;
            default:
                throw new IllegalArgumentException("不支持的连接方法: " + this.getJoinType());
        }

        ObjectReferencePack<List<DataParameter>> leftDataParameters = new ObjectReferencePack<>();
        ObjectReferencePack<List<DataParameter>> rightDataParameters = new ObjectReferencePack<>();
        ObjectReferencePack<List<DataParameter>> criteriaDataParameters = new ObjectReferencePack<>();

        String resultBuilder = this.sources.get(0).toString(sourceType, leftDataParameters, creator) +
                joinTypeStr +
                this.sources.get(1).toString(sourceType, rightDataParameters, creator) +
                " on " +
                this.joinCriteria.toString(sourceType, criteriaDataParameters, creator);

        List<DataParameter> realSqlParameters = new ArrayList<>();
        realSqlParameters.addAll(leftDataParameters.realValue);
        realSqlParameters.addAll(rightDataParameters.realValue);
        realSqlParameters.addAll(criteriaDataParameters.realValue);


        sqlParameters.realValue = realSqlParameters;

        DataParameterSorter.sort(sqlParameters.realValue);

        return resultBuilder;
    }
}
