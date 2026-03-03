package io.obase.test.domain.association.implement;

/**
 * 特殊的我的自行车C 是可以共享的
 */
public class MyBikeC extends MyBikeA {

    /**
     * 是否可共享
     */
    private boolean canShared;

    /**
     * 获取是否可共享
     *
     * @return 是否可共享
     */
    public boolean getCanShared() {
        return this.canShared;
    }

    /**
     * 设置是否可共享
     *
     * @param canShared 是否可共享
     */
    public void setCanShared(boolean canShared) {
        this.canShared = canShared;
    }
}
