/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：别名生成器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 11:57:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

import io.obase.core.common.Utils;
import io.obase.core.odm.objectSys.AssociationTreeNodeAliasGenerator;
import io.obase.core.odm.objectSys.IParameterizedAssociationTreeUpwardVisitorWithResult;

/**
 * 别名生成器，既可用于生成关联树节点的别名，也可用于生成该节点相关投影列的别名
 */
public class AliasGenerator extends AssociationTreeNodeAliasGenerator implements IParameterizedAssociationTreeUpwardVisitorWithResult<String, String> {

    /**
     * 属性或标识成员的映射目标，基于其生成投影列。
     */
    private String fieldName;

    /**
     * 获取属性或标识成员的映射目标，基于其生成投影列
     *
     * @return 属性或标识成员的映射目标
     */
    public String getFieldName() {
        return this.fieldName;
    }

    /**
     * 设置属性或标识成员的映射目标，基于其生成投影列。
     *
     * @param fieldName 属性或标识成员的映射目标
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * 获取遍历关联树的结果
     *
     * @return 遍历操作返回结果的类型
     */
    @Override
    public String getResult() {
        //获取基类结果
        String nodeAlias = super.getResult();
        if (Utils.getStringIsEmpty(this.fieldName))
            return nodeAlias;

        if (Utils.getStringIsEmpty(nodeAlias))
            return "";
        return nodeAlias + "_" + this.fieldName;
    }

    /**
     * 为即将开始的遍历操作设置参数
     *
     * @param argument 参数值
     */
    @Override
    public void setArgument(String argument) {
        this.fieldName = argument;
    }

    /**
     * 重置访问者
     */
    @Override
    public void reset() {
        this.fieldName = null;
    }
}
