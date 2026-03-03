/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：完整性检查未通过异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 14:55:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.util.List;
import java.util.Map;

/**
 * 完整性检查未通过异常
 */
public class IntegrityCheckFailException extends RuntimeException {

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
        this.errorMessageDictionary = errorMessageDictionary;
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
