/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示LIKE运算的表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 11:03:19
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 表示LIKE运算的表达式
 */
public class LikeExpression extends BinaryExpression {

    /**
     * 获取匹配模式
     */
    private final Expression pattern;

    /**
     * Like的类型
     */
    private final ELikeType likeType;

    /**
     * 创建LikeExpression的实例，并设置Left属性和Pattern属性的值
     *
     * @param left     左操作数
     * @param pattern  右操作数
     * @param likeType Like的类型
     */
    public LikeExpression(Expression left, Expression pattern, ELikeType likeType) {
        super(left, null);
        this.likeType = likeType;
        this.pattern = pattern;
    }

    /**
     * 获取匹配模式
     *
     * @return 匹配模式
     */
    public Expression getPattern() {
        return this.pattern;
    }

    /**
     * 派生类实现此方法以判定具体类型的表达式对象是否相等
     *
     * @param other 要与当前表达式进行比较的表达式
     * @return 是否相等
     */
    @Override
    protected boolean concreteEquals(Expression other) {
        if (other instanceof LikeExpression) {
            LikeExpression likeOther = (LikeExpression) other;
            return likeOther.getPattern() == this.getPattern();
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
        //模式匹配字符串 将单引号转义
        String patternStr = this.getPattern().toString(sourceType);

        String pattern;
        switch (sourceType) {
            case MySql:
            case PostgreSql:
            case SqlServer: {
                pattern = "'" + patternStr + "'";
                break;
            }
            case Oracle:
            case Sqlite:
                pattern = patternStr;
                break;
            default:
                throw new IllegalArgumentException("不支持的数据源类型: " + sourceType);
        }

        return this.getLeft().toString(sourceType) + " LIKE " + pattern;
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
        ObjectReferencePack<List<DataParameter>> patternSqlParameter = new ObjectReferencePack<>();

        //此处模式匹配仅为代号
        String patternStr = this.getPattern().toString(sourceType, patternSqlParameter, creator);

        String result = null;
        ObjectReferencePack<List<DataParameter>> leftSqlParameter = new ObjectReferencePack<>();
        switch (sourceType) {
            case SqlServer:
                //处理patternSqlParameter 此集合内仅一条
                Optional<DataParameter> pattenParaSqlServer = patternSqlParameter.realValue.stream().findFirst();
                if (pattenParaSqlServer.isPresent()) {
                    //匹配值 四种可能 前% 后% 全% 中间
                    String pattenValue = pattenParaSqlServer.get().Value.toString();
                    if (pattenValue.startsWith("%") && !pattenValue.endsWith("%")) {
                        pattenParaSqlServer.get().Value = pattenValue.substring(1);
                        result = this.getLeft().toString(sourceType, leftSqlParameter, creator) + "LIKE '%'+" + patternStr + "";
                    } else if (pattenValue.startsWith("%") && pattenValue.endsWith("%")) {
                        pattenParaSqlServer.get().Value = pattenValue.substring(1).substring(0, pattenValue.length() - 1);
                        result = this.getLeft().toString(sourceType, leftSqlParameter, creator) + "LIKE '%'+" + patternStr + "+'%'";

                    } else if (!pattenValue.startsWith("%") && pattenValue.endsWith("%")) {
                        pattenParaSqlServer.get().Value = pattenValue.substring(0, pattenValue.length() - 1);
                        result = this.getLeft().toString(sourceType, leftSqlParameter, creator) + "LIKE " + patternStr + "+'%'";
                    }
                } else {
                    //匹配值 四种可能 前% 后% 全% 中间
                    String realValuePlaceHolder = this.getLeft().toString(sourceType, leftSqlParameter, creator);

                    if (this.likeType == ELikeType.EndWith) {
                        result = realValuePlaceHolder + " LIKE '%' + " + patternStr + "";
                    } else if (this.likeType == ELikeType.Contains) {
                        result = realValuePlaceHolder + " LIKE '%' + " + patternStr + ",'%'";

                    } else if (this.likeType == ELikeType.StartWith) {
                        result = realValuePlaceHolder + " LIKE " + patternStr + "+ '%'";
                    }
                }
                break;
            case Oracle:
            case PostgreSql:
            case MySql: {
                //处理patternSqlParameter 此集合内仅一条
                Optional<DataParameter> pattenPara = patternSqlParameter.realValue.stream().findFirst();
                if (pattenPara.isPresent()) {
                    //匹配值 四种可能 前% 后% 全% 中间
                    String pattenValue = pattenPara.get().Value.toString();
                    if (pattenValue.startsWith("%") && !pattenValue.endsWith("%")) {
                        pattenPara.get().Value = pattenValue.substring(1);
                        result = this.getLeft().toString(sourceType, leftSqlParameter, creator) + "LIKE concat('%'," + patternStr + ")";
                    } else if (pattenValue.startsWith("%") && pattenValue.endsWith("%")) {
                        pattenPara.get().Value = pattenValue.substring(1).substring(0, pattenValue.length() - 1);
                        result = this.getLeft().toString(sourceType, leftSqlParameter, creator) + "LIKE concat('%'," + patternStr + ",'%')";

                    } else if (!pattenValue.startsWith("%") && pattenValue.endsWith("%")) {
                        pattenPara.get().Value = pattenValue.substring(0, pattenValue.length() - 1);
                        result = this.getLeft().toString(sourceType, leftSqlParameter, creator) + "LIKE concat(" + patternStr + ",'%')";
                    }
                } else {
                    //匹配值 四种可能 前% 后% 全% 中间
                    String realValuePlaceHolder = this.getLeft().toString(sourceType, leftSqlParameter, creator);

                    if (this.likeType == ELikeType.EndWith) {
                        result = realValuePlaceHolder + " LIKE concat('%'," + patternStr + ")";
                    } else if (this.likeType == ELikeType.Contains) {
                        result = realValuePlaceHolder + " LIKE concat('%'," + patternStr + ",'%')";

                    } else if (this.likeType == ELikeType.StartWith) {
                        result = realValuePlaceHolder + " LIKE concat(" + patternStr + ", '%')";
                    }
                }
                break;
            }
            case Sqlite: {
                //处理patternSqlParameter 此集合内仅一条
                Optional<DataParameter> pattenPara = patternSqlParameter.realValue.stream().findFirst();
                if (pattenPara.isPresent()) {
                    //匹配值 四种可能 前% 后% 全% 中间
                    result = this.getLeft().toString(sourceType, leftSqlParameter, creator) +
                            " LIKE " + patternStr;
                } else {
                    //匹配值 四种可能 前% 后% 全% 中间
                    String realValuePlaceHolder = this.getLeft().toString(sourceType, leftSqlParameter, creator);

                    result = realValuePlaceHolder +
                            " LIKE " + patternStr;
                }
                break;
            }
            default:
                throw new IllegalArgumentException("不支持的数据源类型: " + sourceType);
        }

        //参数列表
        sqlParameters.realValue = new ArrayList<>();
        sqlParameters.realValue.addAll(leftSqlParameter.realValue);
        sqlParameters.realValue.addAll(patternSqlParameter.realValue);

        DataParameterSorter.sort(sqlParameters.realValue);

        return result;
    }
}
