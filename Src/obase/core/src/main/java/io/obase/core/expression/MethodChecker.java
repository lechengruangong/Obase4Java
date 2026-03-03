/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：JINQ表达式解析器用的方法检查器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-18 15:26:51
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import ch.epfl.labos.iu.orm.queryll2.path.Annotations;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisMethodChecker;
import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.BasicSymbolicInterpreter;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.odm.builder.ImplicitAssociation;
import org.jinq.rebased.org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * 实现Jinq.Analysis中的PathAnalysisMethodChecker接口
 * 定义了一部分安全的方法
 * 从Jinq.Jooq和Jinq.JPA复合而来
 */
public class MethodChecker implements PathAnalysisMethodChecker {

    /**
     * 已注册的实体类
     */
    private static final Map<String, Class<?>> registryClass = new HashMap<>();
    /**
     * 私有MethodChecker字段
     */
    private static final MethodChecker checker = new MethodChecker();
    /**
     * 字段方法 Get/Set
     */
    public final Set<MethodSignature> filedMethod;
    /**
     * 构造函数
     */
    public final Set<MethodSignature> constructorMethod;
    /**
     * 已知的枚举
     */
    public final Map<String, List<Enum<?>>> enums;
    /**
     * 安全的静态方法
     */
    public final Set<MethodSignature> safeStaticMethods;
    /**
     * 安全的方法
     */
    public final Set<MethodSignature> safeMethods;
    /**
     * 可以被支持的方法 如字符串包含等
     */
    public final Set<MethodSignature> canSupportFunctionMethods;
    /**
     * 可以支持的静态方法 如Math.
     */
    public final Set<MethodSignature> canSupportFunctionStaticMethods;
    /**
     * 安全的方法签名
     */
    public final Set<Class<?>> safeMethodAnnotations;
    /**
     * 各种ValueOf方法
     */
    public final Set<MethodSignature> valueOfFunction;
    /**
     * 装箱方法
     */
    public final Set<MethodSignature> boxingFunction;
    /**
     * 比较方法 包含枚举的比较方法 (枚举的比较方法由MethodCheckerFactory加入)
     */
    public final Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonMethods;
    /**
     * 包含Object.Equal的比较方法 除枚举的比较方法外还包含 Object.Equal
     */
    public final Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonMethodsWithObjectEquals;
    /**
     * 静态的比较方法
     */
    public final Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonStaticMethods;
    /**
     * 包含Object.Equal的静态的比较方法
     */
    public final Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonStaticMethodsWithObjectEquals;
    /**
     * Google提供的相等方法
     */
    private final MethodSignature guavaObjectsEqual = new MethodSignature("com/google/common/base/Objects", "equal", "(Ljava/lang/Object;Ljava/lang/Object;)Z");
    /**
     * Object类的Equal
     */
    private final MethodSignature objectEquals;
    /**
     * 可以链式调用的StringBuilder.ToString()
     */
    private final MethodSignature stringBuilderToString;

    /**
     * 构造函数
     */
    private MethodChecker() {
        //安全的静态方法
        this.safeStaticMethods = new HashSet<>();
        //加入Jinq中预制的方法
        this.safeStaticMethods.addAll(TransformationClassAnalyzer.KnownSafeStaticMethods);
        this.safeStaticMethods.add(TransformationClassAnalyzer.integerValueOf);
        this.safeStaticMethods.add(TransformationClassAnalyzer.longValueOf);
        this.safeStaticMethods.add(TransformationClassAnalyzer.floatValueOf);
        this.safeStaticMethods.add(TransformationClassAnalyzer.doubleValueOf);
        this.safeStaticMethods.add(TransformationClassAnalyzer.booleanValueOf);
        this.safeStaticMethods.add(TransformationClassAnalyzer.dateAfter);
        this.safeStaticMethods.add(TransformationClassAnalyzer.dateBefore);
        this.safeStaticMethods.add(TransformationClassAnalyzer.dateEquals);
        this.safeStaticMethods.add(TransformationClassAnalyzer.localDateIsBefore);
        this.safeStaticMethods.add(TransformationClassAnalyzer.localDateIsAfter);
        this.safeStaticMethods.add(TransformationClassAnalyzer.localDateIsEqual);
        //安全的方法
        this.safeMethods = new HashSet<>();
        //加入Jinq中预制的方法
        this.safeMethods.addAll(TransformationClassAnalyzer.KnownSafeMethods);
        this.safeMethods.add(TransformationClassAnalyzer.integerIntValue);
        this.safeMethods.add(TransformationClassAnalyzer.longLongValue);
        this.safeMethods.add(TransformationClassAnalyzer.floatFloatValue);
        this.safeMethods.add(TransformationClassAnalyzer.doubleDoubleValue);
        this.safeMethods.add(TransformationClassAnalyzer.booleanBooleanValue);
        //安全的方法注释
        this.safeMethodAnnotations = new HashSet<>();
        this.safeMethodAnnotations.addAll(TransformationClassAnalyzer.SafeMethodAnnotations);

        //可支持的方法
        this.canSupportFunctionMethods = new HashSet<>();
        //可支持的静态方法
        this.canSupportFunctionStaticMethods = new HashSet<>();
        //比较方法集合
        this.comparisonMethods = new HashMap<>();
        this.comparisonMethodsWithObjectEquals = new HashMap<>();
        this.comparisonStaticMethods = new HashMap<>();
        this.comparisonStaticMethodsWithObjectEquals = new HashMap<>();
        //字段访问器方法集合
        this.filedMethod = new HashSet<>();
        //构造函数集合
        this.constructorMethod = new HashSet<>();
        //枚举集合
        this.enums = new HashMap<>();
        //ValueOf方法
        this.valueOfFunction = new HashSet<>();
        //装箱方法
        this.boxingFunction = new HashSet<>();

        try {

            //包装类方法
            this.valueOfFunction.add(MethodSignature.fromMethod(Integer.class.getMethod("valueOf", int.class)));
            this.valueOfFunction.add(MethodSignature.fromMethod(Long.class.getMethod("valueOf", long.class)));
            this.valueOfFunction.add(MethodSignature.fromMethod(Short.class.getMethod("valueOf", short.class)));
            this.valueOfFunction.add(MethodSignature.fromMethod(Double.class.getMethod("valueOf", double.class)));
            this.valueOfFunction.add(MethodSignature.fromMethod(Float.class.getMethod("valueOf", float.class)));
            this.valueOfFunction.add(MethodSignature.fromMethod(Boolean.class.getMethod("valueOf", boolean.class)));

            this.boxingFunction.add(MethodSignature.fromMethod(Integer.class.getMethod("intValue")));
            this.boxingFunction.add(MethodSignature.fromMethod(Long.class.getMethod("longValue")));
            this.boxingFunction.add(MethodSignature.fromMethod(Short.class.getMethod("shortValue")));
            this.boxingFunction.add(MethodSignature.fromMethod(Byte.class.getMethod("byteValue")));
            this.boxingFunction.add(MethodSignature.fromMethod(Character.class.getMethod("charValue")));
            this.boxingFunction.add(MethodSignature.fromMethod(Double.class.getMethod("doubleValue")));
            this.boxingFunction.add(MethodSignature.fromMethod(Float.class.getMethod("floatValue")));
            this.boxingFunction.add(MethodSignature.fromMethod(Boolean.class.getMethod("booleanValue")));

            //可支持的函数方法
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(BigDecimal.class.getMethod("abs")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(BigInteger.class.getMethod("abs")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(BigDecimal.class.getMethod("negate")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(BigInteger.class.getMethod("negate")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(String.class.getMethod("toUpperCase")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(String.class.getMethod("toLowerCase")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(String.class.getMethod("trim")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(String.class.getMethod("length")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(String.class.getMethod("substring", int.class, int.class)));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(String.class.getMethod("indexOf", String.class)));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(String.class.getMethod("contains", CharSequence.class)));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(String.class.getMethod("startsWith", String.class)));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(String.class.getMethod("endsWith", String.class)));

            //可支持的分组聚合方法
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(IAggregation.class.getMethod("toArray")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(IAggregation.class.getMethod("toList")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(IAggregation.class.getMethod("countLong")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(IAggregation.class.getMethod("sumLong")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(IAggregation.class.getMethod("sumDouble")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(IAggregation.class.getMethod("minDouble")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(IAggregation.class.getMethod("maxDouble")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(IAggregation.class.getMethod("minLong")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(IAggregation.class.getMethod("maxLong")));
            this.canSupportFunctionMethods.add(MethodSignature.fromMethod(IAggregation.class.getMethod("avgDouble")));
            this.canSupportFunctionMethods.addAll(this.boxingFunction);

            //可支持的数学计算方法
            this.canSupportFunctionStaticMethods.add(MethodSignature.fromMethod(Math.class.getMethod("sqrt", double.class)));
            this.canSupportFunctionStaticMethods.add(MethodSignature.fromMethod(Math.class.getMethod("abs", int.class)));
            this.canSupportFunctionStaticMethods.add(MethodSignature.fromMethod(Math.class.getMethod("abs", double.class)));
            this.canSupportFunctionStaticMethods.add(MethodSignature.fromMethod(Math.class.getMethod("abs", long.class)));
            this.canSupportFunctionStaticMethods.add(MethodSignature.fromMethod(Math.class.getMethod("abs", float.class)));
            this.canSupportFunctionStaticMethods.addAll(this.valueOfFunction);

            //可支持的比较方法
            this.comparisonStaticMethods.put(MethodSignature.fromMethod(Objects.class.getMethod("equals", Object.class, Object.class)), TypedValue.ComparisonValue.ComparisonOp.eq);
            this.objectEquals = MethodSignature.fromMethod(Object.class.getMethod("equals", Object.class));
            this.comparisonMethodsWithObjectEquals.put(this.objectEquals, TypedValue.ComparisonValue.ComparisonOp.eq);
            this.comparisonStaticMethodsWithObjectEquals.put(this.guavaObjectsEqual, TypedValue.ComparisonValue.ComparisonOp.eq);
            this.comparisonStaticMethodsWithObjectEquals.put(this.objectEquals, TypedValue.ComparisonValue.ComparisonOp.eq);

            this.stringBuilderToString = MethodSignature.fromMethod(StringBuilder.class.getMethod("toString"));


        } catch (SecurityException | NoSuchMethodException exception) {
            throw new IllegalArgumentException("无法注册指定方法,请参考内部异常", exception);
        }
    }

    /**
     * 返回唯一单例
     *
     * @return 单例对象
     */
    public static MethodChecker getInstance() {
        return checker;
    }

    /**
     * 注册枚举
     *
     * @param fieldJavaType 字段类型
     */
    public static void registerEnum(Class<?> fieldJavaType) {
        String enumTypeName = Type.getInternalName(fieldJavaType);
        checker.enums.put(enumTypeName, Arrays.asList(((Class<Enum<?>>) fieldJavaType).getEnumConstants()));
        MethodSignature eqMethod = new MethodSignature(enumTypeName, "equals", "(Ljava/lang/Object;)Z");
        //分别放入比较方法和安全方法集合
        checker.comparisonMethods.put(eqMethod, TypedValue.ComparisonValue.ComparisonOp.eq);
        checker.comparisonMethodsWithObjectEquals.put(eqMethod, TypedValue.ComparisonValue.ComparisonOp.eq);
        checker.safeMethods.add(eqMethod);
    }

    /**
     * 注册一个类里的安全方法
     *
     * @param entity 实体类型
     */
    public static void registerClassMethod(Class<?> entity) {

        //已注册 不重复注册
        if (registryClass.containsKey(entity.getName()))
            return;

        List<Property> properties = ObaseIntrospector.getObaseBeanProperties(entity);

        for (Property prop : properties) {

            //添加Get和Set方法
            if (prop.getGetterMethod() != null)
                checker.filedMethod.add(new MethodSignature(Type.getInternalName(entity), prop.getGetterMethod().getName(), Type.getMethodDescriptor(prop.getGetterMethod())));
            if (prop.getSetterMethod() != null)
                checker.filedMethod.add(new MethodSignature(Type.getInternalName(entity), prop.getSetterMethod().getName(), Type.getMethodDescriptor(prop.getSetterMethod())));

            //发现有枚举作为GetSet的值 则对其进行注册
            if (prop.getPropertyType().isEnum())
                registerEnum(prop.getPropertyType());
        }
        checker.safeMethods.addAll(checker.filedMethod);
        //加入构造函数
        Constructor<?>[] constructors = entity.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            MethodSignature methodSig = MethodSignature.fromConstructor(constructor);
            checker.constructorMethod.add(methodSig);
        }

        checker.safeMethods.addAll(checker.constructorMethod);

        registryClass.put(entity.getName(), entity);
    }

    /**
     * 根据方法签名判定一个静态方法是否是安全的
     *
     * @param methodSignature 方法签名
     * @return 是否安全
     */
    @Override
    public BasicSymbolicInterpreter.OperationSideEffect isStaticMethodSafe(MethodSignature methodSignature) {
        if ((this.objectEquals.equals(methodSignature) || this.guavaObjectsEqual.equals(methodSignature))) {
            return BasicSymbolicInterpreter.OperationSideEffect.NONE;
        } else {
            return !this.safeStaticMethods.contains(methodSignature) && !this.canSupportFunctionStaticMethods.contains(methodSignature) ? BasicSymbolicInterpreter.OperationSideEffect.UNSAFE : BasicSymbolicInterpreter.OperationSideEffect.NONE;
        }
    }

    /**
     * 根据方法签名判断一个方法是否是链式(存疑)
     *
     * @param methodSignature 方法签名
     * @return 是否是链式
     */
    @Override
    public boolean isFluentChaining(MethodSignature methodSignature) {
        return this.stringBuilderToString.equals(methodSignature);
    }

    /**
     * 是否允许put field操作
     * 一种字节码中的操作 和常量池弹出有关
     *
     * @return 永远是false
     */
    @Override
    public boolean isPutFieldAllowed() {
        return false;
    }

    /**
     * 判断一个方法是否是安全的
     *
     * @param methodSignature 方法签名
     * @param typedValue      类型化的值
     * @param list            类型化的值列表
     * @return 是否是安全的
     */
    @Override
    public BasicSymbolicInterpreter.OperationSideEffect isMethodSafe(MethodSignature methodSignature, TypedValue typedValue, List<TypedValue> list) {
        //检查传入的方法签名是否是安全的
        //依次检查各个集合和是否被标记为安全
        if ("equals".equals(methodSignature.name) && "(Ljava/lang/Object;)Z".equals(methodSignature.desc)) {
            return BasicSymbolicInterpreter.OperationSideEffect.NONE;
        } else if (this.objectEquals.equals(methodSignature)) {
            return BasicSymbolicInterpreter.OperationSideEffect.NONE;
        } else if (!this.safeMethods.contains(methodSignature) && !this.canSupportFunctionMethods.contains(methodSignature)) {
            try {
                Method reflectedMethod = Annotations.asmMethodSignatureToReflectionMethod(methodSignature);
                if ("contains".equals(methodSignature.name) && "(Ljava/lang/Object;)Z".equals(methodSignature.desc) && Collection.class.isAssignableFrom(reflectedMethod.getDeclaringClass())) {
                    return BasicSymbolicInterpreter.OperationSideEffect.NONE;
                }

                if (Annotations.methodHasSomeAnnotations(reflectedMethod, this.safeMethodAnnotations)) {
                    return BasicSymbolicInterpreter.OperationSideEffect.NONE;
                }
            } catch (NoSuchMethodException | ClassNotFoundException exception) {
                //Nothing to do
            }

            return BasicSymbolicInterpreter.OperationSideEffect.UNSAFE;
        } else {
            return BasicSymbolicInterpreter.OperationSideEffect.SAFE;
        }
    }

    /**
     * 获取比较方法
     *
     * @param withObjectEquals 是否包含Equal方法
     * @return 比较方法
     */
    public Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> getComparisonMethods(boolean withObjectEquals) {
        return withObjectEquals ? this.comparisonMethodsWithObjectEquals : this.comparisonMethods;
    }

    /**
     * 获取静态的比较方法
     *
     * @param withObjectEquals 是否包含Equal方法
     * @return 比较方法
     */
    public Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> getComparisonStaticMethods(boolean withObjectEquals) {
        return withObjectEquals ? this.comparisonStaticMethodsWithObjectEquals : this.comparisonStaticMethods;
    }

    /**
     * 是否是已注册类型
     *
     * @param entity 要检查的类型
     * @return 是否是已经注册的类型
     */
    public boolean isRegisteredClass(Class<?> entity) {
        if (ImplicitAssociation.class.isAssignableFrom(entity))
            return true;
        return registryClass.containsKey(entity.getName());
    }
}