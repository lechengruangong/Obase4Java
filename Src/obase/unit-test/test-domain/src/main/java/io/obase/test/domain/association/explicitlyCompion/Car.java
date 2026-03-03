package io.obase.test.domain.association.explicitlyCompion;

import java.util.List;

/**
 * 汽车
 */
public class Car {

    /**
     * 编号
     */
    private String carCode;

    /**
     * 名称
     */
    private String carName;

    /**
     * 车轮
     */
    private List<CarWheel> carWheels;

    /**
     * 获取编号
     *
     * @return 编号
     */
    public String getCarCode() {
        return this.carCode;
    }

    /**
     * 设置编号
     *
     * @param carCode 编号
     */
    public void setCarCode(String carCode) {
        this.carCode = carCode;
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String getCarName() {
        return this.carName;
    }

    /**
     * 设置名称
     *
     * @param carName 名称
     */
    public void setCarName(String carName) {
        this.carName = carName;
    }

    /**
     * 获取车轮
     *
     * @return 车轮
     */
    public List<CarWheel> getCarWheels() {
        return this.carWheels;
    }

    /**
     * 设置车轮
     *
     * @param carWheels 车轮
     */
    public void setCarWheels(List<CarWheel> carWheels) {
        this.carWheels = carWheels;
    }

    /**
     * 根据车轮位置获取车轮
     *
     * @param wheelPosition 车轮位置
     * @return 车轮
     */
    public Wheel GetWheel(WheelPosition wheelPosition) {
        return this.carWheels.stream().filter(p -> p.getWheelPosition() == wheelPosition).map(CarWheel::getWheel).findFirst().orElse(null);
    }
}
