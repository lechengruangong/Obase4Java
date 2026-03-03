/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：构造函数标记.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 10:40:11
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 构造函数标记
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConstructorAttribute {

    /**
     * 参数对应的属性名集合
     *
     * @return 参数对应的属性名集合
     */
    String[] parameterNames() default {};
}
