/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：属性标注属性.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 10:04:17
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 属性标注属性
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeAttributeAttribute {

    /**
     * 映射字段
     *
     * @return 映射字段
     */
    String field() default "";

    /**
     * 最大字符数 只有1到255是有效的 如果设置为0 会被设置为255 如果超过255 会被设置为Text字段
     *
     * @return 最大字符数
     */
    int maxNumber() default 0;

    /**
     * 以小数位数表示的精度，0表示小数点后没有位数。精度最大值28
     *
     * @return 以小数位数表示的精度
     */
    byte precision() default 0;

    /**
     * 指示是否可空。对于主键设置为可空是无效的
     *
     * @return 指示是否可空
     */
    boolean nullable() default true;
}
