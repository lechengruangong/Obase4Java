/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化实体配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-30 14:32:58
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.serialization;

import io.obase.core.odm.builder.ModelBuilder;
import io.obase.core.odm.serialization.SerializationEntity;

/**
 * 序列化实体配置
 *
 * @param <T> 实体类型
 */
public class SerializationEntityConfigurationGeneric<T> extends SerializationEntityConfiguration {

    /**
     * 实体类型
     */
    private final Class<T> typeClass;

    /**
     * 建模器
     */
    private final ModelBuilder builder;

    /**
     * 初始化序列化实体配置
     *
     * @param typeClass 实体类型
     * @param builder   建模器
     */
    public SerializationEntityConfigurationGeneric(Class<T> typeClass, ModelBuilder builder) {

        this.typeClass = typeClass;
        this.builder = builder;
    }

    /**
     * 根据类型配置项中的元数据构建模型类型
     * 本方法由派生类实现
     *
     * @return 序列化实体类型
     */
    @Override
    protected SerializationEntity createReally() {
        return null;
    }
}
