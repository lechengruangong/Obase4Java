/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联加载的拦截器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-24 11:52:29
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.odm.IIntervener;
import net.bytebuddy.implementation.bind.annotation.*;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.concurrent.Callable;

/**
 * 关联加载的拦截器
 */
public class AssociationGetMethodInterceptor {

    /**
     * 字段缓存
     */
    private static final Hashtable<String, Field> dict = new Hashtable<>();

    /**
     * 拦截方法
     *
     * @param callable   原始调用
     * @param method     原始方法
     * @param object     代理后对象
     * @param intervener 介入者
     * @return 新调用的返回值
     * @throws Exception 异常
     */
    @RuntimeType
    public static Object intercept(@SuperCall Callable<?> callable, @Origin Method method, @This Object object,
                                   @FieldValue("intervener") IIntervener intervener) throws Exception {

        try {

            Class<?> clazz = object.getClass();
            //没有介入者 直接返回
            if (intervener == null) {
                return callable.call();
            }

            //检查是否已禁用延迟加载
            Field field = clazz.getDeclaredField("forbidLazyLoading");
            field.setAccessible(true);
            boolean forbidLazyLoading = (boolean) field.get(object);
            if (forbidLazyLoading)
                return callable.call();

            //获取此字段是否已加载
            String fieldName = StringUtils.capitalize(method.getName().replace("get", "")) + "HasCalled";
            String fullName = clazz.getName() + fieldName;
            Field hasCalledFiled;
            if (dict.containsKey(fullName)) {
                hasCalledFiled = dict.get(fullName);
            } else {
                hasCalledFiled = clazz.getDeclaredField(fieldName);
                dict.put(fullName, hasCalledFiled);
            }
            hasCalledFiled.setAccessible(true);
            boolean hasCalled = (boolean) hasCalledFiled.get(object);
            //已调用过 直接返回null
            if (hasCalled) {
                return callable.call();
            }

            intervener.loadAssociation(object, StringUtils.uncapitalize((method.getName().replace("get", ""))));

            hasCalledFiled.set(object, true);

            return callable.call();

        } catch (NoSuchFieldException e) {
            //没有此字段则直接返回
            return callable.call();
        }
    }
}
