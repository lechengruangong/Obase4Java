/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象类型,包括实体型和关联型.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:35:38
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.common.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 为对象类型提供基础实现，是实体型和关联型的基类。
 */
public abstract class ObjectType extends ReferringType implements IMappable {

    /**
     * 并发冲突处理策略
     */
    protected EConcurrentConflictHandlingStrategy concurrentConflictHandlingStrategy;

    /**
     * 指定要包含在对象变更通知中的属性
     */
    protected List<String> noticeAttributes;

    /**
     * 指示对象创建时是否发送通知
     */
    protected boolean notifyCreation = false;

    /**
     * 指示对象删除时是否发送通知
     */
    protected boolean notifyDeletion = false;

    /**
     * 指示对象更新时是否发送通知
     */
    protected boolean notifyUpdate = false;

    /**
     * 对象在关系数据库中的存储顺序（排序规则）
     */
    protected List<OrderRule> storingOrder;

    /**
     * 映射目标
     */
    protected String targetTable;

    /**
     * 用于识别对象版本的属性集，简称版本键。
     */
    protected List<String> versionAttributes;

    /**
     * 根据Clr类型创建Obj类型实例
     *
     * @param clrType      对象运行时类型
     * @param derivingFrom 基类
     */
    protected ObjectType(Class<?> clrType, StructuralType derivingFrom) {
        super(clrType, derivingFrom);
    }

    /**
     * 根据Clr类型创建Obj类型实例
     *
     * @param clrType 对象运行时类型
     */
    protected ObjectType(Class<?> clrType) {
        super(clrType);
    }

    /**
     * 获取要包含在对象变更通知中的属性
     *
     * @return 要包含在对象变更通知中的属性
     */
    public List<String> getNoticeAttributes() {
        return this.noticeAttributes;
    }

    /**
     * 设置要包含在对象变更通知中的属性
     *
     * @param noticeAttributes 要包含在对象变更通知中的属性
     */
    public void setNoticeAttributes(List<String> noticeAttributes) {
        if (noticeAttributes != null)
            noticeAttributes = noticeAttributes.stream().distinct().collect(Collectors.toList());
        this.noticeAttributes = noticeAttributes;
    }

    /**
     * 获取一个值，该值指示对象创建时是否发送通知
     *
     * @return 指示对象创建时是否发送通知
     */
    public boolean getNotifyCreation() {
        return this.notifyCreation;
    }

    /**
     * 设置一个值，该值指示对象创建时是否发送通知
     *
     * @param notifyCreation 指示对象创建时是否发送通知
     */
    public void setNotifyCreation(boolean notifyCreation) {
        this.notifyCreation = notifyCreation;
    }

    /**
     * 获取一个值，该值指示对象删除时是否发送通知
     *
     * @return 指示对象删除时是否发送通知
     */
    public boolean getNotifyDeletion() {
        return this.notifyDeletion;
    }

    /**
     * 设置一个值，该值指示对象删除时是否发送通知
     *
     * @param notifyDeletion 指示对象删除时是否发送通知
     */
    public void setNotifyDeletion(boolean notifyDeletion) {
        this.notifyDeletion = notifyDeletion;
    }

    /**
     * 获取一个值，该值指示对象更新时是否发送通知
     *
     * @return 指示对象更新时是否发送通知
     */
    public boolean getNotifyUpdate() {
        return this.notifyUpdate;
    }

    /**
     * 设置一个值，该值指示对象更新时是否发送通知
     *
     * @param notifyUpdate 指示对象更新时是否发送通知
     */
    public void setNotifyUpdate(boolean notifyUpdate) {
        this.notifyUpdate = notifyUpdate;
    }

    /**
     * 获取映射目标
     *
     * @return 映射目标
     */
    public String getTargetTable() {
        return this.targetTable;
    }

    /**
     * 设置映射目标
     *
     * @param targetTable 映射目标
     */
    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    /**
     * 获取对象在关系数据库中的存储顺序（排序规则）
     *
     * @return 对象在关系数据库中的存储顺序（排序规则）
     */
    public List<OrderRule> getStoringOrder() {
        if (this.storingOrder == null || this.storingOrder.size() == 0)
            return this.getDefaultStoringOrder();
        return this.storingOrder;
    }

    /**
     * 设置对象在关系数据库中的存储顺序（排序规则）
     *
     * @param storingOrder 对象在关系数据库中的存储顺序（排序规则）
     */
    public void setStoringOrder(List<OrderRule> storingOrder) {
        this.storingOrder = storingOrder;
    }

    /**
     * 获取默认的存储排序规则
     * 注：该属性由派生类实现。派生类通过实现此属性来提供特定于自身的默认存储排序规则
     *
     * @return 默认的存储排序规则
     */
    protected abstract List<OrderRule> getDefaultStoringOrder();

    /**
     * 获取用于识别对象版本的属性集，简称版本键
     *
     * @return 用于识别对象版本的属性集，简称版本键
     */
    public List<String> getVersionAttributes() {
        return this.versionAttributes;
    }

    /**
     * 设置用于识别对象版本的属性集，简称版本键
     *
     * @param versionAttributes 用于识别对象版本的属性集，简称版本键
     */
    public void setVersionAttributes(List<String> versionAttributes) {
        this.versionAttributes = versionAttributes;
    }

    /**
     * 获取并发冲突处理策略
     *
     * @return 并发冲突处理策略
     */
    public EConcurrentConflictHandlingStrategy getConcurrentConflictHandlingStrategy() {
        return this.concurrentConflictHandlingStrategy;
    }

    /**
     * 设置并发冲突处理策略
     *
     * @param concurrentConflictHandlingStrategy 并发冲突处理策略
     */
    public void setConcurrentConflictHandlingStrategy(EConcurrentConflictHandlingStrategy concurrentConflictHandlingStrategy) {
        this.concurrentConflictHandlingStrategy = concurrentConflictHandlingStrategy;
    }

    /**
     * 获取当前对象类型的映射表的键字段集合
     *
     * @return 当前对象类型的映射表的键字段集合
     */
    public abstract List<String> getKeyFields();

    /**
     * 设置当前对象类型的映射表的键字段集合
     *
     * @param keyFields 标识成员的映射目标序列
     */
    public abstract void setKeyFields(List<String> keyFields);

    /**
     * 获取对象标识成员的名称的序列
     * 对于实体型，其对象的标识成员为各标识属性；对于关联型，标识成员为各关联端对应的实体型的标识属性
     * 关联对象标识成员的名称按以下规则生成：关联端名称 + ‘.’ + 实体型标识属性名称
     *
     * @return 对象标识成员的名称的序列
     */
    public abstract String[] getKeyMemberNames();

    /**
     * 获取映射目标名称
     *
     * @return 映射目标名称
     */
    @Override
    public String getTargetName() {
        //如果当前类型未设置映射目标，沿“主维”派生关系上溯，直到找到一个设置了映射目标的类型，返回该映射目标。
        if (Utils.getStringIsEmpty(this.targetTable))
            return Utils.getDerivedTargetTable(this);
        return this.targetTable;
    }

    /**
     * 设置映射目标名称
     *
     * @param targetName 映射目标名称
     */
    @Override
    public void setTargetName(String targetName) {
        this.targetTable = targetName;
    }

    /**
     * 获取对象标识
     *
     * @param targetObj 要获取其标识的对象
     * @return 对象标识
     */
    public abstract ObjectKey getObjectKey(Object targetObj);

    /**
     * 对象类型的通用的完整性检查
     *
     * @param errDictionary 错误信息字典
     */
    protected void commonIntegrityCheck(Map<String, List<String>> errDictionary) {
        //错误消息
        List<String> message = new ArrayList<>();
        //检查构造函数
        if (this.getConstructor() == null)
            message.add(this.clrType.getName() + "未配置有效的构造函数.");
        //检查映射表
        if (Utils.getStringIsEmpty(this.targetTable))
            message.add(this.clrType.getName() + "未配置映射表.");
        //检查继承的配置
        if (this.getDerivingFrom() != null && this.getConcreteTypeSign() == null)
            message.add(this.clrType.getName() + "配置为继承" + this.getDerivingFrom().getClrType().getName() + ",却没有配置具体类型判别标志.");
        if (this.getDerivedTypes().size() > 0 && this.getConcreteTypeSign() == null)
            message.add(this.clrType.getName() + "配置为基础类型,却没有配置具体类型判别标志.");
        //检查继承的映射表是否一致
        if (this.getDerivingFrom() instanceof ObjectType) {
            ObjectType derivingObjectType = (ObjectType) this.getDerivingFrom();
            if (!derivingObjectType.getTargetTable().equalsIgnoreCase(this.getTargetTable()))
                message.add(this.clrType.getName() + "配置为继承" + this.getDerivingFrom().getClrType().getName() + ",但映射表与父类不一致,父类映射表为" + derivingObjectType.getTargetTable() + ",当前为" + this.getTargetTable() + ".");
        }

        //检查父类的构造器
        if (this.getDerivingFrom() != null) {
            //比较当前构造器的参数个数和父类构造器的参数个数
            int currentCount = Utils.getConstructorParameterCount(this.constructor);
            int derivingCount = Utils.getConstructorParameterCount(this.getDerivingFrom().getConstructor());
            //不一致 抛出异常
            if (currentCount != derivingCount)
                message.add(this.clrType.getName() + "的构造器参数个数与父类参数个数不一致," + this.clrType.getName() + "为" + currentCount + "个,但父类" + this.getDerivingFrom().getName() + "的构造器参数为" + derivingCount + "个.");
            //如果个数大于0 再检查每一个的类型
            if (currentCount > 0) {
                for (int i = 0; i < currentCount; i++) {
                    Class<?> currentType = this.constructor.getParameters().get(i).getType();
                    Class<?> derivingType = this.getDerivingFrom().getConstructor().getParameters().get(i).getType();
                    //检查类型是否相等
                    if (!currentType.equals(derivingType))
                        message.add(this.clrType.getName() + "的构造器参数第" + (i + 1) + "个参数类型与父类参数类型不一致," + this.clrType.getName() + "为" + currentType.getName() + ",但父类" + this.getDerivingFrom().getName() + "的构造器参数类型为" + derivingType + ".");
                }
            }
        }

        //检查一般属性
        for (Attribute attribute : this.getAttributes()) {
            //检查属性
            if (attribute.getValueSetter() == null)
                if (this.getConstructor() != null && this.getConstructor().getParameterByElement(attribute.getName()) == null)
                    //如果最顶层的继承也没有为此属性的构造函数参数
                    if (Utils.getDerivedIInstanceConstructor(this) != null && Utils.getDerivedIInstanceConstructor(this).getParameterByElement(attribute.getName()) == null)
                        message.add("实体" + this.getName() + "的属性" + attribute.getName() + "没有设值器,且没有在构造函数中使用.");

            if (attribute.getValueGetter() == null)
                message.add("实体" + this.getName() + "的属性" + attribute.getName() + "没有取值器.");
        }

        //检查引用元素的延迟加载配置
        //在Java中 不需要virtual关键字即可重写父类方法 此项无需检查

        //如果有检查失败消息
        if (message.size() > 0) {
            //就与现有的问题合并
            String name = this.clrType != null ? this.clrType.getSimpleName() : this.name;
            if (errDictionary.containsKey(name))
                errDictionary.get(name).addAll(message);
            else
                errDictionary.put(name, message);
        }
    }
}
