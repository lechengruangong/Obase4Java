/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：一些内部使用的工具,封装了常用的方法.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-13 15:56:45
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.common;

import io.obase.common.ActionWithTwoArg;
import io.obase.common.ObjectReferencePack;
import io.obase.common.Tuple;
import io.obase.common.TwoTuple;
import io.obase.core.dependency.injection.ServiceContainer;
import io.obase.core.dependency.injection.ServiceContainerInstance;
import io.obase.core.odm.*;
import io.obase.core.odm.builder.ModelBuilder;
import io.obase.core.odm.builder.StructuralTypeConfiguration;
import io.obase.core.odm.builder.TypeElementConfigurationGeneric;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 一些内部使用的工具
 */
public final class Utils {

    /**
     * 值长度字典 单位 位
     */
    private static final Map<Class<?>, Integer> ValueLengthDictionary = new HashMap<>();

    /*
     静态初始化
    */
    static {
        ValueLengthDictionary.put(byte.class, 8);
        ValueLengthDictionary.put(Byte.class, 8);
        ValueLengthDictionary.put(short.class, 2 * 8);
        ValueLengthDictionary.put(Short.class, 2 * 8);
        ValueLengthDictionary.put(int.class, 4 * 8);
        ValueLengthDictionary.put(Integer.class, 4 * 8);
        ValueLengthDictionary.put(long.class, 8 * 8);
        ValueLengthDictionary.put(Long.class, 8 * 8);
        ValueLengthDictionary.put(char.class, 2 * 8);
        ValueLengthDictionary.put(Character.class, 2 * 8);
        ValueLengthDictionary.put(boolean.class, 8);
        ValueLengthDictionary.put(Boolean.class, 8);
        ValueLengthDictionary.put(float.class, 4 * 8);
        ValueLengthDictionary.put(Float.class, 4 * 8);
        ValueLengthDictionary.put(double.class, 8 * 8);
        ValueLengthDictionary.put(Double.class, 8 * 8);
        ValueLengthDictionary.put(BigDecimal.class, 16 * 8);
        ValueLengthDictionary.put(String.class, 0);
        ValueLengthDictionary.put(Date.class, 8 * 8);
        ValueLengthDictionary.put(LocalDateTime.class, 8 * 8);
        ValueLengthDictionary.put(LocalDate.class, 4 * 8);
        ValueLengthDictionary.put(LocalTime.class, 4 * 8);
        ValueLengthDictionary.put(UUID.class, 36 * 8);
    }

    /**
     * 判断一个字符串是否为null或者""
     *
     * @param str 要判断的字符串
     * @return 是null或者"" 返回true
     */
    public static boolean getStringIsEmpty(String str) {
        return StringUtils.isBlank(str);
    }

    /**
     * 获取字段 一直获取到基类为止
     *
     * @param clazz     目标类型
     * @param filedName 要获取的字段名称
     * @return 字段信息
     */
    public static Field getFieldIncludeSuperclass(Class<?> clazz, String filedName) {
        try {
            if (clazz != null) {
                return clazz.getDeclaredField(filedName);
            }
            return null;
        } catch (NoSuchFieldException e) {
            return getFieldIncludeSuperclass(clazz.getSuperclass(), filedName);
        }
    }

    /**
     * 根据字段类型获取泛型类型
     * 如果字段的声明类型本身就是List Set Map等有泛型参数的 则返回的数组内容为元素类型
     * 如果没有泛型参数 则返回的数组内容是本身的类型
     *
     * @param field 字段
     * @return 字段的声明类型的泛型参数数组 如果字段的声明类型本身就是List Set Map等有泛型参数的 则返回的数组内容为元素类型 如果没有泛型参数 则返回的数组内容是本身的类型
     */
    public static Class<?>[] getFieldGenericTypeArguments(Field field) {
        //如果是定义在元组上的 直接返回类型即可
        if (Tuple.class.isAssignableFrom(field.getDeclaringClass()))
            return new Class[]{field.getType()};
        //如果获取GenericType是Class<?> 表示没有泛型 直接返回
        if (field.getGenericType() instanceof Class<?>) {
            return new Class[]{(Class<?>) field.getGenericType()};
        }
        //不是Class<?> 是Map之类的可以获取多个泛型参数的
        else if (field.getGenericType() instanceof ParameterizedType) {
            //从field.getGenericType()转成ParameterizedType 获取ActualTypeArguments
            Type[] actualTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            //循环参数列表
            return convertTypesToClasses(actualTypes);
        } else {
            //其他情况 暂不支持
            throw new IllegalArgumentException("暂不支持" + field.getType() + "的泛型参数获取");
        }
    }

    /**
     * 获取某个方法的返回值泛型类型
     * 如果返回值的声明类型本身就是List Set Map等有泛型参数的 则返回的数组内容为元素类型
     * 如果没有泛型参数 则返回的数组内容是本身的类型
     *
     * @param method 方法
     * @return 方法返回值的声明类型的泛型参数数组 如果返回值的声明类型本身就是List Set Map等有泛型参数的 则返回的数组内容为元素类型 如果没有泛型参数 则返回的数组内容是本身的类型
     */
    public static Class<?>[] getMethodReturnValueGenericTypeArguments(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
            //获取到方法的参数列表
            Type[] actualTypes = parameterizedType.getActualTypeArguments();
            //循环参数列表
            return convertTypesToClasses(actualTypes);
        } else if (genericReturnType instanceof Class<?>) {
            return new Class[]{(Class<?>) genericReturnType};
        } else {
            //其他情况 暂不支持
            throw new IllegalArgumentException("暂不支持" + method + "的返回值泛型参数获取");
        }
    }

    /**
     * 获取属性是否为多重性
     *
     * @param property 属性
     * @param argType  如果是多重性的,为propInfo.PropertyType.GenericTypeArguments[0]否则为propInfo.PropertyType
     * @return 是否为多重的
     */
    public static boolean getIsMultiple(Property property, ObjectReferencePack<Class<?>> argType) {
        //关联重数（表示是否是集合属性）
        boolean isMultiplicity;

        Class<?>[] elementType = property.getPropertyElementType();

        //多个泛型参数
        if (elementType.length > 1) {
            throw new IllegalArgumentException("暂不支持多个泛型参数的类型进行多重性检查,原始类型为" + property.getPropertyType().getName() + ",泛型参数集合为[" + Arrays.stream(property.getPropertyElementType()).map(Type::getTypeName).collect(Collectors.joining(",")) + "].");
        }

        //如果取得的元素类型和属性类型不一致 就认为是多重的
        isMultiplicity = elementType[0] != property.getPropertyType();
        argType.realValue = elementType[0];

        return isMultiplicity;
    }

    /**
     * 是否是元组
     *
     * @param type 类型
     * @return 是否是元组
     */
    public static boolean isTuple(Class<?> type) {
        return Tuple.class.isAssignableFrom(type);
    }

    /**
     * 将Type[]转换为Class<?>[]
     *
     * @param actualTypes 要转换的Type[]
     * @return 转换后的Class<?>[]
     */
    private static Class<?>[] convertTypesToClasses(Type[] actualTypes) {
        List<Class<?>> result = new ArrayList<>();
        for (Type t : actualTypes) {
            if (t instanceof Class<?>) {
                result.add((Class<?>) t);
            }
            if (t instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) t;
                result.add((Class<?>) parameterizedType.getRawType());
            }
        }
        return result.toArray(new Class<?>[0]);
    }

    /**
     * 获取类型值长度
     *
     * @param type 类型
     * @return 长度
     */
    public static int getValueLength(Class<?> type) {
        //枚举 2字节
        if (type.isEnum())
            return 2 * 8;

        if (ValueLengthDictionary.containsKey(type))
            return ValueLengthDictionary.get(type);
        throw new IllegalArgumentException("无法确定" + type + "类型的长度,因为其不为Obase基元类型");
    }


    /**
     * Db类型转换
     *
     * @param result    值
     * @param typeClass 目标类型
     * @return 转换后的值
     */
    public static Object convertDbValue(Object result, Class<?> typeClass) {

        //enum
        if (typeClass.isEnum()) {
            Enum<?>[] cons = (Enum<?>[]) typeClass.getEnumConstants();
            for (Enum<?> con : cons) {
                if (Objects.equals(String.valueOf(con.ordinal()), String.valueOf(result))) {
                    result = con;
                }
            }
        }
        //int 和 包装类
        else if (int.class.getName().equals(typeClass.getName()) || Integer.class.getName().equals(typeClass.getName())) {
            return Integer.parseInt(result.toString());
        }
        //short 和 包装类
        else if (short.class.getName().equals(typeClass.getName()) || Short.class.getName().equals(typeClass.getName())) {
            return Short.parseShort(result.toString());
        }
        //long 和 包装类
        else if (long.class.getName().equals(typeClass.getName()) || Long.class.getName().equals(typeClass.getName())) {
            return Long.parseLong(result.toString());
        }
        //byte 和 包装类
        else if (byte.class.getName().equals(typeClass.getName()) || Byte.class.getName().equals(typeClass.getName())) {
            return Byte.parseByte(result.toString().trim());
        }
        //char 和 包装类
        else if (char.class.getName().equals(typeClass.getName()) || Character.class.getName().equals(typeClass.getName())) {
            return result.toString().length() > 0 ? result.toString().charAt(0) : '\u0000';
        }
        //float 和 包装类
        else if (float.class.getName().equals(typeClass.getName()) || Float.class.getName().equals(typeClass.getName())) {
            return Float.parseFloat(result.toString());
        }
        //double 和 包装类
        else if (double.class.getName().equals(typeClass.getName()) || Double.class.getName().equals(typeClass.getName())) {
            return Double.parseDouble(result.toString());
        }
        //boolean 和 包装类
        else if (boolean.class.getName().equals(typeClass.getName()) || Boolean.class.getName().equals(typeClass.getName())) {
            if (result.toString().equals("1")) {
                result = true;
            } else if (result.toString().equals("0")) {
                result = false;
            } else {
                result = Boolean.parseBoolean(result.toString());
            }
            //此处装箱
            if (Boolean.class.getName().equals(typeClass.getName()))
                return Boolean.valueOf((boolean) result);
            return result;
        }
        //string
        else if (String.class.getName().equals(typeClass.getName())) {
            return result.toString();
        }
        //BigDecimal
        else if (BigDecimal.class.getName().equals(typeClass.getName())) {
            return new BigDecimal(result.toString());
        }
        //Date
        else if (Date.class.getName().equals(typeClass.getName()) && result instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) result;
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        } else if (Date.class.getName().equals(typeClass.getName()) && result instanceof LocalDate) {
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
            try {
                LocalDate localDate = (LocalDate) result;
                result = (localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                return ft.parse(result.toString());
            } catch (ParseException e) {
                throw new IllegalArgumentException("无妨将" + result + "转换为yyyy-MM-dd的Date");
            }
        } else if (Date.class.getName().equals(typeClass.getName()) && result instanceof LocalTime) {
            SimpleDateFormat ft = new SimpleDateFormat("HH:mm:ss");
            try {
                LocalTime localDate = (LocalTime) result;
                result = (localDate.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                return ft.parse(result.toString());
            } catch (ParseException e) {
                throw new IllegalArgumentException("无法将" + result + "转换为HH:mm:ss的Date");
            }
        } else if (Date.class.getName().equals(typeClass.getName()) && result instanceof String) {
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                result = ft.parse(result.toString());
            } catch (ParseException e) {
                throw new IllegalArgumentException("无法将" + result + "转换为yyyy-MM-dd HH:mm:ss的Date");
            }
        } else if (Date.class.getName().equals(typeClass.getName()) && result instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) result;
            return Date.from(timestamp.toInstant());
        } else if (Date.class.getName().equals(typeClass.getName()) && result instanceof java.sql.Date) {
            java.sql.Date date = (java.sql.Date) result;
            return Date.from(date.toInstant());
        }
        //localDate
        else if (LocalDate.class.getName().equals(typeClass.getName()) && result instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) result;
            return localDateTime.toLocalDate();
        } else if (LocalDate.class.getName().equals(typeClass.getName()) && result instanceof LocalDate) {
            return result;
        } else if (LocalDate.class.getName().equals(typeClass.getName()) && result instanceof String) {
            try {
                return LocalDateTime.parse(result.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")).toLocalDate();
            } catch (DateTimeParseException exception) {
                return LocalDate.parse(result.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd.SSS"));
            }
        } else if (LocalDate.class.getName().equals(typeClass.getName()) && result instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) result;
            return timestamp.toLocalDateTime().toLocalDate();
        } else if (LocalDate.class.getName().equals(typeClass.getName()) && result instanceof java.sql.Date) {
            java.sql.Date date = (java.sql.Date) result;
            return date.toLocalDate();
        }
        //localTime
        else if (LocalTime.class.getName().equals(typeClass.getName()) && result instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) result;
            return localDateTime.toLocalTime();
        } else if (LocalTime.class.getName().equals(typeClass.getName()) && result instanceof LocalTime) {
            return result;
        } else if (LocalTime.class.getName().equals(typeClass.getName()) && result instanceof String) {
            try {
                return LocalDateTime.parse(result.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")).toLocalTime();
            } catch (DateTimeParseException exception) {
                return LocalTime.parse(result.toString(), DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
            }
        } else if (LocalTime.class.getName().equals(typeClass.getName()) && result instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) result;
            return timestamp.toLocalDateTime().toLocalTime();
        } else if (LocalTime.class.getName().equals(typeClass.getName()) && result instanceof java.sql.Time) {
            java.sql.Time time = (java.sql.Time) result;
            return time.toLocalTime();
        }
        //LocalDateTime
        else if (LocalDateTime.class.getName().equals(typeClass.getName()) && result instanceof LocalDateTime) {
            return result;
        } else if (LocalDateTime.class.getName().equals(typeClass.getName()) && result instanceof LocalDate) {
            LocalDate localDate = (LocalDate) result;
            return LocalDateTime.parse(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else if (LocalDateTime.class.getName().equals(typeClass.getName()) && result instanceof LocalTime) {
            LocalTime localTime = (LocalTime) result;
            return LocalDateTime.parse(localTime.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
        } else if (LocalDateTime.class.getName().equals(typeClass.getName()) && result instanceof String) {
            return LocalDateTime.parse(result.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        } else if (LocalDateTime.class.getName().equals(typeClass.getName()) && result instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) result;
            return timestamp.toLocalDateTime();
        } else if (LocalDateTime.class.getName().equals(typeClass.getName()) && result instanceof java.sql.Time) {
            java.sql.Time time = (java.sql.Time) result;
            LocalTime localTime = time.toLocalTime();
            return LocalDateTime.parse(localTime.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
        }
        //UUID
        else if (UUID.class.getName().equals(typeClass.getName()) && result instanceof String) {
            String str = (String) result;
            return UUID.fromString(str);
        }

        return result;
    }

    /**
     * 获取衍生的目标表
     *
     * @param objectType 对象类型
     * @return 衍生的目标表
     */
    public static String getDerivedTargetTable(ObjectType objectType) {
        //如果没有继承 则直接返回当前的目标表
        String result = objectType.getTargetTable();
        ObjectType currentObjectType = (ObjectType) objectType.getDerivingFrom();
        //如果有继承 则一直向上找
        while (currentObjectType != null) {
            result = currentObjectType.getTargetTable();
            currentObjectType = (ObjectType) currentObjectType.getDerivingFrom();
        }

        return result;
    }

    /**
     * 获取衍生的构造函数
     *
     * @param objectType 对象类型
     * @return 衍生的构造函数
     */
    public static IInstanceConstructor getDerivedIInstanceConstructor(ObjectType objectType) {
        //如果没有继承 则直接返回当前的构造函数
        IInstanceConstructor result = objectType.getConstructor();
        ObjectType currentObjectType = (ObjectType) objectType.getDerivingFrom();
        //如果有继承 则一直向上找
        while (currentObjectType != null) {
            result = currentObjectType.getConstructor();
            currentObjectType = (ObjectType) currentObjectType.getDerivingFrom();
        }

        return result;
    }

    /**
     * 获取继承的顶级父类的类型区别标记
     *
     * @param objectType 对象类型
     * @return 继承的顶级父类的类型区别标记
     */
    public static TwoTuple<String, Object> getDerivedConcreteTypeSign(ObjectType objectType) {
        StructuralType derviving = objectType.getDerivingFrom();
        //没有继承别人 但被人继承的 返回标记
        if (derviving == null && objectType.getDerivedTypes().size() > 0) {
            //如果因为没有配置此处出现误判 会在后续检查中处理
            if (objectType.getConcreteTypeSign() != null && objectType.findAttributeByTargetField(objectType.getConcreteTypeSign().getItem1()) == null) {
                return objectType.getConcreteTypeSign();
            }
        }
        //有继承 要一直向上找
        if (derviving != null) {
            ObjectType current = objectType;
            while (derviving != null) {
                current = (ObjectType) derviving;
                derviving = derviving.getDerivingFrom();
            }

            //如果因为没有配置此处出现误判 会在后续检查中处理
            if (current.getConcreteTypeSign() != null && current.findAttributeByTargetField(current.getConcreteTypeSign().getItem1()) == null) {
                return objectType.getConcreteTypeSign();
            }
        }

        return null;
    }

    /**
     * 获取继承链
     *
     * @param targetType 目标结构化类型
     * @return 继承链
     */
    public static List<StructuralType> getDerivingChain(StructuralType targetType) {
        //一路找到顶级
        StructuralType current = targetType;
        StructuralType deriving = targetType.getDerivingFrom();
        //组合成继承链
        List<StructuralType> derivingList = new ArrayList<>();
        derivingList.add(current);
        while (deriving != null) {
            current = deriving;
            derivingList.add(current);
            deriving = current.getDerivingFrom();
        }
        //反序后才是继承链 沿着继承链处理每一个
        Collections.reverse(derivingList);
        return derivingList;
    }

    /**
     * 获取构造器的参数个数
     *
     * @param constructor 构造器
     * @return 构造器的参数个数
     */
    public static int getConstructorParameterCount(IInstanceConstructor constructor) {
        //获取当前构造器参数个数
        int count = constructor.getParameters() != null ? constructor.getParameters().size() : 0;
        //如果构造器是AbstractConstructor 构造器最后一个参数固定为类型区别参数 要减掉
        if (constructor instanceof AbstractConstructor)
            count -= 1;
        return count;
    }

    /**
     * 获取需要定义的外键属性
     *
     * @param objType     对象类型
     * @param returnEnd   外键关联端
     * @param returnedKey 返回的外键
     * @return 定义的外键属性
     */
    public static List<Attribute> getDefinedForeignAttributes(ObjectType objType, AssociationEnd returnEnd, ObjectReferencePack<List<Attribute>> returnedKey) {
        //关联端集合
        List<AssociationEnd> associationEnds = new ArrayList<>();
        if (objType instanceof AssociationType) {
            AssociationType associationType = (AssociationType) objType;
            if (associationType.getVisible() || associationType.getIndependent()) {
                associationEnds.addAll(associationType.getAssociationEnds());
            }

        } else if (objType instanceof EntityType) {
            EntityType entityType = (EntityType) objType;
            //关联引用
            List<AssociationReference> assocRef = entityType.getAssociationReferences();
            for (AssociationReference associationReference : assocRef) {
                AssociationType associationType = associationReference.getAssociationType();
                if (!associationType.getVisible() && !associationType.getIndependent()) {
                    if (associationType.isCompanionEnd(associationReference.getLeftEnd())) {
                        associationEnds.addAll(associationType.getAssociationEnds().stream().filter(p -> !p.getName().equals(associationReference.getLeftEnd())).collect(Collectors.toList()));
                    }
                }
            }
        }

        List<Attribute> returnKey = new ArrayList<>();
        List<Attribute> attrs = new ArrayList<>();
        int i = 1;
        for (AssociationEnd end : associationEnds) {
            for (AssociationEndMapping mapping : end.getMappings()) {
                Attribute attr = objType.findAttributeByTargetField(mapping.getTargetField());
                //找到目标属性
                if (attr == null) {
                    //键的属性
                    Attribute keyAttr = end.getEntityType().getAttribute(mapping.getKeyAttribute());
                    //当前的属性们
                    TypeElement[] attrsArray = attrs.toArray(new TypeElement[0]);
                    String name = objType.nameNew("obase_gen_fk_" + i, attrsArray);
                    i++;
                    //构造一个新属性
                    Attribute newAttr = new Attribute(keyAttr.getDataType(), name);
                    newAttr.setTargetField(mapping.getTargetField());
                    newAttr.setIsForeignKeyDefineMissing(true);
                    attrs.add(newAttr);

                    //与外键关联端相等
                    if (end == returnEnd) returnKey.add(newAttr);
                } else {
                    if (end.equals(returnEnd))
                        returnKey.add(attr);
                }
            }
        }

        returnedKey.realValue = returnKey;
        return attrs;
    }

    /**
     * 制作公开方法的委托取值器
     *
     * @param method 设值方法
     * @return 委托取值器
     */
    public static IValueGetter makeDelegateValueGetter(Method method) {

        if (!Modifier.isPublic(method.getModifiers()))
            throw new IllegalArgumentException(method + "方法不是公开的,不能构造委托取值器,请使用MethodValueGetter");

        try {
            //放置在io.obase.proxy.module.getter软件包下
            String name = "io.obase.proxy.module.getter.DelegateValueGetter_" + method.getDeclaringClass().getName() + "_" + method.getName();

            IValueGetter getter;
            //查询缓存 如果已经构造国相同的 直接取用
            if (GlobalDelegateValueGetterCache.getInstance().getGetter(name) != null) {
                getter = GlobalDelegateValueGetterCache.getInstance().getGetter(name);
            } else {
                ByteBuddy buddy = new ByteBuddy(ClassFileVersion.ofThisVm());

                DynamicType.Builder<Object> builder = buddy.subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS).implement(IValueGetter.class)
                        .name(name);

                builder = builder.defineConstructor(Visibility.PUBLIC).intercept(MethodCall
                        .invoke(Object.class.getDeclaredConstructor())
                        .onSuper());

                builder = builder.defineMethod("getValue", Object.class).withParameter(Object.class)
                        .intercept(MethodCall.invoke(method).onArgument(0).withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC));

                try (DynamicType.Unloaded<?> unloaded = builder.make()) {
                    Class<?> getterClass = unloaded.load(GlobalClassLoaderCache.getInstance().getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded();
                    getter = (IValueGetter) getterClass.getConstructor().newInstance();
                    GlobalDelegateValueGetterCache.getInstance().setGetter(name, getter);
                } catch (IOException e) {
                    throw new RuntimeException("无法创建委托取值器" + name + ",请参考内部异常.", e);
                }
            }
            return getter;
        } catch (Exception exception) {
            throw new RuntimeException("无法创建委托取值器,请参考内部异常.", exception);
        }
    }

    /**
     * 制作公开方法的委托设值器
     *
     * @param method        方法
     * @param parameterType 设置方法的参数类型
     * @return 委托设值器
     */
    public static ValueSetter makeDelegateValueSetter(Method method, Class<?> parameterType) {

        if (!Modifier.isPublic(method.getModifiers()))
            throw new IllegalArgumentException(method + "方法不是公开的,不能构造委托设值器,请使用MethodValueSetter");

        EValueSettingMode mode;
        try {
            String name = "io.obase.proxy.module.setter." + "Action_" + method.getName() + "_" + method.getDeclaringClass().getName() + "_" + method.getParameterTypes()[0].getSimpleName().replace("[", "").replace("]", "");

            ValueSetter setter;
            if (GlobalDelegateValueSetterCache.getInstance().getSetter(name) != null) {
                setter = GlobalDelegateValueSetterCache.getInstance().getSetter(name);
            } else {
                ByteBuddy buddy = new ByteBuddy(ClassFileVersion.ofThisVm());

                TypeDescription.Generic generic = TypeDescription.Generic.Builder.parameterizedType(ActionWithTwoArg.class, method.getDeclaringClass(), parameterType).build();
                DynamicType.Builder<Object> builder = buddy.subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS).implement(generic)
                        .name(name);

                builder = builder.defineConstructor(Visibility.PUBLIC).intercept(MethodCall
                        .invoke(Object.class.getDeclaredConstructor())
                        .onSuper());

                builder = builder.defineMethod("invoke", void.class).withParameters(method.getDeclaringClass(), parameterType)
                        .intercept(MethodCall.invoke(method).onArgument(0).withArgument(1).withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC));

                //加载至JVM
                try (DynamicType.Unloaded<?> unloaded = builder.make()) {
                    Class<?> clazzAction = unloaded.load(GlobalClassLoaderCache.getInstance().getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded();
                    mode = EValueSettingMode.Assignment;
                    setter = new DelegateValueSetter<>((ActionWithTwoArg<?, ?>) clazzAction.getConstructor().newInstance(), mode, method.getParameterTypes()[0]);
                    GlobalDelegateValueSetterCache.getInstance().setSetter(name, setter);
                } catch (IOException e) {
                    throw new RuntimeException("无法创建委托取值器" + name + ",请参考内部异常.", e);
                }
            }
            return setter;

        } catch (Exception e) {
            throw new IllegalArgumentException("无法创建委托设值器,请参考内部异常.", e);
        }
    }

    /**
     * 获取字段的名称
     *
     * @param attribute 属性
     * @param context   版本上下文
     * @return 字段的名称
     */
    public static String getAttributeFiledName(Attribute attribute, VersionCombinationContext context) {
        String connectionStr = "";
        if (context.getParentAttribute() != null) {
            List<Attribute> attributeList = new ArrayList<>();
            while (context.getParentAttribute().iterator().hasNext()) {
                attributeList.add(context.getParentAttribute().getNext());
            }

            if (attributeList.stream().findFirst().isPresent() && attributeList.stream().findFirst().get() instanceof ComplexAttribute) {
                ComplexAttribute complexAttr = (ComplexAttribute) attributeList.stream().findFirst().get();
                if (complexAttr.getMappingConnectionChar() != (char) -1) {
                    connectionStr = complexAttr.getTargetField() + complexAttr.getMappingConnectionChar();
                }
            }
        }
        //字段全名
        return connectionStr + attribute.getTargetField();
    }

    /**
     * 获取依赖注入的服务,获取不到容器或者服务则抛出异常
     *
     * @param contextType  所属上下文类型
     * @param serviceClass 服务类型
     * @param <TService>   服务类型
     * @return 服务
     */
    public static <TService> TService getDependencyInjectionService(Class<?> contextType, Class<TService> serviceClass) {
        //获取依赖注入服务
        ServiceContainer container = ServiceContainerInstance.getInstance().getServiceContainer(contextType);
        if (container == null)
            throw new IllegalArgumentException("无法找到" + contextType.getName() + "的依赖注入容器,请使用ObaseDependencyInjection注册并建造服务容器.");

        TService service = container.getService(serviceClass);

        if (service == null)
            throw new IllegalArgumentException("无法找到" + contextType.getName() + "的" + serviceClass.getName() + "服务,请使用ObaseDependencyInjection注册" + serviceClass + "为服务.");

        return service;
    }

    /**
     * 获取依赖注入的服务,如果获取不到容器或者服务则返回null
     *
     * @param contextType  所属上下文类型
     * @param serviceClass 服务类型
     * @param <TService>   服务类型
     * @return 服务
     */
    public static <TService> TService getDependencyInjectionServiceOrNull(Class<?> contextType, Class<TService> serviceClass) {
        //获取依赖注入服务
        ServiceContainer container = ServiceContainerInstance.getInstance().getServiceContainer(contextType);
        if (container == null)
            return null;

        return container.getService(serviceClass);
    }

    /**
     * 为类型元素配置取值器和设值器
     *
     * @param property    属性
     * @param typeElement 类型元素配置
     */
    public static void configureValueGetterAndSetter(Property property, TypeElementConfigurationGeneric<?, ?> typeElement) {
        //取值器
        //取值方法是可读还是公开的
        if (property.getGetterMethod() != null) {
            typeElement.hasValueGetter(property.getGetterMethod());
        }

        //设值器
        //设值方法是可读还是公开的
        if (property.getSetterMethod() != null) {
            //有公开的设值方法
            Class<?> parType = property.getPropertyType();

            EValueSettingMode model = EValueSettingMode.Assignment;
            if (parType != String.class && Iterable.class.isAssignableFrom(parType))
                model = EValueSettingMode.Appending;

            typeElement.hasValueSetter(ValueSetter.create(property.getSetterMethod(), model));
        } else {
            try {
                //找set+属性名
                Method method = property.getGetterMethod().getDeclaringClass().getDeclaredMethod("set" + property.getName(), property.getPropertyType());
                typeElement.hasValueSetter(new MethodValueSetter(method));
            } catch (NoSuchMethodException ignored) {
                //找不到 不配置
            }
        }
    }

    /**
     * 根据名称查找对应的属性
     *
     * @param clrType 要查找的类型
     * @param name    名称
     * @return 属性
     */
    public static Property getProperty(Class<?> clrType, String name) {
        List<Property> properties = ObaseIntrospector.getObaseBeanProperties(clrType);

        Property property = properties.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);

        if (property == null)
            throw new IllegalArgumentException("无法为在" + clrType.getName() + "找到" + name + ",请检查名称是否正确");

        return property;
    }

    /**
     * 是否存在符合标识推断的属性
     *
     * @param type          要推断的类型
     * @param propertyInfos 符合的属性
     * @return 是否存在符合标识推断的属性
     */
    public static boolean existIdentity(Class<?> type, ObjectReferencePack<List<Property>> propertyInfos) {
        List<String> keyAttrName = new ArrayList<>();
        keyAttrName.add("code");
        keyAttrName.add("id");
        keyAttrName.add(type.getSimpleName().toLowerCase() + "code");
        keyAttrName.add(type.getSimpleName().toLowerCase() + "id");

        //是否为以上四种名称 且 是int long 和 string
        List<Property> result = ObaseIntrospector.getObaseBeanProperties(type).stream()
                .filter(p -> keyAttrName.contains(p.getName().toLowerCase())
                        && (p.getPropertyType().equals(short.class) || p.getPropertyType().equals(Short.class) ||
                        p.getPropertyType().equals(int.class) || p.getPropertyType().equals(Integer.class) ||
                        p.getPropertyType().equals(long.class) || p.getPropertyType().equals(Long.class) ||
                        p.getPropertyType().equals(String.class)))
                .collect(Collectors.toList());

        propertyInfos.realValue = result;

        return result.size() > 0;
    }

    /**
     * 从输入流中读取UTF8字符串
     * 注意此方法会一次性的读取所有流内数据 适用于数据量较小的数据
     *
     * @param serializationStream 输入流
     * @return 字符串
     */
    public static String readUtf8StringFromInputStream(InputStream serializationStream) {
        //初始化一个输出流
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            //从输入流内读取所有的数据
            while ((length = serializationStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            //按照UTF8返回
            return new String(result.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException ioException) {
            throw new RuntimeException("从输入流中读取UTF8字符串发生错误," + ioException.getMessage(), ioException);
        }
    }

    /**
     * 向输出流中写入UTF8字符串的值
     * 注意此方法会一次性的写入所有数据至流内 适用于数据量较小的数据
     *
     * @param valueString         字符串值
     * @param serializationStream 输出流
     */
    public static void writeUtf8StringToOutputStream(String valueString, OutputStream serializationStream) {
        //获取UTF8数组
        byte[] valueBytes = valueString.getBytes(StandardCharsets.UTF_8);
        try {
            serializationStream.write(valueBytes);
        } catch (IOException ioException) {
            throw new RuntimeException("向输出流中写入UTF8字符串发生错误," + ioException.getMessage(), ioException);
        }
    }

    /**
     * 比较是否一一对应
     *
     * @param strings1 第一个集合
     * @param strings2 第二个集合
     * @return 是否一一对应
     */
    public static boolean sequenceEqual(String[] strings1, String[] strings2) {
        if (strings1.length != strings2.length)
            return false;

        boolean result = true;
        for (int i = 0; i < strings1.length; i++) {
            if (!strings1[i].equals(strings2[i])) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * 获取配置的继承链
     *
     * @param targetTypeConfiguration 起始结构化配置
     * @param modelBuilder            建模器
     * @return 结构化配置的继承链
     */
    public static List<StructuralTypeConfiguration<?>> getDerivingConfigChain(StructuralTypeConfiguration<?> targetTypeConfiguration, ModelBuilder modelBuilder) {
        //一路找到顶级
        StructuralTypeConfiguration<?> current = targetTypeConfiguration;
        Class<?> deriving = targetTypeConfiguration.getDerivedFrom();
        //组合成继承链
        List<StructuralTypeConfiguration<?>> derivingList = new ArrayList<>();
        derivingList.add(current);
        while (deriving != null) {
            current = modelBuilder.findConfiguration(deriving);
            derivingList.add(current);
            deriving = current.getDerivedFrom();
        }
        //反序后才是继承链 沿着继承链处理每一个
        Collections.reverse(derivingList);
        return derivingList;
    }

    /**
     * 获取某个内省属性上的标注
     *
     * @param property       内省属性
     * @param annotationClas 标注类
     * @param <T>            标注类型
     * @return 标注
     */
    public static <T extends Annotation> T getAnnotation(Property property, Class<T> annotationClas) {
        T result = null;
        if (property.getGetterMethod() != null) {
            result = property.getGetterMethod().getAnnotation(annotationClas);
        }
        if (result == null && property.getSetterMethod() != null) {
            result = property.getSetterMethod().getAnnotation(annotationClas);
        }

        return result;
    }

    /**
     * 获取某个内省属性上的所有的标注
     *
     * @param property 内省属性
     * @return 标注
     */
    public static Annotation[] getAnnotations(Property property) {
        List<Annotation> result = new ArrayList<>();
        if (property.getGetterMethod() != null) {
            result.addAll(Arrays.asList(property.getGetterMethod().getAnnotations()));
        }
        if (property.getSetterMethod() != null) {
            result.addAll(Arrays.asList(property.getSetterMethod().getAnnotations()));
        }

        return result.toArray(new Annotation[0]);
    }

    /**
     * 获取元组的泛型参数集合
     *
     * @param field 定义为元组的字段
     * @return 元组的泛型参数集合
     */
    public static Class<?>[] getTupleGenericTypeArguments(Field field) {

        List<Class<?>> result = new ArrayList<>();

        ParameterizedType realType;
        if (field.getType() != String.class &&
                Iterable.class.isAssignableFrom(field.getType())) {

            Type[] actualTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            //单个泛型参数
            if (actualTypes.length == 1) {
                if (actualTypes[0] instanceof ParameterizedType) {
                    realType = (ParameterizedType) actualTypes[0];
                } else {
                    throw new IllegalArgumentException("暂不支持此种类型的泛型参数获取,原始类型为" + field.getType().getName());
                }
            }
            //多个泛型参数
            else {
                throw new IllegalArgumentException("暂不支持多个泛型参数的泛型参数获取,原始类型为" + field.getType() + ",泛型参数集合为[" + Arrays.stream(actualTypes).map(Type::getTypeName).collect(Collectors.joining(",")) + "].");
            }
        } else {
            realType = (ParameterizedType) field.getGenericType();
        }


        Type[] parameterizedTypes = realType.getActualTypeArguments();

        for (Type type : parameterizedTypes) {
            if (type instanceof Class<?>) {
                result.add((Class<?>) type);
            } else if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                result.add((Class<?>) parameterizedType.getRawType());
            } else {
                throw new IllegalArgumentException("暂不支持此种类型的泛型参数获取,原始类型为" + field.getType().getName());
            }
        }

        return result.toArray(new Class<?>[0]);
    }

    /**
     * 获取自己和继承类的区分标记值
     *
     * @param structuralType 结构化类型
     * @return 区分标记值
     */
    public static List<Object> getDerivingConcreteTypeValue(StructuralType structuralType) {
        //加入自己的区分标记
        List<Object> result = new ArrayList<>();
        result.add(structuralType.getConcreteTypeSign().getItem2());
        for (StructuralType derivedType : structuralType.getDerivedTypes()) {
            //加入自己继承类的区分标记
            result.addAll(getDerivingConcreteTypeValue(derivedType));
        }
        return result;
    }
}
