package io.obase.test.domain.functional.expression;

/**
 * 表示水罐子
 */
public class WaterTank extends Can {

    /**
     * 容量
     */
    private long vol;

    /**
     * 获取容量
     *
     * @return 容量
     */
    public long getVol() {
        return this.vol;
    }

    /**
     * 设置容量
     *
     * @param vol 容量
     */
    public void setVol(long vol) {
        this.vol = vol;
    }
}
