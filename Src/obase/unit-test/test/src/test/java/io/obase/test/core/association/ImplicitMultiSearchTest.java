package io.obase.test.core.association;

import io.obase.providers.sql.EDataSource;
import io.obase.test.ConfigSetUp;
import io.obase.test.ContextUtils;
import io.obase.test.configuration.TestCaseSourceConfigurationManager;
import io.obase.test.domain.association.multiImplicitSearch.Category;
import io.obase.test.domain.association.multiImplicitSearch.Product;
import io.obase.test.domain.association.multiImplicitSearch.ProductCategory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 隐式多对多关联搜索优化测试
 */
@ExtendWith(ConfigSetUp.class)
public class ImplicitMultiSearchTest {

    /**
     * 初始化方法
     */
    @BeforeAll
    public static void beforeAll() {

        for (var dataSource : TestCaseSourceConfigurationManager.getDataSources()) {
            var context = ContextUtils.createContext(dataSource);

            //清理可能的冗余数据
            context.createSet(Product.class).delete(p -> true, Product.class);
            context.createSet(Category.class).delete(p -> true, Category.class);
            context.createSet(ProductCategory.class).delete(p -> true, ProductCategory.class);

            //构造产品分类
            var category1 = new Category();
            category1.setName("产品分类A");

            var category2 = new Category();
            category2.setName("产品分类B");

            var category3 = new Category();
            category3.setName("产品分类C");

            //构造产品
            var product1 = new Product();
            product1.setCode("CodeX");
            var categories1 = new ArrayList<Category>();
            categories1.add(category1);
            categories1.add(category2);
            product1.setCategories(categories1);
            product1.setName("产品AB");

            var product2 = new Product();
            product2.setCode("CodeY");
            var categories2 = new ArrayList<Category>();
            categories2.add(category2);
            categories2.add(category3);
            product2.setCategories(categories2);
            product2.setName("产品BC");

            //附加
            context.attach(category1);
            context.attach(category2);
            context.attach(category3);
            context.attach(product1);
            context.attach(product2);
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
            context.createSet(Product.class).delete(p -> true, Product.class);
            context.createSet(Category.class).delete(p -> true, Category.class);
            context.createSet(ProductCategory.class).delete(p -> true, ProductCategory.class);
        }
    }

    /**
     * 测试隐式多对多关联搜索优化
     *
     * @param dataSource 数据源
     */
    @ParameterizedTest
    @ArgumentsSource(TestCaseSourceConfigurationManager.class)
    public void test(EDataSource dataSource) {
        var context = ContextUtils.createContext(dataSource);
        String code = "CodeX";
        //根据产品ID查询所属的分类 对于显式化的隐式多对多 可以将关联型作为查询基点 再Include至Category
        var productCategories = context.createSet(ProductCategory.class).filter(p -> p.getProductCode() == code)
                .include(p -> p.getCategory()).toList();

        //有两个
        assertEquals(2, productCategories.size());
        //分别是产品分类A 和 产品分类B
        assertEquals(1, productCategories.stream().filter(p -> Objects.equals(p.getCategory().getName(), "产品分类A")).count());
        assertEquals(1, productCategories.stream().filter(p -> Objects.equals(p.getCategory().getName(), "产品分类B")).count());

        //根据分类名称查询下属的产品 此处需要借助冗余属性CategoryName
        productCategories = context.createSet(ProductCategory.class).filter(p -> p.getCategoryName() == "产品分类B")
                .include(p -> p.getProduct()).toList();

        //有两个
        assertEquals(2, productCategories.size());
        //分别是产品分类A 和 产品分类B
        assertEquals(1, productCategories.stream().filter(p -> Objects.equals(p.getProduct().getName(), "产品AB")).count());
        assertEquals(1, productCategories.stream().filter(p -> Objects.equals(p.getProduct().getName(), "产品BC")).count());

        //测试表达式Include
        context = ContextUtils.createContext(dataSource);

        var products = context.createSet(Product.class).include(p -> p.getCategories()).toList();

        //有两个
        assertEquals(2, products.size());
        //分别是产品分类A 和 产品分类B
        assertEquals(1, products.stream().filter(p -> Objects.equals(p.getName(), "产品AB")).findFirst().get().getCategories().stream().filter(p -> Objects.equals(p.getName(), "产品分类A")).count());
        assertEquals(1, products.stream().filter(p -> Objects.equals(p.getName(), "产品AB")).findFirst().get().getCategories().stream().filter(p -> Objects.equals(p.getName(), "产品分类B")).count());
        //分别是产品分类C 和 产品分类B
        assertEquals(1, products.stream().filter(p -> Objects.equals(p.getName(), "产品BC")).findFirst().get().getCategories().stream().filter(p -> Objects.equals(p.getName(), "产品分类C")).count());
        assertEquals(1, products.stream().filter(p -> Objects.equals(p.getName(), "产品BC")).findFirst().get().getCategories().stream().filter(p -> Objects.equals(p.getName(), "产品分类B")).count());

        //测试字符串的
        products = context.createSet(Product.class).include("Categories").toList();

        //有两个
        assertEquals(2, products.size());
        //分别是产品分类A 和 产品分类B
        assertEquals(1, products.stream().filter(p -> Objects.equals(p.getName(), "产品AB")).findFirst().get().getCategories().stream().filter(p -> Objects.equals(p.getName(), "产品分类A")).count());
        assertEquals(1, products.stream().filter(p -> Objects.equals(p.getName(), "产品AB")).findFirst().get().getCategories().stream().filter(p -> Objects.equals(p.getName(), "产品分类B")).count());
        //分别是产品分类C 和 产品分类B
        assertEquals(1, products.stream().filter(p -> Objects.equals(p.getName(), "产品BC")).findFirst().get().getCategories().stream().filter(p -> Objects.equals(p.getName(), "产品分类C")).count());
        assertEquals(1, products.stream().filter(p -> Objects.equals(p.getName(), "产品BC")).findFirst().get().getCategories().stream().filter(p -> Objects.equals(p.getName(), "产品分类B")).count());

        context = ContextUtils.createContext(dataSource);
        //测试延迟加载
        products = context.createSet(Product.class).toList();

        //根据颜色代码排序 触发延迟加载
        products.forEach(product ->
        {
            List<Category> values = product.getCategories();
            product.setCategories(values.stream().sorted(Comparator.comparingInt(Category::getCategoryId)).collect(Collectors.toList()));
        });

        //有两个
        assertEquals(2, products.size());
        //分别是产品分类A 和 产品分类B
        assertEquals(1, products.stream().filter(p -> Objects.equals(p.getName(), "产品AB")).findFirst().get().getCategories().stream().filter(p -> Objects.equals(p.getName(), "产品分类A")).count());
        assertEquals(1, products.stream().filter(p -> Objects.equals(p.getName(), "产品AB")).findFirst().get().getCategories().stream().filter(p -> Objects.equals(p.getName(), "产品分类B")).count());
        //分别是产品分类C 和 产品分类B
        assertEquals(1, products.stream().filter(p -> Objects.equals(p.getName(), "产品BC")).findFirst().get().getCategories().stream().filter(p -> Objects.equals(p.getName(), "产品分类C")).count());
        assertEquals(1, products.stream().filter(p -> Objects.equals(p.getName(), "产品BC")).findFirst().get().getCategories().stream().filter(p -> Objects.equals(p.getName(), "产品分类B")).count());

    }
}
