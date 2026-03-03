/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：标注类型解析器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 11:07:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.odm.builder.*;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * 标注类型解析器
 */
public class AnnotatedTypeAnalyzer implements ITypeAnalyzer {

    /**
     * 下一节解析器
     */
    private final ITypeAnalyzer analyzer;

    /**
     * 构造标注类型解析器
     *
     * @param analyzer 下一节解析器
     */
    public AnnotatedTypeAnalyzer(ITypeAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    /**
     * 获取类型解析管道中的下一个解析器
     *
     * @return 下一个类型解析器
     */
    @Override
    public ITypeAnalyzer getNext() {
        return this.analyzer;
    }

    /**
     * 配置指定的类型
     *
     * @param type         要配置的类型
     * @param configurator 该类型的配置器
     */
    @Override
    public void configure(Class<?> type, IStructuralTypeConfigurator configurator) {
        if (configurator instanceof IObjectTypeConfigurator) {
            IObjectTypeConfigurator objectTypeConfigurator = (IObjectTypeConfigurator) configurator;
            this.configure(type, objectTypeConfigurator);
        } else {
            //复杂类型 配置构造函数
            this.configureConstructor(type, configurator);
        }
    }

    /**
     * 配置指定的对象类型
     *
     * @param type         要配置的对象类型
     * @param configurator 该对象类型的配置器
     */
    @Override
    public void configure(Class<?> type, IObjectTypeConfigurator configurator) {
        if (configurator instanceof IEntityTypeConfigurator) {
            IEntityTypeConfigurator entityTypeConfigurator = (IEntityTypeConfigurator) configurator;
            this.configure(type, entityTypeConfigurator);
        }

        if (configurator instanceof IAssociationTypeConfigurator) {
            IAssociationTypeConfigurator associationTypeConfigurator = (IAssociationTypeConfigurator) configurator;
            this.configure(type, associationTypeConfigurator);
        }
    }

    /**
     * 配置指定的实体型
     *
     * @param type         要配置的实体类
     * @param configurator 该实体型的配置器
     */
    @Override
    public void configure(Class<?> type, IEntityTypeConfigurator configurator) {
        //类型的标记
        EntityAttribute entityAttribute = type.getAnnotation(EntityAttribute.class);
        if (entityAttribute == null)
            return;
        //配置实体型
        //主键设置
        if (entityAttribute.keyAttributes() != null && entityAttribute.keyAttributes().length > 0) {
            for (String keyAttribute : entityAttribute.keyAttributes()) {
                configurator.hasKeyAttributeI(keyAttribute);
            }
        }

        //是否自增
        configurator.hasKeyIsSelfIncreasedI(entityAttribute.isSelfIncrease());
        //表名
        configurator.toTableI((entityAttribute.tableName() == null || entityAttribute.tableName().isEmpty())
                ? type.getSimpleName()
                : entityAttribute.tableName());
        //配置构造函数
        this.configureConstructor(type, configurator);
    }

    /**
     * 配置指定的关联型
     *
     * @param type         要配置的关联型
     * @param configurator 该关联型的配置器
     */
    @Override
    public void configure(Class<?> type, IAssociationTypeConfigurator configurator) {
        //类型的标记
        AssociationAttribute attribute = type.getAnnotation(AssociationAttribute.class);
        if (attribute == null)
            return;
        //表名
        configurator.toTableI((attribute.tableName() == null || attribute.tableName().isEmpty())
                ? type.getSimpleName()
                : attribute.tableName());
        //配置构造函数
        this.configureConstructor(type, configurator);
        //是否是隐式关联型
        if (type.isAssignableFrom(ImplicitAssociation.class)) {
            Property prop = ObaseIntrospector.getObaseBeanProperties(type).stream().filter(p -> p.getName().equalsIgnoreCase("end1")).findFirst().orElse(null);
            this.configureTableName(configurator, prop);
            prop = ObaseIntrospector.getObaseBeanProperties(type).stream().filter(p -> p.getName().equalsIgnoreCase("end2")).findFirst().orElse(null);
            this.configureTableName(configurator, prop);
        }
    }

    /**
     * 配置表名
     *
     * @param configurator 配置器
     * @param prop         属性
     */
    private void configureTableName(IAssociationTypeConfigurator configurator, Property prop) {
        if (prop != null) {
            Class<?> propType = prop.getPropertyType();
            List<Property> end1props = ObaseIntrospector.getObaseBeanProperties(propType);
            {
                for (Property propertyInfo : end1props) {
                    //如果与要配置的属性类型不一致 跳过
                    if (propertyInfo.getPropertyType() != prop.getPropertyType())
                        continue;
                    //类型的标记
                    ImplicitAssociationAttribute attribute = Utils.getAnnotation(propertyInfo, ImplicitAssociationAttribute.class);
                    if (attribute == null)
                        return;
                    //配置隐式关联型
                    //表名
                    configurator.toTableI((attribute.targetTableName() == null || attribute.targetTableName().isEmpty())
                            ? propType.getSimpleName()
                            : attribute.targetTableName());
                }
            }
        }
    }

    /**
     * 配置构造函数
     *
     * @param type         类型
     * @param configurator 配置器
     */
    private void configureConstructor(Class<?> type, IStructuralTypeConfigurator configurator) {
        //尝试寻找所有构造函数
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        if (constructors.length > 0) {
            for (Constructor<?> constructor : constructors) {
                //如果被标记为构造函数
                ConstructorAttribute constructorAttr = constructor.getAnnotation(ConstructorAttribute.class);
                if (constructorAttr != null) {
                    //配置构造函数
                    IParameterConfigurator parameterConfigurator = configurator.hasConstructorI(constructor);

                    String[] parameters = constructorAttr.parameterNames();

                    if (parameters != null && parameters.length > 0) {
                        if (parameters.length != constructor.getParameters().length) {
                            throw new IllegalArgumentException("构造函数标记ConstructorAttribute的参数数量与所标记的构造函数不符");
                        }

                        for (String parameter : parameters) {
                            Property property = Utils.getProperty(type, parameter);
                            parameterConfigurator.mapI(property.getName());
                        }

                        parameterConfigurator.endI();
                    } else {
                        if (constructor.getParameters().length != 0)
                            throw new IllegalArgumentException("构造函数标记ConstructorAttribute的参数数量与所标记的构造函数不符");
                        parameterConfigurator.endI();
                    }
                }
            }

        }
    }
}
