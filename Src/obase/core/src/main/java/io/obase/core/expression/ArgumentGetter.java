/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：默认的表达式参数提取器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-19 12:06:18
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import java.util.Map;

/**
 * 默认的表达式参数提取器
 * 用参数名和实际值字典存储参数
 */
public class ArgumentGetter implements IArgumentGetter {

    /**
     * 参数字典
     */
    private final Map<String, Object> map;

    /**
     * 参数获取器
     *
     * @param map 参数名称和实际值字典
     */
    public ArgumentGetter(Map<String, Object> map) {

        this.map = map;
    }

    /**
     * 根据参数名获取参数
     *
     * @param parameterName 参数名
     * @return 参数
     */
    @Override
    public Object get(String parameterName) {
        if (this.map.containsKey(parameterName))
            return this.map.get(parameterName);
        return null;
    }
}
