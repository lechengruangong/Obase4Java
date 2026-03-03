/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：逻辑删除类型解析器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 10:42:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.logical.deletion;

import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.odm.builder.*;

import java.util.List;

/**
 * 类型解析器
 */
public class TypeAnalyzer implements ITypeAnalyzer {

    /**
     * 获取类型解析管道中的下一个解析器
     */
    private final ITypeAnalyzer next;

    /**
     * 构造类型解析器
     *
     * @param next 下一个解析器
     */
    public TypeAnalyzer(ITypeAnalyzer next) {
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
        if (configurator instanceof IObjectTypeConfigurator) {
            IObjectTypeConfigurator objectTypeConfigurator = (IObjectTypeConfigurator) configurator;
            this.configure(type, objectTypeConfigurator);
        }

        //复杂类型无配置
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
        this.configExt(type, configurator);
    }

    /**
     * 配置指定的关联型
     *
     * @param type         要配置的关联型
     * @param configurator 该关联型的配置器
     */
    @Override
    public void configure(Class<?> type, IAssociationTypeConfigurator configurator) {
        this.configExt(type, configurator);
    }

    /**
     * 配置具体的拓展
     *
     * @param type         类型
     * @param configurator 配置器
     */
    private void configExt(Class<?> type, IObjectTypeConfigurator configurator) {
        LogicDeletionAttribute logicDeletionAttribute = type.getAnnotation(LogicDeletionAttribute.class);
        if (logicDeletionAttribute != null) {
            LogicDeletionExtensionConfiguration<?> configuration = (LogicDeletionExtensionConfiguration<?>) configurator.hasExtensionI(LogicDeletionExtensionConfiguration.class);
            configuration.hasDeletionField(logicDeletionAttribute.deletionField());
        }

        List<Property> properties = ObaseIntrospector.getObaseBeanProperties(type);

        for (Property propertyInfo : properties) {
            LogicDeletionMarkAttribute logicDeletionMarkAttribute = Utils.getAnnotation(propertyInfo, LogicDeletionMarkAttribute.class);
            if (logicDeletionMarkAttribute != null) {
                LogicDeletionExtensionConfiguration<?> configuration = (LogicDeletionExtensionConfiguration<?>) configurator.hasExtensionI(LogicDeletionExtensionConfiguration.class);
                configuration.hasDeletionMark(propertyInfo.getName());
            }
        }
    }
}
