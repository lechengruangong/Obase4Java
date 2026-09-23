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

import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局模型缓存,用于缓存对象上下文创建的模型，避免重复创建。
 * 如果对象上下文使用InnerContextConfig，缓存将以具体的上下文类型作键；否则，以具体的ContextConfig类型作键。
 * 每个具体类对应一个对象数据模型，不论应用程序域中该类型有多少实例，只有在第一个实例初始化时才会生成模型，后续所有实例将使用该模型。
 * 实施说明
 * （1）模型的建造过程不持有任何锁。模型建造会执行用户注册的中间件（类型解析器、代理类型生成器、补充配置器）、隐含类型发射以及存储结构映射，
 * 如果在锁内建造，一旦这些过程引发异常，锁将无法释放，导致同一线程后续取锁抛出异常、其它线程永久阻塞。
 * （2）每个上下文类型对应一个模型持有者，由持有者保证模型只被建造一次；并发调用时未中选的持有者不会执行建造，因此不会出现重复建造。
 */
public class GlobalModelCache {

    /**
     * 对象上下文类对象数据模型缓存
     */
    private final ConcurrentHashMap<String, ModelHolder> models = new ConcurrentHashMap<>();

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
        return InstanceHolder.INSTANCE;
    }

    /**
     * 从缓存中取出指定上下文类的模型。如果缓存中不存在该上下文类的模型（或该模型尚在建造中）则返回null。
     *
     * @param contextType 具体的对象上下文类型
     * @return 指定上下文类的模型
     */
    public ObjectDataModel getModel(Class<?> contextType) {
        ModelHolder holder = this.models.get(contextType.toString());
        if (holder == null) return null;
        //建造中或建造失败时不返回任何模型，与原有语义一致
        return holder.getIfCreated();
    }

    /**
     * 将对象数据模型放入全局缓存。
     * 说明
     * 模型由第一个调用者建造，建造过程不持有本缓存的任何锁，同一上下文类型的并发调用者会等待该次建造完成后取得同一个模型实例。
     * 如果建造过程引发异常，异常会抛给所有调用者，同时移除本次建造，保证修正问题后可以重新建造，也不会造成锁泄漏。
     *
     * @param contextType 具体的对象上下文类型
     * @param provider    要放入缓存的对象数据模型提供器
     */
    public void setModel(Class<?> contextType, ContextConfigProvider provider) {
        if (provider == null) throw new IllegalArgumentException("provider 不能为空");

        String key = contextType.toString();
        //此处的computeIfAbsent在并发时只会创建一个持有者，因此模型只会被建造一次
        ModelHolder holder = this.models.computeIfAbsent(key, k -> new ModelHolder());

        try {
            //触发或等待本次建造完成，此处不持有任何锁
            ObjectDataModel model = holder.get(provider);
            if (model == null)
                throw new IllegalStateException("上下文" + contextType.getName() + "的对象数据模型建造失败");
        } catch (RuntimeException | Error e) {
            //建造失败：移除本次失败的建造（只移除自己这一个），使后续调用可以重新尝试建造
            this.models.remove(key, holder);
            throw e;
        }
    }

    /**
     * 全局模型缓存单例持有者
     */
    private static final class InstanceHolder {

        /**
         * 单例
         */
        private static final GlobalModelCache INSTANCE = new GlobalModelCache();
    }

    /**
     * 单个上下文类型的模型持有者，保证模型只被建造一次
     */
    private static final class ModelHolder {

        /**
         * 对象数据模型
         */
        private ObjectDataModel model;

        /**
         * 获取对象数据模型，未建造时建造一次
         *
         * @param provider 对象数据模型提供器
         * @return 对象数据模型
         */
        synchronized ObjectDataModel get(ContextConfigProvider provider) {
            //建造可能引发异常，此时保持为null，使后续调用可以重试
            if (this.model == null) this.model = provider.createModel();
            return this.model;
        }

        /**
         * 获取已建造的对象数据模型，未建造时返回null
         *
         * @return 对象数据模型
         */
        synchronized ObjectDataModel getIfCreated() {
            return this.model;
        }
    }
}
