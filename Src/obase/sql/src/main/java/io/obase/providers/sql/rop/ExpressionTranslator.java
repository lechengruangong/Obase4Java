/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表达式翻译器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 12:14:34
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.common.ObjectReferencePack;
import io.obase.core.SubTreeEvaluator;
import io.obase.core.expression.BinaryExpression;
import io.obase.core.expression.ConstantExpression;
import io.obase.core.expression.EExpressionType;
import io.obase.core.expression.Expression;
import io.obase.core.expression.ExpressionVisitor;
import io.obase.core.expression.UnaryExpression;
import io.obase.core.expression.*;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.*;
import io.obase.providers.sql.AliasGenerator;
import io.obase.providers.sql.sqlobject.*;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 表达式翻译器
 */
public class ExpressionTranslator extends ExpressionVisitor {

    /**
     * 数据对象模型
     */
    private final ObjectDataModel model;

    /**
     * 子树求值器
     */
    private final SubTreeEvaluator subTreeEvaluator;
    /**
     * 形参绑定
     */
    private final ParameterBinding[] parameterBindings;
    /**
     * 表达式
     */
    private io.obase.providers.sql.sqlobject.Expression expression;

    /**
     * 构造ExpressionTranslator的新实例
     *
     * @param model             对象数据模型
     * @param subTreeEvaluator  子树求值器
     * @param parameterBindings 形参绑定
     */
    public ExpressionTranslator(ObjectDataModel model, SubTreeEvaluator subTreeEvaluator,
                                ParameterBinding[] parameterBindings) {
        this.subTreeEvaluator = subTreeEvaluator;
        this.model = model;
        this.parameterBindings = parameterBindings;
    }

    /**
     * 翻译指定的表达式
     *
     * @param expression Lambda表达式
     * @return Sql表达式
     */
    public io.obase.providers.sql.sqlobject.Expression translate(Expression expression) {
        if (expression.getExpressionType() == EExpressionType.Lambda) {
            LambdaExpression lambda = (LambdaExpression) expression;
            this.visit(lambda.getBody());
        } else {
            this.visit(expression);
        }

        return this.expression;
    }

    /**
     * 默认的访问成员表达式
     * 访问成员表达式的Expression 而后返回自身
     *
     * @param memberExpression 成员表达式
     * @return 成员表达式自身
     */
    @Override
    protected Expression visitMember(MemberExpression memberExpression) {
        Class<?> type = memberExpression.getHost() != null ? memberExpression.getHost().getType() : memberExpression.getExpression().getType();
        if (type != String.class && Iterable.class.isAssignableFrom(type)) {
            type = (Class<?>) (((ParameterizedType) (((ParameterizedType) type.getGenericSuperclass()).getActualTypeArguments()[0])).getRawType());
        }

        //取模型类型
        StructuralType modelType = this.model.getStructuralType(type);
        if (modelType != null) {
            Field targetField = MemberExpressionExtension.generateField(memberExpression, this.model, this.parameterBindings);
            this.expression = io.obase.providers.sql.sqlobject.Expression.field(targetField);
            return memberExpression;
        }

        //未注册 生成func表达式
        Expression hostObj = memberExpression.getExpression();
        String member = memberExpression.getMemberName();

        if (member.equals("Length")) {

            this.visit(this.subTreeEvaluator.evaluate(hostObj));
            io.obase.providers.sql.sqlobject.Expression arg = this.expression;
            this.expression = io.obase.providers.sql.sqlobject.Expression.function("len", arg);
        }

        return memberExpression;
    }

    /**
     * 默认的访问方法调用表达式
     * 先访问Object然后挨个访问Argument
     * 最后返回自身
     *
     * @param methodCallExpression Lambda表达式
     * @return 自身
     */
    @Override
    protected Expression visitMethodCall(MethodCallExpression methodCallExpression) {
        this.expression = this.callTranslate(methodCallExpression);
        return methodCallExpression;
    }

    /**
     * 默认的访问常量表达式
     * 直接返回自身
     *
     * @param constantExpression 常量表达式
     * @return 常量表达式自身
     */
    @Override
    protected Expression visitConstant(ConstantExpression constantExpression) {
        this.expression = io.obase.providers.sql.sqlobject.Expression.constant(constantExpression.getValue());
        return constantExpression;
    }

    /**
     * 默认访问一元表达式
     * 先访问Operand 先后返回自身
     *
     * @param unaryExpression 一元表达式
     * @return 一元表达式自身
     */
    @Override
    protected Expression visitUnary(UnaryExpression unaryExpression) {
        Expression operand = this.subTreeEvaluator.evaluate(unaryExpression.getOperand());
        this.visit(operand);
        io.obase.providers.sql.sqlobject.Expression operandExp = this.expression;
        switch (unaryExpression.getExpressionType()) {
            case Decrement:
                this.expression = io.obase.providers.sql.sqlobject.Expression.decrement(operandExp);
                break;
            case Increment:
                this.expression = io.obase.providers.sql.sqlobject.Expression.increment(operandExp);
                break;
            case Negate:
            case NegateChecked:
                this.expression = io.obase.providers.sql.sqlobject.Expression.negate(operandExp);
                break;
            case UnaryPlus:
                this.expression = io.obase.providers.sql.sqlobject.Expression.unaryPlus(operandExp);
                break;
            case Not: {
                if (operandExp instanceof InExpression) {
                    InExpression inExpression = (InExpression) operandExp;
                    inExpression.flipOverOperator();
                } else {
                    this.expression = (unaryExpression.getType() == boolean.class || unaryExpression.getType() == Boolean.class) ? io.obase.providers.sql.sqlobject.Expression.not(operandExp) : io.obase.providers.sql.sqlobject.Expression.bitNot(operandExp);
                }
                break;
            }
            case Convert: {
                //int16
                if (unaryExpression.getType() == Short.class)
                    this.expression = io.obase.providers.sql.sqlobject.Expression.function("CONVERT", io.obase.providers.sql.sqlobject.Expression.constant("smallint"), operandExp);
                //int32
                if (unaryExpression.getType() == Integer.class)
                    this.expression = io.obase.providers.sql.sqlobject.Expression.function("CONVERT", io.obase.providers.sql.sqlobject.Expression.constant("int"), operandExp);
                //int64
                if (unaryExpression.getType() == Long.class)
                    this.expression = io.obase.providers.sql.sqlobject.Expression.function("CONVERT", io.obase.providers.sql.sqlobject.Expression.constant("bigint"), operandExp);
                //byte
                if (unaryExpression.getType() == Byte.class)
                    this.expression = io.obase.providers.sql.sqlobject.Expression.function("CONVERT", io.obase.providers.sql.sqlobject.Expression.constant("binary"), operandExp);
                break;
            }
            default:
                throw new IllegalArgumentException("未知的一元表达式类型: " + unaryExpression.getExpressionType());
        }

        return unaryExpression;
    }

    /**
     * 默认的访问二元表达式
     * 先访问左端 然后访问右端 最后返回右端的访问结果
     *
     * @param binaryExpression 二元表达式
     * @return 二元表达式的右端
     */
    @Override
    protected Expression visitBinary(BinaryExpression binaryExpression) {
        Expression nodeLeft = this.subTreeEvaluator.evaluate(binaryExpression.getLeft());
        this.visit(nodeLeft);
        io.obase.providers.sql.sqlobject.Expression left = this.expression;

        Expression nodeRight = this.subTreeEvaluator.evaluate(binaryExpression.getRight());
        this.visit(nodeRight);
        io.obase.providers.sql.sqlobject.Expression right = this.expression;

        switch (binaryExpression.getExpressionType()) {
            case Add:
                this.expression = io.obase.providers.sql.sqlobject.Expression.add(left, right);
                break;
            case Subtract:
                this.expression = io.obase.providers.sql.sqlobject.Expression.subtract(left, right);
                break;
            case Multiply:
                this.expression = io.obase.providers.sql.sqlobject.Expression.multiply(left, right);
                break;
            case Divide:
                this.expression = io.obase.providers.sql.sqlobject.Expression.divide(left, right);
                break;
            case Power:
                this.expression = io.obase.providers.sql.sqlobject.Expression.power(left, right);
                break;
            case Modula:
                this.expression = io.obase.providers.sql.sqlobject.Expression.modulo(left, right);
                break;
            case AndAlso:
                this.expression = io.obase.providers.sql.sqlobject.Expression.andAlso(left, right);
                break;
            case OrElse:
                this.expression = io.obase.providers.sql.sqlobject.Expression.orElse(left, right);
                break;
            case Equal: {
                if (left instanceof InExpression) {
                    InExpression inExp = (InExpression) left;
                    if (right instanceof io.obase.providers.sql.sqlobject.ConstantExpression) {
                        io.obase.providers.sql.sqlobject.ConstantExpression constantExpression = (io.obase.providers.sql.sqlobject.ConstantExpression) right;
                        if (!(boolean) constantExpression.getValue())
                            inExp.flipOverOperator();
                    }

                    this.expression = left;
                } else {
                    this.expression = io.obase.providers.sql.sqlobject.Expression.equal(left, right);
                }
                break;
            }
            case NotEqual: {
                if (left instanceof InExpression) {
                    InExpression inExp = (InExpression) left;
                    if (right instanceof io.obase.providers.sql.sqlobject.ConstantExpression) {
                        io.obase.providers.sql.sqlobject.ConstantExpression constantExpression = (io.obase.providers.sql.sqlobject.ConstantExpression) right;
                        if ((boolean) constantExpression.getValue())
                            inExp.flipOverOperator();
                    }

                    this.expression = left;
                } else {
                    this.expression = io.obase.providers.sql.sqlobject.Expression.notEqual(left, right);
                }
                break;
            }
            case GreaterThan:
                this.expression = io.obase.providers.sql.sqlobject.Expression.greaterThan(left, right);
                break;
            case GreaterThanOrEqual:
                this.expression = io.obase.providers.sql.sqlobject.Expression.greaterThanOrEqual(left, right);
                break;
            case LessThan:
                this.expression = io.obase.providers.sql.sqlobject.Expression.lessThan(left, right);
                break;
            case LessThanOrEqual:
                this.expression = io.obase.providers.sql.sqlobject.Expression.lessThanOrEqual(left, right);
                break;
            case And:
                this.expression = io.obase.providers.sql.sqlobject.Expression.bitAnd(left, right);
                break;
            case Or:
                this.expression = io.obase.providers.sql.sqlobject.Expression.bitOr(left, right);
                break;
            case ExclusiveOr:
                this.expression = io.obase.providers.sql.sqlobject.Expression.bitXor(left, right);
                break;
            case LeftShift:
                this.expression = io.obase.providers.sql.sqlobject.Expression.leftShift(left, right);
                break;
            case RightShift:
                this.expression = io.obase.providers.sql.sqlobject.Expression.rightShift(left, right);
                break;
            default:
                throw new IllegalArgumentException("未知的二元表达式类型: " + binaryExpression.getExpressionType());
        }

        return binaryExpression;
    }

    /**
     * 默认的访问参数表达式
     * 直接返回自身
     *
     * @param parameterExpression 参数表达式
     * @return 常量表达式自身
     */
    @Override
    protected Expression visitParameter(ParameterExpression parameterExpression) {
        ParameterBinding binding = null;
        if (this.parameterBindings != null) {
            Optional<ParameterBinding> parameterBinding = Arrays.stream(this.parameterBindings).filter(p -> p.getParameter() == parameterExpression).findFirst();
            if (parameterBinding.isPresent())
                binding = parameterBinding.get();
        }

        if (binding != null) {
            switch (binding.getReferring()) {
                case Single:
                case Sequence: {
                    this.visit(binding.getExpression());
                    break;
                }
                case Index: {
                    Field filed = new Field("obase$index");
                    this.expression = io.obase.providers.sql.sqlobject.Expression.field(filed);
                    break;
                }
            }
        } else {
            Field filed = new Field("obase$result");
            this.expression = io.obase.providers.sql.sqlobject.Expression.field(filed);
        }

        return parameterExpression;
    }

    /**
     * 默认的访问Lambda表达式
     * 先访问Body 然后挨个访问Parameter
     * 最后返回自身
     *
     * @param lambdaExpression Lambda表达式
     * @return 自身
     */
    @Override
    protected Expression visitLambda(LambdaExpression lambdaExpression) {
        return this.visit(lambdaExpression.getBody());
    }

    /**
     * 翻译调用表达式
     *
     * @param expression 调用表达式
     * @return Sql表达式
     */
    private io.obase.providers.sql.sqlobject.Expression callTranslate(MethodCallExpression expression) {
        //表达式的实例值
        Expression objectValue = expression.getObject() != null ? this.subTreeEvaluator.evaluate(expression.getObject()) : this.subTreeEvaluator.evaluate(expression.getArgument()[0]);
        //表达式的参数值
        Expression argsValue = null;
        if (expression.getArgument().length > 1)
            argsValue = this.subTreeEvaluator.evaluate(expression.getArgument()[1]);
        else if (expression.getArgument().length > 0)
            argsValue = this.subTreeEvaluator.evaluate(expression.getArgument()[0]);

        //翻译表达式
        io.obase.providers.sql.sqlobject.Expression host = objectValue != null ? this.translate(objectValue) : (argsValue != null ? this.translate(argsValue) : null);
        List<io.obase.providers.sql.sqlobject.Expression> args = new ArrayList<>();
        if (expression.getObject() == null) {
            for (int i = 1; i < expression.getArgument().length; i++) {
                args.add(this.translate(this.subTreeEvaluator.evaluate(expression.getArgument()[i])));
            }
        } else {
            for (int i = 0; i < expression.getArgument().length; i++) {
                args.add(this.translate(this.subTreeEvaluator.evaluate(expression.getArgument()[i])));
            }
        }

        //翻译Contains StartsWith EndsWith
        if (expression.getMethod().getName().equalsIgnoreCase("contains") || expression.getMethod().getName().equalsIgnoreCase("startsWith") ||
                expression.getMethod().getName().equalsIgnoreCase("endsWith"))
            return this.translateContains(expression, objectValue, argsValue, host, args);

        //翻译聚合函数
        if (expression.getMethod().getName().equalsIgnoreCase("avg") || expression.getMethod().getName().equalsIgnoreCase("count") ||
                expression.getMethod().getName().equalsIgnoreCase("max")
                || expression.getMethod().getName().equalsIgnoreCase("min") || expression.getMethod().getName().equalsIgnoreCase("sum")
                || expression.getMethod().getName().equalsIgnoreCase("countLong") || expression.getMethod().getName().equalsIgnoreCase("sumLong")
                || expression.getMethod().getName().equalsIgnoreCase("sumDouble") || expression.getMethod().getName().equalsIgnoreCase("minLong")
                || expression.getMethod().getName().equalsIgnoreCase("minDouble") || expression.getMethod().getName().equalsIgnoreCase("maxLong")
                || expression.getMethod().getName().equalsIgnoreCase("maxDouble") || expression.getMethod().getName().equalsIgnoreCase("avgDouble")) {
            //参数表达式
            Expression argExp = null;
            //形参绑定表达式
            Expression bindingExp = null;

            //根据参数个数分别处理
            if (expression.getArgument().length >= 2) {
                argExp = expression.getArgument()[0];
                bindingExp = expression.getArgument()[1];
            } else if (expression.getArgument().length > 0) {
                Expression targetExp = expression.getArgument()[0];
                //如果是参数表达式
                if (targetExp instanceof ParameterExpression) {
                    //赋值给argExp
                    argExp = targetExp;
                }
                //不是参数表达式 但是是Select方法
                else if (targetExp instanceof MethodCallExpression) {
                    MethodCallExpression methodCallExpression = (MethodCallExpression) targetExp;
                    if (methodCallExpression.getMethod().getName().equals("Select")) {
                        argExp = methodCallExpression.getArgument()[0];
                        bindingExp = methodCallExpression.getArgument()[1];
                    }
                }
            }

            //生成形参绑定
            if (argExp instanceof LambdaExpression && bindingExp != null) {
                LambdaExpression lambdaExpression = (LambdaExpression) argExp;
                this.generateParameterBinding(bindingExp, lambdaExpression.getParameters()[0]);
            }

            TypeBase argType = argExp == null ? null : this.model.getTypeOrNull(argExp.getType());
            if (expression.getMethod().getName().equals("length") && argType instanceof StructuralType) {
                ObjectReferencePack<AssociationTreeNode> assoTail = new ObjectReferencePack<>();
                ObjectReferencePack<AttributeTreeNode> attrTail = new ObjectReferencePack<>();
                //提取关联树
                AssociationTree associationTree = expression.extractAssociation(this.model, assoTail,
                        attrTail, this.parameterBindings);

                //取目标名和键字段
                String targetName = ((ObjectType) assoTail.realValue.getRepresentedType()).getTargetName();
                List<String> keyField = ((ObjectType) assoTail.realValue.getRepresentedType()).getKeyFields();

                //生成源
                String alias = associationTree.accept(new AliasGenerator());
                SimpleSource source = new SimpleSource(targetName, alias);

                //表达式
                FieldExpression[] filedExps;
                if (attrTail.realValue != null) {
                    //生长属性树
                    AttributeTree attrTree = attrTail.realValue.asTree();
                    attrTree.accept(new AttributeTreeGrower());
                    filedExps = attrTree.accept(new CountingFieldGenerator(source));
                } else {
                    filedExps = (FieldExpression[]) keyField.stream().map(key -> new FieldExpression(new Field(source, key))).toArray();
                }

                //加入true 表示Distinct
                List<io.obase.providers.sql.sqlobject.Expression> realExps = new ArrayList<>(Arrays.asList(filedExps));
                realExps.add(io.obase.providers.sql.sqlobject.Expression.constant(true));
                //组成方法调用表达式
                return io.obase.providers.sql.sqlobject.Expression.function(expression.getMethod().getName().toLowerCase().contains("avg") ? "Avg" : expression.getMethod().getName(), realExps.toArray(new io.obase.providers.sql.sqlobject.Expression[0]));
            }

            this.visit(argExp);

            return io.obase.providers.sql.sqlobject.Expression.function(this.convertMethodName(expression.getMethod().getName()), this.expression);
        }

        //其他简单函数
        switch (expression.getMethod().getName().toLowerCase()) {
            case "tostring":
                return io.obase.providers.sql.sqlobject.Expression.function("CONVERT", io.obase.providers.sql.sqlobject.Expression.constant("varchar"), host);
            case "substring":
                args.set(0, io.obase.providers.sql.sqlobject.Expression.add(args.get(0), io.obase.providers.sql.sqlobject.Expression.constant(0)));
                return io.obase.providers.sql.sqlobject.Expression.function("SUBSTRING", host, args.get(0), args.get(1));
            case "indexof":
                args.set(0, io.obase.providers.sql.sqlobject.Expression.add(io.obase.providers.sql.sqlobject.Expression.constant("%"), args.get(0)));
                args.set(0, io.obase.providers.sql.sqlobject.Expression.add(args.get(0), io.obase.providers.sql.sqlobject.Expression.constant("%")));
                io.obase.providers.sql.sqlobject.Expression e = io.obase.providers.sql.sqlobject.Expression.function("PATINDEX", args.get(0), host);
                return io.obase.providers.sql.sqlobject.Expression.subtract(e, io.obase.providers.sql.sqlobject.Expression.constant(1));
            case "toupper":
                return io.obase.providers.sql.sqlobject.Expression.function("UPPER", host);
            case "tolower":
                return io.obase.providers.sql.sqlobject.Expression.function("LOWER", host);
            case "toboolean":
                return io.obase.providers.sql.sqlobject.Expression.function("CONVERT", io.obase.providers.sql.sqlobject.Expression.constant("bit"), host);
            case "tobyte":
                return io.obase.providers.sql.sqlobject.Expression.function("CONVERT", io.obase.providers.sql.sqlobject.Expression.constant("binary"), host);
            case "toshort":
                return io.obase.providers.sql.sqlobject.Expression.function("CONVERT", io.obase.providers.sql.sqlobject.Expression.constant("smallint"), host);
            case "toint":
                return io.obase.providers.sql.sqlobject.Expression.function("CONVERT", io.obase.providers.sql.sqlobject.Expression.constant("int"), host);
            case "tolong":
                return io.obase.providers.sql.sqlobject.Expression.function("CONVERT", io.obase.providers.sql.sqlobject.Expression.constant("bigint"), host);
            case "tosingle":
                return io.obase.providers.sql.sqlobject.Expression.function("CONVERT", io.obase.providers.sql.sqlobject.Expression.constant("real"), host);
            case "todouble":
                return io.obase.providers.sql.sqlobject.Expression.function("CONVERT", io.obase.providers.sql.sqlobject.Expression.constant("float"), host);
            case "todatetime":
                return io.obase.providers.sql.sqlobject.Expression.function("CONVERT", io.obase.providers.sql.sqlobject.Expression.constant("datetime"), host);
            case "todecimal":
                return io.obase.providers.sql.sqlobject.Expression.function("CONVERT", io.obase.providers.sql.sqlobject.Expression.constant("numeric"), host);
            case "tochar":
                return io.obase.providers.sql.sqlobject.Expression.function("CONVERT", io.obase.providers.sql.sqlobject.Expression.constant("char"), host);
            case "abs":
                return io.obase.providers.sql.sqlobject.Expression.function("ABS", host);
            case "acos":
                return io.obase.providers.sql.sqlobject.Expression.function("Acos", host);
            case "asin":
                return io.obase.providers.sql.sqlobject.Expression.function("Asin", host);
            case "atan":
                return io.obase.providers.sql.sqlobject.Expression.function("Atan", host);
            case "atan2":
                return io.obase.providers.sql.sqlobject.Expression.function("Atan2", host);
            case "ceiling":
                return io.obase.providers.sql.sqlobject.Expression.function("Ceiling", host);
            case "cos":
                return io.obase.providers.sql.sqlobject.Expression.function("Cos", host);
            case "exp":
                return io.obase.providers.sql.sqlobject.Expression.function("Exp", host);
            case "floor":
                return io.obase.providers.sql.sqlobject.Expression.function("Floor", host);
            case "log":
                return io.obase.providers.sql.sqlobject.Expression.function("Log", host);
            case "pow":
                return io.obase.providers.sql.sqlobject.Expression.function("Pow", host);
            case "round":
                return io.obase.providers.sql.sqlobject.Expression.function("Round", host);
            case "sin":
                return io.obase.providers.sql.sqlobject.Expression.function("Sin", host);
            case "sqrt":
                return io.obase.providers.sql.sqlobject.Expression.function("Sqrt", host);
            case "tan":
                return io.obase.providers.sql.sqlobject.Expression.function("Tan", host);
            default:
                throw new IllegalArgumentException("无法将" + expression.getMethod().getName() + "方法翻译成Sql函数");
        }
    }

    /**
     * 将方法名称转换为Sql的方法名
     *
     * @param name 名称
     * @return Sql的方法名
     */
    private String convertMethodName(String name) {
        switch (name.toLowerCase()) {
            case "countlong":
            case "countdouble":
                return "Count";
            case "sumlong":
            case "sumdouble":
                return "Sum";
            case "minlong":
            case "mindouble":
                return "Min";
            case "maxlong":
            case "maxdouble":
                return "Max";
            case "avgdouble":
                return "Avg";
            default:
                throw new IllegalArgumentException("无法解析的Sql函数");
        }
    }

    /**
     * 生成形参绑定
     *
     * @param bindingExp 绑定的表达式
     * @param parameter  参数表达式
     */
    private void generateParameterBinding(Expression bindingExp, ParameterExpression parameter) {
        ParameterBinding newParameterBinding = null;
        ParameterExpression subParameter = null;

        if (bindingExp instanceof ParameterExpression) {
            ParameterExpression parameterExp = (ParameterExpression) bindingExp;
            if (this.parameterBindings != null) {
                Optional<ParameterBinding> parabinding = Arrays.stream(this.parameterBindings).filter(p -> p.getExpression() == parameterExp).findFirst();
                if (parabinding.isPresent()) {
                    newParameterBinding = new ParameterBinding(parameter, parabinding.get().getReferring(), parabinding.get().getExpression());
                }
            }
        } else if (bindingExp instanceof MethodCallExpression) {
            MethodCallExpression callExpression = (MethodCallExpression) bindingExp;
            if (callExpression.getMethod().getName().equals("Select")) {
                if (callExpression.getArgument()[0] instanceof ParameterExpression && callExpression.getArgument()[1] instanceof LambdaExpression) {
                    newParameterBinding = new ParameterBinding((ParameterExpression) callExpression.getArgument()[0], ((LambdaExpression) callExpression.getArgument()[1]).getBody());
                    subParameter = ((LambdaExpression) callExpression.getArgument()[1]).getParameters()[0];
                }
            }
        }

        //加入形参绑定
        List<ParameterBinding> tempList = new ArrayList<>();
        if (this.parameterBindings != null)
            tempList.addAll(Arrays.asList(this.parameterBindings));

        tempList.add(newParameterBinding);
        if (this.parameterBindings != null) {
            tempList.toArray(this.parameterBindings);
        }
        //如果有下一级
        if (subParameter != null)
            //递归调用
            this.generateParameterBinding(bindingExp, subParameter);
    }

    /**
     * 翻译包含函数
     *
     * @param expression  表达式
     * @param objectValue 值表达式
     * @param argsValue   参数值表达式
     * @param host        宿主
     * @param args        表达式列表
     * @return 翻译后的表达式
     */
    private io.obase.providers.sql.sqlobject.Expression translateContains(MethodCallExpression expression,
                                                                          Expression objectValue, Expression argsValue,
                                                                          io.obase.providers.sql.sqlobject.Expression host, List<io.obase.providers.sql.sqlobject.Expression> args) {

        //先对Host进行解析 是否为配置的Attribute
        //如果是配置的Attribute 且 配置类型为string 则按照like处理
        boolean isAttributeString = false;
        //按照Member表达式查找
        if (objectValue instanceof MemberExpression) {
            MemberExpression objectValueMember = (MemberExpression) objectValue;
            Class<?> hostType = objectValueMember.getExpression().getType();
            //取模型类型
            StructuralType modelType = this.model.getStructuralType(hostType);
            if (modelType != null) {
                //找到对应的Attribute
                Attribute attribute = modelType.getAttribute(objectValueMember.getMemberName());
                if (attribute != null)
                    //是否是配置成string的Attribute
                    isAttributeString = attribute.getDataType() == String.class;
            }
        }

        //是原生string或者被配置为string 进行模式匹配
        if (argsValue != null && ((objectValue == null ? argsValue : objectValue).getType() == String.class || isAttributeString)) {

            //如果参数是MemberExpression 则表示此表达式是反过来的
            if (argsValue instanceof MemberExpression) {
                //包含 做模式匹配
                if (expression.getMethod().getName().equalsIgnoreCase("contains")) {
                    return io.obase.providers.sql.sqlobject.Expression.like(host, this.translate(argsValue), ELikeType.Contains);
                }

                //开始
                if (expression.getMethod().getName().equalsIgnoreCase("startsWith"))
                    return io.obase.providers.sql.sqlobject.Expression.like(host, this.translate(argsValue), ELikeType.StartWith);

                //结束
                if (expression.getMethod().getName().equalsIgnoreCase("endsWith"))
                    return io.obase.providers.sql.sqlobject.Expression.like(host, this.translate(argsValue), ELikeType.EndWith);
            }

            String containArgs = argsValue.toString().replace("\"", "");
            if (argsValue instanceof ConstantExpression) {
                containArgs = ((ConstantExpression) argsValue).getValue().toString();
            }
            //包含 做模式匹配
            if (expression.getMethod().getName().equalsIgnoreCase("contains")) {
                //自己指定的模式匹配
                if (containArgs.startsWith("%") || containArgs.endsWith("%"))
                    return io.obase.providers.sql.sqlobject.Expression.like(host, args.get(0));

                return io.obase.providers.sql.sqlobject.Expression.like(host, "%" + containArgs + "%");
            }

            //开始 尾加%
            if (expression.getMethod().getName().equalsIgnoreCase("startsWith"))
                return io.obase.providers.sql.sqlobject.Expression.like(host, containArgs + "%");

            //结束 前加%
            if (expression.getMethod().getName().equalsIgnoreCase("endsWith"))
                return io.obase.providers.sql.sqlobject.Expression.like(host, "%" + containArgs);
        }

        if (((objectValue == null ? argsValue : objectValue) instanceof ConstantExpression)) {
            ConstantExpression constantExpression = (ConstantExpression) (objectValue == null ? argsValue : objectValue);
            Object value = constantExpression.getValue();
            if (value instanceof Iterable) {
                return io.obase.providers.sql.sqlobject.Expression.in(args.get(0), (Iterable<Object>) value);
            }
        }
        return null;
    }
}
