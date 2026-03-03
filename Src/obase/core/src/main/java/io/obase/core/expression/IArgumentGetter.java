/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：参数获取器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-18 16:29:20
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

/**
 * 参数获取器
 */
public interface IArgumentGetter {

    /**
     * 根据参数名获取参数
     *
     * @param parameterName 参数名
     * @return 参数
     */
    Object get(String parameterName);
}
