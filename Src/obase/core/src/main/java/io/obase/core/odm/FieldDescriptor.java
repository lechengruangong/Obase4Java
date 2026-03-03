/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：字段描述.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 15:08:06
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;


import io.obase.common.FunctionWithNoArg;
import io.obase.core.common.Utils;
import io.obase.core.expression.*;
import io.obase.core.odm.objectSys.EParameterReferring;
import io.obase.core.odm.objectSys.ParameterBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 负责描述类型的字段，提供字段的关键信息。
 */
public class FieldDescriptor {

    /**
     * 表达式文本化器
     */
    private final ExpressionTextualizer expressionTextualizer;

    /**
     * 字段类型
     */
    private final Class<?> type;

    /**
     * 值表达式，该表达式的结果作为字段的值
     */
    private final Expression valueExpression;

    /**
     * 是否在构造函数内创建参数
     */
    private boolean createConstructorParameter;

    /**
     * 指示是否为字段附加取值器
     */
    private boolean hasGetter;

    /**
     * 指示是否为字段附加设值器
     */
    private boolean hasSetter;

    /**
     * 字段名称
     */
    private String name;

    /**
     * 转换为字符串表示形式的结果
     */
    private String toStringResult;

    /**
     * 创建FieldDescriptor实例，该实例描述一个具有指定值表达式的字段
     *
     * @param valueExp     值表达式
     * @param paraBindings 形参绑定
     */
    public FieldDescriptor(Expression valueExp, ParameterBinding[] paraBindings) {

        //如果为LambdaExpression则取Body
        if (valueExp instanceof LambdaExpression) {
            LambdaExpression lambdaExpression = (LambdaExpression) valueExp;
            valueExp = lambdaExpression.getBody();
        }
        this.valueExpression = valueExp;
        this.type = valueExp.getType();

        if (paraBindings != null)
            this.expressionTextualizer = new ExpressionTextualizer(paraBindings);
        else
            this.expressionTextualizer = null;
    }


    /**
     * 创建FieldDescriptor实例，该实例描述一个具有指定类型字段
     *
     * @param type 字段类型
     */
    public FieldDescriptor(Class<?> type) {
        this.type = type;
        this.expressionTextualizer = null;
        this.valueExpression = null;
    }

    /**
     * 创建FieldDescriptor实例，该实例描述一个具有指定名称和类型的字段
     *
     * @param type 字段类型
     * @param name 字段名称
     */
    public FieldDescriptor(Class<?> type, String name) {
        this(type);
        this.name = name;
    }

    /**
     * 获取是否为字段附加取值器
     *
     * @return 是否为字段附加取值器
     */
    public boolean getHasGetter() {
        return this.hasGetter;
    }

    /**
     * 设置是否为字段附加取值器
     *
     * @param hasGetter 是否为字段附加取值器
     */
    public void setHasGetter(boolean hasGetter) {
        this.hasGetter = hasGetter;
    }

    /**
     * 获取是否为字段附加设值器
     *
     * @return 是否为字段附加设值器
     */
    public boolean getHasSetter() {
        return this.hasSetter;
    }

    /**
     * 设置是否为字段附加设值器
     *
     * @param hasSetter 是否为字段附加设值器
     */
    public void setHasSetter(boolean hasSetter) {
        this.hasSetter = hasSetter;
    }

    /**
     * 获取字段名称
     *
     * @return 字段名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取字段类型
     *
     * @return 字段类型
     */
    public Class<?> getType() {
        return this.type;
    }

    /**
     * 获取值表达式，该表达式的结果作为字段的值
     *
     * @return 值表达式
     */
    public Expression getValueExpression() {
        return this.valueExpression;
    }

    /**
     * 获取是否创建构造函数参数
     *
     * @return 是否创建构造函数参数
     */
    public boolean getCreateConstructorParameter() {
        return this.createConstructorParameter;
    }

    /**
     * 设置是否创建构造函数参数
     *
     * @param createConstructorParameter 是否创建构造函数参数
     */
    public void setCreateConstructorParameter(boolean createConstructorParameter) {
        this.createConstructorParameter = createConstructorParameter;
    }

    /**
     * 属性（Property）命名规则
     * 如果字段名以下划线开头，取第二个字符的大写形式（注意，第二个字符可能即为大写），然后串联从第三个开始直到末尾的字符。
     * 如果字段名以“m_”、“M_”、“f_”、“F_”开头，取从第三个字符开始直到末尾的字符。
     *
     * @return 属性名称
     */
    public String getPropertyName() {
        //没有 只能返回空字符串
        if (Utils.getStringIsEmpty(this.name))
            return "";

        //是否以下划线开头
        if (this.name.startsWith("_") && this.name.length() > 2) {
            StringBuilder result = new StringBuilder(String.valueOf(this.name.charAt(1)).toUpperCase());
            //剩下的字符
            char[] remain = this.name.substring(2).toCharArray();
            for (char item : remain) result.append(item);

            return result.toString();
        }

        //以“m_”、“M_”、“f_”、“F_”开头
        if ((this.name.startsWith("m_") || this.name.startsWith("M_") || this.name.startsWith("f_") ||
                this.name.startsWith("F_")) && this.name.length() > 2) {
            StringBuilder result = new StringBuilder();
            //剩下的字符
            char[] remain = this.name.substring(2).toCharArray();
            for (char item : remain) result.append(item);

            return result.toString();
        }

        return this.name;
    }

    /**
     * 转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        //已有结果 直接返回
        if (!Utils.getStringIsEmpty(this.toStringResult))
            return this.toStringResult;
        //没有 但是有Type 且没有name 和 binding
        if (this.type != null && (Utils.getStringIsEmpty(this.name)) && this.expressionTextualizer == null)
            this.toStringResult = this.type.getName();
        else if (this.type != null && !Utils.getStringIsEmpty(this.name) && this.expressionTextualizer == null)
            this.toStringResult = this.type.getName() + "," + this.name;
        else if (this.type != null && !Utils.getStringIsEmpty(this.name)) {
            try {
                this.toStringResult = this.type.getName() + "," + this.name + "," + this.expressionTextualizer.parser(this.valueExpression);
            } catch (Exception e) {
                throw new IllegalArgumentException("字段描述符toString()异常" + e.getMessage(), e);
            }
        }

        return this.toStringResult;
    }

    /**
     * 如果显式指定了名称，直接返回；否则调用namingFunc生成名称，并写入变量_name。
     *
     * @param namingFunc 命名委托
     * @return 获取名称
     */
    public String getName(FunctionWithNoArg<String> namingFunc) {
        //没指定 用命名委托指定
        if (Utils.getStringIsEmpty(this.name))
            this.name = namingFunc.invoke();

        return this.name;
    }

    /**
     * 一个表达式访问者，其功能是将表达式转换为文本表示形式。
     */
    private static class ExpressionTextualizer extends ExpressionVisitor {
        /**
         * 形参绑定
         */
        private final ParameterBinding[] paraBindings;

        /**
         * 文本化的结果
         */
        private String textResult;

        /**
         * 创建ExpressionTextualizer实例
         *
         * @param paraBindings 要转换的表达式的形参绑定
         */
        public ExpressionTextualizer(ParameterBinding[] paraBindings) {
            this.paraBindings = paraBindings;
        }

        /**
         * 对表达式进行文本化
         *
         * @param expression 表达式
         * @return 文本化结果
         */
        public String parser(Expression expression) {
            this.visit(expression);
            return this.textResult;
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
            this.textResult = constantExpression.getValue().toString();
            return Expression.constant(this.textResult);
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
            //访问内部
            this.visit(unaryExpression.getOperand());
            String result = this.textResult;

            //模型化表达式
            switch (unaryExpression.getExpressionType()) {
                case Decrement:
                    this.textResult = result + "-1";
                    break;
                case Increment:
                    this.textResult = result + "+1";
                    break;
                case Negate:
                case NegateChecked:
                    this.textResult = "-" + result;
                    break;
                case UnaryPlus:
                    this.textResult = "+" + result;
                    break;
                case Not:
                    this.textResult = "!" + result;
                    break;
            }

            return Expression.constant(this.textResult);
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
            //对左右求值
            this.visit(binaryExpression.getLeft());
            String leftResult = this.textResult;

            this.visit(binaryExpression.getRight());
            String rightResult = this.textResult;

            String operate = "   ";
            switch (binaryExpression.getExpressionType()) {
                case Add:
                case AddChecked:
                    operate = " + ";
                    break;
                case AddAssign:
                case AddAssignChecked:
                    operate = " += ";
                    break;
                case And:
                    operate = " & ";
                    break;
                case AndAlso:
                    operate = " && ";
                    break;
                case AndAssign:
                    operate = " &= ";
                    break;
                case Divide:
                    operate = " / ";
                    break;
                case DivideAssign:
                    operate = " /= ";
                    break;
                case Equal:
                    operate = " == ";
                    break;
                case Decrement:
                    operate = " - ";
                    break;
                case ExclusiveOr:
                    operate = " ^ ";
                    break;
                case ExclusiveOrAssign:
                    operate = " ^= ";
                    break;
                case GreaterThan:
                    operate = " > ";
                    break;
                case Increment:
                    operate = " + 1";
                    break;
                case LeftShift:
                    operate = " << ";
                    break;
                case LeftShiftAssign:
                    operate = " <<= ";
                    break;
                case GreaterThanOrEqual:
                    operate = " >= ";
                    break;
                case LessThan:
                    operate = " < ";
                    break;
                case LessThanOrEqual:
                    operate = " <= ";
                    break;
                case Modula:
                    operate = " % ";
                    break;
                case ModuloAssign:
                    operate = " %= ";
                    break;
                case MultiplyAssign:
                    operate = " *= ";
                    break;
                case MultiplyAssignChecked:
                    operate = " *= ";
                    break;
                case MultiplyChecked:
                    operate = " * ";
                    break;
                case NotEqual:
                    operate = " != ";
                    break;
                case Or:
                    operate = " | ";
                    break;
                case OrAssign:
                    operate = " |= ";
                    break;
                case OrElse:
                    operate = " || ";
                    break;
                case PostDecrementAssign:
                    operate = " -- ";
                    break;
                case PostIncrementAssign:
                    operate = " ++ ";
                    break;
                case Power:
                    operate = " ^ ";
                    break;
                case PowerAssign:
                    operate = " ^= ";
                    break;
                case RightShift:
                    operate = " >> ";
                    break;
                case RightShiftAssign:
                    operate = " >>= ";
                    break;
                case Subtract:
                    operate = " - ";
                    break;
                case SubtractAssign:
                    operate = " -= ";
                    break;
                case SubtractAssignChecked:
                    operate = " -= ";
                    break;
                case SubtractChecked:
                    operate = " - ";
                    break;
                case Multiply:
                    operate = " * ";
                    break;
            }

            this.textResult = "(" + leftResult + ") " + operate + " (" + rightResult + ")";

            return Expression.constant(this.textResult);
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
            //主体表达式
            this.visit(lambdaExpression.getBody());
            //主体结果
            String bodyResult = this.textResult;
            //每个参数
            List<String> list = new ArrayList<>();

            for (ParameterExpression parameterExpression : lambdaExpression.getParameters()) {
                this.visit(parameterExpression);
                String parameterResult = this.textResult;
                list.add(parameterResult);
            }

            //最终结果
            this.textResult = bodyResult + "(" + String.join(",", list) + ")";

            return Expression.constant(this.textResult);
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
            //搜索绑定
            Optional<ParameterBinding> bindingExp = Arrays.stream(this.paraBindings).filter(p -> p.getParameter() == parameterExpression).findFirst();
            if (!bindingExp.isPresent()) {
                this.textResult = "s";
            } else {
                //如果是索引
                if (bindingExp.get().getReferring() == EParameterReferring.Index) {
                    this.textResult = "index";
                } else {
                    String expResult = "";
                    //绑定的表达式不为空 处理此表达式
                    if (bindingExp.get().getExpression() != null) {
                        this.visit(bindingExp.get().getExpression());
                        expResult = this.textResult;
                    }

                    //不是单个的
                    if (bindingExp.get().getReferring() != EParameterReferring.Single)
                        this.textResult = expResult + "[]";
                }
            }

            return Expression.constant(this.textResult);
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

            this.visit(memberExpression.getExpression());
            String hostStr = this.textResult;
            this.textResult = hostStr + "." + memberExpression.getMemberName();

            return Expression.constant(this.textResult);
        }
    }
}
