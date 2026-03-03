/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：退化路径收集器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 14:57:45
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.heterog;

import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.objectSys.AssociationTreeNode;
import io.obase.core.query.QueryOp;
import io.obase.core.query.QueryOpVisitorWithResult;
import io.obase.core.query.SelectOp;

import java.util.ArrayList;
import java.util.List;

/**
 * 针对查询链中的退化投影运算，记录其退化路径，按顺序形成一个退化序列
 */
public class AtrophyCollector extends QueryOpVisitorWithResult<AssociationTreeNode[]> {

    /**
     * 暂存退化路径序列
     */
    private final List<AssociationTreeNode> atrophyPaths = new ArrayList<>();

    /**
     * 执行通用后置访问逻辑
     *
     * @param queryOp       要访问的查询运算
     * @param previousState 访问前一运算时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     * @return 是否继续访问
     */
    @Override
    protected boolean postVisitGenerally(QueryOp queryOp, Object previousState, Object preVisitState) {
        if (queryOp instanceof SelectOp) {
            SelectOp selectOp = (SelectOp) queryOp;
            if (selectOp.getAtrophyPath().getAssociationPath() != null)
                this.atrophyPaths.add(selectOp.getAtrophyPath().getAssociationPath());
        }

        return false;
    }

    /**
     * 执行通用前置访问逻辑
     *
     * @param queryOp          要访问的查询运算
     * @param previousState    访问前一运算时产生的状态数据
     * @param outPreviousState 返回一个状态数据，在遍历到下一运算时该数据将被视为前序状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     * @return 是否继续访问
     */
    @Override
    protected boolean preVisitGenerally(QueryOp queryOp, Object previousState, ObjectReferencePack<Object> outPreviousState, ObjectReferencePack<Object> outPreVisitState) {
        outPreviousState.realValue = outPreVisitState.realValue = null;
        return false;
    }

    /**
     * 获取访问操作的结果
     *
     * @return 获取访问操作的结果
     */
    @Override
    public AssociationTreeNode[] getResult() {
        return this.atrophyPaths.toArray(new AssociationTreeNode[0]);
    }
}
