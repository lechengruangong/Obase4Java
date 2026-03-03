/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：映射单元.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:10:57
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.common.ActionWithOneArg;
import io.obase.common.FunctionWithTwoArgs;
import io.obase.common.ObjectReferencePack;
import io.obase.core.FilterSegment;
import io.obase.core.IMappingWorkflow;
import io.obase.core.MappingFilter;
import io.obase.core.mapping.pipeline.PostExecuteCommandEventArgs;
import io.obase.core.mapping.pipeline.PreExecuteCommandEventArgs;
import io.obase.core.odm.*;

import java.util.*;

/**
 * 映射单元。
 * 映射单元由一个或多个对象组成，这些对象将映射到同一个表，它们的操作不可拆分，应由同一条Sql语句完成。
 */
public class MappingUnit {

    /**
     * 伴随端队列
     */
    private Queue<CompanionMapping> companionMappings;

    /**
     * 主体对象
     */
    private Object hostObject;

    /**
     * 当前映射单元参照的对象集合
     */
    private List<Object> referredObjects;

    /**
     * 获取伴随映射的对象及其状态
     *
     * @return 伴随映射的对象及其状态
     */
    public List<CompanionMapping> getCompanionMappings() {
        if (this.companionMappings == null)
            this.companionMappings = new ArrayDeque<>();
        return new ArrayList<>(this.companionMappings);
    }

    /**
     * 获取映射单元的主体对象
     *
     * @return 映射单元的主体对象
     */
    public Object getHostObject() {
        return this.hostObject;
    }

    /**
     * 获取当前映射单元参照的对象集合
     *
     * @return 当前映射单元参照的对象集合
     */
    public List<Object> getReferredObjects() {
        if (this.referredObjects == null)
            this.referredObjects = new ArrayList<>();
        return this.referredObjects;
    }

    /**
     * 获取参与映射的对象，包含主体对象和伴随映射对象
     *
     * @return 参与映射的对象，包含主体对象和伴随映射对象
     */
    public List<Object> getMappingObjects() {
        List<Object> list = new ArrayList<>();
        if (this.hostObject != null)
            list.add(this.hostObject);
        list.addAll(this.getCompanionMappings());
        return list;
    }

    /**
     * 向映射单元添加伴随关联对象，并指定其状态和被其参照的对象。
     *
     * @param companion    要添加的伴随关联对象
     * @param status       伴随关联对象的状态
     * @param referredObjs 关联对象参照的对象的集合
     */
    public void addCompanion(Object companion, EObjectStatus status, Object[] referredObjs) {
        this.getReferredObjects().addAll(Arrays.asList(referredObjs));
        this.addCompanion(companion, status);
    }

    /**
     * 向映射单元添加伴随关联对象，并指定其状态。
     *
     * @param companion 要添加的伴随关联对象
     * @param status    伴随关联的状态
     */
    public void addCompanion(Object companion, EObjectStatus status) {
        CompanionMapping companionMapping = new CompanionMapping(companion, status);
        if (this.companionMappings == null)
            this.companionMappings = new ArrayDeque<>();
        this.companionMappings.add(companionMapping);
    }

    /**
     * 向映射单元添加主体对象。注：只有实体对象和独立关联对象才能作为主体对象。
     *
     * @param hostObj 主体对象
     */
    public void addHost(Object hostObj) {
        this.hostObject = hostObj;
    }

    /**
     * 向映射单元添加关联对象，该关联对象将作为映射单元的主体对象。注：只有当关联对象为独立映射时才可作为主体对象。
     *
     * @param associationObj 要添加的关联对象
     * @param referredObjs   关联对象参照的对象的集合
     */
    public void addHost(Object associationObj, Object[] referredObjs) {
        this.hostObject = associationObj;
        this.getReferredObjects().addAll(Arrays.asList(referredObjs));
    }

    /**
     * 将映射单元中的对象转换为特定的存储数据结构
     *
     * @param mappingWorkflow     映射工作流机制
     * @param status              映射单元的状态，即该单元中主对象的状态，不考虑伴随对象的状态
     * @param model               对象数据模型
     * @param attributeHasChanged 一个委托，用于检查对象的属性是否已修改。三个类型参数分别对应于要检查的对象、要检查的属性和检查结果
     */
    private void mapObjects(IMappingWorkflow mappingWorkflow, EObjectStatus status, ObjectDataModel model,
                            FunctionWithTwoArgs<Object, String, Boolean> attributeHasChanged) {
        ObjectMapper objectMapper = new ObjectMapper(mappingWorkflow);

        boolean flag = false;

        //处理宿主类型
        if (this.hostObject != null) {
            ObjectType objType = model.getObjectType(this.hostObject.getClass());
            objectMapper.generateSource(objType);
            objectMapper.determineChangeType(status, objType);
            if (status != EObjectStatus.Added) objectMapper.generateCriteria(this.hostObject, objType);
            objectMapper.generateFieldSetter(this.hostObject, objType, status, s -> attributeHasChanged != null && attributeHasChanged.invoke(this.hostObject, s));
            flag = true;
        }

        //处理伴随映射
        if (this.companionMappings != null && this.companionMappings.size() > 0)
            for (CompanionMapping cm : this.companionMappings) {
                Object associationObj = cm.getAssociationObj();
                AssociationType associationType = model.getAssociationType(associationObj.getClass());
                if (!flag) {
                    objectMapper.generateSource(associationType);
                    objectMapper.determineChangeType(cm.getStatus(), associationType);
                    objectMapper.generateCriteria(associationObj, associationType);
                }

                objectMapper.generateFieldSetter(associationObj, associationType, cm.getStatus(), s -> attributeHasChanged != null && attributeHasChanged.invoke(this.hostObject, s));
            }
    }

    /**
     * 在映射过程中校验对象版本
     *
     * @param mappingWorkflow              映射工作流机制
     * @param model                        对象数据模型
     * @param attributeOriginalValueGetter 用于获取属性原始值的委托
     */
    private void checkVersion(IMappingWorkflow mappingWorkflow, ObjectDataModel model,
                              IGetAttributeValue attributeOriginalValueGetter) {
        MappingFilter filter = mappingWorkflow.and();
        //处理每个对象
        List<Object> mappingObjs = this.getMappingObjects();

        for (Object mappingObj : mappingObjs) {
            if (mappingObj == null) continue;
            ObjectType objType = mappingObj instanceof CompanionMapping
                    ? model.getObjectType(((CompanionMapping) mappingObj).getAssociationObj().getClass())
                    : model.getObjectType(mappingObj.getClass());

            List<String> attrNames = objType.getVersionAttributes();
            if (attrNames != null && attrNames.size() > 0) {
                for (String attrName : attrNames) {
                    Attribute attr = objType.getAttribute(attrName);
                    //取原始值
                    if (attributeOriginalValueGetter != null) {
                        Object value = attributeOriginalValueGetter.getAttributeValue(mappingObj, attr, null);
                        FilterSegment segment = filter.addSegment();
                        segment.setField(attrName);
                        segment.setReferenceValue(value);
                    }
                }
            }
        }

        filter.end();
    }

    /**
     * 保存新对象
     *
     * @param mappingWorkflow       映射工作流机制
     * @param model                 对象数据模型
     * @param preExecutionCallback  执行前回调委托
     * @param postExecutionCallback 执行后回调委托
     */
    public void saveNew(IMappingWorkflow mappingWorkflow, ObjectDataModel model,
                        ActionWithOneArg<PreExecuteCommandEventArgs> preExecutionCallback,
                        ActionWithOneArg<PostExecuteCommandEventArgs> postExecutionCallback) {
        mappingWorkflow.begin();
        this.mapObjects(mappingWorkflow, EObjectStatus.Added, model, null);
        EntityType mappingEntityType = model.getEntityType(this.hostObject.getClass());

        //标识自增
        if (mappingEntityType != null && mappingEntityType.getKeyIsSelfIncreased()) {
            ObjectReferencePack<Object> identity = new ObjectReferencePack<>();
            mappingWorkflow.commit(preExecutionCallback, postExecutionCallback, identity);
            ObjectSystemVisitor.setValue(this.hostObject, mappingEntityType, mappingEntityType.getKeyAttributes().get(0), identity.realValue);
        } else {
            mappingWorkflow.commit(preExecutionCallback, postExecutionCallback);
        }
    }

    /**
     * 保存旧对象
     *
     * @param mappingWorkflow              映射工作流机制
     * @param checkVersion                 指示是否进行版本校验
     * @param model                        对象数据模型
     * @param attributeHasChanged          一个委托，用于检查对象的属性是否已修改。三个类型参数分别对应于要检查的对象、要检查的属性和检查结果。
     * @param preExecutionCallback         执行前回调委托
     * @param postExecutionCallback        执行后回调委托
     * @param attributeOriginalValueGetter 用于获取属性原值的委托
     */
    public void saveOld(IMappingWorkflow mappingWorkflow, boolean checkVersion, ObjectDataModel model,
                        FunctionWithTwoArgs<Object, String, Boolean> attributeHasChanged,
                        ActionWithOneArg<PreExecuteCommandEventArgs> preExecutionCallback,
                        ActionWithOneArg<PostExecuteCommandEventArgs> postExecutionCallback,
                        IGetAttributeValue attributeOriginalValueGetter) {
        mappingWorkflow.begin();

        this.mapObjects(mappingWorkflow, EObjectStatus.Modified, model, attributeHasChanged);
        if (checkVersion)
            this.checkVersion(mappingWorkflow, model, attributeOriginalValueGetter);

        mappingWorkflow.commit(preExecutionCallback, postExecutionCallback);
    }
}
