/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：隐式关联右端标注属性.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 10:13:49
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 隐式关联右端标注属性
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RightEndMappingAttribute {

    /**
     * 此端的键属性
     *
     * @return 此端的键属性
     */
    String keyAttribute();

    /**
     * 此端的键属性映射字段
     *
     * @return 此端的键属性映射字段
     */
    String targetField();
}
