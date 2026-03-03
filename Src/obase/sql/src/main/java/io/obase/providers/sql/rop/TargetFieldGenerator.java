/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：映射字段生成器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 14:58:18
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.ComplexAttribute;
import io.obase.core.odm.objectSys.AttributeTree;
import io.obase.core.odm.objectSys.AttributeTreeNode;
import io.obase.core.odm.objectSys.IAttributeTreeUpwardVisitorWithResult;

import java.util.HashMap;
import java.util.Map;

/**
 * 映射字段生成器
 */
public class TargetFieldGenerator implements IAttributeTreeUpwardVisitorWithResult<String> {

    /**
     * 用于缓存生成结果的字典，其键为属性树节点，值为该节点所代表的属性的映射目标。
     */
    private final Map<AttributeTreeNode, String> fieldCache = new HashMap<>();

    /**
     * 指示是否启用缓存
     */
    private boolean enableCache;

    /**
     * 生成结果
     */
    private String result = "";

    /**
     * 获取一个值，该值指示是否启用缓存
     *
     * @return 是否启用缓存
     */
    public boolean getEnableCache() {
        return this.enableCache;
    }

    /**
     * 设置一个值，该值指示是否启用缓存
     *
     * @param enableCache 是否启用缓存
     */
    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    /**
     * 前置访问，即在访问父级前执行操作
     *
     * @param subTree          被访问的子树
     * @param childState       访问子级时产生的状态数据
     * @param outChildState    返回一个状态数据，在遍历到父级时该数据将被视为子级状态
     * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
     * @return 是否继续访问
     */
    @Override
    public boolean preVisit(AttributeTree subTree, Object childState, ObjectReferencePack<Object> outChildState, ObjectReferencePack<Object> outPreVisitState) {
        //直接返回
        if (!this.enableCache) {
            outChildState.realValue = null;
            outPreVisitState.realValue = null;
            return true;
        }

        //如果是根节点
        if (subTree.getParent() == null) {
            outChildState.realValue = null;
            outPreVisitState.realValue = null;
            this.result = "";
            return false;
        }

        //查找换成
        String cacheValue = null;
        if (this.fieldCache.containsKey(subTree.getNode())) cacheValue = this.fieldCache.get(subTree.getNode());

        if (cacheValue == null) {
            outChildState.realValue = null;
            outPreVisitState.realValue = null;
            return true;
        }

        this.result = cacheValue;
        outPreVisitState.realValue = true;
        outChildState.realValue = null;
        return false;
    }

    /**
     * 后置访问，即在访问父级后执行操作
     *
     * @param subTree       被访问的子树
     * @param childState    访问子级时产生的状态数据
     * @param preVisitState 前置访问产生的状态数据
     */
    @Override
    public void postVisit(AttributeTree subTree, Object childState, Object preVisitState) {
        //前置结果为true
        if (preVisitState instanceof Boolean) {
            Boolean boolState = (Boolean) preVisitState;
            if (boolState)
                return;
        }
        //取属性的字段
        String attributeField = subTree.getAttribute().getTargetField();
        this.result = this.result + attributeField;
        //如果不是复杂属性 则到此结束
        if (!subTree.getIsComplex())
            return;
        //此时肯定为复杂属性
        char connectChar = ((ComplexAttribute) subTree.getAttribute()).getMappingConnectionChar();
        //写入结果 存入缓存
        this.result = connectChar == (char) -1 ? "" : this.result + connectChar;
        this.fieldCache.put(subTree.getNode(), this.result);
    }

    /**
     * 重置访问者
     */
    @Override
    public void reset() {
        this.result = "";
    }

    /**
     * 获取遍历属性树的结果
     *
     * @return 获取遍历属性树的结果
     */
    @Override
    public String getResult() {
        return this.result;
    }
}
