/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：全局模型缓存.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 16:32:12
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.core.odm.ObjectDataModel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

/**
 * 全局模型缓存,用于缓存对象上下文创建的模型，避免重复创建。
 * 如果对象上下文使用InnerContextConfig，缓存将以具体的上下文类型作键；否则，以具体的ContextConfig类型作键。
 * 每个具体类对应一个对象数据模型，不论应用程序域中该类型有多少实例，只有在第一个实例初始化时才会生成模型，后续所有实例将使用该模型。
 */
public class GlobalModelCache {

    /**
     * 单例
     */
    private static volatile GlobalModelCache instance;


    /**
     * 对象上下文类对象数据模型缓存
     */
    private final Map<String, ObjectDataModel> models = new HashMap<>();

    /**
     * 邮戳锁
     */
    private final StampedLock stampedLock = new StampedLock();

    /**
     * 创建全局模型缓存实例
     */
    private GlobalModelCache() {
    }

    /**
     * 获取全局模型缓存实例。本属性确保应用程序域中有且仅有一个全局模型缓存实例。
     *
     * @return 全局模型缓存实例
     */
    public static GlobalModelCache getInstance() {
        if (instance == null) {
            synchronized (GlobalModelCache.class) {
                if (instance == null) {
                    instance = new GlobalModelCache();
                }
            }
        }
        return instance;
    }

    /**
     * 从缓存中取出指定上下文类的模型
     *
     * @param contextType 具体的对象上下文类型
     * @return 指定上下文类的模型
     */
    public ObjectDataModel getModel(Class<?> contextType) {
        long stamp = this.stampedLock.readLock();
        try {
            return this.models.getOrDefault(contextType.toString(), null);
        } finally {
            this.stampedLock.unlockRead(stamp);
        }
    }

    /**
     * 将对象数据模型放入全局缓存
     *
     * @param contextType 具体的对象上下文类型
     * @param provider    要放入缓存的对象数据模型提供器
     */
    public void setModel(Class<?> contextType, ContextConfigProvider provider) {
        long stamp = this.stampedLock.writeLock();
        this.models.put(contextType.toString(), provider.createModel());
        this.stampedLock.unlockWrite(stamp);
    }
}

