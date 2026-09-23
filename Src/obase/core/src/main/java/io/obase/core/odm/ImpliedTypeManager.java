/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：隐含类型管理器,管理代理类型.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 15:12:17
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.common.FunctionWithOneArg;
import io.obase.core.IdentityArray;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 隐含类型管理器，负责创建、维护隐含类型，并提供对这些类型的访问入口。
 * 隐含类型是指非由应用程序开发人员定义，而由Obase基于实现某些功能需要自行定义的类型。通常情况下，这些类型对开发人员是不可见的。
 */
public class ImpliedTypeManager {

    /**
     * 命名空间
     */
    private static final String nameSpace = "io.obase.proxy.module";
    /**
     * 接受管理的隐含类型。
     * 值为TypeHolder，保证同一个标识的隐含类型只会被定义一次，且定义（IL发射）过程不持有任何锁。
     */
    private final ConcurrentHashMap<IdentityArray, TypeHolder> impliedTypes = new ConcurrentHashMap<>();
    /**
     * 命名计数器，用于在命名过程中累加计数，避免命名重复。
     */
    private final AtomicInteger namingCounter = new AtomicInteger();

    /**
     * 创建ImpliedTypeManager实例
     */
    private ImpliedTypeManager() {
    }

    /**
     * 获取当前应用程序域中唯一的隐含类型管理器实例
     *
     * @return 单例
     */
    public static ImpliedTypeManager getCurrent() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * 隐含类型管理器单例持有者
     */
    private static final class InstanceHolder {

        /**
         * 单例
         */
        private static final ImpliedTypeManager INSTANCE = new ImpliedTypeManager();
    }

    /**
     * 获取隐含类型。如果该标识的隐含类型尚未定义完成则返回null。
     *
     * @param identity 要获取类型的标识
     * @return 隐含类型
     */
    public Class<?> getType(IdentityArray identity) {
        //尝试从已定义的隐含类型中获取 尚未定义完成时返回null
        TypeHolder holder = this.impliedTypes.get(identity);
        return holder == null ? null : holder.getIfCreated();
    }

    /**
     * 向隐含类型管理器申请一个类型，该类型定义且只定义了指定的字段。
     *
     * @param fields        类型应当且只能定义的字段
     * @param defineMembers 一个委托，用于定义类型的成员。
     * @return 隐含类型
     */
    public Class<?> applyType(FieldDescriptor[] fields, FunctionWithOneArg<DynamicType.Builder<?>, DynamicType.Builder<?>> defineMembers) {

        Object[] filedTexts = Arrays.stream(fields).map(p -> (Object) p.toString()).toArray();

        IdentityArray identityArray = new IdentityArray(filedTexts);

        return this.searchOrDefineType(identityArray, Object.class, null, fields, defineMembers, null);
    }

    /**
     * 向隐含类型管理器申请一个类型，该类型定义且只定义了指定的字段，如果这样的类型有多个则以指定的子标识进一步识别。
     *
     * @param fields       类型应当且只能定义的字段
     * @param subIdentity  子标识
     * @param defineMember 一个委托，用于定义类型的成员
     * @return 符合条件的隐含类型
     */
    public Class<?> applyType(FieldDescriptor[] fields, IdentityArray subIdentity,
                              FunctionWithOneArg<DynamicType.Builder<?>, DynamicType.Builder<?>> defineMember) {
        Object[] filedTexts = Arrays.stream(fields).map(p -> (Object) p.toString()).toArray();

        IdentityArray identityArray = new IdentityArray(filedTexts);
        identityArray.append(subIdentity);

        return this.searchOrDefineType(identityArray, Object.class, null, fields, defineMember, null);
    }

    /**
     * 向隐含类型管理器申请一个类型，该类型派生自指定的基类型
     *
     * @param baseType      类型的基类
     * @param defineMembers 用于定义类型成员的委托
     * @return 符合条件的隐含类型
     */
    public Class<?> applyType(Class<?> baseType, FunctionWithOneArg<DynamicType.Builder<?>, DynamicType.Builder<?>> defineMembers) {
        IdentityArray identityArray = new IdentityArray(baseType.getName());

        return this.searchOrDefineType(identityArray, baseType, null, null, defineMembers, null);
    }

    /**
     * 向隐含类型管理器申请一个类型，该类型派生自指定的基类型，如果这样的类型有多个则以指定的子标识进一步识别。
     *
     * @param baseType      类型的基类
     * @param subIdentity   子标识
     * @param defineMembers 用于定义类型成员的委托
     * @return 符合条件的隐含类型
     */
    public Class<?> applyType(Class<?> baseType, IdentityArray subIdentity, FunctionWithOneArg<DynamicType.Builder<?>, DynamicType.Builder<?>> defineMembers) {

        IdentityArray identityArray = new IdentityArray(baseType.getName());
        identityArray.append(subIdentity);

        return this.searchOrDefineType(identityArray, baseType, null, null, defineMembers, null);
    }

    /**
     * 向隐含类型管理器申请一个类型，该类型派生自指定的基类型，定义且只定义了指定的字段
     *
     * @param baseType      类型的基类
     * @param fields        类型应当且只能定义的字段
     * @param defineMembers >一个委托，用于定义类型的成员
     * @return 符合条件的隐含类型
     */
    public Class<?> applyType(Class<?> baseType, FieldDescriptor[] fields, FunctionWithOneArg<DynamicType.Builder<?>, DynamicType.Builder<?>> defineMembers) {

        IdentityArray identityArray = new IdentityArray(baseType.getName());

        return this.searchOrDefineType(identityArray, baseType, null, fields, defineMembers, null);
    }

    /**
     * 向隐含类型管理器申请一个类型，该类型派生自指定的基类型，定义且只定义了指定的字段，如果这样的类型有多个则以指定的子标识进一步识别。
     *
     * @param baseType      类型的基类
     * @param fields        类型应当且只能定义的字段
     * @param subIdentity   子标识
     * @param defineMembers 一个委托，用于定义类型的成员
     * @return 符合条件的隐含类型
     */
    public Class<?> applyType(Class<?> baseType, FieldDescriptor[] fields, IdentityArray subIdentity, FunctionWithOneArg<DynamicType.Builder<?>, DynamicType.Builder<?>> defineMembers) {

        IdentityArray identityArray = new IdentityArray(baseType.getName());
        identityArray.append(subIdentity);

        return this.searchOrDefineType(identityArray, baseType, null, fields, defineMembers, null);
    }

    /**
     * 向隐含类型管理器申请一个类型，该类型实现指定的接口，定义且只定义了指定的字段
     *
     * @param interfaces    类型实现的接口
     * @param fields        类型应当且只能定义的字段
     * @param defineMembers 一个委托，用于定义类型的成员
     * @return 符合条件的隐含类型
     */
    public Class<?> applyType(Class<?>[] interfaces, FieldDescriptor[] fields, FunctionWithOneArg<DynamicType.Builder<?>, DynamicType.Builder<?>> defineMembers) {
        Object[] interfacesTexts = Arrays.stream(interfaces).map(Class::getName).toArray();

        IdentityArray identityArray = new IdentityArray(interfacesTexts);

        return this.searchOrDefineType(identityArray, Object.class, interfaces, fields, defineMembers, null);
    }


    /**
     * 向隐含类型管理器申请一个类型，该类型派生自指定的基类型并实现指定的接口。
     *
     * @param baseType      类型的基类
     * @param interfaces    类型实现的接口
     * @param defineMembers 用于定义类型成员的委托
     * @return 符合条件的隐含类型
     */
    public Class<?> applyType(Class<?> baseType, Class<?>[] interfaces, FunctionWithOneArg<DynamicType.Builder<?>, DynamicType.Builder<?>> defineMembers, Constructor<?> constructor) {
        IdentityArray identityArray = new IdentityArray(baseType.getName());
        Object[] interfacesTexts = Arrays.stream(interfaces).map(Class::getName).toArray();
        identityArray.append(interfacesTexts);

        return this.searchOrDefineType(identityArray, baseType, interfaces, null, defineMembers, constructor);
    }

    /**
     * 向隐含类型管理器申请一个类型，该类型派生自指定的基类型并实现指定的接口，如果这样的类型有多个则以指定的子标识进一步识别。
     *
     * @param baseType      类型的基类
     * @param interfaces    类型实现的接口
     * @param subIdentity   子标识
     * @param defineMembers 用于定义类型成员的委托
     * @return 符合条件的隐含类型
     */
    public Class<?> applyType(Class<?> baseType, Class<?>[] interfaces, IdentityArray subIdentity, FunctionWithOneArg<DynamicType.Builder<?>, DynamicType.Builder<?>> defineMembers) {
        Object[] interfacesTexts = Arrays.stream(interfaces).map(Class::getName).toArray();

        IdentityArray identityArray = new IdentityArray(baseType.getName());
        identityArray.append(interfacesTexts);
        identityArray.append(subIdentity);

        return this.searchOrDefineType(identityArray, baseType, interfaces, null, defineMembers, null);
    }

    /**
     * 向隐含类型管理器申请一个类型，该类型派生自指定的基类型，实现指定的接口，定义且只定义了指定的字段。
     *
     * @param baseType      类型的基类
     * @param interfaces    类型实现的接口
     * @param fields        类型应当且只能定义的字段
     * @param defineMembers 一个委托，用于定义类型的成员
     * @return 符合条件的隐含类型
     */
    public Class<?> applyType(Class<?> baseType, Class<?>[] interfaces, FieldDescriptor[] fields, FunctionWithOneArg<DynamicType.Builder<?>, DynamicType.Builder<?>> defineMembers) {

        IdentityArray identityArray = new IdentityArray(baseType.getName());
        Object[] interfacesTexts = Arrays.stream(interfaces).map(Class::getName).toArray();
        Object[] filedTexts = Arrays.stream(fields).map(p -> (Object) p.toString()).toArray();

        identityArray.append(interfacesTexts);
        identityArray.append(filedTexts);

        return this.searchOrDefineType(identityArray, baseType, interfaces, fields, defineMembers, null);
    }

    /**
     * 向隐含类型管理器申请一个类型，该类型派生自指定的基类型，实现指定的接口，定义且只定义了指定的字段，如果这样的类型有多个则以指定的子标识进一步识别。
     *
     * @param baseType      类型的基类
     * @param interfaces    类型实现的接口
     * @param fields        类型应当且只能定义的字段
     * @param subIdentity   子标识
     * @param defineMembers 一个委托，用于定义类型的成员
     * @return 符合条件的隐含类型
     */
    public Class<?> applyType(Class<?> baseType, Class<?>[] interfaces, FieldDescriptor[] fields, IdentityArray subIdentity, FunctionWithOneArg<DynamicType.Builder<?>, DynamicType.Builder<?>> defineMembers) {

        IdentityArray identityArray = new IdentityArray(baseType.getName());
        Object[] filedTexts = Arrays.stream(fields).map(p -> (Object) p.toString()).toArray();
        Object[] interfacesTexts = Arrays.stream(interfaces).map(Class::getName).toArray();

        identityArray.append(interfacesTexts);
        identityArray.append(filedTexts);
        identityArray.append(subIdentity);

        return this.searchOrDefineType(identityArray, baseType, interfaces, fields, defineMembers, null);
    }

    /**
     * 以指定的标识搜索隐含类型，如果未找到则根据指定的内容创建类型。
     *
     * @param identity      要搜索的类型的标识
     * @param baseType      要定义的类型的基类
     * @param interfaces    要定义的类型的实现接口
     * @param fields        要定义的字段
     * @param defineMembers 一个委托，用于定义类型的成员
     * @return 隐含类型
     */
    private Class<?> searchOrDefineType(IdentityArray identity, Class<?> baseType, Class<?>[] interfaces, FieldDescriptor[] fields, FunctionWithOneArg<DynamicType.Builder<?>, DynamicType.Builder<?>> defineMembers, Constructor<?> constructor) {

        //查找是否已定义
        Class<?> existType = this.getType(identity);
        if (existType != null)
            return existType;

        //命名
        String name = baseType.getSimpleName() + "_Obase_ImpliedType_" + this.namingCounter.incrementAndGet();

        //同一个标识并发申请时，只有一个TypeHolder会被执行，未中选的不会被触发，因此类型只会被定义一次
        TypeHolder holder = this.impliedTypes.computeIfAbsent(identity,
                key -> new TypeHolder(name, baseType, interfaces, fields, defineMembers, constructor));

        //触发或等待定义完成，定义过程（IL发射）不持有任何锁，异常也不会造成锁泄漏
        return holder.get();
    }

    /**
     * 隐含类型的延迟定义持有器。
     * 保证同一个标识的隐含类型只被定义一次，定义过程不持有任何锁。
     */
    private final class TypeHolder {

        /**
         * 类型名称
         */
        private final String name;

        /**
         * 基类型
         */
        private final Class<?> baseType;

        /**
         * 实现的接口
         */
        private final Class<?>[] interfaces;

        /**
         * 要定义的字段
         */
        private final FieldDescriptor[] fields;

        /**
         * 定义类型成员的委托
         */
        private final FunctionWithOneArg<DynamicType.Builder<?>, DynamicType.Builder<?>> defineMembers;

        /**
         * 构造函数
         */
        private final Constructor<?> constructor;

        /**
         * 定义完成的类型
         */
        private Class<?> type;

        /**
         * 定义是否已完成
         */
        private boolean created;

        /**
         * 定义失败时的异常，与延迟初始化的语义一致，失败会被缓存并抛给所有调用者
         */
        private Throwable failure;

        TypeHolder(String name, Class<?> baseType, Class<?>[] interfaces, FieldDescriptor[] fields,
                   FunctionWithOneArg<DynamicType.Builder<?>, DynamicType.Builder<?>> defineMembers, Constructor<?> constructor) {
            this.name = name;
            this.baseType = baseType;
            this.interfaces = interfaces;
            this.fields = fields;
            this.defineMembers = defineMembers;
            this.constructor = constructor;
        }

        /**
         * 触发或等待类型定义完成
         *
         * @return 隐含类型
         */
        synchronized Class<?> get() {
            if (this.failure != null) {
                if (this.failure instanceof RuntimeException) throw (RuntimeException) this.failure;
                if (this.failure instanceof Error) throw (Error) this.failure;
                throw new IllegalStateException(this.failure);
            }

            if (!this.created) {
                try {
                    this.type = ImpliedTypeManager.this.defineType(this.name, this.interfaces, this.baseType,
                            this.fields, this.defineMembers, this.constructor);
                    this.created = true;
                } catch (RuntimeException | Error e) {
                    this.failure = e;
                    throw e;
                }
            }

            return this.type;
        }

        /**
         * 获取已定义的类型，尚未定义完成时返回null
         *
         * @return 隐含类型
         */
        synchronized Class<?> getIfCreated() {
            return this.created ? this.type : null;
        }
    }

    /**
     * 根据指定的内容定义隐含类型
     *
     * @param name          类型名称
     * @param interfaces    基类型
     * @param baseType      类型实现的接口
     * @param fields        一个委托，用于定义类型的成员
     * @param defineMembers 类型实现的接口
     * @return 定义的隐含类型
     */
    private Class<?> defineType(String name, Class<?>[] interfaces, Class<?> baseType, FieldDescriptor[] fields,
                                FunctionWithOneArg<DynamicType.Builder<?>, DynamicType.Builder<?>> defineMembers, Constructor<?> constructor) {

        ByteBuddy buddy = new ByteBuddy(ClassFileVersion.ofThisVm());
        DynamicType.Builder<?> builder = buddy.subclass(baseType, ConstructorStrategy.Default.NO_CONSTRUCTORS);
        if (interfaces != null && interfaces.length > 0)
            for (Class<?> clazz : interfaces) {
                builder = builder.implement(clazz);
            }
        builder.implement(Serializable.class);
        builder = builder.name(nameSpace + "." + name);

        //如果都不在函数里创建参数
        if (fields != null && Arrays.stream(fields).noneMatch(FieldDescriptor::getCreateConstructorParameter)) {
            builder = this.preConstruct(baseType, constructor, builder);
        }

        if (fields == null) {
            builder = this.preConstruct(baseType, constructor, builder);
        }

        //有字段
        if (fields != null) {
            //命名计数器
            AtomicInteger i = new AtomicInteger();
            HashMap<String, Class<?>> nameList = new HashMap<>();
            for (FieldDescriptor field : fields) {
                //类型
                Class<?> filedType = field.getType();

                //设值取值方法
                Visibility typeAttr = field.getHasGetter() || field.getHasSetter()
                        ? Visibility.PRIVATE
                        : Visibility.PUBLIC;

                //名称
                String filedName = field.getName(() -> {
                    //字段前半部分
                    String filedStart = field.getHasGetter() || field.getHasSetter() ? "_field_" : "Field_";
                    return filedStart + (i.incrementAndGet());
                });

                builder = builder.defineField(filedName, filedType, typeAttr);
                nameList.put(filedName, filedType);

                if (field.getHasSetter() || field.getHasGetter()) {

                    //定一个属性访问器
                    String propName = field.getPropertyName();

                    //设值方法
                    if (field.getHasSetter()) {
                        builder = builder.defineMethod("set" + propName, void.class, Visibility.PUBLIC).withParameters(filedType)
                                .intercept(FieldAccessor.ofField(filedName));
                    }

                    //取值方法
                    if (field.getHasGetter()) {
                        builder = builder.defineMethod("get" + propName, filedType, Visibility.PUBLIC).intercept(FieldAccessor.ofField(filedName));
                    }
                }
            }

            //如果要在构造函数内创建参数
            if (Arrays.stream(fields).anyMatch(FieldDescriptor::getCreateConstructorParameter)) {

                Class<?>[] types;

                if (constructor == null) {
                    constructor = baseType.getDeclaredConstructors()[0];
                }

                constructor.setAccessible(true);
                Class<?>[] first = constructor.getParameterTypes();
                Class<?>[] second = nameList.values().toArray(new Class<?>[0]);
                types = Arrays.copyOf(first, first.length + second.length);
                System.arraycopy(second, 0, types, first.length, second.length);

                MethodCall call = MethodCall.invoke(baseType.getDeclaredConstructors()[0])
                        .withAllArguments();

                String[] names = nameList.keySet().toArray(new String[0]);
                for (int f = first.length; f < types.length; f++) {
                    call.andThen(FieldAccessor.ofField(names[f - first.length])
                            .setsArgumentAt(f));
                }

                builder = builder.defineConstructor(Visibility.PUBLIC)
                        .withParameters(types)
                        .intercept(call);

            }
        }
        //调用
        if (defineMembers != null) {
            builder = defineMembers.invoke(builder);
        }

        try (DynamicType.Unloaded<?> unloaded = builder.make()) {
            return unloaded.load(GlobalClassLoaderCache.getInstance().getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded();
        } catch (Exception e) {
            throw new RuntimeException("无法构造代理类型,请参考内部异常.", e);
        }
    }

    /**
     * 检查一次预创建构造函数
     *
     * @param baseType    基础类型
     * @param constructor 构造函数
     * @param builder     类型建造器
     * @return 类型建造器
     */
    private DynamicType.Builder<?> preConstruct(Class<?> baseType, Constructor<?> constructor, DynamicType.Builder<?> builder) {
        if (constructor == null) {
            builder = builder.defineConstructor(Visibility.PUBLIC).intercept(MethodCall
                    .invoke(baseType.getDeclaredConstructors()[0])
                    .onSuper());
        } else {
            constructor.setAccessible(true);
            builder = builder.defineConstructor(Visibility.PUBLIC).withParameters(constructor.getParameterTypes()).intercept(MethodCall
                    .invoke(constructor).withAllArguments());
        }
        return builder;
    }
}
