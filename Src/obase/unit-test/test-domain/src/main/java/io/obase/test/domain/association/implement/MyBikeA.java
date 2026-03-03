package io.obase.test.domain.association.implement;

/**
 * 特殊的我的自行车A 有一个额外的旗子
 */
public class MyBikeA extends Bike {

    /**
     * 旗子
     */
    private BikeFlag flag;

    /**
     * 旗子编码
     */
    private String flagCode;

    /**
     * 获取旗子
     *
     * @return 旗子
     */
    public BikeFlag getFlag() {
        return this.flag;
    }

    /**
     * 设置旗子
     *
     * @param flag 旗子
     */
    public void setFlag(BikeFlag flag) {
        this.flag = flag;
    }


    /**
     * 获取旗子编码
     *
     * @return 旗子编码
     */
    public String getFlagCode() {
        return this.flagCode;
    }


    /**
     * 设置旗子编码
     *
     * @param flagCode 旗子编码
     */
    public void setFlagCode(String flagCode) {
        this.flagCode = flagCode;
    }
}
