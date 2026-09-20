/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：完整性检查未通过异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 14:55:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 完整性检查未通过异常
 */
public class IntegrityCheckFailException extends RuntimeException {

    /**
     * 对象数据模型(ODM)完整性检查错误信息前缀
     */
    public static final String ODM_MESSAGE_PREFIX = "[ODM]";

    /**
     * 序列化对象数据模型(SODM)完整性检查错误信息前缀
     */
    public static final String SODM_MESSAGE_PREFIX = "[SODM]";

    /**
     * 完整性检查错误信息字典
     */
    private final Map<String, List<String>> errorMessageDictionary;

    /**
     * 初始化完整性检查未通过异常
     *
     * @param errorMessageDictionary 完整性检查错误信息字典
     */
    public IntegrityCheckFailException(Map<String, List<String>> errorMessageDictionary) {
        this(errorMessageDictionary, null);
    }

    /**
     * 初始化完整性检查未通过异常
     *
     * @param errorMessageDictionary 完整性检查错误信息字典
     * @param messagePrefix          错误信息前缀 用于区分错误信息的来源(ODM或SODM) 不指定时直接使用原始的错误信息
     */
    public IntegrityCheckFailException(Map<String, List<String>> errorMessageDictionary, String messagePrefix) {
        //不需要前缀 直接使用原始的错误信息字典
        if (messagePrefix == null || messagePrefix.isEmpty()) {
            this.errorMessageDictionary = errorMessageDictionary;
        }
        //需要前缀 为每一条错误信息添加前缀 便于区分错误信息的来源
        else {
            Map<String, List<String>> prefixedDictionary = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> item : errorMessageDictionary.entrySet()) {
                List<String> prefixedMessages = new ArrayList<>();
                for (String message : item.getValue())
                    prefixedMessages.add(messagePrefix + message);
                prefixedDictionary.put(item.getKey(), prefixedMessages);
            }
            this.errorMessageDictionary = prefixedDictionary;
        }
    }

    /**
     * 获取完整性检查错误信息字典
     *
     * @return 完整性检查错误信息字典
     */
    public Map<String, List<String>> getErrorMessageDictionary() {
        return this.errorMessageDictionary;
    }

    /**
     * 返回异常消息
     *
     * @return 异常消息
     */
    @Override
    public String getMessage() {
        return this.toString();
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "完整性检查未通过,请参考errorMessageDictionary的内容修改模型配置或者关闭完整性检查.";
    }
}
