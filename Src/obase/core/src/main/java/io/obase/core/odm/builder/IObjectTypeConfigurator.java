/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：定义配置对象类型的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-6-23 17:02:49
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.odm.EConcurrentConflictHandlingStrategy;
import io.obase.core.odm.IBehaviorTrigger;

import java.util.List;

/**
 * 定义配置对象类型的规范
 */
public interface IObjectTypeConfigurator extends IStructuralTypeConfigurator {

    /**
     * 获取类型各元素上设置的行为触发器，注：相同的触发器只返回一个实例。
     *
     * @return 行为触发器
     */
    List<IBehaviorTrigger> getBehaviorTriggersI();

    /**
     * 获取映射表
     *
     * @return 映射表
     */
    String getTargetTableI();

    /**
     * 获取行为触发器触发的对象行为所涉及到的元素
     *
     * @param trigger 指定的触发器实例
     * @return 触发的对象行为所涉及到的元素
     */
    ITypeElementConfigurator[] getBehaviorElementsI(IBehaviorTrigger trigger);

    /**
     * 设置并发冲突处理策略(覆盖现有配置)
     *
     * @param strategy 冲突处理策略
     */
    void hasConcurrentConflictHandlingStrategyI(EConcurrentConflictHandlingStrategy strategy);

    /**
     * 设置并发冲突处理策略
     *
     * @param strategy 冲突处理策略
     * @param override 是否覆盖既有配置
     */
    void hasConcurrentConflictHandlingStrategyI(EConcurrentConflictHandlingStrategy strategy, boolean override);

    /**
     * 设置要包含在对象变更通知中的属性(覆盖现有配置)
     *
     * @param noticeAttributes 要包含的属性的名称的集合
     */
    void hasNoticeAttributesI(String[] noticeAttributes);

    /**
     * 设置要包含在对象变更通知中的属性
     *
     * @param noticeAttributes 要包含的属性的名称的集合
     * @param override         是否覆盖既有配置
     */
    void hasNoticeAttributesI(String[] noticeAttributes, boolean override);

    /**
     * 设置一个值，该值指示对象创建时是否发送通知(覆盖现有配置)
     *
     * @param notifyCreation 指示是否发送对象创建通知
     */
    void hasNotifyCreationI(boolean notifyCreation);

    /**
     * 设置一个值，该值指示对象创建时是否发送通知
     *
     * @param notifyCreation 指示是否发送对象创建通知
     * @param override       是否覆盖既有配置
     */
    void hasNotifyCreationI(boolean notifyCreation, boolean override);

    /**
     * 设置一个值，该值指示对象删除时是否发送通知(覆盖现有配置)
     *
     * @param notifyDeletion 指示是否发送对象删除通知
     */
    void hasNotifyDeletionI(boolean notifyDeletion);

    /**
     * 设置一个值，该值指示对象删除时是否发送通知
     *
     * @param notifyDeletion 指示是否发送对象删除通知
     * @param override       是否覆盖既有配置
     */
    void hasNotifyDeletionI(boolean notifyDeletion, boolean override);

    /**
     * 设置一个值，该值指示对象更新时是否发送通知(覆盖现有配置)
     *
     * @param notifyUpdate 指示是否发送对象更新通知
     */
    void hasNotifyUpdateI(boolean notifyUpdate);

    /**
     * 设置一个值，该值指示对象更新时是否发送通知
     *
     * @param notifyUpdate 指示是否发送对象更新通知
     * @param override     是否覆盖既有配置
     */
    void hasNotifyUpdateI(boolean notifyUpdate, boolean override);

    /**
     * 设置版本标识属性集（版本键）。每调用一次本方法将追加一个版本标识属性。(覆盖现有配置)
     *
     * @param attribute 属性的名称
     */
    void hasVersionAttributeI(String attribute);

    /**
     * 设置版本标识属性集（版本键）。每调用一次本方法将追加一个版本标识属性。
     *
     * @param attribute 属性的名称
     * @param override  是否覆盖既有配置
     */
    void hasVersionAttributeI(String attribute, boolean override);

    /**
     * 设置映射表(覆盖现有配置)
     *
     * @param table 映射表的名称
     */
    void toTableI(String table);

    /**
     * 设置映射表
     *
     * @param table    映射表的名称
     * @param override 是否覆盖既有配置
     */
    void toTableI(String table, boolean override);
}
