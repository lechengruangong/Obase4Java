/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：元组工具类
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-11 16:32:41
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.common;

/**
 * 元组工具类
 */
public class TupleUtils {

    /**
     * 创建元组
     *
     * @param items 元组元素集合
     * @return 元组
     */
    public static Tuple of(Object[] items) {
        if (items.length <= 1 || items.length > 5)
            throw new IllegalArgumentException("元组个数错误,不支持" + items.length + "个参数的元组.");

        if (items.length == 2) {
            return of(items[0], items[1]);
        }

        if (items.length == 3) {
            return of(items[0], items[1], items[2]);
        }

        if (items.length == 4) {
            return of(items[0], items[1], items[2], items[3]);
        }

        return of(items[0], items[1], items[2], items[3], items[4]);
    }

    /**
     * 返回一个二元组
     *
     * @param item1 第一个元素
     * @param item2 第二个元素
     * @param <T1>  第一个元素类型
     * @param <T2>  第二个元素类型
     * @return 二元组
     */
    public static <T1, T2> TwoTuple<T1, T2> of(T1 item1, T2 item2) {
        return new TwoTuple<>(item1, item2);
    }

    /**
     * 返回一个三元组
     *
     * @param item1 第一个元素
     * @param item2 第二个元素
     * @param item3 第三个元素
     * @param <T1>  第一个元素类型
     * @param <T2>  第二个元素类型
     * @param <T3>  第三个元素类型
     * @return 三元组
     */
    public static <T1, T2, T3> ThreeTuple<T1, T2, T3> of(T1 item1, T2 item2, T3 item3) {
        return new ThreeTuple<>(item1, item2, item3);
    }

    /**
     * 返回一个四元组
     *
     * @param item1 第一个元素
     * @param item2 第二个元素
     * @param item3 第三个元素
     * @param item4 第四个元素
     * @param <T1>  第一个元素类型
     * @param <T2>  第二个元素类型
     * @param <T3>  第三个元素类型
     * @param <T4>  第四个元素类型
     * @return 四元组
     */
    public static <T1, T2, T3, T4> FourTuple<T1, T2, T3, T4> of(T1 item1, T2 item2, T3 item3, T4 item4) {
        return new FourTuple<>(item1, item2, item3, item4);
    }

    /**
     * 返回一个五元组
     *
     * @param item1 第一个元素
     * @param item2 第二个元素
     * @param item3 第三个元素
     * @param item4 第四个元素
     * @param item5 第五个元素
     * @param <T1>  第一个元素类型
     * @param <T2>  第二个元素类型
     * @param <T3>  第三个元素类型
     * @param <T4>  第四个元素类型
     * @param <T5>  第五个元素类型
     * @return 五元组
     */
    public static <T1, T2, T3, T4, T5> FiveTuple<T1, T2, T3, T4, T5> of(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5) {
        return new FiveTuple<>(item1, item2, item3, item4, item5);
    }
}
