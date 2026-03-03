/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象变更通知.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-10 16:57:36
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.mapping.pipeline;

import java.util.List;

/**
 * 对象变更通知
 */
public class ObjectChangeNotice extends ChangeNotice {

    /**
     * 对象的标识
     */
    private final List<ObjectAttribute> objectKeys;

    /**
     * 对象的属性及其取值
     */
    private final List<ObjectAttribute> attributes;

    /**
     * 初始化ChangeNotice类的新实例
     *
     * @param changeAction 对象变更行为，可取值为Create、Update、Delete、Increase，分别对应“创建”、“修改”、“删除”、“就地累加”四种行为。
     * @param namespace    对象类型的命名空间
     * @param objectType   对象类型名称
     * @param objectKeys   对象的属性及其取值
     * @param attributes   变更的属性及其取值
     */
    public ObjectChangeNotice(String changeAction, String namespace, String objectType, List<ObjectAttribute> objectKeys, List<ObjectAttribute> attributes) {
        super(changeAction, namespace, objectType, EChangeNoticeType.ObjectChange);
        this.objectKeys = objectKeys;
        this.attributes = attributes;
    }

    /**
     * 获取对象的标识
     *
     * @return 对象的标识
     */
    public List<ObjectAttribute> getObjectKeys() {
        return this.objectKeys;
    }

    /**
     * 获取对象的属性及其取值
     *
     * @return 对象的属性及其取值
     */
    public List<ObjectAttribute> getAttributes() {
        return this.attributes;
    }
}
