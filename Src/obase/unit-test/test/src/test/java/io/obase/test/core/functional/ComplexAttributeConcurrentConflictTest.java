package io.obase.test.core.functional;

import io.obase.core.saving.RepeatCreationException;
import io.obase.core.saving.UnSupportedException;
import io.obase.core.saving.VersionConflictException;
import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.functional.complexKeyValueWithVersion.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 复杂属性测试并发冲突策略
 */
@ExtendWith(ConfigSetUp.class)
public class ComplexAttributeConcurrentConflictTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);
            //销毁所有旧对象
            context.createSet(ComplexIgnoreKeyValue.class).delete(p -> true, ComplexIgnoreKeyValue.class);
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
            context.createSet(ComplexIgnoreKeyValue.class).delete(p -> true, ComplexIgnoreKeyValue.class);
        }
    }

    /**
     * 测试 复杂属性 忽略策略
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void complexAttributeIgnoreConcurrentConflict(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var value = new ComplexIgnoreKeyValue();
        value.setId(1);
        var complexKeyValue = new ComplexKeyValue();
        complexKeyValue.setKey("Key");
        complexKeyValue.setValue(1);
        value.setKeyValue(complexKeyValue);
        value.setVersionKey(1);

        //附加
        context.createSet(ComplexIgnoreKeyValue.class).attach(value);
        context.saveChanges();

        //重复插入 异常忽略
        value = new ComplexIgnoreKeyValue();
        value.setId(1);
        complexKeyValue = new ComplexKeyValue();
        complexKeyValue.setKey("Key");
        complexKeyValue.setValue(2);
        value.setKeyValue(complexKeyValue);
        value.setVersionKey(1);

        context = ContextUtils.createContext(dataSource);

        if (dataSource == EDataSource.MySql || dataSource == EDataSource.Sqlite || dataSource == EDataSource.SqlServer) {
            //附加
            context.createSet(ComplexIgnoreKeyValue.class).attach(value);
            context.saveChanges();
        } else {
            try {
                //附加
                context.createSet(ComplexIgnoreKeyValue.class).attach(value);
                context.saveChanges();
            } catch (Exception ex) {
                assertTrue(ex instanceof UnSupportedException);
            }
        }

        context = ContextUtils.createContext(dataSource);
        var queryIgnoreComplexKeyValue = context.createSet(ComplexIgnoreKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);

        assertNotNull(queryIgnoreComplexKeyValue);
        assertEquals(1, queryIgnoreComplexKeyValue.getKeyValue().getValue());

        //修改
        queryIgnoreComplexKeyValue.setKeyValue(new ComplexKeyValue());
        queryIgnoreComplexKeyValue.getKeyValue().setValue(2);
        queryIgnoreComplexKeyValue.getKeyValue().setKey("Key");
        //模拟一个版本键被修改
        HashMap<String, Object> map = new HashMap<>();
        map.put("versionKey", 2);
        context.createSet(ComplexIgnoreKeyValue.class).setAttributes(map, p -> p.getId() == 1, ComplexIgnoreKeyValue.class);
        //会被累加
        context.saveChanges();

        context = ContextUtils.createContext(dataSource);
        //重新查出来
        queryIgnoreComplexKeyValue = context.createSet(ComplexIgnoreKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);

        assertNotNull(queryIgnoreComplexKeyValue);
        assertEquals(1, queryIgnoreComplexKeyValue.getKeyValue().getValue());

        context.createSet(ComplexIgnoreKeyValue.class).delete(p -> p.getId() > 0, ComplexIgnoreKeyValue.class);
    }

    /**
     * 测试 复杂属性 忽略策略
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void complexAttributeThrowExceptionConcurrentConflict(EDataSource dataSource) {

        var context = ContextUtils.createContext(dataSource);

        var value = new ComplexThrowExceptionKeyValue();
        value.setId(1);
        var complexKeyValue = new ComplexKeyValue();
        complexKeyValue.setKey("Key");
        complexKeyValue.setValue(1);
        value.setKeyValue(complexKeyValue);
        value.setVersionKey(1);

        //附加
        context.createSet(ComplexThrowExceptionKeyValue.class).attach(value);
        context.saveChanges();

        context = ContextUtils.createContext(dataSource);
        //重复插入 异常忽略
        value = new ComplexThrowExceptionKeyValue();
        value.setId(1);
        complexKeyValue = new ComplexKeyValue();
        complexKeyValue.setKey("Key");
        complexKeyValue.setValue(2);
        value.setKeyValue(complexKeyValue);
        value.setVersionKey(1);

        context.createSet(ComplexThrowExceptionKeyValue.class).attach(value);
        try {
            context.saveChanges();
        } catch (Exception ex) {
            assertTrue(ex instanceof RepeatCreationException);
        }


        context = ContextUtils.createContext(dataSource);
        var queryAccumulateKeyValue = context.createSet(ComplexThrowExceptionKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
        //被累加至5
        assertNotNull(queryAccumulateKeyValue);
        assertEquals(1, queryAccumulateKeyValue.getKeyValue().getValue());

        //修改
        queryAccumulateKeyValue.setKeyValue(new ComplexKeyValue());
        queryAccumulateKeyValue.getKeyValue().setValue(3);
        //模拟一个版本键被修改
        HashMap<String, Object> map = new HashMap<>();
        map.put("versionKey", 2);
        context.createSet(ComplexThrowExceptionKeyValue.class).setAttributes(map, p -> p.getId() == 1, ComplexThrowExceptionKeyValue.class);
        //会被累加
        try {
            context.saveChanges();
        } catch (Exception ex) {
            assertTrue(ex instanceof VersionConflictException);
        }

        context.createSet(ComplexThrowExceptionKeyValue.class).delete(p -> p.getId() > 0, ComplexThrowExceptionKeyValue.class);
    }

    /**
     * 测试 复杂属性 覆盖策略
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void complexAttributeOverWriteConcurrentConflict(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var value = new ComplexOverwriteKeyValue();
        value.setId(1);
        ComplexKeyValue complexKeyValue = new ComplexKeyValue();
        complexKeyValue.setKey("Key");
        complexKeyValue.setValue(1);
        value.setKeyValue(complexKeyValue);
        value.setVersionKey(1);

        //附加
        context.createSet(ComplexOverwriteKeyValue.class).attach(value);
        context.saveChanges();

        context = ContextUtils.createContext(dataSource);
        //重复插入 异常忽略
        value = new ComplexOverwriteKeyValue();
        value.setId(1);
        complexKeyValue = new ComplexKeyValue();
        complexKeyValue.setKey("Key");
        complexKeyValue.setValue(2);
        value.setKeyValue(complexKeyValue);
        value.setVersionKey(1);

        ComplexOverwriteKeyValue queryIgnoreComplexKeyValue;
        if (dataSource == EDataSource.MySql || dataSource == EDataSource.Sqlite || dataSource == EDataSource.SqlServer) {
            //附加
            context.createSet(ComplexOverwriteKeyValue.class).attach(value);
            context.saveChanges();

            context = ContextUtils.createContext(dataSource);
            queryIgnoreComplexKeyValue = context.createSet(ComplexOverwriteKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);

            assertNotNull(queryIgnoreComplexKeyValue);
            assertEquals(2, queryIgnoreComplexKeyValue.getKeyValue().getValue());

        } else {
            try {
                //附加
                context.createSet(ComplexOverwriteKeyValue.class).attach(value);
                context.saveChanges();
            } catch (Exception ex) {
                assertTrue(ex instanceof UnSupportedException);
            }

            context = ContextUtils.createContext(dataSource);
            queryIgnoreComplexKeyValue = context.createSet(ComplexOverwriteKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
            assertNotNull(queryIgnoreComplexKeyValue);
        }

        //修改
        queryIgnoreComplexKeyValue.setKeyValue(new ComplexKeyValue());
        queryIgnoreComplexKeyValue.getKeyValue().setValue(2);
        queryIgnoreComplexKeyValue.getKeyValue().setKey("Key");
        //模拟一个版本键被修改
        HashMap<String, Object> map = new HashMap<>();
        map.put("versionKey", 2);
        context.createSet(ComplexOverwriteKeyValue.class).setAttributes(map, p -> p.getId() == 1, ComplexOverwriteKeyValue.class);
        //会被累加
        context.saveChanges();

        context = ContextUtils.createContext(dataSource);
        //重新查出来
        queryIgnoreComplexKeyValue = context.createSet(ComplexOverwriteKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);

        assertNotNull(queryIgnoreComplexKeyValue);
        assertEquals(2, queryIgnoreComplexKeyValue.getKeyValue().getValue());

        context.createSet(ComplexOverwriteKeyValue.class).delete(p -> p.getId() > 0, ComplexOverwriteKeyValue.class);
    }

    /**
     * 测试 复杂属性 重建策略
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void complexAttributeReconstructConcurrentConflict(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var value = new ComplexReconstructKeyValue();
        value.setId(1);
        var complexKeyValue = new ComplexKeyValue();
        complexKeyValue.setKey("Key");
        complexKeyValue.setValue(1);
        value.setKeyValue(complexKeyValue);
        value.setVersionKey(1);

        //附加
        context.createSet(ComplexReconstructKeyValue.class).attach(value);
        context.saveChanges();

        ComplexReconstructKeyValue queryAccumulateKeyValue;
        context = ContextUtils.createContext(dataSource);
        queryAccumulateKeyValue = context.createSet(ComplexReconstructKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
        //被累加至5
        assertNotNull(queryAccumulateKeyValue);
        assertEquals(1, queryAccumulateKeyValue.getKeyValue().getValue());

        //修改
        queryAccumulateKeyValue.setVersionKey(4);
        //模拟一个版本键被修改
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", 2);
        context.createSet(ComplexReconstructKeyValue.class).setAttributes(map, p -> p.getId() == 1, ComplexReconstructKeyValue.class);
        //会被累加
        context.saveChanges();

        //重新查出来
        context = ContextUtils.createContext(dataSource);
        queryAccumulateKeyValue = context.createSet(ComplexReconstructKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
        //累加至9
        assertNotNull(queryAccumulateKeyValue);
        assertEquals(4, queryAccumulateKeyValue.getVersionKey());

        queryAccumulateKeyValue = context.createSet(ComplexReconstructKeyValue.class).findFirst(p -> p.getId() == 2).orElse(null);
        //累加至9
        assertNotNull(queryAccumulateKeyValue);
        assertEquals(1, queryAccumulateKeyValue.getVersionKey());

        context.createSet(ComplexReconstructKeyValue.class).delete(p -> p.getId() > 0, ComplexReconstructKeyValue.class);
    }

    /**
     * 测试 复杂属性 合并策略 累加处理
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void complexAttributeAccumulateCombineConcurrentConflict(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var value = new ComplexAccumulateCombineKeyValue();
        value.setId(1);
        var complexKeyValue = new AccumulateCombineComplexKeyValue();
        complexKeyValue.setKey("Key");
        complexKeyValue.setValue(5);
        value.setKeyValue(complexKeyValue);
        value.setVersionKey(1);

        //附加
        context.createSet(ComplexAccumulateCombineKeyValue.class).attach(value);
        context.saveChanges();

        //重复插入 异常忽略
        value = new ComplexAccumulateCombineKeyValue();
        value.setId(1);
        complexKeyValue = new AccumulateCombineComplexKeyValue();
        complexKeyValue.setKey("Key");
        complexKeyValue.setValue(2);
        value.setKeyValue(complexKeyValue);
        value.setVersionKey(1);

        context = ContextUtils.createContext(dataSource);

        ComplexAccumulateCombineKeyValue queryIgnoreComplexKeyValue;
        if (dataSource == EDataSource.MySql || dataSource == EDataSource.Sqlite || dataSource == EDataSource.SqlServer) {
            //附加
            context.createSet(ComplexAccumulateCombineKeyValue.class).attach(value);
            context.saveChanges();

            context = ContextUtils.createContext(dataSource);
            queryIgnoreComplexKeyValue = context.createSet(ComplexAccumulateCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);

            assertNotNull(queryIgnoreComplexKeyValue);
            assertEquals(7, queryIgnoreComplexKeyValue.getKeyValue().getValue());
        } else {

            try {
                //附加
                context.createSet(ComplexAccumulateCombineKeyValue.class).attach(value);
                context.saveChanges();
            } catch (Exception ex) {
                assertTrue(ex instanceof UnSupportedException);
            }

            context = ContextUtils.createContext(dataSource);
            queryIgnoreComplexKeyValue = context.createSet(ComplexAccumulateCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
            assertNotNull(queryIgnoreComplexKeyValue);
        }

        //修改
        queryIgnoreComplexKeyValue.setKeyValue(new AccumulateCombineComplexKeyValue());
        queryIgnoreComplexKeyValue.getKeyValue().setValue(7);
        queryIgnoreComplexKeyValue.getKeyValue().setKey("Key");
        //模拟一个版本键被修改
        HashMap<String, Object> map = new HashMap<>();
        map.put("versionKey", 2);
        context.createSet(ComplexAccumulateCombineKeyValue.class).setAttributes(map, p -> p.getId() == 1, ComplexAccumulateCombineKeyValue.class);
        //会被累加
        context.saveChanges();

        context = ContextUtils.createContext(dataSource);
        //重新查出来
        queryIgnoreComplexKeyValue = context.createSet(ComplexAccumulateCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);

        assertNotNull(queryIgnoreComplexKeyValue);
        if (dataSource == EDataSource.MySql || dataSource == EDataSource.Sqlite || dataSource == EDataSource.SqlServer) {
            assertEquals(14, queryIgnoreComplexKeyValue.getKeyValue().getValue());
        } else {
            assertEquals(7, queryIgnoreComplexKeyValue.getKeyValue().getValue());
        }


        context.createSet(ComplexAccumulateCombineKeyValue.class).delete(p -> p.getId() > 0, ComplexAccumulateCombineKeyValue.class);
    }

    /**
     * 测试 简单属性 合并策略 忽略处理
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void complexAttributeIgnoreCombineConcurrentConflict(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var value = new ComplexIgnoreCombineKeyValue();
        value.setId(1);
        var complexKeyValue = new IgnoreCombineComplexKeyValue();
        complexKeyValue.setKey("Key");
        complexKeyValue.setValue(5);
        value.setKeyValue(complexKeyValue);
        value.setVersionKey(1);

        //附加
        context.createSet(ComplexIgnoreCombineKeyValue.class).attach(value);
        context.saveChanges();

        //重复插入 异常忽略
        value = new ComplexIgnoreCombineKeyValue();
        value.setId(1);
        complexKeyValue = new IgnoreCombineComplexKeyValue();
        complexKeyValue.setKey("Key");
        complexKeyValue.setValue(2);
        value.setKeyValue(complexKeyValue);
        value.setVersionKey(1);

        context = ContextUtils.createContext(dataSource);

        ComplexIgnoreCombineKeyValue queryIgnoreComplexKeyValue;
        if (dataSource == EDataSource.MySql || dataSource == EDataSource.Sqlite || dataSource == EDataSource.SqlServer) {

            //附加
            context.createSet(ComplexIgnoreCombineKeyValue.class).attach(value);
            context.saveChanges();

            context = ContextUtils.createContext(dataSource);
            queryIgnoreComplexKeyValue = context.createSet(ComplexIgnoreCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);

            assertNotNull(queryIgnoreComplexKeyValue);
            assertEquals(5, queryIgnoreComplexKeyValue.getKeyValue().getValue());
        } else {

            try {
                //附加
                context.createSet(ComplexIgnoreCombineKeyValue.class).attach(value);
                context.saveChanges();
            } catch (Exception ex) {
                assertTrue(ex instanceof UnSupportedException);
            }

            context = ContextUtils.createContext(dataSource);
            queryIgnoreComplexKeyValue = context.createSet(ComplexIgnoreCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
            assertNotNull(queryIgnoreComplexKeyValue);
        }

        //修改
        queryIgnoreComplexKeyValue.setKeyValue(new IgnoreCombineComplexKeyValue());
        queryIgnoreComplexKeyValue.getKeyValue().setValue(999);
        queryIgnoreComplexKeyValue.getKeyValue().setKey("Key");
        //模拟一个版本键被修改
        HashMap<String, Object> map = new HashMap<>();
        map.put("versionKey", 2);
        context.createSet(ComplexIgnoreCombineKeyValue.class).setAttributes(map, p -> p.getId() == 1, ComplexIgnoreCombineKeyValue.class);
        //会被累加
        context.saveChanges();

        context = ContextUtils.createContext(dataSource);
        //重新查出来
        queryIgnoreComplexKeyValue = context.createSet(ComplexIgnoreCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);

        assertNotNull(queryIgnoreComplexKeyValue);
        assertEquals(5, queryIgnoreComplexKeyValue.getKeyValue().getValue());

        context.createSet(ComplexIgnoreCombineKeyValue.class).delete(p -> p.getId() > 0, ComplexIgnoreCombineKeyValue.class);
    }

    /**
     * 测试 复杂属性 覆写策略
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void complexAttributeOverWriteCombineConcurrentConflict(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        var value = new ComplexOverwriteCombineKeyValue();
        value.setId(1);
        var complexKeyValue = new OverWriteCombineComplexKeyValue();
        complexKeyValue.setKey("Key");
        complexKeyValue.setValue(5);
        value.setKeyValue(complexKeyValue);
        value.setVersionKey(1);

        //附加
        context.createSet(ComplexOverwriteCombineKeyValue.class).attach(value);
        context.saveChanges();

        //重复插入 异常忽略
        value = new ComplexOverwriteCombineKeyValue();
        value.setId(1);
        complexKeyValue = new OverWriteCombineComplexKeyValue();
        complexKeyValue.setKey("Key");
        complexKeyValue.setValue(2);
        value.setKeyValue(complexKeyValue);
        value.setVersionKey(1);

        context = ContextUtils.createContext(dataSource);

        ComplexOverwriteCombineKeyValue queryIgnoreComplexKeyValue;
        if (dataSource == EDataSource.MySql || dataSource == EDataSource.Sqlite || dataSource == EDataSource.SqlServer) {

            //附加
            context.createSet(ComplexOverwriteCombineKeyValue.class).attach(value);
            context.saveChanges();

            context = ContextUtils.createContext(dataSource);
            queryIgnoreComplexKeyValue = context.createSet(ComplexOverwriteCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);

            assertNotNull(queryIgnoreComplexKeyValue);
            assertEquals(2, queryIgnoreComplexKeyValue.getKeyValue().getValue());
        } else {

            try {
                //附加
                context.createSet(ComplexOverwriteCombineKeyValue.class).attach(value);
                context.saveChanges();
            } catch (Exception ex) {
                assertTrue(ex instanceof UnSupportedException);
            }

            context = ContextUtils.createContext(dataSource);
            queryIgnoreComplexKeyValue = context.createSet(ComplexOverwriteCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);
            assertNotNull(queryIgnoreComplexKeyValue);
        }

        //修改
        queryIgnoreComplexKeyValue.setKeyValue(new OverWriteCombineComplexKeyValue());
        queryIgnoreComplexKeyValue.setVersionKey(999);
        //模拟一个版本键被修改
        HashMap<String, Object> map = new HashMap<>();
        map.put("versionKey", 2);
        context.createSet(ComplexOverwriteCombineKeyValue.class).setAttributes(map, p -> p.getId() == 1, ComplexOverwriteCombineKeyValue.class);
        //会被累加
        context.saveChanges();

        context = ContextUtils.createContext(dataSource);
        //重新查出来
        queryIgnoreComplexKeyValue = context.createSet(ComplexOverwriteCombineKeyValue.class).findFirst(p -> p.getId() == 1).orElse(null);

        assertNotNull(queryIgnoreComplexKeyValue);
        assertEquals(999, queryIgnoreComplexKeyValue.getVersionKey());

        context.createSet(ComplexOverwriteCombineKeyValue.class).delete(p -> p.getId() > 0, ComplexOverwriteCombineKeyValue.class);
    }
}
