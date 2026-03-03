/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示一个字段的表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 17:12:26
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;


import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示一个字段的表达式
 */
public class FieldExpression extends Expression {

    /**
     * 字段表达式所表示的字段
     */
    private final Field field;

    /**
     * 创建FieldExpression的实例，并设置Field属性的值。
     *
     * @param field 字段表达式所表示的字段
     */
    public FieldExpression(Field field) {
        this.field = field;
    }

    /**
     * 获取字段表达式所表示的字段
     *
     * @return 表达式所表示的字段
     */
    public Field getField() {
        return this.field;
    }

    /**
     * 派生类实现此方法以判定具体类型的表达式对象是否相等
     *
     * @param other 要与当前表达式进行比较的表达式
     * @return 是否相等
     */
    @Override
    protected boolean concreteEquals(Expression other) {
        if (other instanceof FieldExpression) {
            FieldExpression fieIdOther = (FieldExpression) other;
            return fieIdOther.getField().equals(this.getField());
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
        return this.getField().toString(sourceType);
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
        //字段表达式没有参数化
        sqlParameters.realValue = new ArrayList<>();

        return this.getField().toString(sourceType);
    }
}
