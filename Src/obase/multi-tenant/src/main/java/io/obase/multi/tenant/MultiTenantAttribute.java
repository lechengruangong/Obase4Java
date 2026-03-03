/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：多租户注属性.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 11:31:06
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.multi.tenant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多租户注属性 用于指定哪个字段用于多租户
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiTenantAttribute {

    /**
     * 哪个字段用于多租户
     *
     * @return 哪个字段用于多租户
     */
    String multiTenantField() default "";

    /**
     * 多租户ID类型
     *
     * @return 多租户ID类型
     */
    Class<?> tenantIdType();
}
