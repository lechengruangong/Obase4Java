/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：实现基于UUID的标识生成器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 16:34:59
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import java.util.UUID;

/**
 * 实现基于UUID的标识生成器
 * 生成规则：返回UUID的无连接符小写字符格式。
 */
public class UuidBasedIdGenerator implements IDGenerator<String> {
    /**
     * 生成下一个标识
     *
     * @return 标识
     */
    @Override
    public String next() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
}
