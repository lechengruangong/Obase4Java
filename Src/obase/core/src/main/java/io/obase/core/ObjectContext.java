/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象上下文.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-13 15:25:46
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.EIsolationLevel;
import io.obase.core.common.EventHandler;
import io.obase.core.mapping.pipeline.ConcreteModule;
import io.obase.core.mapping.pipeline.IMappingModule;
import io.obase.core.odm.*;
import io.obase.core.query.QueryProvider;
import io.obase.core.query.heterog.HeterogQueryProvider;
import io.obase.core.saving.ObjectSystemVisitor;
import io.obase.core.saving.SavingProvider;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对象上下文，负责为应用程序提供接口。
 */
public abstract class ObjectContext {

    /**
     * 配置提供程序
     */
    private final ContextConfigProvider configProvider;

    /**
     * 对象数据模型
     */
    protected ObjectDataModel model;
    /**
     * 新对象集合
     */
    protected HashMap<Object, ObjectHouse> newObjects;
    /**
     * 对象仓集合
     */
    protected List<ObjectHouse> objectHouses;
    /**
     * 旧对象集合
     */
    protected HashMap<Object, ObjectHouse> oldObjects;
    /**
     * 本地事务是否开始
     */
    private boolean transactionBegun;
    /**
     * Initializing（开始初始化）事件，在执行第一项初始化任务前引发
     */
    private EventHandler<EventObject> initializing;

    /**
     * PreCreatedModel（预建模）事件，在即将开始建模前引发
     */
    private EventHandler<EventObject> preCreateModel;

    /**
     * PostCreatedModel（建模完成）事件，在建模刚完成时引发
     */
    private EventHandler<PostCreateModelEventArgs> postCreatedModel;

    /**
     * PostRegisterModule（模块注册）事件，在每注册完一个映射模块时引发
     */
    private EventHandler<PostRegisterModuleEventArgs> postRegisterModule;

    /**
     * Initialized（初始化完成）事件，在执行完最后一项初始化任务后引发
     */
    private EventHandler<EventObject> initialized;

    /**
     * 构造ObjectContext对象
     *
     * @param provider 配置提供器
     */
    protected ObjectContext(ContextConfigProvider provider) {

        if (provider == null)
            throw new IllegalArgumentException("不能用空的对象上下文配置提供者初始化对象上下文.");

        this.onInitializing();

        this.configProvider = provider;
        this.configProvider.setObjectContext(this);

        this.onPreCreateModel();

        //获取模型键
        Class<?> cacheKey = this.getClass();
        //获取模型
        this.model = GlobalModelCache.getInstance().getModel(cacheKey);


        if (this.model == null) {
            GlobalModelCache.getInstance().setModel(cacheKey, this.configProvider);
            this.model = GlobalModelCache.getInstance().getModel(cacheKey);
        }
        this.configProvider.model = this.model;

        this.onPostCreatedModel(new PostCreateModelEventArgs(this, this.model));

        //自动创建对象集合对象
        if (this.configProvider.getWhetherCreateSet()) {
            java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                //如果定义了ObjectSet 且没有初始化
                try {
                    if (f.getType().equals(ObjectSet.class) && f.get(this) == null) {
                        Type[] parameterizedTypes = ((ParameterizedType) f.getGenericType()).getActualTypeArguments();
                        Class<?> setType = null;
                        //必然是单个泛型参数
                        if (parameterizedTypes[0] instanceof Class<?>) {
                            setType = (Class<?>) parameterizedTypes[0];
                        } else if (parameterizedTypes[0] instanceof ParameterizedType) {
                            ParameterizedType parameterizedType = (ParameterizedType) parameterizedTypes[0];
                            setType = (Class<?>) parameterizedType.getRawType();
                        }
                        if (setType != null)
                            f.set(this, new ObjectSet<>(this, setType));
                    }
                } catch (IllegalAccessException e) {
                    //忽略即可
                }
            }
        }

        //处理继承类的判别标识 如果有 注册模块
        if (this.model.getTypes().stream().anyMatch(p -> p.getConcreteTypeSign() != null))
            this.registerModule(new ConcreteModule());

        this.OnInitialized();
    }

    /**
     * 获取当前上下文使用的对象数据模型
     *
     * @return 当前上下文使用的对象数据模型
     */
    public ObjectDataModel getModel() {
        return this.model;
    }

    /**
     * 获取配置提供程序
     *
     * @return 配置提供程序
     */
    public ContextConfigProvider getConfigProvider() {
        return this.configProvider;
    }

    /**
     * 获取是否开始本地事务
     *
     * @return 是否开始本地事务
     */
    public boolean getTransactionBegun() {
        return this.transactionBegun;
    }

    /**
     * Initializing（开始初始化）事件，在执行第一项初始化任务前引发
     *
     * @return 开始初始化事件
     */
    public EventHandler<EventObject> getInitializing() {
        if (this.initializing == null)
            this.initializing = new EventHandler<>();
        return this.initializing;
    }

    /**
     * PreCreatedModel（预建模）事件，在即将开始建模前引发
     *
     * @return 预建模事件
     */
    public EventHandler<EventObject> getPreCreateModel() {
        if (this.preCreateModel == null)
            this.preCreateModel = new EventHandler<>();
        return this.preCreateModel;
    }

    /**
     * PostCreatedModel（建模完成）事件，在建模刚完成时引发
     *
     * @return 建模完成事件
     */
    public EventHandler<PostCreateModelEventArgs> getPostCreatedModel() {
        if (this.postCreatedModel == null)
            this.postCreatedModel = new EventHandler<>();
        return this.postCreatedModel;
    }

    /**
     * PostRegisterModule（模块注册）事件，在每注册完一个映射模块时引发
     *
     * @return 模块注册事件
     */
    public EventHandler<PostRegisterModuleEventArgs> getPostRegisterModule() {
        if (this.postRegisterModule == null)
            this.postRegisterModule = new EventHandler<>();
        return this.postRegisterModule;
    }

    /**
     * Initialized（初始化完成）事件，在执行完最后一项初始化任务后引发
     *
     * @return 初始化完成事件
     */
    public EventHandler<EventObject> getInitialized() {
        if (this.initialized == null)
            this.initialized = new EventHandler<>();
        return this.initialized;
    }

    /**
     * 将对象标记为已删除
     *
     * @param obj 要标记为删除的对象
     * @param <T> 对象的类型
     */
    public <T> void remove(T obj) {
        //获取对象的模型
        ObjectType ot = this.getModel().getObjectType(obj.getClass());
        //获取删除对象的标识（标识:通过模型的主键标识）
        ObjectKey key = ObjectSystemVisitor.getObjectKey(obj, ot);
        //如果上下文中不存在这个旧对象则不执行任何操作
        if (!this.oldObjects.containsKey(key))
            return;
        ObjectHouse house = this.oldObjects.get(key);
        if (house != null) {
            //标记为删除状态（SaveChanges时删除）
            house.remove();
        } else {
            //新对象不存在者返回
            if (!this.newObjects.containsKey(obj))
                return;
            ObjectHouse h = this.newObjects.get(obj);
            if (h != null) {
                //从新对象字典移除
                this.newObjects.remove(obj);
                //从对象仓集合移除
                this.objectHouses.remove(h);
            }
        }
    }


    /**
     * 在对象上下文中创建一个对象集
     *
     * @param type 对象类型
     * @return 对象集
     */
    public <T> ObjectSet<T> createSet(Class<T> type) {
        return new ObjectSet<>(this, type);
    }

    /**
     * 使用无参构造函数创建对象的新实例并附加到上下文
     * 默认使用HasNewInstanceConstructor配置的新实例构造函数 未配置时使用HasConstructor配置的构造函数
     *
     * @param typeClass 类型
     * @return 创建出的对象
     */
    public <T> T Create(Class<T> typeClass) {
        return this.createSet(typeClass).create(typeClass);
    }


    /**
     * 使用参数创建对象的新实例并附加到上下文
     * 默认使用HasNewInstanceConstructor配置的新实例构造函数 未配置时使用HasConstructor配置的构造函数
     *
     * @param typeClass 类型
     * @param parameter 构造函数参数
     * @return 创建出的对象
     */
    public <T> T Create(Class<T> typeClass, Object... parameter) {
        return this.createSet(typeClass).create(typeClass, parameter);
    }

    /**
     * 向当前对象上下文注册映射模块
     *
     * @param module 要注册的模块
     */
    public void registerModule(IMappingModule module) {
        //提供程序
        SavingProvider saveProvider = this.configProvider.getSavingProvider();
        QueryProvider queryProvider = this.configProvider.getQueryProvider();

        module.init(saveProvider, saveProvider, queryProvider, saveProvider, this);
        this.onPostRegisterModule(new PostRegisterModuleEventArgs(this, module));
    }

    /**
     * Initializing（开始初始化）事件，在执行第一项初始化任务前引发
     */
    private void onInitializing() {
        if (this.initializing != null)
            this.initializing.publishEvent(new EventObject(this));
    }

    /**
     * PreCreatedModel（预建模）事件，在即将开始建模前引发
     */
    private void onPreCreateModel() {
        if (this.preCreateModel != null)
            this.preCreateModel.publishEvent(new EventObject(this));
    }

    /**
     * PostCreatedModel（建模完成）事件，在建模刚完成时引发
     *
     * @param e 建模完成事件数据
     */
    private void onPostCreatedModel(PostCreateModelEventArgs e) {
        if (this.postCreatedModel != null)
            this.postCreatedModel.publishEvent(e);
    }

    /**
     * PostRegisterModule（模块注册）事件，在每注册完一个映射模块时引发
     *
     * @param e 模块注册事件数据
     */
    private void onPostRegisterModule(PostRegisterModuleEventArgs e) {
        if (this.postRegisterModule != null)
            this.postRegisterModule.publishEvent(e);
    }

    /**
     * Initialized（初始化完成）事件，在执行完最后一项初始化任务后引发
     */
    private void OnInitialized() {
        if (this.initialized != null)
            this.initialized.publishEvent(new EventObject(this));
    }

    /**
     * 附加对象到对象上下文
     *
     * @param obj 要附加的对象
     * @param <T> 对象类型
     */
    public <T> void attach(T obj) {
        if (!this.attached(obj)) {
            ObjectReferencePack<T> objRef = new ObjectReferencePack<>();
            objRef.realValue = obj;
            this.attach(objRef, true, true);
        }
    }

    /**
     * 附加对象到对象上下文
     *
     * @param obj    要附加的对象
     * @param isNew  指示要附加的对象是否为新创建的
     * @param asRoot 指示要附加的对象是否为根对象
     * @param <T>    对象类型
     */
    public <T> void attach(ObjectReferencePack<T> obj, boolean isNew, boolean asRoot) {
        //根据对象类型获取模型对象
        ObjectType objectType = this.model.getObjectType(obj.realValue.getClass());
        if (objectType == null)
            throw new IllegalArgumentException(obj.realValue.getClass() + "没有在模型中注册,无法附加.");
        if (this.objectHouses == null)
            this.objectHouses = new ArrayList<>();
        if (isNew) //新对象（表示要添加到数据库的对象）
        {
            if (this.newObjects == null)
                this.newObjects = new HashMap<>();
            if (this.newObjects.containsKey(obj.realValue)) return;
            //为新对象创建一个对象仓
            ObjectHouse house = new ObjectHouse(this);
            //将新对象放入对象仓
            house.putIn(obj.realValue, objectType, true, asRoot);
            //将新的对象仓放入当前上下文的对象仓集合
            this.objectHouses.add(house);
            //放入新对象集合
            this.newObjects.put(obj.realValue, house);
        } else//老（旧）对象（不是新对象）
        {
            //获取老对象(老对象：查出来的都是老对象)的标识（标识:通过模型的主键标识）
            ObjectKey objKey = ObjectSystemVisitor.getObjectKey(obj.realValue, objectType);
            if (this.oldObjects == null)
                this.oldObjects = new HashMap<>();
            if (this.oldObjects.containsKey(objKey)) {

                //旧对象
                Object oldObj = this.oldObjects.get(objKey).getObject();

                //检测当前模型对象中所有引用对象和属性
                //检查_oldObj的引用元素（记为R），如果值为空，进一步检查obj中该元素的值是否为空，如果不为空，将其值赋给R。
                //覆盖属性的算法类同。
                this.assimilate(obj.realValue, objectType, oldObj);

                obj.realValue = (T) oldObj;

                //如果这个对象最终按照根对象附加 则此处覆盖对象仓的asRoot
                if (asRoot != this.oldObjects.get(objKey).getAsRoot() && asRoot)
                    this.oldObjects.get(objKey).overwriteRootTag();

            } else {
                //为老对象建立对象仓
                ObjectHouse house = new ObjectHouse(this);
                //将老对象装入仓里
                house.putIn(obj.realValue, objectType, false, asRoot);
                //加入对象仓集合
                this.objectHouses.add(house);
                //加入旧对象字典
                this.oldObjects.put(objKey, house);
            }
        }
    }

    /**
     * 检查指定的对象是否已附加到对象上下文
     *
     * @param obj 要检查的对象
     * @return 是否已附加到对象上下文
     */
    public boolean attached(Object obj) {
        //检查对象是否存在上下文件（当做新对象和老对象检查）
        return this.attached(obj, true) || this.attached(obj, false);
    }

    /**
     * 检查指定的对象是否已附加到对象上下文
     *
     * @param obj   要检查的对象
     * @param isNew 指示要检查的对象是否为新创建的对象
     * @return 是否已附加到对象上下文
     */
    public boolean attached(Object obj, boolean isNew) {
        if (isNew) //新对象
        {
            //检查这个新对象是否存在上下（是否已被附加）
            if (this.newObjects == null || obj == null)
                return false;
            return this.newObjects.containsKey(obj);
        }
        if (this.oldObjects == null || obj == null)
            return false;
        //获取老对象的标识（标识:通过模型的主键标识）
        ObjectKey key = ObjectSystemVisitor.getObjectKey(obj, this.model.getStructuralType(obj.getClass()));
        //检查老对象是否存在
        return this.oldObjects.containsKey(key);
    }

    /**
     * 检查指定的对象是否已附加到对象上下文
     *
     * @param obj   要检查的对象
     * @param house 当对象已附加时返回对象所在的对象仓
     * @return 是否已附加到对象上下文
     */
    private boolean attached(Object obj, ObjectReferencePack<ObjectHouse> house) {
        //检查对象是否存在上下文中（当做新对象和老对象检查），如果存在返回存在的对象仓
        return this.attached(obj, false, house) || this.attached(obj, true, house);
    }

    /**
     * 检查指定的对象是否已附加到对象上下文
     *
     * @param obj   要检查的对象
     * @param isNew 指示要检查的对象是否为新创建的对象
     * @param house 对象仓
     * @return 是否已附加到对象上下文
     */
    private boolean attached(Object obj, boolean isNew, ObjectReferencePack<ObjectHouse> house) {
        boolean result = false;

        if (isNew) {
            //检查新对象是否存在
            if (this.newObjects == null || obj == null)
                return false;
            if (this.newObjects.containsKey(obj)) {
                //返回存在的对象仓
                result = true;
                house.realValue = this.newObjects.get(obj);
            }
        } else {
            //检查老对象是否存在
            if (this.oldObjects == null || obj == null)
                return false;
            //获取老对象的标识（标识:通过模型的主键标识）
            ObjectKey key = ObjectSystemVisitor.getObjectKey(obj, this.model.getStructuralType(obj.getClass()));
            if (this.oldObjects.containsKey(key)) {
                //返回存在的对象仓
                result = true;
                house.realValue = this.oldObjects.get(key);
            }
        }

        return result;
    }

    /**
     * 检查指定的对象是否已附加到对象上下文
     *
     * @param obj        要检查的对象
     * @param house      当对象已附加时返回对象所在的对象仓
     * @param assimilate 对象已附加时，指示是否使用传入的对象覆盖其属性和引用元素
     * @return 是否已附加到对象上下文
     */
    boolean attached(Object obj, ObjectReferencePack<ObjectHouse> house, boolean assimilate) {
        //检查对象是否存在上下文中（当做新对象和老对象检查），如果存在返回存在的对象仓
        //先作为旧对象检查，然后作为新对象检查。
        //如果assimilate==true，首先检查已附加的对象与传入的对象是否为同一对象，如果不是则用传入对象的属性和引用元素值覆盖已附加的对象。覆盖引用算法如下：
        //检查_oldObj的引用元素（记为R），如果值为空，进一步检查obj中该元素的值是否为空，如果不为空，将其值赋给R。
        //覆盖属性的算法类同
        return this.attached(obj, false, house, assimilate) || this.attached(obj, true, house, assimilate);
    }

    /**
     * 检查指定的对象是否已附加到对象上下文
     *
     * @param obj        要检查的对象
     * @param isNew      作为新对象还是旧对象检查
     * @param house      当对象已附加时返回对象所在的对象仓
     * @param assimilate 对象已附加时，指示是否使用传入的对象覆盖其属性和引用元素
     * @return 是否已附加到对象上下文
     */
    private boolean attached(Object obj, boolean isNew, ObjectReferencePack<ObjectHouse> house, boolean assimilate) {
        house.realValue = null;
        boolean result = false;
        if (isNew) {
            //检查新对象是否存在
            if (this.newObjects == null || obj == null)
                return false;
            if (this.newObjects.containsKey(obj)) {
                //返回存在的对象仓
                result = true;
                house.realValue = this.newObjects.get(obj);
            }
        } else {
            //检查老对象是否存在
            if (this.oldObjects == null || obj == null)
                return false;
            //获取老对象的标识（标识:通过模型的主键标识）
            ObjectKey key = ObjectSystemVisitor.getObjectKey(obj, this.model.getStructuralType(obj.getClass()));
            if (this.oldObjects.containsKey(key)) {
                //返回存在的对象仓
                result = true;
                //是否施加覆盖
                if (assimilate) {
                    Object oldObj = this.oldObjects.get(key).getObject();
                    this.assimilate(obj, this.model.getObjectType(obj.getClass()), oldObj);
                }
                house.realValue = this.oldObjects.get(key);
            }
        }

        return result;
    }

    /**
     * 覆盖引用和属性
     *
     * @param obj        要检查的对象
     * @param objectType 对象类型
     * @param oldObj     存于对象仓中的旧对象
     * @param <T>        对象类型
     */
    private <T> void assimilate(T obj, ObjectType objectType, Object oldObj) {
        //覆盖引用元素
        for (ReferenceElement referenceElement : objectType.getReferenceElements()) {
            if (referenceElement instanceof AssociationReference) {
                AssociationReference associationReference = (AssociationReference) referenceElement;
                //旧对象中引用值为空
                Object oldRefValue = associationReference.getValue(oldObj);

                if (oldRefValue == null) {
                    //检测新对象中对应的值
                    Object objRefValue = associationReference.getValue(obj);
                    if (objRefValue != null) {
                        //新对象不是空则赋给旧对象
                        associationReference.setValue(oldObj, objRefValue);
                    }
                }
            } else if (referenceElement instanceof AssociationEnd) {
                AssociationEnd associationEnd = (AssociationEnd) referenceElement;
                //旧对象中端值为空
                Object oldEndValue = associationEnd.getValue(oldObj);
                if (oldEndValue == null) {
                    //检测新对象中对应的值
                    Object objEndValue = associationEnd.getValue(obj);
                    if (objEndValue != null) {
                        //新对象不是空则赋给旧对象
                        associationEnd.setValue(oldObj, objEndValue);
                    }
                }
            }
        }
        //覆盖属性
        for (Attribute attribute : objectType.getAttributes()) {
            if (attribute.getIsForeignKeyDefineMissing())
                continue;
            Object oldAttrValue = attribute.getValue(oldObj);
            //检测新对象中对应的值
            Object objAttrValue = attribute.getValue(obj);
            if (oldAttrValue == null) {
                if (objAttrValue != null) {
                    //新对象不是空则赋给旧对象
                    attribute.setValue(oldObj, objAttrValue);
                }
            } else {
                if (objAttrValue != null) {
                    if (!objAttrValue.toString().equals(oldAttrValue.toString()))
                        attribute.setValue(oldObj, objAttrValue);
                }
            }
        }
    }

    /**
     * 将对象上下文中发生更改的对象保存到数据源
     */
    public void saveChanges() {
        if (this.objectHouses == null)
            this.objectHouses = new ArrayList<>();
        //对象变更探测（如：对象修改过的对象状态设为修改、对象删除的对象状态设为删除等等）
        this.detectObjectChange();

        ObjectReferencePack<List<Object>> added, changed, deleted, addedCompanions, deletedCompanions;
        added = new ObjectReferencePack<>();
        changed = new ObjectReferencePack<>();
        deleted = new ObjectReferencePack<>();
        addedCompanions = new ObjectReferencePack<>();
        deletedCompanions = new ObjectReferencePack<>();

        //对象分类（如：分析出那些对象是新增，那些对象是修改，那些对象是删除等等）
        this.objectClassify(added, changed, deleted, addedCompanions, deletedCompanions);

        //调用保存提供程序保存
        this.configProvider.getSavingProvider().save(added.realValue, changed.realValue, deleted.realValue, addedCompanions.realValue, deletedCompanions.realValue,
                this::judgeAttributeChange, this::getAttributeOriginalValue);

        if (this.objectHouses == null)
            this.objectHouses = new ArrayList<>();
        //移除标记
        List<Integer> removedList = new ArrayList<>();

        //处理每一个对象
        for (int i = 0; i < this.objectHouses.size(); i++)
            //如果被移除了 就添加至要移除的名单
            if (this.objectHouses.get(i).getIsRemoved()) {
                this.oldObjects.remove(this.objectHouses.get(i).getObjectKey());
                removedList.add(i);
            } else {
                if (this.objectHouses.get(i).getIsNew()) {
                    //新对象 转移至旧对象中
                    ObjectHouse obj = this.objectHouses.get(i);
                    this.newObjects.remove(this.objectHouses.get(i).getObject());
                    ObjectKey key = ObjectSystemVisitor.getObjectKey(obj.getObject(), obj.getObjectType());
                    if (this.oldObjects == null)
                        this.oldObjects = new HashMap<>();
                    //如果没有添加成功 那就是已存在
                    //如已存在的是关联型 是重复的关联型对象
                    //是由于新的隐式关联型的equal函数未通过Emit重写导致的
                    if (this.oldObjects.containsKey(key)) {
                        removedList.add(i);
                    } else {
                        this.oldObjects.put(key, obj);
                    }
                }
                //接收变更
                this.objectHouses.get(i).acceptChanges();
            }

        //具体移除
        List<ObjectHouse> removedHouses = new ArrayList<>();
        for (int removed : removedList) {
            removedHouses.add(this.objectHouses.get(removed));
        }

        for (ObjectHouse removeHouse : removedHouses)
            this.objectHouses.remove(removeHouse);
    }

    /**
     * 探测对象变更
     * 遍历所有实体对象：对新增关联，附加；对旧关联，替换对象，并标记为“被保留”
     * 遍历所有关联对象，将未被标记为“被保留”的对象执行Remove操作
     */
    private void detectObjectChange() {
        //获取上下文中的根对象（因为所有老对象都可以通过根对象找出来：比如查询分类（根对象）会查出关联的文章对象，通过分类能找到分类和文章的关联进而找到文章）
        List<ObjectHouse> houses = this.objectHouses.stream().filter(ObjectHouse::getAsRoot).collect(Collectors.toList());
        for (ObjectHouse h : houses) {
            //所有根对象都默认保留
            h.setIsRetained(true);
            if (h.getObjectType() instanceof EntityType) {
                EntityType type = (EntityType) h.getObjectType();
                this.detectAssociation(h.getObject(), type);
            } else {
                AssociationType type = (AssociationType) h.getObjectType();
                this.detectAssociationEnd(h.getObject(), type, "");
            }
        }

        for (ObjectHouse h : this.objectHouses) {
            //属性变更探测
            h.detectAttributesChange();
            if (h.getObjectType() instanceof AssociationType && !h.getIsNew() && !h.getIsRetained()) {
                AssociationType type = (AssociationType) h.getObjectType();
                for (AssociationEnd end : type.getAssociationEnds()) {
                    //取出关联端的对象仓
                    Object endObj = ObjectSystemVisitor.getValue(h.getObject(), end);
                    if (endObj != null) {
                        ObjectKey objKey = ObjectSystemVisitor.getObjectKey(endObj, end.getEntityType());
                        if (this.oldObjects != null) {
                            boolean flag = false;
                            //直接检测 如果包含 标记可以检测
                            if (this.oldObjects.containsKey(objKey)) {
                                flag = true;
                            } else {
                                //不包含 可能是继承 按照实际类型查询
                                objKey = ObjectSystemVisitor.getObjectKey(endObj, this.model.getEntityType(endObj.getClass()));
                                //如果包含 标记可以检测
                                if (this.oldObjects.containsKey(objKey)) {
                                    flag = true;
                                }
                            }
                            //可以检测 进行检测
                            if (flag) {
                                ObjectHouse endObjHouse = this.oldObjects.get(objKey);

                                if (endObjHouse.getIsRetained())
                                    this.remove(h.getObject());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 探测指定实体对象的关联对象
     *
     * @param entityObj  要探测其关联对象的实体对象
     * @param entityType 实体对象的类型
     */
    private void detectAssociation(Object entityObj, EntityType entityType) {
        //遍历实体模型的关联（探测这些关联）
        for (AssociationReference re : entityType.getAssociationReferences()) {
            //从对象取出关联对象（实际的对象，不是模型对象）
            Iterable<Object> assocObjs = ObjectSystemVisitor.associationNavigate(entityObj, re);
            if (assocObjs != null) {
                //遍历关联对象集合（关联对象可能是集合（如：类目对象包含文章集合），这里做统一处理（不是集合关联也当做集合处理））
                for (Object assocObj : assocObjs) {

                    ObjectReferencePack<ObjectHouse> house = new ObjectReferencePack<>();
                    //检查对象是否存在上下文（检查的老对象）
                    boolean attached = this.attached(assocObj, house, true);
                    if (attached) {
                        if (!house.realValue.getIsNew() && !house.realValue.getIsRetained()) {
                            //用这个对象替换老对象（供后面和快照对比探测是否被修改过）
                            house.realValue.replaceObject(assocObj);
                            //标记为保留（这个对象不会被删除）
                            house.realValue.setIsRetained(true);
                            //是否是根对象
                            boolean asRoot = house.realValue.getAsRoot();
                            if (!asRoot)
                                this.detectAssociationEnd(assocObj, re.getAssociationType(), re.getLeftEnd());
                        }
                    } else {
                        //当做新对象附加到上下文（内部相同的对象只附加一次）
                        ObjectReferencePack<Object> obj = new ObjectReferencePack<>();
                        obj.realValue = assocObj;
                        this.attach(obj, true, false);
                        this.detectAssociationEnd(assocObj, re.getAssociationType(), re.getLeftEnd());
                    }
                }
            }
        }
    }

    /**
     * 探测指定关联对象的关联端对象
     *
     * @param assocObj        要探测其端对象的关联对象
     * @param associationType 关联对象的类型
     * @param excludedEnd     指定要排除的关联端
     */
    private void detectAssociationEnd(Object assocObj, AssociationType associationType, String excludedEnd) {

        //遍历关联端
        for (AssociationEnd end : associationType.getAssociationEnds()) {
            //排除指定端
            if (!Objects.equals(end.getName(), excludedEnd)) {
                //从关联对象中获取端对象
                Object endObj = ObjectSystemVisitor.getValue(assocObj, end);
                if (endObj == null) continue;
                ObjectReferencePack<ObjectHouse> house = new ObjectReferencePack<>();
                //检查端是否在上下文存在
                boolean attached = this.attached(endObj, house);

                if (attached) {
                    if (!house.realValue.getIsNew() && !house.realValue.getIsRetained()) {
                        //替换端对象（供后面进行属性变更探测,以确定是否修改）
                        house.realValue.replaceObject(endObj);
                        //标记为保留（不会被删除）
                        house.realValue.setIsRetained(true);
                        //是否为根对象
                        boolean asRoot = house.realValue.getAsRoot();
                        if (!asRoot) this.detectAssociation(endObj, end.getEntityType());
                    }
                } else {
                    //附加新对象
                    ObjectReferencePack<Object> obj = new ObjectReferencePack<>();
                    obj.realValue = endObj;
                    this.attach(obj, end.getDefaultAsNew(), false);
                    //视为新对象（默认为false,需用户配置）
                    if (end.getDefaultAsNew()) {
                        //对关联端进行关联引用探测（每个关联对象都是一个实体，如： ）
                        this.detectAssociation(endObj, end.getEntityType());
                    }
                }
            }
        }
    }

    /**
     * 判定指定的属性是否已更改
     *
     * @param obj      要检查的属性所属的对象
     * @param attrName 要检查的属性
     * @return 是否已更改
     */
    private boolean judgeAttributeChange(Object obj, String attrName) {
        //获取对象的标识（标识:通过模型的主键标识）
        ObjectKey key = ObjectSystemVisitor.getObjectKey(obj, this.model.getStructuralType(obj.getClass()));
        if (this.oldObjects.containsKey(key)) //上下文存在对象
        {
            //取出旧对象的对象仓
            ObjectHouse house = this.oldObjects.get(key);
            //检查制度属性是否更改
            return house.judgeAttributeChange(attrName);
        }

        return false;
    }

    /**
     * 获取指定对象指定属性的原值
     *
     * @param obj       要获取其属性原值的对象
     * @param attribute 属性
     * @param parent    父属性
     * @return 原始值
     */
    private Object getAttributeOriginalValue(Object obj, Attribute attribute, AttributePath parent) {
        //获取此对象的旧对象字典
        ObjectType objectType = this.model.getObjectType(obj.getClass());
        ObjectKey objectKey = ObjectSystemVisitor.getObjectKey(obj, objectType);
        ObjectHouse house = this.oldObjects.get(objectKey);

        return house.getAttributeOriginalValue(attribute, parent);
    }

    /**
     * 按对象状态对对象上下文中的对象进行分类，挑选出新增的、修改过的、已更改的对象
     *
     * @param added             返回新增的对象。该对象既可能为新创建的对象也可能为数据源中已存在的对象
     * @param changed           返回已修改的对象
     * @param deleted           返回已删除的对象
     * @param addedCompanions   增加的伴随关联对象
     * @param deletedCompanions 删除的伴随关联对象
     */
    private void objectClassify(ObjectReferencePack<List<Object>> added, ObjectReferencePack<List<Object>> changed, ObjectReferencePack<List<Object>> deleted,
                                ObjectReferencePack<List<Object>> addedCompanions, ObjectReferencePack<List<Object>> deletedCompanions) {
        //新增对象集合
        added.realValue = new ArrayList<>();
        //修改对象集合
        changed.realValue = new ArrayList<>();
        //删除对象集合
        deleted.realValue = new ArrayList<>();
        //新增关联集合
        addedCompanions.realValue = new ArrayList<>();
        //删除关联集合
        deletedCompanions.realValue = new ArrayList<>();

        //遍历上下文中对象仓集合，确定对象所属于哪个集合
        for (ObjectHouse house : this.objectHouses) {
            //分析隐式关联对象
            if (house.getObjectType() instanceof AssociationType) {
                AssociationType assType = (AssociationType) house.getObjectType();
                if (assType.getCompanionEnd() != null) {
                    //获取关联对象的一端（这里是伴随端）
                    Object endObj = ObjectSystemVisitor.getValue(house.getObject(), assType.getCompanionEnd());
                    //判断伴随端是否存在上下文中
                    ObjectReferencePack<ObjectHouse> endHouse = new ObjectReferencePack<>();
                    if (!(endObj == null || !this.attached(endObj, endHouse))) {
                        switch (endHouse.realValue.getStatus()) {
                            case Added: //新增关联端（比如分类下添加文章：在文章分类这个关系中文章是新加的分类是原本就有的）
                                added.realValue.add(house.getObject());
                                break;
                            case Deleted: //删除关联端（如：删除了文章，这个文章和分类的关联也要删除）
                                deleted.realValue.add(house.getObject());
                                break;
                            default:
                                switch (house.getStatus()) {
                                    case Added: //新增关联（如在一个已有的文章放到已有的分类）
                                        addedCompanions.realValue.add(house.getObject());
                                        break;
                                    case Modified: //修改关联对象
                                        changed.realValue.add(house.getObject());
                                        break;
                                    case Deleted: //删除关联（如上：移除一个关系，文章移走）
                                        deletedCompanions.realValue.add(house.getObject());
                                        break;
                                }
                                break;
                        }

                        continue;
                    }
                }
            }

            //根据对象仓的状态确认对象要执行的操作（删除或新增等等）
            switch (house.getStatus()) {
                case Added:
                    added.realValue.add(house.getObject());
                    break;
                case Deleted:
                    deleted.realValue.add(house.getObject());
                    break;
                case Modified: //对象的属性被修改
                    changed.realValue.add(house.getObject());
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 开始本地事务
     */
    public void beginTransaction() {
        this.prepareStorageProviders();

        for (IStorageProvider provider : this.configProvider.getStorageProviders().values()) {
            provider.beginTransaction(EIsolationLevel.TRANSACTION_READ_COMMITTED);
        }

        this.transactionBegun = true;
    }


    /**
     * 提交本地事务
     */
    public void commit() {

        this.prepareStorageProviders();

        for (IStorageProvider provider : this.configProvider.getStorageProviders().values()) {
            provider.commitTransaction();
        }
        this.transactionBegun = false;
    }


    /**
     * 回滚本地事务
     */
    public void rollbackTransaction() {

        this.prepareStorageProviders();

        for (IStorageProvider provider : this.configProvider.getStorageProviders().values()) {
            provider.rollbackTransaction();
        }
        this.transactionBegun = false;
    }

    /**
     * 显式的声明释放资源
     */
    public void release() {
        for (IStorageProvider provider : this.configProvider.getStorageProviders().values()) {
            provider.releaseResource();
        }
    }

    /**
     * 准备存储提供器
     * 如果当前没有存储提供器 则创建对应的提供器
     */
    private void prepareStorageProviders() {
        if (this.configProvider.getStorageProviders().isEmpty()) {
            if (this.configProvider.getQueryProvider() instanceof HeterogQueryProvider) {
                HeterogQueryProvider heterogQueryProvider = (HeterogQueryProvider) this.configProvider.getQueryProvider();
                heterogQueryProvider.getStorageProviderCreator().invoke(this.model.getStorageSymbol() == null ? StorageSymbols.getCurrent().getDefault() : this.model.getStorageSymbol());
            }
        }
    }
}
