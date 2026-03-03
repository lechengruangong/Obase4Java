/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：QuerySql或者ChangeSql的参数化视图.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-13 11:10:49
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.sqlobject.ChangeSql;
import io.obase.providers.sql.sqlobject.DataParameter;
import io.obase.providers.sql.sqlobject.MonomerSource;
import io.obase.providers.sql.sqlobject.QuerySql;

import java.util.*;
import java.util.stream.Collectors;

/**
 * QuerySql或者ChangeSql的参数化视图
 */
public class SqlParameterizedView {

    /**
     * Sql语句
     */
    private final String sqlString;

    /**
     * 查询的Sql语句所用的参数字典
     */
    private final Map<Integer, Object> parameters;

    /**
     * 初始化QuerySql或者ChangeSql的参数化视图
     *
     * @param sqlString  Sql语句
     * @param parameters 查询的Sql语句所用的参数字典
     */
    private SqlParameterizedView(String sqlString, Map<Integer, Object> parameters) {
        this.sqlString = sqlString;
        this.parameters = parameters;
    }

    /**
     * 获取QuerySql参数化视图
     *
     * @param querySql   查询用Sql
     * @param dataSource 查询源类型
     * @return 参数化视图
     */
    public static SqlParameterizedView getSqlParameterizedView(QuerySql querySql, EDataSource dataSource) {
        ObjectReferencePack<List<DataParameter>> parameterList = new ObjectReferencePack<>();
        parameterList.realValue = new ArrayList<>();
        String querySqlStr = querySql.toSql(dataSource, parameterList, new StandardParameterCreator());
        return getSqlParameters(parameterList, querySqlStr);
    }

    /**
     * 获取ChangeSql参数化视图
     *
     * @param changeSql  修改用Sql
     * @param dataSource 查询源类型
     * @return 参数化视图
     */
    public static SqlParameterizedView getSqlParameterizedView(ChangeSql changeSql, EDataSource dataSource) {
        ObjectReferencePack<List<DataParameter>> parameterList = new ObjectReferencePack<>();
        parameterList.realValue = new ArrayList<>();
        String changeSqlStr = changeSql.toSql(dataSource, parameterList, new StandardParameterCreator());
        if (changeSql.getSource() instanceof MonomerSource) {
            MonomerSource monomerSource = (MonomerSource) changeSql.getSource();
            //如果是空字符串 是因为调用了清除Symbol 此时应还原
            if (monomerSource.getSymbol() != null && monomerSource.getSymbol().isEmpty())
                monomerSource.resetSymbol();
        }
        return getSqlParameters(parameterList, changeSqlStr);
    }

    /**
     * 获取参数的具体值
     *
     * @param parameterList 参数列表
     * @param querySqlStr   参数字符串
     * @return 参数化视图
     */
    private static SqlParameterizedView getSqlParameters(ObjectReferencePack<List<DataParameter>> parameterList, String querySqlStr) {
        parameterList.realValue = parameterList.realValue.stream().sorted(Comparator.comparing(p -> p.Index)).collect(Collectors.toList());
        Map<Integer, Object> parameters = new HashMap<>();
        for (DataParameter dataParameter : parameterList.realValue) {
            parameters.put(dataParameter.Index, dataParameter.Value);
        }
        return new SqlParameterizedView(querySqlStr, parameters);
    }

    /**
     * 获取Sql语句
     *
     * @return Sql语句
     */
    public String getSqlString() {
        return this.sqlString;
    }

    /**
     * 获取Sql语句所用的参数字典
     *
     * @return 查询的Sql语句所用的参数字典
     */
    public Map<Integer, Object> getParameters() {
        return this.parameters;
    }

    /**
     * 获取Sql的简单表示形式
     *
     * @return 查询Sql的简单表示形式
     */
    public String getSimpleSqlString() {
        String result = this.sqlString;
        for (int key = 1; key <= this.parameters.size(); key++) {
            Object value = this.parameters.get(key);
            if (value instanceof String) {
                value = "'" + value + "'";
            }
            result = result.replaceFirst("[?]", value.toString());
        }
        return result;
    }

}
