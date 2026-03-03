/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：复杂类型配置项.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-22 16:20:46
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.common.Property;
import io.obase.core.odm.ComplexType;
import io.obase.core.odm.DelegateConstructor;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.StructuralType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 复杂类型配置项
 *
 * @param <TComplex>
 */
public class ComplexTypeConfiguration<TComplex> extends StructuralTypeConfigurationGeneric<TComplex, ComplexTypeConfiguration<TComplex>> {

    /**
     * 包含元素配置项
     */
    private HashMap<String, TypeElementConfiguration> typeElementConfigurations;

    /**
     * 创建StructuralTypeConfiguration的实例
     *
     * @param clrType      运行时类型
     * @param modelBuilder 指定类型配置项所属的建模器
     */
    public ComplexTypeConfiguration(Class<TComplex> clrType, ModelBuilder modelBuilder) {
        super(clrType, modelBuilder);
    }

    /**
     * 获取所有的元素配置项，包括属性配置项、关联引用配置项、关联端配置项
     *
     * @return 所有的元素配置项，包括属性配置项、关联引用配置项、关联端配置项
     */
    @Override
    protected HashMap<String, TypeElementConfiguration> getElementConfigurations() {
        if (this.typeElementConfigurations == null)
            this.typeElementConfigurations = new HashMap<>();
        return this.typeElementConfigurations;
    }

    /**
     * 设置所有的元素配置项，包括属性配置项、关联引用配置项、关联端配置项
     *
     * @param typeElementConfigurationList 所有的元素配置项，包括属性配置项、关联引用配置项、关联端配置项
     */
    @Override
    protected void setElementConfigurations(HashMap<String, TypeElementConfiguration> typeElementConfigurationList) {
        this.typeElementConfigurations = typeElementConfigurationList;
    }

    /**
     * 标识属性集合
     *
     * @return 标识属性集合
     */
    @Override
    protected List<String> getKeyAttributes() {
        return new ArrayList<>();
    }

    /**
     * 创建引用元素
     *
     * @param property 属性
     * @return 引用元素配置
     */
    @Override
    protected ITypeElementConfigurator createReferenceElement(Property property) {
        //复杂类型无需配置引用元素
        return null;
    }

    /**
     * 创建隐式关联型
     */
    @Override
    public void createImplicitAssociationConfiguration() {
        //复杂类型上无需创建隐式关联型
    }

    /**
     * 根据类型配置项中的元数据构建模型类型
     * 由派生类实现
     *
     * @param buildingModel 对象数据模型
     * @return 结构化类型
     */
    @Override
    StructuralType createReally(ObjectDataModel buildingModel) {
        //根据配置项数据创建模型对象并设值
        ComplexType structuralType;
        if (this.derivingFrom != null) {
            StructuralType derivingFrom = buildingModel.getStructuralType(this.derivingFrom);
            if (derivingFrom == null)
                throw new IllegalArgumentException(String.format("无法找到%s所声明的基类%s,需要先注册基类.", this.clrType.getName(), this.derivingFrom.getName()));
            structuralType = new ComplexType(this.clrType, derivingFrom);
        } else {
            structuralType = new ComplexType(this.clrType, null);
        }
        if (this.constructor == null) {
            try {
                Constructor<?> fConstructor = this.clrType.getConstructor();
                this.constructor = new DelegateConstructor<>(() -> {
                    try {
                        return fConstructor.newInstance();
                    } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                        throw new IllegalArgumentException("无法创建复杂类型对象" + e.getMessage(), e);
                    }
                });
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("无法获取" + this.clrType.getName() + "公开且无参的构造函数,请为其配置构造函数,", e);
            }
        }

        structuralType.setNewInstanceConstructor(this.newInstanceConstructor);

        structuralType.setConstructor(this.constructor);
        structuralType.setNamespace(this.namespace);
        structuralType.setName(this.name);
        return structuralType;
    }

    /**
     * 根据类型配置项中的元数据配置模型类型，被配置的模型类型已根据当前类型配置项实例生成并已注册到指定的模型中。
     * 注：调用方调用Create方法创建模型类型时，由于类型的元素还未创建，因此某些属性可能无法当场配置，可以等到类型元素创建（CreateElement被调用）完成
     * 时，调用本方法完成类型配置。
     *
     * @param model 对象数据模型
     */
    @Override
    void configure(ObjectDataModel model) {
        //复杂类型无需额外配置
    }

    /**
     * 根据名称获取元素配置器
     *
     * @param name 元素名称
     * @return 类型元素配置器
     */
    @Override
    public ITypeElementConfigurator getElement(String name) {
        return (ITypeElementConfigurator) this.typeElementConfigurations.get(name);
    }

    /**
     * 通过反射从CLR类型中收集元数据，生成类型配置项
     *
     * @param analyticPipeline 类型解析管道
     */
    @Override
    void reflectionModeling(ITypeAnalyzer analyticPipeline) {
        ITypeAnalyzer pipeLine = analyticPipeline;
        while (pipeLine != null) {
            pipeLine.configure(this.clrType, this);
            pipeLine = pipeLine.getNext();
        }
    }
}
