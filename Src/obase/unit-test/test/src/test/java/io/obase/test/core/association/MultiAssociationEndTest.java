package io.obase.test.core.association;

import io.obase.common.TwoTuple;
import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.association.multiAssociationEnd.Product;
import io.obase.test.domain.association.multiAssociationEnd.Property;
import io.obase.test.domain.association.multiAssociationEnd.PropertyTakingValue;
import io.obase.test.domain.association.multiAssociationEnd.PropertyValue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 多方(多个关联端)关联测试
 */
@ExtendWith(ConfigSetUp.class)
public class MultiAssociationEndTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(Product.class).delete(p -> p.getCode() != "", Product.class);
            context.createSet(Property.class).delete(p -> p.getCode() != "", Property.class);
            context.createSet(PropertyValue.class).delete(p -> p.getCode() != "", PropertyValue.class);
            context.createSet(PropertyTakingValue.class).delete(p ->
                    p.getProductCode() != "" || p.getPropertyCode() != "" || p.getPropertyValueCode() != "", PropertyTakingValue.class);
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
            context.createSet(Product.class).delete(p -> p.getCode() != "", Product.class);
            context.createSet(Property.class).delete(p -> p.getCode() != "", Property.class);
            context.createSet(PropertyValue.class).delete(p -> p.getCode() != "", PropertyValue.class);
            context.createSet(PropertyTakingValue.class).delete(p ->
                    p.getProductCode() != "" || p.getPropertyCode() != "" || p.getPropertyValueCode() != "", PropertyTakingValue.class);
        }
    }

    /**
     * 显式多方关联测试
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void explicitTest(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);

        //初始化产品
        var product1 = new Product();
        product1.setCode("A");
        product1.setName("上衣");

        var product2 = new Product();
        product2.setCode("B");
        product2.setName("裤子");

        //初始化属性
        var property = new Property();
        property.setCode("Color");
        property.setName("颜色");

        //初始属性值
        var propertyValue1 = new PropertyValue();
        propertyValue1.setCode("ColorA");
        propertyValue1.setValue("白色");

        var propertyValue2 = new PropertyValue();
        propertyValue2.setCode("ColorB");
        propertyValue2.setValue("黑色");

        //建立关系
        //产品1 有两种颜色 白色和黑色
        var propertyTakingValue1 = new PropertyTakingValue();
        propertyTakingValue1.setProduct(product1);
        propertyTakingValue1.setPropertyCode(product1.getCode());
        propertyTakingValue1.setProperty(property);
        propertyTakingValue1.setPropertyCode(property.getCode());
        propertyTakingValue1.setPropertyValue(propertyValue1);
        propertyTakingValue1.setPropertyValueCode(propertyValue1.getCode());
        propertyTakingValue1.setPropertyPhotoUrl("/产品1/白色.jpg");

        var propertyTakingValue2 = new PropertyTakingValue();
        propertyTakingValue2.setProduct(product1);
        propertyTakingValue2.setPropertyCode(product1.getCode());
        propertyTakingValue2.setProperty(property);
        propertyTakingValue2.setPropertyCode(property.getCode());
        propertyTakingValue2.setPropertyValue(propertyValue2);
        propertyTakingValue2.setPropertyValueCode(propertyValue2.getCode());
        propertyTakingValue2.setPropertyPhotoUrl("/产品1/黑色.jpg");

        //产品2有一种颜色 白色
        var propertyTakingValue3 = new PropertyTakingValue();
        propertyTakingValue3.setProduct(product2);
        propertyTakingValue3.setPropertyCode(product2.getCode());
        propertyTakingValue3.setProperty(property);
        propertyTakingValue3.setPropertyCode(property.getCode());
        propertyTakingValue3.setPropertyValue(propertyValue1);
        propertyTakingValue3.setPropertyValueCode(propertyValue1.getCode());
        propertyTakingValue3.setPropertyPhotoUrl("/产品2/白色.jpg");

        //附加至上下文
        context.attach(product1);
        context.attach(product2);
        context.attach(property);
        context.attach(propertyValue1);
        context.attach(propertyValue2);
        context.attach(propertyTakingValue1);
        context.attach(propertyTakingValue2);
        context.attach(propertyTakingValue3);
        //保存
        context.saveChanges();

        context = ContextUtils.createContext(dataSource);

        //查询出来 验证属性 此处使用了Include加载PropertyTakingValue->Property和PropertyTakingValue->PropertyValue
        var productList = context.createSet(Product.class)
                .include("PropertyTakingValues.Property")
                .include("PropertyTakingValues.PropertyValue").toList();

        //根据颜色代码排序
        productList.forEach(product ->
        {
            product.setPropertyTakingValues(product.getPropertyTakingValues().stream().sorted(Comparator.comparing(PropertyTakingValue::getPropertyValueCode)).collect(Collectors.toList()));
        });

        //有两个商品
        assertEquals(2, productList.size());
        //分别是裤子和上衣
        assertEquals("上衣", productList.get(0).getName());
        assertEquals("裤子", productList.get(1).getName());
        //上衣的属性是颜色
        assertEquals("颜色", productList.get(0).getPropertyTakingValues().get(0).getProperty().getName());
        assertEquals("颜色", productList.get(0).getPropertyTakingValues().get(1).getProperty().getName());
        //上衣有白色和黑色
        assertEquals("白色", productList.get(0).getPropertyTakingValues().get(0).getPropertyValue().getValue());
        assertEquals("黑色", productList.get(0).getPropertyTakingValues().get(1).getPropertyValue().getValue());
        //图片分别是/产品1/白色.jpg /产品1/黑色.jpg
        assertEquals("/产品1/白色.jpg", productList.get(0).getPropertyTakingValues().get(0).getPropertyPhotoUrl());
        assertEquals("/产品1/黑色.jpg", productList.get(0).getPropertyTakingValues().get(1).getPropertyPhotoUrl());

        //裤子的属性是颜色
        assertEquals("颜色", productList.get(1).getPropertyTakingValues().get(0).getProperty().getName());
        //裤子有白色
        assertEquals("白色", productList.get(1).getPropertyTakingValues().get(0).getPropertyValue().getValue());
        //图片是/产品2/白色.jpg
        assertEquals("/产品2/白色.jpg", productList.get(1).getPropertyTakingValues().get(0).getPropertyPhotoUrl());

        //删除
        context.createSet(Product.class).delete(p -> p.getCode() != "", Product.class);
        context.createSet(Property.class).delete(p -> p.getCode() != "", Property.class);
        context.createSet(PropertyValue.class).delete(p -> p.getCode() != "", PropertyValue.class);
        context.createSet(PropertyTakingValue.class).delete(p ->
                p.getProductCode() != "" || p.getPropertyCode() != "" || p.getPropertyValueCode() != "", PropertyTakingValue.class);
    }

    /**
     * 隐式多方关联测试
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void implicitTestTest(EDataSource dataSource) {

        var context = ContextUtils.createContext(dataSource);

        //初始化产品
        var product1 = new Product();
        product1.setCode("A");
        product1.setName("上衣");

        var product2 = new Product();
        product2.setCode("B");
        product2.setName("裤子");

        //初始化属性
        var property = new Property();
        property.setCode("Color");
        property.setName("颜色");

        //初始属性值
        var propertyValue1 = new PropertyValue();
        propertyValue1.setCode("ColorA");
        propertyValue1.setValue("白色");

        PropertyValue propertyValue2 = new PropertyValue();
        propertyValue2.setCode("ColorB");
        propertyValue2.setValue("黑色");

        //建立关系
        //产品1 有两种颜色 白色和黑色
        var temp1 = new ArrayList<TwoTuple<Property, PropertyValue>>();
        temp1.add(new TwoTuple<>(property, propertyValue1));
        temp1.add(new TwoTuple<>(property, propertyValue2));
        product1.setPropertyValues(temp1);

        //产品2有一种颜色 白色
        var temp2 = new ArrayList<TwoTuple<Property, PropertyValue>>();
        temp2.add(new TwoTuple<>(property, propertyValue1));
        product2.setPropertyValues(temp2);

        //附加至上下文
        context.attach(product1);
        context.attach(product2);
        context.attach(property);
        context.attach(propertyValue1);
        context.attach(propertyValue2);
        //保存
        context.saveChanges();

        context = ContextUtils.createContext(dataSource);

        //查询出来 验证属性 此处使用了Include加载PropertyTakingValue->Property和PropertyTakingValue->PropertyValue
        var productList = context.createSet(Product.class)
                .include("PropertyValues.Item1")
                .include("PropertyValues.Item2").toList();

        //根据颜色代码排序
        productList.forEach(product ->
        {
            product.setPropertyValues(product.getPropertyValues().stream().sorted(Comparator.comparing(p -> p.getItem2().getCode())).collect(Collectors.toList()));
        });

        //有两个商品
        assertEquals(2, productList.size());
        //分别是裤子和上衣
        assertEquals("上衣", productList.get(0).getName());
        assertEquals("裤子", productList.get(1).getName());
        //上衣的属性是颜色
        assertEquals("颜色", productList.get(0).getPropertyValues().get(0).getItem1().getName());
        assertEquals("颜色", productList.get(0).getPropertyValues().get(1).getItem1().getName());
        //上衣有白色和黑色
        assertEquals("白色", productList.get(0).getPropertyValues().get(0).getItem2().getValue());
        assertEquals("黑色", productList.get(0).getPropertyValues().get(1).getItem2().getValue());

        //裤子的属性是颜色
        assertEquals("颜色", productList.get(1).getPropertyValues().get(0).getItem1().getName());
        //裤子有白色
        assertEquals("白色", productList.get(1).getPropertyValues().get(0).getItem2().getValue());

        //测试延迟加载
        context = ContextUtils.createContext(dataSource);

        //查询出来 验证延迟加载
        productList = context.createSet(Product.class).toList();

        //根据颜色代码排序
        productList.forEach(product ->
        {
            product.setPropertyValues(product.getPropertyValues().stream().sorted(Comparator.comparing(p -> p.getItem2().getCode())).collect(Collectors.toList()));
        });

        //有两个商品
        assertEquals(2, productList.size());
        //分别是裤子和上衣
        assertEquals("上衣", productList.get(0).getName());
        assertEquals("裤子", productList.get(1).getName());
        //上衣的属性是颜色
        assertEquals("颜色", productList.get(0).getPropertyValues().get(0).getItem1().getName());
        assertEquals("颜色", productList.get(0).getPropertyValues().get(1).getItem1().getName());
        //上衣有白色和黑色
        assertEquals("白色", productList.get(0).getPropertyValues().get(0).getItem2().getValue());
        assertEquals("黑色", productList.get(0).getPropertyValues().get(1).getItem2().getValue());

        //裤子的属性是颜色
        assertEquals("颜色", productList.get(1).getPropertyValues().get(0).getItem1().getName());
        //裤子有白色
        assertEquals("白色", productList.get(1).getPropertyValues().get(0).getItem2().getValue());

        //删除
        context.createSet(Product.class).delete(p -> p.getCode() != "", Product.class);
        context.createSet(Property.class).delete(p -> p.getCode() != "", Property.class);
        context.createSet(PropertyValue.class).delete(p -> p.getCode() != "", PropertyValue.class);
        context.createSet(PropertyTakingValue.class).delete(p ->
                p.getProductCode() != "" || p.getPropertyCode() != "" || p.getPropertyValueCode() != "", PropertyTakingValue.class);
    }
}
