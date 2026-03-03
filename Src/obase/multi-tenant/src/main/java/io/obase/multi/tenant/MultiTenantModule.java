/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：多租户映射模块.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 11:46:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.multi.tenant;

import io.obase.core.ObjectContext;
import io.obase.core.expression.*;
import io.obase.core.mapping.pipeline.*;
import io.obase.core.odm.Attribute;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.StructuralType;
import io.obase.core.query.QueryOp;
import io.obase.core.saving.MappingUnit;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

/**
 * 多租户映射模块
 */
public class MultiTenantModule implements IMappingModule {

    /**
     * 模型
     */
    private ObjectDataModel model;

    /**
     * 宿主上下文类型
     */
    private Class<?> hostContextType;

    /**
     * 构造多租户映射模块
     */
    public MultiTenantModule() {
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
        queryPipeline.getBeginQuery().addListener(this::queryPipelineOnBeginQuery);
        savingPipeline.getBeginSavingUnit().addListener(this::savingPipelineOnBeginSavingUnit);
        this.model = objectContext.getModel();
        this.hostContextType = objectContext.getClass();
    }

    /**
     * 订阅事件
     *
     * @param eventObject 开始保存事件数据
     */
    private void savingPipelineOnBeginSavingUnit(BeginSavingUnitEventArgs eventObject) {
        MappingUnit unit = eventObject.getMappingUnit();
        if (unit.getHostObject() != null)
            this.setMultiTenantValue(unit.getHostObject());

        if (unit.getMappingObjects() != null) {
            for (Object obj : unit.getMappingObjects()) {
                if (obj != null)
                    this.setMultiTenantValue(obj);
            }
        }
    }

    /**
     * 设置多租户的值
     *
     * @param obj 对象
     */
    private void setMultiTenantValue(Object obj) {
        StructuralType structuralType = this.model.getStructuralType(obj.getClass());
        if (structuralType != null) {
            MultiTenantExtension ext = (MultiTenantExtension) structuralType.getExtension(MultiTenantExtension.class);
            if (ext != null) {
                String attrName = (ext.getTenantIdMark() == null || ext.getTenantIdMark().isEmpty()) ? "Obase_gen_tenantIdMark" : ext.getTenantIdMark();
                Attribute attr = structuralType.getAttribute(attrName);

                Object value = MultiTenantExtensions.getTenantId(this.hostContextType);

                attr.getValueSetter().setValue(obj, value);
            }
        }
    }

    /**
     * 订阅事件
     *
     * @param eventObject 查询对象数据
     */
    private void queryPipelineOnBeginQuery(QueryEventArgs eventObject) {
        QueryOp queryOp = eventObject.getContext().getQuery();
        if (queryOp.getSourceModelType() instanceof StructuralType) {
            StructuralType structuralType = (StructuralType) queryOp.getSourceModelType();
            MultiTenantExtension ext = (MultiTenantExtension) structuralType.getExtension(MultiTenantExtension.class);
            if (ext != null) {
                //如果不是标记的 就是Obase生成的
                Method member;
                try {
                    member = (ext.getTenantIdMark() == null || ext.getTenantIdMark().isEmpty()) ?
                            structuralType.getRebuildingType().getMethod("getObase_gen_tenantIdMark") :
                            structuralType.getRebuildingType().getMethod("get" + StringUtils.capitalize(ext.getTenantIdMark()));
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException("获取多租户字段失败", e);
                }

                ParameterExpression parameterExp = Expression.parameter("o", structuralType.getRebuildingType());

                //取出租户ID
                Object tenantId = MultiTenantExtensions.getTenantId(this.hostContextType);
                //载入全局就组两个 否则一个
                Object[] sourceObjs = ext.getLoadingGlobal() ? new Object[]{tenantId, ext.getGlobalTenantId()} : new Object[]{tenantId};

                Expression segments = null;
                for (Object obj : sourceObjs) {

                    //构造一个形如 引用键==参考键.值的表达式
                    ConstantExpression right = Expression.constant(obj);
                    MemberExpression left = Expression.member(parameterExp, member, parameterExp, parameterExp.getType());
                    BinaryExpression segment = Expression.equal(left, right, null);
                    segments = segments == null ? segment : Expression.or(segment, segments, null);

                }
                LambdaExpression lambdaExpression = Expression.lambda(new ParameterExpression[]{parameterExp}, segments);
                QueryOp logicDelete = QueryOp.where(lambdaExpression, queryOp.getModel(),
                        queryOp);
                eventObject.getContext().setQuery(logicDelete);
            }
        }
    }
}

