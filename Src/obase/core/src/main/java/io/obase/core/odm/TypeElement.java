/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：类型元素,为各种类型元素（属性、关联引用、关联端）提供基础实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-24 15:50:28
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 为各种类型元素（属性、关联引用、关联端）提供基础实现。
 */
public abstract class TypeElement {

    /**
     * 元素的类型
     */
    private final EElementType elementType;

    /**
     * 元素扩展
     */
    private final List<ElementExtension> extensions = new ArrayList<>();

    /**
     * 名称
     */
    private final String name;

    /**
     * 元素宿主对象的类型
     */
    private StructuralType hostType;

    /**
     * 指示元素是否具有多重性，即其值是否为集合类型。
     */
    private boolean isMultiple;

    /**
     * 取值器
     */
    private IValueGetter valueGetter;

    /**
     * 设置器
     */
    private IValueSetter valueSetter;

    /**
     * 创建TypeElement实例
     *
     * @param name        元素的名称
     * @param elementType 元素的类型
     */
    protected TypeElement(String name, EElementType elementType) {
        this.name = name;
        this.elementType = elementType;
    }

    /**
     * 获取
     *
     * @return 取值器
     */
    public IValueGetter getValueGetter() {
        return this.valueGetter;
    }

    /**
     * 设置取值器
     *
     * @param valueGetter 取值器
     */
    public void setValueGetter(IValueGetter valueGetter) {
        this.valueGetter = valueGetter;
    }

    /**
     * 获取设值器
     *
     * @return 设值器
     */
    public IValueSetter getValueSetter() {
        return this.valueSetter;
    }

    /**
     * 设置设值器
     *
     * @param valueSetter 设值器
     */
    public void setValueSetter(IValueSetter valueSetter) {
        this.valueSetter = valueSetter;
    }

    /**
     * 获取元素（属性、关联引用、关联端）的名称
     *
     * @return 名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取宿主类型
     *
     * @return 宿主类型
     */
    public StructuralType getHostType() {
        return this.hostType;
    }

    /**
     * 设置宿主类型
     *
     * @param hostType 宿主类型
     */
    void setHostType(StructuralType hostType) {
        this.hostType = hostType;
    }

    /**
     * 获取一个值，该值指示元素是否具有多重性，即其值是否为集合类型。
     *
     * @return 是否具有多重性
     */
    public boolean getIsMultiple() {
        return this.isMultiple;
    }

    /**
     * 设置一个值，该值指示元素是否具有多重性，即其值是否为集合类型。
     *
     * @param multiple 是否具有多重性
     */
    public void setIsMultiple(boolean multiple) {
        this.isMultiple = multiple;
    }

    /**
     * 获取元素值的类型
     *
     * @return 元素值的类型
     */
    public abstract TypeBase getValueType();


    /**
     * 获取元素的类型
     *
     * @return 元素的类型
     */
    public EElementType getElementType() {
        return this.elementType;
    }

    /**
     * 为当前元素添加扩展
     *
     * @param extension 要添加的元素扩展
     */
    public void addExtension(ElementExtension extension) {
        this.extensions.add(extension);
    }

    /**
     * 为当前元素添加扩展
     *
     * @param extensionType 扩展类型，它是一个继承自ElementExtension的类型
     * @return 新创建的类型扩展实例
     */
    public ElementExtension addExtension(Class<?> extensionType) {
        if (!(ElementExtension.class.isAssignableFrom(extensionType)))
            throw new IllegalArgumentException("添加扩展失败," + extensionType.getName() + "不是ElementExtension类型");
        try {
            ElementExtension extension = (ElementExtension) extensionType.getConstructor().newInstance();
            this.extensions.add(extension);
            return extension;
        } catch (Exception e) {
            throw new IllegalArgumentException("添加扩展失败," + extensionType.getName() + "没有适合的无参构造函数", e);
        }
    }

    /**
     * 为指定对象的当前元素设置值，适用于具有多重性的元素
     *
     * @param targetObj 要为其元素设值的对象
     * @param value     元素的值
     */
    public void setValue(Object targetObj, Iterable<Object> value) {
        if (targetObj instanceof IIntervene) {
            IIntervene inter1 = (IIntervene) targetObj;
            //禁用延迟加载（防止延迟加载期间内部访问属性又开始加载，造成死循环）
            inter1.forbidLazyLoading();
        }

        EValueSettingMode settingMode = this.valueSetter.getMode();
        switch (settingMode) {
            case Assignment:
                this.valueSetter.setValue(targetObj, value);
                break;
            case Appending:
                if (value == null) return;
                for (Object valueItem : value) {
                    this.valueSetter.setValue(targetObj, valueItem);
                }
                break;
        }

        if (targetObj instanceof IIntervene) {
            IIntervene inter2 = (IIntervene) targetObj;
            //禁用延迟加载（防止延迟加载期间内部访问属性又开始加载，造成死循环）
            inter2.enableLazyLoading();
        }
    }

    /**
     * 为指定对象的当前元素设置值，适用于不具多重性的元素。
     *
     * @param targetObj 要为其元素设值的对象
     * @param value     元素的值
     */
    public void setValue(Object targetObj, Object value) {

        if (targetObj instanceof IIntervene) {
            IIntervene inter1 = (IIntervene) targetObj;
            //禁用延迟加载（防止延迟加载期间内部访问属性又开始加载，造成死循环）
            inter1.forbidLazyLoading();
        }

        //前置过滤，如果value实现了IEnumerable或IEnumerable<>，调用另一重载。
        Class<?> valueType = value.getClass();

        if (!valueType.equals(String.class) && Arrays.asList(valueType.getInterfaces()).contains(Iterable.class)) {
            Iterable<Object> iEnumerableValue = (Iterable<Object>) value;
            this.setValue(targetObj, iEnumerableValue);
        } else {
            this.valueSetter.setValue(targetObj, value);
        }

        if (targetObj instanceof IIntervene) {
            IIntervene inter2 = (IIntervene) targetObj;
            //禁用延迟加载（防止延迟加载期间内部访问属性又开始加载，造成死循环）
            inter2.enableLazyLoading();
        }
    }

    /**
     * 从指定对象取出当前元素的值
     *
     * @param targetObj 要取其元素值的对象
     * @return 当前元素的值
     */
    public Object getValue(Object targetObj) {
        //实施说明  如果是引用元素，调用取值器前要记下是否已启用延迟加载，然后禁用延迟加载，调用后恢复到原始状态。
        Object result;
        if (this instanceof ReferenceElement && targetObj instanceof IIntervene) {
            ReferenceElement re = (ReferenceElement) this;
            IIntervene inter = (IIntervene) targetObj;
            //禁用延迟加载（防止延迟加载期间内部访问属性又开始加载，造成死循环）
            inter.forbidLazyLoading();
            //获取值
            result = re.getValueGetter().getValue(targetObj);
            //启用延迟加载
            inter.enableLazyLoading();
        } else {
            //获取值
            result = this.getValueGetter().getValue(targetObj);
        }

        return result;
    }

    /**
     * 获取元素扩展
     *
     * @param extensionType 扩展类型，即派生自TypeExtension的具体类型
     * @return 返回类型扩展实例；如果指定的扩展类型不存在，返回null。
     */
    public ElementExtension getExtension(Class<?> extensionType) {
        Optional<ElementExtension> elementExtension = this.extensions.stream().filter(p -> p.getClass() == extensionType).findFirst();
        return elementExtension.orElse(null);
    }
}
