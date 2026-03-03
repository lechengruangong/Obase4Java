package io.obase.test.domain.association.multiplexAssociation;

import java.util.List;

/**
 * 表示员工
 */
public class Employee {

    /**
     * 员工编码
     */
    private String employeeCode;

    /**
     * 管理的房间
     */
    private List<OfficeRoom> manageRooms;

    /**
     * 名称
     */
    private String name;

    /**
     * 工作的房间
     */
    private OfficeRoom workRoom;

    /**
     * 工作的房间编码
     */
    private String workRoomCode;

    /**
     * 获取员工编码
     *
     * @return 员工编码
     */
    public String getEmployeeCode() {
        return this.employeeCode;
    }

    /**
     * 设置员工编码
     *
     * @param employeeCode 员工编码
     */
    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置名称
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取管理的房间
     *
     * @return 管理的房间
     */
    public List<OfficeRoom> getManageRooms() {
        return this.manageRooms;
    }

    /**
     * 设置管理的房间
     *
     * @param manageRooms 管理的房间
     */
    public void setManageRooms(List<OfficeRoom> manageRooms) {
        this.manageRooms = manageRooms;
    }

    /**
     * 获取工作的房间
     *
     * @return 工作的房间
     */
    public OfficeRoom getWorkRoom() {
        return this.workRoom;
    }

    /**
     * 设置工作的房间
     *
     * @param workRoom 工作的房间
     */
    public void setWorkRoom(OfficeRoom workRoom) {
        this.workRoom = workRoom;
    }

    /**
     * 获取工作的房间编码
     *
     * @return 工作的房间编码
     */
    public String getWorkRoomCode() {
        return this.workRoomCode;
    }

    /**
     * 设置工作的房间编码
     *
     * @param workRoomCode 工作的房间编码
     */
    public void setWorkRoomCode(String workRoomCode) {
        this.workRoomCode = workRoomCode;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "Employee{" +
                "employeeCode='" + this.employeeCode + '\'' +
                ", manageRooms=" + this.manageRooms +
                ", name='" + this.name + '\'' +
                ", workRoom=" + this.workRoom +
                ", workRoomCode='" + this.workRoomCode + '\'' +
                '}';
    }
}
