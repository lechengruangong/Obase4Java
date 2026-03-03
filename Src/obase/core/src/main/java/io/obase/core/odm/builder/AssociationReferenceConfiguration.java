/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联引用配置项.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-23 16:54:47
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.core.odm.builder;

import io.obase.common.FunctionWithNoArg;
import io.obase.core.common.Utils;
import io.obase.core.odm.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 关联引用配置项
 */
public abstract class AssociationReferenceConfiguration<TEntity>
        extends ReferenceElementConfiguration<TEntity,
        AssociationReferenceConfiguration<TEntity>> implements IAssociationReferenceConfigurator {

    /**
     * 用于获取关联类型的委托
     */
    protected final FunctionWithNoArg<Class<?>> associationTypeFunc;
    /**
     * 枚举元素类型
     */
    private final EElementType elementType;
    /**
     * 关联型对应的CLR类型
     */
    protected Class<?> associationType;

    /**
     * 左端名
     */
    protected String leftEnd;
    /**
     * 右端名
     */
    protected String rightEnd;
    /**
     * 聚合级别
     */
    private EAggregationLevel aggregationLevel = EAggregationLevel.None;

    /**
     * 创建类型元素配置项实例
     *
     * @param dataType          关联引用的关联类型
     * @param name              元素（属性、关联引用、关联端）名称
     * @param isMultiple        指示元素是否具有多重性，即其值是否为集合
     * @param typeConfiguration 创建当前元素配置项的类型配置项。
     */
    protected AssociationReferenceConfiguration(String name, Class<?> dataType, Boolean isMultiple, Class<TEntity> entityType, EntityTypeConfiguration<TEntity> typeConfiguration) {
        super(name, isMultiple, typeConfiguration, entityType);
        this.associationType = dataType;
        this.elementType = EElementType.AssociationReference;
        this.associationTypeFunc = null;
    }

    /**
     * 创建类型元素配置项实例
     *
     * @param name                元素（属性、关联引用、关联端）名称
     * @param isMultiple          指示元素是否具有多重性，即其值是否为集合
     * @param entityType          实体型类型
     * @param typeConfiguration   创建当前元素配置项的类型配置项
     * @param associationTypeFunc 获取关联型类型的委托
     */
    protected AssociationReferenceConfiguration(String name, Boolean isMultiple, Class<TEntity> entityType, EntityTypeConfiguration<TEntity> typeConfiguration, FunctionWithNoArg<Class<?>> associationTypeFunc) {
        super(name, isMultiple, typeConfiguration, entityType);
        this.associationTypeFunc = associationTypeFunc;
        this.elementType = EElementType.AssociationReference;
    }

    /**
     * 左端名
     *
     * @return 左端名
     */
    String getLeftEnd() {
        return this.leftEnd;
    }

    /**
     * 右端名
     *
     * @return 右端名
     */
    String getRightEnd() {
        return this.rightEnd;
    }

    /**
     * 关联型对应的CLR类型
     *
     * @return 关联型对应的CLR类型
     */
    Class<?> getAssociationType() {
        return this.associationType;
    }

    /**
     * 获取元素类型
     *
     * @return 元素类型
     */
    @Override
    public EElementType getElementType() {
        return this.elementType;
    }

    /**
     * 获取行为触发器，对于属性是指修改触发器，对于关联引用和关联端是加载触发器
     *
     * @return 行为触发器
     */
    @Override
    public List<IBehaviorTrigger> getBehaviorTriggers() {
        if (this.LoadingTriggers == null)
            this.LoadingTriggers = new ArrayList<>();
        return this.LoadingTriggers;
    }

    /**
     * 设置聚合级别(覆盖现有配置)
     *
     * @param level 级别
     */
    @Override
    public void hasAggregationLevelI(EAggregationLevel level) {
        this.hasAggregationLevelI(level, true);
    }

    /**
     * 设置聚合级别
     *
     * @param level    级别
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasAggregationLevelI(EAggregationLevel level, boolean override) {
        if (override)
            this.hasAggregationLevel(level);
        else {
            //是默认值
            if (this.aggregationLevel == EAggregationLevel.None)
                this.hasAggregationLevel(level);
        }
    }

    /**
     * 设置左端名(覆盖现有配置)
     *
     * @param leftEnd 左端名
     */
    @Override
    public void hasLeftEndI(String leftEnd) {
        this.hasLeftEndI(leftEnd, true);
    }

    /**
     * 设置左端名
     *
     * @param leftEnd  左端名
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasLeftEndI(String leftEnd, boolean override) {
        if (override)
            this.hasLeftEnd(leftEnd);
        else {
            if (Utils.getStringIsEmpty(this.leftEnd))
                this.hasLeftEnd(leftEnd);
        }
    }

    /**
     * 设置右端名(覆盖现有配置)
     *
     * @param rightEnd 右端名
     */
    @Override
    public void hasRightEndI(String rightEnd) {
        this.hasRightEndI(rightEnd, true);
    }

    /**
     * 设置右端名
     *
     * @param rightEnd 右端名
     * @param override 是否覆盖既有配置
     */
    @Override
    public void hasRightEndI(String rightEnd, boolean override) {
        if (override)
            this.hasRightEnd(rightEnd);
        else {
            if (Utils.getStringIsEmpty(this.rightEnd))
                this.hasRightEnd(rightEnd);
        }
    }

    /**
     * 设置聚合级别
     *
     * @param level 聚合级别
     * @return 自身
     */
    public AssociationReferenceConfiguration<TEntity> hasAggregationLevel(EAggregationLevel level) {
        this.aggregationLevel = level;
        return this;
    }

    /**
     * 设置左端名 即关联引用本端
     *
     * @param leftEnd 左端名
     * @return 自身
     */
    AssociationReferenceConfiguration<TEntity> hasLeftEnd(String leftEnd) {
        this.leftEnd = leftEnd;
        return this;
    }

    /**
     * 设置右端名 即关联引用对端
     *
     * @param rightEnd 左端名
     * @return 自身
     */
    AssociationReferenceConfiguration<TEntity> hasRightEnd(String rightEnd) {
        this.rightEnd = rightEnd;
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

        if (this.associationType == null)
            throw new IllegalArgumentException("未能获取关联型类型.");


        AssociationType associationType = model.getAssociationType(this.associationType);

        AssociationReference ass = new AssociationReference(this.getName(), associationType, this.leftEnd, this.rightEnd);

        ass.setAggregationLevel(this.aggregationLevel);
        ass.setEnableLazyLoading(this.enableLazyLoading);
        ass.setLoadingTriggers(this.LoadingTriggers);
        ass.setValueGetter(this.getValueGetter());
        ass.setValueSetter(this.getValueSetter());
        ass.setLoadingPriority(this.loadingPriority);
        ass.setIsMultiple(this.isMultiple);

        return ass;
    }
}
