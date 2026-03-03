/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：节点别名生成器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-8 15:40:03
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Utils;
import io.obase.core.odm.ReferenceElement;
import io.obase.core.odm.typeviews.SelfReference;

import java.util.HashMap;
import java.util.Map;

/**
 * 节点别名生成器
 */
public class AssociationTreeNodeAliasGenerator implements IAssociationTreeUpwardVisitorWithResult<String> {

    /**
     * 用于缓存别名的字典，其键为节点，值为别名。
     */
    private final Map<AssociationTreeNode, String> aliasCache = new HashMap<>();

    /**
     * 指示是否启用缓存
     */
    private boolean enableCache = true;

    /**
     * 生成的别名
     */
    private String nodeAlias;

    /**
     * 基于关联树的某一节点，生成指定指定元素指向的子节点的别名。
     * 实施说明：
     * （1）根据别名协定生成；
     * （2）baseNodeAlias为空时推定基节点为根节点。
     *
     * @param element       指向子节点的元素
     * @param baseNodeAlias 基节点别名
     * @return 别名
     */
    public static String generateAlias(ReferenceElement element, String baseNodeAlias) {
        //放入基节点
        StringBuilder stringBuilder = baseNodeAlias == null ? new StringBuilder() : new StringBuilder(baseNodeAlias);

        //反身节点 无别名
        if (element instanceof SelfReference) return null;
        //其他的_element.Name
        stringBuilder.append("_").append(element.getName());

        return stringBuilder.toString();
    }

    /**
     * 获取一个值，该值指示是否启用缓存。
     *
     * @return 是否启用缓存
     */
    public boolean getEnableCache() {
        return this.enableCache;
    }

    /**
     * 设置一个值，该值指示是否启用缓存。
     *
     * @param enableCache 是否启用缓存
     */
    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    /**
     * 获取遍历关联树的结果
     *
     * @return 遍历操作返回结果的类型
     */
    @Override
    public String getResult() {
        return this.nodeAlias;
    }

    /**
     * 后置访问，即在访问父级后执行操作
     *
     * @param subTree       被访问的子树
     * @param childState    访问子级时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     */
    @Override
    public void postVisit(AssociationTree subTree, Object childState, Object preVisitState) {
        //如果前置数据是true
        if (preVisitState instanceof Boolean) {
            Boolean revisitBool = (Boolean) preVisitState;
            if (revisitBool)
                return;
        }
        if (!(Utils.getStringIsEmpty(subTree.getElementName())))
            //生成别名
            this.nodeAlias = this.nodeAlias + "_" + subTree.getElementName();
        //是否缓存
        if (!this.enableCache) return;

        this.aliasCache.put(subTree.getNode(), this.nodeAlias);
    }

    /**
     * 前置访问，即在访问父级前执行操作
     *
     * @param subTree          被访问的子树
     * @param childState       访问子级时产生的状态数据
     * @param outChildState    返回一个状态数据，在遍历到父级时该数据将被视为子级状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     * @return 是否继续访问父级
     */
    @Override
    public boolean preVisit(AssociationTree subTree, Object childState, ObjectReferencePack<Object> outChildState, ObjectReferencePack<Object> outPreVisitState) {
        //未启用缓存 继续向上递归
        if (!this.enableCache) {
            outChildState.realValue = null;
            outPreVisitState.realValue = null;
            return true;
        }

        if (subTree.getIsRoot() || (subTree.getParent() != null &&
                subTree.getParent().getRepresentedType().getElement(subTree.getElementName()) instanceof SelfReference)) {
            outPreVisitState.realValue = true;
            outChildState.realValue = null;
            this.nodeAlias = "";
            return false;
        }

        //缓存项存在
        if (this.aliasCache.containsKey(subTree.getNode())) {
            //取缓存
            this.nodeAlias = this.aliasCache.get(subTree.getNode());
            outPreVisitState.realValue = true;
            outChildState.realValue = null;
            return false;
        }

        outChildState.realValue = null;
        outPreVisitState.realValue = null;
        return true;
    }

    /**
     * 重置访问者
     */
    @Override
    public void reset() {
        //Nothing to Do
    }
}
