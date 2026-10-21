package io.obase.test.core.functional;

import io.obase.common.TwoTuple;
import io.obase.core.odm.AssociationReference;
import io.obase.core.odm.IInstanceConstructor;
import io.obase.core.odm.InstanceConstructor;
import io.obase.core.odm.ObjectDataModel;
import io.obase.core.odm.ObjectType;
import io.obase.core.odm.builder.ModelBuilder;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 测试构造函数参数绑定元素的合法性校验。
 * 构造参数只能绑定到本类型的简单属性（含复杂类型属性），绑定到关联引用、关联端等引用元素时应抛出可读的异常，
 * 而不是在建模阶段深处抛出不点名任何信息的空引用异常。
 */
public class ConstructorParameterBindingTest {

    /**
     * 国家（隐式关联的被引用端）
     */
    public static class Country {

        /**
         * 国家标识
         */
        private long countryId;

        /**
         * 国家名称
         */
        private String name;

        public long getCountryId() {
            return this.countryId;
        }

        public void setCountryId(long countryId) {
            this.countryId = countryId;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * 人员 含有隐式关联的关联引用 构造参数为两个标量
     */
    public static class Person {

        /**
         * 人员标识
         */
        private final long personId;

        /**
         * 人员名称
         */
        private final String name;

        /**
         * 关联引用（隐式关联，端是实体型Country）
         */
        private Country homeCountry;

        public Person(long personId, String name) {
            this.personId = personId;
            this.name = name;
        }

        public long getPersonId() {
            return this.personId;
        }

        public String getName() {
            return this.name;
        }

        public Country getHomeCountry() {
            return this.homeCountry;
        }

        public void setHomeCountry(Country homeCountry) {
            this.homeCountry = homeCountry;
        }
    }

    /**
     * 构造参数绑定到普通（只读）标量属性
     */
    public static class PersonWithScalarCtor {

        /**
         * 人员标识
         */
        private final long personId;

        /**
         * 人员名称
         */
        private final String name;

        /**
         * 关联引用
         */
        private Country homeCountry;

        public PersonWithScalarCtor(long personId, String name) {
            this.personId = personId;
            this.name = name;
        }

        public long getPersonId() {
            return this.personId;
        }

        public String getName() {
            return this.name;
        }

        public Country getHomeCountry() {
            return this.homeCountry;
        }

        public void setHomeCountry(Country homeCountry) {
            this.homeCountry = homeCountry;
        }
    }

    /**
     * 复杂类型
     */
    public static class Address {

        /**
         * 城市
         */
        private String city;

        public String getCity() {
            return this.city;
        }

        public void setCity(String city) {
            this.city = city;
        }
    }

    /**
     * 构造参数绑定到复杂类型属性
     */
    public static class PersonWithComplexCtor {

        /**
         * 人员标识
         */
        private final long personId;

        /**
         * 地址（复杂类型）
         */
        private final Address address;

        public PersonWithComplexCtor(long personId, Address address) {
            this.personId = personId;
            this.address = address;
        }

        public long getPersonId() {
            return this.personId;
        }

        public Address getAddress() {
            return this.address;
        }
    }

    /**
     * 含显式关联引用的实体
     */
    public static class PersonWithExplicitReference {

        /**
         * 人员标识
         */
        private final long personId;

        /**
         * 人员名称
         */
        private String name;

        /**
         * 基于显式关联的关联引用
         */
        private Country homeCountry;

        public PersonWithExplicitReference(long personId, String name) {
            this.personId = personId;
            this.name = name;
        }

        public long getPersonId() {
            return this.personId;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Country getHomeCountry() {
            return this.homeCountry;
        }

        public void setHomeCountry(Country homeCountry) {
            this.homeCountry = homeCountry;
        }
    }

    /**
     * 显式关联型
     */
    public static class PersonCountry {

        /**
         * 人员（关联端）
         */
        private PersonWithExplicitReference person;

        /**
         * 国家（关联端）
         */
        private Country country;

        public PersonCountry() {
        }

        public PersonCountry(PersonWithExplicitReference person, Country country) {
            this.person = person;
            this.country = country;
        }

        public PersonWithExplicitReference getPerson() {
            return this.person;
        }

        public void setPerson(PersonWithExplicitReference person) {
            this.person = person;
        }

        public Country getCountry() {
            return this.country;
        }

        public void setCountry(Country country) {
            this.country = country;
        }
    }

    /**
     * 城市（多方关联的参与方）
     */
    public static class City {

        /**
         * 城市标识
         */
        private long cityId;

        /**
         * 城市名称
         */
        private String name;

        public long getCityId() {
            return this.cityId;
        }

        public void setCityId(long cityId) {
            this.cityId = cityId;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * 参与多方（三端）隐式关联的人员 其关联引用指向多方关联本身而不是某个关联端
     */
    public static class PersonWithMultiAssociation {

        /**
         * 人员标识
         */
        private final long personId;

        /**
         * 人员名称
         */
        private final String name;

        /**
         * 多方关联的关联引用 关联型的端数为3 不提供右端
         */
        private List<TwoTuple<City, Country>> cities;

        public PersonWithMultiAssociation(long personId, String name) {
            this.personId = personId;
            this.name = name;
        }

        public long getPersonId() {
            return this.personId;
        }

        public String getName() {
            return this.name;
        }

        public List<TwoTuple<City, Country>> getCities() {
            return this.cities;
        }

        public void setCities(List<TwoTuple<City, Country>> cities) {
            this.cities = cities;
        }
    }

    /**
     * 构造参数名与导航属性名相关的实体（用于MapDefault）
     */
    public static class PersonWithDefaultMap {

        /**
         * 人员标识
         */
        private final long personId;

        /**
         * 关联引用
         */
        private final Country homeCountry;

        public PersonWithDefaultMap(long personId, Country homeCountry) {
            this.personId = personId;
            this.homeCountry = homeCountry;
        }

        public long getPersonId() {
            return this.personId;
        }

        public Country getHomeCountry() {
            return this.homeCountry;
        }
    }

    /**
     * 绑定元素名称写错的实体
     */
    public static class PersonWithWrongElementName {

        /**
         * 人员标识
         */
        private final long personId;

        /**
         * 人员名称
         */
        private final String name;

        public PersonWithWrongElementName(long personId, String name) {
            this.personId = personId;
            this.name = name;
        }

        public long getPersonId() {
            return this.personId;
        }

        public String getName() {
            return this.name;
        }
    }

    /**
     * 创建一个建模器（无上下文 仅用于建模）
     *
     * @return 建模器
     */
    private static ModelBuilder createModelBuilder() {
        return new ModelBuilder(null);
    }

    /**
     * 配置国家实体型
     *
     * @param modelBuilder 对象数据模型建造器
     */
    private static void configCountry(ModelBuilder modelBuilder) {
        var country = modelBuilder.entity(Country.class);
        country.hasKeyAttribute(Country::getCountryId).hasKeyIsSelfIncreased(true);
        country.toTable("Country");
    }

    /**
     * 获取类型的构造函数 免去在每个用例中处理受检异常
     *
     * @param type           类型
     * @param parameterTypes 参数类型
     * @param <T>            类型
     * @return 构造函数
     */
    private static <T> Constructor<T> getCtor(Class<T> type, Class<?>... parameterTypes) {
        try {
            return type.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("测试类型" + type.getSimpleName() + "上没有预期的构造函数", e);
        }
    }

    /**
     * 直接调用建模阶段重建代理类型构造器的私有方法ModelBuilder.createConstructor
     * proxyType传入原类型即可 本方法只用到其getDeclaredConstructor
     *
     * @param targetType   对象类型对应的Java类型
     * @param objectType   对象类型模型
     * @param modelBuilder 建模器
     * @return 重建的构造器
     */
    private static IInstanceConstructor createProxyConstructor(Class<?> targetType, ObjectType objectType,
                                                               ModelBuilder modelBuilder) {
        Method method;
        try {
            method = ModelBuilder.class.getDeclaredMethod("createConstructor", Class.class, IInstanceConstructor.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("建模器上找不到重建构造器的方法", e);
        }
        method.setAccessible(true);
        try {
            return (IInstanceConstructor) method.invoke(modelBuilder, targetType, objectType.getConstructor());
        } catch (InvocationTargetException e) {
            //把反射调用包装的异常还原出来 便于断言真实异常类型
            if (e.getCause() instanceof RuntimeException) throw (RuntimeException) e.getCause();
            if (e.getCause() instanceof Error) throw (Error) e.getCause();
            throw new IllegalStateException("重建构造器时发生受检异常", e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法调用建模器上重建构造器的方法", e);
        }
    }

    /**
     * 用例1/2：构造参数绑定到普通（只读）标量属性 建模应通过
     */
    @Test
    public void mapScalarTest() {
        var modelBuilder = createModelBuilder();
        configCountry(modelBuilder);

        var p = modelBuilder.entity(PersonWithScalarCtor.class);
        p.hasKeyAttribute(PersonWithScalarCtor::getPersonId).hasKeyIsSelfIncreased(false);
        p.toTable("PersonWithScalarCtor");
        p.hasConstructor(getCtor(PersonWithScalarCtor.class, long.class, String.class))
                .map(PersonWithScalarCtor::getPersonId)
                .map(PersonWithScalarCtor::getName)
                .end();

        assertDoesNotThrow(() -> {
            modelBuilder.build();
        });
    }

    /**
     * 用例3：构造参数绑定到复杂类型属性 建模应通过
     */
    @Test
    public void mapComplexTypeTest() {
        var modelBuilder = createModelBuilder();
        configCountry(modelBuilder);
        modelBuilder.complex(Address.class);

        var p = modelBuilder.entity(PersonWithComplexCtor.class);
        p.hasKeyAttribute(PersonWithComplexCtor::getPersonId).hasKeyIsSelfIncreased(false);
        p.toTable("PersonWithComplexCtor");
        p.hasConstructor(getCtor(PersonWithComplexCtor.class, long.class, Address.class))
                .map(PersonWithComplexCtor::getPersonId)
                .map(PersonWithComplexCtor::getAddress)
                .end();

        assertDoesNotThrow(() -> {
            modelBuilder.build();
        });
    }

    /**
     * 用例5：构造参数绑定到隐式关联的关联引用 Map阶段即拦截
     */
    @Test
    public void mapImplicitAssociationReferenceTest() {
        var modelBuilder = createModelBuilder();
        configCountry(modelBuilder);

        var p = modelBuilder.entity(Person.class);
        p.hasKeyAttribute(Person::getPersonId).hasKeyIsSelfIncreased(false);
        p.toTable("Person");

        var constructor = getCtor(Person.class, long.class, String.class);
        var exception = assertThrows(IllegalArgumentException.class, () ->
                p.hasConstructor(constructor)
                        .map(Person::getPersonId)
                        .map(Person::getHomeCountry)
                        .end());

        //异常信息应点名类型名、属性名和正确做法
        assertTrue(exception.getMessage().contains("Person"));
        assertTrue(exception.getMessage().contains("HomeCountry"));
        assertTrue(exception.getMessage().contains("AssociationEnd + HasMapping + AssociationReference"));
    }

    /**
     * 用例5（兜底）：关联引用的右端无法确定时（显式关联或端数大于2的多方关联），
     * 建模阶段重建代理类型构造器（ModelBuilder.createConstructor → Parameter.getType → AssociationReference.getValueType）
     * 应给出点名问题的异常，而不是裸的空引用异常
     */
    @Test
    public void bindImplicitAssociationReferenceTest() {
        var modelBuilder = createModelBuilder();
        configCountry(modelBuilder);
        modelBuilder.entity(City.class).hasKeyAttribute(City::getCityId).hasKeyIsSelfIncreased(false);

        var p = modelBuilder.entity(PersonWithMultiAssociation.class);
        p.hasKeyAttribute(PersonWithMultiAssociation::getPersonId).hasKeyIsSelfIncreased(false);
        p.toTable("PersonWithMultiAssociation");
        p.hasConstructor(getCtor(PersonWithMultiAssociation.class, long.class, String.class))
                .map(PersonWithMultiAssociation::getPersonId)
                .map(PersonWithMultiAssociation::getName)
                .end();

        //多方(三端)关联 关联引用不指向具体的关联端
        var association = modelBuilder.association();
        association.associationEnd(PersonWithMultiAssociation.class).associationReference(x -> x.getCities());
        association.associationEnd(City.class);
        association.associationEnd(Country.class);
        association.toTable("PersonCityCountry");

        ObjectDataModel model = modelBuilder.build();
        var objectType = model.getObjectType(PersonWithMultiAssociation.class);
        var element = objectType.getElement("Cities");
        assertNotNull(element);
        var associationReference = (AssociationReference) element;
        //把关联型改为隐式关联 使AssociationReference.getValueType走“取右端”的分支
        //而三端关联的gotRightEnd()恒返回null 这正是原缺陷抛出裸空引用异常的路径
        associationReference.getAssociationType().setVisible(false);
        assertNull(associationReference.gotRightEnd());
        assertEquals(3, associationReference.getAssociationType().getAssociationEnds().size());

        //绕过Map阶段的校验 把name参数绑定到这个关联引用上
        ((InstanceConstructor) objectType.getConstructor()).getParameters().get(1).setElementName("Cities");

        var exception = assertThrows(IllegalArgumentException.class, () ->
                createProxyConstructor(PersonWithMultiAssociation.class, objectType, modelBuilder));
        //异常不能是裸空引用异常 且要点名关联引用、端数和正确做法
        assertTrue(exception.getMessage().contains("Cities"));
        assertTrue(exception.getMessage().contains("端数为3"));
        assertTrue(exception.getMessage().contains("AssociationEnd + HasMapping + AssociationReference"));
    }

    /**
     * 用例6：构造参数绑定到显式关联型上的关联引用 Map阶段即拦截
     */
    @Test
    public void mapExplicitAssociationReferenceTest() {
        var modelBuilder = createModelBuilder();
        configCountry(modelBuilder);

        var p = modelBuilder.entity(PersonWithExplicitReference.class);
        p.hasKeyAttribute(PersonWithExplicitReference::getPersonId).hasKeyIsSelfIncreased(false);
        p.toTable("PersonWithExplicitReference");

        var association = modelBuilder.association(PersonCountry.class);
        association.associationEnd(PersonCountry::getPerson).hasMapping("personId", "personId");
        association.associationEnd(PersonCountry::getCountry).hasMapping("countryId", "countryId");
        association.toTable("PersonCountry");

        var constructor = getCtor(PersonWithExplicitReference.class, long.class, String.class);
        var exception = assertThrows(IllegalArgumentException.class, () ->
                p.hasConstructor(constructor)
                        .map(PersonWithExplicitReference::getPersonId)
                        .map(PersonWithExplicitReference::getHomeCountry)
                        .end());

        assertTrue(exception.getMessage().contains("PersonWithExplicitReference"));
        assertTrue(exception.getMessage().contains("HomeCountry"));
        assertTrue(exception.getMessage().contains("AssociationEnd + HasMapping + AssociationReference"));
    }

    /**
     * 用例7：构造参数绑定到关联端 Map阶段即拦截
     */
    @Test
    public void mapAssociationEndTest() {
        var modelBuilder = createModelBuilder();
        configCountry(modelBuilder);

        var association = modelBuilder.association(PersonCountry.class);
        association.associationEnd(PersonCountry::getPerson).hasMapping("personId", "personId");
        association.associationEnd(PersonCountry::getCountry).hasMapping("countryId", "countryId");
        association.toTable("PersonCountry");

        var constructor = getCtor(PersonCountry.class, PersonWithExplicitReference.class, Country.class);
        var exception = assertThrows(IllegalArgumentException.class, () ->
                association.hasConstructor(constructor)
                        .map(PersonCountry::getPerson)
                        .map(PersonCountry::getCountry)
                        .end());

        assertTrue(exception.getMessage().contains("PersonCountry"));
        assertTrue(exception.getMessage().contains("Person"));
        assertTrue(exception.getMessage().contains("引用元素"));
    }

    /**
     * 用例8：参数名与导航属性名仅大小写不同时 Map阶段即拦截。
     * 说明
     * MapDefault按参数名绑定元素，Java未开启-parameters编译时构造函数参数名为arg0/arg1，
     * 不会与导航属性同名，故此处分两步验证：参数名不冲突时MapDefault不应误报；
     * 而按名称绑定时忽略大小写匹配属性，避免如homeCountry与HomeCountry仅大小写不同时漏判。
     */
    @Test
    public void mapDefaultAssociationReferenceTest() {
        var modelBuilder = createModelBuilder();
        configCountry(modelBuilder);

        var p = modelBuilder.entity(PersonWithDefaultMap.class);
        p.hasKeyAttribute(PersonWithDefaultMap::getPersonId).hasKeyIsSelfIncreased(false);
        p.toTable("PersonWithDefaultMap");

        var constructor = getCtor(PersonWithDefaultMap.class, long.class, Country.class);

        //参数名与属性名不冲突时 MapDefault不应误报
        assertDoesNotThrow(() -> {
            p.hasConstructor(constructor)
                    .map("PersonId")
                    .mapDefault()
                    .end();
        });

        //参数名homeCountry与导航属性HomeCountry仅大小写不同 忽略大小写匹配属性后应被拦截
        var exception = assertThrows(IllegalArgumentException.class, () ->
                p.hasConstructor(constructor)
                        .map("PersonId")
                        .map("homeCountry")
                        .end());

        assertTrue(exception.getMessage().contains("homeCountry"));
        assertTrue(exception.getMessage().contains("HomeCountry"));
        assertTrue(exception.getMessage().contains("AssociationEnd + HasMapping + AssociationReference"));
    }

    /**
     * 用例9：绑定元素名称写错（元素不存在） 建模阶段应给出点名元素名与类型名的异常
     */
    @Test
    public void mapNotExistElementTest() {
        var modelBuilder = createModelBuilder();

        var p = modelBuilder.entity(PersonWithWrongElementName.class);
        p.hasKeyAttribute(PersonWithWrongElementName::getPersonId).hasKeyIsSelfIncreased(false);
        p.toTable("PersonWithWrongElementName");
        //先把构造器配成合法的 完成一次建模让元素配置项就绪
        p.hasConstructor(getCtor(PersonWithWrongElementName.class, long.class, String.class))
                .map(PersonWithWrongElementName::getPersonId)
                .map(PersonWithWrongElementName::getName)
                .end();
        ObjectDataModel model = modelBuilder.build();
        var objectType = model.getObjectType(PersonWithWrongElementName.class);

        //再把name参数改绑到不存在的元素上（绕过Map阶段的校验）
        ((InstanceConstructor) objectType.getConstructor()).getParameters().get(1).setElementName("NotExistElement");

        var exception = assertThrows(IllegalArgumentException.class, () ->
                createProxyConstructor(PersonWithWrongElementName.class, objectType, modelBuilder));
        //异常应点名元素名与类型名
        assertTrue(exception.getMessage().contains("NotExistElement"));
        assertTrue(exception.getMessage().contains("PersonWithWrongElementName"));
    }

    /**
     * 用例10：不配置构造器 走无参构造推断 建模应通过
     */
    @Test
    public void implicitConstructorTest() {
        var modelBuilder = createModelBuilder();
        //Country有无参构造
        configCountry(modelBuilder);

        assertDoesNotThrow(() -> {
            modelBuilder.build();
        });
    }

    /**
     * 用例11：显式关联型 + 两端HasMapping + 构造参数只绑标量 建模应通过
     */
    @Test
    public void explicitAssociationWithScalarCtorTest() {
        var modelBuilder = createModelBuilder();
        configCountry(modelBuilder);

        var p = modelBuilder.entity(PersonWithExplicitReference.class);
        p.hasKeyAttribute(PersonWithExplicitReference::getPersonId).hasKeyIsSelfIncreased(false);
        p.toTable("PersonWithExplicitReference");
        p.hasConstructor(getCtor(PersonWithExplicitReference.class, long.class, String.class))
                .map(PersonWithExplicitReference::getPersonId)
                .map(PersonWithExplicitReference::getName)
                .end();

        var association = modelBuilder.association(PersonCountry.class);
        association.associationEnd(PersonCountry::getPerson).hasMapping("personId", "personId");
        association.associationEnd(PersonCountry::getCountry).hasMapping("countryId", "countryId");
        association.toTable("PersonCountry");

        assertDoesNotThrow(() -> {
            modelBuilder.build();
        });
    }
}
