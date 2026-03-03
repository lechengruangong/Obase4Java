/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：逻辑删除映射模块.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 11:12:24
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.logical.deletion;

import io.obase.core.ObjectContext;
import io.obase.core.expression.*;
import io.obase.core.mapping.pipeline.*;
import io.obase.core.odm.StructuralType;
import io.obase.core.query.QueryOp;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

/**
 * 逻辑删除映射模块
 */
public class LogicDeletionModule implements IMappingModule {
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
        queryPipeline.getBeginQuery().addListener(eventObject -> {
            QueryOp queryOp = eventObject.getContext().getQuery();
            if (queryOp.getSourceModelType() instanceof StructuralType) {
                StructuralType structuralType = (StructuralType) queryOp.getSourceModelType();
                LogicDeletionExtension ext = (LogicDeletionExtension) structuralType.getExtension(LogicDeletionExtension.class);
                if (ext != null) {
                    //如果不是标记的 就是Obase生成的
                    Method member;
                    try {
                        member = (ext.getDeletionMark() == null || ext.getDeletionMark().isEmpty()) ?
                                structuralType.getRebuildingType().getMethod("getObase_gen_deletionMark") :
                                structuralType.getRebuildingType().getMethod("get" + StringUtils.capitalize(ext.getDeletionMark()));
                    } catch (NoSuchMethodException e) {
                        throw new IllegalArgumentException("获取逻辑删除字段失败", e);
                    }

                    ParameterExpression parameterExp = Expression.parameter("o", structuralType.getRebuildingType());
                    //构造一个形如 逻辑删除字段==false 的表达式
                    MemberExpression left = Expression.member(parameterExp, member, parameterExp, parameterExp.getType());
                    ConstantExpression right = Expression.constant(false);
                    BinaryExpression segment = Expression.equal(left, right, null);
                    QueryOp logicDelete = QueryOp.where(Expression.lambda(new ParameterExpression[]{parameterExp}, segment), queryOp.getModel(),
                            queryOp);
                    eventObject.getContext().setQuery(logicDelete);
                }
            }
        });
    }
}
