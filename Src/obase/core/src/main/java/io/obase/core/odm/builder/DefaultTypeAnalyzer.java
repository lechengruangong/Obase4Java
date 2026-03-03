/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：默认的类型解析器,设置类型的映射表,主键,构造器等配置.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-24 11:19:42
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.odm.builder.implicitAssociationConfigor.EEndMulti;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认的类型解析器
 */
public class DefaultTypeAnalyzer implements ITypeAnalyzer {

    /**
     * 存储从程序集解析类型过程中应忽略的类型
     */
    private final HashSet<Class<?>> ignoredTypes;

    /**
     * 建模器
     */
    private final ModelBuilder modelBuilder;

    /**
     * 下一节
     */
    private final ITypeAnalyzer next;

    /**
     * 构造默认的类型解析器
     *
     * @param ignoredTypes 要忽略的类型
     * @param modelBuilder 建模器
     * @param next         下一节
     */
    public DefaultTypeAnalyzer(HashSet<Class<?>> ignoredTypes, ModelBuilder modelBuilder, ITypeAnalyzer next) {
        this.ignoredTypes = ignoredTypes;
        this.modelBuilder = modelBuilder;
        this.next = next;
    }

    /**
     * 获取类型解析管道中的下一个解析器
     *
     * @return 下一个类型解析器
     */
    @Override
    public ITypeAnalyzer getNext() {
        return this.next;
    }

    /**
     * 配置指定的类型
     *
     * @param type         要配置的类型
     * @param configurator 该类型的配置器
     */
    @Override
    public void configure(Class<?> type, IStructuralTypeConfigurator configurator) {
        //忽略的类型不参与推断
        if (this.ignoredTypes.contains(type))
            return;

        //对于实体和显式关联，如果存在protected internal的构造方法（java版参照处理），推断为构造器；
        //如果不存在，但存在无参构造方法，推断为构造器；如果也不存在，就用第一个。

        Constructor<?>[] constructors = type.getDeclaredConstructors();

        Constructor<?> constructor = null;

        if (constructors.length > 0) {
            for (Constructor<?> c : constructors) {
                //有参数的 不进行推断
                if (c.getParameterCount() > 0)
                    continue;
                constructor = c;
            }
        }
        //取第一个
        if (constructor == null) {
            constructor = Arrays.stream(constructors).filter(p -> p.getParameterCount() == 0).findFirst().orElse(null);
        }

        //能找到符合推断的
        if (constructor != null)
            //配置 应当是无参的 直接End
            configurator.hasConstructorI(constructor, false).endI();


        //继续按照具体类型配置
        //对象类型
        if (configurator instanceof IObjectTypeConfigurator) {
            IObjectTypeConfigurator objectTypeConfigurator = (IObjectTypeConfigurator) configurator;
            this.configure(type, objectTypeConfigurator);
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
        //忽略的类型不参与推断
        if (this.ignoredTypes.contains(type))
            return;

        //继续按照具体类型配置
        //关联型或者实体型
        if (configurator instanceof IEntityTypeConfigurator) {
            IEntityTypeConfigurator entityTypeConfigurator = (IEntityTypeConfigurator) configurator;
            //将简单名称推断为表名
            configurator.toTableI(type.getSimpleName(), false);
            //继续配置
            this.configure(type, entityTypeConfigurator);
        } else if (configurator instanceof IAssociationTypeConfigurator) {
            IAssociationTypeConfigurator associationTypeConfigurator = (IAssociationTypeConfigurator) configurator;
            //显式关联型
            if (!ImplicitAssociation.class.isAssignableFrom(associationTypeConfigurator.getAssociationTypeI())) {
                //将简单名称推断为表名
                configurator.toTableI(type.getSimpleName(), false);
            } else {
                IAssociationEndConfigurator[] ends = associationTypeConfigurator.getAssociationEndsI();

                //先根据伴随端进行配置
                String companionEndTargetTable = "";
                //查找伴随端
                for (IAssociationEndConfigurator endConfig : ends) {
                    if (endConfig instanceof io.obase.core.odm.builder.implicitAssociationConfigor.AssociationEndConfiguration) {
                        io.obase.core.odm.builder.implicitAssociationConfigor.AssociationEndConfiguration endConfiguration = (io.obase.core.odm.builder.implicitAssociationConfigor.AssociationEndConfiguration) endConfig;
                        if (endConfiguration.getIsCompanionEnd())
                            companionEndTargetTable = this.getEntityTargetTable(endConfiguration.getEntityTypeI());
                    }

                }
                if (!Utils.getStringIsEmpty(companionEndTargetTable))
                    configurator.toTableI(companionEndTargetTable, false);

                //如果是两方关联 进行如下推断
                //A和B有关联
                //1. A 上 B 一对一 B上A 一对一 无法推断
                //2. A 上 B 一对一 B上A 一对多 关联表设为A
                //3. A 上 B 一对一 B上A 没有 关联表设为A
                //4. A 上 B 一对多 B上A 一对多 关联表设为 A + Ass + B
                //5. A 上 B 一对多 B上A 没有 关联表设为B
                configurator.toTableI(
                        ends.length == 2
                                ? this.GetTargetTable(ends)
                                //多方关联 直接推断为独立表
                                //MultiAss + 各个端Clr类型的名称
                                : "MultiAss" + Arrays.stream(ends).map(p -> p.getEntityTypeI().getSimpleName()).collect(Collectors.joining("")),
                        false);
            }
            //继续配置
            this.configure(type, associationTypeConfigurator);
        } else {
            throw new IllegalArgumentException("未知的配置器类型");
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
        //忽略的类型不参与推断
        if (this.ignoredTypes.contains(type))
            return;
        ObjectReferencePack<List<Property>> ids = new ObjectReferencePack<>();
        //推断标识属性
        if (Utils.existIdentity(type, ids))
            for (Property id : ids.realValue)
                configurator.hasKeyAttributeI(id.getName());
    }

    /**
     * 配置指定的关联型
     *
     * @param type         要配置的关联型
     * @param configurator 该关联型的配置器
     */
    @Override
    public void configure(Class<?> type, IAssociationTypeConfigurator configurator) {
        //忽略的类型不参与推断
        if (this.ignoredTypes.contains(type))
            return;
        //配置为显式关联
        configurator.setIsVisibleI(true, false);
    }

    /**
     * 根据关联端上的引用推断表名
     *
     * @param ends 关联端集合
     * @return 表名
     */
    private String GetTargetTable(IAssociationEndConfigurator[] ends) {
        //此刻 一定是两个端
        IAssociationEndConfigurator end1 = ends[0];
        IAssociationEndConfigurator end2 = ends[1];
        //分别获取当前端上另外一端的引用情况
        EEndMulti end1Multi = this.getEndMulti(end1, end2);
        EEndMulti end2Multi = this.getEndMulti(end2, end1);

        //End1上End2 的引用情况
        if (end1Multi == EEndMulti.None) {
            //end1上没有End2的引用
            switch (end2Multi) {
                case None:
                    //都没有 可能是继承来的等原因 不处理即可
                    return null;
                case Single:
                    //end1上没有 end2上有一对一 设为end2的表
                    return this.getEntityTargetTable(end2.getEntityTypeI());
                case Multi:
                    //end1上没有 end2上有一对多 设为end1的表
                    return this.getEntityTargetTable(end1.getEntityTypeI());
                default:
                    throw new IllegalArgumentException("未知的关联端引用类型" + end2Multi);
            }
        }

        if (end1Multi == EEndMulti.Single) {
            //end1上有End2的引用 一对一
            switch (end2Multi) {
                case None:
                    //end1上一对一 end2上没有 设为end1的表
                    return this.getEntityTargetTable(end1.getEntityTypeI());
                case Single:
                    //end1上一对一 end2上有一对一 无法推断
                    return "";
                case Multi:
                    //end1上一对一 end2上有一对多 设为end1的表
                    return this.getEntityTargetTable(end1.getEntityTypeI());
                default:
                    throw new IllegalArgumentException("未知的关联端引用类型" + end2Multi);
            }
        }

        if (end1Multi == EEndMulti.Multi) {
            //end1上有End2的引用 一对多
            switch (end2Multi) {
                case None:
                    //end1上一对多 end2上没有 设为end2的表
                    return this.getEntityTargetTable(end2.getEntityTypeI());
                case Multi:
                    //end1上一对多 end2上有一对多 设为独立关联表
                    return end1.getEntityTypeI().getSimpleName() + "Ass" + end2.getEntityTypeI().getSimpleName();
                case Single:
                    //end1上一对多 end2上有一对一 设为end2的表
                    return this.getEntityTargetTable(end2.getEntityTypeI());
                default:
                    throw new IllegalArgumentException("未知的关联端引用类型" + end2Multi);
            }
        }

        return "";
    }

    /**
     * 获取某端上另外一端引用的多重性
     *
     * @param end1 当前端
     * @param end2 另外一端
     * @return 某端上另外一端引用的多重性
     */
    private EEndMulti getEndMulti(IAssociationEndConfigurator end1, IAssociationEndConfigurator end2) {
        Class<?> endType = end1.getEntityTypeI();
        StructuralTypeConfiguration<?> endConfigType = this.modelBuilder.findConfiguration(endType);
        List<String> ignoreList = endConfigType.getIgnoreList();

        List<Property> properties = ObaseIntrospector.getObaseBeanProperties(endType);

        //检查每个属性
        for (Property property : properties) {

            if (ignoreList.stream().anyMatch(p -> p.equalsIgnoreCase(property.getName())))
                continue;

            ObjectReferencePack<Class<?>> type = new ObjectReferencePack<>();

            boolean isMulti = Utils.getIsMultiple(property, type);
            if (type.realValue.equals(end2.getEntityTypeI()) || end2.getEntityTypeI().isAssignableFrom(type.realValue)) {
                return isMulti ? EEndMulti.Multi : EEndMulti.Single;
            }

        }

        return EEndMulti.None;
    }

    /**
     * 获取实体型的映射表
     *
     * @param entityType 实体型
     * @return 实体型的映射表
     */
    private String getEntityTargetTable(Class<?> entityType) {
        StructuralTypeConfiguration<?> structuralTypeConfiguration = this.modelBuilder.findConfiguration(entityType);
        if (structuralTypeConfiguration instanceof IEntityTypeConfigurator) {
            IEntityTypeConfigurator configurator = (IEntityTypeConfigurator) structuralTypeConfiguration;
            return configurator.getTargetTableI();
        }
        return "";
    }
}
