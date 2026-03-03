/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：默认的类型成员解析管道,解析属性访问器为属性,关联引用,关联端等.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-22 14:36:50
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.odm.*;
import io.obase.core.odm.builder.implicitAssociationConfigor.AssociationConfiguratorBuilder;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认的类型成员解析管道
 * 实现反射建模逻辑
 */
public class DefaultTypeMemberAnalyzer implements ITypeMemberAnalyzer {

    /**
     * 建模器
     */
    private final ModelBuilder modelBuilder;

    /**
     * 类型成员解析管道中的下一个解析器
     */
    private final ITypeMemberAnalyzer next;

    /**
     * 构造默认的类型成员解析管道
     *
     * @param modelBuilder 建模器
     * @param next         类型成员解析管道中的下一个解析器
     */
    public DefaultTypeMemberAnalyzer(ModelBuilder modelBuilder, ITypeMemberAnalyzer next) {
        this.modelBuilder = modelBuilder;
        this.next = next;
    }

    /**
     * 为类型元素配置取值器和设值器
     *
     * @param memberInfo   属性
     * @param configurator 类型元素配置
     */
    private static void configureValueGetterAndSetter(Property memberInfo, ITypeElementConfigurator configurator) {
        //取值器
        //取值方法是可读还是公开的
        if (memberInfo.getGetterMethod() != null) {
            configurator.hasValueGetterI(memberInfo.getGetterMethod(), false);
        }

        //设值器
        //设值方法是可读还是公开的
        if (memberInfo.getSetterMethod() != null) {
            //有公开的设值方法
            Class<?> parType = memberInfo.getPropertyType();

            EValueSettingMode model = EValueSettingMode.Assignment;
            if (parType != String.class && Iterable.class.isAssignableFrom(parType))
                model = EValueSettingMode.Appending;

            configurator.hasValueSetterI(ValueSetter.create(memberInfo.getSetterMethod(), model), false);
        } else {
            try {
                //找set+属性名
                Method method = memberInfo.getGetterMethod().getDeclaringClass().getDeclaredMethod("set" + memberInfo.getName(), memberInfo.getPropertyType());
                configurator.hasValueSetterI(new MethodValueSetter(method), false);
            } catch (NoSuchMethodException ignored) {
                //没有 忽略掉
            }
        }
    }

    /**
     * 获取类型成员解析管道中的下一个解析器
     *
     * @return 下一个成员解析器
     */
    @Override
    public ITypeMemberAnalyzer getNext() {
        return this.next;
    }

    /**
     * 判定指定的类型成员是否将作为类型元素
     *
     * @param memberInfo 类型成员
     * @param name       如果作为元素，返回元素名称 取元素[0]
     * @return 是否将作为类型元素
     */
    @Override
    public boolean asElement(Property memberInfo, ObjectReferencePack<String> name) {
        //默认的判断条件
        boolean result = PrimitiveType.isObasePrimitive(memberInfo.getPropertyType());
        name.realValue = null;
        return result;
    }

    /**
     * 基于指定的类型成员，配置指定的元素
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    @Override
    public void configure(Property memberInfo, ITypeElementConfigurator configurator) {
        if (configurator instanceof IReferenceElementConfigurator) {
            IReferenceElementConfigurator referenceElementConfigurator = (IReferenceElementConfigurator) configurator;
            this.configure(memberInfo, referenceElementConfigurator);
        }

        if (configurator instanceof IAttributeConfigurator) {
            IAttributeConfigurator attributeConfigurator = (IAttributeConfigurator) configurator;
            this.configure(memberInfo, attributeConfigurator);
        }
    }

    /**
     * 基于指定的类型成员，配置指定的属性
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    @Override
    public void configure(Property memberInfo, IAttributeConfigurator configurator) {
        //映射字段
        configurator.toFieldI(memberInfo.getName(), false);
        //取值器和设值器
        configureValueGetterAndSetter(memberInfo, configurator);

    }

    /**
     * 基于指定的类型成员，配置指定的引用元素
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    @Override
    public void configure(Property memberInfo, IReferenceElementConfigurator configurator) {
        if (configurator instanceof IAssociationEndConfigurator) {
            IAssociationEndConfigurator associationEndConfigurator = (IAssociationEndConfigurator) configurator;
            this.configure(memberInfo, associationEndConfigurator);
        }

        if (configurator instanceof IAssociationReferenceConfigurator) {
            IAssociationReferenceConfigurator associationReferenceConfigurator = (IAssociationReferenceConfigurator) configurator;
            this.configure(memberInfo, associationReferenceConfigurator);
        }
    }

    /**
     * 基于指定的类型成员，配置指定的关联引用
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    @Override
    public void configure(Property memberInfo, IAssociationReferenceConfigurator configurator) {
        ObjectReferencePack<Class<?>> type = new ObjectReferencePack<>();

        //属性为集合类型
        Utils.getIsMultiple(memberInfo, type);
        //是否是元组
        boolean isTuple = Utils.isTuple(type.realValue);

        if (configurator.upwardI() instanceof StructuralTypeConfiguration) {
            StructuralTypeConfiguration<?> structuralTypeConfiguration = (StructuralTypeConfiguration<?>) configurator.upwardI();

            ModelBuilder modelBuilder = structuralTypeConfiguration.getModelBuilder();
            //尝试按照显式进行查询
            StructuralTypeConfiguration<?> obvious = modelBuilder.findConfiguration(type.realValue);
            //不为空 则查询是否为关联型配置
            if (obvious instanceof AssociationTypeConfiguration) {

                Class<?> firstType = null;
                IAssociationEndConfigurator firstEnd = Arrays.stream(((IAssociationTypeConfigurator) obvious).getAssociationEndsI()).findFirst().orElse(null);
                if (firstEnd != null)
                    firstType = firstEnd.getEntityTypeI();

                Class<?> finalFirstType = firstType;
                if (firstType != null && Arrays.stream(((IAssociationTypeConfigurator) obvious).getAssociationEndsI()).allMatch(p -> p.getEntityTypeI().equals(finalFirstType))) {
                    //如果是显式自关联 不配置 需要用户配置
                } else {
                    //查找显式关联型的各个属性
                    List<Property> obviousProps = ObaseIntrospector.getObaseBeanProperties(obvious.getClrType());
                    //只保留与关联端类型相同的属性
                    List<Class<?>> endType = Arrays.stream(((IAssociationTypeConfigurator) obvious).getAssociationEndsI()).map(IAssociationEndConfigurator::getEntityTypeI).collect(Collectors.toList());

                    obviousProps = obviousProps.stream().filter(p -> endType.contains(p.getPropertyType())).collect(Collectors.toList());
                    //查找显式关联型的各个属性配置左端右端
                    this.configLeftAndRight(configurator, obviousProps, memberInfo);
                }
            }

            //没找到显示关联型
            //按照隐式关联型查询 引用的类型是否被配置为实体型
            //不是元组 按照普通的两方关联处理
            Class<?>[] endTypes = new Class<?>[0];

            if (!isTuple) {
                //查询属性类型模型配置项
                StructuralTypeConfiguration<?> implicitEntityConfig = this.modelBuilder.findConfiguration(type.realValue);
                if (implicitEntityConfig instanceof IEntityTypeConfigurator) {
                    //提取关联端
                    endTypes = new Class<?>[]{structuralTypeConfiguration.clrType, type.realValue};
                }

            }
            //是元组 要分拆为多方关联
            else {
                //如果是元组 取出所有类型参数判断
                //元组泛型参数
                Class<?>[] tupleTypeList = memberInfo.getPropertyElementType();
                List<StructuralTypeConfiguration<?>> configs = Arrays.stream(tupleTypeList).map(this.modelBuilder::findConfiguration).collect(Collectors.toList());
                //都是实体型 才进入推断
                if (configs.stream().allMatch(p -> p instanceof IEntityTypeConfigurator)) {
                    //加入自己这一端的类型
                    List<Class<?>> endTypesList = new ArrayList<>();
                    endTypesList.add(structuralTypeConfiguration.clrType);
                    //取出另外的端类型
                    endTypesList.addAll(Arrays.asList(tupleTypeList));
                    //提取关联端
                    endTypes = endTypesList.toArray(new Class<?>[0]);
                }
            }

            String endTags = AssociationConfiguratorBuilder.generateEndsTag(endTypes, this.modelBuilder);
            //Tag不是空
            if (!Utils.getStringIsEmpty(endTags)) {
                //配置隐式关联关联引用
                //查找隐式关联型建造器
                AssociationConfiguratorBuilder implicitAssociationConfig = this.modelBuilder.findImplicitAssociationConfigurationBuilder(endTags);
                if (implicitAssociationConfig != null) {
                    Class<?> firstType = null;
                    IAssociationEndConfigurator firstEnd = implicitAssociationConfig.getEndConfigurations().stream().findFirst().orElse(null);
                    if (firstEnd != null)
                        firstType = firstEnd.getEntityTypeI();
                    Class<?> finalFirstType = firstType;
                    if (firstType != null && implicitAssociationConfig.getEndConfigurations().stream().allMatch(p -> p.getEntityTypeI().equals(finalFirstType))) {
                        //如果是显式自关联 不配置 需要用户配置
                    } else {
                        //隐式关联的各个属性 配置左端右端
                        this.configLeftAndRight(configurator, ObaseIntrospector.getObaseBeanProperties(implicitAssociationConfig.getAssociationType()), memberInfo);
                    }
                }
            }

            //取值器和设值器
            configureValueGetterAndSetter(memberInfo, configurator);

            //追加触发器
            if (configurator instanceof TypeElementConfiguration) {
                TypeElementConfiguration typeElement = (TypeElementConfiguration) configurator;
                if (typeElement.getBehaviorTriggers().size() == 0 && memberInfo.getGetterMethod() != null) {
                    //启用了延迟加载才配置触发器
                    if (configurator.getEnableLazyLoadingI()) {
                        configurator.hasLoadingTriggerI(memberInfo.getGetterMethod(), false);
                    }
                }

            }
        }
    }

    /**
     * 基于指定的类型成员，配置指定的关联端
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    @Override
    public void configure(Property memberInfo, IAssociationEndConfigurator configurator) {
        //默认配置关联端取值器和设值器
        configureValueGetterAndSetter(memberInfo, configurator);

        //追加触发器
        if (configurator instanceof TypeElementConfiguration) {
            TypeElementConfiguration typeElement = (TypeElementConfiguration) configurator;
            if ((typeElement.getBehaviorTriggers().stream().anyMatch(p -> !p.getUniqueId().equalsIgnoreCase(memberInfo.getName())) || typeElement.getBehaviorTriggers().size() == 0) &&
                    memberInfo.getGetterMethod() != null) {
                //启用了延迟加载才配置触发器
                if (configurator.getEnableLazyLoadingI()) {
                    //默认属性触发器（用以延时加载，访问属性的get的访问器时触发）
                    configurator.hasLoadingTriggerI(new PropertyGetTrigger<>(memberInfo), false);
                }
            }
        }

        if (!(this.modelBuilder.findConfiguration(configurator.getEntityTypeI()) instanceof IEntityTypeConfigurator))
            throw new IllegalArgumentException(configurator.getEntityTypeI().getName() + "未配置为实体型");

        //进入宿主 关联型
        if (configurator.upwardI() instanceof IAssociationTypeConfigurator) {
            IEntityTypeConfigurator entityTypeConfigurator = (IEntityTypeConfigurator) this.modelBuilder.findConfiguration(configurator.getEntityTypeI());
            IAssociationTypeConfigurator associationTypeConfigurator = (IAssociationTypeConfigurator) configurator.upwardI();
            //处理每一个键
            String[] keyAttrs = entityTypeConfigurator.getKeyAttributesFiledI();

            for (String attr : keyAttrs) {
                String targetField;
                //不在同一映射表
                if (!entityTypeConfigurator.getTargetTableI().equals(associationTypeConfigurator.getTargetTableI())) {
                    //主键参考列表
                    List<String> list = new ArrayList<>();
                    list.add("code");
                    list.add("id");
                    //自身或者是类名加自身
                    targetField = list.contains(attr.toLowerCase())
                            ? configurator.getEntityTypeI().getSimpleName() + StringUtils.capitalize(attr) : StringUtils.capitalize(attr);
                }
                //在同一映射表
                else {
                    targetField = attr;
                }

                configurator.hasMappingI(attr, targetField, false);
            }
        }
    }

    /**
     * 基于指定的类型成员，配置指定的类型或其所属的元素
     *
     * @param memberInfo   要解析的成员
     * @param configurator 用于配置类型元素的配置器
     */
    @Override
    public void configure(Property memberInfo, IStructuralTypeConfigurator configurator) {
        //默认配置器无作为其他元素进行配置
    }

    /**
     * 配置左端和右端
     *
     * @param configurator 关联引用配置器
     * @param props        关联型属性集合
     * @param propertyInfo 当前要配置的属性
     */
    private void configLeftAndRight(IAssociationReferenceConfigurator configurator, List<Property> props, Property propertyInfo) {
        //与当前属性所在类的类型相同 推断为左端
        Property leftEnd = props.stream().filter(p -> p.getPropertyType().equals(propertyInfo.getGetterMethod().getDeclaringClass()) || propertyInfo.getGetterMethod().getDeclaringClass().isAssignableFrom(p.getPropertyType())).findFirst().orElse(null);
        String leftEndName = leftEnd == null ? "" : leftEnd.getName();
        if (!leftEndName.isEmpty())
            configurator.hasLeftEndI(StringUtils.capitalize(leftEndName), false);
        //此处不需要推断右端 而是在默认的补充配置中根据关联端推断右端
    }
}
