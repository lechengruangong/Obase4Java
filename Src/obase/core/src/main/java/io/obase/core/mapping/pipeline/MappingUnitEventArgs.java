package io.obase.core.mapping.pipeline;

import io.obase.core.saving.EObjectStatus;
import io.obase.core.saving.MappingUnit;

import java.util.EventObject;

/**
 * 与映射单元相关的事件的数据类
 */
public abstract class MappingUnitEventArgs extends EventObject {

    /**
     * 映射单元主对象状态
     */
    private final EObjectStatus hostObjectStatus;

    /**
     * 映射单元
     */
    private final MappingUnit mappingUnit;

    /**
     * 创建MappingUnitEventArgs实例，并指定映射单元。
     *
     * @param source           源
     * @param mappingUnit      映射单元
     * @param hostObjectStatus 映射单元主对象状态
     */
    protected MappingUnitEventArgs(Object source, MappingUnit mappingUnit, EObjectStatus hostObjectStatus) {
        super(source);
        this.hostObjectStatus = hostObjectStatus;
        this.mappingUnit = mappingUnit;
    }

    /**
     * 获取映射单元
     *
     * @return 映射单元
     */
    public MappingUnit getMappingUnit() {
        return this.mappingUnit;
    }

    /**
     * 映射单元主对象状态
     *
     * @return 主对象状态
     */
    public EObjectStatus getHostObjectStatus() {
        return this.hostObjectStatus;
    }
}

