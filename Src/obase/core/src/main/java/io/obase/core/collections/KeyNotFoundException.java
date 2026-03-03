/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：读取器的基础集合中找不到指定基键时引发的异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-12 16:29:22
└──────────────────────────────────────────────────────────────┘
*/

package io.obase.core.collections;

/**
 * 在读取器的基础集合中找不到指定基键时引发的异常
 * 此类在C#版本中为泛型类
 */
public class KeyNotFoundException extends RuntimeException {

    /**
     * 缺失的基键
     */
    private final Object key;

    /**
     * 引发该异常的键序基读取器
     */
    private final IKeySequenceBasedReader<Object, Object> reader;

    /**
     * 创建KeyNotFoundException异常
     *
     * @param key    缺失的基键
     * @param reader 引发该异常的键序基读取器
     */
    public KeyNotFoundException(Object key, IKeySequenceBasedReader<Object, Object> reader) {
        this.key = key;
        this.reader = reader;
    }

    /**
     * 获取缺失的基键
     *
     * @return 缺失的基键
     */
    public Object getKey() {
        return this.key;
    }

    /**
     * 获取引发该异常的键序基读取器
     *
     * @return 引发该异常的键序基读取器
     */
    public IKeySequenceBasedReader<Object, Object> getReader() {
        return this.reader;
    }
}
