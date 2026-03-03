/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表达式,Sql语句的基本单元.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 11:55:31
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.List;
import java.util.Objects;

/**
 * 表达式是构成Sql语句的基本单元，在Select、Where、Set、OrderBy、Join等子句中广泛存在。
 * 常量表达式和字段表达式是两种基本表达式。两个或多个表达式经运算（如算术运算、关系运算、逻辑运算、函数运算）可得出一个更复杂的表达式。按此规则，不管一个表达式多么
 * 复杂，最终都是由一系列基本表达式经多层次运算得出的，因此表达式可以看成树形结构（称为表达式树），其根节点为表达式本身，叶子节点为基本表达式，中间节点为算术表达式
 * 、关系表达式、逻辑表达式或函数表达式等。
 * Expression类是一个抽象基类，表示表达式树节点的类派生自该基类。同时，它还包含用来创建各种节点类的 静态工厂方法。
 */
public abstract class Expression {

    /**
     * 表达式类型
     */
    private EExpressionType nodeType;

    /**
     * 表达式的静态类型
     */
    private Class<?> type;

    /**
     * 创建一个Value属性设置为指定值的ConstantExpression
     *
     * @param value 常量值
     * @return 静态表达式
     */
    public static ConstantExpression constant(Object value) {
        ConstantExpression temp = new ConstantExpression(value);
        temp.setNodeType(EExpressionType.Constant);
        return temp;
    }

    /**
     * 创建一个Value属性和Type属性设置为指定值的ConstantExpression
     *
     * @param value 常量值
     * @param type  常量类型
     * @return 静态表达式
     */
    public static ConstantExpression constant(Object value, Class<?> type) {
        ConstantExpression constant = new ConstantExpression(value);
        constant.setNodeType(EExpressionType.Constant);
        constant.setType(type);
        return constant;
    }

    /**
     * 创建一个表示算术加法运算的ArithmeticExpression
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 算术运算的表达式
     */
    public static ArithmeticExpression add(Expression left, Expression right) {
        ArithmeticExpression arithmeticExpression = new ArithmeticExpression(left, right);
        arithmeticExpression.setNodeType(EExpressionType.Add);
        return arithmeticExpression;
    }

    /**
     * 创建一个表示算术减法运算的ArithmeticExpression
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 算术运算的表达式
     */
    public static ArithmeticExpression subtract(Expression left, Expression right) {
        ArithmeticExpression arithmeticExpression = new ArithmeticExpression(left, right);
        arithmeticExpression.setNodeType(EExpressionType.Subtract);
        return arithmeticExpression;
    }

    /**
     * 创建一个表示算术乘法运算的ArithmeticExpression
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 算术运算的表达式
     */
    public static ArithmeticExpression multiply(Expression left, Expression right) {
        ArithmeticExpression arithmeticExpression = new ArithmeticExpression(left, right);
        arithmeticExpression.setNodeType(EExpressionType.Multiply);
        return arithmeticExpression;
    }

    /**
     * 创建一个表示算术除法运算的ArithmeticExpression
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 算术运算的表达式
     */
    public static ArithmeticExpression divide(Expression left, Expression right) {
        ArithmeticExpression arithmeticExpression = new ArithmeticExpression(left, right);
        arithmeticExpression.setNodeType(EExpressionType.Divide);
        return arithmeticExpression;
    }

    /**
     * 创建一个表示幂运算的ArithmeticExpression
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 算术运算的表达式
     */
    public static ArithmeticExpression power(Expression left, Expression right) {
        ArithmeticExpression arithmeticExpression = new ArithmeticExpression(left, right);
        arithmeticExpression.setNodeType(EExpressionType.Power);
        return arithmeticExpression;
    }

    /**
     * 创建一个表示相等比较运算的ComparisonExpression
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 比较表达式
     */
    public static ComparisonExpression equal(Expression left, Expression right) {
        ComparisonExpression comparisonExpression = new ComparisonExpression(left, right);
        comparisonExpression.setNodeType(EExpressionType.Equal);
        return comparisonExpression;
    }

    /**
     * 创建一个表示不相等比较运算的ComparisonExpression
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 比较表达式
     */
    public static ComparisonExpression notEqual(Expression left, Expression right) {
        ComparisonExpression comparisonExpression = new ComparisonExpression(left, right);
        comparisonExpression.setNodeType(EExpressionType.NotEqual);
        return comparisonExpression;
    }

    /**
     * 创建一个表示“小于”比较运算的ComparisonExpression。
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 比较表达式
     */
    public static ComparisonExpression lessThan(Expression left, Expression right) {
        ComparisonExpression comparisonExpression = new ComparisonExpression(left, right);
        comparisonExpression.setNodeType(EExpressionType.LessThan);
        return comparisonExpression;
    }

    /**
     * 创建一个表示“小于或等于”比较运算的ComparisonExpression
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 比较表达式
     */
    public static ComparisonExpression lessThanOrEqual(Expression left, Expression right) {
        ComparisonExpression comparisonExpression = new ComparisonExpression(left, right);
        comparisonExpression.setNodeType(EExpressionType.LessThanOrEqual);
        return comparisonExpression;
    }

    /**
     * 创建一个表示“大于”比较运算的ComparisonExpression。
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 比较表达式
     */
    public static ComparisonExpression greaterThan(Expression left, Expression right) {
        ComparisonExpression comparisonExpression = new ComparisonExpression(left, right);
        comparisonExpression.setNodeType(EExpressionType.GreaterThan);
        return comparisonExpression;
    }

    /**
     * 创建一个表示“大于或等于”比较运算的ComparisonExpression
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 比较表达式
     */
    public static ComparisonExpression greaterThanOrEqual(Expression left, Expression right) {
        ComparisonExpression comparisonExpression = new ComparisonExpression(left, right);
        comparisonExpression.setNodeType(EExpressionType.GreaterThanOrEqual);
        return comparisonExpression;
    }

    /**
     * 创建一个表示LIKE运算的ComparisonExpression
     *
     * @param left    左操作数
     * @param pattern 匹配模式
     * @return 比较表达式
     */
    public static LikeExpression like(Expression left, String pattern, ELikeType likeType) {
        LikeExpression likeExpression = new LikeExpression(left, new ConstantExpression(pattern), likeType);
        likeExpression.setNodeType(EExpressionType.Like);
        return likeExpression;
    }

    /**
     * 创建一个表示LIKE运算的LikeExpression
     *
     * @param left    左操作数
     * @param pattern 匹配模式
     * @return Like表达式
     */
    public static LikeExpression like(Expression left, String pattern) {
        LikeExpression likeExpression = new LikeExpression(left, new ConstantExpression(pattern), ELikeType.Contains);
        likeExpression.setNodeType(EExpressionType.Like);
        return likeExpression;
    }

    /**
     * 创建一个表示LIKE运算的ComparisonExpression
     *
     * @param left    左操作数
     * @param pattern 表示匹配模式的表达式
     * @return Like表达式
     */
    public static LikeExpression like(Expression left, Expression pattern, ELikeType likeType) {
        LikeExpression likeExpression = new LikeExpression(left, pattern, likeType);
        likeExpression.setNodeType(EExpressionType.Like);
        return likeExpression;
    }

    /**
     * 创建一个表示LIKE运算的ComparisonExpression
     *
     * @param left    左操作数
     * @param pattern 表示匹配模式的表达式
     * @return Like表达式
     */
    public static LikeExpression like(Expression left, Expression pattern) {
        LikeExpression likeExpression = new LikeExpression(left, pattern, ELikeType.Contains);
        likeExpression.setNodeType(EExpressionType.Like);
        return likeExpression;
    }

    /**
     * 创建一个表示IN运算的表达式
     *
     * @param left     左操作数
     * @param valueSet 值域
     * @return IN表达式
     */
    public static InExpression in(Expression left, Object[] valueSet) {
        InExpression inExpression = new InExpression(left, valueSet, EInOperator.IN);
        inExpression.setNodeType(EExpressionType.In);
        return inExpression;
    }

    /**
     * 创建一个表示IN运算的表达式
     *
     * @param left     左操作数
     * @param valueSet 值域
     * @return IN表达式
     */
    public static InExpression in(Expression left, Iterable<Object> valueSet) {
        InExpression inExpression = new InExpression(left, valueSet, EInOperator.IN);
        inExpression.setNodeType(EExpressionType.In);
        return inExpression;
    }

    /**
     * 创建一个表示NOT IN运算的表达式
     *
     * @param left     左操作数
     * @param valueSet 值域
     * @return IN表达式
     */
    public static InExpression notIn(Expression left, Iterable<Object> valueSet) {
        InExpression inExpression = new InExpression(left, valueSet, EInOperator.NOTIN);
        inExpression.setNodeType(EExpressionType.NotIn);
        return inExpression;
    }

    /**
     * 创建一个表示逻辑AND运算的BinaryLogicExpression
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 二元逻辑表达式
     */
    public static BinaryLogicExpression andAlso(Expression left, Expression right) {
        BinaryLogicExpression binaryLogicExpression = new BinaryLogicExpression(left, right);
        binaryLogicExpression.setNodeType(EExpressionType.AndAlso);
        return binaryLogicExpression;
    }

    /**
     * 创建一个表示逻辑OR运算的BinaryLogicExpression
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 二元逻辑表达式
     */
    public static BinaryLogicExpression orElse(Expression left, Expression right) {
        BinaryLogicExpression binaryLogicExpression = new BinaryLogicExpression(left, right);
        binaryLogicExpression.setNodeType(EExpressionType.OrElse);
        return binaryLogicExpression;
    }

    /**
     * 创建一个表示逻辑求反运算的UnaryExpression
     *
     * @param operand 操作数
     * @return 取反表达式
     */
    public static UnaryExpression not(Expression operand) {
        UnaryExpression unary = new UnaryExpression(operand);
        unary.setNodeType(EExpressionType.Not);
        return unary;
    }

    /**
     * 创建一个表示函数调用的FunctionExpression。
     *
     * @param functionName 函数名称
     * @param arguments    表示各实参的表达式组成的集合
     * @return 函数表达式
     */
    public static FunctionExpression function(String functionName, Expression[] arguments) {
        FunctionExpression result = new FunctionExpression(functionName, arguments);
        result.setNodeType(EExpressionType.Function);
        return result;
    }

    /**
     * 创建一个FunctionExpression，它表示对不带参数的函数的调用
     *
     * @param functionName 函数名称
     * @return 函数表达式
     */
    public static FunctionExpression function(String functionName) {
        FunctionExpression result = new FunctionExpression(functionName, new Expression[0]);
        result.setNodeType(EExpressionType.Function);
        return result;
    }

    /**
     * 创建一个FunctionExpression，它表示对带一个参数的函数的调用
     *
     * @param functionName 函数名称
     * @param arg0         表示第一个实参的表达式
     * @return 函数表达式
     */
    public static FunctionExpression function(String functionName, Expression arg0) {
        Expression[] args = new Expression[1];
        args[0] = arg0;
        FunctionExpression result = new FunctionExpression(functionName, args);
        result.setNodeType(EExpressionType.Function);
        return result;
    }

    /**
     * 创建一个FunctionExpression，它表示对带两个参数的函数的调用
     *
     * @param functionName 函数名称
     * @param arg0         表示第一个实参的表达式
     * @param arg1         表示第二个实参的表达式
     * @return 函数表达式
     */
    public static FunctionExpression function(String functionName, Expression arg0, Expression arg1) {
        Expression[] args = new Expression[2];
        args[0] = arg0;
        args[1] = arg1;
        FunctionExpression result = new FunctionExpression(functionName, args);
        result.setNodeType(EExpressionType.Function);
        return result;
    }

    /**
     * 创建一个FunctionExpression，它表示对带三个参数的函数的调用
     *
     * @param functionName 函数名称
     * @param arg0         表示第一个实参的表达式
     * @param arg1         表示第二个实参的表达式
     * @param arg2         表示第三个实参的表达式
     * @return 函数表达式
     */
    public static FunctionExpression function(String functionName, Expression arg0, Expression arg1, Expression arg2) {
        Expression[] args = new Expression[3];
        args[0] = arg0;
        args[1] = arg1;
        args[2] = arg2;
        FunctionExpression result = new FunctionExpression(functionName, args);
        result.setNodeType(EExpressionType.Function);
        return result;
    }

    /**
     * 创建一个Field属性设置为指定值的FieldExpression
     *
     * @param field 字段表达式所表示的字段
     * @return 字段表达式
     */
    public static FieldExpression field(Field field) {
        FieldExpression fieldExpression = new FieldExpression(field);
        fieldExpression.setType(Object.class);
        fieldExpression.setNodeType(EExpressionType.Field);

        return fieldExpression;
    }

    /**
     * 创建一个表示算术余数运算的ArithmeticExpression，其中Left属性为被除数，Right属性为除数
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 算数表达式
     */
    public static ArithmeticExpression modulo(Expression left, Expression right) {
        ArithmeticExpression arithmetic = new ArithmeticExpression(left, right);
        arithmetic.setNodeType(EExpressionType.Modulo);
        return arithmetic;
    }

    /**
     * 创建一个表示递增运算（a+1，不就地修改a）的UnaryExpression
     *
     * @param operand 操作数
     * @return 递增表达式
     */
    public static UnaryExpression increment(Expression operand) {
        UnaryExpression unary = new UnaryExpression(operand);
        unary.setNodeType(EExpressionType.Increment);
        return unary;
    }

    /**
     * 创建一个表示递减运算（a-1，不就地修改a）的UnaryExpression
     *
     * @param operand 操作数
     * @return 递减表达式
     */
    public static UnaryExpression decrement(Expression operand) {
        UnaryExpression unary = new UnaryExpression(operand);
        unary.setNodeType(EExpressionType.Decrement);
        return unary;
    }

    /**
     * 创建一个表示一元加法运算（+a，不就地修改a）的UnaryExpression。
     *
     * @param operand 操作数
     * @return 一元加表达式
     */
    public static UnaryExpression unaryPlus(Expression operand) {
        UnaryExpression unary = new UnaryExpression(operand);
        unary.setNodeType(EExpressionType.UnaryPlus);
        return unary;
    }

    /**
     * 创建一个表示算术求反运算（-a，不就地修改a）的UnaryExpression
     *
     * @param operand 操作数
     * @return 一元减表达式
     */
    public static UnaryExpression negate(Expression operand) {
        UnaryExpression unary = new UnaryExpression(operand);
        unary.setNodeType(EExpressionType.Negate);
        return unary;
    }

    /**
     * 创建一个表示按位与运算的表达式
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 二元按位运算表达式
     */
    public static BinaryBitExpression bitAnd(Expression left, Expression right) {
        BinaryBitExpression binaryBitExpression = new BinaryBitExpression(left, right);
        binaryBitExpression.setNodeType(EExpressionType.BitAnd);
        return binaryBitExpression;
    }

    /**
     * 创建一个表示按位取反运算的表达式
     *
     * @param operand 操作数
     * @return 二元按位运算表达式
     */
    public static UnaryExpression bitNot(Expression operand) {
        UnaryExpression unary = new UnaryExpression(operand);
        unary.setNodeType(EExpressionType.BitNot);
        return unary;
    }

    /**
     * 创建一个表示按位或运算的表达式
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 二元按位运算表达式
     */
    public static BinaryBitExpression bitOr(Expression left, Expression right) {
        BinaryBitExpression binaryBitExpression = new BinaryBitExpression(left, right);
        binaryBitExpression.setNodeType(EExpressionType.BitOr);
        return binaryBitExpression;
    }

    /**
     * 创建一个表示按位异或运算的表达式
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 二元按位运算表达式
     */
    public static BinaryBitExpression bitXor(Expression left, Expression right) {
        BinaryBitExpression binaryBitExpression = new BinaryBitExpression(left, right);
        binaryBitExpression.setNodeType(EExpressionType.BitXor);
        return binaryBitExpression;
    }

    /**
     * 创建一个表示按位左移运算的表达式
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 二元按位运算表达式
     */
    public static BinaryBitExpression leftShift(Expression left, Expression right) {
        BinaryBitExpression binaryBitExpression = new BinaryBitExpression(left, right);
        binaryBitExpression.setNodeType(EExpressionType.LeftShift);
        return binaryBitExpression;
    }

    /**
     * 创建一个表示按位右移运算的表达式
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 二元按位运算表达式
     */
    public static BinaryBitExpression rightShift(Expression left, Expression right) {
        BinaryBitExpression binaryBitExpression = new BinaryBitExpression(left, right);
        binaryBitExpression.setNodeType(EExpressionType.RightShift);
        return binaryBitExpression;
    }

    /**
     * 创建全局通配符表达式
     *
     * @return 全局通配符表达式
     */
    public static WildcardExpression wildcard() {
        return new WildcardExpression("*");
    }

    /**
     * 创建在指定名称的源范围内的通配符表达式
     *
     * @param source 源名称
     * @return 全局通配符表达式
     */
    public static WildcardExpression wildcard(String source) {
        return new WildcardExpression(source);
    }

    /**
     * 创建在指定源范围内的通配符表达式
     *
     * @param source 源
     * @return 全局通配符表达式
     */
    public static WildcardExpression wildcard(ISource source) {
        return new WildcardExpression(source);
    }

    /**
     * 获取表达式的节点类型
     *
     * @return 表达式类型
     */
    public EExpressionType getNodeType() {
        return this.nodeType;
    }

    /**
     * 设置表达式类型
     *
     * @param nodeType 表达式类型
     */
    protected void setNodeType(EExpressionType nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * 获取表达式的静态类型
     *
     * @return 表达式的静态类型
     */
    public Class<?> getType() {
        return this.type;
    }

    /**
     * 设置静态类型
     *
     * @param type 表达式的静态类型
     */
    protected void setType(Class<?> type) {
        this.type = type;
    }

    /**
     * 重写相等方法
     *
     * @param o 另一个对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Expression that = (Expression) o;
        return this.nodeType == that.nodeType && this.type.equals(that.type);
    }

    /**
     * 重写获取哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.nodeType, this.type);
    }

    /**
     * 接受指定的访问者对当前表达式实例的访问
     * 注：本方法有可能返回一个新的表达式。如果访问者返回的表达式实例与当前实例相等，本方法返回当前实例，否则返回新实例。
     *
     * @param visitor 表达式访问者
     * @return Expression 对当前表达式访问的结果
     */
    public Expression accept(ExpressionVisitor visitor) {
        return visitor.visit(this);
    }

    /**
     * 派生类实现此方法以判定具体类型的表达式对象是否相等
     *
     * @param other 要与当前表达式进行比较的表达式
     * @return 是否相等
     */
    protected abstract boolean concreteEquals(Expression other);

    /**
     * 针对指定的数据源类型，返回表达式的文本表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    public abstract String toString(EDataSource sourceType);

    /**
     * 使用参数化的方式 和 指定的数据源 将表达式表示为字符串形式
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return 字符串表示形式
     */
    public abstract String toString(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator);
}
