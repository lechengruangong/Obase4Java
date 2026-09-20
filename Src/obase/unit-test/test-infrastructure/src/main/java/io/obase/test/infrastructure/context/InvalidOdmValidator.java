package io.obase.test.infrastructure.context;

import io.obase.core.odm.OdmValidator;
import io.obase.core.odm.builder.ModelBuilder;
import io.obase.test.domain.simpleType.NullableJavaBean;

/**
 * 一个无法通过ODM完整性检查的验证器
 * 只配置实体型 不配置主键
 */
public class InvalidOdmValidator extends OdmValidator {

    /**
     * 使用指定的建模器创建对象数据模型
     *
     * @param modelBuilder 对象数据模型建造器
     */
    @Override
    protected void createModel(ModelBuilder modelBuilder) {
        //只配置实体型 不配置主键 完整性检查时会产生ODM的错误信息
        modelBuilder.entity(NullableJavaBean.class);
    }
}
