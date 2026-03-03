/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：实现基于时间的标识生成器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 17:11:25
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import java.util.Date;
import java.util.Random;

/**
 * 实现基于时间的标识生成器
 */
public class TimeBasedIdGenerator implements IDGenerator<Long> {

    /**
     * 生成标识使用的随机数产生器
     */
    private final Random random = new Random(new Date().getTime());

    /**
     * 生成下一个标识
     *
     * @return 标识
     */
    @Override
    public Long next() {
        long unixStamp = System.currentTimeMillis() / 1000;

        return unixStamp * 100 + this.random.nextInt(99);
    }
}
