package io.obase.test.domain.functional.serialization;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 某种身份
 */
public class Identity {

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 身份标识
     */
    private UUID id;

    /**
     * 名称
     */
    private String name;

    /**
     * 查询时间
     */
    private LocalDateTime queryTime;

    /**
     * 角色
     */
    private String role;

    /**
     * 版本
     */
    private int version;

    /**
     * 次版本
     */
    private int subVersion;

    /**
     * 初始化某种身份
     *
     * @param id         身份标识
     * @param createTime 创建时间
     * @param role       角色
     */
    public Identity(UUID id, LocalDateTime createTime, String role) {
        this.id = id;
        this.createTime = createTime;
        this.role = role;
        this.queryTime = LocalDateTime.now();
        this.name = id.toString().replace("-", "");
    }

    /**
     * 反序列化函数
     *
     * @param id         身份标识
     * @param createTime 创建时间
     * @param role       角色
     * @param queryTime  查询时间
     * @param name       名称
     */
    protected Identity(UUID id, LocalDateTime createTime, String role, LocalDateTime queryTime, String name) {
        this.id = id;
        this.createTime = createTime;
        this.role = role;
        this.queryTime = queryTime;
        this.name = name;
    }

    /**
     * 创建时间
     *
     * @return 创建时间
     */
    public LocalDateTime getCreateTime() {
        return this.createTime;
    }

    /**
     * 创建时间
     *
     * @param createTime 创建时间
     */
    void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    /**
     * 身份标识
     *
     * @return 身份标识
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * 身份标识
     *
     * @param id 身份标识
     */
    void setId(UUID id) {
        this.id = id;
    }

    /**
     * 名称
     *
     * @return 名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 名称
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 查询时间
     *
     * @return 查询时间
     */
    public LocalDateTime getQueryTime() {
        return this.queryTime;
    }

    /**
     * 查询时间
     *
     * @param queryTime 查询时间
     */
    void setQueryTime(LocalDateTime queryTime) {
        this.queryTime = queryTime;
    }

    /**
     * 角色
     *
     * @return 角色
     */
    public String getRole() {
        return this.role;
    }

    /**
     * 角色
     *
     * @param role 角色
     */
    void setRole(String role) {
        this.role = role;
    }

    /**
     * 版本
     *
     * @return 版本
     */
    public int getVersion() {
        return this.version;
    }

    /**
     * 版本
     *
     * @param version 版本
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * 次版本
     *
     * @return 次版本
     */
    public int getSubVersion() {
        return this.subVersion;
    }

    /**
     * 次版本
     *
     * @param subVersion 次版本
     */
    public void setSubVersion(int subVersion) {
        this.subVersion = subVersion;
    }

    /**
     * 转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "Identity{" +
                "createTime=" + this.createTime +
                ", id=" + this.id +
                ", queryTime=" + this.queryTime +
                ", role='" + this.role + '\'' +
                '}';
    }
}
