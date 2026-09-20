package io.obase.test.infrastructure.context;

import io.obase.core.odm.OdmValidator;
import io.obase.core.odm.builder.ModelBuilder;
import io.obase.test.infrastructure.modelRegister.CoreModelRegister;

/**
 * 简单的ODM验证器,用于验证ODM的合法性
 */
public class SimpleOdmValidator extends OdmValidator {

    /**
     * 使用指定的建模器创建对象数据模型
     *
     * @param modelBuilder 对象数据模型建造器
     */
    @Override
    protected void createModel(ModelBuilder modelBuilder) {
        CoreModelRegister.registry(modelBuilder);
    }
}
