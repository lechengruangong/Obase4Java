package io.obase.test.domain.association.multiplexAssociation;

/**
 * 表示办公室房间
 */
public class OfficeRoom {

    /**
     * 房间名称
     */
    private String name;

    /**
     * 房间号
     */
    private String roomCode;

    /**
     * 获取房间名称
     *
     * @return 房间名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置房间名称
     *
     * @param name 房间名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取房间名称
     *
     * @return 房间名称
     */
    public String getRoomCode() {
        return this.roomCode;
    }

    /**
     * 设置房间名称
     *
     * @param roomCode 房间名称
     */
    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "OfficeRoom{" +
                "name='" + this.name + '\'' +
                ", roomCode='" + this.roomCode + '\'' +
                '}';
    }
}
