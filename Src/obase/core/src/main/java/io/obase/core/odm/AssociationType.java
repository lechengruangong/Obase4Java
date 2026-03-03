/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联型.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-2 15:40:05
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

/**
 * 表示关联型
 */
public class AssociationType extends ObjectType {

    /**
     * 邮戳锁
     */
    private final StampedLock stampedLock = new StampedLock();

    /**
     * 伴随端
     */
    private AssociationEnd companionEnd;

    /**
     * 默认的存储排序规则
     */
    private List<OrderRule> defaultStoringOrder;

    /**
     * 获取当前对象类型的映射表的键字段集合
     */
    private List<String> keyFields;

    /**
     * 是否为显式关联，默认为false（表示有关联关系类型）
     */
    private boolean visible = false;

    /**
     * 根据Clr类型创建Obj类型实例
     *
     * @param clrType      对象运行时类型
     * @param derivingFrom 基类
     */
    public AssociationType(Class<?> clrType, StructuralType derivingFrom) {
        super(clrType, derivingFrom);
        this.typeName.IsAssociation = true;
        this.typeName.IsEntity = false;
    }

    /**
     * 根据Clr类型创建Obj类型实例
     *
     * @param clrType 对象运行时类型
     */
    public AssociationType(Class<?> clrType) {
        super(clrType);
        this.typeName.IsAssociation = true;
        this.typeName.IsEntity = false;
    }

    /**
     * 获取伴随关联端(和表名相同的端)，如果关联型为独立映射返回Null。
     * 伴随端的判定条件：（1）映射表与关联型相同；（2）关联端的映射字段与实体型标识属性的映射字段相同
     * 增补:如果关联端实体型有基类（含间接基类），还要比对基类的映射表，只要其本身或基数中有一个与关联型的映射表相同，就应判定为伴随。
     *
     * @return 伴随关联端
     */
    public AssociationEnd getCompanionEnd() {
        if (this.companionEnd == null) {
            for (AssociationEnd item : this.getAssociationEnds()) {
                //条件1和条件2
                List<String> targetFieldList1 = item.getMappings().stream().map(AssociationEndMapping::getTargetField).collect(Collectors.toList());
                List<String> targetFieldList2 = item.getMappings().stream().map(AssociationEndMapping::getTargetField).collect(Collectors.toList());
                List<String> itemFields1 = new ArrayList<>(item.getEntityType().getKeyFields());
                List<String> itemFields2 = new ArrayList<>(item.getEntityType().getKeyFields());
                targetFieldList1.removeAll(itemFields1);
                itemFields2.removeAll(targetFieldList2);

                if (this.targetTable.equalsIgnoreCase(item.getEntityType().getTargetTable()) && targetFieldList1.size() == 0 && itemFields2.size() == 0) {
                    this.companionEnd = item;
                }
                //增补条件
                ObjectType currentObjectType = (ObjectType) item.getEntityType().getDerivingFrom();
                List<String> derivingFromTableNames = new ArrayList<>();
                while (currentObjectType != null) {
                    derivingFromTableNames.add(currentObjectType.getTargetTable());
                    currentObjectType = (ObjectType) currentObjectType.getDerivingFrom();
                }

                if (derivingFromTableNames.contains(this.targetTable))
                    this.companionEnd = item;
            }
        }
        return this.companionEnd;
    }

    /**
     * 获取关联型包含的关联端的集合
     *
     * @return 关联端的集合
     */
    public List<AssociationEnd> getAssociationEnds() {
        List<AssociationEnd> associationEnds = new ArrayList<>();

        for (TypeElement ele : this.getElements()) {
            if (ele instanceof AssociationEnd) {
                associationEnds.add((AssociationEnd) ele);
            }
        }

        return associationEnds;
    }

    /**
     * 获取关联型包含的聚合关联端的集合
     *
     * @return 关联端的集合
     */
    public List<AssociationEnd> getAggregatedEnds() {
        List<AssociationEnd> associationEnds = new ArrayList<>();

        for (TypeElement ele : this.getElements()) {
            if (ele instanceof AssociationEnd) {
                AssociationEnd end = (AssociationEnd) ele;
                if (end.getIsAggregated())
                    associationEnds.add(end);
            }
        }

        return associationEnds;
    }

    /**
     * 获取一个值，该值表示是否为显式关联，默认为false
     *
     * @return 是否为显式关联，默认为false
     */
    public boolean getVisible() {
        return this.visible;
    }

    /**
     * 设置一个值，该值表示是否为显式关联，默认为false
     *
     * @param visible 是否为显式关联，默认为false
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * 获取一个值，该值指示关联型是否为独立映射
     *
     * @return 该值指示关联型是否为独立映射
     */
    public boolean getIndependent() {
        boolean isIndependent = true;
        for (AssociationEnd item : this.getAssociationEnds()) {
            if (this.targetTable.equalsIgnoreCase(item.getEntityType().getTargetTable()) || this.targetTable.equalsIgnoreCase(Utils.getDerivedTargetTable(item.getEntityType()))) {
                isIndependent = false;
                break;
            }
        }
        return isIndependent;
    }

    /**
     * 获取当前对象类型的映射表的键字段集合
     *
     * @return 当前对象类型的映射表的键字段集合
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
                    for (AssociationEnd end : this.getAssociationEnds()) {
                        for (AssociationEndMapping map : end.getMappings()) {
                            this.keyFields.add(map.getTargetField());
                        }
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
                    for (AssociationEnd end : this.getAssociationEnds()) {
                        for (AssociationEndMapping map : end.getMappings()) {
                            OrderRule orderRule = new OrderRule();
                            orderRule.setOrderBy(map);
                            this.defaultStoringOrder.add(orderRule);
                        }
                    }
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
        List<String> result = new ArrayList<>();
        for (AssociationEnd end : this.getAssociationEnds()) {
            for (String name : end.getEntityType().getKeyMemberNames()) {
                result.add(end.getName() + "." + name);
            }
        }
        return result.toArray(new String[0]);
    }

    /**
     * 根据指定的关联端名称及标识属性查找关联端映射，并返回映射目标字段。
     *
     * @param endName      关联端名称
     * @param keyAttribute 关联端对象的标识属性
     * @return 映射目标字段
     */
    public String findAssociationEndMapping(String endName, String keyAttribute) {
        AssociationEnd end = this.getAssociationEnd(endName);
        AssociationEndMapping mapping = end.getMappings().stream().filter(p -> p.getKeyAttribute().equalsIgnoreCase(keyAttribute)).findFirst().orElse(null);
        return mapping == null ? null : mapping.getTargetField();
    }

    /**
     * 根据指定的路径（用点号分隔的关联端名称及标识属性）查找关联端映射，并返回映射目标字段。
     *
     * @param path 关联端名称
     * @return 映射目标字段
     */
    public String findAssociationEndMapping(String path) {
        String[] paths = path.split("\\.");
        return this.findAssociationEndMapping(paths[0], paths[1]);
    }


    /**
     * 判定指定的关联端是否为当前关联型的伴随关联端（简称伴随端）,和关联型表名相同返回true,否则false。
     *
     * @param associationEnd 要判定的关联端
     * @return 和关联型表名相同返回true, 否则false
     */
    public boolean isCompanionEnd(AssociationEnd associationEnd) {
        return associationEnd.equals(this.getCompanionEnd());
    }

    /**
     * 判定指定的关联端是否为当前关联型的伴随关联端（简称伴随端）。
     *
     * @param endName 要判定的关联端的名称
     * @return 和关联型表名相同返回true, 否则false
     */
    public boolean isCompanionEnd(String endName) {
        return this.isCompanionEnd(this.getAssociationEnd(endName));
    }

    /**
     * 根据名称获取关联端
     *
     * @param name 关联端名称
     * @return 关联端
     */
    public AssociationEnd getAssociationEnd(String name) {
        return (AssociationEnd) this.getElement(name);
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

        //隐式关联型 不能有属性
        if (!this.visible) {
            TypeElement attr = this.elements.values().stream().filter(p -> p.getElementType().equals(EElementType.Attribute)).findFirst().orElse(null);
            if (attr != null)
                if (!((Attribute) attr).getIsForeignKeyDefineMissing())
                    message.add("隐式关联型" + this.getName() + "内应只有关联端,属性" + attr.getName() + "不应被定义.");
        }

        //关联端数量
        if (this.getAssociationEnds() == null || this.getAssociationEnds().size() == 0)
            message.add("关联型" + this.getName() + "内无关联端.");

        if (this.getAssociationEnds() != null && this.getAssociationEnds().size() < 2)
            message.add("关联型" + this.getName() + "内关联端少于2个.");

        if (this.getAssociationEnds() != null) {
            //检查关联端
            for (AssociationEnd end : this.getAssociationEnds()) {
                //检查关联端本身
                Property endProperty = ObaseIntrospector.getObaseBeanProperties(this.clrType).stream().filter(p -> p.getName().equalsIgnoreCase(end.getName())).findFirst().orElse(null);
                if (endProperty == null)
                    message.add("关联型" + this.getName() + "内无法找到关联端" + end.getName() + "的属性访问器.");

                if (end.getMappings() == null || end.getMappings().size() == 0)
                    message.add("关联型" + this.getName() + "的关联端" + end.getName() + "没有映射.");

                //检查映射
                if (end.getMappings() != null) {
                    //按照KeyAttr分组 分组后如果与之前个数不相同 则有重复
                    boolean isRepeat = end.getMappings().stream().collect(Collectors.groupingBy(AssociationEndMapping::getKeyAttribute)).size() != end.getMappings().size();

                    if (isRepeat)
                        message.add("关联型" + this.getName() + "的关联端" + end.getName() + "内有重复的映射.");

                    //检查Mapping的KeyAttr是否在端类型中存在
                    for (AssociationEndMapping mapping : end.getMappings())
                        if (end.getEntityType().getAttribute(mapping.getKeyAttribute()) == null)
                            message.add("关联型" + this.getName() + "的关联端" + end.getName() + "映射" + mapping.getKeyAttribute() + "属性无法在端类型" + end.getEntityType().getClrType().getName() + "中找到.");
                    //检查是否所有的KeyAttr都有映射
                    for (String entityTypeKeyAttribute : end.getEntityType().getKeyAttributes()) {
                        //获取端类型的标识属性的映射数量 必须为1
                        long mapCount = end.getMappings().stream().filter(p -> p.getKeyAttribute().equalsIgnoreCase(entityTypeKeyAttribute)).count();
                        if (mapCount != 1)
                            message.add("关联型" + this.getName() + "的" + end.getEntityType().getClrType().getName() + "类型关联端" + end.getName() + "的标识属性" + entityTypeKeyAttribute + "应有且只1个映射,但现在有" + mapCount + "个映射.");
                    }
                }

                //检查设值器和取值器
                if (end.getValueSetter() == null)
                    message.add(this.getClrType().getName() + "的关联端" + end.getName() + "没有设值器.");

                if (end.getValueGetter() == null)
                    message.add(this.getClrType().getName() + "的关联端" + end.getName() + "没有取值器.");
            }
            //检查键属性
            if (this.keyFields == null) {
                this.keyFields = new ArrayList<>();
                for (AssociationEnd end : this.getAssociationEnds())
                    for (AssociationEndMapping map : end.getMappings()) {
                        this.keyFields.add(map.getTargetField());
                    }
            }

            //检查键属性
            if (this.defaultStoringOrder == null) {
                this.defaultStoringOrder = new ArrayList<>();
                for (AssociationEnd end : this.getAssociationEnds())
                    for (AssociationEndMapping map : end.getMappings()) {
                        OrderRule orderRule = new OrderRule();
                        orderRule.setOrderBy(map);
                        orderRule.setInverted(false);
                        this.defaultStoringOrder.add(orderRule);
                    }
            }
        }

        //通用的对象类型检查
        this.commonIntegrityCheck(errDictionary);

        //如果有检查失败消息
        if (message.size() > 0) {
            //就与现有的问题合并
            String name = this.clrType != null ? this.clrType.getSimpleName() : this.name;
            if (!errDictionary.containsKey(name))
                errDictionary.put(name, message);
            else
                errDictionary.get(name).addAll(message);
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
        List<ObjectKeyMember> objectKeyMemberList = new ArrayList<>();
        //遍历关联端
        for (AssociationEnd associationEnd : this.getAssociationEnds()) {
            //获取端对象
            Object endObj = this.getValue(targetObj, associationEnd);
            for (AssociationEndMapping mapping : associationEnd.getMappings()) {
                Object value;
                if (endObj == null) {
                    //根据字段名查找属性
                    Attribute attr = this.findAttributeByTargetField(mapping.getTargetField());
                    if (attr == null)
                        continue;
                    value = this.getValue(targetObj, attr);
                } else {
                    //取出关联端的标识属性值
                    value = this.getValue(endObj, associationEnd.getEntityType(), mapping.getKeyAttribute());
                }

                //创建标识成员
                ObjectKeyMember member =
                        new ObjectKeyMember(
                                associationEnd.getEntityType().getClrType().getName() + "-" + associationEnd.getName() + "." +
                                        mapping.getKeyAttribute(), value);
                objectKeyMemberList.add(member);
            }
        }

        //创建对象标识
        return new ObjectKey(this, objectKeyMemberList);
    }

    /**
     * 从对象中获取元素（属性、关联引用、关联端）的值
     *
     * @param obj     目标对象
     * @param element 目标元素
     * @return 值
     */
    private Object getValue(Object obj, TypeElement element) {
        Object result;
        if (!(element instanceof ReferenceElement && obj instanceof IIntervene)) {
            //获取值
            result = element.getValueGetter().getValue(obj);
        } else {
            IIntervene inter = (IIntervene) obj;
            //禁用延迟加载（防止延迟加载期间内部访问属性又开始加载，造成死循环）
            inter.forbidLazyLoading();
            //获取值
            result = element.getValueGetter().getValue(obj);
            //启用延迟加载
            inter.enableLazyLoading();
        }

        return result;
    }

    /**
     * 从对象中获取元素（属性、关联引用、关联端）的值。
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
     * 获取类型的筛选键。
     * 对于类型的某一个属性或属性序列，如果其值或值序列可以作为该类型实例的标识，该属性或属性序列即可作为该类型的筛选键。
     * 对于实体型，可以用主键作为筛选键。对于关联型，可以用其在各关联端上的外键属性组合成的属性序列作为筛选键。
     *
     * @return 构成筛选键的属性序列
     */
    @Override
    public Attribute[] getFilterKey() {
        //关联型 取在各关联端上的外键属性组合成的属性序列作为筛选键
        List<Attribute> attributes = new ArrayList<>();
        for (AssociationEnd associationEnd : this.getAssociationEnds()) {
            attributes.addAll(Arrays.asList(associationEnd.getForeignKey(true)));
        }
        return attributes.toArray(new Attribute[0]);
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "AssociationType:{{Name-\"" + this.getName() + "\",ClrType-\"" + this.getClrType().getName() + "\"}}";
    }
}
