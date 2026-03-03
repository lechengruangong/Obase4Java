/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：逻辑删除的补充配置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 11:01:14
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.logical.deletion;

import io.obase.core.odm.Attribute;
import io.obase.core.odm.StructuralType;
import io.obase.core.odm.builder.IComplementConfigurator;
import io.obase.core.odm.builder.StructuralTypeConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

/**
 * 补充配置器
 */
public class ComplementConfigurator implements IComplementConfigurator {

    /**
     * 补充配置管道中的下一个配置器
     */
    private final IComplementConfigurator next;

    /**
     * 初始化补充配置器
     *
     * @param next 下一个配置器
     */
    public ComplementConfigurator(IComplementConfigurator next) {
        this.next = next;
    }

    /**
     * 补充配置管道中的下一个配置器
     *
     * @return 下一个补充配置器
     */
    @Override
    public IComplementConfigurator getNext() {
        return this.next;
    }

    /**
     * 根据类型配置项中的元数据配置指定的类型
     *
     * @param targetType    要配置的类型
     * @param configuration 包含配置元数据的类型配置项
     */
    @Override
    public void configure(StructuralType targetType, StructuralTypeConfiguration<?> configuration) {
        LogicDeletionExtension ext = (LogicDeletionExtension) targetType.getExtension(LogicDeletionExtension.class);
        if (ext != null && (ext.getDeletionMark() == null || ext.getDeletionMark().isEmpty())) {
            Attribute attribute = new Attribute(boolean.class, "Obase_gen_deletionMark");
            //目标字段 若果未设置DeletionField就和DeletionMark相同
            attribute.setTargetField((ext.getDeletionField() == null || ext.getDeletionField().isEmpty()) ? ext.getDeletionMark() : ext.getDeletionField());
            //默认不为空
            attribute.setNullable(false);
            Field field;
            try {
                field = targetType.getRebuildingType().getDeclaredField(StringUtils.uncapitalize(attribute.getName()));
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("无法获取逻辑删除字段.", e);
            }
            //构造FieldValueGetter
            LogicDeletionFieldValueGetter valueGetter = new LogicDeletionFieldValueGetter(field, targetType);
            attribute.setValueGetter(valueGetter);
            //构造FieldValueSetter
            LogicDeletionFieldValueSetter setter = new LogicDeletionFieldValueSetter(field, targetType);
            attribute.setValueSetter(setter);
            targetType.addAttribute(attribute);
        }
    }
}
