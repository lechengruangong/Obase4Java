/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Count 字段生成器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 15:04:50
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.providers.sql.rop;

import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.objectSys.AttributeTree;
import io.obase.core.odm.objectSys.IAttributeTreeDownwardVisitorWithResult;
import io.obase.providers.sql.sqlobject.Field;
import io.obase.providers.sql.sqlobject.FieldExpression;
import io.obase.providers.sql.sqlobject.MonomerSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Count 字段生成器
 */
public class CountingFieldGenerator implements IAttributeTreeDownwardVisitorWithResult<FieldExpression[]> {

    /**
     * 作为生成结果的字段表达式
     */
    private final List<FieldExpression> fieldExpressions = new ArrayList<>();

    /**
     * 字段所属的源
     */
    private final MonomerSource source;

    /**
     * 映射字段生成器
     */
    private final TargetFieldGenerator targetFieldGenerator = new TargetFieldGenerator();

    /**
     * 构造Count 字段生成器
     *
     * @param source 字段所属的源
     */
    public CountingFieldGenerator(MonomerSource source) {
        this.source = source;
    }

    /**
     * 前置访问，即在访问子级前执行操作
     *
     * @param subTree          被访问的子树
     * @param parentState      访问父级时产生的状态数据
     * @param outParentState   返回一个状态数据，在遍历到子级时该数据将被视为父级状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     */
    @Override
    public void preVisit(AttributeTree subTree, Object parentState, ObjectReferencePack<Object> outParentState, ObjectReferencePack<Object> outPreVisitState) {
        outParentState.realValue = null;
        outPreVisitState.realValue = null;


        if (subTree.getIsComplex())
            return;
        //加入字段
        String targetFiled = subTree.accept(this.targetFieldGenerator);
        this.fieldExpressions.add(new FieldExpression(new Field(this.source, targetFiled)));
    }

    /**
     * 后置访问，即在访问子级后执行操作
     *
     * @param subTree       被访问的子树
     * @param parentState   访问父级时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     */
    @Override
    public void postVisit(AttributeTree subTree, Object parentState, Object preVisitState) {
        //Nothing to do
    }

    /**
     * 重置访问者
     */
    @Override
    public void reset() {
        //Nothing to do
    }

    /**
     * 获取遍历属性树的结果
     *
     * @return 获取遍历属性树的结果
     */
    @Override
    public FieldExpression[] getResult() {
        return this.fieldExpressions.toArray(new FieldExpression[0]);
    }
}
