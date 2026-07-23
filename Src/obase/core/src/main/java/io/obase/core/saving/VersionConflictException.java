/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示版本冲突的异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 16:16:05
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.odm.ObjectKey;
import io.obase.core.odm.ObjectType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 表示版本冲突的异常
 */
public class VersionConflictException extends ConcurrentConflictException {

    /**
     * 发生冲突的各个对象（主对象和伴随映射对象）的初始版本标识
     */
    private final List<ObjectKey> initVersionKeys;

    /**
     * 创建VersionConflictException实例
     *
     * @param obj             发生冲突的对象
     * @param objType         发生冲突的对象的类型
     * @param initVersionKeys 发生冲突的各个对象（主对象和伴随映射对象）的初始版本标识
     * @param exception       内部异常
     */
    public VersionConflictException(Object obj, ObjectType objType, List<ObjectKey> initVersionKeys, Exception exception) {
        super(obj, objType, exception);
        this.initVersionKeys = initVersionKeys;
    }

    /**
     * 获取发生冲突的各个对象（主对象和伴随映射对象）的初始版本标识
     *
     * @return 发生冲突的各个对象（主对象和伴随映射对象）的初始版本标识
     */
    public List<ObjectKey> getInitVersionKeys() {
        return this.initVersionKeys;
    }

    /**
     * 返回异常消息
     *
     * @return 异常消息
     */
    @Override
    public String getMessage() {
        return String.format("发生了并发冲突，更新对象时发现本地版本已过时，对象标识为[%s]", this.initVersionKeys == null ? "" : this.initVersionKeys.stream().map(ObjectKey::getTypeName).collect(Collectors.joining(",")));
    }
}
