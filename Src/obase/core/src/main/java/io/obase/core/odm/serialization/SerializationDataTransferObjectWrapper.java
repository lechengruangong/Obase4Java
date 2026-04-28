/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化数据传输对象包装对象.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-30 14:07:12
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.serialization;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 序列化数据传输对象包装对象
 * 内部引用序列化数据传输对象 用于简化序列化过程中的类型检测
 */
public class SerializationDataTransferObjectWrapper {

    /**
     * 序列化数据传输对象
     */
    private List<SerializationDataTransferObject> dto;

    /**
     * 创建时间
     */
    private LocalDateTime createTime = LocalDateTime.now();

    /**
     * 修改时间
     */
    private LocalDateTime modifiedTime;

    /**
     * 初始化序列化数据传输对象包装对象
     *
     * @param dto 序列化数据传输对象
     */
    public SerializationDataTransferObjectWrapper(List<SerializationDataTransferObject> dto) {
        this.dto = dto;
        if (dto == null)
            this.dto = new ArrayList<>();
    }

    /**
     * 获取序列化数据传输对象
     *
     * @return 序列化数据传输对象
     */
    public List<SerializationDataTransferObject> getDto() {
        return this.dto;
    }

    /**
     * 设置序列化数据传输对象
     *
     * @param dto 序列化数据传输对象
     */
    public void setDto(List<SerializationDataTransferObject> dto) {
        this.dto = dto;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public LocalDateTime getCreateTime() {
        return this.createTime;
    }

    /**
     * 设置创建时间
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取修改时间
     *
     * @return 修改时间
     */
    public LocalDateTime getModifiedTime() {
        return this.modifiedTime;
    }

    /**
     * 设置修改时间
     *
     * @param modifiedTime 修改时间
     */
    public void setModifiedTime(LocalDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
