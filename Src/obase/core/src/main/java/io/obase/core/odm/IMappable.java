/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：标明类型为可映射类型并公开映射所需的信息.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 15:30:57
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.util.List;

/**
 * 标明类型为可映射类型并公开映射所需的信息
 */
public interface IMappable {

    /**
     * 获取映射目标名称
     *
     * @return 映射目标名称
     */
    String getTargetName();

    /**
     * 设置映射目标名称
     *
     * @param targetName 映射目标名称
     */
    void setTargetName(String targetName);

    /**
     * 获取标识成员的名称序列
     *
     * @return 标识成员的名称序列
     */
    String[] getKeyMemberNames();

    /**
     * 获取标识成员的映射目标序列
     *
     * @return 标识成员的映射目标序列
     */
    List<String> getKeyFields();

    /**
     * 设置标识成员的映射目标序列
     *
     * @param keyFields 标识成员的映射目标序列
     */
    void setKeyFields(List<String> keyFields);
}
