/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：数据行分派器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 11:19:53
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.ObjectKey;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.IAssociationTreeDownwardVisitor;

/**
 * 数据行分派器
 */
public class DataRowAssigner implements IAssociationTreeDownwardVisitor {

    /**
     * DataRowAssignment存储
     */
    private final DataRowAssignmentSet dataRowAssignmentSet;

    /**
     * 被分派的数据行
     */
    private DataRow dataRow;

    /**
     * 创建DataRowAssigner实例
     *
     * @param dataRowAssignmentSet 用于存储DataRowAssignment实例的容器
     */
    public DataRowAssigner(DataRowAssignmentSet dataRowAssignmentSet) {
        this.dataRowAssignmentSet = dataRowAssignmentSet;
    }

    /**
     * 前置访问，即在访问子级前执行操作。
     *
     * @param subTree          被访问的关联树子树
     * @param parentState      访问父级时产生的状态数据
     * @param outParentState   返回一个状态数据，在遍历到子级时该数据将被视为父级状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     * @return 是否继续访问
     */
    @Override
    public boolean preVisit(AssociationTree subTree, Object parentState, ObjectReferencePack<Object> outParentState, ObjectReferencePack<Object> outPreVisitState) {
        if (this.dataRow != null) {
            //获取键
            ObjectKey objectKey = this.dataRow.getObjectKey(subTree.getNode());

            if (objectKey != null)
                //不存在等效行 则加入
                if (!this.dataRowAssignmentSet.containEquivalent(subTree.getNode(), this.dataRow))
                    this.dataRowAssignmentSet.add(subTree.getNode(), this.dataRow);
        }


        outParentState.realValue = null;
        outPreVisitState.realValue = null;

        return true;
    }

    /**
     * 后置访问，即在访问子级后执行操作
     *
     * @param subTree       被访问的关联树子树
     * @param parentState   访问父级时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     */
    @Override
    public void postVisit(AssociationTree subTree, Object parentState, Object preVisitState) {
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
     * 设置待分派的数据行
     *
     * @param dataRow 待分派数据行
     */
    public void setDataRow(DataRow dataRow) {
        this.dataRow = dataRow;
    }
}
