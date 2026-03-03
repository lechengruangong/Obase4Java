/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表达式基类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-18 16:12:54
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;


import io.obase.common.ObjectReferencePack;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.objectSys.*;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 仿造C#表达式类基类
 * 用于表示TypedValue访问后的结果
 * 同时还要承担表达式计算的职责
 */
public abstract class Expression {

    /**
     * 表达式类型
     */
    protected EExpressionType expressionType;
    /**
     * 表达式返回的类型
     */
    protected Class<?> type;

    /**
     * 构造一个与操作的二元表达式
     *
     * @param left  左端
     * @param right 右端
     * @param type  表达式的返回值类型
     * @return 二元表达式
     */
    public static BinaryExpression and(Expression left, Expression right, Class<?> type) {
        return new BinaryExpression(left, right, EExpressionType.AndAlso, type);
    }

    /**
     * 构造一个或操作的二元表达式
     *
     * @param left  左端
     * @param right 右端
     * @param type  表达式的返回值类型
     * @return 二元表达式
     */
    public static BinaryExpression or(Expression left, Expression right, Class<?> type) {
        return new BinaryExpression(left, right, EExpressionType.OrElse, type);
    }

    /**
     * 构造一个相等操作的二元表达式
     *
     * @param left  左端
     * @param right 右端
     * @param type  表达式的返回值类型
     * @return 二元表达式
     */
    public static BinaryExpression equal(Expression left, Expression right, Class<?> type) {
        return new BinaryExpression(left, right, EExpressionType.Equal, type);
    }

    /**
     * 构造一个不相等操作的二元表达式
     *
     * @param left  左端
     * @param right 右端
     * @param type  表达式的返回值类型
     * @return 二元表达式
     */
    public static BinaryExpression notEqual(Expression left, Expression right, Class<?> type) {
        return new BinaryExpression(left, right, EExpressionType.NotEqual, type);
    }

    /**
     * 构造一个小于操作的二元表达式
     *
     * @param left  左端
     * @param right 右端
     * @param type  表达式的返回值类型
     * @return 二元表达式
     */
    public static BinaryExpression lessThan(Expression left, Expression right, Class<?> type) {
        return new BinaryExpression(left, right, EExpressionType.LessThan, type);
    }

    /**
     * 构造一个大于操作的二元表达式
     *
     * @param left  左端
     * @param right 右端
     * @param type  表达式的返回值类型
     * @return 二元表达式
     */
    public static BinaryExpression greaterThan(Expression left, Expression right, Class<?> type) {
        return new BinaryExpression(left, right, EExpressionType.GreaterThan, type);
    }

    /**
     * 构造一个小于等于操作的二元表达式
     *
     * @param left  左端
     * @param right 右端
     * @param type  表达式的返回值类型
     * @return 二元表达式
     */
    public static BinaryExpression lessThanOrEqual(Expression left, Expression right, Class<?> type) {
        return new BinaryExpression(left, right, EExpressionType.LessThanOrEqual, type);
    }

    /**
     * 构造一个大于等于操作的二元表达式
     *
     * @param left  左端
     * @param right 右端
     * @param type  表达式的返回值类型
     * @return 二元表达式
     */
    public static BinaryExpression greaterThanOrEqual(Expression left, Expression right, Class<?> type) {
        return new BinaryExpression(left, right, EExpressionType.GreaterThanOrEqual, type);
    }

    /**
     * 构造一个算数加法操作的二元表达式
     *
     * @param left  左端
     * @param right 右端
     * @param type  表达式的返回值类型
     * @return 二元表达式
     */
    public static BinaryExpression add(Expression left, Expression right, Class<?> type) {
        return new BinaryExpression(left, right, EExpressionType.Add, type);
    }

    /**
     * 构造一个算数减法操作的二元表达式
     *
     * @param left  左端
     * @param right 右端
     * @param type  表达式的返回值类型
     * @return 二元表达式
     */
    public static BinaryExpression subtract(Expression left, Expression right, Class<?> type) {
        return new BinaryExpression(left, right, EExpressionType.Subtract, type);
    }

    /**
     * 构造一个算数乘法操作的二元表达式
     *
     * @param left  左端
     * @param right 右端
     * @param type  表达式的返回值类型
     * @return 二元表达式
     */
    public static BinaryExpression multi(Expression left, Expression right, Class<?> type) {
        return new BinaryExpression(left, right, EExpressionType.Multiply, type);
    }

    /**
     * 构造一个算数除法操作的二元表达式
     *
     * @param left  左端
     * @param right 右端
     * @param type  表达式的返回值类型
     * @return 二元表达式
     */
    public static BinaryExpression divide(Expression left, Expression right, Class<?> type) {
        return new BinaryExpression(left, right, EExpressionType.Divide, type);
    }

    /**
     * 构造一个算数取模操作的二元表达式
     *
     * @param left  左端
     * @param right 右端
     * @param type  表达式的返回值类型
     * @return 二元表达式
     */
    public static BinaryExpression modula(Expression left, Expression right, Class<?> type) {
        return new BinaryExpression(left, right, EExpressionType.Modula, type);
    }

    /**
     * 构造一个指定类型的二元表达式
     *
     * @param left           左端
     * @param right          右端
     * @param type           表达式的返回值类型
     * @param expressionType 表达式的类型
     * @return 二元表达式
     */
    public static BinaryExpression makeBinary(Expression left, Expression right, Class<?> type, EExpressionType expressionType) {
        return new BinaryExpression(left, right, expressionType, type);
    }

    /**
     * 构造一个常量表达式
     *
     * @param value 值
     * @return 常量表达式
     */
    public static ConstantExpression constant(Object value) {
        return new ConstantExpression(value);
    }

    /**
     * 构造一个lambda表达式
     *
     * @param parameterExpressions 参数列表
     * @param body                 表达式体
     * @return lambda表达式
     */
    public static LambdaExpression lambda(ParameterExpression[] parameterExpressions, Expression body) {
        return new LambdaExpression(parameterExpressions, body);
    }


    /**
     * 构造一个参数表达式
     *
     * @param name   参数名
     * @param type   参数的类型
     * @param isHost 是否为宿主参数
     * @return 参数表达式
     */
    public static ParameterExpression parameter(String name, Class<?> type, Object obj, boolean isHost) {
        return new ParameterExpression(name, obj, type, isHost);
    }

    /**
     * 构造一个参数表达式
     * 此参数表达式表示宿主参数 即表达式中代表类型的参数
     *
     * @param name 参数名
     * @param type 参数的类型
     * @return 参数表达式
     */
    public static ParameterExpression parameter(String name, Class<?> type) {
        return new ParameterExpression(name, null, type, true);
    }

    /**
     * 构造一个成员访问表达式
     *
     * @param expression   成员所属类的表达式
     * @param memberMethod 诚邀访问方法
     * @param host         宿主表达式
     * @param hostType     宿主类型
     * @return 成员访问表达式
     */
    public static MemberExpression member(Expression expression, Method memberMethod, Expression host, Class<?> hostType) {

        String memberName = null;
        Property property = ObaseIntrospector.getObaseBeanProperties(memberMethod.getDeclaringClass()).stream().filter(p -> Objects.equals(p.getGetterMethod().toString(), memberMethod.toString())).findFirst().orElse(null);

        if (property != null) {
            memberName = property.getName();
        }

        return new MemberExpression(memberMethod, expression, memberName, property, host, hostType);
    }

    /**
     * 构造一个成员访问表达式
     *
     * @param expression   成员所属类的表达式
     * @param memberMethod 诚邀访问方法
     * @param hostType     宿主类型
     * @return 成员访问表达式
     */
    public static MemberExpression member(Expression expression, Method memberMethod, Class<?> hostType) {
        return Expression.member(expression, memberMethod, null, hostType);
    }

    /**
     * 构造一个调用表达式
     *
     * @param argument 参数列表
     * @param method   方法
     * @param object   所属的对象
     * @return 调用表达式
     */
    public static MethodCallExpression call(Expression[] argument, Method method, Expression object) {
        return new MethodCallExpression(argument, method, object);
    }

    /**
     * 构造一个构造函数表达式
     *
     * @param type 类型
     * @return 构造函数表达式
     */
    public static NewExpression news(Class<?> type) {
        return new NewExpression(type);
    }

    /**
     * 构造成员绑定
     *
     * @param memberMethod 成员方法
     * @param expression   表达式
     * @return 成员绑定
     */
    public static MemberAssignment bind(Method memberMethod, Expression expression) {
        return new MemberAssignment(expression, memberMethod);
    }

    /**
     * 构造成员赋值初始化表达式
     *
     * @param expression 表达式
     * @param bindings   成员绑定
     * @return 成员赋值初始化表达式
     */
    public static MemberInitExpression memberInit(NewExpression expression, MemberBinding[] bindings) {
        return new MemberInitExpression(expression, bindings);
    }

    /**
     * 构造一个算数取反表达式
     *
     * @param operand 操作数
     * @return 算数取反表达式
     */
    public static UnaryExpression negate(Expression operand) {
        return new UnaryExpression(operand, EExpressionType.Negate);
    }

    /**
     * 构造一个逻辑取反表达式
     *
     * @param operand 操作数
     * @return 逻辑取反表达式
     */
    public static UnaryExpression not(Expression operand) {
        return new UnaryExpression(operand, EExpressionType.Not);
    }

    /**
     * 构造一个转换表达式
     *
     * @param operand 操作数
     * @return 转换表达式
     */
    public static UnaryExpression convert(Expression operand) {
        return new UnaryExpression(operand, EExpressionType.Convert);
    }

    /**
     * 获取表达式类型
     *
     * @return 表达式类型
     */
    public abstract EExpressionType getExpressionType();

    /**
     * 获取表达式返回的类型
     *
     * @return 表达式返回的类型
     */
    public abstract Class<?> getType();

    /**
     * 计算表达式的值
     *
     * @param getter 参数值获取器
     * @return 计算后的结果
     */
    public abstract Object calculate(IArgumentGetter getter);

    /**
     * 接受访问方法 各个表达式自行实现
     *
     * @param visitor 表达式访问器
     * @return 访问结果
     */
    public Expression accept(ExpressionVisitor visitor) {
        return visitor.visit(this);
    }

    /**
     * 接受表达式访问者
     *
     * @param visitor 表达式访问者
     * @param model   对象数据模型
     */
    public void accept(IAssociationTreeDownwardVisitor visitor, ObjectDataModel model) {
        this.expressionVerify(this);
        //根据表达式提取关联树
        AssociationTree assTree = this.extractAssociationWithAttributeTree(model, new ObjectReferencePack<>(), null);
        //如果有关联树 遍历关联树访问者
        if (assTree != null && visitor != null) assTree.accept(visitor);
    }

    /**
     * 接受关联树访问者
     *
     * @param visitor   关联树访问者
     * @param model     对象数据模型
     * @param <TResult> 访问结果类型
     * @return 访问结果
     */
    public <TResult> TResult accept(IAssociationTreeDownwardVisitorWithResult<TResult> visitor, ObjectDataModel model) {
        ObjectReferencePack<TResult> assoResult = new ObjectReferencePack<>();
        this.accept(visitor, null, model, assoResult, new ObjectReferencePack<>());
        return assoResult.realValue;
    }

    /**
     * 接受关联树访问者和属性树访问者
     *
     * @param assVisitor  关联树访问者
     * @param attrVisitor 属性树访问者
     * @param model       对象数据模型
     * @param assResult   返回关联树访问结果
     * @param attrResult  返回属性树访问结果
     * @param <TResult>   访问结果类型
     */
    public <TResult> void accept(
            IAssociationTreeDownwardVisitorWithResult<TResult> assVisitor, IAttributeTreeDownwardVisitorWithResult<TResult> attrVisitor,
            ObjectDataModel model, ObjectReferencePack<TResult> assResult, ObjectReferencePack<TResult> attrResult) {
        this.expressionVerify(this);
        //根据表达式提取关联树
        ObjectReferencePack<AttributeTree> attrTree = new ObjectReferencePack<>();
        AssociationTree assTree = this.extractAssociationWithAttributeTree(model, attrTree, null);
        //如果有关联树 遍历关联树访问者
        if (assTree != null && assVisitor != null) assResult.realValue = assTree.accept(assVisitor);
        // 如果有属性树 遍历属性树访问者
        if (attrTree.realValue != null && attrVisitor != null)
            attrResult.realValue = attrTree.realValue.accept(attrVisitor);
    }

    /**
     * 从表达式中抽取关联并表示成关联树
     *
     * @param model        对象数据模型
     * @param paraBindings 形参绑定
     * @return 抽取出的关联树
     */
    public AssociationTree extractAssociation(ObjectDataModel model,
                                              ParameterBinding[] paraBindings) {
        return this.extractAssociation(model, new ObjectReferencePack<>(), new ObjectReferencePack<>(), new ObjectReferencePack<>(), paraBindings);
    }

    /**
     * 从表达式中抽取关联并表示成关联树,不抽取属性树
     *
     * @param model        对象数据模型
     * @param paraBindings 形参绑定
     * @return 抽取出的关联树
     */
    public AssociationTree onlyExtractAssociation(ObjectDataModel model,
                                                  ParameterBinding[] paraBindings) {
        this.expressionVerify(this);
        //构造生长器
        AssociationGrower grower = new AssociationGrower(model, null);
        grower.setExtractingAttribute(false);
        grower.setParameterBindings(paraBindings);
        //访问
        grower.visit(this);

        return grower.getAssociationTree();
    }

    /**
     * 从表达式中抽取关联并表示成关联树，同时抽取属性树
     *
     * @param model        对象数据模型
     * @param attrTree     返回从表达式中抽取的属性树
     * @param paraBindings 形参绑定
     * @return 抽取出的关联树
     */
    public AssociationTree extractAssociationWithAttributeTree(ObjectDataModel model,
                                                               ObjectReferencePack<AttributeTree> attrTree, ParameterBinding[] paraBindings) {
        return this.extractAssociation(model, new ObjectReferencePack<>(), new ObjectReferencePack<>(), attrTree, paraBindings);
    }

    /**
     * 从表达式中抽取关联并表示成关联树
     *
     * @param model        对象数据模型
     * @param attrTail     返回从表达式中抽取的属性树的末节点
     * @param paraBindings 形参绑定
     * @return 抽取出的关联树
     */
    public AssociationTree extractAssociationWithTreeNode(ObjectDataModel model,
                                                          ObjectReferencePack<AttributeTreeNode> attrTail,
                                                          ParameterBinding[] paraBindings) {
        return this.extractAssociation(model, new ObjectReferencePack<>(), attrTail, new ObjectReferencePack<>(), paraBindings);
    }

    /**
     * 从表达式中抽取关联并表示成关联树，同时抽取属性树。
     *
     * @param model        对象数据模型
     * @param attrTree     返回从表达式抽取的属性树
     * @param attrTail     返回从表达式抽取的属性树的末节点
     * @param paraBindings 形参绑定
     * @return 抽取出的关联树
     */
    public AssociationTree extractAssociationWithAttributeTree(ObjectDataModel model,
                                                               ObjectReferencePack<AttributeTree> attrTree, ObjectReferencePack<AttributeTreeNode> attrTail,
                                                               ParameterBinding[] paraBindings) {
        return this.extractAssociation(model, new ObjectReferencePack<>(), attrTail, attrTree, paraBindings);
    }

    /**
     * 从表达式中抽取关联并表示成关联树。
     *
     * @param model        对象数据模型
     * @param assTail      返回从表达式中抽取的关联树的末节点
     * @param paraBindings 形参绑定
     * @return 抽取出的关联树
     */
    public AssociationTree extractAssociation(ObjectDataModel model,
                                              ObjectReferencePack<AssociationTreeNode> assTail,
                                              ParameterBinding[] paraBindings) {
        return this.extractAssociation(model, assTail, new ObjectReferencePack<>(), new ObjectReferencePack<>(), paraBindings);
    }

    /**
     * 从表达式中抽取关联并表示成关联树，同时抽取属性树。
     *
     * @param model        对象数据模型
     * @param assTail      返回从表达式中抽取的关联树末节点
     * @param attrTree     返回从表达式中抽取的属性树
     * @param paraBindings 形参绑定
     * @return 抽取出的关联树
     */
    public AssociationTree extractAssociationWithAssociationTreeNodeAndAttributeTree(ObjectDataModel model,
                                                                                     ObjectReferencePack<AssociationTreeNode> assTail, ObjectReferencePack<AttributeTree> attrTree,
                                                                                     ParameterBinding[] paraBindings) {
        return this.extractAssociation(model, assTail, new ObjectReferencePack<>(), attrTree, paraBindings);
    }


    /**
     * 从表达式中抽取关联并表示成关联树，同时抽取属性树
     *
     * @param model        对象数据模型
     * @param assTail      返回从表达式中抽取的关联树末节点
     * @param attrTail     返回从表达式中抽取的属性树
     * @param paraBindings 形参绑定
     * @return 抽取出的关联树
     */
    public AssociationTree extractAssociation(ObjectDataModel model,
                                              ObjectReferencePack<AssociationTreeNode> assTail, ObjectReferencePack<AttributeTreeNode> attrTail,
                                              ParameterBinding[] paraBindings) {
        return this.extractAssociation(model, assTail, attrTail, new ObjectReferencePack<>(), paraBindings);
    }

    /**
     * 从表达式中抽取关联并表示成关联树，同时抽取属性树。
     *
     * @param model        对象数据模型
     * @param assTail      返回从表达式中抽取的关联树末节点
     * @param attrTail     返回从表达式中抽取的属性树末节点
     * @param attrTree     返回从表达式中抽取的属性树
     * @param paraBindings 形参绑定
     * @return 抽取出的关联树
     */
    public AssociationTree extractAssociation(ObjectDataModel model,
                                              ObjectReferencePack<AssociationTreeNode> assTail,
                                              ObjectReferencePack<AttributeTreeNode> attrTail,
                                              ObjectReferencePack<AttributeTree> attrTree,
                                              ParameterBinding[] paraBindings) {

        this.expressionVerify(this);
        //构造生长器
        AssociationGrower grower = new AssociationGrower(model, null);
        grower.setExtractingAttribute(true);
        grower.setParameterBindings(paraBindings);
        //访问
        grower.visit(this);
        //值
        assTail.realValue = grower.getLastAssociationNode() == null ? null : grower.getLastAssociationNode().getNode();
        attrTail.realValue = grower.getLastAttributeNode() == null ? null : grower.getLastAttributeNode().getNode();
        attrTree.realValue = grower.getAttributeTree();

        return grower.getAssociationTree();

    }

    /**
     * 根据表达式的指引生长指定的关联树。
     *
     * @param assoTree     待生长的关联树
     * @param model        对象数据模型
     * @param paraBindings 形参绑定
     * @return 关联树生长后的末节点
     */
    public AssociationTreeNode growAssociationTree(AssociationTree assoTree,
                                                   ObjectDataModel model, ParameterBinding[] paraBindings) {
        return this.growAssociationTree(assoTree, model, new ObjectReferencePack<>(), new ObjectReferencePack<>(), paraBindings);
    }

    /**
     * 根据表达式的指引生长指定的关联树。
     *
     * @param assoTree     待生长的关联树
     * @param model        对象数据模型
     * @param paraBindings 形参绑定
     * @return 关联树生长后的末节点
     */
    public AssociationTreeNode onlyGrowAssociationTree(AssociationTree assoTree,
                                                       ObjectDataModel model, ParameterBinding[] paraBindings, Expression preExpression) {
        this.expressionVerify(this);
        AssociationGrower grower = new AssociationGrower(model, assoTree);
        grower.setExtractingAttribute(false);
        grower.setPreExpression(preExpression);
        grower.setParameterBindings(paraBindings);

        //访问
        grower.visit(this);
        return grower.getLastAssociationNode().getNode();
    }

    /**
     * 根据表达式的指引生长指定的关联树，同时从表达式抽取属性树。
     *
     * @param assoTree     待生长的关联树
     * @param model        对象数据模型
     * @param attrTree     从表达式抽取的属性树
     * @param paraBindings 形参绑定
     * @return 关联树生长后的末节点
     */
    public AssociationTreeNode growAssociationTreeWithAttributeTree(AssociationTree assoTree,
                                                                    ObjectDataModel model, ObjectReferencePack<AttributeTree> attrTree, ParameterBinding[] paraBindings) {
        return this.growAssociationTree(assoTree, model, attrTree, new ObjectReferencePack<>(), paraBindings);
    }

    /**
     * 根据表达式的指引生长指定的关联树，同时从表达式抽取属性树。
     *
     * @param assTree      待生长的关联树
     * @param model        对象数据模型
     * @param attrTail     从表达式抽取的属性树的末节点
     * @param paraBindings 形参绑定
     * @return 关联树生长后的末节点
     */
    public AssociationTreeNode growAssociationTree(AssociationTree assTree,
                                                   ObjectDataModel model, ObjectReferencePack<AttributeTreeNode> attrTail, ParameterBinding[] paraBindings) {
        return this.growAssociationTree(assTree, model, new ObjectReferencePack<>(), attrTail, paraBindings);
    }

    /**
     * 根据表达式的指引生长指定的关联树，同时从表达式抽取属性树。
     *
     * @param assTree      待生长的关联树
     * @param model        对象数据模型
     * @param attrTree     从表达式抽取的属性树
     * @param attrTail     从表达式抽取的属性树的末节点
     * @param paraBindings 形参绑定
     * @return 关联树生长后的末节点
     */
    public AssociationTreeNode growAssociationTree(AssociationTree assTree,
                                                   ObjectDataModel model, ObjectReferencePack<AttributeTree> attrTree,
                                                   ObjectReferencePack<AttributeTreeNode> attrTail, ParameterBinding[] paraBindings) {

        this.expressionVerify(this);
        AssociationGrower grower = new AssociationGrower(model, assTree);
        grower.setParameterBindings(paraBindings);
        grower.setExtractingAttribute(true);

        //访问
        grower.visit(this);
        //值
        attrTree.realValue = grower.getAttributeTree();
        attrTail.realValue = grower.getLastAttributeNode() == null ? null : grower.getLastAttributeNode().getNode();

        return grower.getLastAssociationNode().getNode();
    }

    /**
     * 检查成员表达式和参数表达式
     *
     * @param ex 要检查的检查表达式
     */
    private void expressionVerify(Expression ex) {
        if (ex instanceof MemberExpression) return;
        if (ex instanceof ParameterExpression) return;
        /*20210915 Obase修改添加 LambdaExpression和MethodCallExpression*/
        if (ex instanceof LambdaExpression) return;
        if (ex instanceof MethodCallExpression) {
            MethodCallExpression methodCallExpression = (MethodCallExpression) ex;
            String methodName = methodCallExpression.getMethod().getName();
            if (methodName.equalsIgnoreCase("map") || methodName.equalsIgnoreCase("flatmap"))
                return;
        }
        throw new IllegalArgumentException("Obase.Odm.ObjectSys.ExpressionExtension扩展不支持表达式)。");
    }
}
