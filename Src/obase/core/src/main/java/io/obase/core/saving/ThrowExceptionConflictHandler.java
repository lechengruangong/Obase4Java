/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：发生并发冲突时引发异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 16:16:24
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.odm.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 发生并发冲突时引发异常
 */
public class ThrowExceptionConflictHandler extends ConcurrentConflictHandler implements IRepeatCreationHandler,
        IVersionConflictHandler, IUpdatingPhantomHandler {

    /**
     * 由下层抛出的异常
     */
    private final Exception innerException;

    /**
     * 用于获取属性原值的委托
     */
    private final IGetAttributeValue attributeOriginalValueGetter;

    /**
     * 创建ConcurrentConflictHandler实例
     *
     * @param model                        对象数据模型
     * @param innerException               内部异常
     * @param attributeOriginalValueGetter 用于获取属性原值的委托
     */
    public ThrowExceptionConflictHandler(ObjectDataModel model, Exception innerException, IGetAttributeValue attributeOriginalValueGetter) {
        super(model);
        this.attributeOriginalValueGetter = attributeOriginalValueGetter;
        this.innerException = innerException;
    }

    /**
     * 处理并发冲突
     *
     * @param mappingUnit  映射执行器
     * @param conflictType 并发冲突类型
     */
    @Override
    public void processConflict(MappingUnit mappingUnit, EConcurrentConflictType conflictType) {
        if (mappingUnit == null)
            return;
        Object obj = mappingUnit.getHostObject();
        if (obj == null) return;
        ObjectType objType = this.getModel().getObjectType(obj.getClass());
        RuntimeException ex = null;
        switch (conflictType) {

            case RepeatCreation:
                ex = new RepeatCreationException(obj, objType, this.innerException);
                break;
            case VersionConflict:

                //没有版本键 发生版本冲突异常不能表示实际的情况 暂且忽略
                if (objType.getVersionAttributes() == null || objType.getVersionAttributes().size() == 0)
                    return;

                List<Object> objItems = mappingUnit.getMappingObjects();
                List<ObjectKey> keys = new ArrayList<>();

                if (objItems != null && objItems.size() > 0) {
                    for (Object objItem : objItems) {
                        if (objItem == null) continue;
                        List<ObjectKeyMember> members = new ArrayList<>();
                        ObjectType itemType = this.getModel().getObjectType(objItem.getClass());
                        if (itemType == null) continue;
                        //加入键属性
                        for (String keyMenberName : itemType.getKeyFields()) {
                            TypeElement keyMemberValue = itemType.getElement(keyMenberName);
                            members.add(
                                    new ObjectKeyMember(itemType.getClrType().getName() + "-" + keyMenberName, keyMemberValue));
                        }
                        if (itemType.getVersionAttributes() != null && itemType.getVersionAttributes().size() > 0) {
                            for (String attrName : itemType.getVersionAttributes()) {
                                Attribute attr = itemType.getAttribute(attrName);
                                Object valObj = this.attributeOriginalValueGetter.getAttributeValue(objItem, attr, null);
                                ObjectKeyMember member = new ObjectKeyMember(itemType.getClrType().getName() + "-" + attrName, valObj);
                                members.add(member);
                            }
                        }

                        keys.add(new ObjectKey(itemType, members));
                    }
                }

                ex = new VersionConflictException(obj, objType, keys, this.innerException);
                break;
            case UpdatingPhantom:
                ex = new UpdatingPhantomException(obj, objType, this.innerException);
                break;
        }
        throw ex;
    }

    /**
     * 处理重复创建冲突
     *
     * @param mappingUnit 映射执行器
     */
    @Override
    public void processRepeatConflict(MappingUnit mappingUnit) {
        this.processConflict(mappingUnit, EConcurrentConflictType.RepeatCreation);
    }

    /**
     * 处理更新幻影冲突
     *
     * @param mappingUnit 映射执行器
     */
    @Override
    public void processUpdatingPhantomConflict(MappingUnit mappingUnit) {
        this.processConflict(mappingUnit, EConcurrentConflictType.UpdatingPhantom);
    }

    /**
     * 处理版本冲突
     *
     * @param mappingUnit 映射执行器
     */
    @Override
    public void processVersionConflict(MappingUnit mappingUnit) {
        this.processConflict(mappingUnit, EConcurrentConflictType.VersionConflict);
    }
}

