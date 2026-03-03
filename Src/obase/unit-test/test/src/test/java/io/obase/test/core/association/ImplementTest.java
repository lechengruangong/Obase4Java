package io.obase.test.core.association;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.association.implement.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 继承关系测试
 */
@ExtendWith(ConfigSetUp.class)
public class ImplementTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(Bike.class).delete(p -> p.getCode() != "", Bike.class);
            context.createSet(BikeLight.class).delete(p -> p.getCode() != "", BikeLight.class);
            context.createSet(BikeWheel.class).delete(p -> p.getCode() != "", BikeWheel.class);
            context.createSet(BikeFlag.class).delete(p -> p.getCode() != "", BikeFlag.class);
            context.createSet(BikeBucket.class).delete(p -> p.getCode() != "", BikeBucket.class);
            context.createSet(Prize.class).delete(p -> p.getId() > 0, Prize.class);
            context.createSet(Activity.class).delete(p -> p.getId() > 0, Activity.class);
            context.createSet(Words.class).delete(p -> p.getWordsId() > 0, Words.class);
            context.createSet(CustomerDialogue.class).delete(p -> p.getDialogueId() > 0, CustomerDialogue.class);

            //新增对象
            var bikeLight = new BikeLight();
            bikeLight.setCode("AAA-L");
            bikeLight.setValue(5);
            context.attach(bikeLight);

            var bike = new Bike();
            bike.setCode("AAA");
            bike.setLightCode("AAA-L");
            bike.setName("AAA号自行车");
            context.attach(bike);

            var bikeWheel1 = new BikeWheel();
            bikeWheel1.setCode("AAA-W-1");
            bikeWheel1.setBikeCode("AAA");
            BikeWheel bikeWheel2 = new BikeWheel();
            bikeWheel2.setCode("AAA-W-2");
            bikeWheel2.setBikeCode("AAA");
            context.attach(bikeWheel1);
            context.attach(bikeWheel2);

            var myBikeALight = new BikeLight();
            myBikeALight.setCode("AAA-L-A");
            myBikeALight.setValue(10);
            context.attach(myBikeALight);

            var myBikeAFlag = new BikeFlag();
            myBikeAFlag.setCode("AAA-F-A");
            myBikeAFlag.setValue("I am RICH!");
            context.attach(myBikeAFlag);

            var myBikeA = new MyBikeA();
            myBikeA.setCode("AAA-A");
            myBikeA.setLightCode("AAA-L-A");
            myBikeA.setName("AAA-A号自行车");
            myBikeA.setFlagCode("AAA-F-A");
            context.attach(myBikeA);

            var myBikeAWheel1 = new BikeWheel();
            myBikeAWheel1.setCode("AAA-W-A-1");
            myBikeAWheel1.setBikeCode("AAA-A");

            var myBikeAWheel2 = new BikeWheel();
            myBikeAWheel2.setCode("AAA-W-A-2");
            myBikeAWheel2.setBikeCode("AAA-A");
            context.attach(myBikeAWheel1);
            context.attach(myBikeAWheel2);

            var myBikeBLight = new BikeLight();
            myBikeBLight.setCode("AAA-L-B");
            myBikeBLight.setValue(15);
            context.attach(myBikeBLight);

            var myBikeBBucket = new BikeBucket();
            myBikeBBucket.setCode("AAA-B-B");
            myBikeBBucket.setSp("500cm3");
            context.attach(myBikeBBucket);

            var myBikeB = new MyBikeB();
            myBikeB.setCode("AAA-B");
            myBikeB.setLightCode("AAA-L-B");
            myBikeB.setBucketCode("AAA-B-B");
            context.attach(myBikeB);

            var myBikeBWheel1 = new BikeWheel();
            myBikeBWheel1.setCode("AAA-W-B-1");
            myBikeBWheel1.setBikeCode("AAA-B");
            var myBikeBWheel2 = new BikeWheel();
            myBikeBWheel2.setCode("AAA-W-B-2");
            myBikeBWheel2.setBikeCode("AAA-B");

            context.attach(myBikeBWheel1);
            context.attach(myBikeBWheel2);

            var myBikeCLight = new BikeLight();
            myBikeCLight.setCode("AAA-L-C");
            myBikeCLight.setValue(15);
            context.attach(myBikeCLight);

            var myBikeCFlag = new BikeFlag();
            myBikeCFlag.setCode("AAA-F-C");
            myBikeCFlag.setValue("I am RICH!");
            context.attach(myBikeCFlag);

            MyBikeC myBikeC = new MyBikeC();
            myBikeC.setCode("AAA-C");
            myBikeC.setLightCode("AAA-L-C");
            myBikeC.setName("AAA-C号自行车");
            myBikeC.setFlagCode("AAA-F-C");
            myBikeC.setCanShared(true);
            context.attach(myBikeC);

            var myBikeCWheel1 = new BikeWheel();
            myBikeCWheel1.setCode("AAA-W-C-1");
            myBikeCWheel1.setBikeCode("AAA-C");

            var myBikeCWheel2 = new BikeWheel();
            myBikeCWheel2.setCode("AAA-W-C-2");
            myBikeCWheel2.setBikeCode("AAA-C");
            context.attach(myBikeCWheel1);
            context.attach(myBikeCWheel2);

            //保存
            context.saveChanges();

            //初始化一个活动
            var activity = new Activity();
            activity.setName("某活动");

            //构造两个不同的奖品
            var inKindPrize = new InKindPrize();
            inKindPrize.setName("某某奖品");

            var redEnvelope = new RedEnvelope();
            redEnvelope.setAmount(5);

            var luckyRedEnvelope = new LuckyRedEnvelope();
            luckyRedEnvelope.setAmount(5);
            luckyRedEnvelope.setActual(10);

            //建立关系
            List<Prize> prizes = new ArrayList<>();
            prizes.add(inKindPrize);
            prizes.add(redEnvelope);
            prizes.add(luckyRedEnvelope);
            activity.setPrizeList(prizes);

            //附加至上下文
            context.attach(activity);
            context.attach(inKindPrize);
            context.attach(redEnvelope);
            context.attach(luckyRedEnvelope);
            //保存
            context.saveChanges();

            //初始化一个对话
            var dialogue = new CustomerDialogue();
            dialogue.setTitle("客户对话");
            dialogue.setCustomerMemo("重要客户");
            dialogue.setCustomerName("客户F");

            //附加至上下文
            context.attach(dialogue);

            //初始化发言
            var words1 = new Words();
            words1.setContent("您好！");
            words1.setDialogue(dialogue);

            var words2 = new Words();
            words2.setContent("请问有什么可以帮您？");
            words2.setDialogue(dialogue);

            //附加至上下文
            context.attach(words1);
            context.attach(words2);

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
            context.createSet(Bike.class).delete(p -> p.getCode() != "", Bike.class);
            context.createSet(BikeLight.class).delete(p -> p.getCode() != "", BikeLight.class);
            context.createSet(BikeWheel.class).delete(p -> p.getCode() != "", BikeWheel.class);
            context.createSet(BikeFlag.class).delete(p -> p.getCode() != "", BikeFlag.class);
            context.createSet(BikeBucket.class).delete(p -> p.getCode() != "", BikeBucket.class);
            context.createSet(Prize.class).delete(p -> p.getId() > 0, Prize.class);
            context.createSet(Activity.class).delete(p -> p.getId() > 0, Activity.class);
            context.createSet(Words.class).delete(p -> p.getWordsId() > 0, Words.class);
            context.createSet(CustomerDialogue.class).delete(p -> p.getDialogueId() > 0, CustomerDialogue.class);
        }
    }

    /**
     * 继承测试
     * 测试的情景为A有B和C两个继承类 A,B,C均可被构造 并且在基类A内定义了用于区分具体类型的属性
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void curdTest1(EDataSource dataSource) {
        //查询验证
        var context = ContextUtils.createContext(dataSource);
        //一起查出来
        var bikeList = context.createSet(Bike.class).include(p -> p.getLight()).include(p -> p.getWheels()).toList();

        //有三个 分别是Bike MyBikeA MyBikeB
        assertNotNull(bikeList);
        assertEquals(4, bikeList.size());

        assertNotNull(bikeList.get(0));
        assertEquals(1, bikeList.get(0).getType());
        assertTrue(bikeList.get(1) instanceof MyBikeA);
        assertEquals(2, bikeList.get(1).getType());
        assertEquals("AAA-F-A", ((MyBikeA) bikeList.get(1)).getFlagCode());
        assertTrue(bikeList.get(2) instanceof MyBikeB);
        assertEquals(3, bikeList.get(2).getType());
        assertEquals("AAA-B-B", ((MyBikeB) bikeList.get(2)).getBucketCode());
        assertTrue(bikeList.get(3) instanceof MyBikeC);
        assertEquals(4, bikeList.get(3).getType());
        assertTrue(((MyBikeC) bikeList.get(3)).getCanShared());

        //检查关联对象
        for (Bike b : bikeList) {
            assertNotNull(b.getLight());
            assertNotNull(b.getWheels());
            assertEquals(2, b.getWheels().size());
        }

        //只查MyBikeA
        var myBikeAList = context.createSet(MyBikeA.class).include(p -> p.getLight()).include(p -> p.getWheels()).include(p -> p.getFlag()).toList();

        //两个 一个A 一个C
        assertNotNull(myBikeAList);
        assertEquals(2, myBikeAList.size());

        assertNotNull(myBikeAList.get(0));
        assertEquals(2, myBikeAList.get(0).getType());

        assertNotNull(myBikeAList.get(1));
        assertEquals(4, myBikeAList.get(1).getType());

        //检查关联对象
        assertNotNull(myBikeAList.get(0).getLight());
        assertNotNull(myBikeAList.get(0).getWheels());
        assertEquals(2, myBikeAList.get(0).getWheels().size());
        assertNotNull(myBikeAList.get(0).getFlag());

        assertNotNull(myBikeAList.get(1).getLight());
        assertNotNull(myBikeAList.get(1).getWheels());
        assertEquals(2, myBikeAList.get(1).getWheels().size());
        assertNotNull(myBikeAList.get(1).getFlag());

        //只查MyBikeB
        var myBikeBList = context.createSet(MyBikeB.class).include(p -> p.getLight()).include(p -> p.getWheels()).include(p -> p.getBucket()).toList();

        //只有一个
        assertNotNull(myBikeBList);
        assertEquals(1, myBikeBList.size());

        assertNotNull(myBikeBList.get(0));

        //检查关联对象
        assertNotNull(myBikeBList.get(0).getLight());
        assertNotNull(myBikeBList.get(0).getWheels());
        assertEquals(2, myBikeBList.get(0).getWheels().size());
        assertNotNull(myBikeBList.get(0).getBucket());

        //修改对象
        context = ContextUtils.createContext(dataSource);

        var qMyBikeA = context.createSet(MyBikeA.class).findFirst().orElse(null);

        assertNotNull(qMyBikeA);
        //修改普通属性
        qMyBikeA.setName("AAA-A号自行车-New");

        context.saveChanges();

        //检查修改的值
        qMyBikeA = context.createSet(MyBikeA.class).findFirst().orElse(null);

        assertNotNull(qMyBikeA);
        assertEquals("AAA-A号自行车-New", qMyBikeA.getName());

        //删除对象
        context = ContextUtils.createContext(dataSource);

        bikeList = context.createSet(Bike.class).toList();
        //移除
        for (Bike b : bikeList) {
            context.remove(b);
        }
        //保存
        context.saveChanges();

        //验证
        var count = context.createSet(Bike.class).count();

        assertEquals(0, count);
    }

    /**
     * 继承测试
     * 测试的情景为A有B和C两个继承类 A是抽象的 B,C可被构造 并且没有在基类A内定义了用于区分具体类型的属性
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void curdTest2(EDataSource dataSource) {
        //查询出来验证
        var context = ContextUtils.createContext(dataSource);
        //一并加载奖品
        var queryActivity = context.createSet(Activity.class).include(p -> p.getPrizeList()).findFirst().orElse(null);
        //不为空
        assertNotNull(queryActivity);
        //有两个奖品
        assertEquals(3, queryActivity.getPrizeList().size());
        //实体奖品有一个
        assertEquals(1, queryActivity.getPrizeList().stream().filter(p -> p instanceof InKindPrize).count());
        //是某某奖品
        assertEquals("某某奖品",
                queryActivity.getPrizeList().stream().filter(p -> p instanceof InKindPrize).map(p -> (InKindPrize) p).toList().get(0).getName());
        //红包有两个
        assertEquals(2, queryActivity.getPrizeList().stream().filter(p -> p instanceof RedEnvelope).count());
        //5块钱的红包
        assertEquals(5, queryActivity.getPrizeList().stream().filter(p -> p instanceof RedEnvelope).map(p -> (RedEnvelope) p).toList().get(0).getAmount());
        //10块钱的幸运红包
        assertEquals(10, queryActivity.getPrizeList().stream().filter(p -> p instanceof LuckyRedEnvelope).map(p -> (LuckyRedEnvelope) p).toList().get(0).getActual());

        //查询出来验证
        context = ContextUtils.createContext(dataSource);
        //根据某个具体类型查询
        var qInKindPrize = context.createSet(InKindPrize.class).findFirst().orElse(null);
        //不为空
        assertNotNull(qInKindPrize);
        assertEquals("某某奖品", qInKindPrize.getName());

        //根据某个具体类型查询
        var qRedEnvelope = context.createSet(RedEnvelope.class).findFirst().orElse(null);
        //不为空
        assertNotNull(qRedEnvelope);
        assertEquals(5, qRedEnvelope.getAmount());

        //根据某个具体类型查询
        var qLuckyRedEnvelope = context.createSet(LuckyRedEnvelope.class).findFirst().orElse(null);
        //不为空
        assertNotNull(qLuckyRedEnvelope);
        assertEquals(5, qLuckyRedEnvelope.getAmount());
        assertEquals(10, qLuckyRedEnvelope.getActual());

        //修改
        qInKindPrize.setName("某某奖品-New");
        qRedEnvelope.setAmount(2);
        qLuckyRedEnvelope.setAmount(10);
        qLuckyRedEnvelope.setActual(20);
        context.saveChanges();

        //查询出来验证
        context = ContextUtils.createContext(dataSource);
        //根据某个具体类型查询
        List<InKindPrize> qInKindPrizes = context.createSet(InKindPrize.class).toList();
        //不为空
        assertNotNull(qInKindPrizes);
        assertEquals(1, qInKindPrizes.size());
        assertEquals("某某奖品-New", qInKindPrizes.get(0).getName());

        //根据某个具体类型查询
        var qRedEnvelopes = context.createSet(RedEnvelope.class).toList();
        //不为空
        assertNotNull(qRedEnvelopes);
        assertEquals(2, qRedEnvelopes.size());
        assertEquals(2, qRedEnvelopes.get(0).getAmount());
        assertEquals(10, qRedEnvelopes.get(1).getAmount());
        assertEquals(20, ((LuckyRedEnvelope) qRedEnvelopes.get(1)).getActual());

        //删除
        context = ContextUtils.createContext(dataSource);
        //一并加载奖品
        var queryActivities = context.createSet(Activity.class).include(p -> p.getPrizeList()).toList();
        for (Activity query : queryActivities) {
            assertNotNull(query);
            context.remove(query);
            for (Prize priz : query.getPrizeList()) {
                assertNotNull(priz);
                context.remove(priz);
            }
        }
        context.saveChanges();
        //验证
        var count = context.createSet(Prize.class).count();
        assertEquals(0, count);

        count = context.createSet(Activity.class).count();
        assertEquals(0, count);
    }

    /**
     * 继承测试
     * 测试的情景为A引用B B有继承类B1 实际上使用的是B的子类B1
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void curdTest3(EDataSource dataSource) {
        //查询出来验证
        var context = ContextUtils.createContext(dataSource);
        //一并加载对话
        var queryWords = context.createSet(Words.class).include(p -> p.getDialogue()).findFirst().orElse(null);
        //不为空
        assertNotNull(queryWords);
        //对话为客户对话
        assertNotNull(queryWords.getDialogue());
        assertTrue(queryWords.getDialogue() instanceof CustomerDialogue);

        //测试使用关联属性查询
        queryWords = context.createSet(Words.class).include(p -> p.getDialogue()).findFirst(p -> p.getDialogue().getTitle() == "客户对话").orElse(null);
        //不为空
        assertNotNull(queryWords);
        //对话为客户对话
        assertNotNull(queryWords.getDialogue());
        assertTrue(queryWords.getDialogue() instanceof CustomerDialogue);
        //标题是客户对话
        assertEquals("客户对话", queryWords.getDialogue().getTitle());

        //测试使用关联属性带转换查询
        queryWords = context.createSet(Words.class).include(p -> p.getDialogue()).findFirst(p ->
                p.getDialogue().getTitle() == "客户对话" && ((CustomerDialogue) p.getDialogue()).getCustomerName() == "客户F").orElse(null);
        //不为空
        assertNotNull(queryWords);
        //对话为客户对话
        assertNotNull(queryWords.getDialogue());
        assertTrue(queryWords.getDialogue() instanceof CustomerDialogue);
        //标题是客户对话
        assertEquals("客户对话", queryWords.getDialogue().getTitle());
        //客户名称是客户F
        assertEquals("客户F", ((CustomerDialogue) queryWords.getDialogue()).getCustomerName());
    }
}
