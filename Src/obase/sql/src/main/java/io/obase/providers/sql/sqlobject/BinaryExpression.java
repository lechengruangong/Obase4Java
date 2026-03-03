/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示二元运算的表达式.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 16:28:50
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示二元运算的表达式
 */
public abstract class BinaryExpression extends Expression {

    /**
     * 左操作数
     */
    private Expression left;

    /**
     * 右操作数
     */
    private Expression right;

    /**
     * 创建BinaryExpression的实例，并设置Left属性和Right属性的值。
     *
     * @param left  左操作数
     * @param right 右操作数
     */
    protected BinaryExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    /**
     * 获取左操作数
     *
     * @return 左操作数
     */
    public Expression getLeft() {
        return this.left;
    }

    /**
     * 获取右操作数
     *
     * @return 右操作数
     */
    public Expression getRight() {
        return this.right;
    }

    /**
     * 派生类实现此方法以判定具体类型的表达式对象是否相等
     *
     * @param other 要与当前表达式进行比较的表达式
     * @return 是否相等
     */
    @Override
    protected boolean concreteEquals(Expression other) {
        if (other instanceof BinaryExpression) {
            BinaryExpression binaryOther = (BinaryExpression) other;
            return this.getLeft() == binaryOther.getLeft() && this.getRight() == binaryOther.getRight();
        }
        return false;
    }

    /**
     * 生成二元表达式的数据参数
     *
     * @return 数据参数
     */
    protected List<ObjectReferencePack<List<DataParameter>>> genBinaryDataParameter() {
        List<ObjectReferencePack<List<DataParameter>>> result = new ArrayList<>();
        ObjectReferencePack<List<DataParameter>> leftSqlParameter = new ObjectReferencePack<>();
        leftSqlParameter.realValue = new ArrayList<>();
        ObjectReferencePack<List<DataParameter>> rightSqlParameter = new ObjectReferencePack<>();
        rightSqlParameter.realValue = new ArrayList<>();
        result.add(leftSqlParameter);
        result.add(rightSqlParameter);
        return result;
    }

    /**
     * 替换布尔字段表达式
     * 在SqlSever里 不能直接使用布尔字段作为条件表达式 需要转换为等于true的表达式
     * 如果是常量布尔值 则替换为 1=1
     */
    protected void replaceBoolField() {
        //如果是字段表达式 提取其中的字段 与 常量true 组合成结果
        if (this.left instanceof FieldExpression) {
            this.left = equal(this.left, new ConstantExpression(true));
        }
        //如果是常量表达式 且值为布尔类型 则替换为 1=1
        if (this.left instanceof ConstantExpression) {
            ConstantExpression constantExpression = (ConstantExpression) this.left;
            if (constantExpression.getValue() instanceof Boolean) {
                this.left = equal(new ConstantExpression(1), new ConstantExpression(1));
            }
        }

        //如果是字段表达式 提取其中的字段 与 常量true 组合成结果
        if (this.right instanceof FieldExpression) {
            this.right = equal(this.right, new ConstantExpression(true));
        }
        //如果是常量表达式 且值为布尔类型 则替换为 1=1
        if (this.right instanceof ConstantExpression) {
            ConstantExpression constantExpression = (ConstantExpression) this.right;
            if (constantExpression.getValue() instanceof Boolean) {
                this.right = equal(new ConstantExpression(1), new ConstantExpression(1));
            }
        }
    }
}
