package io.obase.test.core.association;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.association.explicitlySelf.Friend;
import io.obase.test.domain.association.explicitlySelf.Guest;
import io.obase.test.domain.association.self.Area;
import io.obase.test.domain.association.self.FriendlyArea;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Comparator;
import java.util.Date;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 自关联测试
 * 包括显式和隐式自关联
 */
@ExtendWith(ConfigSetUp.class)
public class SelfAssociationTest {
    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的旧数据
            context.createSet(Area.class).delete(p -> p.getCode() != "", Area.class);
            context.createSet(FriendlyArea.class).delete(p -> p.getAreaCode() != "", FriendlyArea.class);
            context.createSet(Guest.class).delete(p -> p.getGuestId() > 0, Guest.class);
            context.createSet(Friend.class).delete(p -> p.getMySelfId() > 0 || p.getFriendId() > 0, Friend.class);

            //几个区域
            var area1 = new Area();
            area1.setCode("P1");
            area1.setName("某某省");

            var area2 = new Area();
            area2.setCode("C1");
            area2.setName("某某市A");
            area2.setParentCode("P1");

            var area3 = new Area();
            area3.setCode("C2");
            area3.setName("某某市B");
            area3.setParentCode("P1");

            //C2和C3 是友好区域
            var friendly = new FriendlyArea();
            friendly.setArea(area2);
            friendly.setAreaCode(area2.getCode());
            friendly.setFriend(area3);
            friendly.setFriendlyAreaCode(area3.getCode());
            friendly.setStartTime(new Date());

            context.attach(area1);
            context.attach(area2);
            context.attach(area3);
            context.attach(friendly);

            context.saveChanges();

            //初始化宾客
            var guest1 = new Guest();
            guest1.setName("宾客1");

            var guest2 = new Guest();
            guest2.setName("宾客2");

            var guest3 = new Guest();
            guest3.setName("宾客3");


            //建立朋友关系 此处的朋友关系是表示单向的 即分为我的朋友 和 朋友是我的人 类似于qq中 我加了你好友 但是你只是同意了 但没有加我做你的好友
            //friend1即表示guest1和guest2交了朋友 但从guest2来看 guest1是朋友是我的人并不是我的好友 因为没有创建一个MySelf是宾客2 Friend是宾客1的关系 其余同理
            var friend1 = new Friend();
            friend1.setMeetIn("某活动1");
            friend1.setMySelf(guest1);
            friend1.setFriendGuest(guest2);

            var friend2 = new Friend();
            friend2.setMeetIn("某活动2");
            friend2.setMySelf(guest1);
            friend2.setFriendGuest(guest3);

            var friend3 = new Friend();
            friend3.setMeetIn("某活动3");
            friend3.setMySelf(guest2);
            friend3.setFriendGuest(guest3);


            //附加至上下文
            context.attach(guest1);
            context.attach(guest2);
            context.attach(guest3);
            context.attach(friend1);
            context.attach(friend2);
            context.attach(friend3);
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

            //清理可能的旧数据
            context.createSet(Area.class).delete(p -> p.getCode() != "", Area.class);
            context.createSet(FriendlyArea.class).delete(p -> p.getAreaCode() != "", FriendlyArea.class);
            context.createSet(Guest.class).delete(p -> p.getGuestId() > 0, Guest.class);
            context.createSet(Friend.class).delete(p -> p.getMySelfId() > 0 || p.getFriendId() > 0, Friend.class);
        }
    }

    /**
     * 测试隐式自关联
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void selfTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //查询P1区域
        var p1 = context.createSet(Area.class).findFirst(p -> p.getCode() == "P1").orElse(null);
        //P1区域的下级区域是C1和C2 第1个是C1友好区域是C2
        assertNotNull(p1);
        assertNotNull(p1.getSubAreas());
        assertNotNull(p1.getSubAreas().get(0));
        assertNotNull(p1.getSubAreas().get(0).getFriendlyAreas());
        assertNotNull(p1.getSubAreas().get(0).getFriendlyAreas().get(0));
        //查询C1区域
        var p2 = context.createSet(Area.class).include(Area::getParentArea).findFirst(p -> p.getCode() == "C1").orElse(null);
        //C1区域的父级区域是P1
        assertNotNull(p2);
        assertNotNull(p2.getParentArea());
    }

    /**
     * 测试显式自关联
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void explicitlySelfTest(EDataSource dataSource) {

        var context = ContextUtils.createContext(dataSource);

        //查询宾客 此处使用Include一并加载朋友 和 宾客
        var guestList = context.createSet(Guest.class).include("MyFriends.FriendGuest")
                .include("MyFriends.MySelf").include("FriendOfMes.FriendGuest")
                .include("FriendOfMes.MySelf").toList();

        //排序
        guestList.forEach(p ->
        {
            p.setMyFriends(p.getMyFriends().stream().sorted(Comparator.comparingInt(q -> q.getFriendGuest().getGuestId())).collect(Collectors.toList()));
            p.setFriendOfMes(p.getFriendOfMes().stream().sorted(Comparator.comparingInt(q -> q.getFriendGuest().getGuestId())).collect(Collectors.toList()));
        });

        //验证关系
        //共有三名宾客
        assertEquals(3, guestList.size());
        //第一名宾客 宾客1
        assertEquals("宾客1", guestList.get(0).getName());
        //宾客1有两个朋友 宾客2 和 宾客3
        assertEquals(2, guestList.get(0).getMyFriends().size());
        assertEquals(1, guestList.get(0).getMyFriends().stream().filter(p -> p.getFriendGuest().getName().equals("宾客2")).count());
        assertEquals(1, guestList.get(0).getMyFriends().stream().filter(p -> p.getFriendGuest().getName().equals("宾客3")).count());
        //分别是在活动1和活动2里认识的
        assertEquals(1, guestList.get(0).getMyFriends().stream().filter(p -> p.getMeetIn().equals("某活动1")).count());
        assertEquals(1, guestList.get(0).getMyFriends().stream().filter(p -> p.getMeetIn().equals("某活动2")).count());
        //没有好友是宾客1的人
        assertEquals(0, guestList.get(0).getFriendOfMes().size());

        //第二名是宾客2
        assertEquals("宾客2", guestList.get(1).getName());
        //宾客2有一个朋友 宾客3
        assertEquals(1, guestList.get(1).getMyFriends().size());
        assertEquals("宾客3", guestList.get(1).getMyFriends().get(0).getFriendGuest().getName());
        //在活动3里认识的
        assertEquals("某活动3", guestList.get(1).getMyFriends().get(0).getMeetIn());
        //好友是宾客2的人只有宾客1
        assertEquals(1, guestList.get(1).getFriendOfMes().size());
        assertEquals("宾客1", guestList.get(1).getFriendOfMes().get(0).getMySelf().getName());
        //在活动3里认识的
        assertEquals("某活动1", guestList.get(1).getFriendOfMes().get(0).getMeetIn());

        //第二名是宾客3
        assertEquals("宾客3", guestList.get(2).getName());
        //宾客3没有好友
        assertEquals(0, guestList.get(2).getMyFriends().size());
        //好友是宾客2的人有宾客1 和 宾客2
        assertEquals(2, guestList.get(2).getFriendOfMes().size());
        assertEquals(1, guestList.get(2).getFriendOfMes().stream().filter(p -> p.getMySelf().getName().equals("宾客1")).count());
        assertEquals(1, guestList.get(2).getFriendOfMes().stream().filter(p -> p.getMySelf().getName().equals("宾客2")).count());
        //在活动2和活动3里认识的
        assertEquals(1, guestList.get(2).getFriendOfMes().stream().filter(p -> p.getMeetIn().equals("某活动2")).count());
        assertEquals(1, guestList.get(2).getFriendOfMes().stream().filter(p -> p.getMeetIn().equals("某活动3")).count());
    }
}
