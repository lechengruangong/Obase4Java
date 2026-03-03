/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：更改通知模块,用于发送对象更改通知.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:30:23
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.core.ObjectContext;
import io.obase.core.common.Utils;
import io.obase.core.expression.Expression;
import io.obase.core.odm.*;
import io.obase.core.saving.CompanionMapping;
import io.obase.core.saving.EObjectStatus;
import io.obase.core.saving.ObjectSystemVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 更改通知模块，用于发送对象更改通知。
 * 订阅ISavingPipeline.EndSavingUnit、IDeletingPipeline.
 * EndDeletingGroup和IDirectlyChangingPipeline.EndDirectlyChanging事件。
 * 更改通知的数据结构及生成通知的算法参见设计文档“执行映射/更改通知”章节。
 */
public class ChangeNoticeModule implements IMappingModule {

    /**
     * 数据模型
     */
    private final ObjectDataModel model;

    /**
     * 变更消息发送器
     */
    private final IChangeNoticeSender sender;

    /**
     * 更改通知模块
     *
     * @param model  数据模型
     * @param sender 变更消息发送器
     */
    public ChangeNoticeModule(ObjectDataModel model, IChangeNoticeSender sender) {
        this.model = model;
        this.sender = sender;
    }

    /**
     * 初始化更改通知模块
     *
     * @param context 上下文
     */
    public ChangeNoticeModule(ObjectContext context) {
        this.model = context.getModel();
        this.sender = Utils.getDependencyInjectionService(context.getClass(), IChangeNoticeSender.class);
    }

    /**
     * 初始化映射模块
     *
     * @param savingPipeline           "保存"管道
     * @param deletingPipeline         "删除"管道
     * @param queryPipeline            "查询"管道
     * @param directlyChangingPipeline "就地修改"管道
     * @param objectContext            对象上下文
     */
    @Override
    public void init(ISavingPipeline savingPipeline, IDeletingPipeline deletingPipeline, IQueryPipeline queryPipeline, IDirectlyChangingPipeline directlyChangingPipeline, ObjectContext objectContext) {
        //订阅事件
        if (savingPipeline.getEndSavingUnit() != null)
            savingPipeline.getEndSavingUnit().addListener(this::savingPipelineEndSavingUnit);
        if (deletingPipeline.getEndDeletingGroup() != null)
            deletingPipeline.getEndDeletingGroup().addListener(this::deletingPipelineEndDeletingGroup);
        if (directlyChangingPipeline.getEndDirectlyChanging() != null)
            directlyChangingPipeline.getEndDirectlyChanging().addListener(this::directlyChangingPipelineEndDirectlyChanging);
    }

    /**
     * 保存结束事件
     *
     * @param eventObject 事件数据
     */
    private void savingPipelineEndSavingUnit(EndSavingUnitEventArgs eventObject) {
        List<IChangeNoticeWriter> writers = new ArrayList<>();

        //判空
        if (eventObject.getMappingUnit() == null)
            return;

        //取出信息
        Object host = eventObject.getMappingUnit().getHostObject();
        if (host == null)
            return;
        ObjectType objectType = this.model.getObjectType(host.getClass());
        List<CompanionMapping> companions = eventObject.getMappingUnit().getCompanionMappings();

        //不通知创建 且保存状态为创建 返回
        if (!objectType.getNotifyCreation() && eventObject.getHostObjectStatus() == EObjectStatus.Added) return;
        //不通知更新 且保存状态为更新 返回
        if (!objectType.getNotifyUpdate() && eventObject.getHostObjectStatus() == EObjectStatus.Modified) return;

        //发送宿主通知
        writers.add(this.generateObjectChangeWriters(host, objectType, eventObject.getHostObjectStatus()));

        //发送伴随通知
        if (objectType.getNotifyUpdate()) {
            for (CompanionMapping companion : companions) {
                AssociationType companionAssociationType = this.model.getAssociationType(companion.getAssociationObj().getClass());
                int value = 0;
                if (companionAssociationType.getNotifyCreation()) value = value | EObjectStatus.Added.getStatus();
                if (companionAssociationType.getNotifyUpdate()) value = value | EObjectStatus.Modified.getStatus();
                if (companionAssociationType.getNotifyDeletion()) value = value | EObjectStatus.Deleted.getStatus();

                if ((value & companion.getStatus().getStatus()) == companion.getStatus().getStatus())
                    //加入编写器集合
                    writers.add(this.generateObjectChangeWriters(companion.getAssociationObj(), companionAssociationType,
                            companion.getStatus()));
            }
        }

        this.sendNotices(writers);
    }

    /**
     * 删除结束事件
     *
     * @param e 事件数据
     */
    private void deletingPipelineEndDeletingGroup(EndDeletingGroupEventArgs e) {
        List<IChangeNoticeWriter> writers = new ArrayList<>();

        //判空
        if (e.getObjects() == null)
            return;

        //取出信息
        Object[] objects = e.getObjects();
        //不通知删除 不发送消息
        if (!e.getObjectType().getNotifyDeletion()) return;

        for (Object obj : objects)
            //处理每个对象
            writers.add(this.generateObjectChangeWriters(obj, e.getObjectType(), EObjectStatus.Deleted));

        this.sendNotices(writers);
    }

    /**
     * 直接修改结束事件处理
     *
     * @param eventObject 事件数据
     */
    private void directlyChangingPipelineEndDirectlyChanging(EndDirectlyChangingEventArgs eventObject) {
        List<IChangeNoticeWriter> writers = new ArrayList<>();

        //获取当前对象类型
        ObjectType objectType = this.model.getObjectType(eventObject.getObjectType());
        //检查通知状态
        if (!objectType.getNotifyUpdate() && (eventObject.getChangeType() == EDirectlyChangeType.Increment ||
                eventObject.getChangeType() == EDirectlyChangeType.Update)) return;
        if (!objectType.getNotifyDeletion() && eventObject.getChangeType() == EDirectlyChangeType.Delete) return;

        //加入编写器集合
        writers.add(this.generateDirectlyChangingNoticeWriters(objectType, eventObject.getExpression(), eventObject.getChangeType(), eventObject.getNewValues()));

        this.sendNotices(writers);
    }

    /**
     * 生成对象修改通知
     *
     * @param obj          对象
     * @param objectType   对象类型
     * @param objectStatus 修改状态
     * @return 对象修改通知
     */
    private IChangeNoticeWriter generateObjectChangeWriters(Object obj, ObjectType objectType, EObjectStatus objectStatus) {
        String changeAction;
        switch (objectStatus) {
            case Unchanged:
                changeAction = "";
                break;
            case Added:
                changeAction = "Create";
                break;
            case Deleted:
                changeAction = "Delete";
                break;
            case Modified:
                changeAction = "Update";
                break;
            default:
                throw new IndexOutOfBoundsException("未知的对象修改类型");
        }

        List<ObjectAttribute> objectKeyList = new ArrayList<>();
        List<ObjectAttribute> objectAttributeList = new ArrayList<>();

        if (objectType instanceof EntityType) {
            EntityType entityType = (EntityType) objectType;

            for (Attribute attribute : entityType.getAttributes()) {
                if (entityType.getKeyAttributes() != null && entityType.getKeyAttributes().stream().anyMatch(p -> p.equals(attribute.getName()))) {
                    ObjectAttribute objectAttribute = new ObjectAttribute();
                    objectAttribute.setAttribute(attribute.getName());
                    objectAttribute.setValue(ObjectSystemVisitor.getValue(obj, objectType, attribute.getName()));

                    objectKeyList.add(objectAttribute);
                }

                this.getNoticeAttribute(obj, objectType, objectAttributeList, attribute);
            }
        } else if (objectType instanceof AssociationType) {
            AssociationType associationType = (AssociationType) objectType;

            for (Attribute attribute : associationType.getAttributes()) {
                for (AssociationEnd end : associationType.getAssociationEnds()) {
                    for (AssociationEndMapping endMapping : end.getMappings()) {
                        ObjectAttribute objectAttributeValue = new ObjectAttribute();
                        objectAttributeValue.setAttribute(endMapping.getKeyAttribute());
                        objectAttributeValue.setValue(ObjectSystemVisitor.getValue(obj, objectType, endMapping.getKeyAttribute()));
                        objectKeyList.add(objectAttributeValue);
                    }
                }

                this.getNoticeAttribute(obj, objectType, objectAttributeList, attribute);
            }
        }

        return new ObjectChangeNoticeWriter(changeAction, objectType.getNamespace(), objectType.getName(), objectKeyList,
                objectAttributeList);
    }

    /**
     * 获取要包含在通知内的属性和值
     *
     * @param obj                 对象
     * @param objectType          对象类型
     * @param objectAttributeList 通知属性列表
     * @param attribute           属性
     */
    private void getNoticeAttribute(Object obj, ObjectType objectType, List<ObjectAttribute> objectAttributeList, Attribute attribute) {
        if (objectType.getNoticeAttributes() != null && objectType.getNoticeAttributes().stream().anyMatch(p -> p.equals(attribute.getName()))) {
            ObjectAttribute objectAttribute = new ObjectAttribute();
            objectAttribute.setAttribute(attribute.getName());
            objectAttribute.setValue(ObjectSystemVisitor.getValue(obj, objectType, attribute.getName()));

            objectAttributeList.add(objectAttribute);
        }
    }

    /**
     * 生成就地修改通知
     *
     * @param objectType 对象类型
     * @param expression 修改表达式
     * @param changeType 修改类型
     * @param newValues  字段值键值对
     * @return 就地修改通知
     */
    private IChangeNoticeWriter generateDirectlyChangingNoticeWriters(ObjectType objectType, Expression expression, EDirectlyChangeType changeType, Map<String, Object> newValues) {

        HashMap<String, Object> realValues = new HashMap<>();

        if (newValues != null && newValues.size() > 0) {
            for (String key : newValues.keySet())
                realValues.put(key, newValues.get(key));
        }

        String changeAction;
        switch (changeType) {
            case Delete:
                changeAction = "Delete";
                break;
            case Update:
                changeAction = "Update";
                break;
            case Increment:
                changeAction = "Increase";
                break;
            default:
                throw new IndexOutOfBoundsException("未知的就地修改类型.");
        }

        return new DirectlyChangingNoticeWriter(changeAction, objectType.getNamespace(), objectType.getName(),
                expression.toString(), changeType, realValues);
    }

    /**
     * 发送通知
     *
     * @param writers 通知编写器
     */
    private void sendNotices(List<IChangeNoticeWriter> writers) {
        //发送
        for (IChangeNoticeWriter writer : writers)
            this.sender.send(writer.write());
    }
}
