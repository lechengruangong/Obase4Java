/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：成员绑定.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 15:34:23
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import java.lang.reflect.Method;

/**
 * 成员绑定
 * 用于表示构造对象的表达式中成员的初始化
 * JAVA中无此种语法
 */
public abstract class MemberBinding {

    /**
     * 获取绑定类型
     *
     * @return 绑定类型
     */
    public abstract EMemberBindingType getBidingType();

    /**
     * 获取成员设置方法
     *
     * @return 成员设置方法
     */
    public abstract Method getMemberSetMethod();
}
