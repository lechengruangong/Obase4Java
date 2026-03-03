/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象运算上下文.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 16:17:22
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query.oop;

/**
 * 对象运算上下文
 */
public class OopContext {

    /**
     * 查询源对象序列，简称查询源
     */
    private final Iterable<Object> source;

    /**
     * 对象运算结果
     */
    private Object result;

    /**
     * 构造OopContext的新实例
     *
     * @param objects 作为查询源的对象序列
     */
    public OopContext(Iterable<Object> objects) {
        this.source = objects;
        this.result = objects;
    }

    /**
     * 构造OopContext的新实例
     *
     * @param knownResult 当前已知的运算结果
     */
    public OopContext(Object knownResult) {
        this.result = knownResult;
        this.source = null;
    }

    /**
     * 获取查询源对象序列，简称查询源。
     *
     * @return 获取查询源对象序列，简称查询源。
     */
    public Iterable<Object> getSource() {
        return this.source;
    }

    /**
     * 获取查询结果
     *
     * @return 查询结果
     */
    public Object getResult() {
        return this.result;
    }

    /**
     * 设置查询结果
     *
     * @param result 查询结果
     */
    public void setResult(Object result) {
        this.result = result;
    }
}
