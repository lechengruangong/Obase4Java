/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象数据模型查看器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 15:25:52
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.ObjectContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 对象数据模型查看器
 */
public class ObjectDataModelViewer {

    /**
     * 获取对象数据模型映射的简单视图
     * 仅包含实体型的映射表和关联引用的映射关系
     *
     * @param context 要查看的上下文
     * @return 结果字符串
     */
    public static StringBuilder getSimpleObjectDataModelMappingView(ObjectContext context) {
        //获取模型
        ObjectDataModel model = context.getModel();
        //结果
        StringBuilder result = new StringBuilder();
        //检查实体型即可
        List<EntityType> entities = model.getTypes().stream().filter(p -> p instanceof EntityType).map(p -> (EntityType) p).collect(Collectors.toList());
        result.append("本模型共包含").append(entities.size()).append("个实体型. ").append(System.lineSeparator());
        //简略版 只处理本身和关联引用的
        for (EntityType entity : entities) {
            result.append(System.lineSeparator());
            processEntity(entity, result);
            processAssociationReference(entity, result);
            result.append(System.lineSeparator());
        }

        return result;
    }

    /**
     * 获取对象数据模型映射的简单视图
     * 包含完整的映射关系
     *
     * @param context 要查看的上下文
     * @return 结果字符串
     */
    public static StringBuilder getFullObjectDataModelMappingView(ObjectContext context) {
        //获取模型
        ObjectDataModel model = context.getModel();
        //结果
        StringBuilder result = new StringBuilder();
        //检查实体型即可
        List<EntityType> entities = model.getTypes().stream().filter(p -> p instanceof EntityType).map(p -> (EntityType) p).collect(Collectors.toList());
        result.append("本模型共包含").append(entities.size()).append("个实体型.").append(System.lineSeparator());
        //简略版 只处理本身和关联引用的
        for (EntityType entity : entities) {
            result.append(System.lineSeparator());
            processEntity(entity, result);
            processAttribute(entity, result);
            processAssociationReference(entity, result);
            result.append(System.lineSeparator());
        }

        return result;
    }

    /**
     * 处理实体型本身的映射
     *
     * @param entityType    实体型
     * @param stringBuilder 结果
     */
    private static void processEntity(EntityType entityType, StringBuilder stringBuilder) {
        if (entityType.getDerivingFrom() != null)
            stringBuilder.append("实体型").append(entityType.getClrType().getName()).append("继承自").append(entityType.getDerivingFrom().getClrType().getName()).append(",映射表为").append(entityType.getTargetTable()).append(".").append(System.lineSeparator());
        else
            stringBuilder.append("实体型").append(entityType.getClrType().getName()).append("的映射表为").append(entityType.getTargetTable()).append(".").append(System.lineSeparator());

        stringBuilder.append("实体型").append(entityType.getClrType().getName()).append("共有").append(entityType.getKeyAttributes().size()).append("个主键.").append(System.lineSeparator());
        int seq = 1;
        List<Attribute> keyAttributes = entityType.getKeyAttributes().stream().map(key -> entityType.getAttributes().stream().filter(p -> p.getName().equalsIgnoreCase(key)).findFirst().orElse(null)).collect(Collectors.toList());
        for (Attribute keyAttribute : keyAttributes) {
            if (keyAttribute != null)
                stringBuilder.append(seq).append(". ").append(entityType.getKeyIsSelfIncreased() ? "" : "非").append("自增主键").append(keyAttribute.getName()).append(",映射类型")
                        .append(keyAttribute.getDataType()).append(",映射字段").append(keyAttribute.getTargetField()).append(".").append(System.lineSeparator());
            seq++;
        }
    }

    /**
     * 处理属性的映射
     *
     * @param entityType    实体型
     * @param stringBuilder 结果
     */
    private static void processAttribute(EntityType entityType, StringBuilder stringBuilder) {
        stringBuilder.append("实体型").append(entityType.getClrType().getName()).append("共有").append(entityType.getAttributes().size()).append("个属性.").append(System.lineSeparator());
        int seq = 1;
        for (Attribute attribute : entityType.getAttributes()) {
            if (attribute instanceof ComplexAttribute) {
                ComplexAttribute complex = (ComplexAttribute) attribute;
                stringBuilder.append(seq).append(". ").append("复杂属性").append(complex.getName()).append(",使用的复杂类型为").append(complex.getComplexType().getClrType().getName()).append(",映射类型").append(complex.getDataType())
                        .append(",映射字段").append(complex.getTargetField()).append(".").append(System.lineSeparator());
            } else {
                stringBuilder.append(seq).append(". ").append("简单属性").append(attribute.getName()).append(",映射类型").append(attribute.getDataType()).append(",映射字段").append(attribute.getTargetField()).append(".")
                        .append(System.lineSeparator());
            }
            seq++;
        }
    }

    /**
     * 处理关联引用的映射
     *
     * @param entityType    实体型
     * @param stringBuilder 结果
     */
    private static void processAssociationReference(EntityType entityType, StringBuilder stringBuilder) {
        stringBuilder.append("实体型").append(entityType.getClrType().getName()).append("共有").append(entityType.getAssociationReferences().size()).append("个关联引用.").append(System.lineSeparator());
        int seq = 1;
        for (AssociationReference reference : entityType.getAssociationReferences()) {
            stringBuilder.append(seq).append(". 关联引用").append(reference.getName()).append(",对应关联型为").append(reference.getAssociationType().getClrType().getName()).append(",映射表为").append(reference.getAssociationType().getTargetTable())
                    .append(System.lineSeparator());
            stringBuilder.append("在映射表").append(reference.getAssociationType().getTargetTable()).append("中,共有关联端").append(reference.getAssociationType().getAssociationEnds().size()).append("个.")
                    .append(System.lineSeparator());
            for (AssociationEnd end : reference.getAssociationType().getAssociationEnds()) {
                stringBuilder.append("关联端").append(end.getEntityType().getClrType().getName()).append("的映射为:").append(System.lineSeparator());
                for (AssociationEndMapping mapping : end.getMappings())
                    stringBuilder.append("主键").append(mapping.getKeyAttribute()).append("映射为").append(mapping.getTargetField()).append(System.lineSeparator());
            }
            seq++;
        }
    }
}

