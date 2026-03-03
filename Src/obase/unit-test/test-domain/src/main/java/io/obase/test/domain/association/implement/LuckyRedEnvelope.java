package io.obase.test.domain.association.implement;

/**
 * 幸运红包 获得的钱翻倍
 */
public class LuckyRedEnvelope extends RedEnvelope {

    /**
     * 实际获得金额
     */
    private int actual;

    /**
     * 实际获得金额
     */
    public int getActual() {
        return this.actual;
    }

    /**
     * 实际获得金额
     */
    public void setActual(int actual) {
        this.actual = actual;
    }
}
