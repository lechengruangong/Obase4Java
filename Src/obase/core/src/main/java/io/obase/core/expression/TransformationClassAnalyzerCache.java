/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表达式全局分析器缓存.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-18 15:26:51
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

/**
 * 全局分析器缓存
 */
public class TransformationClassAnalyzerCache {

    /**
     * 单例
     */
    private static volatile TransformationClassAnalyzerCache instance;
    /**
     * 分析器缓存
     */
    private final Map<String, TransformationClassAnalyzer> transformationClassAnalyzerHashMap = new HashMap<>();
    /**
     * 邮戳锁
     */
    private final StampedLock stampedLock = new StampedLock();

    /**
     * 创建全局模型缓存实例
     */
    private TransformationClassAnalyzerCache() {

    }

    /**
     * 获取单例
     *
     * @return 单例
     */
    public static TransformationClassAnalyzerCache getInstance() {
        if (instance == null) {
            synchronized (TransformationClassAnalyzerCache.class) {
                if (instance == null) {
                    instance = new TransformationClassAnalyzerCache();
                }
            }
        }
        return instance;
    }

    /**
     * 设置分析器
     *
     * @param name                        名称
     * @param transformationClassAnalyzer 分析器
     */
    public void setTransformationClassAnalyzer(String name, TransformationClassAnalyzer transformationClassAnalyzer) {
        long stamp = this.stampedLock.writeLock();
        this.transformationClassAnalyzerHashMap.put(name, transformationClassAnalyzer);
        this.stampedLock.unlockWrite(stamp);
    }

    /**
     * 获取分析器
     *
     * @param name 名称
     * @return 分析器
     */
    public TransformationClassAnalyzer getTransformationClassAnalyzer(String name) {
        long stamp = this.stampedLock.readLock();
        try {
            return this.transformationClassAnalyzerHashMap.getOrDefault(name, null);
        } finally {
            this.stampedLock.unlockRead(stamp);
        }
    }
}
