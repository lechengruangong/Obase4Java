/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：更改通知基类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:12:40
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import java.io.Serializable;

/**
 * 为更改通知提供基础实现
 */
public abstract class ChangeNotice implements Serializable {

    /**
     * 对象变更行为，可取值为Create、Update、Delete、Increase，分别对应“创建”、“修改”、“删除”、“就地累加”四种行为。
     */
    private final String changeAction;

    /**
     * 对象类型的命名空间
     */
    private final String namespace;

    /**
     * 对象类型名称
     */
    private final String objectType;

    /**
     * 变更通知的类型
     */
    private final EChangeNoticeType type;

    /**
     * 初始化ChangeNotice类的新实例
     *
     * @param changeAction 对象变更行为，可取值为Create、Update、Delete、Increase，分别对应“创建”、“修改”、“删除”、“就地累加”四种行为。
     * @param namespace    对象类型的命名空间
     * @param objectType   对象类型名称
     * @param type         通知类型
     */
    protected ChangeNotice(String changeAction, String namespace, String objectType, EChangeNoticeType type) {
        this.changeAction = changeAction;
        this.namespace = namespace;
        this.objectType = objectType;
        this.type = type;
    }

    /**
     * 获取对象变更行为，可取值为Create、Update、Delete、Increase，分别对应“创建”、“修改”、“删除”、“就地累加”四种行为。
     *
     * @return 对象变更行为，可取值为Create、Update、Delete、Increase，分别对应“创建”、“修改”、“删除”、“就地累加”四种行为。
     */
    public String getChangeAction() {
        return this.changeAction;
    }

    /**
     * 获取对象类型的命名空间
     *
     * @return 对象类型的命名空间
     */
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * 获取对象类型名称
     *
     * @return 对象类型名称
     */
    public String getObjectType() {
        return this.objectType;
    }

    /**
     * 获取通知类型
     *
     * @return 通知类型
     */
    public EChangeNoticeType getType() {
        return this.type;
    }
}
