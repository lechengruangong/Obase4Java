package io.obase.test.core.functional;

import io.obase.core.odm.IConcreteTypeDiscriminator;
import io.obase.core.odm.IntegrityCheckFailException;
import io.obase.core.odm.StructuralType;
import io.obase.core.odm.builder.ModelBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 测试配置继承关系时"必须先配父类"的校验与提示。
 * 父类未注册、或父类未配置具体类型判别值时，应在建模阶段抛出具名且说明改法的异常，
 * 而不是在建模深处抛出不点名任何信息的空引用异常。
 */
public class InheritanceConfigurationOrderTest {

    /**
     * 用例1：父类先配完整、再配子类 建模应通过（原有行为不变）
     */
    @Test
    public void parentConfiguredBeforeChildTest() {
        var modelBuilder = createModelBuilder();

        var animal = modelBuilder.entity(Animal.class);
        animal.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        animal.hasConcreteTypeDiscriminator("Type");
        animal.hasConcreteTypeSign(1);
        animal.toTable("Animal");

        var dog = modelBuilder.entity(Dog.class);
        dog.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        dog.deriveFrom(Animal.class);
        dog.hasConcreteTypeSign(2);
        dog.toTable("Animal");

        assertDoesNotThrow(() -> modelBuilder.build());
    }

    /**
     * 用例2：三级继承链 严格父先子后 建模应通过（原有行为不变）
     */
    @Test
    public void threeLevelInheritanceTest() {
        var modelBuilder = createModelBuilder();

        var animal = modelBuilder.entity(Animal.class);
        animal.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        animal.hasConcreteTypeDiscriminator("Type");
        animal.hasConcreteTypeSign(1);
        animal.toTable("Animal");

        var dog = modelBuilder.entity(Dog.class);
        dog.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        dog.deriveFrom(Animal.class);
        dog.hasConcreteTypeSign(2);
        dog.toTable("Animal");

        var puppy = modelBuilder.entity(Puppy.class);
        puppy.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        puppy.deriveFrom(Dog.class);
        puppy.hasConcreteTypeSign(3);
        puppy.toTable("Animal");

        assertDoesNotThrow(() -> modelBuilder.build());
    }

    /**
     * 用例3：抽象基类参与继承 判别值用一个不会被实际使用的值 建模应通过（原有行为不变）
     */
    @Test
    public void abstractBaseInheritanceTest() {
        var modelBuilder = createModelBuilder();

        var shape = modelBuilder.entity(Shape.class);
        shape.hasKeyAttribute("ShapeId").hasKeyIsSelfIncreased(true);
        shape.hasConcreteTypeDiscriminator("Type");
        //抽象类型不会被创建 配置一个特殊值即可
        shape.hasConcreteTypeSign(-1);
        shape.toTable("Shape");

        var circle = modelBuilder.entity(Circle.class);
        circle.hasKeyAttribute("ShapeId").hasKeyIsSelfIncreased(true);
        circle.deriveFrom(Shape.class);
        circle.hasConcreteTypeSign(1);
        circle.toTable("Shape");

        assertDoesNotThrow(() -> modelBuilder.build());
    }

    /**
     * 用例4：CLR(JVM)上有继承关系但未配置DeriveFrom 两者各自建表 建模应通过（原有行为不变）
     */
    @Test
    public void noDeriveFromConfiguredTest() {
        var modelBuilder = createModelBuilder();

        var animal = modelBuilder.entity(Animal.class);
        animal.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        animal.toTable("Animal");

        var dog = modelBuilder.entity(Dog.class);
        dog.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        dog.toTable("Dog");

        assertDoesNotThrow(() -> modelBuilder.build());
    }

    /**
     * 用例5：子类配置hasConcreteTypeDiscriminator而父类未注册 应抛具名异常而不是空引用异常
     */
    @Test
    public void childHasConcreteTypeDiscriminatorWithoutParentTest() {
        var modelBuilder = createModelBuilder();

        var dog = modelBuilder.entity(Dog.class);
        dog.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        dog.deriveFrom(Animal.class);

        var exception = assertThrows(IllegalArgumentException.class, () -> dog.hasConcreteTypeDiscriminator("Type"));
        //异常应点名子类与父类
        assertTrue(exception.getMessage().contains(nameOf(Dog.class)));
        assertTrue(exception.getMessage().contains(nameOf(Animal.class)));
    }

    /**
     * 用例6：子类配置hasConcreteTypeSign而父类未注册 应抛具名异常而不是空引用异常
     */
    @Test
    public void childHasConcreteTypeSignWithoutParentTest() {
        var modelBuilder = createModelBuilder();

        var dog = modelBuilder.entity(Dog.class);
        dog.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        dog.deriveFrom(Animal.class);

        var exception = assertThrows(IllegalArgumentException.class, () -> dog.hasConcreteTypeSign(1));
        //异常应点名子类与父类
        assertTrue(exception.getMessage().contains(nameOf(Dog.class)));
        assertTrue(exception.getMessage().contains(nameOf(Animal.class)));
    }

    /**
     * 用例7：子类只配置DeriveFrom而父类未注册 建模阶段应抛具名异常
     */
    @Test
    public void childOnlyDeriveFromWithoutParentTest() {
        var modelBuilder = createModelBuilder();

        var dog = modelBuilder.entity(Dog.class);
        dog.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        dog.deriveFrom(Animal.class);
        dog.toTable("Animal");

        var exception = assertThrows(IllegalArgumentException.class, modelBuilder::build);
        //异常应点名子类与父类 并指出需要先注册父类
        assertTrue(exception.getMessage().contains(nameOf(Dog.class)));
        assertTrue(exception.getMessage().contains(nameOf(Animal.class)));
        assertTrue(exception.getMessage().contains("注册"));
    }

    /**
     * 用例8：父类已注册但没有配置判别器与判别值 建模阶段应抛具名异常
     */
    @Test
    public void parentWithoutConcreteTypeSignTest() {
        var modelBuilder = createModelBuilder();

        var animal = modelBuilder.entity(Animal.class);
        animal.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        animal.toTable("Animal");

        var dog = modelBuilder.entity(Dog.class);
        dog.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        dog.deriveFrom(Animal.class);
        dog.toTable("Animal");

        var exception = assertThrows(IllegalArgumentException.class, modelBuilder::build);
        //异常应点名父类 并给出判别器与判别值的配置方法
        assertTrue(exception.getMessage().contains(nameOf(Animal.class)));
        assertTrue(exception.getMessage().contains("HasConcreteTypeDiscriminator"));
        assertTrue(exception.getMessage().contains("HasConcreteTypeSign"));
    }

    /**
     * 用例9：父类配置了判别器与判别值 但子类没有配置判别值 建模阶段应抛具名异常
     * （父类使用内置判别器时 建模需要收集整条继承链的判别值）
     */
    @Test
    public void childWithoutConcreteTypeSignTest() {
        var modelBuilder = createModelBuilder();

        var animal = modelBuilder.entity(Animal.class);
        animal.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        animal.hasConcreteTypeDiscriminator("Type");
        animal.hasConcreteTypeSign(1);
        animal.toTable("Animal");

        var dog = modelBuilder.entity(Dog.class);
        dog.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        dog.deriveFrom(Animal.class);
        dog.toTable("Animal");

        var exception = assertThrows(IllegalArgumentException.class, modelBuilder::build);
        //异常应点名未配置判别值的子类 并给出判别器与判别值的配置方法
        assertTrue(exception.getMessage().contains(nameOf(Dog.class)));
        assertTrue(exception.getMessage().contains("具体类型判别值"));
        assertTrue(exception.getMessage().contains("HasConcreteTypeSign"));
    }

    /**
     * 用例9-1：父类配置了自定义判别器与判别值 但子类没有配置判别值
     * 建模应给出完整性检查的说明性错误 而不是空引用异常
     */
    @Test
    public void childWithoutConcreteTypeSignWithCustomDiscriminatorTest() {
        var modelBuilder = createModelBuilder();

        var animal = modelBuilder.entity(Animal.class);
        animal.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        animal.hasConcreteTypeDiscriminator("Type", new NullConcreteTypeDiscriminator());
        animal.hasConcreteTypeSign(1);
        animal.toTable("Animal");

        var dog = modelBuilder.entity(Dog.class);
        dog.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        dog.deriveFrom(Animal.class);
        dog.toTable("Animal");

        var exception = assertThrows(IntegrityCheckFailException.class, modelBuilder::build);
        //完整性检查应点名子类 并指出没有配置具体类型判别标志
        StringBuilder messages = new StringBuilder();
        for (List<String> message : exception.getErrorMessageDictionary().values())
            for (String item : message)
                messages.append(item).append(";");
        assertTrue(messages.toString().contains(nameOf(Dog.class)));
        assertTrue(messages.toString().contains("具体类型判别"));
    }

    /**
     * 用例10：父类已注册但没有配置判别器 子类配置判别值时应给出既有的说明性提示
     */
    @Test
    public void parentWithoutDiscriminatorChildHasSignTest() {
        var modelBuilder = createModelBuilder();

        var animal = modelBuilder.entity(Animal.class);
        animal.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        animal.toTable("Animal");

        var dog = modelBuilder.entity(Dog.class);
        dog.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        dog.deriveFrom(Animal.class);

        var exception = assertThrows(IllegalArgumentException.class, () -> dog.hasConcreteTypeSign(2));
        //既有的说明性提示 指出需要先在基类上配置判别字段
        assertTrue(exception.getMessage().contains("没有找到类型判别字段"));
        assertTrue(exception.getMessage().contains("HasConcreteTypeDiscriminator"));
    }

    /**
     * 用例11：子类先DeriveFrom、父类随后注册并配置、最后补子类判别值 建模应通过。
     * DeriveFrom本身不校验父类是否已注册 注册顺序只要保证建模时父类已在模型中即可。
     */
    @Test
    public void parentRegisteredAfterChildDeriveFromTest() {
        var modelBuilder = createModelBuilder();

        //先注册子类并声明继承 此时父类还未注册
        var dog = modelBuilder.entity(Dog.class);
        dog.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        dog.deriveFrom(Animal.class);

        //再注册并配置父类
        var animal = modelBuilder.entity(Animal.class);
        animal.hasKeyAttribute("Id").hasKeyIsSelfIncreased(true);
        animal.hasConcreteTypeDiscriminator("Type");
        animal.hasConcreteTypeSign(1);
        animal.toTable("Animal");

        //最后补上子类的判别值
        dog.hasConcreteTypeSign(2);
        dog.toTable("Animal");

        assertDoesNotThrow(() -> modelBuilder.build());
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
     * 取Java类型的限定名称 用于断言异常消息中点名的类型
     *
     * @param type Java类型
     * @return 类型的限定名称
     */
    private static String nameOf(Class<?> type) {
        return type.getName();
    }

    /**
     * 基类
     */
    public static class Animal {

        /**
         * 标识
         */
        private long id;

        /**
         * 名称
         */
        private String name;

        public long getId() {
            return this.id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * 派生类
     */
    public static class Dog extends Animal {

        /**
         * 品种
         */
        private String breed;

        public String getBreed() {
            return this.breed;
        }

        public void setBreed(String breed) {
            this.breed = breed;
        }
    }

    /**
     * 三级继承链的最末一级
     */
    public static class Puppy extends Dog {

        /**
         * 年龄
         */
        private int age;

        public int getAge() {
            return this.age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    /**
     * 抽象基类
     */
    public abstract static class Shape {

        /**
         * 标识
         */
        private long shapeId;

        public long getShapeId() {
            return this.shapeId;
        }

        public void setShapeId(long shapeId) {
            this.shapeId = shapeId;
        }
    }

    /**
     * 抽象基类的派生类
     */
    public static class Circle extends Shape {

        /**
         * 半径
         */
        private double radius;

        public double getRadius() {
            return this.radius;
        }

        public void setRadius(double radius) {
            this.radius = radius;
        }
    }

    /**
     * 不做实际判别的自定义判别器 用于验证配置了自定义判别器时的建模行为
     */
    private static class NullConcreteTypeDiscriminator implements IConcreteTypeDiscriminator {

        /**
         * 根据类型代码选择一个具体类型
         *
         * @param typeCode 类型代码
         * @return 本用例中不使用
         */
        @Override
        public StructuralType discriminate(Object typeCode) {
            return null;
        }
    }
}
