/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示单体源.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 17:21:23
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.List;

/**
 * 表示单体源，即非由联接运算生成的源
 */
public abstract class MonomerSource implements ISource {

    /**
     * 获取指代符，该指代符用于在Sql语句的其它部分引用源
     *
     * @return 指代符
     */
    public abstract String getSymbol();

    /**
     * 获取一个值，该值指示源是否支持排序冒泡
     *
     * @return 是否支持排序冒泡
     */
    @Override
    public abstract boolean getCanBubbleOrder();

    /**
     * 将当前源与另一源执行左连接运算，得出一个新源
     *
     * @param other    另一个源
     * @param criteria 连接条件
     * @return 左连接后的源
     */
    @Override
    public ISource leftJoin(ISource other, ICriteria criteria) {
        if (other == null)
            return this;
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
        if (other == null)
            return this;
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
        if (other == null)
            return this;
        return new JoinedSource(this, other, criteria, ESourceJoinType.Inner);
    }

    /**
     * 针对指定的数据源类型，生成数据源实例的字符串表示形式，该字符串可用于From子句、Update子句和Insert Into子句。
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public abstract String toString(EDataSource sourceType);

    /**
     * 使用参数化的方式 默认的用途 和 指定的数据源 将Sql对象表示为Sql字符串
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return 字符串表示形式
     */
    @Override
    public abstract String toString(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator);

    /**
     * 为源的指代符设置前缀，设置前缀后源的指代符变更为该前缀串联原指代符。
     *
     * @param prefix 前缀
     */
    public abstract void setSymbolPrefix(String prefix);

    /**
     * 别称设为NULL
     */
    public abstract void resetSymbol();

    /**
     * 清除别称
     */
    public abstract void clearSymbol();
}
