/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：隐式关联标注属性.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 10:26:40
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 隐式关联标注属性
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ImplicitAssociationAttribute {

    /**
     * 是否启用延迟加载
     *
     * @return 是否启用延迟加载
     */
    boolean enableLazyLoading() default false;

    /**
     * 映射表名
     *
     * @return 映射表名
     */
    String targetTableName() default "";
}
