/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：定义配置属性的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:29:00
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.common.Property;
import io.obase.core.odm.IAttributeCombinationHandler;
import io.obase.core.odm.IBehaviorTrigger;

import java.lang.reflect.Method;

/**
 * 定义配置属性的规范
 */
public interface IAttributeConfigurator extends ITypeElementConfigurator {

    /**
     * 设置修改触发器(覆盖现有配置)
     *
     * @param changeTrigger 修改触发器
     */
    void hasChangeTriggerI(IBehaviorTrigger changeTrigger);

    /**
     * 设置修改触发器
     *
     * @param changeTrigger 修改触发器
     * @param override      是否覆盖既有配置
     */
    void hasChangeTriggerI(IBehaviorTrigger changeTrigger, boolean override);

    /**
     * 使用一个能触发属性修改的方法为属性创建修改触发器(覆盖现有配置)
     *
     * @param method 触发属性修改的方法
     */
    void hasChangeTriggerI(Method method);

    /**
     * 使用一个能触发属性修改的方法为属性创建修改触发器
     *
     * @param method   触发属性修改的方法
     * @param override 是否覆盖既有配置
     */
    void hasChangeTriggerI(Method method, boolean override);

    /**
     * 使用一个能触发属性修改的属性访问器为属性创建Property-Set型修改触发器(覆盖现有配置)
     *
     * @param property 触发属性修改的属性访问器
     */
    void hasChangeTriggerI(Property property);

    /**
     * 使用一个能触发属性修改的属性访问器为属性创建Property-Set型修改触发器
     *
     * @param property 触发属性修改的属性访问器
     * @param override 是否覆盖既有配置
     */
    void hasChangeTriggerI(Property property, boolean override);

    /**
     * 使用一个能触发属性修改的属性访问器为属性创建修改触发器(覆盖现有配置)
     *
     * @param property    触发属性修改的属性访问器
     * @param triggerType 要创建的触发器类型
     */
    void hasChangeTriggerI(Property property, EBehaviorTriggerType triggerType);

    /**
     * 使用一个能触发属性修改的属性访问器为属性创建修改触发器
     *
     * @param property    触发属性修改的属性访问器
     * @param triggerType 要创建的触发器类型
     * @param override    是否覆盖既有配置
     */
    void hasChangeTriggerI(Property property, EBehaviorTriggerType triggerType, boolean override);

    /**
     * 使用一个能触发属性修改的成员为属性创建修改触发器(覆盖现有配置)
     *
     * @param memberName  成员的名称
     * @param triggerType 要创建的触发器类型
     */
    void hasChangeTriggerI(String memberName, EBehaviorTriggerType triggerType);

    /**
     * 使用一个能触发属性修改的成员为属性创建修改触发器
     *
     * @param memberName  成员的名称
     * @param triggerType 要创建的触发器类型
     * @param override    是否覆盖既有配置
     */
    void hasChangeTriggerI(String memberName, EBehaviorTriggerType triggerType, boolean override);

    /**
     * 使用与属性同名的成员为属性创建修改触发器(覆盖现有配置)
     *
     * @param triggerType 要创建的触发器类型
     */
    void hasChangeTriggerI(EBehaviorTriggerType triggerType);

    /**
     * 使用一个能触发属性修改的成员为属性创建修改触发器
     *
     * @param triggerType 要创建的触发器类型
     * @param override    是否覆盖既有配置
     */
    void hasChangeTriggerI(EBehaviorTriggerType triggerType, boolean override);

    /**
     * 使用与属性同名的属性访问器为属性创建Property-Set型修改触发器(覆盖现有配置)
     */
    void hasChangeTriggerI();

    /**
     * 使用与属性同名的属性访问器为属性创建Property-Set型修改触发器
     *
     * @param override 是否覆盖既有配置
     */
    void hasChangeTriggerI(boolean override);

    /**
     * 设置属性的合并处理器(覆盖现有配置)
     *
     * @param combiner 属性的合并处理器
     */
    void hasCombinationHandlerI(IAttributeCombinationHandler combiner);

    /**
     * 设置属性的合并处理器
     *
     * @param combiner 属性的合并处理器
     * @param override 是否覆盖既有配置
     */
    void hasCombinationHandlerI(IAttributeCombinationHandler combiner, boolean override);

    /**
     * 设置与指定的属性合并处理策略对应的合并处理器(覆盖现有配置)
     *
     * @param strategy 属性的合并处理策略
     */
    void hasCombinationHandlerI(EAttributeCombinationHandlingStrategy strategy);

    /**
     * 设置与指定的属性合并处理策略对应的合并处理器
     *
     * @param strategy 属性的合并处理策略
     * @param override 是否覆盖既有配置
     */
    void hasCombinationHandlerI(EAttributeCombinationHandlingStrategy strategy, boolean override);

    /**
     * 设置映射连接符(覆盖现有配置)
     *
     * @param value 映射连接符
     */
    void hasMappingConnectionCharI(char value);

    /**
     * 设置映射连接符
     *
     * @param value    映射连接符
     * @param override 是否覆盖既有配置
     */
    void hasMappingConnectionCharI(char value, boolean override);

    /**
     * 设置映射字段(覆盖现有配置)
     *
     * @param field 映射字段
     */
    void toFieldI(String field);

    /**
     * 设置映射字段
     *
     * @param field    映射字段
     * @param override 是否覆盖既有配置
     */
    void toFieldI(String field, boolean override);

    /**
     * 设置最大字符数
     * 仅属性数据类型为String时有效，否则抛出IllegalArgumentException；传入值会乘以8后作为属性的值长度（以位为单位）。
     * 未配置时最大字符数为0（即未设置），此时属性的值长度为0，由SQL结构映射生成字段时按40个字符处理，即数据库字段的默认长度为40
     *
     * @param maxCharNumber 最大字符数，不能为负值，否则抛出IllegalArgumentException
     */
    void hasMaxCharNumberI(int maxCharNumber);

    /**
     * 设置最大字符数
     * 仅属性数据类型为String时有效，否则抛出IllegalArgumentException；传入值会乘以8后作为属性的值长度（以位为单位）。
     * 未配置时最大字符数为0（即未设置），此时属性的值长度为0，由SQL结构映射生成字段时按40个字符处理，即数据库字段的默认长度为40
     * override为false时，仅在当前最大字符数仍为0（未设置）时才生效
     *
     * @param maxCharNumber 最大字符数，不能为负值，否则抛出IllegalArgumentException
     * @param override      是否覆盖既有配置
     */
    void hasMaxCharNumberI(int maxCharNumber, boolean override);

    /**
     * 设置精度
     * 只支持为映射类型decimal设置精度
     *
     * @param precision 以小数位数表示的精度，0表示小数点后没有位数。精度最大值28
     */
    void hasPrecisionI(byte precision);

    /**
     * 设置精度
     * 只支持为映射类型decimal设置精度
     *
     * @param precision 以小数位数表示的精度，0表示小数点后没有位数。精度最大值28
     * @param override  是否覆盖既有配置
     */
    void hasPrecisionI(byte precision, boolean override);

    /**
     * 设置是否可空
     *
     * @param value 指示是否可空。对于主键设置为可空是无效的
     */
    void hasNullableI(boolean value);

    /**
     * 设置是否可空
     *
     * @param value    指示是否可空。对于主键设置为可空是无效的
     * @param override 是否覆盖既有配置
     */
    void hasNullableI(boolean value, boolean override);
}
