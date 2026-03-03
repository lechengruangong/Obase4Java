/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Zip运算.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-30 15:33:52
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

import io.obase.core.expression.LambdaExpression;

/**
 * 表示Zip运算
 */
public class ZipOp extends QueryOp {

    /**
     * 第一个序列的元素类型
     */
    private final Class<?> firstType;

    /**
     * 合并投影函数，用于指定如何合并这两个序列中的元素。不指定则返回两个序列中的元素一一对应构成的元组序列。
     */
    private final LambdaExpression resultSelector;

    /**
     * 返回值类型
     */
    private final Class<?> resultType;

    /**
     * 要合并的第二个序列
     */
    private final Object second;

    /**
     * 第二个序列的元素类型
     */
    private final Class<?> secondType;

    /**
     * 创建ZipOp实例
     *
     * @param second         要合并的第二个序列
     * @param sourceType     源类型
     * @param resultSelector 合并投影函数，用于指定如何合并这两个序列中的元素
     */
    ZipOp(Iterable<?> second, LambdaExpression resultSelector, Class<?> sourceType) {
        super(EQueryOpName.Zip, sourceType);

        this.second = second;
        this.resultSelector = resultSelector;
        this.firstType = null;
        this.resultType = null;
        this.secondType = null;
    }

    /**
     * 创建ZipOp实例
     *
     * @param firstType  第一个序列的元素类型
     * @param resultType 返回值类型
     * @param second     要合并的第二个序列
     */
    ZipOp(Class<?> firstType, Class<?> resultType, Iterable<?> second, Class<?> sourceType) {
        super(EQueryOpName.Zip, sourceType);
        this.resultSelector = null;
        this.firstType = firstType;
        this.resultType = resultType;
        this.second = second;
        this.secondType = Object.class;
    }

    /**
     * 获取第一个序列元素的类型
     *
     * @return 第一个序列元素的类型
     */
    public Class<?> getFirstType() {
        if (this.firstType != null)
            return this.firstType;
        if (this.resultSelector != null) {
            return this.resultSelector.getParameters()[0].getType();
        }
        return null;
    }

    /**
     * 获取合并投影函数，该函数用于指定如何合并这两个序列中的元素。不指定则返回两个序列中的元素一一对应构成的元组序列。
     *
     * @return 合并投影函数
     */
    public LambdaExpression getResultSelector() {
        return this.resultSelector;
    }

    /**
     * 获取合并结果序列元素的类型
     *
     * @return 合并结果序列元素的类型
     */
    public Class<?> getResultType() {
        if (this.resultType != null)
            return this.resultType;
        if (this.resultSelector != null) {
            return this.resultSelector.getBody().getType();
        }
        return null;
    }

    /**
     * 获取要合并的第二个序列
     *
     * @return 要合并的第二个序列
     */
    public Object getSecond() {
        return this.second;
    }

    /**
     * 获取第二个序列元素的类型
     *
     * @return 第二个序列元素的类型
     */
    public Class<?> getSecondType() {
        if (this.secondType != null)
            return this.secondType;
        if (this.resultSelector != null) {
            return this.resultSelector.getParameters()[0].getType();
        }
        return null;
    }
}
