/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示IN运算的表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 14:22:25
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表示IN运算的表达式
 */
public class InExpression extends BinaryExpression {

    /**
     * 广义IN运算符
     */
    private EInOperator operator;

    /**
     * IN运算的值域
     */
    private Iterable<?> valueSet;

    /**
     * 创建InExpression的实例，并设置Left属性、ValueSet属性和Operator属性的值。
     *
     * @param left      左操作数
     * @param valueSet  值域
     * @param eOperator 广义IN运算符
     */
    public InExpression(Expression left, Object[] valueSet, EInOperator eOperator) {
        super(left, null);

        this.operator = eOperator;
        this.valueSet = Arrays.asList(valueSet);
    }

    /**
     * 创建InExpression的实例，并设置Left属性、ValueSet属性和Operator属性的值
     *
     * @param left      左操作数
     * @param valueSet  值域
     * @param eOperator 广义IN运算符
     */
    public InExpression(Expression left, Iterable<?> valueSet, EInOperator eOperator) {
        super(left, null);

        this.operator = eOperator;
        this.valueSet = valueSet;
    }

    /**
     * 获取IN运算的值域
     *
     * @return IN运算的值域
     */
    public Object[] getValueSet() {
        List<Object> array = new ArrayList<>();
        for (Object obj : this.valueSet) {
            array.add(obj);
        }
        return array.toArray();
    }

    /**
     * 设置IN运算的值域
     *
     * @param valueSet IN运算的值域
     */
    public void setValueSet(Object[] valueSet) {
        this.valueSet = Arrays.asList(valueSet);
    }

    /**
     * 获取广义IN运算符
     *
     * @return 广义IN运算符
     */
    public EInOperator getOperator() {
        return this.operator;
    }

    /**
     * 派生类实现此方法以判定具体类型的表达式对象是否相等
     *
     * @param other 要与当前表达式进行比较的表达式
     * @return 是否相等
     */
    @Override
    protected boolean concreteEquals(Expression other) {
        if (other instanceof InExpression) {
            InExpression inOther = (InExpression) other;
            return this.getOperator() == inOther.getOperator() && Arrays.equals(this.getValueSet(), inOther.getValueSet());
        }
        return false;
    }

    /**
     * 翻转操作符
     */
    public void flipOverOperator() {
        switch (this.operator) {
            case IN:
                this.operator = EInOperator.NOTIN;
                break;
            case NOTIN:
                this.operator = EInOperator.IN;
                break;
            default:
                throw new IllegalArgumentException("未知的In操作符类型: " + this.operator);
        }
    }

    /**
     * 针对指定的数据源类型，返回表达式的文本表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType) {
        switch (this.operator) {
            case IN:
                return this.getLeft().toString(sourceType) + " IN (" + Arrays.stream(this.getValueSet()).map(Object::toString).collect(Collectors.joining(",")) + ")";
            case NOTIN:
                return this.getLeft().toString(sourceType) + " NOT IN (" + Arrays.stream(this.getValueSet()).map(Object::toString).collect(Collectors.joining(",")) + ")";
            default:
                throw new IllegalArgumentException("未知的In操作符类型: " + this.operator);
        }
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
        //每个部分的参数集合
        List<ObjectReferencePack<List<DataParameter>>> resultParameter = this.genBinaryDataParameter();
        ObjectReferencePack<List<DataParameter>> rightSqlParameter = resultParameter.get(1);
        ObjectReferencePack<List<DataParameter>> leftSqlParameter = resultParameter.get(0);
        //字符串
        String resultStr;
        //值字符串
        List<String> parameterStrList = new ArrayList<>();

        int i = 1;
        for (Object value : this.valueSet) {

            DataParameter dataParameter = creator.create();
            dataParameter.Index = i;
            dataParameter.Value = value;

            rightSqlParameter.realValue.add(dataParameter);
            parameterStrList.add("?");

            i++;
        }

        //没有值
        if (i == 1) {
            switch (this.operator) {
                case IN:
                    resultStr = "1<>1";
                    break;
                case NOTIN:
                    resultStr = "1=1";
                    break;
                default:
                    throw new IllegalArgumentException("未知的In操作符类型: " + this.operator);
            }

            sqlParameters.realValue = new ArrayList<>();
        } else {
            switch (this.operator) {
                case IN:
                    resultStr = this.getLeft().toString(sourceType, leftSqlParameter, creator) + " IN (" + parameterStrList.stream().map(Object::toString).collect(Collectors.joining(",")) + ")";
                    break;
                case NOTIN:
                    resultStr = this.getLeft().toString(sourceType, leftSqlParameter, creator) + " NOT IN (" + parameterStrList.stream().map(Object::toString).collect(Collectors.joining(",")) + ")";
                    break;
                default:
                    throw new IllegalArgumentException("未知的In操作符类型: " + this.operator);
            }

            sqlParameters.realValue = new ArrayList<>();
            sqlParameters.realValue.addAll(leftSqlParameter.realValue);
            sqlParameters.realValue.addAll(rightSqlParameter.realValue);

            DataParameterSorter.sort(sqlParameters.realValue);
        }

        return resultStr;
    }
}
