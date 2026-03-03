package io.obase.test.domain.association.explicitlyCompion;

/**
 * 表示汽车的车轮
 */
public class CarWheel {

    /**
     * 汽车编号
     */
    private String carCode;

    /**
     * 车轮编号
     */
    private String wheelCode;

    /**
     * 汽车
     */
    private Car car;

    /**
     * 车轮
     */
    private Wheel wheel;

    /**
     * 车轮位置
     */
    private WheelPosition wheelPosition;

    /**
     * 构造汽车车轮
     *
     * @param car           汽车
     * @param wheel         车轮
     * @param wheelPosition 位置
     */
    public CarWheel(Car car, Wheel wheel, WheelPosition wheelPosition) {
        this.car = car;
        this.carCode = car.getCarCode();
        this.wheel = wheel;
        this.wheelCode = wheel.getWheelCode();
        this.wheelPosition = wheelPosition;
    }

    /**
     * 反序列化使用
     */
    protected CarWheel() {
    }

    /**
     * 获取汽车编号
     *
     * @return 汽车编号
     */
    public String getCarCode() {
        return this.carCode;
    }

    /**
     * 设置汽车编号
     *
     * @param carCode 汽车编号
     */
    public void setCarCode(String carCode) {
        this.carCode = carCode;
    }

    /**
     * 获取车轮编号
     *
     * @return 车轮编号
     */
    public String getWheelCode() {
        return this.wheelCode;
    }

    /**
     * 设置车轮编号
     *
     * @param wheelCode 车轮编号
     */
    public void setWheelCode(String wheelCode) {
        this.wheelCode = wheelCode;
    }

    /**
     * 获取汽车
     *
     * @return 汽车
     */
    public Car getCar() {
        return this.car;
    }

    /**
     * 设置汽车
     *
     * @param car 汽车
     */
    public void setCar(Car car) {
        this.car = car;
    }

    /**
     * 获取车轮
     *
     * @return 车轮
     */
    public Wheel getWheel() {
        return this.wheel;
    }

    /**
     * 设置车轮
     *
     * @param wheel 车轮
     */
    public void setWheel(Wheel wheel) {
        this.wheel = wheel;
    }

    /**
     * 获取车轮位置
     *
     * @return 车轮位置
     */
    public WheelPosition getWheelPosition() {
        return this.wheelPosition;
    }

    /**
     * 设置车轮位置
     *
     * @param wheelPosition 车轮位置
     */
    public void setWheelPosition(WheelPosition wheelPosition) {
        this.wheelPosition = wheelPosition;
    }
}
