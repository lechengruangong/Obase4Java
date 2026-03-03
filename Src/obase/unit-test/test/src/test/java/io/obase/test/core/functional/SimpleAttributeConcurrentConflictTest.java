package io.obase.test.core.functional;

import io.obase.core.saving.RepeatCreationException;
import io.obase.core.saving.UnSupportedException;
import io.obase.core.saving.VersionConflictException;
import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.functional.keyValueVersion.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简单属性测试并发冲突策略
 */
@ExtendWith(ConfigSetUp.class)
public class SimpleAttributeConcurrentConflictTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);
            //销毁所有旧对象
            context.createSet(ThrowExceptionKeyValue.class).delete(p -> true, ThrowExceptionKeyValue.class);
        }
    }

    /**
     * 销毁方法
     */
    @AfterAll
    public static void afterAll() {
        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);
            //销毁所有旧对象
            context.createSet(ThrowExceptionKeyValue.class).delete(p -> true, ThrowExceptionKeyValue.class);
        }
    }

    /**
     * 测试 简单属性 合并策略 累加处理
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void simpleAttributeAccumulateCombineConcurrentConflict(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var accumulateCombineKeyValue = new AccumulateCombineKeyValue();
        accumulateCombineKeyValue.setKey("Key");
        accumulateCombineKeyValue.setValue(5);
        accumulateCombineKeyValue.setId(1);
        accumulateCombineKeyValue.setVersionKey(1);

        //附加
        context.createSet(AccumulateCombineKeyValue.class).attach(accumulateCombineKeyValue);
        context.saveChanges();

        accumulateCombineKeyValue = new AccumulateCombineKeyValue();
        accumulateCombineKeyValue.setKey("Key");
        accumulateCombineKeyValue.setValue(5);
        accumulateCombineKeyValue.setId(1);
        accumulateCombineKeyValue.setVersionKey(1);

        AccumulateCombineKeyValue queryAccumulateKeyValue;
        context = ContextUtils.createContext(dataSource);
        if (dataSource == EDataSource.MySql || dataSource == EDataSource.Sqlite || dataSource == EDataSource.SqlServer) {
            context.createSet(AccumulateCombineKeyValue.class).attach(accumulateCombineKeyValue);
            context.saveChanges();

            context = ContextUtils.createContext(dataSource);
            queryAccumulateKeyValue = context.createSet(AccumulateCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);

            //被累加至5
            assertNotNull(queryAccumulateKeyValue);
            assertEquals(10, queryAccumulateKeyValue.getValue());
        } else {

            try {
                context.createSet(AccumulateCombineKeyValue.class).attach(accumulateCombineKeyValue);
                context.saveChanges();
            } catch (Exception ex) {
                assertTrue(ex instanceof UnSupportedException);
            }
            context = ContextUtils.createContext(dataSource);
            queryAccumulateKeyValue = context.createSet(AccumulateCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
        }

        //修改
        queryAccumulateKeyValue.setValue(9);
        //模拟一个版本键被修改
        HashMap<String, Object> map = new HashMap<>();
        map.put("versionKey", 2);
        context.createSet(AccumulateCombineKeyValue.class).setAttributes(map, p -> p.getId() == 1, AccumulateCombineKeyValue.class);

        //会被累加
        context.saveChanges();

        //重新查出来
        context = ContextUtils.createContext(dataSource);
        queryAccumulateKeyValue = context.createSet(AccumulateCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
        //累加至9
        assertNotNull(queryAccumulateKeyValue);
        assertEquals(9, queryAccumulateKeyValue.getValue());

        context.createSet(AccumulateCombineKeyValue.class).delete(p -> p.getKey() != "", AccumulateCombineKeyValue.class);
    }

    /**
     * 测试 简单属性 合并策略 忽略处理
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void simpleAttributeIgnoreCombineConcurrentConflict(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var accumulateCombineKeyValue = new IgnoreCombineKeyValue();
        accumulateCombineKeyValue.setKey("Key");
        accumulateCombineKeyValue.setValue(5);
        accumulateCombineKeyValue.setId(1);
        accumulateCombineKeyValue.setVersionKey(1);

        //附加
        context.createSet(IgnoreCombineKeyValue.class).attach(accumulateCombineKeyValue);
        context.saveChanges();

        accumulateCombineKeyValue = new IgnoreCombineKeyValue();
        accumulateCombineKeyValue.setKey("Key");
        accumulateCombineKeyValue.setValue(5);
        accumulateCombineKeyValue.setId(1);
        accumulateCombineKeyValue.setVersionKey(1);

        context = ContextUtils.createContext(dataSource);

        IgnoreCombineKeyValue queryAccumulateKeyValue;
        if (dataSource == EDataSource.MySql || dataSource == EDataSource.Sqlite || dataSource == EDataSource.SqlServer) {

            context.createSet(IgnoreCombineKeyValue.class).attach(accumulateCombineKeyValue);
            context.saveChanges();

            context = ContextUtils.createContext(dataSource);
            queryAccumulateKeyValue = context.createSet(IgnoreCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
            //被累加至5
            assertNotNull(queryAccumulateKeyValue);
            assertEquals(5, queryAccumulateKeyValue.getValue());

        } else {

            try {
                context.createSet(IgnoreCombineKeyValue.class).attach(accumulateCombineKeyValue);
                context.saveChanges();
            } catch (Exception ex) {
                assertTrue(ex instanceof UnSupportedException);
            }
            context = ContextUtils.createContext(dataSource);
            queryAccumulateKeyValue = context.createSet(IgnoreCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
        }


        //修改
        queryAccumulateKeyValue.setValue(9);
        //模拟一个版本键被修改
        HashMap<String, Object> map = new HashMap<>();
        map.put("versionKey", 2);
        context.createSet(IgnoreCombineKeyValue.class).setAttributes(map, p -> p.getId() == 1, IgnoreCombineKeyValue.class);
        //会被累加
        context.saveChanges();

        //重新查出来
        context = ContextUtils.createContext(dataSource);
        queryAccumulateKeyValue = context.createSet(IgnoreCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
        //累加至9
        assertNotNull(queryAccumulateKeyValue);
        assertEquals(5, queryAccumulateKeyValue.getValue());

        context.createSet(IgnoreCombineKeyValue.class).delete(p -> p.getKey() != "", IgnoreCombineKeyValue.class);
    }

    /**
     * 测试 简单属性 忽略策略
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void simpleAttributeIgnoreConcurrentConflict(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var accumulateCombineKeyValue = new IgnoreKeyValue();
        accumulateCombineKeyValue.setKey("Key");
        accumulateCombineKeyValue.setValue(1);
        accumulateCombineKeyValue.setId(1);
        accumulateCombineKeyValue.setVersionKey(1);

        //附加
        context.createSet(IgnoreKeyValue.class).attach(accumulateCombineKeyValue);
        context.saveChanges();

        accumulateCombineKeyValue = new IgnoreKeyValue();
        accumulateCombineKeyValue.setKey("Key");
        accumulateCombineKeyValue.setValue(2);
        accumulateCombineKeyValue.setId(1);
        accumulateCombineKeyValue.setVersionKey(1);

        context = ContextUtils.createContext(dataSource);
        if (dataSource == EDataSource.MySql || dataSource == EDataSource.Sqlite || dataSource == EDataSource.SqlServer) {
            context.createSet(IgnoreKeyValue.class).attach(accumulateCombineKeyValue);
            context.saveChanges();
        } else {
            try {
                context.createSet(IgnoreKeyValue.class).attach(accumulateCombineKeyValue);
                context.saveChanges();
            } catch (Exception ex) {
                assertTrue(ex instanceof UnSupportedException);
            }
        }

        context = ContextUtils.createContext(dataSource);
        var queryAccumulateKeyValue = context.createSet(IgnoreKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
        //被累加至5
        assertNotNull(queryAccumulateKeyValue);
        assertEquals(1, queryAccumulateKeyValue.getValue());

        //修改
        queryAccumulateKeyValue.setValue(2);
        //模拟一个版本键被修改
        HashMap<String, Object> map = new HashMap<>();
        map.put("versionKey", 2);
        context.createSet(IgnoreKeyValue.class).setAttributes(map, p -> p.getId() == 1, IgnoreKeyValue.class);
        //会被累加
        context.saveChanges();

        //重新查出来
        context = ContextUtils.createContext(dataSource);
        queryAccumulateKeyValue = context.createSet(IgnoreKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
        //累加至9
        assertNotNull(queryAccumulateKeyValue);
        assertEquals(1, queryAccumulateKeyValue.getValue());

        context.createSet(IgnoreKeyValue.class).delete(p -> p.getKey() != "", IgnoreKeyValue.class);
    }

    /**
     * 测试 简单属性 合并策略 覆盖处理
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void simpleAttributeOverWriteCombineConcurrentConflict(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var accumulateCombineKeyValue = new OverwriteCombineKeyValue();
        accumulateCombineKeyValue.setKey("Key");
        accumulateCombineKeyValue.setValue(1);
        accumulateCombineKeyValue.setId(1);
        accumulateCombineKeyValue.setVersionKey(1);

        //附加
        context.createSet(OverwriteCombineKeyValue.class).attach(accumulateCombineKeyValue);
        context.saveChanges();

        accumulateCombineKeyValue = new OverwriteCombineKeyValue();
        accumulateCombineKeyValue.setKey("Key");
        accumulateCombineKeyValue.setValue(2);
        accumulateCombineKeyValue.setId(1);
        accumulateCombineKeyValue.setVersionKey(1);

        context = ContextUtils.createContext(dataSource);

        OverwriteCombineKeyValue queryAccumulateKeyValue;
        if (dataSource == EDataSource.MySql || dataSource == EDataSource.Sqlite || dataSource == EDataSource.SqlServer) {
            context.createSet(OverwriteCombineKeyValue.class).attach(accumulateCombineKeyValue);
            context.saveChanges();

            context = ContextUtils.createContext(dataSource);
            queryAccumulateKeyValue = context.createSet(OverwriteCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
            //被累加至5
            assertNotNull(queryAccumulateKeyValue);
            assertEquals(2, queryAccumulateKeyValue.getValue());
        } else {
            try {
                context.createSet(OverwriteCombineKeyValue.class).attach(accumulateCombineKeyValue);
                context.saveChanges();
            } catch (Exception ex) {
                assertTrue(ex instanceof UnSupportedException);
            }
            context = ContextUtils.createContext(dataSource);
            queryAccumulateKeyValue = context.createSet(OverwriteCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
        }

        //修改
        queryAccumulateKeyValue.setValue(9);
        //模拟一个版本键被修改
        HashMap<String, Object> map = new HashMap<>();
        map.put("versionKey", 2);
        context.createSet(OverwriteCombineKeyValue.class).setAttributes(map, p -> p.getId() == 1, OverwriteCombineKeyValue.class);
        //会被累加
        context.saveChanges();

        //重新查出来
        context = ContextUtils.createContext(dataSource);
        queryAccumulateKeyValue = context.createSet(OverwriteCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
        //累加至9
        assertNotNull(queryAccumulateKeyValue);
        assertEquals(9, queryAccumulateKeyValue.getValue());

        context.createSet(OverwriteCombineKeyValue.class).delete(p -> p.getKey() != "", OverwriteCombineKeyValue.class);
    }

    /**
     * 测试 简单属性 覆盖策略
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void simpleAttributeOverWriteConcurrentConflict(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var accumulateCombineKeyValue = new OverwriteKeyValue();
        accumulateCombineKeyValue.setKey("Key");
        accumulateCombineKeyValue.setValue(1);
        accumulateCombineKeyValue.setId(1);
        accumulateCombineKeyValue.setVersionKey(1);

        //附加
        context.createSet(OverwriteKeyValue.class).attach(accumulateCombineKeyValue);
        context.saveChanges();

        accumulateCombineKeyValue = new OverwriteKeyValue();
        accumulateCombineKeyValue.setKey("Key");
        accumulateCombineKeyValue.setValue(2);
        accumulateCombineKeyValue.setId(1);
        accumulateCombineKeyValue.setVersionKey(1);

        context = ContextUtils.createContext(dataSource);

        OverwriteKeyValue queryAccumulateKeyValue;
        if (dataSource == EDataSource.MySql || dataSource == EDataSource.Sqlite || dataSource == EDataSource.SqlServer) {

            context.createSet(OverwriteKeyValue.class).attach(accumulateCombineKeyValue);
            context.saveChanges();

            context = ContextUtils.createContext(dataSource);
            queryAccumulateKeyValue = context.createSet(OverwriteKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
            //被累加至5
            assertNotNull(queryAccumulateKeyValue);
            assertEquals(2, queryAccumulateKeyValue.getValue());
        } else {

            try {
                context.createSet(OverwriteKeyValue.class).attach(accumulateCombineKeyValue);
                context.saveChanges();
            } catch (Exception ex) {
                assertTrue(ex instanceof UnSupportedException);
            }
            context = ContextUtils.createContext(dataSource);
            queryAccumulateKeyValue = context.createSet(OverwriteKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
        }


        //修改
        queryAccumulateKeyValue.setValue(3);
        //模拟一个版本键被修改
        HashMap<String, Object> map = new HashMap<>();
        map.put("versionKey", 2);
        context.createSet(OverwriteKeyValue.class).setAttributes(map, p -> p.getId() == 1, OverwriteKeyValue.class);
        //会被累加
        context.saveChanges();

        //重新查出来
        context = ContextUtils.createContext(dataSource);
        queryAccumulateKeyValue = context.createSet(OverwriteKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
        //累加至9
        assertNotNull(queryAccumulateKeyValue);
        assertEquals(3, queryAccumulateKeyValue.getValue());

        context.createSet(OverwriteKeyValue.class).delete(p -> p.getKey() != "", OverwriteKeyValue.class);
    }

    /**
     * 测试 简单属性 重建策略
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void simpleAttributeReconstructConcurrentConflict(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var accumulateCombineKeyValue = new ReconstructKeyValue();
        accumulateCombineKeyValue.setKey("Key");
        accumulateCombineKeyValue.setValue(1);
        accumulateCombineKeyValue.setId(1);
        accumulateCombineKeyValue.setVersionKey(1);

        //附加
        context.createSet(ReconstructKeyValue.class).attach(accumulateCombineKeyValue);
        context.saveChanges();

        ReconstructKeyValue queryAccumulateKeyValue;
        context = ContextUtils.createContext(dataSource);
        queryAccumulateKeyValue = context.createSet(ReconstructKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
        //被累加至5
        assertNotNull(queryAccumulateKeyValue);
        assertEquals(1, queryAccumulateKeyValue.getValue());

        //修改
        queryAccumulateKeyValue.setValue(4);
        //模拟一个版本键被修改
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", 2);
        context.createSet(ReconstructKeyValue.class).setAttributes(map, p -> p.getId() == 1, ReconstructKeyValue.class);
        //会被累加
        context.saveChanges();

        //重新查出来
        context = ContextUtils.createContext(dataSource);
        queryAccumulateKeyValue = context.createSet(ReconstructKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
        //累加至9
        assertNotNull(queryAccumulateKeyValue);
        assertEquals(4, queryAccumulateKeyValue.getValue());

        queryAccumulateKeyValue = context.createSet(ReconstructKeyValue.class).findFirst(p -> p.getId() == 2).orElse(null);
        //累加至9
        assertNotNull(queryAccumulateKeyValue);
        assertEquals(1, queryAccumulateKeyValue.getValue());

        context.createSet(ReconstructKeyValue.class).delete(p -> p.getKey() != "", ReconstructKeyValue.class);
    }

    /**
     * 测试 简单属性 抛出异常策略
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void SimpleAttributeThrowExceptionConcurrentConflict(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var accumulateCombineKeyValue = new ThrowExceptionKeyValue();
        accumulateCombineKeyValue.setKey("Key");
        accumulateCombineKeyValue.setValue(1);
        accumulateCombineKeyValue.setId(1);
        accumulateCombineKeyValue.setVersionKey(1);

        //附加
        context.createSet(ThrowExceptionKeyValue.class).attach(accumulateCombineKeyValue);
        context.saveChanges();

        context = ContextUtils.createContext(dataSource);
        accumulateCombineKeyValue = new ThrowExceptionKeyValue();
        accumulateCombineKeyValue.setKey("Key");
        accumulateCombineKeyValue.setValue(2);
        accumulateCombineKeyValue.setId(1);
        accumulateCombineKeyValue.setVersionKey(1);

        context.createSet(ThrowExceptionKeyValue.class).attach(accumulateCombineKeyValue);
        try {
            context.saveChanges();
        } catch (Exception ex) {
            assertTrue(ex instanceof RepeatCreationException);
        }

        context = ContextUtils.createContext(dataSource);
        ThrowExceptionKeyValue queryAccumulateKeyValue = context.createSet(ThrowExceptionKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
        //被累加至5
        assertNotNull(queryAccumulateKeyValue);
        assertEquals(1, queryAccumulateKeyValue.getValue());

        //修改
        queryAccumulateKeyValue.setValue(3);
        //模拟一个版本键被修改
        HashMap<String, Object> map = new HashMap<>();
        map.put("versionKey", 2);
        context.createSet(ThrowExceptionKeyValue.class).setAttributes(map, p -> p.getId() == 1, ThrowExceptionKeyValue.class);
        //会被累加
        try {
            context.saveChanges();
        } catch (Exception ex) {
            assertTrue(ex instanceof VersionConflictException);
        }

        context.createSet(ThrowExceptionKeyValue.class).delete(p -> p.getKey() != "", ThrowExceptionKeyValue.class);
    }
}
