/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：实体型.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-2 12:22:30
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

/**
 * 表示实体型
 */
public class EntityType extends ObjectType {

    /**
     * 邮戳锁
     */
    private final StampedLock stampedLock = new StampedLock();

    /**
     * 默认的存储排序规则
     */
    private List<OrderRule> defaultStoringOrder;

    /**
     * 标识属性组
     */
    private List<String> keyAttributes = new ArrayList<>();

    /**
     * 标识字段组
     */
    private List<String> keyFields = null;

    /**
     * 标识是否自增
     */
    private boolean keyIsSelfIncreased;

    /**
     * 根据Clr类型创建Obj类型实例
     *
     * @param clrType      对象运行时类型
     * @param derivingFrom 基类
     */
    public EntityType(Class<?> clrType, StructuralType derivingFrom) {
        super(clrType, derivingFrom);
        this.typeName.IsAssociation = false;
        this.typeName.IsEntity = true;
    }

    /**
     * 根据Clr类型创建Obj类型实例
     *
     * @param clrType 对象运行时类型
     */
    public EntityType(Class<?> clrType) {
        super(clrType);
        this.typeName.IsAssociation = false;
        this.typeName.IsEntity = true;
    }

    /**
     * 获取实体型包含的关联引用的集合
     *
     * @return 实体型包含的关联引用的集合
     */
    public List<AssociationReference> getAssociationReferences() {
        List<AssociationReference> associationEnds = new ArrayList<>();

        for (TypeElement ele : this.getElements()) {
            if (ele instanceof AssociationReference) {
                associationEnds.add((AssociationReference) ele);
            }
        }

        return associationEnds;
    }

    /**
     * 获取一个值，该值指示标识是否自增
     *
     * @return 一个值，该值指示标识是否自增
     */
    public boolean getKeyIsSelfIncreased() {
        return this.keyIsSelfIncreased;
    }

    /**
     * 设置一个值，该值指示标识是否自增
     *
     * @param keyIsSelfIncreased 一个值，该值指示标识是否自增
     */
    public void setKeyIsSelfIncreased(boolean keyIsSelfIncreased) {
        long stamp = this.stampedLock.writeLock();
        this.keyIsSelfIncreased = keyIsSelfIncreased;
        this.getKeyAttributes().forEach(s -> {
            Attribute attr = this.getAttribute(s);
            if (attr != null)
                attr.setDbGenerateValue(keyIsSelfIncreased);
        });
        this.stampedLock.unlockWrite(stamp);
    }

    /**
     * 获取标识属性组
     *
     * @return 标识属性组
     */
    public List<String> getKeyAttributes() {
        if (this.keyAttributes == null)
            this.keyAttributes = new ArrayList<>();
        return this.keyAttributes;
    }

    /**
     * 设置标识属性组
     *
     * @param keyAttributes 标识属性组
     */
    public void setKeyAttributes(List<String> keyAttributes) {
        long stamp = this.stampedLock.writeLock();
        this.keyAttributes = keyAttributes;
        this.getKeyAttributes().forEach(s -> {
            Attribute attr = this.getAttribute(s);
            if (attr != null)
                attr.setDbGenerateValue(this.keyIsSelfIncreased);
        });
        this.stampedLock.unlockWrite(stamp);
    }

    /**
     * 获取当前对象类型的映射表的键字段集合
     *
     * @return 获取当前对象类型的映射表的键字段集合
     */
    @Override
    public List<String> getKeyFields() {

        long stamp = this.stampedLock.readLock();
        try {
            while (this.keyFields == null) {
                long ws = this.stampedLock.tryConvertToWriteLock(stamp);
                if (ws != 0L) {
                    stamp = ws;
                    this.keyFields = new ArrayList<>();
                    for (String key : this.getKeyAttributes()) {
                        this.keyFields.add(this.getAttribute(key).getTargetField());
                    }
                    break;
                } else {
                    this.stampedLock.unlockRead(stamp);
                    stamp = this.stampedLock.writeLock();
                }
            }
            return this.keyFields;
        } finally {
            this.stampedLock.unlock(stamp);
        }
    }

    /**
     * 设置当前对象类型的映射表的键字段集合
     *
     * @param keyFields 标识成员的映射目标序列
     */
    @Override
    public void setKeyFields(List<String> keyFields) {
        this.keyFields = keyFields;
    }

    /**
     * 获取默认的存储排序规则
     * 注：该属性由派生类实现。派生类通过实现此属性来提供特定于自身的默认存储排序规则
     *
     * @return 默认的存储排序规则
     */
    @Override
    protected List<OrderRule> getDefaultStoringOrder() {
        long stamp = this.stampedLock.readLock();
        try {
            while (this.defaultStoringOrder == null || this.defaultStoringOrder.size() == 0) {
                long ws = this.stampedLock.tryConvertToWriteLock(stamp);
                if (ws != 0L) {
                    stamp = ws;
                    this.defaultStoringOrder = new ArrayList<>();
                    this.getKeyAttributes().forEach(s -> {
                        OrderRule orderRule = new OrderRule();
                        orderRule.setOrderBy(this.getAttribute(s));
                        this.defaultStoringOrder.add(orderRule);
                    });
                    break;
                } else {
                    this.stampedLock.unlockRead(stamp);
                    stamp = this.stampedLock.writeLock();
                }
            }
            return this.defaultStoringOrder;
        } finally {
            this.stampedLock.unlock(stamp);
        }
    }


    /**
     * 获取对象标识成员的名称的序列
     * 对于实体型，其对象的标识成员为各标识属性；对于关联型，标识成员为各关联端对应的实体型的标识属性
     * 关联对象标识成员的名称按以下规则生成：关联端名称 + ‘.’ + 实体型标识属性名称
     *
     * @return 对象标识成员的名称的序列
     */
    @Override
    public String[] getKeyMemberNames() {
        return this.getKeyAttributes().toArray(new String[0]);
    }

    /**
     * 根据名称查找关联引用
     *
     * @param name 关联引用名称
     * @return 关联引用
     */
    public AssociationReference getAssociationReference(String name) {
        return (AssociationReference) this.getElement(name);
    }

    /**
     * 完整性检查
     * 继承类需要检查则重写此方法
     *
     * @param errDictionary 错误信息字典
     */
    @Override
    public void integrityCheck(Map<String, List<String>> errDictionary) {
        //错误消息
        List<String> message = new ArrayList<>();
        //没设置主键
        if (this.keyAttributes == null || this.keyAttributes.size() == 0) {
            //有继承 将继承的复制过来
            if (this.getDerivingFrom() instanceof EntityType) {
                EntityType derivingFrom = (EntityType) this.getDerivingFrom();
                this.keyAttributes = derivingFrom.getKeyAttributes();
            }
        }
        //再次检查 没有就抛异常
        if (this.keyAttributes == null || this.keyAttributes.size() == 0)
            message.add("实体" + this.getName() + "的键属性未设置");

        //检查键
        List<Attribute> keyAttrs = this.getAttributes().stream().filter(p -> this.getKeyAttributes().contains(p.getName())).collect(Collectors.toList());

        //自增 但是是联合主键
        if (this.keyIsSelfIncreased && keyAttrs.size() > 1)
            message.add("实体" + this.getName() + "的键属性是联合主键,不能是自增的");

        //检查主键
        for (Attribute keyAttr : keyAttrs) {
            //检查键属性
            if (keyAttr.getValueSetter() == null)
                if (this.getConstructor().getParameterByElement(keyAttr.getName()) == null)
                    message.add("实体" + this.getName() + "的键属性" + keyAttr.getName() + "没有设值器,且没有在构造函数中使用.");

            if (this.keyIsSelfIncreased && keyAttr.getDataType() != int.class && keyAttr.getDataType() != long.class &&
                    keyAttr.getDataType() != short.class && keyAttr.getDataType() != Integer.class && keyAttr.getDataType() != Long.class &&
                    keyAttr.getDataType() != Short.class)
                message.add("实体" + this.getName() + "的键属性" + keyAttr.getName() + "是自增的但不是short,int,long类型.");

            if (keyAttr.getValueGetter() == null)
                message.add("实体" + this.getName() + "的键属性" + keyAttr.getName() + "没有取值器.");
        }

        //检查关联引用
        for (AssociationReference reference : this.getAssociationReferences()) {
            //检查左端
            if (Utils.getStringIsEmpty(reference.getLeftEnd()))
                message.add(this.getClrType().getName() + "的关联引用" + reference.getName() + "的端未能自动配置,请手动配置此关联引用.");

            if (reference.getAssociationType().getAssociationEnds().stream().noneMatch(p -> p.getName().equalsIgnoreCase(reference.getLeftEnd())))
                message.add(this.getClrType().getName() + "的关联引用" + reference.getName() + "的左端" + reference.getLeftEnd() + "无法与关联端的名字相匹配,请检查关联端的名称和左端名称是否一致.");

            //检查右端
            if (reference.getAssociationType().getAssociationEnds().stream().noneMatch(p -> p.getName().equalsIgnoreCase(reference.getRightEnd())) && !reference.getAssociationType().getVisible())
                message.add(this.getClrType().getName() + "的关联引用" + reference.getName() + "的右端" + reference.getRightEnd() + "无法与关联端的名字相匹配,请检查关联端的名称和右端名称是否一致.");
            //检查设值器和取值器
            if (reference.getValueGetter() == null)
                message.add(this.getClrType().getName() + "的关联引用" + reference.getName() + "没有取值器.");

            if (reference.getValueSetter() == null)
                message.add(this.getClrType().getName() + "的关联引用" + reference.getName() + "没有设值器.");

            //检查左端是否和右端相同
            if (reference.getLeftEnd().equalsIgnoreCase(reference.getRightEnd()))
                message.add(this.getClrType().getName() + "的关联引用" + reference.getName() + "的左端和右端不能相同.");

            //检查关联引用的多重性
            Property prop = Utils.getProperty(this.clrType, reference.getName());
            //获取引用的多重性
            boolean isMulti = Utils.getIsMultiple(prop, new ObjectReferencePack<>());
            //如果这个关联引用是一对多
            if (isMulti) {
                List<AssociationEnd> ends = reference.getAssociationType().getAssociationEnds();
                //而且也不是自关联 那么此关联引用的关联型映射表就不能与当前实体相同
                if (!ends.stream().allMatch(p -> p.getEntityType().getClrType().equals(ends.get(0).getEntityType().getClrType()))
                        && this.getTargetTable().equalsIgnoreCase(reference.getAssociationType().getTargetTable()))
                    message.add(this.getClrType().getName() + "的关联引用" + reference.getName() + "是一对多的,其关联型" + reference.getAssociationType().getTargetTable() + "关联表不能是自身的映射表" + this.getTargetTable() + ".");
            }
        }

        //检查键属性和默认排序
        if (this.keyFields == null)
            this.keyFields = new ArrayList<>();
        if (this.defaultStoringOrder == null)
            this.defaultStoringOrder = new ArrayList<>();

        for (String key : this.getKeyAttributes()) {
            Attribute attr = this.getAttribute(key);
            if (attr == null)
                message.add(this.getClrType().getName() + "的主键" + key + "没有对应属性.");
            //如果键字段不包含当前的主键 添加键字段
            if (attr != null && !this.keyFields.contains(attr.getTargetField()))
                this.keyFields.add(attr.getTargetField());
            //如果默认排序不包含当前的主键 添加默认排序
            if (attr != null && this.defaultStoringOrder.stream().noneMatch(o -> o.getOrderBy().getTargetField().equalsIgnoreCase(attr.getTargetField()))) {
                OrderRule orderRule = new OrderRule();
                orderRule.setOrderBy(attr);
                this.defaultStoringOrder.add(orderRule);
            }
        }

        //通用的对象类型检查
        this.commonIntegrityCheck(errDictionary);

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

    /**
     * 获取对象标识
     *
     * @param targetObj 要获取其标识的对象
     * @return 对象标识
     */
    @Override
    public ObjectKey getObjectKey(Object targetObj) {
        //对象标识成员由标识属性名与属性值组合
        List<ObjectKeyMember> objectKeyMemberList = new ArrayList<>();

        if (this.getKeyAttributes() != null) {
            for (String key : this.getKeyAttributes()) {
                //获取属性值
                Object value = this.getValue(targetObj, this, key);
                //创建标识成员
                ObjectKeyMember member = new ObjectKeyMember(this.getClrType().getName() + "-" + key, value);
                objectKeyMemberList.add(member);
            }
        }

        //创建对象标识
        return new ObjectKey(this, objectKeyMemberList);
    }

    /**
     * 从对象中获取元素（属性、关联引用、关联端）的值
     *
     * @param obj         目标对象
     * @param type        对象的类型（实体型、关联型、复杂类型）
     * @param elementName 目标元素的名称
     * @return 值
     */
    private Object getValue(Object obj, StructuralType type, String elementName) {
        //获取元素
        TypeElement typeElement = type.getElement(elementName);
        if (typeElement == null) throw new IllegalArgumentException("无法获取到" + elementName + "的类型元素.");
        //获取对象元素的值
        return this.getValue(obj, typeElement);
    }

    /**
     * 从对象中获取元素（属性、关联引用、关联端）的值。
     *
     * @param obj     目标对象
     * @param element 目标元素
     * @return 值
     */
    private Object getValue(Object obj, TypeElement element) {
        Object result;
        if (element instanceof ReferenceElement && obj instanceof IIntervene) {
            IIntervene inter = (IIntervene) obj;
            //禁用延迟加载（防止延迟加载期间内部访问属性又开始加载，造成死循环）
            inter.forbidLazyLoading();
            //获取值
            result = element.getValueGetter().getValue(obj);
            //启用延迟加载
            inter.enableLazyLoading();
        } else {
            //获取值
            result = element.getValueGetter().getValue(obj);
        }

        return result;
    }

    /**
     * 获取实体型的键（即标识属性序列）
     *
     * @return 实体型的键
     */
    public Attribute[] getKey() {
        //从键属性名称投影到属性
        return this.keyAttributes.stream().map(this::getAttribute).toArray(Attribute[]::new);
    }

    /**
     * 获取类型的筛选键。
     * 对于类型的某一个属性或属性序列，如果其值或值序列可以作为该类型实例的标识，该属性或属性序列即可作为该类型的筛选键。
     * 对于实体型，可以用主键作为筛选键。对于关联型，可以用其在各关联端上的外键属性组合成的属性序列作为筛选键。
     *
     * @return 构成筛选键的属性序列
     */
    @Override
    public Attribute[] getFilterKey() {
        //实体型的键属性就是其筛选键
        return this.getKey();
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "EntityType:{{Name-\"" + this.getName() + "\",ClrType-\"" + this.getClrType().getName() + "\"}}";
    }
}
