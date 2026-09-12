/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象数据模型,此模型全局应只有一个.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-26 15:58:46
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.odm.serialization.SerializationObjectDataModel;
import io.obase.core.odm.typeviews.TypeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;

/**
 * 表示对象数据模型
 * <p>
 * 模型在建造完成后会被多个线程共享（全局模型缓存），并且在视图查询过程中仍会向模型添加类型，
 * 因此本类的容器均使用并发容器或写时复制的快照，读侧无需加锁。
 */
public class ObjectDataModel {

    /**
     * 邮戳锁，用于保护types快照的写入
     */
    private final StampedLock stampedLock = new StampedLock();

    /**
     * clr类型与代理类型字典
     */
    private final Map<Class<?>, Class<?>> proxyReal = new ConcurrentHashMap<>();

    /**
     * 序列化对象数据模型
     */
    private final SerializationObjectDataModel serializationModel = new SerializationObjectDataModel();

    /**
     * clr类型与模型字典
     */
    private final Map<Class<?>, StructuralType> structuralTypes = new ConcurrentHashMap<>();

    /**
     * 模型存储标记
     */
    private StorageSymbol storageSymbol;

    /**
     * 模型中的所有类型
     * 写时复制：写入时整体替换，读取方拿到的始终是一个不会再变的快照
     */
    private volatile List<StructuralType> types = new ArrayList<>();

    /**
     * clr类型与模型字典
     *
     * @return clr类型与模型字典
     */
    public Map<Class<?>, StructuralType> getStructuralTypes() {
        return this.structuralTypes;
    }

    /**
     * 获取序列化对象数据模型
     *
     * @return 序列化对象数据模型
     */
    public SerializationObjectDataModel getSerializationModel() {
        return this.serializationModel;
    }

    /**
     * 获取模型中的所有类型
     *
     * @return 模型中的所有类型
     */
    public List<StructuralType> getTypes() {
        return this.types;
    }


    /**
     * 获取存储标记
     *
     * @return 默认存储标记
     */
    public StorageSymbol getStorageSymbol() {
        return this.storageSymbol;
    }

    /**
     * 设置存储标记
     *
     * @param storageSymbol 默认存储标记
     * @throws IllegalArgumentException 只能设置一次默认存储标记，对已设置的默认存储标记进行修改将引发异常。
     */
    public void setStorageSymbol(StorageSymbol storageSymbol) {
        if (this.storageSymbol != null)
            throw new IllegalArgumentException("只能设置一次默认存储标记");
        this.storageSymbol = storageSymbol;
    }

    /**
     * 根据指定的CLR类型获取实体型
     *
     * @param type CLR类型
     * @return 实体型
     */
    public EntityType getEntityType(Class<?> type) {
        TypeBase typeBase = this.getTypeOrNull(type);
        if (typeBase instanceof EntityType)
            return (EntityType) typeBase;
        return null;
    }

    /**
     * 根据指定的CLR类型获取关联型
     *
     * @param type CLR类型
     * @return 关联型
     */
    public AssociationType getAssociationType(Class<?> type) {
        TypeBase typeBase = this.getTypeOrNull(type);
        if (typeBase instanceof AssociationType)
            return (AssociationType) typeBase;
        return null;
    }

    /**
     * 根据指定的CLR类型获取复杂类型
     *
     * @param type CLR类型
     * @return 复杂类型
     */
    public ComplexType getComplexType(Class<?> type) {
        TypeBase result = this.getTypeOrNull(type);
        if (result instanceof ComplexType) {
            return (ComplexType) result;
        }
        return null;
    }

    /**
     * 根据指定的CLR类型在模型中搜索对象类型
     *
     * @param type CLR类型
     * @return 对象类型
     */
    public ObjectType getObjectType(Class<?> type) {
        TypeBase typeBase = this.getTypeOrNull(type);
        if (typeBase instanceof ObjectType) {
            return (ObjectType) typeBase;
        }
        return null;
    }

    /**
     * 根据指定的CLR类型在模型中搜索相应的类型
     *
     * @param type CLR类型
     * @return 结构化类型
     */
    public StructuralType getStructuralType(Class<?> type) {
        TypeBase typeBase = this.getTypeOrNull(type);
        return typeBase instanceof StructuralType ? (StructuralType) typeBase : null;
    }

    /**
     * 据指定的类型名称及其所在命名空间在模型中搜索相应的类型
     *
     * @param nameSpace 命名空间
     * @param name      类型名称
     * @return 结构化类型
     */
    public StructuralType getStructuralType(String nameSpace, String name) {
        //构造类型名
        TypeName typeName = new TypeName();
        typeName.Namespace = nameSpace;
        typeName.Name = name;

        for (StructuralType structuralType : this.structuralTypes.values())
            if (structuralType.getTypeName() == typeName)
                return structuralType;
        return null;
    }

    /**
     * 向模型添加类型
     *
     * @param modelType 要添加到模型中的类型（实体型、关联型、复杂类型）
     */
    public void addType(StructuralType modelType) {
        if (modelType == null) throw new IllegalArgumentException("modelType不能为null");
        //先指定结构类型所属的模型，再向容器发布，保证任何读到该类型的线程都能看到已关联的模型
        modelType.setModel(this);
        //覆盖原有的类型
        Class<?> clrType = modelType.clrType;
        if (clrType != null)
            this.structuralTypes.put(clrType, modelType);
        //写时复制的方式发布类型快照，读取types的一方无需加锁
        long stamp = this.stampedLock.writeLock();
        try {
            if (!this.types.contains(modelType)) {
                List<StructuralType> snapshot = new ArrayList<>(this.types);
                snapshot.add(modelType);
                this.types = snapshot;
            }
        } finally {
            this.stampedLock.unlockWrite(stamp);
        }
        //如果有代理类型，则将代理类型与实际类型映射
        Class<?> proxyType = modelType.getProxyType();
        if (proxyType != null)
            this.proxyReal.put(proxyType, clrType);
    }

    /**
     * 获取指定CLR类型的模型类型
     *
     * @param type 类型
     * @return 类型
     * @throws UnknownTypeException 不能识别的类型
     */
    public TypeBase getType(Class<?> type) {
        TypeBase result = this.getTypeOrNull(type);
        if (result == null) throw new UnknownTypeException(type);
        return result;
    }

    /**
     * 根据指定的CLR类型在模型中搜索可发出引用类型
     *
     * @param type CLR类型
     * @return 主引类型
     */
    public ReferringType getReferringType(Class<?> type) {
        TypeBase result = this.getTypeOrNull(type);
        if (result == null)
            return null;
        return (ReferringType) result;
    }

    /**
     * 根据指定的CLR类型在模型中搜索可发出引用类型
     *
     * @param type CLR类型
     * @return 类型视图
     */
    public TypeView getTypeView(Class<?> type) {
        StructuralType structType = this.getStructuralType(type);
        if (structType == null) throw new IllegalArgumentException("CLR类型对应的在模型中不存在");
        return (TypeView) structType;
    }

    /**
     * 获取指定CLR类型的模型类型
     *
     * @param type CLr类型
     * @return 如果不存在相应的类型（既不为预定义的基元类型，又未在模型中注册为结构化类型）则返回null
     */
    public TypeBase getTypeOrNull(Class<?> type) {
        //从代理类型里取
        if (this.proxyReal.get(type) != null) type = this.proxyReal.get(type);
        //取出clr类型对应模型
        TypeBase result = this.structuralTypes.get(type);
        //是否为系统基元类型
        if (PrimitiveType.isObasePrimitive(type))
            return PrimitiveType.fromType(type);
        return result;
    }

    /**
     * 检测模型中是否存在指定的类型
     *
     * @param type 程序语言中的类型
     * @return 如果存在返回true，否则返回false。
     */
    public boolean exist(Class<?> type) {
        return this.structuralTypes.containsKey(type);
    }

    /**
     * 创建代理类型映射，即为模型类型的CLR类型指定一个代理类型
     *
     * @param type      实际类型
     * @param proxyType 代理类型
     */
    void createProxyMapping(Class<?> type, Class<?> proxyType) {
        if (proxyType == null) return;
        //移除该实际类型原有的代理类型映射
        for (Map.Entry<Class<?>, Class<?>> pair : this.proxyReal.entrySet())
            if (pair.getValue() == type)
                this.proxyReal.remove(pair.getKey());
        //添加新的代理类型映射（重复登记同一个代理类型不会引发异常）
        this.proxyReal.put(proxyType, type);
    }
}
