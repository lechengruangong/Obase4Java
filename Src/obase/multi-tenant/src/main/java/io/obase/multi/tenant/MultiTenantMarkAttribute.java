/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：多租户标记标注属性.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 11:31:50
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.multi.tenant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多租户标记标注属性 用于指定多租户标记的属性的名称
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiTenantMarkAttribute {
}
