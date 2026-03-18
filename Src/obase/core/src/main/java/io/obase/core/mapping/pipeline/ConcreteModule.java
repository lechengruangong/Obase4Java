/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：类型判别模块.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 17:04:48
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.common.TwoTuple;
import io.obase.core.ObjectContext;
import io.obase.core.common.Utils;
import io.obase.core.expression.*;
import io.obase.core.odm.StructuralType;
import io.obase.core.query.QueryOp;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 类型判别模块
 */
public class ConcreteModule implements IMappingModule {
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
    }

    /**
     * 订阅事件
     *
     * @param eventObject 事件数据
     */
    private void queryPipelineOnBeginQuery(QueryEventArgs eventObject) {
        QueryOp queryOp = eventObject.getContext().getQuery();
        if (queryOp.getSourceModelType() instanceof StructuralType) {
            StructuralType structuralType = (StructuralType) queryOp.getSourceModelType();
            TwoTuple<String, Object> sign = structuralType.getConcreteTypeSign();
            if (sign != null && structuralType.getDerivingFrom() != null) {
                Method member;
                try {
                    //如果不是标记的 就是Obase生成的
                    member = structuralType.getRebuildingType().getMethod("get" + sign.getItem1());
                } catch (NoSuchMethodException ex) {
                    try {
                        //找不到由用户定义的 就找自己补充的
                        member = structuralType.getRebuildingType().getMethod("getObase_gen_ct");
                    } catch (NoSuchMethodException ex1) {
                        member = null;
                    }
                }

                if (member != null) {
                    ParameterExpression parameterExp = Expression.parameter("o", structuralType.getRebuildingType());
                    //载入全局就组两个 否则一个
                    List<Object> sourceObjs = Utils.getDerivingConcreteTypeValue(structuralType);
                    Expression segments = null;
                    for (Object obj : sourceObjs) {

                        //构造一个形如 引用键==参考键.值的表达式
                        MemberExpression left = Expression.member(parameterExp, member, parameterExp, parameterExp.getType());
                        ConstantExpression right = Expression.constant(obj);
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


}
