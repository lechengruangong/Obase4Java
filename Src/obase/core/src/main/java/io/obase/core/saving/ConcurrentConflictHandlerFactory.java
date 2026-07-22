/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：为所有的冲突处理器工厂提供基础实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 16:03:57
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.common.FunctionWithTwoArgs;
import io.obase.core.IStorageProvider;
import io.obase.core.odm.EConcurrentConflictHandlingStrategy;
import io.obase.core.odm.IGetAttributeValue;
import io.obase.core.odm.ObjectDataModel;

/**
 * 为所有的冲突处理器工厂提供基础实现
 */
public abstract class ConcurrentConflictHandlerFactory {

    /**
     * 用于探测属性值是否已更改的委托
     */
    private FunctionWithTwoArgs<Object, String, Boolean> attributeHasChanged;

    /**
     * 用于获取属性原值的委托
     */
    private IGetAttributeValue attributeOriginalValueGetter;

    /**
     * 对象数据模型
     */
    private ObjectDataModel model;

    /**
     * 在冲突处理过程中实施持久化的存储提供程序
     */
    private IStorageProvider storageProvider;

    /**
     * 内部异常
     */
    private Exception innerException;

    /**
     * 根据指定的并发冲突处理策略选取相应的冲突处理器工厂。
     * 如果指定的处理策略为“忽略”，返回null。
     *
     * @param strategy 冲突处理策略
     * @return 冲突处理器工厂
     */
    public static ConcurrentConflictHandlerFactory chooseFactory(EConcurrentConflictHandlingStrategy strategy) {
        ConcurrentConflictHandlerFactory factory = null;
        switch (strategy) {

            case Ignore:
                break;
            case ThrowException:
                factory = new ThrowExceptionConflictHandlerFactory();
                break;
            case Overwrite:
                factory = new OverwriteConflictHandlerFactory();
                break;
            case Combine:
                factory = new CombineConflictHandlerFactory();
                break;
            case Reconstruct:
                factory = new ReconstructConflictHandlerFactory();
                break;
        }

        return factory;
    }

    /**
     * 获取对象数据模型
     *
     * @return 对象数据模型
     */
    public ObjectDataModel getModel() {
        return this.model;
    }

    /**
     * 设置对象数据模型
     *
     * @param model 对象数据模型
     */
    public void setModel(ObjectDataModel model) {
        this.model = model;
    }

    /**
     * 获取用于探测属性值是否已更改的委托
     *
     * @return 用于探测属性值是否已更改的委托
     */
    public FunctionWithTwoArgs<Object, String, Boolean> getAttributeHasChanged() {
        return this.attributeHasChanged;
    }

    /**
     * 设置用于探测属性值是否已更改的委托
     *
     * @param attributeHasChanged 用于探测属性值是否已更改的委托
     */
    public void setAttributeHasChanged(FunctionWithTwoArgs<Object, String, Boolean> attributeHasChanged) {
        this.attributeHasChanged = attributeHasChanged;
    }

    /**
     * 获取用于获取属性原值的委托
     *
     * @return 用于获取属性原值的委托
     */
    public IGetAttributeValue getAttributeOriginalValueGetter() {
        return this.attributeOriginalValueGetter;
    }

    /**
     * 设置用于获取属性原值的委托
     *
     * @param attributeOriginalValueGetter 用于获取属性原值的委托
     */
    public void setAttributeOriginalValueGetter(IGetAttributeValue attributeOriginalValueGetter) {
        this.attributeOriginalValueGetter = attributeOriginalValueGetter;
    }

    /**
     * 在冲突处理过程中实施持久化的存储提供程序
     *
     * @return 冲突处理过程中实施持久化的存储提供程序
     */
    public IStorageProvider getStorageProvider() {
        return this.storageProvider;
    }

    /**
     * 在冲突处理过程中实施持久化的存储提供程序
     *
     * @param storageProvider 冲突处理过程中实施持久化的存储提供程序
     */
    public void setStorageProvider(IStorageProvider storageProvider) {
        this.storageProvider = storageProvider;
    }

    /**
     * 获取内部异常
     *
     * @return 内部异常
     */
    public Exception getInnerException() {
        return this.innerException;
    }

    /**
     * 设置内部异常
     *
     * @param innerException 内部异常
     */
    public void setInnerException(Exception innerException) {
        this.innerException = innerException;
    }

    /**
     * 创建“重复创建”冲突的处理器
     *
     * @return “重复创建”冲突的处理器
     */
    public abstract IRepeatCreationHandler createRepeatCreationHandler();

    /**
     * 创建版本冲突的处理器
     *
     * @return 版本冲突的处理器
     */
    public abstract IVersionConflictHandler createVersionConflictHandler();

    /**
     * 创建“更新幻影”冲突的处理器
     *
     * @return “更新幻影”冲突的处理器
     */
    public abstract IUpdatingPhantomHandler createUpdatingPhantomHandler();
}

