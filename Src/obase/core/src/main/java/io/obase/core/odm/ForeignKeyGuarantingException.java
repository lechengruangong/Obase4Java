/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：外键保证异常,缺失外键属性但因某种原因无法定义该属性时引发外键保证异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 15:05:36
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

/**
 * 外键保证异常。
 * 外键保证机制在发现缺失外键属性但因某种原因无法定义该属性时引发外键保证异常
 */
public class ForeignKeyGuarantingException extends RuntimeException {

    /**
     * 创建ForeignKeyGuarantingException实例。
     *
     * @param message 异常信息
     */
    public ForeignKeyGuarantingException(String message) {
        super(message);
    }
}
