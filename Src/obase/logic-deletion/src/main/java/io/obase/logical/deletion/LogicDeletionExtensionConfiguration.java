/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：逻辑删除扩展配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 10:48:44
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.logical.deletion;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.LambdaTranslator;
import io.obase.core.expression.MemberExpression;
import io.obase.core.expression.SerializedFunction;
import io.obase.core.odm.TypeExtension;
import io.obase.core.odm.builder.TypeExtensionConfiguration;

/**
 * 逻辑删除扩展配置
 *
 * @param <TObject> 要配置逻辑删除的类型
 */
public class LogicDeletionExtensionConfiguration<TObject> extends TypeExtensionConfiguration {

    /**
     * 删除标记的映射字段
     */
    private String deletionField;

    /**
     * 逻辑删除标记的属性的名称
     */
    private String deletionMark;

    /**
     * 配置逻辑删除标记的属性的名称
     *
     * @param expression 删除标记的属性的名称
     */
    public void hasDeletionMark(SerializedFunction<TObject, Boolean> expression) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(expression);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();

            this.deletionMark = memberExpression.getMemberName();
        } else {
            throw new IllegalArgumentException("HasDeletionMark只能使用MemberAccessExpression");
        }
    }

    /**
     * 配置逻辑删除标记的属性的名称
     *
     * @param deletionMark 删除标记的属性的名称
     */
    void hasDeletionMark(String deletionMark) {
        this.deletionMark = deletionMark;
    }

    /**
     * 配置删除标记的映射字段
     *
     * @param deletionField 映射字段
     */
    public void hasDeletionField(String deletionField) {
        this.deletionField = deletionField;
    }

    /**
     * 获取类型扩展的类型
     *
     * @return 类型扩展的类型
     */
    @Override
    public Class<? extends TypeExtension> getExtensionType() {
        return LogicDeletionExtension.class;
    }

    /**
     * 根据配置元数据生成类型扩展实例
     *
     * @return 类型扩展实例
     */
    @Override
    public TypeExtension makeExtension() {
        if ((this.deletionMark == null || this.deletionMark.isEmpty()) && (this.deletionField == null || this.deletionField.isEmpty()))
            throw new IllegalArgumentException("逻辑删除标记DeletionMark和逻辑删除字段DeletionField不能同时为空");

        LogicDeletionExtension extension = new LogicDeletionExtension();
        extension.setDeletionMark(this.deletionMark);
        extension.setDeletionField(this.deletionField);

        return extension;
    }
}
