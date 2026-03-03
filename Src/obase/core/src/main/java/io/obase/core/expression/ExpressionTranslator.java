/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Lambda表达式翻译器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 15:32:04
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import ch.epfl.labos.iu.orm.queryll2.symbolic.*;
import org.jinq.rebased.org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Lambda表达式翻译器
 * 继承自TypedValueVisitor
 */
public class ExpressionTranslator extends TypedValueVisitor<Map<String, ParameterExpression>, Expression, TypedValueVisitorException> {

    /**
     * 实参列表
     */
    private final Map<Integer, Object> realArguments;

    /**
     * 表达式翻译器
     *
     * @param realArguments 实参列表
     */
    public ExpressionTranslator(Map<Integer, Object> realArguments) {
        this.realArguments = realArguments;
    }

    /**
     * 默认的访问方法 调用此方法表示具体的访问方法未重写
     *
     * @param val TypedValue值
     * @param in  一并传入的参数
     * @return 表达式
     * @throws TypedValueVisitorException 转换异常
     */
    @Override
    public Expression defaultValue(TypedValue val, Map<String, ParameterExpression> in) throws TypedValueVisitorException {
        throw new TypedValueVisitorException("此访问方法未重写: " + val);
    }

    /**
     * 访问二元逻辑表达式
     *
     * @param val 值
     * @param in  一并传入的参数
     * @return 二元表达式
     * @throws TypedValueVisitorException 转换异常
     */
    @Override
    public Expression comparisonOpValue(TypedValue.ComparisonValue val, Map<String, ParameterExpression> in) throws TypedValueVisitorException {
        Expression left = val.left.visit(this, in);
        Expression right = val.right.visit(this, in);

        Class<?> type;
        if (val.isPrimitive())
            type = this.getTypeByName(val.getType());
        else {
            try {
                type = Class.forName(val.getType().getClassName());
            } catch (ClassNotFoundException e) {
                throw new TypedValueVisitorException("无法加载类" + val.getType().getClassName(), e);
            }
        }

        BinaryExpression binaryExpression = null;
        switch (val.compOp) {
            //相等
            case eq:
                binaryExpression = Expression.equal(left, right, type);
                break;
            //不相等
            case ne:
                binaryExpression = Expression.notEqual(left, right, type);
                break;
            //小于
            case lt:
                binaryExpression = Expression.lessThan(left, right, type);
                break;
            //大于
            case gt:
                binaryExpression = Expression.greaterThan(left, right, type);
                break;
            //小于等于
            case le:
                binaryExpression = Expression.lessThanOrEqual(left, right, type);
                break;
            //大于等于
            case ge:
                binaryExpression = Expression.greaterThanOrEqual(left, right, type);
                break;
        }

        return binaryExpression;
    }

    /**
     * 访问虚拟方法表达式
     *
     * @param val 虚拟方法表达式
     * @param in  一并传入的参数
     * @return 表达式
     * @throws TypedValueVisitorException 转换异常
     */
    @Override
    public Expression virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, Map<String, ParameterExpression> in) throws TypedValueVisitorException {
        MethodSignature sig = val.getSignature();
        //处理方法签名
        //先处理包装类的方法 包含 int long double float boolean
        String rName = null;
        if (MethodChecker.getInstance().boxingFunction.contains(sig)) {
            String[] rNames = val.base.toString().split("\\.");
            if (rNames.length > 1) {
                rName = rNames[rNames.length - 1].replace("()", "");
            }
        }
        //被包装的
        if (rName != null) {
            sig = new MethodSignature(((MethodCallValue.VirtualMethodCallValue) val.base).owner, rName, ((MethodCallValue.VirtualMethodCallValue) val.base).desc);
        }

        //处理Get 则此处为MemberExpress
        if (MethodChecker.getInstance().filedMethod.contains(sig)) {
            Expression baseExp = val.base.visit(this, in);
            ParameterExpression parameter = null;
            MemberExpression member = null;
            if (rName != null) {
                //多访问一层
                MethodCallValue.VirtualMethodCallValue virtualMethodCallValue = (MethodCallValue.VirtualMethodCallValue) val.base;
                parameter = (ParameterExpression) virtualMethodCallValue.base.visit(this, in);
            } else {

                if (baseExp instanceof ParameterExpression) {
                    //base即为Lambda表达式的形参
                    parameter = (ParameterExpression) baseExp;
                }

                if (baseExp instanceof MemberExpression) {
                    member = (MemberExpression) baseExp;
                }

                //如果是转换表达式 此处内部已经有成员访问表达式了
                if (baseExp instanceof UnaryExpression) {
                    UnaryExpression unaryExpression = (UnaryExpression) baseExp;
                    if (unaryExpression.expressionType == EExpressionType.Convert) {
                        try {
                            //处理转化的逻辑 找转换后的具体类型
                            Class<?> baseType = Class.forName(val.base.getType().getClassName());
                            Method filedMethod = baseType.getMethod(sig.name);

                            return Expression.member(baseExp, filedMethod, unaryExpression, baseExp.getType());
                        } catch (ClassNotFoundException | NoSuchMethodException e) {
                            throw new TypedValueVisitorException("无法获取转换的成员访问", e);
                        }
                    }
                }
            }

            //普通的表达式
            try {
                Method filedMethod = baseExp.getType().getMethod(sig.name);

                return Expression.member(member == null ? parameter : member, filedMethod, member, baseExp.getType());

            } catch (NoSuchMethodException e) {
                if ((baseExp instanceof MemberExpression)) {
                    return baseExp;
                }
                throw new TypedValueVisitorException("无法获取成员访问", e);
            }
        } else {
            //调用方
            Expression object = val.base.visit(this, in);

            Expression[] argument = new Expression[val.args.size()];
            for (int i = 0; i < val.args.size(); i++) {
                argument[i] = val.args.get(i).visit(this, in);
            }

            Class<?>[] argumentClass = new Class<?>[argument.length];
            Arrays.stream(argument).map(Expression::getType).collect(Collectors.toList()).toArray(argumentClass);

            //特殊处理一下contains string的contains用的参数是CharSequence接口
            if (Objects.equals(sig.name, "contains")) {
                if (object.getType() == String.class)
                    argumentClass[0] = CharSequence.class;
                if (Iterable.class.isAssignableFrom(object.getType()))
                    argumentClass[0] = Object.class;
            }


            try {

                //如果是构造函数表达式 则这些参数被用于构造函数
                if (object instanceof NewExpression) {
                    NewExpression newExpression = (NewExpression) object;

                    try {
                        //找出构造函数
                        Constructor<?> constructor = object.getType().getDeclaredConstructor(argumentClass);
                        newExpression.setArgument(argument);
                        newExpression.setConstructor(constructor);
                    } catch (NoSuchMethodException ex) {
                        for (int i = 0; i < argumentClass.length; i++) {
                            if (argumentClass[i].equals(boolean.class)) {
                                argumentClass[i] = Boolean.class;
                            }
                        }
                        //找出构造函数
                        Constructor<?> constructor = object.getType().getDeclaredConstructor(argumentClass);
                        newExpression.setArgument(argument);
                        newExpression.setConstructor(constructor);
                    }


                    return newExpression;
                }

                Method method = argument.length == 0 ? object.getType().getMethod(sig.name) : object.getType().getMethod(sig.name, argumentClass);
                //如果是Bool值 且不是参数值 直接取
                if (method.equals(Boolean.class.getMethod("booleanValue")) && !(object instanceof ParameterExpression))
                    return object;

                return Expression.call(argument, method, object);

            } catch (NoSuchMethodException e) {

                //装箱方法 直接作为静态值处理
                if (MethodChecker.getInstance().boxingFunction.contains(sig)) {
                    return object;
                }

                throw new TypedValueVisitorException("无法获取方法", e);
            }
        }
    }

    /**
     * 访问常量表达式
     *
     * @param val 常量值
     * @param in  一并传入
     * @return 常量表达式
     */
    @Override
    public Expression constantValue(ConstantValue val, Map<String, ParameterExpression> in) {
        Object value = val.getConstant();

        return Expression.constant(value);
    }

    /**
     * 获取静态字段值
     *
     * @param val 值
     * @param in  一并传入
     * @return 常量表达式
     */
    @Override
    public Expression getStaticFieldValue(TypedValue.GetStaticFieldValue val, Map<String, ParameterExpression> in) throws TypedValueVisitorException {

        if (MethodChecker.getInstance().enums.containsKey(val.owner)) {
            String fullName = this.getFullEnumConstantName(val.owner, val.name);
            Integer ordinal = this.getEnumConstantOrdinal(val.owner, val.name);
            if (fullName != null)
                return Expression.constant(ordinal);
        }
        //布尔值也会被包装成这静态字段
        else if ("java/lang/Boolean".equals(val.owner)) {
            if ("TRUE".equals(val.name) || "FALSE".equals(val.name))
                return Expression.constant("TRUE".equals(val.name) ? "TRUE" : "FALSE");
        }
        return this.defaultValue(val, in);
    }

    /**
     * 获取枚举的名称
     *
     * @param className 类名
     * @param name      名称
     * @return 枚举的名称
     */
    private String getFullEnumConstantName(String className, String name) {
        List<Enum<?>> enumConstants = MethodChecker.getInstance().enums.get(className);
        if (enumConstants == null) return null;
        for (Enum<?> e : enumConstants) {
            if (e.name().equals(name))
                return className.replace("/", ".") + "." + name;
        }
        return null;
    }

    /**
     * 获取枚举的排序
     *
     * @param className 枚举的类名称
     * @param name      枚举的名称
     * @return 排序
     */
    private Integer getEnumConstantOrdinal(String className, String name) {
        List<Enum<?>> enumConstants = MethodChecker.getInstance().enums.get(className);
        if (enumConstants == null) return null;
        for (Enum<?> e : enumConstants) {
            if (e.name().equals(name)) {
                return e.ordinal();
            }
        }
        return null;
    }

    /**
     * 访问参数表达式
     *
     * @param val 值
     * @param in  一并传入
     * @return 参数表达式
     */
    @Override
    public Expression argValue(TypedValue.ArgValue val, Map<String, ParameterExpression> in) throws TypedValueVisitorException {

        //参数名
        String name = val.toString();
        try {
            Class<?> type;
            if (val.isPrimitive()) {
                type = this.getTypeByName(val.getType());
            } else {
                type = Class.forName(val.getType().getClassName());
            }
            Object obj = null;
            int index = val.getIndex();
            ConstantExpression constantExpression = null;
            boolean isHost = true;
            if (this.realArguments.containsKey(val.getIndex())) {
                obj = this.realArguments.get(val.getIndex());
                if (type.isEnum()) {
                    //如果是枚举对象 则处理为ordinal
                    List<Enum<?>> enumConstants = MethodChecker.getInstance().enums.get(val.getType().getInternalName());
                    for (Enum<?> en : enumConstants) {
                        if (en.toString().equals(obj.toString())) {
                            obj = en.ordinal();
                        }
                    }
                }

                constantExpression = Expression.constant(obj);
                isHost = false;
            }
            ParameterExpression parameter = Expression.parameter(name, type, obj, isHost);
            parameter.setIndex(index);
            if (!in.containsKey(name))
                in.put(name, parameter);
            if (constantExpression != null)
                return constantExpression;
            else
                return parameter;
        } catch (ClassNotFoundException e) {
            throw new TypedValueVisitorException("无法获取参数类型", e);
        }
    }

    /**
     * 访问算数一元操作
     *
     * @param val 值
     * @param in  一并传入
     * @return 一元表达式
     */
    @Override
    public Expression unaryMathOpValue(TypedValue.UnaryMathOpValue val, Map<String, ParameterExpression> in) throws TypedValueVisitorException {
        Expression operand = val.operand.visit(this, in);

        if (val.op == TypedValue.UnaryMathOpValue.UnaryOp.neg) {
            return Expression.negate(operand);
        } else {
            throw new IllegalArgumentException("非一元取反: " + val.op);
        }
    }

    /**
     * 访问一元逻辑变表达式
     *
     * @param val 值
     * @param in  一并传入
     * @return 一元表达式
     */
    @Override
    public Expression notOpValue(TypedValue.NotValue val, Map<String, ParameterExpression> in) throws TypedValueVisitorException {
        Expression operand = val.operand.visit(this, in);

        return Expression.not(operand);
    }

    /**
     * 访问算数表达式
     *
     * @param val 值
     * @param in  一并传入
     * @return 二元表达式
     */
    @Override
    public Expression mathOpValue(TypedValue.MathOpValue val, Map<String, ParameterExpression> in) throws TypedValueVisitorException {

        Expression left = val.left.visit(this, in);
        Expression right = val.right.visit(this, in);

        Class<?> type;
        if (val.isPrimitive())
            type = this.getTypeByName(val.getType());
        else {
            try {
                type = Class.forName(val.getType().getClassName());
            } catch (ClassNotFoundException e) {
                throw new TypedValueVisitorException("无法加载类" + val.getType().getClassName() + e);
            }
        }
        BinaryExpression binaryExpression = null;
        switch (val.op) {
            case plus:
                binaryExpression = Expression.add(left, right, type);
                break;
            case minus:
                binaryExpression = Expression.subtract(left, right, type);
                break;
            case mul:
                binaryExpression = Expression.multi(left, right, type);
                break;
            case div:
                binaryExpression = Expression.divide(left, right, type);
                break;
            case mod:
                binaryExpression = Expression.modula(left, right, type);
                break;
            case cmp:
                throw new TypedValueVisitorException("非法的表达式,比较操作不应出现在算数表达式内.");
        }

        return binaryExpression;
    }

    /**
     * 根据Jinq定义的类型获取Class
     *
     * @param type Jinq定义的类型
     * @return Class
     */
    private Class<?> getTypeByName(Type type) {
        String typeName = type.getClassName();

        switch (typeName) {
            case "boolean": {
                return Boolean.class;
            }
            case "short": {
                return Short.class;
            }
            case "int": {
                return Integer.class;
            }
            case "float": {
                return Float.class;
            }
            case "long": {
                return Long.class;
            }
            case "double": {
                return Double.class;
            }
            default:
                throw new IllegalArgumentException("无法解析的类型: " + typeName);
        }
    }

    /**
     * 访问静态方法表达式
     *
     * @param val 值
     * @param in  一并传入
     * @return 表达式
     * @throws TypedValueVisitorException 异常
     */
    @Override
    public Expression staticMethodCallValue(MethodCallValue.StaticMethodCallValue val, Map<String, ParameterExpression> in) throws TypedValueVisitorException {
        MethodSignature sig = val.getSignature();

        if (MethodChecker.getInstance().valueOfFunction.contains(sig)) {
            return val.args.get(0).visit(this, in);
        }

        throw new IllegalArgumentException("无法解析的静态方法: " + sig);
    }

    /**
     * 重写访问字段方法
     *
     * @param val 值
     * @param in  一并传入
     * @return 成员访问表达式
     * @throws TypedValueVisitorException 异常
     */
    @Override
    public Expression getFieldValue(TypedValue.GetFieldValue val, Map<String, ParameterExpression> in) throws TypedValueVisitorException {
        ParameterExpression parameter = (ParameterExpression) val.operand.visit(this, in);
        //首字母大写
        char[] cs = val.name.toCharArray();
        cs[0] = Character.toUpperCase(cs[0]);
        String realName = "get" + String.valueOf(cs);

        try {
            //如果使用的是方法引用 或者 此类型未在模型中注册 则抛出异常
            if (Modifier.isAbstract(parameter.getType().getModifiers()) || !MethodChecker.getInstance().isRegisteredClass(parameter.getType())) {
                throw new IllegalArgumentException(String.format("此属性定义于父类中,请使用形如p->p.%s()方法调用.", realName));
            }

            Method filedMethod = parameter.getType().getMethod(realName);

            return Expression.member(parameter, filedMethod, parameter, parameter.getType());

        } catch (NoSuchMethodException e) {
            throw new TypedValueVisitorException("无法获取成员访问", e);
        }
    }

    /**
     * 从写访问this方法
     *
     * @param val 值
     * @param in  一并传入
     * @return 参数表达式
     */
    @Override
    public Expression thisValue(TypedValue.ThisValue val, Map<String, ParameterExpression> in) throws TypedValueVisitorException {
        //参数名
        String name = val.toString();
        try {
            Class<?> thisClazz = Class.forName(val.getType().getClassName());
            ParameterExpression parameter = Expression.parameter(name, thisClazz, null, true);
            if (!in.containsKey(name))
                in.put(name, parameter);
            return parameter;
        } catch (ClassNotFoundException e) {
            throw new TypedValueVisitorException("无法获取参数类型", e);
        }
    }

    /**
     * 访问构造函数表达式
     *
     * @param val 值
     * @param in  一并传入
     * @return 表达式
     */
    @Override
    public Expression newValue(TypedValue.NewValue val, Map<String, ParameterExpression> in) throws TypedValueVisitorException {

        try {
            return Expression.news(Class.forName(val.getType().getClassName()));
        } catch (ClassNotFoundException e) {
            throw new TypedValueVisitorException("无法获取构造函数类型", e);
        }
    }

    /**
     * 访问一元操作
     *
     * @param val 值
     * @param in  一并传入
     * @return 表达式
     */
    @Override
    public Expression unaryOpValue(TypedValue.UnaryOperationValue val, Map<String, ParameterExpression> in) throws TypedValueVisitorException {
        Expression operandExpression = val.operand.visit(this, in);
        try {
            //如果这是一个转换表达式套在外面 且 内部是一个访问表达式
            //就需要记录下转换的对象类型
            Class<?> convertType = Class.forName(val.getType().getClassName());
            if (!operandExpression.getType().equals(convertType)) {
                UnaryExpression unaryExpression = Expression.convert(operandExpression);
                unaryExpression.setConvertType(convertType);
                return unaryExpression;
            }
        } catch (ClassNotFoundException e) {
            return operandExpression;
        }
        return operandExpression;
    }
}