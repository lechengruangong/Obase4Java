/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：标注建模的程序集分析器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-19 11:00:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.odm.annotation;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.AssemblyUtil;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.odm.builder.IAssemblyAnalyzer;
import io.obase.core.odm.builder.ModelBuilder;
import io.obase.core.odm.builder.implicitAssociationConfigor.AssociationConfiguratorBuilder;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 标注建模的程序集分析器
 */
public class AssemblyAnalyzer implements IAssemblyAnalyzer {
    /**
     * 从指定的程序集提取类型并注册到模型
     *
     * @param assembly     要解析的包名
     * @param modelBuilder 建模器
     */
    @Override
    public void analyze(String assembly, ModelBuilder modelBuilder) {
        //获取所有包内的类
        Set<Class<?>> allClasses = AssemblyUtil.getAllClassByPackageName(assembly);
        this.analyze(allClasses.toArray(new Class<?>[0]), modelBuilder);
    }

    /**
     * 从指定的类型数组中提取类型并注册到模型
     *
     * @param types        指定的类型
     * @param modelBuilder 建模器
     */
    @Override
    public void analyze(Class<?>[] types, ModelBuilder modelBuilder) {
        for (Class<?> type : types) {
            //类型的标记
            Annotation[] attrs = type.getAnnotations();

            if (attrs.length > 0) {
                if (Arrays.stream(attrs).filter(p -> p instanceof EntityAttribute || p instanceof AssociationAttribute || p instanceof ComplexAttribute).count() > 1)
                    throw new IllegalArgumentException("不支持将" + type.getName() + "同时标注为多个模型类型");

                Annotation attribute = Arrays.stream(attrs).filter(p -> p instanceof EntityAttribute || p instanceof AssociationAttribute || p instanceof ComplexAttribute).findFirst().orElse(null);

                if (attribute == null)
                    continue;

                //配置实体型
                if (attribute instanceof EntityAttribute) {
                    modelBuilder.entity(type);
                }

                //配置显式关联型
                if (attribute instanceof AssociationAttribute) {
                    modelBuilder.association(type);
                }

                //配置复杂类型
                if (attribute instanceof ComplexAttribute) {
                    modelBuilder.complex(type);
                }
            }
        }

        for (Class<?> type : types) {
            //配置隐式关联型
            List<Property> properties = ObaseIntrospector.getObaseBeanProperties(type);
            for (Property prop : properties) {
                ImplicitAssociationAttribute implicitAttr = Utils.getAnnotation(prop, ImplicitAssociationAttribute.class);
                if (implicitAttr != null) {
                    //解析并创建隐式关联型
                    ObjectReferencePack<Class<?>> endType = new ObjectReferencePack<>();

                    //属性为集合类型
                    Utils.getIsMultiple(prop, endType);
                    Class<?>[] endTypes = new Class<?>[]{type, endType.realValue};
                    AssociationConfiguratorBuilder builder = modelBuilder.association();
                    for (Class<?> end : endTypes) {
                        builder.associationEnd(end);
                    }
                }
            }
        }
    }
}
