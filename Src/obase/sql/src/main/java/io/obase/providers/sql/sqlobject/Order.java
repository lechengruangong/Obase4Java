/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示排序规则.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 17:35:59
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.providers.sql.EDataSource;

import java.util.Objects;

/**
 * 表示排序规则。排序规则由排序依据（字段）和排序方向构成
 */
public class Order {

    /**
     * 作为排序依据的表达式
     */
    private final Expression expression;

    /**
     * 排序方向
     */
    private EOrderDirection direction;

    /**
     * 是否是由Order执行器添加的
     */
    private boolean isAddByExecutor;

    /**
     * 创建排序规则
     *
     * @param field 作为排序依据的字段
     */
    public Order(String field) {
        this(Expression.field(new Field(field)));
    }

    /**
     * 创建排序规则
     *
     * @param field     排序字段
     * @param direction 排序方向
     */
    public Order(Field field, EOrderDirection direction) {
        this(Expression.field(field));
        this.direction = direction;
    }

    /**
     * 创建排序规则
     *
     * @param source    作为排序依据的字段所属的源
     * @param field     作为排序依据的字段
     * @param direction 排序方向
     */
    public Order(ISource source, String field, EOrderDirection direction) {
        this(new Field((MonomerSource) source, field), direction);
    }

    /**
     * 创建排序规则
     *
     * @param source 作为排序依据的字段所属的源
     * @param field  作为排序依据的字段
     */
    public Order(ISource source, String field) {
        this(Expression.field(new Field((MonomerSource) source, field)));
    }

    /**
     * 创建排序规则
     *
     * @param field     作为排序依据的字段
     * @param direction 排序方向
     */
    public Order(String field, EOrderDirection direction) {
        this(new Field(field), direction);
    }

    /**
     * 创建排序规则
     *
     * @param source 作为排序依据的字段所属源的名称
     * @param field  作为排序依据的字段
     */
    public Order(String source, String field) {
        this(Expression.field(new Field(source, field)));
    }

    /**
     * 创建排序规则
     *
     * @param source    作为排序依据的字段所属源的名称
     * @param field     作为排序依据的字段
     * @param direction 排序方向
     */
    public Order(String source, String field, EOrderDirection direction) {
        this(new Field(source, field), direction);
    }

    /**
     * 创建排序规则
     *
     * @param expression 排序表达式
     */
    public Order(Expression expression) {
        this.expression = expression;
    }

    /**
     * 创建排序规则
     *
     * @param expression 排序字段
     * @param direction  排序方向
     */
    public Order(Expression expression, EOrderDirection direction) {
        this(expression);
        this.direction = direction;
    }

    /**
     * 获取作为排序依据的表达式
     *
     * @return 排序依据的表达式
     */
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * 获取作为排序依据的字段
     *
     * @return 排序依据的表达式
     */
    public Field getField() {
        return ((FieldExpression) this.expression).getField();
    }

    /**
     * 获取排序方向
     *
     * @return 排序方向
     */
    public EOrderDirection getDirection() {
        return this.direction;
    }

    /**
     * 设置排序方向
     *
     * @param direction 排序方向
     */
    public void setDirection(EOrderDirection direction) {
        this.direction = direction;
    }

    /**
     * 获取是否是由Order执行器添加的
     *
     * @return 是否是由Order执行器添加的
     */
    public boolean getIsAddByExecutor() {
        return this.isAddByExecutor;
    }

    /**
     * 设置是否是由Order执行器添加的
     *
     * @param addByExecutor 是否是由Order执行器添加的
     */
    public void setIsAddByExecutor(boolean addByExecutor) {
        this.isAddByExecutor = addByExecutor;
    }

    /**
     * 将表达式访问者引导至排序依据表达式
     *
     * @param visitor 要引导的表达式访问者
     */
    public void guideExpressionVisitor(ExpressionVisitor visitor) {
        this.getExpression().accept(visitor);
    }

    /**
     * 重写转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return this.toString(EDataSource.SqlServer);
    }

    /**
     * 转换为字符串表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    public String toString(EDataSource sourceType) {
        return this.getExpression().toString(sourceType) + " " + this.getDirection();
    }

    /**
     * 重写相等比较方法
     *
     * @param o 另一个对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(this.expression, order.expression) && this.direction == order.direction;
    }

    /**
     * 重写获取哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.expression, this.direction);
    }
}
