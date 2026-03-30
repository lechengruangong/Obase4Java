/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化实体配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-30 14:28:03
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.serialization;

import io.obase.core.odm.serialization.SerializationEntity;

/**
 * 序列化实体配置
 */
public abstract class SerializationEntityConfiguration {

    /**
     * 创建序列化实体方法
     *
     * @return 序列化实体
     */
    public SerializationEntity create() {
        //调用实现类的CreateReally方法构建模型类型
        return this.createReally();
    }

    /**
     * 根据类型配置项中的元数据构建模型类型
     * 本方法由派生类实现
     *
     * @return 序列化实体类型
     */
    protected abstract SerializationEntity createReally();
}
