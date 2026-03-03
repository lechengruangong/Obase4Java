package io.obase.test.core.association;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.association.explicitlyCompion.Car;
import io.obase.test.domain.association.explicitlyCompion.CarWheel;
import io.obase.test.domain.association.explicitlyCompion.Wheel;
import io.obase.test.domain.association.explicitlyCompion.WheelPosition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 伴随映射的显式关联测试
 */
@ExtendWith(ConfigSetUp.class)
public class ExplicitlyCompanionTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {
        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(Car.class).delete(p -> p.getCarCode() != "", Car.class);
            context.createSet(Wheel.class).delete(p -> p.getWheelCode() != "", Wheel.class);

            //初始化一辆车
            var car = new Car();
            car.setCarCode(UUID.randomUUID().toString());
            car.setCarName("某车");

            //初始化四个车轮
            var wheel1 = new Wheel();
            wheel1.setWheelCode(UUID.randomUUID().toString());
            var wheel2 = new Wheel();
            wheel2.setWheelCode(UUID.randomUUID().toString());
            var wheel3 = new Wheel();
            wheel3.setWheelCode(UUID.randomUUID().toString());
            var wheel4 = new Wheel();
            wheel4.setWheelCode(UUID.randomUUID().toString());

            //创建汽车车轮关系
            var carWheels = new ArrayList<CarWheel>();
            carWheels.add(new CarWheel(car, wheel1, WheelPosition.FrontLeft));
            carWheels.add(new CarWheel(car, wheel2, WheelPosition.FrontRight));
            carWheels.add(new CarWheel(car, wheel3, WheelPosition.BackLeft));
            carWheels.add(new CarWheel(car, wheel4, WheelPosition.BackRight));

            //为车和车轮建立关联
            car.setCarWheels(carWheels);
            //都附加至上下文
            context.attach(car);
            context.attach(wheel1);
            context.attach(wheel2);
            context.attach(wheel3);
            context.attach(wheel4);
            for (var carWheel : carWheels)
                context.attach(carWheel);
            //保存
            context.saveChanges();
        }
    }

    /**
     * 销毁方法
     */
    @AfterAll
    public static void afterAll() {
        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(Car.class).delete(p -> p.getCarCode() != "", Car.class);
            context.createSet(Wheel.class).delete(p -> p.getWheelCode() != "", Wheel.class);
        }
    }

    /**
     * 测试伴随映射的显式关联查询
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void queryTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //查询出来 使用Include一并加载CarWheels 和 Wheel
        var queryCar = context.createSet(Car.class).include("CarWheels.Wheel").findFirst().orElse(null);

        //验证属性
        assertNotNull(queryCar);

        //有四个车轮
        assertEquals(4, queryCar.getCarWheels().size());
        //左前为wheel1
        assertNotNull(queryCar.GetWheel(WheelPosition.FrontLeft));
        //右前为wheel2
        assertNotNull(queryCar.GetWheel(WheelPosition.FrontRight));
        //左后为wheel3
        assertNotNull(queryCar.GetWheel(WheelPosition.BackLeft));
        //右后为wheel4
        assertNotNull(queryCar.GetWheel(WheelPosition.BackRight));

        //删除
        context.remove(queryCar);
        for (var carWheel : queryCar.getCarWheels())
            context.remove(carWheel.getWheel());
        //保存
        context.saveChanges();
    }
}
