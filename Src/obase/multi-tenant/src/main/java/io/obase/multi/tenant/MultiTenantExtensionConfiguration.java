/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：多租户扩展配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 11:24:18
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.multi.tenant;

import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.LambdaTranslator;
import io.obase.core.expression.MemberExpression;
import io.obase.core.expression.SerializedFunction;
import io.obase.core.odm.TypeExtension;
import io.obase.core.odm.builder.TypeExtensionConfiguration;

import java.util.UUID;

/**
 * 多租户扩展配置
 *
 * @param <TObject> 要配置多租户的类型
 */
public class MultiTenantExtensionConfiguration<TObject> extends TypeExtensionConfiguration {

    /**
     * 全局租户ID
     */
    private Object globalTenantId;

    /**
     * 是否包含全局Id进行查询
     */
    private boolean loadingGlobal;

    /**
     * 多租户标记的映射字段
     */
    private String tenantIdField;

    /**
     * 多租户标记的属性的名称
     */
    private String tenantIdMark;

    /**
     * 多租户的Id类型
     */
    private Class<?> tenantIdType;

    /**
     * 配置多租户标记的属性的名称
     *
     * @param expression 表达式
     */
    public void hasStringTenantIdMark(SerializedFunction<TObject, String> expression) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(expression);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();

            this.tenantIdMark = memberExpression.getMemberName();
            this.tenantIdType = String.class;
        } else {
            throw new IllegalArgumentException("HasDeletionMark只能使用MemberAccessExpression");
        }
    }

    /**
     * 配置多租户标记的属性的名称
     *
     * @param expression 表达式
     */
    public void hasIntTenantIdMark(SerializedFunction<TObject, Integer> expression) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(expression);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();

            this.tenantIdMark = memberExpression.getMemberName();
            this.tenantIdType = int.class;
        } else {
            throw new IllegalArgumentException("HasDeletionMark只能使用MemberAccessExpression");
        }
    }

    /**
     * 配置多租户标记的属性的名称
     *
     * @param expression 表达式
     */
    public void hasLongTenantIdMark(SerializedFunction<TObject, Long> expression) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(expression);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();

            this.tenantIdMark = memberExpression.getMemberName();
            this.tenantIdType = long.class;
        } else {
            throw new IllegalArgumentException("HasDeletionMark只能使用MemberAccessExpression");
        }
    }

    /**
     * 配置多租户标记的属性的名称
     *
     * @param expression 表达式
     */
    public void hasUUIDTenantIdMark(SerializedFunction<TObject, UUID> expression) {
        LambdaTranslator translator = new LambdaTranslator();
        LambdaExpression lambdaExpression = translator.getLambdaExpression(expression);
        if (lambdaExpression.getBody() instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) lambdaExpression.getBody();

            this.tenantIdMark = memberExpression.getMemberName();
            this.tenantIdType = UUID.class;
        } else {
            throw new IllegalArgumentException("HasDeletionMark只能使用MemberAccessExpression");
        }
    }

    /**
     * 配置多租户的映射字段
     *
     * @param tenantIdField 多租户字段名
     * @param tenantIdType  多租户字段类型
     */
    public void hasTenantIdField(String tenantIdField, Class<?> tenantIdType) {
        if (tenantIdType != int.class && tenantIdType != Integer.class && tenantIdType != long.class && tenantIdType != Long.class
                && tenantIdType != String.class && tenantIdType != UUID.class)
            throw new IllegalArgumentException("多租户主键属性必须为string,int,long,Guid类型中的一种");

        this.tenantIdField = tenantIdField;

        //已使用Mark配置 此处忽略
        if (this.tenantIdMark == null || this.tenantIdMark.isEmpty())
            this.tenantIdType = tenantIdType;
    }

    /**
     * 设置全局租户ID
     *
     * @param tenantId 会同时启用包含全局租户ID查询
     */
    public void hasGlobalTenantId(Object tenantId) {
        Class<?> tenantIdType = tenantId.getClass();

        if (this.tenantIdType == null)
            throw new IllegalArgumentException("需要先设置多租户主键属性.");

        if (tenantIdType != Integer.class && tenantIdType != Long.class && tenantIdType != String.class && tenantIdType != UUID.class)
            throw new IllegalArgumentException("多租户主键属性必须为string,int,long,Uuid类型中的一种");

        if (!tenantIdType.equals(this.tenantIdType))
            throw new IllegalArgumentException("多租户主键属性与全局租户ID值类型不符.");

        this.globalTenantId = tenantId;
        this.loadingGlobal = true;
    }

    /**
     * 设置是否启用包含全局租户ID查询
     *
     * @param loadingGlobal 是否启用包含全局租户ID查询
     */
    public void hasLoadingGlobal(boolean loadingGlobal) {
        this.loadingGlobal = loadingGlobal;
    }

    /**
     * 获取类型扩展的类型
     *
     * @return 类型扩展的类型
     */
    @Override
    public Class<? extends TypeExtension> getExtensionType() {
        return MultiTenantExtension.class;
    }

    /**
     * 根据配置元数据生成类型扩展实例
     *
     * @return 类型扩展实例
     */
    @Override
    public TypeExtension makeExtension() {
        if ((this.tenantIdField == null || this.tenantIdField.isEmpty()) && (this.tenantIdMark == null || this.tenantIdMark.isEmpty()))
            throw new IllegalArgumentException("多租户标记TenantIdMark和多租户字段TenantIdField不能同时为空");

        MultiTenantExtension extension = new MultiTenantExtension();
        extension.setTenantIdMark(this.tenantIdMark);
        extension.setTenantIdField(this.tenantIdField);
        extension.setTenantIdType(this.tenantIdType);
        extension.setLoadingGlobal(this.loadingGlobal);
        extension.setGlobalTenantId(this.globalTenantId);

        return extension;
    }
}
