/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：适用于隐式关联的关联引用配置器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-23 17:26:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder.implicitAssociationConfigor;

import io.obase.common.FunctionWithOneArg;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.odm.AssociationType;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.StructuralType;
import io.obase.core.odm.TypeElement;
import io.obase.core.odm.builder.EntityTypeConfiguration;
import io.obase.core.odm.builder.StructuralTypeConfiguration;

/**
 * 适用于隐式关联的关联引用配置器
 *
 * @param <TEntity>   关联引用所属的实体类型
 * @param <TReferred> 被引对象组成的元组的类型 被引对象是指关联引用指向的一个或一组对象，如果关联引用是多重性的，它是指其中的一个或一组
 */
public class AssociationReferenceConfiguration<TEntity, TReferred> extends io.obase.core.odm.builder.AssociationReferenceConfiguration<TEntity> {

    /**
     * 隐式关联建造器
     */
    private final AssociationConfiguratorBuilder builder;

    /**
     * 关联引用所在关联端在关联型上的索引号（从1开始计数）
     */
    private final byte associationEndIndex;

    /**
     * 元组标准化函数及其反函数
     */
    private ITupleStandardizer tupleStandardizer;

    /**
     * 创建类型元素配置项实例
     *
     * @param name                     关联引用名称
     * @param isMultiple               指示关联引用是否具有多重性，即其值是否为集合
     * @param endIndex                 关联引用所在关联端在关联型上的索引号（从1开始计数）
     * @param entityType               端的实体型类型
     * @param associationConfigBuilder 关联配置器建造器
     */
    public AssociationReferenceConfiguration(String name, boolean isMultiple, byte endIndex, Class<TEntity> entityType, AssociationConfiguratorBuilder associationConfigBuilder) {
        super(name, isMultiple, entityType, (EntityTypeConfiguration<TEntity>) associationConfigBuilder.getModelBuilder().findConfiguration(entityType), associationConfigBuilder::getAssociationType);
        this.builder = associationConfigBuilder;
        this.associationEndIndex = endIndex;
    }

    /**
     * 设置元组标准化函数及其反函数
     *
     * @param standardizingFunc 元组标准化函数
     * @param revertingFunc     标准化函数的反函数
     * @return 自身
     */
    public AssociationReferenceConfiguration<TEntity, TReferred> hasTupleStandardizer(FunctionWithOneArg<TReferred, Object> standardizingFunc, FunctionWithOneArg<Object, TReferred> revertingFunc) {
        this.tupleStandardizer = new DelegateTupleStandardizer<>(standardizingFunc, revertingFunc);
        return this;
    }

    /**
     * 根据元素配置项包含的元数据信息创建元素实例
     * 本方法由派生类实现
     *
     * @param model 对象数据模型
     * @return 类型元素
     */
    @Override
    public TypeElement createReally(ObjectDataModel model) {
        //首先检查_associationType是否为空，如果是则调用_associationTypeFunc委托获取关联类型。成功获取后调用基础实现。
        if (this.associationType == null) {
            if (this.associationTypeFunc == null)
                throw new IllegalArgumentException("关联型和关联型获取委托不能同时为空.");

            this.associationType = this.associationTypeFunc.invoke();
        }

        EntityTypeConfiguration<TEntity> entityTypeConfiguration = (EntityTypeConfiguration<TEntity>) this.upwardI();
        Property property = Utils.getProperty(entityTypeConfiguration.getClrType(), this.getName());
        //查找关联型配置
        StructuralTypeConfiguration<?> assConfig = this.builder.getModelBuilder().findConfiguration(this.associationType);
        StructuralType assType = assConfig.getCreatedType();
        //构造元组标准化处理器
        if (this.tupleStandardizer == null) {
            if (this.builder.getEndCount() == 2)
                this.tupleStandardizer = new TwoAssociationTupleStandardizer();
            else
                this.tupleStandardizer = new MultiAssociationTupleStandardizer(property);
        }

        //构造包装器
        AssociationReferenceValueWrapper wrapper = new AssociationReferenceValueWrapper(this.associationEndIndex, (AssociationType) assType, this.getValueGetter(), this.getValueSetter(),
                this.builder.getEndCount() != 2, this.isMultiple, this.tupleStandardizer);
        //替换
        this.setValueGetter(wrapper);
        this.setValueSetter(wrapper);

        return super.createReally(model);
    }
}
