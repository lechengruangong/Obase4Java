/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：就地修改通知.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:47:25
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import java.util.Map;

/**
 * 就地修改通知
 */
public class DirectlyChangingNotice extends ChangeNotice {

    /**
     * 筛选条件表达式
     */
    private final String criteria;

    /**
     * 就地修改类型
     */
    private final EDirectlyChangeType directlyChangeType;

    /**
     * 新值键值对
     */
    private final Map<String, Object> newValues;

    /**
     * 初始化ChangeNotice类的新实例
     *
     * @param changeAction       对象变更行为，可取值为Create、Update、Delete、Increase，分别对应“创建”、“修改”、“删除”、“就地累加”四种行为
     * @param namespace          对象类型的命名空间
     * @param objectType         对象类型名称
     * @param criteria           筛选条件表达式
     * @param directlyChangeType 就地修改类型
     * @param newValues          修改的字段和值键值对
     */
    public DirectlyChangingNotice(String changeAction, String namespace, String objectType, String criteria, EDirectlyChangeType directlyChangeType, Map<String, Object> newValues) {
        super(changeAction, namespace, objectType, EChangeNoticeType.DirectlyChanging);
        this.criteria = criteria;
        this.directlyChangeType = directlyChangeType;
        this.newValues = newValues;
    }

    /**
     * 获取筛选条件表达式
     *
     * @return 筛选条件表达式
     */
    public String getCriteria() {
        return this.criteria;
    }

    /**
     * 获取就地修改类型
     *
     * @return 就地修改类型
     */
    public EDirectlyChangeType getDirectlyChangeType() {
        return this.directlyChangeType;
    }

    /**
     * 获取新值键值对
     *
     * @return 新值键值对
     */
    public Map<String, Object> getNewValues() {
        return this.newValues;
    }
}
