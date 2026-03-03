/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联端.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-2 15:41:33
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.common.ObjectReferencePack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.StampedLock;

/**
 * 表示关联引用
 */
public class AssociationEnd extends ReferenceElement {

    /**
     * 邮戳锁
     */
    private final StampedLock stampedLock = new StampedLock();

    /**
     * 关联端的实体型
     */
    private final EntityType entityType;
    /**
     * 外键所在的类型的寄存器
     */
    public ObjectType definingTypeOfForeignKey;
    /**
     * 指示是否把关联端对象默认视为新对象。当该属性为true时，如果关联端对象未被显式附加到上下文，该对象将被视为新对象实施持久化。
     */
    private boolean defaultAsNew;
    /**
     * 外键寄存器
     */
    private Attribute[] foreignKey;

    /**
     * 指示当前关联端是否为聚合关联端
     */
    private boolean isAggregated;

    /**
     * 关联端映射
     */
    private List<AssociationEndMapping> mappings = new ArrayList<>();

    /**
     * 寄存 引用元素所承载的对象导航行为
     */
    private ObjectNavigation navigation;

    /**
     * 创建关联端的实例
     *
     * @param name 关联端名称
     */
    private AssociationEnd(String name) {
        super(name, EElementType.AssociationEnd);
        this.entityType = null;
    }

    /**
     * 创建关联端的实例
     *
     * @param name       关联端名称
     * @param entityType 关联端的实体型
     */
    public AssociationEnd(String name, EntityType entityType) {
        super(name, EElementType.AssociationEnd);
        this.entityType = entityType;
    }


    /**
     * 获取一个值，该值指示当前关联端是否为聚合关联端。
     *
     * @return 指示当前关联端是否为聚合关联端
     */
    public boolean getIsAggregated() {
        return this.isAggregated;
    }

    /**
     * 设置一个值，该值指示当前关联端是否为聚合关联端。
     *
     * @param aggregated 指示当前关联端是否为聚合关联端
     */
    public void setIsAggregated(boolean aggregated) {
        this.isAggregated = aggregated;
    }

    /**
     * 获取一个值，该值指示是否把关联端对象默认视为新对象。当该属性为true时，如果关联端对象未被显式附加到上下文，该对象将被视为新对象实施持久化。
     *
     * @return 指示是否把关联端对象默认视为新对象
     */
    public boolean getDefaultAsNew() {
        return this.defaultAsNew;
    }

    /**
     * 设置一个值，该值指示是否把关联端对象默认视为新对象。当该属性为true时，如果关联端对象未被显式附加到上下文，该对象将被视为新对象实施持久化。
     *
     * @param defaultAsNew 指示是否把关联端对象默认视为新对象
     */
    public void setDefaultAsNew(boolean defaultAsNew) {
        this.defaultAsNew = defaultAsNew;
    }

    /**
     * 获取关联端到关联表的映射。（关联端实体型的属性名到关联表的字段名
     *
     * @return 关联表的映射
     */
    public List<AssociationEndMapping> getMappings() {
        return this.mappings;
    }

    /**
     * 设置关联端到关联表的映射。（关联端实体型的属性名到关联表的字段名）
     *
     * @param mappings 关联端到关联表的映射
     */
    public void setMappings(List<AssociationEndMapping> mappings) {
        this.mappings = mappings;
    }

    /**
     * 获取关联端的实体型
     *
     * @return 实体型
     */
    public EntityType getEntityType() {
        return this.entityType;
    }

    /**
     * 获取引用元素的类型。当引用元素为关联引用时返回AssociationType；为关联端时返回EntityType。
     *
     * @return 引用元素的类型
     */
    @Override
    public ObjectType getReferenceType() {
        return this.entityType;
    }

    /**
     * 获取引用元素所承载的对象导航行为
     *
     * @return 引用元素所承载的对象导航行为
     */
    @Override
    public ObjectNavigation getNavigation() {
        long stamp = this.stampedLock.readLock();
        try {
            while (this.navigation == null) {
                long ws = this.stampedLock.tryConvertToWriteLock(stamp);
                if (ws != 0L) {
                    stamp = ws;
                    if (this.getHostType() instanceof AssociationType) {
                        AssociationType associationType = (AssociationType) this.getHostType();
                        this.navigation = new ObjectNavigation(associationType, null, this.getName());
                    }
                    break;
                } else {
                    this.stampedLock.unlockRead(stamp);
                    stamp = this.stampedLock.writeLock();
                }
            }
            return this.navigation;
        } finally {
            this.stampedLock.unlock(stamp);
        }
    }

    /**
     * 获取引用元素在对象导航中承担的功能
     *
     * @return 引用元素在对象导航中承担的功能
     */
    @Override
    public ENavigationUse getNavigationUse() {
        return ENavigationUse.ArrivingReference;
    }

    /**
     * 获取元素值的类型
     *
     * @return 元素值的类型
     */
    @Override
    public TypeBase getValueType() {
        return this.entityType;
    }

    /**
     * 获取指定的关联端标识属性的映射目标字段
     *
     * @param keyAttribute 要获取其映射目标的标识属性的名称
     * @return 映射目标字段
     */
    public String getTargetField(String keyAttribute) {
        Optional<AssociationEndMapping> mapping = this.getMappings().stream().filter(m -> m.getKeyAttribute().equalsIgnoreCase(keyAttribute)).findFirst();
        if (mapping.isPresent())
            return mapping.get().getTargetField();
        throw new RuntimeException("无法获取关联端标识属性" + keyAttribute + "的映射目标字段");
    }

    /**
     * 在基于当前引用元素实施关联导航的过程中，向前推进一步。
     *
     * @param sourceObj 本次导航步的出发地
     * @return 推进后的结果
     */
    @Override
    public Object[] navigationStep(Object sourceObj) {
        //端对象 导航自己
        return new Object[]{sourceObj};
    }


    /**
     * 验证延迟加载合法性，由派生类实现
     *
     * @param reason 返回不能启用延迟加载的原因
     * @return 如果可以启用延迟加载返回true，否则返回false，同时返回原因。
     */
    @Override
    protected boolean validateLazyLoading(ObjectReferencePack<String> reason) {
        reason.realValue = "";
        for (AssociationEndMapping mapping : this.mappings) {
            Attribute attr = this.getHostType().findAttributeByTargetField(mapping.getTargetField());
            if (attr == null) {
                reason.realValue = "当前对象没有关联端对象的标识属性";
                return false;
            }
        }
        return true;
    }

    /**
     * 检测关联端是否为伴随关联端。
     *
     * @return 是否为伴随关联端
     */
    public boolean isCompanionEnd() {
        //找关联型 检测自己
        if (this.getHostType() instanceof AssociationType) {
            AssociationType association = (AssociationType) this.getHostType();
            return association.isCompanionEnd(this);
        }

        return false;
    }

    /**
     * 获取关联端所属关联型在该端上的外键。
     * 对于显式关联或隐式伴随关联的伴随端实体型，如果存在一个属性序列，各属性的映射字段依次为所述关联某端的映射字段，则该属性序列为该关联型在该关联端上的外键，其中的属
     * 性称为外键属性。
     * 显式关联（独立或伴随）的外键定义在关联类型上；隐式伴随关联的外键定义在伴随端实体类型上；隐式独立关联一般没有外键，需要时可将隐式独立关联显式化，在显式化的关联类
     * 上定义外键。显式关联或隐式伴随关联也可能没有外键。
     * 说明
     * defineMissing参数指示外键属性缺失时的行为，如果其值为true，则自动定义缺失的属性，否则引发异常。
     * 但是，即使指示自动定义缺失的外键属性，也不保证能定义成功。将根据现实条件判定能否定义，如果不能定义则引发ForeignKeyGuarantingException。
     *
     * @param definingType  返回定义外键的类型
     * @param defineMissing 指示当外键属性缺失时是否定义该属性
     * @return 外键
     */
    public Attribute[] getForeignKey(ObjectReferencePack<ObjectType> definingType, boolean defineMissing) {
        if (this.foreignKey != null && this.foreignKey.length > 0 && this.definingTypeOfForeignKey != null) {
            definingType.realValue = this.definingTypeOfForeignKey;
            return this.foreignKey;
        }

        //HostType 为 AssociationType 转换为ObjectType
        this.definingTypeOfForeignKey = (ObjectType) this.getHostType();
        definingType.realValue = this.definingTypeOfForeignKey;

        //从Mapping里查
        Attribute[] result = null;
        if (this.getHostType() instanceof AssociationType) {
            AssociationType associationType = (AssociationType) this.getHostType();
            //隐式非独立
            if (!associationType.getVisible() && !associationType.getIndependent()) {
                this.definingTypeOfForeignKey = associationType.getCompanionEnd().getEntityType();
                definingType.realValue = this.definingTypeOfForeignKey;
            }

            if (defineMissing) {

                //当前端为伴随端
                boolean isCom = this.isCompanionEnd();
                if (isCom && !associationType.getVisible()) {
                    ObjectType type = definingType.realValue;
                    result = definingType.realValue.getAttributes().stream().filter(p -> type.getKeyFields().contains(p.getName())).toArray(Attribute[]::new);
                } else {
                    //自己补
                    DerivingBasedForeignKeyGuarantor guarantor = new DerivingBasedForeignKeyGuarantor();
                    result = guarantor.guarantee(definingType.realValue, this);
                }
            } else {
                List<Attribute> tempResult = new ArrayList<>();
                for (AssociationEndMapping mapping : this.mappings) {
                    Attribute attribute = definingType.realValue.findAttributeByTargetField(mapping.getTargetField());
                    if (attribute != null)
                        tempResult.add(attribute);
                    else
                        throw new ForeignKeyNotFoundException("外键属性没有定义");
                }

                result = tempResult.toArray(new Attribute[0]);
            }

            //最后检查一下
            if (result.length == 0) {
                List<Attribute> tempResult = new ArrayList<>();
                for (AssociationEndMapping mapping : this.mappings) {
                    Attribute attribute = definingType.realValue.findAttributeByTargetField((mapping.getTargetField()));
                    if (attribute != null)
                        tempResult.add(attribute);
                    else
                        throw new ForeignKeyNotFoundException("外键属性没有定义");
                }

                result = tempResult.toArray(new Attribute[0]);
            }
        }

        return result;
    }

    /**
     * 获取关联端所属关联型在该端上的外键。
     * 说明
     * defineMissing参数指示外键属性缺失时的行为，如果其值为true，则自动定义缺失的属性，否则引发异常。
     * 但是，即使指示自动定义缺失的外键属性，也不保证能定义成功。将根据现实条件判定能否定义，如果不能定义则引发ForeignKeyGuarantingException。
     *
     * @param defineMissing 指示当外键属性缺失时是否定义该属性
     * @return 外键
     */
    public Attribute[] getForeignKey(boolean defineMissing) {
        long stamp = this.stampedLock.readLock();
        try {
            while (this.foreignKey == null || this.foreignKey.length == 0) {
                long ws = this.stampedLock.tryConvertToWriteLock(stamp);
                if (ws != 0L) {
                    stamp = ws;
                    this.foreignKey = this.getForeignKey(new ObjectReferencePack<>(), defineMissing);
                    break;
                } else {
                    this.stampedLock.unlockRead(stamp);
                    stamp = this.stampedLock.writeLock();
                }
            }
            return this.foreignKey;
        } finally {
            this.stampedLock.unlock(stamp);
        }
    }

    /**
     * 获取关联端所属关联型在该端上的外键。
     * 说明
     * defineMissing参数指示外键属性缺失时的行为，如果其值为true，则自动定义缺失的属性，否则引发异常。
     * 但是，即使指示自动定义缺失的外键属性，也不保证能定义成功。将根据现实条件判定能否定义，如果不能定义则引发ForeignKeyGuarantingException。
     *
     * @return 外键
     */
    public Attribute[] getForeignKey() {
        return this.getForeignKey(false);
    }

    /**
     * 检查关联端所属关联型在该端上的外键是否已定义
     *
     * @return 外键是否已定义
     */
    public boolean foreignKeyExist() {
        if (this.foreignKey != null && this.foreignKey.length > 0 && this.definingTypeOfForeignKey != null)
            return true;
        this.definingTypeOfForeignKey = (ObjectType) this.getHostType();
        if (this.getHostType() instanceof AssociationType) {
            AssociationType associationType = (AssociationType) this.getHostType();
            //隐式非独立
            if (!associationType.getVisible() && !associationType.getIndependent()) {
                this.definingTypeOfForeignKey = associationType.getCompanionEnd().getEntityType();
            }
            List<Attribute> tempResult = new ArrayList<>();
            for (AssociationEndMapping mapping : this.mappings) {
                Attribute attribute = this.definingTypeOfForeignKey.findAttributeByTargetField(mapping.getTargetField());
                if (attribute != null)
                    tempResult.add(attribute);
                else
                    return false;
            }
            tempResult.toArray(this.foreignKey);

        }
        return this.foreignKey != null && this.foreignKey.length > 0;
    }

    /**
     * 获取引用元素的引用键。
     * 设S(s1, s2, ..., sn)为对象O的属性序列，R为O的一个引用元素，该引用的目标型RT存在一个属性序列T(t1, t2, ...,
     * tn)，将S与T的元素一一配对，即ti -> si，然后以ti为依据、以si在O上的取值为参考值构建过滤器，即
     * ∩ ti = vi (i = 1, 2, ..., n)，其中vi为属性si在对象O上的取值，
     * 以该过滤器作用于RT的实例集，如果所得到的对象集刚好为R的值，则称T为R的引用键，ti为引用属性，S为参考键，si为参考属性。
     * 引用键和参考键均不是必须的，如果没有不影响引用的加载，例如在关系数据库中可以通过联表的方式加载。
     * 说明
     * defineMissing参数指示引用键属性缺失时的行为，如果其值为true，则自动定义缺失的属性，否则引发KeyAttributeLackException。
     * 但是，即使指示自动定义缺失的属性，也不保证能定义成功。将根据现实条件判定能否定义，如果不能定义则引发CannotDefiningAttributeException。
     *
     * @param defineMissing 指示是否自动定义缺失的属性
     * @return 引用元素的引用键
     */
    @Override
    public Attribute[] getReferringKey(boolean defineMissing) {
        //从端实体型的属性中获取
        Attribute[] array = this.getEntityType().getAttributes().stream().filter(p -> this.getEntityType().getKeyFields().contains(p.getTargetField())).toArray(Attribute[]::new);
        if (array.length == 0) {
            throw new CannotDefiningAttributeException("无法定义键属性", null);
        }
        return array;
    }

    /**
     * 获取引用元素的引用键。
     * 设S(s1, s2, ..., sn)为对象O的属性序列，R为O的一个引用元素，该引用的目标型RT存在一个属性序列T(t1, t2, ...,
     * tn)，将S与T的元素一一配对，即ti -> si，然后以ti为依据、以si在O上的取值为参考值构建过滤器，即
     * ∩ ti = vi (i = 1, 2, ..., n)，其中vi为属性si在对象O上的取值，
     * 以该过滤器作用于RT的实例集，如果所得到的对象集刚好为R的值，则称T为R的引用键，ti为引用属性，S为参考键，si为参考属性。
     * 引用键和参考键均不是必须的，如果没有不影响引用的加载，例如在关系数据库中可以通过联表的方式加载。
     * 说明
     * defineMissing参数指示引用键属性缺失时的行为，如果其值为true，则自动定义缺失的属性，否则引发KeyAttributeLackException。
     * 但是，即使指示自动定义缺失的属性，也不保证能定义成功。将根据现实条件判定能否定义，如果不能定义则引发CannotDefiningAttributeException。
     *
     * @return 引用元素的引用键
     */
    @Override
    public Attribute[] getReferringKey() {
        return this.getReferringKey(false);
    }

    /**
     * 获取引用元素的参考键。
     * 设S(s1, s2, ..., sn)为对象O的属性序列，R为O的一个引用元素，该引用的目标型RT存在一个属性序列T(t1, t2, ...,
     * tn)，将S与T的元素一一配对，即ti -> si，然后以ti为依据、以si在O上的取值为参考值构建过滤器，即
     * ∩ ti = vi (i = 1, 2, ..., n)，其中vi为属性si在对象O上的取值，
     * 以该过滤器作用于RT的实例集，如果所得到的对象集刚好为R的值，则称T为R的引用键，ti为引用属性，S为参考键，si为参考属性。
     * 引用键和参考键均不是必须的，如果没有不影响引用的加载，例如在关系数据库中可以通过联表的方式加载。
     * 说明
     * defineMissing参数指示参考键属性缺失时的行为，如果其值为true，则自动定义缺失的属性，否则引发KeyAttributeLackException。
     * 但是，即使指示自动定义缺失的属性，也不保证能定义成功。将根据现实条件判定能否定义，如果不能定义则引发CannotDefiningAttributeException。
     *
     * @param defineMissing 指示是否自动定义缺失的属性
     * @return 引用元素的参考键
     */
    @Override
    public Attribute[] getReferredKey(boolean defineMissing) {
        if (this.getHostType() instanceof AssociationType) {
            AssociationType association = (AssociationType) this.getHostType();
            try {
                //从端上取参考键
                return association.getAssociationEnd(this.getName()).getForeignKey(defineMissing);
            } catch (Exception e) {
                throw new CannotDefiningAttributeException(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 获取引用元素的参考键。
     * 设S(s1, s2, ..., sn)为对象O的属性序列，R为O的一个引用元素，该引用的目标型RT存在一个属性序列T(t1, t2, ...,
     * tn)，将S与T的元素一一配对，即ti -> si，然后以ti为依据、以si在O上的取值为参考值构建过滤器，即
     * ∩ ti = vi (i = 1, 2, ..., n)，其中vi为属性si在对象O上的取值，
     * 以该过滤器作用于RT的实例集，如果所得到的对象集刚好为R的值，则称T为R的引用键，ti为引用属性，S为参考键，si为参考属性。
     * 引用键和参考键均不是必须的，如果没有不影响引用的加载，例如在关系数据库中可以通过联表的方式加载。
     * 说明
     * defineMissing参数指示参考键属性缺失时的行为，如果其值为true，则自动定义缺失的属性，否则引发KeyAttributeLackException。
     * 但是，即使指示自动定义缺失的属性，也不保证能定义成功。将根据现实条件判定能否定义，如果不能定义则引发CannotDefiningAttributeException。
     *
     * @return 引用元素的参考键
     */
    @Override
    public Attribute[] getReferredKey() {
        return this.getReferredKey(false);
    }

    /**
     * 获取关联端标识属性的值
     * 首先探测关联对象上是否定义了退化属性，如果是则取该属性的值，否则从关联端对象取值。参见活动图“获取关联端标识属性的值”。
     *
     * @param asoObj       关联端所属的关联对象
     * @param keyAttribute 关联端标识属性的名称
     * @return 关联端标识属性的值
     */
    public Object getKeyAttributeValue(Object asoObj, String keyAttribute) {
        String targetField = this.getTargetField(keyAttribute);
        //寻找退化属性
        Attribute fieldAttribute = this.getHostType().findAttributeByTargetField(targetField);

        //未定义退化属性 或者 退化属性为默认值 则去查询端对象
        if (fieldAttribute == null)
            return this.getEndKeyAttributeValue(asoObj, keyAttribute);

        try {
            Object value = fieldAttribute.getValue(asoObj);
            return this.isDefaultValue(value) ? this.getEndKeyAttributeValue(asoObj, keyAttribute) : value;
        } catch (Exception e) {
            try {
                return this.getEndKeyAttributeValue(asoObj, keyAttribute);
            } catch (Exception ex) {
                throw new IllegalArgumentException("既无法从关联型" + asoObj.getClass() + "的映射字段为" + targetField + "的属性中获取有效的标识也无法从端对象" + this.getEntityType().getClrType() + "中获取有效的标识,请检查此关联型的赋值.", ex);
            }

        }
    }

    /**
     * 获取端对象的键值
     *
     * @param asoObj       端对象
     * @param keyAttribute 键属性的名称
     * @return 端对象键属性的值
     */
    private Object getEndKeyAttributeValue(Object asoObj, String keyAttribute) {
        Object endObj = this.getValue(asoObj);
        Attribute attr = this.getEntityType().getAttribute(keyAttribute);
        return attr.getValue(endObj);
    }

    /**
     * 判断是否为Obase基元类型默认值
     *
     * @param object 要判断的值
     * @return 是否为Obase基元类型默认值
     */
    private boolean isDefaultValue(Object object) {
        Class<?> className = object.getClass();
        if (className.equals(java.lang.Integer.class)) {
            return (int) object == 0;
        } else if (className.equals(java.lang.Byte.class)) {
            return (byte) object == 0;
        } else if (className.equals(java.lang.Long.class)) {
            return (long) object == 0L;
        } else if (className.equals(java.lang.Double.class)) {
            return (double) object == 0.0d;
        } else if (className.equals(java.lang.Float.class)) {
            return (float) object == 0.0f;
        } else if (className.equals(java.lang.Character.class)) {
            return (char) object == '\u0000';
        } else if (className.equals(java.lang.Short.class)) {
            return (short) object == 0;
        } else if (className.equals(String.class))
            return ((String) object).isEmpty();
        return false;
    }

    /**
     * 转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "AssociationEnd:{{Name-\"" + this.getName() + "\",EntityType-\"" + this.entityType + "\"}}";
    }
}
