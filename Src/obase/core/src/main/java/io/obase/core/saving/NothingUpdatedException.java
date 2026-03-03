/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示Update语句未更新任何记录时引发的异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 16:24:12
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

/**
 * 表示Update语句未更新任何记录时引发的异常
 */
public class NothingUpdatedException extends RuntimeException {

    /**
     * 创建NothingUpdatedException实例
     */
    public NothingUpdatedException() {
        super("未更新任何记录");
    }
}
