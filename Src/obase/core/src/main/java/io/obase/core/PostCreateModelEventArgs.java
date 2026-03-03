/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：创建模型后事件数据.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 17:12:16
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.core.odm.ObjectDataModel;

import java.util.EventObject;

/**
 * PostCreatedModel事件的事件参数
 */
public class PostCreateModelEventArgs extends EventObject {

    /**
     * 刚创建的对象数据模型
     */
    private final ObjectDataModel model;

    /**
     * 初始化PostCreateModelEventArgs的新实例
     *
     * @param source 源
     * @param model  创建的对象数据模型
     */
    public PostCreateModelEventArgs(Object source, ObjectDataModel model) {
        super(source);
        this.model = model;
    }

    /**
     * 获取刚创建的对象数据模型
     *
     * @return 创建的对象数据模型
     */
    public ObjectDataModel getModel() {
        return this.model;
    }
}
