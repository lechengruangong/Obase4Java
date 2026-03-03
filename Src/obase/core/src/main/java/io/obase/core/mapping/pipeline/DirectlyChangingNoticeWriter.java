/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：就地修改变更通知编写器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:46:15
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import io.obase.common.FunctionWithOneArg;

import java.util.Map;

/**
 * 就地修改变更通知编写器
 */
public class DirectlyChangingNoticeWriter implements IChangeNoticeWriter {

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
     * 构造就地修改变更通知编写器
     *
     * @param changeAction       对象变更行为
     * @param namespace          对象类型的命名空间
     * @param objectType         对象类型名称
     * @param criteria           筛选条件表达式
     * @param directlyChangeType 就地修改类型
     * @param newValues          修改的字段和值键值对
     */
    public DirectlyChangingNoticeWriter(String changeAction, String namespace, String objectType, String criteria, EDirectlyChangeType directlyChangeType, Map<String, Object> newValues) {
        this.changeAction = changeAction;
        this.namespace = namespace;
        this.objectType = objectType;
        this.criteria = criteria;
        this.directlyChangeType = directlyChangeType;
        this.newValues = newValues;
    }

    /**
     * 编写通知的字符串形式
     *
     * @param serializeFunction 序列化方法
     * @return 通知的字符串形式
     */
    @Override
    public String write(FunctionWithOneArg<Object, String> serializeFunction) {
        return serializeFunction.invoke(this.write());
    }

    /**
     * 编写通知
     *
     * @return 通知
     */
    @Override
    public ChangeNotice write() {
        return new DirectlyChangingNotice(this.changeAction, this.namespace, this.objectType, this.criteria, this.directlyChangeType,
                this.newValues);
    }
}
