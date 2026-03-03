/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：异构查询按一定规则进行分解得到的片段.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 15:06:52
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.heterog;

import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.query.QueryOp;

/**
 * 一个数据结构，用于表示对异构查询按一定规则进行分解得到的片段
 */
public class HeterogQuerySegments {

    /**
     * 补充链
     */
    public QueryOp Complement;

    /**
     * 查询链中的包含运算（显式或隐式）生成的包含树（以主体链末节点的源类型为基点）。
     */
    public AssociationTree Including;

    /**
     * 主体链 对于同构查询，主体链是其自身剔除包含运算后形成的查询链。
     */
    public QueryOp MainQuery;

    /**
     * 主体链末尾的异构运算。
     */
    public QueryOp MainTail;
}
