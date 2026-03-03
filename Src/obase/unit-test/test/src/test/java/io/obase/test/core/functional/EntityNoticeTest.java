package io.obase.test.core.functional;

import com.alibaba.fastjson2.JSON;
import io.obase.core.mapping.pipeline.ChangeNoticeExtensions;
import io.obase.core.mapping.pipeline.DirectlyChangingNotice;
import io.obase.core.mapping.pipeline.EDirectlyChangeType;
import io.obase.core.mapping.pipeline.ObjectChangeNotice;
import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.functional.NoticeStudentInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实体通知测试
 */
@ExtendWith(ConfigSetUp.class)
public class EntityNoticeTest {

    /**
     * 文件路径
     */
    private final String path;

    /**
     * 初始化测试
     */
    public EntityNoticeTest() {
        File file = new File("");
        this.path = file.getAbsolutePath() + "/TestQueue.txt";
    }

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(NoticeStudentInfo.class).delete(p -> p.getStudentId() >= 0, NoticeStudentInfo.class);
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
            context.createSet(NoticeStudentInfo.class).delete(p -> p.getStudentId() >= 0, NoticeStudentInfo.class);
        }
    }

    /**
     * 测试对象变更通知
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void noticeTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        //启用修改消息通知
        ChangeNoticeExtensions.enableChangeNotice(context);
        //创建对象
        var studentInfo = new NoticeStudentInfo();
        studentInfo.setStudentId(888);
        studentInfo.setBackground("强大背景");
        studentInfo.setDescription("不可详查");
        //保存
        context.createSet(NoticeStudentInfo.class).attach(studentInfo);
        context.saveChanges();

        //读取变更通知
        var changeNotice = this.readChangeMessage();
        //读取到变更通知是创建通知
        assertNotNull(changeNotice);
        assertEquals(1, changeNotice.getObjectKeys().size());
        assertEquals(3, changeNotice.getAttributes().size());
        assertEquals("Create", changeNotice.getChangeAction());
        var description = changeNotice.getAttributes().stream().filter(p -> p.getAttribute().equalsIgnoreCase("Description")).findFirst().orElse(null);
        assertNotNull(description);
        assertEquals("不可详查", description.getValue().toString());
        var background = changeNotice.getAttributes().stream().filter(p -> p.getAttribute().equalsIgnoreCase("Background")).findFirst().orElse(null);
        assertNotNull(background);
        assertEquals("强大背景", background.getValue().toString());
        //修改对象
        studentInfo.setBackground("神秘背景");
        context.saveChanges();

        //读取变更通知
        changeNotice = this.readChangeMessage();
        //读取到变更通知是修改通知
        assertNotNull(changeNotice);
        assertEquals(1, changeNotice.getObjectKeys().size());
        assertEquals(3, changeNotice.getAttributes().size());
        assertEquals("Update", changeNotice.getChangeAction());
        description = changeNotice.getAttributes().stream().filter(p -> p.getAttribute().equalsIgnoreCase("Description")).findFirst().orElse(null);
        assertNotNull(description);
        assertEquals("不可详查", description.getValue().toString());
        background = changeNotice.getAttributes().stream().filter(p -> p.getAttribute().equalsIgnoreCase("Background")).findFirst().orElse(null);
        assertNotNull(background);
        assertEquals("神秘背景", background.getValue().toString());

        //标记删除
        context.createSet(NoticeStudentInfo.class).remove(studentInfo);
        context.saveChanges();

        //读取变更通知
        changeNotice = this.readChangeMessage();
        //读取到变更通知是删除通知
        assertNotNull(changeNotice);
        assertEquals(1, changeNotice.getObjectKeys().size());
        assertEquals(3, changeNotice.getAttributes().size());
        assertEquals("Delete", changeNotice.getChangeAction());
        description = changeNotice.getAttributes().stream().filter(p -> p.getAttribute().equalsIgnoreCase("Description")).findFirst().orElse(null);
        assertNotNull(description);
        assertEquals("不可详查", description.getValue().toString());
        background = changeNotice.getAttributes().stream().filter(p -> p.getAttribute().equalsIgnoreCase("Background")).findFirst().orElse(null);
        assertNotNull(background);
        assertEquals("神秘背景", background.getValue().toString());

        //直接修改对象
        Map<String, Object> map = new HashMap<>();
        map.put("Background", "极度强大");
        map.put("Description", "无法估计");
        context.createSet(NoticeStudentInfo.class).setAttributes(map, p -> p.getStudentId() == 888, NoticeStudentInfo.class);

        var directlyChangeNotice = this.readDirectMessage();
        assertNotNull(directlyChangeNotice);
        assertEquals(EDirectlyChangeType.Update, directlyChangeNotice.getDirectlyChangeType());
        assertEquals("极度强大", directlyChangeNotice.getNewValues().get("Background"));
        assertEquals("无法估计", directlyChangeNotice.getNewValues().get("Description"));
    }

    /**
     * 转换为变更通知对象
     *
     * @return 变更通知对象
     */
    private ObjectChangeNotice readChangeMessage() {
        return JSON.parseObject(this.readMessage(), ObjectChangeNotice.class);
    }

    /**
     * 转换为直接通知对象
     *
     * @return 直接通知对象
     */
    private DirectlyChangingNotice readDirectMessage() {
        return JSON.parseObject(this.readMessage(), DirectlyChangingNotice.class);
    }

    /**
     * 读取通知字节数组 并清空Txt文件用于模拟队列
     *
     * @return 读取结果
     */
    private String readMessage() {

        var fromFile = new File(this.path);
        try (var fileStream = Files.newInputStream(fromFile.toPath())) {

            var bf = new BufferedReader(new InputStreamReader(fileStream));
            var result = bf.readLine();
            var isDel = fromFile.delete();
            if (!isDel)
                throw new RuntimeException("未能清理模拟队列文件.");
            bf.close();

            return result;
        } catch (IOException e) {
            fail(e.getMessage(), e);
            return null;
        }
    }
}
