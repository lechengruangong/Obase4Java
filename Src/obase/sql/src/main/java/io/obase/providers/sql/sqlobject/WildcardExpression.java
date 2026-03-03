/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示通配符表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 16:05:34
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.List;

/**
 * 表示通配符表达式。通配符表达式一般用作Count函数的参数
 */
public class WildcardExpression extends Expression {

    /**
     * 作为通配符作用范围的源
     */
    private final ISource source;

    /**
     * 将指定名称的源作为通配范围，创建表示通配符表达式的WildcardExpression的实例
     *
     * @param source 源名称
     */
    public WildcardExpression(String source) {
        this.source = new SimpleSource(source);
    }

    /**
     * 将指定的源作为通配范围，创建表示通配符表达式的WildcardExpression的实例
     *
     * @param source 源
     */
    public WildcardExpression(ISource source) {
        this.source = source;
    }

    /**
     * 获取设定通配范围的源
     *
     * @return 通配范围的源
     */
    public ISource getSource() {
        return this.source;
    }

    /**
     * 派生类实现此方法以判定具体类型的表达式对象是否相等
     *
     * @param other 要与当前表达式进行比较的表达式
     * @return 是否相等
     */
    @Override
    protected boolean concreteEquals(Expression other) {
        if (other instanceof WildcardExpression) {
            WildcardExpression wildcardExpressionOther = (WildcardExpression) other;
            return this.getSource().equals(wildcardExpressionOther.getSource());
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
        return this.source.toString(sourceType) + ".*";
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
        DataParameterSorter.sort(sqlParameters.realValue);
        return this.source.toString(sourceType, sqlParameters, creator) + ".*";
    }
}
