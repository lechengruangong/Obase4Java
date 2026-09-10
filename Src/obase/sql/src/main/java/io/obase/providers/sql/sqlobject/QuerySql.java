/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：查询Sql语句的对象化表示.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 17:33:45
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.common.SqlAliasCollector;
import io.obase.providers.sql.common.SqlAliasReplacer;
import io.obase.providers.sql.common.SqlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询Sql语句的对象化表示
 */
public class QuerySql extends SqlBase implements ISetOperand {

    /**
     * 聚合函数。默认值为None
     */
    private EAggregationFunction aggregation = EAggregationFunction.None;

    /**
     * 表示是否对结果集去重
     */
    private boolean distinct;

    /**
     * 分组子句
     */
    private GroupBy groupBy;

    /**
     * 结果集过滤子句
     */
    private Having having;

    /**
     * 排序规则
     */
    private List<Order> orders;

    /**
     * 投影集
     */
    private ISelectionSet selectionSet;

    /**
     * 指定跳过多少行
     */
    private int skipNumber;


    /**
     * 指定提取多少行
     * 注：
     * （1）同时设置_takeNumber和_distinct表示先执行去重操作再提取指定行数。
     * （2）仅对MySql和Oracle有效，其它数据源将忽略此属性。
     */
    private int takeNumber;

    /**
     * 创建查询Sql语句，指定查询源
     *
     * @param source 查询源
     */
    public QuerySql(ISource source) {
        super(source, ESqlType.Query);
        this.source = source;
    }

    /**
     * 创建查询Sql语句，指定查询源、筛选条件和排序字段。
     *
     * @param sourceName 源名
     * @param criteria   筛选条件
     * @param orderField 排序字段
     */
    public QuerySql(String sourceName, ICriteria criteria, String orderField) {
        this(sourceName, criteria);
        this.orders.add(new Order(orderField));
    }

    /**
     * 创建查询Sql语句，指定查询源和筛选条件
     *
     * @param sourceName 源名
     * @param criteria   筛选条件
     */
    public QuerySql(String sourceName, ICriteria criteria) {
        this(sourceName);
        this.setCriteria(criteria);
    }

    /**
     * 创建查询Sql语句，指定查询源、字段列表、筛选条件、排序字段和排序方向。
     *
     * @param sourceName     源名
     * @param fields         字段列表
     * @param criteria       筛选条件
     * @param orderField     排序字段
     * @param orderDirection 排序方向
     */
    public QuerySql(String sourceName, String[] fields, ICriteria criteria, String orderField, EOrderDirection orderDirection) {
        this(sourceName, fields, criteria);
        this.orders.add(new Order(this.getSource(), orderField, orderDirection));
    }

    /**
     * 创建查询Sql语句，指定查询源、字段列表、筛选条件和排序字段。
     *
     * @param sourceName 源名
     * @param fields     字段列表
     * @param criteria   筛选条件
     * @param orderField 排序字段
     */
    public QuerySql(String sourceName, String[] fields, ICriteria criteria, String orderField) {
        this(sourceName, fields, criteria);
        this.orders.add(new Order(orderField));
    }

    /**
     * 创建查询Sql语句，指定查询源、字段列表和筛选条件
     *
     * @param sourceName 源名
     * @param fields     字段列表
     * @param criteria   筛选条件
     */
    public QuerySql(String sourceName, String[] fields, ICriteria criteria) {
        this(sourceName, fields);
        this.setCriteria(criteria);
    }

    /**
     * 创建查询Sql语句，指定查询源和字段列表
     *
     * @param sourceName 查询源
     * @param fields     字段列表
     */
    public QuerySql(String sourceName, String[] fields) {
        this(sourceName);
        this.selectionSet = new FieldSet(this.getSource(), Arrays.stream(fields).collect(Collectors.toList()));
    }

    /**
     * 创建查询Sql语句，指定查询源、筛选条件、排序字段和排序方向。
     *
     * @param sourceName     源名
     * @param criteria       筛选条件
     * @param orderField     排序字段
     * @param orderDirection 排序方向
     */
    public QuerySql(String sourceName, ICriteria criteria, String orderField, EOrderDirection orderDirection) {
        this(sourceName, criteria);
        this.orders.add(new Order(this.getSource(), orderField, orderDirection));
    }

    /**
     * 创建查询Sql语句，目标字段属于多个源
     *
     * @param fieldSet 字段集数组
     * @param source   源名
     * @param criteria 筛选条件
     * @param orders   排序
     */
    public QuerySql(FieldSet fieldSet, ISource source, ICriteria criteria, Order... orders) {
        super(source, criteria, ESqlType.Query);
        this.selectionSet = fieldSet;
        this.source = source;
        this.setCriteria(criteria);
        if (orders != null)
            this.orders.addAll(Arrays.asList(orders));
    }

    /**
     * 创建查询Sql语句，目标字段属于多个源
     *
     * @param fieldSets 字段集数组
     * @param source    源名
     * @param criteria  筛选条件
     * @param orders    排序
     */
    public QuerySql(List<FieldSet> fieldSets, ISource source, ICriteria criteria, Order... orders) {
        super(source, criteria, ESqlType.Query);

        List<SelectionColumn> columns = new ArrayList<>();

        if (fieldSets != null && fieldSets.size() > 0) {
            for (FieldSet item : fieldSets) {
                if (item.getNames() == null || item.getNames().size() == 0) {
                    WildcardColumn tempWild = new WildcardColumn();
                    tempWild.setSource((MonomerSource) item.getSource());
                    columns.add(tempWild);
                    continue;
                }

                String[] names = item.getNames().toArray(new String[0]);
                String[] alias = item.getAliases().toArray(new String[0]);

                for (int i = 0; i < names.length; i++) {
                    ExpressionColumn col = new ExpressionColumn();
                    col.setAlias(alias[i]);
                    Field field = new Field(names[i]);
                    field.setSource((MonomerSource) item.getSource());
                    col.setExpression(Expression.field(field));
                    columns.add(col);
                }
            }
        }

        this.selectionSet = new SelectionSet(columns);
        this.source = source;
        this.setCriteria(criteria);
        if (orders != null)
            this.orders.addAll(Arrays.asList(orders));
    }

    /**
     * 创建查询Sql语句，指定查询源
     *
     * @param sourceName 源名
     */
    public QuerySql(String sourceName) {
        super(new SimpleSource(sourceName), ESqlType.Query);
    }

    /**
     * 获取投影集
     *
     * @return 投影集
     */
    public ISelectionSet getSelectionSet() {
        if (this.selectionSet == null)
            this.selectionSet = new SelectionSet();
        return this.selectionSet;
    }

    /**
     * 设置投影集
     *
     * @param selectionSet 投影集
     */
    public void setSelectionSet(ISelectionSet selectionSet) {
        this.selectionSet = selectionSet;
    }

    /**
     * 表示是否对结果集去重
     *
     * @return 是否对结果集去重
     */
    public boolean getDistinct() {
        return this.distinct;
    }

    /**
     * 表示是否对结果集去重
     *
     * @param distinct 是否对结果集去重
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * 获取排序规则
     *
     * @return 排序规则
     */
    public List<Order> getOrders() {
        if (this.orders == null)
            this.orders = new ArrayList<>();
        return this.orders;
    }

    /**
     * 设置排序规则
     *
     * @param orders 排序规则
     */
    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    /**
     * 获取一个值，该值指定跳过多少行。
     *
     * @return 跳过多少行
     */
    public int getSkipNumber() {
        return this.skipNumber;
    }

    /**
     * 设置一个值，该值指定跳过多少行
     *
     * @param skipNumber 跳过多少行
     */
    public void setSkipNumber(int skipNumber) {
        this.skipNumber = skipNumber;
    }

    /**
     * 获取聚合函数。默认值为None
     *
     * @return 聚合函数
     */
    public EAggregationFunction getAggregation() {
        return this.aggregation;
    }

    /**
     * 设置聚合函数
     *
     * @param aggregation 聚合函数
     */
    public void setAggregation(EAggregationFunction aggregation) {
        this.aggregation = aggregation;
    }

    /**
     * 指定提取多少行
     *
     * @return 提取多少行
     */
    public int getTakeNumber() {
        return this.takeNumber;
    }

    /**
     * 指定提取多少行
     *
     * @param takeNumber 提取多少行
     */
    public void setTakeNumber(int takeNumber) {
        this.takeNumber = takeNumber;
    }

    /**
     * 获取分组子句
     *
     * @return 分组子句
     */
    public GroupBy getGroupBy() {
        return this.groupBy;
    }

    /**
     * 设置分组子句
     *
     * @param groupBy 分组子句
     */
    public void setGroupBy(GroupBy groupBy) {
        this.groupBy = groupBy;
    }

    /**
     * 结果集过滤子句
     *
     * @return 集过滤子句
     */
    public Having getHaving() {
        return this.having;
    }

    /**
     * 结果集过滤子句
     *
     * @param having 集过滤子句
     */
    public void setHaving(Having having) {
        this.having = having;
    }

    /**
     * 针对指定的数据源类型，根据查询Sql语句的对象表示法生成Sql语句。
     * 生成后按别名映射字典将规则别名统一替换为短别名，以避免数据库因别名过长而截断。
     *
     * @param sourceType 数据源类型
     * @return Sql语句
     */
    @Override
    public String toSql(EDataSource sourceType) {
        String sql = renderSql(sourceType);
        return SqlAliasReplacer.replace(sql, SqlAliasCollector.collect(this));
    }

    /**
     * 生成Sql语句（未进行别名缩短）。
     *
     * @param sourceType 数据源类型
     * @return Sql语句
     */
    private String renderSql(EDataSource sourceType) {
        //判定是否为集源
        if (this.getSource() instanceof SetSource) {
            SetSource setSource = (SetSource) this.getSource();
            if (this.getTakeNumber() == 0 && !this.getDistinct() && this.getOrders().size() == 0 &&
                    this.getAggregation() == EAggregationFunction.None && this.getSelectionSet().getColumns().size() == 1 &&
                    this.getSelectionSet().getColumns().get(0) instanceof WildcardColumn)
                return setSource.getQuerySet().toSql(sourceType);
        }

        StringBuilder sqlStrBuilder;

        String isNullStr;
        switch (sourceType) {
            case SqlServer: {
                isNullStr = "isnull";
                break;
            }
            case PostgreSql: {
                isNullStr = "COALESCE";
                break;
            }
            case Oledb:
            case MySql:
            case Oracle:
            case Sqlite: {
                isNullStr = "ifnull";
                break;
            }
            default:
                throw new IllegalArgumentException("Unexpected value: " + sourceType);
        }

        //聚合函数
        switch (this.getAggregation()) {
            case None:
                break;
            case Average:
                sqlStrBuilder = new StringBuilder("select " + isNullStr + "(Avg(cast(" + this.getSelectionSet().getColumns().stream().map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) + " as decimal(10,2))),0) from " + this.getSource().toString(sourceType) + " ");
                if (this.getCriteria() != null)
                    sqlStrBuilder.append(" where").append(this.getCriteria().toString(sourceType)).append(" ");
                return sqlStrBuilder.toString();
            case Count:
                sqlStrBuilder = new StringBuilder("select count(1) from " + this.getSource().toString(sourceType) + " ");
                if (this.getCriteria() != null)
                    sqlStrBuilder.append(" where").append(this.getCriteria().toString(sourceType)).append(" ");
                return sqlStrBuilder.toString();
            case Max:
                sqlStrBuilder = new StringBuilder("select " + isNullStr + "(Max(" + this.getSelectionSet().getColumns().stream().map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) + " ),0) from " + this.getSource().toString(sourceType) + " ");
                if (this.getCriteria() != null)
                    sqlStrBuilder.append(" where").append(this.getCriteria().toString(sourceType)).append(" ");
                return sqlStrBuilder.toString();
            case Min:
                sqlStrBuilder = new StringBuilder("select " + isNullStr + "(Min(" + this.getSelectionSet().getColumns().stream().map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) + " ),0) from " + this.getSource().toString(sourceType) + " ");
                if (this.getCriteria() != null)
                    sqlStrBuilder.append(" where").append(this.getCriteria().toString(sourceType)).append(" ");
                return sqlStrBuilder.toString();
            case Sum:
                sqlStrBuilder = new StringBuilder("select " + isNullStr + "(Sum(" + this.getSelectionSet().getColumns().stream().map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) + " ),0) from " + this.getSource().toString(sourceType) + " ");
                if (this.getCriteria() != null)
                    sqlStrBuilder.append(" where").append(this.getCriteria().toString(sourceType)).append(" ");
                return sqlStrBuilder.toString();

            default:
                throw new IllegalArgumentException("未知的聚合类型: " + this.getAggregation());
        }

        sqlStrBuilder = new StringBuilder("select " + (this.getDistinct() ? "Distinct " : ""));

        switch (sourceType) {
            case SqlServer: {
                StringBuilder orderStringBuilder = new StringBuilder();
                //加入Take
                if (this.takeNumber > 0) sqlStrBuilder.append(" top ").append(this.takeNumber).append(" ");
                //Select部分
                if (this.getSelectionSet() != null && this.getSelectionSet().getColumns().size() > 0)
                    sqlStrBuilder.append(this.getSelectionSet().toString(sourceType));
                else
                    sqlStrBuilder.append("*");

                //From部分
                sqlStrBuilder.append(" from ").append(this.getSource().toString(sourceType)).append(" ");
                //Where部分
                if (this.getCriteria() != null)
                    sqlStrBuilder.append(" where ").append(this.getCriteria().toString(sourceType)).append(" ");
                //Group部分
                if (this.getGroupBy() != null)
                    sqlStrBuilder.append(this.getGroupBy().toString(sourceType)).append("  ");
                //Having部分
                if (this.getHaving() != null) sqlStrBuilder.append(this.getHaving().toString(sourceType)).append("  ");
                //Order部分
                if (this.getOrders() != null && this.getOrders().size() > 0) {
                    orderStringBuilder.append(" order by ");
                    List<Order> orders = SqlUtils.distinctOrders(this.getOrders());
                    for (int i = 0; i < orders.size(); i++) {
                        Order order = orders.get(i);
                        orderStringBuilder.append(i != orders.size() - 1
                                ? " " + order.getExpression().toString(sourceType) + "  " + order.getDirection() + ","
                                : " " + order.getExpression().toString(sourceType) + " " + order.getDirection());
                    }
                }

                //跳过(Skip)部分
                if (this.getSkipNumber() <= 0) {
                    sqlStrBuilder.append(orderStringBuilder.append(" "));
                } else {
                    sqlStrBuilder = new StringBuilder("select " + (this.getDistinct() ? "Distinct " : ""));
                    //加入Take
                    if (this.takeNumber > 0) sqlStrBuilder.append(" top ").append(this.takeNumber).append(" ");
                    sqlStrBuilder.append(" t.* ");
                    String selectStr = this.getSelectionSet() != null && this.getSelectionSet().getColumns().size() > 0
                            ? this.getSelectionSet().getColumns().stream().map(p -> p.toString(sourceType)).collect(Collectors.joining(","))
                            : "*";
                    String orderStr = this.getOrders() == null || this.getOrders().size() == 0
                            ? "1"
                            : this.getOrders().stream().map(s ->
                            " " + s.getField().toString(sourceType) + " " + s.getDirection() + " ").collect(Collectors.joining(","));
                    sqlStrBuilder.append(" from (select ").append(selectStr).append(",ROW_NUMBER() over(order by ").append(orderStr).append(" ) as rownum from ").append(this.getSource().toString(sourceType)).append(" ");
                    if (this.getCriteria() != null)
                        sqlStrBuilder.append(" where ").append(this.getCriteria().toString(sourceType)).append(" ");
                    sqlStrBuilder.append(" ) t where t.rownum > ").append(this.getSkipNumber());
                    if (this.orders != null && this.orders.size() > 0) sqlStrBuilder.append(" order by t.rownum asc");
                }

                break;
            }
            case MySql:
            case Oracle:
            case Sqlite: {
                StringBuilder orderStringBuilder = new StringBuilder();
                //Select部分
                if (this.getSelectionSet() != null && this.getSelectionSet().getColumns().size() > 0)
                    sqlStrBuilder.append(this.getSelectionSet().toString(sourceType));
                else
                    sqlStrBuilder.append("*");

                //From部分
                sqlStrBuilder.append("from ").append(this.getSource().toString(sourceType)).append("  ");
                //Where部分
                if (this.getCriteria() != null)
                    sqlStrBuilder.append("where ").append(this.getCriteria().toString(sourceType)).append("  ");
                //Group部分
                if (this.getGroupBy() != null)
                    sqlStrBuilder.append(this.getGroupBy().toString(sourceType)).append("  ");
                //Having部分
                if (this.getHaving() != null) sqlStrBuilder.append(this.getHaving().toString(sourceType)).append("  ");
                //Order部分
                if (this.getOrders() != null && this.getOrders().size() > 0) {
                    orderStringBuilder.append(" order  by ");
                    List<Order> orders = SqlUtils.distinctOrders(this.getOrders());
                    for (int i = 0; i < orders.size(); i++) {
                        Order order = orders.get(i);
                        orderStringBuilder.append(i != orders.size() - 1
                                ? " " + order.getExpression().toString(sourceType) + "  " + order.getDirection() + ","
                                : " " + order.getExpression().toString(sourceType) + "  " + order.getDirection());
                    }
                }

                sqlStrBuilder.append(orderStringBuilder.append(" "));
                //Limit Skip和Take部分
                if (this.takeNumber > 0) {
                    if (this.skipNumber >= 0)
                        sqlStrBuilder.append(" limit ").append(this.skipNumber).append(",").append(this.takeNumber);
                } else {
                    if (this.skipNumber > 0) sqlStrBuilder.append(" limit ").append(this.skipNumber);
                }

                break;
            }
            case PostgreSql: {
                StringBuilder orderStringBuilder = new StringBuilder();
                //Select部分
                if (this.getSelectionSet() != null && this.getSelectionSet().getColumns().size() > 0)
                    sqlStrBuilder.append(this.getSelectionSet().toString(sourceType));
                else
                    sqlStrBuilder.append("*");

                //From部分
                sqlStrBuilder.append("from ").append(this.getSource().toString(sourceType)).append(" ");
                //Where部分
                if (this.getCriteria() != null)
                    sqlStrBuilder.append("where ").append(this.getCriteria().toString(sourceType)).append(" ");
                //Group部分
                if (this.getGroupBy() != null)
                    sqlStrBuilder.append(this.getGroupBy().toString(sourceType)).append("  ");
                //Having部分
                if (this.getHaving() != null) sqlStrBuilder.append(this.getHaving().toString(sourceType)).append("  ");
                //Order部分
                if (this.getOrders() != null && this.getOrders().size() > 0) {
                    orderStringBuilder.append(" order by  ");
                    List<Order> orders = SqlUtils.distinctOrders(this.getOrders());
                    for (int i = 0; i < orders.size(); i++) {
                        Order order = orders.get(i);
                        orderStringBuilder.append(i != orders.size() - 1
                                ? " " + order.getExpression().toString(sourceType) + "   " + order.getDirection() + ","
                                : " " + order.getExpression().toString(sourceType) + "  " + order.getDirection());
                    }
                }

                sqlStrBuilder.append(orderStringBuilder.append(" "));
                //Limit Skip和Take部分
                if (this.takeNumber > 0) {
                    if (this.skipNumber >= 0)
                        sqlStrBuilder.append(" limit ").append(this.takeNumber).append(" OFFSET ").append(this.skipNumber);
                } else {
                    if (this.skipNumber > 0) sqlStrBuilder.append(" OFFSET ").append(this.skipNumber);
                }

                break;
            }
            default:
                throw new IllegalArgumentException("不支持的数据源: " + sourceType);
        }

        return sqlStrBuilder.toString();
    }

    /**
     * 使用参数化的方式 和 指定的数据源 将Sql对象表示为Sql字符串。
     * 生成后按别名映射字典将规则别名统一替换为短别名，以避免数据库因别名过长而截断。
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return Sql语句
     */
    @Override
    public String toSql(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        String sql = renderSql(sourceType, sqlParameters, creator);
        return SqlAliasReplacer.replace(sql, SqlAliasCollector.collect(this));
    }

    /**
     * 使用参数化的方式 和 指定的数据源 将Sql对象表示为Sql字符串（未进行别名缩短）。
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return Sql语句
     */
    private String renderSql(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        //判定是否为集源
        if (this.getSource() instanceof SetSource) {
            SetSource setSource = (SetSource) this.getSource();
            if (this.getTakeNumber() == 0 && !this.getDistinct() && this.getOrders().size() == 0 &&
                    this.getAggregation() == EAggregationFunction.None && this.getSelectionSet().getColumns().size() == 1 &&
                    this.getSelectionSet().getColumns().get(0) instanceof WildcardColumn)
                return setSource.getQuerySet().toSql(sourceType, sqlParameters, creator);
        }

        StringBuilder sqlStrBuilder;

        String isNullStr;
        switch (sourceType) {
            case SqlServer: {
                isNullStr = "isnull";
                break;
            }
            case PostgreSql: {
                isNullStr = "COALESCE";
                break;
            }
            case Oledb:
            case MySql:
            case Oracle:
            case Sqlite: {
                isNullStr = "ifnull";
                break;
            }
            default:
                throw new IllegalArgumentException("不支持的数据源: " + sourceType);
        }

        sqlParameters.realValue = new ArrayList<>();

        //聚合函数
        switch (this.getAggregation()) {
            case None:
                break;
            case Average:
                sqlStrBuilder = new StringBuilder("select " + isNullStr + "(Avg(cast(" + this.getSelectionSet().getColumns().stream().map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) + " as decimal(10,2))),0) from " + this.getSource().toString(sourceType) + " ");
                if (this.getCriteria() != null) {
                    ObjectReferencePack<List<DataParameter>> paras = new ObjectReferencePack<>();
                    sqlStrBuilder.append("  WHERE ").append(this.getCriteria().toString(sourceType, paras, creator)).append("   ");
                    sqlParameters.realValue.addAll(paras.realValue);
                }
                return sqlStrBuilder.toString();
            case Count:
                sqlStrBuilder = new StringBuilder("select count(1) from " + this.getSource().toString(sourceType) + " ");
                if (this.getCriteria() != null) {
                    ObjectReferencePack<List<DataParameter>> paras = new ObjectReferencePack<>();
                    sqlStrBuilder.append(" WHERE ").append(this.getCriteria().toString(sourceType, paras, creator)).append("  ");
                    sqlParameters.realValue.addAll(paras.realValue);
                }
                return sqlStrBuilder.toString();
            case Max:
                sqlStrBuilder = new StringBuilder("select " + isNullStr + "(Max(" + this.getSelectionSet().getColumns().stream().map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) + " ),0) from " + this.getSource().toString(sourceType) + " ");
                if (this.getCriteria() != null) {
                    ObjectReferencePack<List<DataParameter>> paras = new ObjectReferencePack<>();
                    sqlStrBuilder.append(" WHERE  ").append(this.getCriteria().toString(sourceType, paras, creator)).append(" ");
                    sqlParameters.realValue.addAll(paras.realValue);
                }
                return sqlStrBuilder.toString();
            case Min:
                sqlStrBuilder = new StringBuilder("select " + isNullStr + "(Min(" + this.getSelectionSet().getColumns().stream().map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) + " ),0) from " + this.getSource().toString(sourceType) + " ");
                if (this.getCriteria() != null) {
                    ObjectReferencePack<List<DataParameter>> paras = new ObjectReferencePack<>();
                    sqlStrBuilder.append("  WHERE ").append(this.getCriteria().toString(sourceType, paras, creator)).append(" ");
                    sqlParameters.realValue.addAll(paras.realValue);
                }
                return sqlStrBuilder.toString();
            case Sum:
                sqlStrBuilder = new StringBuilder("select " + isNullStr + "(Sum(" + this.getSelectionSet().getColumns().stream().map(p -> p.toString(sourceType)).collect(Collectors.joining(",")) + " ),0) from " + this.getSource().toString(sourceType) + " ");
                if (this.getCriteria() != null) {
                    ObjectReferencePack<List<DataParameter>> paras = new ObjectReferencePack<>();
                    sqlStrBuilder.append(" WHERE ").append(this.getCriteria().toString(sourceType, paras, creator)).append("   ");
                    sqlParameters.realValue.addAll(paras.realValue);
                }
                return sqlStrBuilder.toString();

            default:
                throw new IllegalArgumentException("未知的聚合类型: " + this.getAggregation());
        }

        sqlStrBuilder = new StringBuilder("select " + (this.getDistinct() ? "Distinct " : ""));

        switch (sourceType) {
            case SqlServer: {
                StringBuilder orderStringBuilder = new StringBuilder();
                //加入Take
                if (this.takeNumber > 0) sqlStrBuilder.append("  top ").append(this.takeNumber).append(" ");
                //Select部分
                if (this.getSelectionSet() == null || this.getSelectionSet().getColumns().size() == 0)
                    sqlStrBuilder.append("*");
                else
                    sqlStrBuilder.append(this.getSelectionSet().toString(sourceType));

                //From部分
                ObjectReferencePack<List<DataParameter>> paras = new ObjectReferencePack<>();
                sqlStrBuilder.append("  from ").append(this.getSource().toString(sourceType, paras, creator)).append(" ");
                sqlParameters.realValue.addAll(paras.realValue);
                //Where部分
                if (this.getCriteria() != null) {
                    ObjectReferencePack<List<DataParameter>> cParas = new ObjectReferencePack<>();
                    sqlStrBuilder.append("  WHERE ").append(this.getCriteria().toString(sourceType, cParas, creator)).append("  ");
                    sqlParameters.realValue.addAll(cParas.realValue);
                }
                //Group部分
                if (this.getGroupBy() != null)
                    sqlStrBuilder.append(this.getGroupBy().toString(sourceType)).append("  ");
                //Having部分
                if (this.getHaving() != null) sqlStrBuilder.append(this.getHaving().toString(sourceType)).append("   ");
                //Order部分
                if (this.getOrders() != null && this.getOrders().size() > 0) {
                    orderStringBuilder.append(" order  by ");
                    List<Order> orders = SqlUtils.distinctOrders(this.getOrders());
                    for (int i = 0; i < orders.size(); i++) {
                        Order order = orders.get(i);
                        orderStringBuilder.append(i != orders.size() - 1
                                ? "  " + order.getExpression().toString(sourceType) + " " + order.getDirection() + ","
                                : " " + order.getExpression().toString(sourceType) + " " + order.getDirection());
                    }
                }

                //跳过(Skip)部分
                if (this.getSkipNumber() <= 0) {
                    sqlStrBuilder.append(orderStringBuilder.append(" "));
                } else {
                    sqlStrBuilder = new StringBuilder("select  " + (this.getDistinct() ? "Distinct " : ""));
                    //加入Take
                    if (this.takeNumber > 0) sqlStrBuilder.append("  top ").append(this.takeNumber).append(" ");
                    sqlStrBuilder.append(" t.* ");
                    String selectStr = this.getSelectionSet() != null && this.getSelectionSet().getColumns().size() > 0
                            ? this.getSelectionSet().getColumns().stream().map(p -> p.toString(sourceType)).collect(Collectors.joining(","))
                            : "*";
                    String orderStr = this.getOrders() == null || this.getOrders().size() == 0
                            ? "1"
                            : this.getOrders().stream().map(s ->
                            " " + s.getField().toString(sourceType) + "  " + s.getDirection() + " ").collect(Collectors.joining(","));
                    sqlStrBuilder.append(" from (select ").append(selectStr).append(",ROW_NUMBER() over (order by ").append(orderStr).append(" ) as rownum from ").append(this.getSource().toString(sourceType)).append(" ");
                    if (this.getCriteria() != null) {
                        ObjectReferencePack<List<DataParameter>> cparas = new ObjectReferencePack<>();
                        sqlStrBuilder.append(" where").append(this.getCriteria().toString(sourceType, cparas, creator)).append(" ");
                        sqlParameters.realValue.addAll(cparas.realValue);
                    }
                    sqlStrBuilder.append(" ) t where t.rownum > ").append(this.getSkipNumber());
                    if (this.orders != null && this.orders.size() > 0) sqlStrBuilder.append(" order by t.rownum asc");
                }

                break;
            }
            case Oracle: {
                StringBuilder orderStringBuilder = new StringBuilder();
                //Select部分
                if (this.getSelectionSet() != null && this.getSelectionSet().getColumns().size() > 0)
                    sqlStrBuilder.append(this.getSelectionSet().toString(sourceType));
                else
                    sqlStrBuilder.append("* ");

                //From部分
                ObjectReferencePack<List<DataParameter>> paras = new ObjectReferencePack<>();
                sqlStrBuilder.append(",ROWNUM paging_rownumber from ").append(this.getSource().toString(sourceType, paras, creator)).append(" ");
                sqlParameters.realValue.addAll(paras.realValue);
                //Where部分
                if (this.getCriteria() != null) {
                    ObjectReferencePack<List<DataParameter>> cparas = new ObjectReferencePack<>();
                    sqlStrBuilder.append(" where   ").append(this.getCriteria().toString(sourceType, cparas, creator)).append(" ");
                    sqlParameters.realValue.addAll(cparas.realValue);
                }
                //Group部分
                if (this.getGroupBy() != null) sqlStrBuilder.append(this.getGroupBy().toString(sourceType)).append(" ");
                //Having部分
                if (this.getHaving() != null) sqlStrBuilder.append(this.getHaving().toString(sourceType)).append("  ");
                //Order部分
                if (this.getOrders() != null && this.getOrders().size() > 0) {
                    orderStringBuilder.append("  order by ");
                    List<Order> orders = SqlUtils.distinctOrders(this.getOrders());
                    for (int i = 0; i < orders.size(); i++) {
                        Order order = orders.get(i);
                        orderStringBuilder.append(i != orders.size() - 1
                                ? " " + order.getExpression().toString(sourceType) + " " + order.getDirection() + ","
                                : " " + order.getExpression().toString(sourceType) + "  " + order.getDirection());
                    }
                }

                sqlStrBuilder.append(orderStringBuilder.append(" "));
                //Limit Skip和Take部分
                if (this.takeNumber > 0) {
                    sqlStrBuilder.append(this.getCriteria() == null ? " Where " : " and ").append(" rownum <= ").append(this.skipNumber + this.takeNumber);
                    sqlStrBuilder = new StringBuilder("select * from (" + sqlStrBuilder + ") TP where TP.paging_rownumber > " + this.skipNumber);
                }

                break;
            }
            case MySql:
            case Sqlite: {

                //Select部分
                if (this.getSelectionSet() != null && this.getSelectionSet().getColumns().size() > 0)
                    sqlStrBuilder.append(this.getSelectionSet().toString(sourceType));
                else
                    sqlStrBuilder.append("*");
                StringBuilder orderStringBuilder = new StringBuilder();

                //From部分
                ObjectReferencePack<List<DataParameter>> paras = new ObjectReferencePack<>();
                sqlStrBuilder.append("  from ").append(this.getSource().toString(sourceType, paras, creator)).append(" ");
                sqlParameters.realValue.addAll(paras.realValue);
                //Where部分
                if (this.getCriteria() != null) {
                    ObjectReferencePack<List<DataParameter>> cparas = new ObjectReferencePack<>();
                    sqlStrBuilder.append(" where ").append(this.getCriteria().toString(sourceType, cparas, creator)).append(" ");
                    sqlParameters.realValue.addAll(cparas.realValue);
                }
                //Group部分
                if (this.getGroupBy() != null)
                    sqlStrBuilder.append(this.getGroupBy().toString(sourceType)).append("  ");
                //Having部分
                if (this.getHaving() != null) sqlStrBuilder.append(this.getHaving().toString(sourceType)).append(" ");
                //Order部分
                if (this.getOrders() != null && this.getOrders().size() > 0) {
                    orderStringBuilder.append(" order  by ");
                    List<Order> orders = SqlUtils.distinctOrders(this.getOrders());
                    for (int i = 0; i < orders.size(); i++) {
                        Order order = orders.get(i);
                        orderStringBuilder.append(i != orders.size() - 1
                                ? " " + order.getExpression().toString(sourceType) + "  " + order.getDirection() + ","
                                : "  " + order.getExpression().toString(sourceType) + " " + order.getDirection());
                    }
                }

                sqlStrBuilder.append(orderStringBuilder.append(" "));
                //Limit Skip和Take部分
                if (this.takeNumber > 0) {
                    if (this.skipNumber >= 0)
                        sqlStrBuilder.append("  limit ").append(this.skipNumber).append(",").append(this.takeNumber);
                } else {
                    if (this.skipNumber > 0) sqlStrBuilder.append(" limit ").append(this.skipNumber);
                }

                break;
            }
            case PostgreSql: {
                StringBuilder orderStringBuilder = new StringBuilder();
                //Select部分
                if (this.getSelectionSet() != null && this.getSelectionSet().getColumns().size() > 0)
                    sqlStrBuilder.append(this.getSelectionSet().toString(sourceType));
                else
                    sqlStrBuilder.append("*");

                //From部分
                ObjectReferencePack<List<DataParameter>> paras = new ObjectReferencePack<>();
                sqlStrBuilder.append(" from ").append(this.getSource().toString(sourceType, paras, creator)).append(" ");
                sqlParameters.realValue.addAll(paras.realValue);
                //Where部分
                if (this.getCriteria() != null) {
                    ObjectReferencePack<List<DataParameter>> cparas = new ObjectReferencePack<>();
                    sqlStrBuilder.append("where ").append(this.getCriteria().toString(sourceType, cparas, creator)).append(" ");
                    sqlParameters.realValue.addAll(cparas.realValue);
                }
                //Group部分
                if (this.getGroupBy() != null) sqlStrBuilder.append(this.getGroupBy().toString(sourceType)).append(" ");
                //Having部分
                if (this.getHaving() != null) sqlStrBuilder.append(this.getHaving().toString(sourceType)).append(" ");
                //Order部分
                if (this.getOrders() != null && this.getOrders().size() > 0) {
                    orderStringBuilder.append(" order by ");
                    List<Order> orders = SqlUtils.distinctOrders(this.getOrders());
                    for (int i = 0; i < orders.size(); i++) {
                        Order order = orders.get(i);
                        orderStringBuilder.append(i != orders.size() - 1
                                ? " " + order.getExpression().toString(sourceType) + " " + order.getDirection() + ","
                                : " " + order.getExpression().toString(sourceType) + " " + order.getDirection());
                    }
                }

                sqlStrBuilder.append(orderStringBuilder.append(" "));
                //Limit Skip和Take部分
                if (this.takeNumber > 0) {
                    if (this.skipNumber >= 0)
                        sqlStrBuilder.append(" limit  ").append(this.takeNumber).append(" OFFSET ").append(this.skipNumber);
                } else {
                    if (this.skipNumber > 0) sqlStrBuilder.append(" OFFSET ").append(this.skipNumber);
                }

                break;
            }

            default:
                throw new IllegalArgumentException("不支持的数据源: " + sourceType);
        }

        DataParameterSorter.sort(sqlParameters.realValue);
        return sqlStrBuilder.toString();
    }

    /**
     * 使用参数化的方式 和 默认的数据源 将Sql对象表示为Sql字符串
     *
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return Sql语句
     */
    @Override
    public String toSql(ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        return this.toSql(EDataSource.SqlServer, sqlParameters, creator);
    }

    /**
     * 清除所有排序规则
     */
    public void clearOrder() {
        this.getOrders().clear();
    }

    /**
     * 排序冒泡
     * 排序冒泡是指在不改变结果集顺序的条件下将查询源的排序规则提升为查询的排序规则。
     */
    public void bubbleOrder() {
        if (this.getGroupBy() != null) return;
        if (this.getOrders() == null || this.getOrders().size() == 0) {
            this.getSource().bubbleOrder(this);
        }
    }

    /**
     * 将查询结果集反序
     */
    public void reverse() {
        this.bubbleOrder();
        for (Order order : this.getOrders()) {
            switch (order.getDirection()) {
                case Desc:
                    order.setDirection(EOrderDirection.Asc);
                    break;
                case Asc:
                    order.setDirection(EOrderDirection.Desc);
                    break;
            }
        }
    }
}
