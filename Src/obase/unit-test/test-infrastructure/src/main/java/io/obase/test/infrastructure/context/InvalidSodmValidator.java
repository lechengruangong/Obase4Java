package io.obase.test.infrastructure.context;

import io.obase.core.odm.OdmValidator;
import io.obase.core.odm.builder.ModelBuilder;
import io.obase.test.domain.functional.serialization.Identity;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 一个无法通过SODM完整性检查的验证器
 * 配置了序列化实体型的反序列化构造函数 但只配置了其中一个参数
 */
public class InvalidSodmValidator extends OdmValidator {

    /**
     * 使用指定的建模器创建对象数据模型
     *
     * @param modelBuilder 对象数据模型建造器
     */
    @Override
    protected void createModel(ModelBuilder modelBuilder) {
        //配置一个序列化实体型
        var identityEntityConfiguration = modelBuilder.serializationEntity(Identity.class);

        //配置Identity的反序列化构造函数
        Constructor<Identity> identityConstructor;
        try {
            identityConstructor = Identity.class.getDeclaredConstructor(UUID.class, LocalDateTime.class, String.class, LocalDateTime.class, long.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("无法获取Identity的构造字段", e);
        }

        //只配置第一个参数 完整性检查时会产生SODM的错误信息
        identityEntityConfiguration.hasConstructor(identityConstructor)
                .hasParameter((Identity p) -> p.getId(), UUID.class, true);
    }
}
