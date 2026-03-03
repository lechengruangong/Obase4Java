/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：隐式关联型的Clr类型管理器,存放所有创建的隐式关联型.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-24 17:45:52
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.implicitAssociationConfigor;

import io.obase.core.odm.FieldDescriptor;
import io.obase.core.odm.GlobalClassLoaderCache;
import io.obase.core.odm.builder.ImplicitAssociation;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

/**
 * 隐式关联型的Clr类型管理器
 */
public class ImplicitAssociationManager {

    /**
     * 命名空间
     */
    private static final String nameSpace = "io.obase.proxy.module";
    /**
     * 单例对象
     */
    private static volatile ImplicitAssociationManager manager;
    /**
     * 接受管理的隐式关联型类型
     */
    private final Set<Class<?>> impliedTypes = new HashSet<>();

    /**
     * 邮戳锁
     */
    private final StampedLock stampedLock = new StampedLock();

    /**
     * 命名计数器，用于在命名过程中累加计数，避免命名重复。
     */
    private int namingCounter;

    /**
     * 创建ImplicitAssociationManager实例
     */
    private ImplicitAssociationManager() {
    }

    /**
     * 获取当前应用程序域中唯一的隐式关联型类型管理器实例
     *
     * @return 单例
     */
    public static ImplicitAssociationManager getCurrent() {
        if (manager == null) {
            synchronized (ImplicitAssociationManager.class) {
                manager = new ImplicitAssociationManager();
            }
        }
        return manager;
    }

    /**
     * 获取所有已定义的隐式关联型
     *
     * @return 所有已定义的隐式关联型
     */
    public Set<Class<?>> getImpliedTypes() {
        return this.impliedTypes;
    }

    /**
     * 向隐含类型管理器申请一个类型，该类型派生自指定的基类型，定义且只定义了指定的字段，如果这样的类型有多个则以指定的子标识进一步识别。
     *
     * @param fields   类型应当且只能定义的字段
     * @param fullName 全名
     * @return 定义的隐式关联型
     */
    public Class<?> applyType(FieldDescriptor[] fields, String fullName) {
        return this.searchOrDefineType(fullName, fields);
    }

    /**
     * 以指定的标识搜索隐含类型，如果未找到则根据指定的内容创建隐式关联型Clr类型。
     *
     * @param fullName 全名
     * @param fields   类型应当且只能定义的字段
     * @return 定义的隐式关联型
     */
    private Class<?> searchOrDefineType(String fullName, FieldDescriptor[] fields) {
        //命名
        String name = fullName + "_" + (++this.namingCounter);
        long stamp = this.stampedLock.writeLock();
        //定义一个新类型
        Class<?> type = this.defineType(name, fields);
        this.impliedTypes.add(type);
        this.stampedLock.unlockWrite(stamp);
        return type;
    }

    /**
     * 根据指定的内容定义隐式关联型Clr类型
     *
     * @param name   类型名称
     * @param fields 类型应当且只能定义的字段
     * @return 定义的隐式关联型
     */
    private Class<?> defineType(String name, FieldDescriptor[] fields) {
        ByteBuddy buddy = new ByteBuddy(ClassFileVersion.ofThisVm());
        DynamicType.Builder<?> builder = buddy.subclass(ImplicitAssociation.class, ConstructorStrategy.Default.NO_CONSTRUCTORS);
        builder.implement(Serializable.class);
        builder = builder.name(nameSpace + "." + name);

        builder = builder.defineConstructor(Visibility.PUBLIC).intercept(MethodCall
                .invoke(ImplicitAssociation.class.getDeclaredConstructors()[0])
                .onSuper());

        //有字段
        if (fields != null) {
            //命名计数器
            AtomicInteger i = new AtomicInteger();
            for (FieldDescriptor field : fields) {
                //名称
                String filedName = field.getName(() -> {
                    //字段前半部分
                    String filedStart = field.getHasGetter() || field.getHasSetter() ? "_field_" : "Field_";
                    return filedStart + (i.incrementAndGet());
                });

                //类型
                Class<?> filedType = field.getType();

                //设值取值方法
                Visibility typeAttr = field.getHasGetter() || field.getHasSetter()
                        ? Visibility.PRIVATE
                        : Visibility.PUBLIC;
                builder = builder.defineField(filedName, filedType, typeAttr);

                if (field.getHasSetter() || field.getHasGetter()) {

                    //定一个属性访问器
                    String propName = field.getPropertyName();

                    //取值方法
                    if (field.getHasGetter()) {
                        builder = builder.defineMethod("get" + propName, filedType, Visibility.PUBLIC).intercept(FieldAccessor.ofField(filedName));
                    }

                    //设值方法
                    if (field.getHasSetter()) {
                        builder = builder.defineMethod("set" + propName, void.class, Visibility.PUBLIC).withParameters(filedType)
                                .intercept(FieldAccessor.ofField(filedName));
                    }
                }
            }
        }

        try (DynamicType.Unloaded<?> unloaded = builder.make()) {
            return unloaded.load(GlobalClassLoaderCache.getInstance().getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded();
        } catch (Exception exception) {
            throw new RuntimeException("无法构造隐式关联型类型,请参考内部异常.", exception);
        }

    }
}
