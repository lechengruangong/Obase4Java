/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：显式关联型标注属性.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 10:59:59
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 显式关联型标注属性
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AssociationAttribute {

    /**
     * 映射表名 为Null或空字符串表示与类名相同
     *
     * @return 映射表名 为Null或空字符串表示与类名相同
     */
    String tableName() default "";
}
