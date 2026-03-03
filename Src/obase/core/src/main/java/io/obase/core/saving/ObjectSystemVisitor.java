/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象系统访问器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:53:07
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 对象系统访问器
 */
@Deprecated
public class ObjectSystemVisitor {

    /**
     * 关联导航。
     * 取出源对象中指定关联引用的值，如果为隐式关联则实施显式化操作（自动创建隐式关联对象）。
     *
     * @param obj       源对象
     * @param reference 要导航的关联引用
     * @return 关联导航得到的对象
     */
    public static Iterable<Object> associationNavigate(Object obj, AssociationReference reference) {
        //获取关联引用对象（取出的是实体对象集合:List<文章>）
        Iterable<Object> target = getValue(obj, reference);

        if (!reference.getAssociationType().getVisible()) {
            AssociationType type = reference.getAssociationType();
            List<Object> assocObjs = null;
            //遍历关联对象引用
            for (Object item : target) {
                if (assocObjs == null)
                    assocObjs = new ArrayList<>();
                //左端对象 和 右端对象
                Map<String, Object> dic = new HashMap<>();
                dic.put(reference.getLeftEnd(), obj);
                dic.put(reference.getRightEnd(), item);
                //创建隐式关联对象（构造的是隐式关联对象（ImplicitAssociation<分类,文章>））
                Object assObj = buildObject(type, dic);
                //找出左端
                Optional<AssociationEnd> leftEnd = type.getAssociationEnds().stream().filter(p -> p.getName().equalsIgnoreCase(reference.getLeftEnd())).findFirst();
                leftEnd.ifPresent(associationEnd -> {
                    try {
                        setEndMappingFiled(type, associationEnd, obj, assObj);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("无法为关联左端设值" + e.getMessage(), e);
                    }
                });
                //找出右端
                Optional<AssociationEnd> rightEnd = type.getAssociationEnds().stream().filter(p -> p.getName().equalsIgnoreCase(reference.getRightEnd())).findFirst();
                rightEnd.ifPresent(associationEnd -> {
                    try {
                        setEndMappingFiled(type, associationEnd, item, assObj);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("无法为关联右端设值" + e.getMessage(), e);
                    }
                });
                //添加到关联对象集合
                assocObjs.add(assObj);
            }

            //隐式关联对象集合
            target = assocObjs;
        }

        //返回 显示关联直接就是引用对象 隐式则需要创建
        return target;
    }

    /**
     * 为隐式关联型的关联映射属性设值
     *
     * @param type   关联型
     * @param end    关联端
     * @param endObj 端对象
     * @param assObj 关联对象
     */
    private static void setEndMappingFiled(AssociationType type, AssociationEnd end, Object endObj, Object assObj) {
        //端的实体型
        EntityType endType = end.getEntityType();
        //端的键属性
        List<Attribute> endKeyAttrs = endType.getAttributes().stream().filter(p -> endType.getKeyAttributes().contains(p.getName())).collect(Collectors.toList());
        //在端内寻找符合条件的映射和属性
        for (Attribute endKeyAttr : endKeyAttrs) {
            for (AssociationEndMapping mapping : end.getMappings()) {
                //如果映射键属性和端对象键属性相同
                if (Objects.equals(endKeyAttr.getName(), mapping.getKeyAttribute())) {
                    //从端对象内取出键属性
                    Object keyValue = endKeyAttr.getValueGetter().getValue(endObj);
                    //为关联型内映射属性(映射属性在表内字段肯定和映射的目标属性相同)赋值
                    Optional<Attribute> attribute = type.getAttributes().stream().filter(p -> Objects.equals(p.getTargetField(), mapping.getTargetField())).findFirst();
                    if (attribute.isPresent() && attribute.get().getValueSetter() != null) {
                        attribute.get().getValueSetter().setValue(assObj, keyValue);
                    }
                }
            }
        }
    }

    /**
     * 从对象中获取元素（属性、关联引用、关联端）的值
     *
     * @param obj         目标对象
     * @param elementName 目标元素的名称
     * @param model       对象数据模型
     * @return 获取到的值
     */
    public static Object getValue(Object obj, String elementName, ObjectDataModel model) {
        //获取模型
        StructuralType type = model.getStructuralType(obj.getClass());
        //获取对象元素的值
        return getValue(obj, type, elementName);
    }

    /**
     * 对象中获取元素（属性、关联引用、关联端）的值
     *
     * @param obj         目标对象
     * @param type        对象的类型（实体型、关联型、复杂类型）
     * @param elementName 目标元素的名称
     * @return 获取到的值
     */
    public static Object getValue(Object obj, StructuralType type, String elementName) {
        //获取元素
        TypeElement typeElement = type.getElement(elementName);
        //获取对象元素的值
        return getValue(obj, typeElement);
    }

    /**
     * 从对象中获取元素（属性、关联引用、关联端）的值
     *
     * @param obj     目标对象
     * @param element 目标元素
     * @return 获取到的值
     */
    public static Object getValue(Object obj, TypeElement element) {
        if (element instanceof ReferenceElement && obj instanceof IIntervene) {
            IIntervene inter = (IIntervene) obj;
            //禁用延迟加载（防止延迟加载期间内部访问属性又开始加载，造成死循环）
            inter.forbidLazyLoading();
            //获取值
            Object result = element.getValueGetter().getValue(obj);
            //启用延迟加载
            inter.enableLazyLoading();
            return result;
        } else {
            //获取值
            return element.getValueGetter().getValue(obj);
        }
    }

    /**
     * 从对象获取关联引用的所有取值，如果关联重数大于1则多次调用取值器取出所有值。
     *
     * @param obj       目标对象
     * @param reference 要读取其值的关联引用
     * @return 获取到的值
     */
    public static Iterable<Object> getValue(Object obj, AssociationReference reference) {
        if (obj instanceof IIntervene) {
            IIntervene inter = (IIntervene) obj;
            inter.forbidLazyLoading();
        }

        Object value = reference.getValueGetter().getValue(obj);

        //是否是多重的
        if (reference.getIsMultiple()) {
            Iterable<Object> values = (Iterable<Object>) value;
            if (values == null)
                values = new ArrayList<>();
            if (obj instanceof IIntervene) {
                IIntervene inter = (IIntervene) obj;
                inter.enableLazyLoading();
            }
            return values;
        }

        if (obj instanceof IIntervene) {
            IIntervene inter = (IIntervene) obj;
            inter.enableLazyLoading();
        }
        //都转换为列表
        if (value == null) {
            return new ArrayList<>();
        } else {
            List<Object> result = new ArrayList<>();
            result.add(value);
            return result;
        }
    }

    /**
     * 获取关联对象指定关联端的标识属性的值
     *
     * @param associationObj  指定的关联对象
     * @param associationType 指定关联对象的类型
     * @param associationEnd  要获取其标识属性值的关联端
     * @param keyAttribute    要获取其值的标识属性
     * @return 获取到的值
     */
    public static Object getValue(Object associationObj, AssociationType associationType, AssociationEnd associationEnd,
                                  String keyAttribute) {
        Object value = null;
        //获取关联端对象
        Object endObj = getValue(associationObj, associationEnd);
        if (endObj == null) {
            //获取关联端标识属性字段名
            String fieId = associationEnd.getTargetField(keyAttribute);
            //根据字段在关联型查询属性
            Attribute attr = associationType.findAttributeByTargetField(fieId);
            if (attr != null)
                //获取属性值
                value = getValue(associationObj, attr);
        } else {
            //获取关联端的属性值
            value = getValue(endObj, associationEnd.getEntityType(), keyAttribute);
        }

        //返回标识属性值
        return value;
    }

    /**
     * 从对象中获取指定子属性的值。子属性是某一复杂属性的类型的属性，该复杂属性称为该子属性的父属性。
     *
     * @param obj       目标对象
     * @param attribute 子属性
     * @param parent    指向父属性的属性路径
     * @return 获取到的值
     */
    public static Object getValue(Object obj, Attribute attribute, AttributePath parent) {
        Object targetObj = obj;
        Iterator<Attribute> enumerator = parent.iterator();
        while (enumerator.hasNext()) targetObj = parent.getNext().getValueGetter().getValue(targetObj);
        IValueGetter attributeValueGetter = attribute.getValueGetter();
        return attributeValueGetter.getValue(targetObj);
    }

    /**
     * 为对象的关联引用设置值，如果关联重数大于1则多次调用设值器。
     *
     * @param obj       目标对象
     * @param reference 目标关联引用
     * @param values    要设置的值的集合
     */
    public static void setValue(Object obj, AssociationReference reference, Object[] values) {
        //追加设值
        if (values != null && values.length > 0)
            for (Object value : values)
                setValue(obj, reference, value);
    }

    /**
     * 为对象的指定元素（属性、关联引用、关联端）设置值
     *
     * @param obj         目标对象
     * @param elementName 目标元素的名称
     * @param value       值对象
     * @param model       对象数据模型
     */
    public static void setValue(Object obj, String elementName, Object value, ObjectDataModel model) {
        //获取对象模型
        StructuralType type = model.getStructuralType(obj.getClass());
        //模型元素设值
        setValue(obj, type, elementName, value);
    }

    /**
     * 为对象的指定元素（属性、关联引用、关联端）设置值
     *
     * @param obj         目标对象
     * @param type        对象的类型（实体型、关联型、复杂类型
     * @param elementName 目标元素的名称
     * @param value       值对象
     */
    public static void setValue(Object obj, StructuralType type, String elementName, Object value) {
        //获取元素
        TypeElement element = type.getElement(elementName);
        //元素设值
        setValue(obj, element, value);
    }

    /**
     * 为对象的指定元素（属性、关联引用、关联端）设置值
     *
     * @param obj     目标对象
     * @param element 目标元素
     * @param value   值对象
     */
    public static void setValue(Object obj, TypeElement element, Object value) {
        if (element instanceof ReferenceElement) {
            //禁用延迟加载（设值时禁用延迟加载避免造成循环）
            if (obj instanceof IIntervene) {
                IIntervene inter = (IIntervene) obj;
                inter.forbidLazyLoading();
            }
            //设置值
            if (element.getValueSetter() != null)
                element.getValueSetter().setValue(obj, value);
            //启用延迟加载
            if (obj instanceof IIntervene) {
                IIntervene inter = (IIntervene) obj;
                inter.enableLazyLoading();
            }
        } else {
            if (element.getValueSetter() != null)
                element.getValueSetter().setValue(obj, value);
        }
    }

    /**
     * 构造对象
     *
     * @param type            目标对象的类型（实体型、关联型、复杂类型）
     * @param referredObjects 一个字典，存储目标对象的关联引用或关联端，键为元素名称，值为关联引用或关联端对象
     * @return 构造的对象
     */
    public static Object buildObject(StructuralType type, Map<String, Object> referredObjects) {
        //使用构造器构造对象
        Object target = type.getConstructor().construct(null);
        //遍历引用属性（关联端、关联引用）
        for (TypeElement re : type.getElements())
            if (referredObjects != null) {
                if (referredObjects.containsKey(re.getName())) {
                    setValue(target, re, referredObjects.get(re.getName()));
                }
            }

        return target;
    }

    /**
     * 构造对象
     *
     * @param type       目标对象的类型（实体型、关联型、复杂类型）
     * @param attrGetter 一个委托，用于获取目标对象的属性值，委托参数为要获取其值的属性，委托返回值为属性值
     * @param nullCount  此对象查出的数据中DBNull的个数
     * @return 构造的对象
     */
    public static Object buildObject(StructuralType type, Function<Attribute, Object> attrGetter, ObjectReferencePack<Integer> nullCount) {
        //使用构造器构造对象
        Object target = type.getConstructor().construct(null);

        //遍历属性
        for (Attribute attr : type.getAttributes()) {
            //是否为复杂属性 复杂属性进行剥离
            Object value = !attr.getIsComplex()
                    ? attrGetter.apply(attr)
                    : buildObject(((ComplexAttribute) attr).getComplexType(), attrGetter, nullCount);
            //向对象设置属性值
            setValue(target, type, attr.getName(), value);
            //记录DBNull的个数
            if (value == null) nullCount.realValue++;
        }

        return target;
    }

    /**
     * 构造对象
     *
     * @param type            目标对象的类型（实体型、关联型、复杂类型）
     * @param attrGetter      一个委托，用于获取目标对象的属性值，委托参数为要获取其值的属性，委托返回值为属性值
     * @param referredObjects 引用对象
     * @param nullCount       此对象查出的数据中DBNull的个数
     * @return 构造的对象
     */
    public static Object buildObject(StructuralType type, Function<Attribute, Object> attrGetter,
                                     Map<String, Object> referredObjects, ObjectReferencePack<Integer> nullCount) {
        //使用构造器构造对象
        Object target = buildObject(type, attrGetter, nullCount);

        for (TypeElement re : type.getElements()) {
            setValue(target, re, referredObjects.get(re.getName()));
        }

        return target;
    }

    /**
     * 获取实体对象的标识
     *
     * @param entityObj  目标对象
     * @param entityType 目标对象对应的实体型
     * @return 对象标识
     */
    public static ObjectKey getObjectKey(Object entityObj, EntityType entityType) {
        //对象标识成员由标识属性名与属性值组合
        List<ObjectKeyMember> objectKeyMemberList = new ArrayList<>();
        if (entityType.getKeyAttributes() != null)
            for (String key : entityType.getKeyAttributes()) {
                //获取属性值
                Object value = getValue(entityObj, entityType, key);
                //创建标识成员
                ObjectKeyMember member = new ObjectKeyMember(entityType.getClrType().getName() + "-" + key, value);
                objectKeyMemberList.add(member);
            }

        //创建对象标识
        return new ObjectKey(entityType, objectKeyMemberList);
    }

    /**
     * 获取关联对象的标识
     *
     * @param associationObj  目标对象
     * @param associationType 目标对象对应的关联型
     * @return 对象标识
     */
    public static ObjectKey getObjectKey(Object associationObj, AssociationType associationType) {
        List<ObjectKeyMember> objectKeyMemberList = new ArrayList<>();
        //遍历关联端
        for (AssociationEnd associationEnd : associationType.getAssociationEnds()) {
            //获取端对象
            Object endObj = getValue(associationObj, associationEnd);
            //遍历关联端的映射
            for (AssociationEndMapping mapping : associationEnd.getMappings()) {
                Object value;
                if (endObj == null) {
                    //根据字段名查找属性
                    Attribute attr = associationType.findAttributeByTargetField(mapping.getTargetField());
                    value = getValue(associationObj, attr);
                } else {
                    //取出关联端的标识属性值
                    value = getValue(endObj, associationEnd.getEntityType(), mapping.getKeyAttribute());
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
        return new ObjectKey(associationType, objectKeyMemberList);
    }

    /**
     * 获取对象的标识
     *
     * @param obj       目标对象
     * @param modelType 目标对象对应的模型类型
     * @return 对象标识
     */
    public static ObjectKey getObjectKey(Object obj, StructuralType modelType) {
        if (modelType instanceof EntityType) {
            return getObjectKey(obj, (EntityType) modelType);
        } else if (modelType instanceof AssociationType) {
            return getObjectKey(obj, (AssociationType) modelType);
        }
        return null;
    }

    /**
     * 获取关联端标识
     *
     * @param associationObj  要获取其标识的关联端所属的关联对象
     * @param associationType 关联对象的类型
     * @param associationEnd  要获取其标识的关联端
     * @return 对象标识
     */
    public static ObjectKey getObjectKey(Object associationObj, AssociationType associationType,
                                         AssociationEnd associationEnd) {
        //获取关联端对象
        Object endObj = getValue(associationObj, associationEnd);
        List<ObjectKeyMember> members = new ArrayList<>();

        for (AssociationEndMapping mapping : associationEnd.getMappings()) {
            Object value;
            if (endObj == null) {
                //根据字段名查找属性
                Attribute attr = associationType.findAttributeByTargetField(mapping.getTargetField());
                //获取关联对象属性值
                value = getValue(associationObj, attr);
            } else {
                //获取关联端的标识属性值
                value = getValue(endObj, associationEnd.getEntityType(), mapping.getKeyAttribute());
            }

            //创建标识成员
            ObjectKeyMember member = new ObjectKeyMember(associationEnd.getEntityType().getClrType().getName() + "-" + mapping.getKeyAttribute(),
                    value);
            members.add(member);
        }

        //创建对象标识
        return new ObjectKey(associationType, members);
    }
}
