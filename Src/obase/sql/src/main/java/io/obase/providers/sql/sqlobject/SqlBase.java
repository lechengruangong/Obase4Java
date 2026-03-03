package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.List;

/**
 * 为查询Sql语句和修改Sql语句提供基础实现
 */
public abstract class SqlBase {

    /**
     * 表示源，用于生成From子句
     */
    protected ISource source;
    /**
     * 表示条件，用于生成Where子句。
     */
    private ICriteria criteria;
    /**
     * Sql语句的类型
     */
    private ESqlType sqlType;

    /**
     * 无参构造SqlBase实例
     */
    protected SqlBase() {
    }

    /**
     * 创建SqlBase实例，并设置其源和类型
     *
     * @param source  源
     * @param sqlType Sql类型
     */
    protected SqlBase(ISource source, ESqlType sqlType) {
        this.source = source;
        this.sqlType = sqlType;
    }

    /**
     * 创建SqlBase实例，并设置其源、条件和类型
     *
     * @param source   源
     * @param criteria 条件
     * @param sqlType  Sql类型
     */
    protected SqlBase(ISource source, ICriteria criteria, ESqlType sqlType) {
        this(source, sqlType);
        this.criteria = criteria;
    }

    /**
     * 获取源，用于生成From子句
     *
     * @return 源
     */
    public ISource getSource() {
        return this.source;
    }

    /**
     * 设置源，用于生成From子句
     *
     * @param source 源
     */
    public void setSource(ISource source) {
        this.source = source;
    }

    /**
     * 获取条件，用于生成Where子句
     *
     * @return 条件
     */
    public ICriteria getCriteria() {
        return this.criteria;
    }

    /**
     * 设置条件，用于生成Where子句
     *
     * @param criteria 条件，用于生成Where子句
     */
    public void setCriteria(ICriteria criteria) {
        this.criteria = criteria;
    }

    /**
     * 获取Sql语句的类型
     *
     * @return Sql语句的类型
     */
    public ESqlType getSqlType() {
        return this.sqlType;
    }

    /**
     * 设置Sql语句的类型
     *
     * @param sqlType Sql语句的类型
     */
    public void setSqlType(ESqlType sqlType) {
        this.sqlType = sqlType;
    }

    /**
     * 针对指定的数据源类型，根据查询Sql语句的对象表示法生成Sql语句
     *
     * @param sourceType 数据源类型
     * @return Sql语句
     */
    public abstract String toSql(EDataSource sourceType);

    /**
     * 使用参数化的方式 和 指定的数据源 将Sql对象表示为Sql字符串
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return Sql语句
     */
    public abstract String toSql(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator);

    /**
     * 使用参数化的方式 和 默认的数据源 将Sql对象表示为Sql字符串
     *
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return Sql语句
     */
    public abstract String toSql(ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator);
}
