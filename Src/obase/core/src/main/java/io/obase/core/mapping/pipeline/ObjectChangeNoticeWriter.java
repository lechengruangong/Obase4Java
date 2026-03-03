/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象变更通知编写器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:56:34
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;


import io.obase.common.FunctionWithOneArg;

import java.util.List;

/**
 * 对象变更通知编写器
 */
public class ObjectChangeNoticeWriter implements IChangeNoticeWriter {

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
     * 对象的标识
     */
    private final List<ObjectAttribute> objectKeys;

    /**
     * 对象的属性及其取值
     */
    private final List<ObjectAttribute> attributes;

    /**
     * 初始化初始化对象变更通知编写器
     *
     * @param changeAction 对象变更行为
     * @param namespace    对象类型的命名空间
     * @param objectType   对象类型名称
     * @param objectKeys   对象的属性及其取值
     * @param attributes   对象的标识
     */
    public ObjectChangeNoticeWriter(String changeAction, String namespace, String objectType, List<ObjectAttribute> objectKeys, List<ObjectAttribute> attributes) {
        this.changeAction = changeAction;
        this.namespace = namespace;
        this.objectType = objectType;
        this.objectKeys = objectKeys;
        this.attributes = attributes;
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
        return new ObjectChangeNotice(this.changeAction, this.namespace, this.objectType, this.objectKeys, this.attributes);
    }
}
