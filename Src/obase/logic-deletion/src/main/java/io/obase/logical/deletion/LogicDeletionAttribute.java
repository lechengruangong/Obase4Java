/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：逻辑删除注属性.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 10:48:02
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.logical.deletion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 逻辑删除注属性 用于指定哪个字段用于逻辑删除
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogicDeletionAttribute {

    /**
     * 哪个字段用于逻辑删除
     *
     * @return 逻辑删除的字段名
     */
    String deletionField() default "";
}
