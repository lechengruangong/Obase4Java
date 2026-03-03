package io.obase.test.infrastructure.modelRegister;

import io.obase.addon.test.domain.annotation.AnnotationDomesticAddress;
import io.obase.addon.test.domain.annotation.AnnotationJavaBeanWithCustomAttribute;
import io.obase.addon.test.domain.logical.deletion.LogicDeletion;
import io.obase.addon.test.domain.logical.deletion.LogicDeletionNoDef;
import io.obase.addon.test.domain.multi.tenant.MultiTenantSchool;
import io.obase.addon.test.domain.multi.tenant.MultiTenantSchoolNoDef;
import io.obase.addon.test.domain.multi.tenant.MultiTenantTeacher;
import io.obase.addon.test.domain.multi.tenant.MultiTenantTeacherNoDef;
import io.obase.core.odm.builder.ModelBuilder;
import io.obase.logical.deletion.LogicDeletionExtensionConfiguration;
import io.obase.multi.tenant.MultiTenantExtensionConfiguration;
import io.obase.odm.annotation.AnnotationModelingExtensions;

import java.util.UUID;

/**
 * 插件的模型注册器
 */
public class AddonModelRegister {

    /**
     * 注册方法
     *
     * @param modelBuilder 建模器
     */
    @SuppressWarnings("Convert2MethodRef")
    public static void registry(ModelBuilder modelBuilder) {
        //调用此方法 指定某几个个软件包 解析此软件包下所有的标注类
        AnnotationModelingExtensions.useAnnotationModeling(modelBuilder, new String[]{"io.obase.addon.test.domain"});

        //JavaBean已标注 且无特殊属性 自定义表名 仅需要在标记上指明主键和主键是否自增 和 指定反序列化构造函数
        //此处不需要配置

        //AnnotationJavaBeanWithCustomAttribute类已标注 但有特殊的属性Strings需要手动的设值器和取值器
        var entity = modelBuilder.entity(AnnotationJavaBeanWithCustomAttribute.class);
        entity.attribute(p -> p.getStrings(), String.class)
                .hasValueGetter((AnnotationJavaBeanWithCustomAttribute javaBean) -> {
                    if (javaBean.getStrings() != null && javaBean.getStrings().length > 0)
                        return String.join(",", javaBean.getStrings());
                    else
                        return "";
                })
                .hasValueSetter((AnnotationJavaBeanWithCustomAttribute javaBean, String s) -> {
                    if (s != null && !s.isEmpty())
                        javaBean.setStrings(s.split(","));
                });
        //此处设置的表名 主键 是否自增 反序列化构造函数会被标注属性覆盖
        entity.toTable("123");
        entity.hasKeyAttribute(AnnotationJavaBeanWithCustomAttribute::getBool);
        entity.hasKeyIsSelfIncreased(true);

        //复杂类型
        var domesticAddressConfig = modelBuilder.entity(AnnotationDomesticAddress.class);
        domesticAddressConfig.attribute(AnnotationDomesticAddress::getCity).hasMappingConnectionChar('_');
        domesticAddressConfig.attribute(AnnotationDomesticAddress::getRegion).hasMappingConnectionChar('-');

        //School已标注 此处无需配置
        //Class已标注 此处无需配置
        //Student已标注 此处无需配置
        //Teacher已标注 此处无需配置
        //ClassTeacher已标注 此处无需配置

        //代码配置逻辑删除
        var logicDeletion = modelBuilder.entity(LogicDeletion.class);
        logicDeletion.hasKeyAttribute(p -> p.getIntNumber()).hasKeyIsSelfIncreased(false);
        //创建逻辑删除扩展
        LogicDeletionExtensionConfiguration<LogicDeletion> logicDeletionExt = logicDeletion.hasExtension(LogicDeletionExtensionConfiguration.class);
        //当类中有定义逻辑删除字段时 指定为逻辑删除标记
        logicDeletionExt.hasDeletionMark(p -> p.getBool());
        //映射字段与标记名相同 则不需要下一行HasDeletionField设置字段 当前逻辑删除标记Bool与字段Bool相同 故此行可以注释掉
        //logicDeletionExt.HasDeletionField("Bool");

        //LogicDeletionAnnotation已标注 此处无需配置

        //代码配置未定义字段的逻辑删除
        var logicDeletionNoDef = modelBuilder.entity(LogicDeletionNoDef.class);
        logicDeletionNoDef.hasKeyAttribute(p -> p.getIntNumber()).hasKeyIsSelfIncreased(false);
        //创建逻辑删除扩展
        LogicDeletionExtensionConfiguration<LogicDeletionNoDef> logicDeletionNoDefExt = logicDeletionNoDef.hasExtension(LogicDeletionExtensionConfiguration.class);
        //当类中未定义逻辑删除字段时 仅需要指定为逻辑删除映射字段
        logicDeletionNoDefExt.hasDeletionField("Bool");

        //LogicDeletionNoDefAnnotation已标注 此处无需配置

        //代码配置的多租户
        var multiTenantSchool = modelBuilder.entity(MultiTenantSchool.class);
        multiTenantSchool.hasKeyAttribute(p -> p.getSchoolId()).hasKeyIsSelfIncreased(true);
        //创建多租户扩展
        MultiTenantExtensionConfiguration<MultiTenantSchool> multiTenantExt1 = multiTenantSchool.hasExtension(MultiTenantExtensionConfiguration.class);
        //当类中有定义多租户字段时 指定为多租户标记
        multiTenantExt1.hasUUIDTenantIdMark(p -> p.getMultiTenantId());
        //映射字段与标记名相同 则不需要下一行HasTenantIdField设置字段和类型
        //multiTenantExt.HasTenantIdField("MultiTenantId",typeof(Guid));
        //配置一个全是0的GUID作为全局ID
        multiTenantExt1.hasGlobalTenantId(new UUID(0, 0));
        multiTenantSchool.toTable("School");

        var multiTenantTeacher = modelBuilder.entity(MultiTenantTeacher.class);
        multiTenantTeacher.hasKeyAttribute(p -> p.getTeacherId()).hasKeyIsSelfIncreased(true);
        //创建多租户扩展
        MultiTenantExtensionConfiguration<MultiTenantTeacher> multiTenantExt2 = multiTenantTeacher.hasExtension(MultiTenantExtensionConfiguration.class);
        //当类中有定义多租户字段时 指定为多租户标记
        multiTenantExt2.hasUUIDTenantIdMark(p -> p.getMultiTenantId());
        //映射字段与标记名相同 则不需要下一行HasTenantIdField设置字段和类型
        //multiTenantExt.HasTenantIdField("MultiTenantId",typeof(Guid));
        //配置一个全是0的GUID作为全局ID
        multiTenantExt2.hasGlobalTenantId(new UUID(0, 0));
        multiTenantTeacher.toTable("Teacher");
        //配置关联 符合推断 无需配置

        //MultiTenantSchoolAnnotation/TeacherAnnotation已标注 此处无需配置

        //代码配置未定义字段的多租户
        var multiTenantSchoolNoDef = modelBuilder.entity(MultiTenantSchoolNoDef.class);
        multiTenantSchoolNoDef.hasKeyAttribute(p -> p.getSchoolId()).hasKeyIsSelfIncreased(true);
        //创建多租户扩展
        MultiTenantExtensionConfiguration<MultiTenantSchoolNoDef> multiTenantExtNoDef1 = multiTenantSchoolNoDef.hasExtension(MultiTenantExtensionConfiguration.class);
        //当类中未定义多租户字段时 需要指定字段设置字段和类型
        multiTenantExtNoDef1.hasTenantIdField("MultiTenantId", UUID.class);
        //配置一个全是0的GUID作为全局ID
        multiTenantExtNoDef1.hasGlobalTenantId(new UUID(0, 0));
        multiTenantSchoolNoDef.toTable("School");

        var multiTenantTeacherNoDef = modelBuilder.entity(MultiTenantTeacherNoDef.class);
        multiTenantTeacherNoDef.hasKeyAttribute(p -> p.getTeacherId()).hasKeyIsSelfIncreased(true);
        //创建多租户扩展
        MultiTenantExtensionConfiguration<MultiTenantTeacherNoDef> multiTenantExtNoDef2 = multiTenantTeacherNoDef.hasExtension(MultiTenantExtensionConfiguration.class);
        //当类中未定义多租户字段时 需要指定字段设置字段和类型
        multiTenantExtNoDef2.hasTenantIdField("MultiTenantId", UUID.class);
        //配置一个全是0的GUID作为全局ID
        multiTenantExtNoDef2.hasGlobalTenantId(new UUID(0, 0));
        multiTenantTeacherNoDef.toTable("Teacher");
        //配置关联 符合推断 无需配置

        //MultiTenantSchoolNoDefAnnotation/TeacherNoDefAnnotation已标注 此处无需配置
    }
}
