package io.obase.test.core.association;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.association.multiplexAssociation.Employee;
import io.obase.test.domain.association.multiplexAssociation.OfficeRoom;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 两个类之间有多种关系测试
 */
@ExtendWith(ConfigSetUp.class)
public class MultiplexAssociationTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            var employees = context.createSet(Employee.class).include(p -> p.getManageRooms()).toList();
            for (var emp : employees) {
                if (emp.getManageRooms() != null)
                    emp.getManageRooms().clear();
            }
            context.saveChanges();
            context.createSet(OfficeRoom.class).delete(p -> p.getRoomCode() != "", OfficeRoom.class);
            context.createSet(Employee.class).delete(p -> p.getEmployeeCode() != "", Employee.class);
            //加入员工和房间
            var room1 = new OfficeRoom();
            room1.setName("某房间1");
            room1.setRoomCode("L101");
            var room2 = new OfficeRoom();
            room2.setName("某房间2");
            room2.setRoomCode("L102");
            var room3 = new OfficeRoom();
            room3.setName("某房间3");
            room3.setRoomCode("L103");
            var employee = new Employee();
            employee.setEmployeeCode("A01");
            employee.setName("某员工");
            employee.setWorkRoom(room1);
            var list = new ArrayList<OfficeRoom>();
            list.add(room2);
            list.add(room3);
            employee.setManageRooms(list);

            context.attach(room1);
            context.attach(room2);
            context.attach(room3);
            context.attach(employee);
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
            var employees = context.createSet(Employee.class).include(p -> p.getManageRooms()).toList();
            for (var emp : employees) {
                if (emp.getManageRooms() != null)
                    emp.getManageRooms().clear();
            }
            context.saveChanges();
            context.createSet(OfficeRoom.class).delete(p -> p.getRoomCode() != "", OfficeRoom.class);
            context.createSet(Employee.class).delete(p -> p.getEmployeeCode() != "", Employee.class);
        }
    }

    /**
     * 测试两个类之间有多种关系
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void test(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //查询员工 加载工作房间和管理的房间
        var employee = context.createSet(Employee.class).include(p -> p.getWorkRoom()).include(p -> p.getManageRooms()).findFirst().orElse(null);
        //检查各个关联属性 有2个管理的房间 1个工作房间
        assertNotNull(employee);
        assertNotNull(employee.getWorkRoom());
        assertNotNull(employee.getManageRooms());
        assertEquals(2, employee.getManageRooms().size());

        //测试投影查询
        var rooms = context.createSet(Employee.class).filter(p -> p.getName() == "某员工").flatMap(p -> p.getManageRooms(), OfficeRoom.class)
                .filter(p -> p.getName() == "某房间2").toList();

        //检查投影查询结果
        assertNotNull(rooms);
        assertEquals(1, rooms.size());
        assertEquals("L102", rooms.get(0).getRoomCode());
    }
}
