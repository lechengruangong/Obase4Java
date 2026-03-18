package io.obase.test.infrastructure.modelRegister;

import io.obase.core.odm.DelegateValueGetter;
import io.obase.core.odm.EConcurrentConflictHandlingStrategy;
import io.obase.core.odm.EValueSettingMode;
import io.obase.core.odm.builder.EAttributeCombinationHandlingStrategy;
import io.obase.core.odm.builder.ModelBuilder;
import io.obase.test.domain.association.Class;
import io.obase.test.domain.association.*;
import io.obase.test.domain.association.defaultAsNew.*;
import io.obase.test.domain.association.duplicateMapping.GoodsAttribute;
import io.obase.test.domain.association.duplicateMapping.SelectableValue;
import io.obase.test.domain.association.duplicateMapping.StandardValue;
import io.obase.test.domain.association.explicitlyCompion.CarWheel;
import io.obase.test.domain.association.explicitlySelf.Friend;
import io.obase.test.domain.association.explicitlySelf.Guest;
import io.obase.test.domain.association.implement.*;
import io.obase.test.domain.association.multiAssociationEnd.Property;
import io.obase.test.domain.association.multiAssociationEnd.PropertyTakingValue;
import io.obase.test.domain.association.multiAssociationEnd.PropertyValue;
import io.obase.test.domain.association.multiImplicitSearch.Category;
import io.obase.test.domain.association.multiImplicitSearch.Product;
import io.obase.test.domain.association.multiImplicitSearch.ProductCategory;
import io.obase.test.domain.association.multiplexAssociation.Employee;
import io.obase.test.domain.association.multiplexAssociation.OfficeRoom;
import io.obase.test.domain.association.noAssociationExtAttr.*;
import io.obase.test.domain.association.selectResult.SimpleGroup;
import io.obase.test.domain.association.selectResult.SimpleStu;
import io.obase.test.domain.association.self.Area;
import io.obase.test.domain.association.self.FriendlyArea;
import io.obase.test.domain.functional.*;
import io.obase.test.domain.functional.complexKeyValueWithVersion.*;
import io.obase.test.domain.functional.dataError.DataErrorStudent;
import io.obase.test.domain.functional.dataError.DataErrorStudentInfo;
import io.obase.test.domain.functional.dependencyInjection.*;
import io.obase.test.domain.functional.expression.Box;
import io.obase.test.domain.functional.expression.Can;
import io.obase.test.domain.functional.expression.WaterTank;
import io.obase.test.domain.functional.keyValueVersion.*;
import io.obase.test.domain.simpleType.*;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 核心的模型注册器
 */
public class CoreModelRegister {

    /**
     * 注册方法
     *
     * @param modelBuilder 建模器
     */
    @SuppressWarnings("Convert2MethodRef")
    public static void registry(ModelBuilder modelBuilder) {
        //忽略项
        modelBuilder.ignore(SimpleJavaBeanSelect.class);
        modelBuilder.ignore(SmallJavaBeanLikeModel.class);
        modelBuilder.ignore(SimpleGroup.class);
        modelBuilder.ignore(SimpleStu.class);
        modelBuilder.ignore(ServiceSA.class);
        modelBuilder.ignore(ServiceTA.class);
        modelBuilder.ignore(ServiceSB.class);
        modelBuilder.ignore(ServiceTB.class);
        modelBuilder.ignore(ServiceSC.class);
        modelBuilder.ignore(ServiceTC.class);
        modelBuilder.ignore(ServiceSD.class);
        modelBuilder.ignore(ServiceTD.class);
        modelBuilder.ignore(ServiceSE.class);
        modelBuilder.ignore(ServiceTE.class);
        modelBuilder.ignore(ServiceSF.class);
        modelBuilder.ignore(ServiceTF.class);
        modelBuilder.ignore(ServiceSG.class);
        modelBuilder.ignore(ServiceTG.class);
        modelBuilder.ignore(ServiceSH.class);
        modelBuilder.ignore(ServiceTH.class);
        modelBuilder.ignore(Box.class);
        modelBuilder.ignore(Can.class);
        modelBuilder.ignore(WaterTank.class);

        //单独注册几个类型
        modelBuilder.registerType(School.class, Student.class);

        //符合推断的用程序集都注册方法注册
        modelBuilder.registerType("io.obase.test.domain");

        //对应测试core.simple文件夹内NullableSimpleTypeEnumerableTest/SimpleTypeEnumerableTest/SimpleTypeWithConstructorArgsEnumerableTest
        //region 基础失血模型

        //失血模型 主键不符合推断 需要自定义属性
        var javaBeanLikeModelConfiguration = modelBuilder.entity(JavaBean.class);
        //主键 和 主键是否自增
        javaBeanLikeModelConfiguration.hasKeyAttribute(JavaBean::getIntNumber).hasKeyIsSelfIncreased(false);
        //自定义的属性
        javaBeanLikeModelConfiguration.attribute(JavaBean::getStrings, String.class)
                .useSerializer(new CommaSplitSerializer(), String[].class)
                //设置为255长 超过255会令数据库建表类型变为Text
                .hasMaxCharNumber(255)
                //设置为不可空
                .hasNullable(false);
        //自定义精度 精度固定为(M,N) M即数据库decimal字段的最大值 MySql为65 SqlServer为38 Sqlite没有此概念 N即为HasPrecision设置的值 不能超过28
        javaBeanLikeModelConfiguration.attribute(JavaBean::getDecimalNumber).hasPrecision((byte) 5);

        //使用多个构造函数参数的失血模型 主键不符合推断 需要自定义属性 需要自定义构造函数
        var javaBeanWithConstructorArgsConfiguration = modelBuilder.entity(JavaBeanWithConstructorArgs.class);
        //主键 和 主键是否自增
        javaBeanWithConstructorArgsConfiguration.hasKeyAttribute(JavaBeanWithConstructorArgs::getIntNumber).hasKeyIsSelfIncreased(false);
        Constructor<JavaBeanWithConstructorArgs> constructor1;
        try {
            constructor1 = JavaBeanWithConstructorArgs.class.getConstructor(BigDecimal.class, LocalDateTime.class, String.class, boolean.class, int.class, long.class, byte.class, char.class, float.class, double.class, LocalTime.class, LocalDate.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("无法获取到JavaBeanWithConstructorArgs的构造函数", e);
        }
        //自定义的构造函数
        javaBeanWithConstructorArgsConfiguration
                //指定构造函数
                .hasConstructor(constructor1)
                //指定构造函数参数 配置哪个参数为哪个字段
                .map(JavaBeanWithConstructorArgs::getDecimalNumber)
                .map(JavaBeanWithConstructorArgs::getDateTime)
                .map(JavaBeanWithConstructorArgs::getString)
                .map(JavaBeanWithConstructorArgs::getBool)
                .map(JavaBeanWithConstructorArgs::getIntNumber)
                .map(JavaBeanWithConstructorArgs::getLongNumber)
                .map(JavaBeanWithConstructorArgs::getByteNumber)
                .map(JavaBeanWithConstructorArgs::getCharNumber)
                .map(JavaBeanWithConstructorArgs::getFloatNumber)
                .map(JavaBeanWithConstructorArgs::getDoubleNumber)
                .map(JavaBeanWithConstructorArgs::getTime)
                .map(JavaBeanWithConstructorArgs::getDate)
                .end();
        //自定义的属性
        javaBeanWithConstructorArgsConfiguration.attribute(JavaBeanWithConstructorArgs::getStrings, String.class)
                .useSerializer(new JsonSerializer(), String[].class);


        //可空值类型 主键不符合推断
        var nullableJavaBeanConfiguration = modelBuilder.entity(NullableJavaBean.class);
        //配置主键
        nullableJavaBeanConfiguration.hasKeyAttribute(NullableJavaBean::getIntNumber).hasKeyIsSelfIncreased(false);

        //endregion

        //对应测试core.association文件夹内AssociationQueryTest/AssociationUpdateAndDeleteTest/CompositePrimaryKeyTest/SelfAssociationTest/
        //AggregatedEndTest
        //region 基础关系模型

        //配置实体型
        //学校无需配置 符合推断
        //School

        //班级基本符合推断 只需要忽略Teachers
        modelBuilder.entity(Class.class).ignore(p -> p.getTeachers());

        //学生 符合推断 无需配置
        //Student

        //学生信息 符合推断
        //StudentInfo

        //老师 符合推断 无需配置
        //Teacher

        //通行证 不符合推断 是个联合主键 且 没有无参构造函数
        var passPaperCfg = modelBuilder.entity(PassPaper.class);
        //联合主键
        passPaperCfg.hasKeyAttribute(p -> p.getTeacherId()).hasKeyAttribute(p -> p.getType()).hasKeyIsSelfIncreased(false);
        Constructor<PassPaper> constructor2;
        try {
            constructor2 = PassPaper.class.getConstructor(long.class, EPassPaperType.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("无法获取到PassPaper的构造函数", e);
        }
        //构造函数
        passPaperCfg.hasConstructor(constructor2).map(p -> p.getTeacherId()).map(p -> p.getType()).end();

        //区域 符合推断 无需配置
        //Area

        //配置关联型
        //班级->学校 关联 基本符合推断 无需配置

        //班级->学生 关联 基本符合推断 需要配置关联引用
        var classStudent = modelBuilder.association();
        //配置Class端 映射符合推断 不需要配置映射
        var classStudentEnd1 = classStudent.associationEnd(Class.class);
        //Class端的关联引用 配置特殊的取值器和设值器 延迟加载
        classStudentEnd1.associationReference(Class::getStudents);
        //配置Student端 符合推断 只需要配置延迟加载
        classStudent.associationEnd(Student.class).associationReference(Student::getClazz).hasEnableLazyLoading(true);

        //班级->老师 关联 不符合推断 没有无参构造函数 需要自定义属性
        var classAssTeacher = modelBuilder.association(ClassTeacher.class);
        //配置Class端 映射符合推断 不需要配置映射
        var classAssTeacherEnd1 = classAssTeacher.associationEnd(ClassTeacher::getClazz);
        //设置关联端延迟加载
        classAssTeacherEnd1.hasEnableLazyLoading(true);
        //设置关联引用延迟加载
        classAssTeacherEnd1.associationReference("ClassTeachers", true);
        //配置Teacher端 设置关联端延迟加
        classAssTeacher.associationEnd(ClassTeacher::getTeacher).hasEnableLazyLoading(true);
        //特殊配置属性
        classAssTeacher.attribute("Subject", String.class)
                .hasValueGetter((ClassTeacher classTeacher) -> {
                    if (classTeacher.getSubject() != null && classTeacher.getSubject().size() > 0)
                        return String.join(",", classTeacher.getSubject());
                    else
                        return "";
                })
                .hasValueSetter((ClassTeacher classTeacher, String s) -> {
                    if (s != null && !s.isEmpty())
                        classTeacher.setSubject(new ArrayList<>(Arrays.asList(s.split(","))));
                });
        Constructor<ClassTeacher> constructor3;
        try {
            constructor3 = ClassTeacher.class.getDeclaredConstructor(long.class, long.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("无法获取到ClassTeacher的构造函数", e);
        }
        //构造函数
        classAssTeacher.hasConstructor(constructor3).map("ClassId").map("TeacherId").end();
        Constructor<ClassTeacher> constructor4;
        try {
            constructor4 = ClassTeacher.class.getConstructor(long.class, long.class, boolean.class, boolean.class, List.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("无法获取到ClassTeacher的构造函数", e);
        }
        //新实例构造函数
        classAssTeacher.hasNewInstanceConstructor(constructor4);

        //学生->学校 关联 符合推断 但需要配置延迟加载
        var studentSchool = modelBuilder.association();
        //配置Student端 映射符合推断 不需要配置映射
        var studentSchoolEnd1 = studentSchool.associationEnd(Student.class);
        //Student端的关联引用 配置延迟加载
        studentSchoolEnd1.associationReference(p -> p.getSchool()).hasEnableLazyLoading(true);
        //配置School端 符合推断 无需配置
        studentSchool.associationEnd(School.class);

        //学生->班级 关联 符合推断 在上文班级->学生中已配置

        //学生->学生信息 关联 无法推断关联表 需要设置关联表 StudentInfo端需要设置默认为新对象附加
        var studentStudentInfo = modelBuilder.association();
        //Student端 符合推断 无需配置
        studentStudentInfo.associationEnd(Student.class);
        //配置StudentInfo端 设置为伴随端 关联表即为StudentInfo的映射表
        var studentInfoEnd2 = studentStudentInfo.associationEnd(StudentInfo.class).asCompanion(true);
        //默认附加为新对象
        //具体的作用可以查看下面关联端是否作为新对象创建部分的说明
        studentInfoEnd2.hasDefaultAsNew(true);
        //并且设置此端为聚合的 默认为false
        //设置为ture 表示聚合的端对象在被聚合的关系解除或者关系的另外一端被删除是会被一并删除
        //当前关系中 即删除学生时和解除学生和学生信息关系时(如把学生中的学生信息置空或者替换,如果是一对多的从集合中移除) 学生信息也会被删除
        studentInfoEnd2.isAggregated(true);

        //老师->学校关联 需要配置关联引用
        var teacherSchool = modelBuilder.association();
        //Teacher端 符合推断 无需配置 只需要配置关联引用的延迟加载
        teacherSchool.associationEnd(Teacher.class).associationReference(p -> p.getSchool()).hasEnableLazyLoading(true);
        //School端 符合推断 无需配置
        teacherSchool.associationEnd(School.class);

        //教师->通行证关联
        var teacherPassPaper = modelBuilder.association();
        //teacher端 符合推断 无需配置 只需要配置关联引用的延迟加载
        teacherPassPaper.associationEnd(Teacher.class).associationReference(p -> p.getPassPaperList()).hasEnableLazyLoading(true);
        //PassPaper端 符合推断 无需配置 只需要配置关联引用的延迟加载
        teacherPassPaper.associationEnd(PassPaper.class).associationReference(p -> p.getTeacher()).hasEnableLazyLoading(true);

        //学生->学生一卡通账户关联 符合推断 无需配置

        //区域的自关联
        var areaArea = modelBuilder.association();
        //配置第一个Area端
        var areaEnd1 = areaArea.associationEnd(Area.class);
        //配置映射
        areaEnd1.hasMapping("Code", "Code");
        //配置是否启用延迟加载
        areaEnd1.hasEnableLazyLoading(true);
        //配置关联引用和关联引用的延迟加载
        var subRef = areaEnd1.associationReference(p -> p.getParentArea());
        subRef.hasEnableLazyLoading(true);
        //配置第二个Area端
        var areaEnd2 = areaArea.associationEnd(Area.class);
        //配置映射
        areaEnd2.hasMapping("Code", "ParentCode");
        //配置是否启用延迟加载
        areaEnd2.hasEnableLazyLoading(true);
        //配置关联引用和关联引用的延迟加载
        var parentRef = areaEnd2.associationReference(p -> p.getSubAreas());
        parentRef.hasEnableLazyLoading(true);
        //映射表
        areaArea.toTable("Area");

        //区域的显式自关联 友好区域
        var friendlyArea = modelBuilder.association(FriendlyArea.class);
        //配置第一个Area端
        var friendlyAreaEnd1 = friendlyArea.associationEnd(p -> p.getArea());
        //配置延迟加载 映射
        friendlyAreaEnd1.hasMapping("Code", "AreaCode").hasEnableLazyLoading(true);
        //配置关联引用 是否启用延迟加载 映射
        friendlyAreaEnd1.associationReference("FriendlyAreas", true).hasEnableLazyLoading(true);
        //配置第二个Area端 配置映射 是否启用延迟加载
        friendlyArea.associationEnd(p -> p.getFriend()).hasMapping("Code", "FriendlyAreaCode").hasEnableLazyLoading(true);
        //映射表
        friendlyArea.toTable("FriendlyArea");

        //宾客的显式自关联
        //将宾客配置为实体型
        var guestEntity = modelBuilder.entity(Guest.class);
        //配置主键
        guestEntity.hasKeyAttribute(p -> p.getGuestId());

        //为宾客和宾客的朋友关系配置显式关联型
        var guestAssGuestAssociation = modelBuilder.association(Friend.class);
        //配置关联端 此处为自关联 两端都是Guest 所以只要配置每个端即可 无需根据类型进行判定 MySelf这一端在关联表中Friend的主键GuestIde映射为MySelfId
        var guestEnd1 = guestAssGuestAssociation.associationEnd(p -> p.getMySelf());
        guestEnd1.hasMapping("GuestId", "MySelfId");
        guestEnd1.associationReference("MyFriends", true);
        //配置关联端 此处为自关联 两端都是Guest 所以只要配置每个端即可 无需根据类型进行判定 FriendGuest这一端在关联表中Friend的主键GuestId映射为FriendFriendId
        var guestEnd2 = guestAssGuestAssociation.associationEnd(p -> p.getFriendGuest());
        guestEnd2.hasMapping("GuestId", "FriendId");
        guestEnd2.associationReference("FriendOfMes", true);

        //endregion

        //对应测试文件core.association文件夹内DefaultAsNewTest
        //region 关联端是否作为新对象创建

        //关联端是否默认创建新对象配置控制如果某一个对象被创建出来后 未附加至上下文 但作为其他已附加对象的引用对象时 是否作为新对象附加至上下文
        //默认为不作为 因为对象往往是由应用层创建的 是否需要附加由应用层决定即可
        //但 如果某个对象的关联是无法通过此对象外部进行创建 如只能在构造函数内一起创建时 外部无法获取这个被一起创建的对象进行附加操作
        //就需要将关联端是否作为新对象创建设为true

        //默认作为不新对象创建的学校
        var defaultSchoolConfig = modelBuilder.entity(DefaultSchool.class);
        //配置主键
        defaultSchoolConfig.hasKeyAttribute(p -> p.getSchoolId()).hasKeyIsSelfIncreased(true);
        //配置映射表
        defaultSchoolConfig.toTable("School");

        //默认不作为新对象创建的学生
        var defaultStudentCfgConfiguration = modelBuilder.entity(DefaultStudent.class);
        //配置主键
        defaultStudentCfgConfiguration.hasKeyAttribute(p -> p.getStudentId()).hasKeyIsSelfIncreased(true);
        //配置映射表
        defaultStudentCfgConfiguration.toTable("Student");

        //默认不作为新对象创建的教师
        var defaultTeacherConfig = modelBuilder.entity(DefaultTeacher.class);
        //配置主键
        defaultTeacherConfig.hasKeyAttribute(p -> p.getTeacherId()).hasKeyIsSelfIncreased(true);
        //配置映射表
        defaultTeacherConfig.toTable("Teacher");

        //默认作为新对象创建的班级
        var defaultNewClassCfg = modelBuilder.entity(DefaultNewClass.class);
        //配置主键
        defaultNewClassCfg.hasKeyAttribute(p -> p.getClassId()).hasKeyIsSelfIncreased(true);
        //配置映射表
        defaultNewClassCfg.toTable("Class");

        //默认作为新对象创建的班级->默认不作为新对象创建的学生关系
        var defaultNewClassAssociationStudent = modelBuilder.association();
        //设置关联端 此端有引用 映射符合推断
        defaultNewClassAssociationStudent.associationEnd(DefaultNewClass.class)
                //配置引用
                .associationReference(p -> p.getStudents());
        //设置关联端 此端没有引用 映射符合推断 配置默认作为新对象创建
        defaultNewClassAssociationStudent.associationEnd(DefaultStudent.class).hasDefaultAsNew(true);

        //默认作为新对象创建的班级->默认不作为新对象创建的学校的关联
        var defaultNewClassAssociationSchool = modelBuilder.association();
        //设置关联端 此端有引用 映射符合推断
        defaultNewClassAssociationSchool.associationEnd(DefaultNewClass.class)
                //配置引用
                .associationReference(p -> p.getSchool());
        //设置关联端 此端没有引用 映射符合推断 配置默认作为新对象创建
        defaultNewClassAssociationSchool.associationEnd(DefaultSchool.class).hasDefaultAsNew(true);

        //默认作为新对象创建的任课教师关联
        var defaultNewClassAssociationClassTeacher = modelBuilder.association(DefaultNewClassTeacher.class);
        //配置关联端 此端有引用 映射符合推断
        defaultNewClassAssociationClassTeacher.associationEnd(p -> p.getClazz())
                //配置引用
                .associationReference("ClassTeachers", true);
        //设置关联端 此端没有引用 映射符合推断 配置默认作为新对象创建
        defaultNewClassAssociationClassTeacher.associationEnd(p -> p.getTeacher()).hasDefaultAsNew(true);
        //设置关联表
        defaultNewClassAssociationClassTeacher.toTable("ClassTeacher");

        //默认不作为新对象创建的班级
        var defaultClassCfg = modelBuilder.entity(DefaultClass.class);
        //配置主键
        defaultClassCfg.hasKeyAttribute(p -> p.getClassId()).hasKeyIsSelfIncreased(true);
        //配置映射表
        defaultClassCfg.toTable("Class");

        //默认不作为新对象创建的班级->默认不作为新对象创建的学生关系
        var defaultClassAssociationStudent = modelBuilder.association();
        //设置关联端 此端有引用 映射符合推断
        defaultClassAssociationStudent.associationEnd(DefaultClass.class)
                //配置引用
                .associationReference(p -> p.getStudents());
        //设置关联端 此端没有引用 映射符合推断
        defaultClassAssociationStudent.associationEnd(DefaultStudent.class);

        //默认不作为新对象创建的班级->默认不作为新对象创建的学校的关联
        var defaultClassAssociationSchool = modelBuilder.association();
        //设置关联端 此端有引用 映射符合推断
        defaultClassAssociationSchool.associationEnd(DefaultClass.class)
                //配置引用
                .associationReference(p -> p.getSchool());
        //设置关联端 此端没有引用 映射符合推断 配置默认作为新对象创建
        defaultClassAssociationSchool.associationEnd(DefaultSchool.class);

        //默认不作为新对象创建的任课教师关联型
        var defaultClassAssociationClassTeacher = modelBuilder.association(DefaultClassTeacher.class);
        //配置关联端 此端有引用 映射符合推断
        defaultClassAssociationClassTeacher.associationEnd(p -> p.getClazz())
                //配置引用
                .associationReference("ClassTeachers", true);
        //配置关联端 此端无引用 映射符合推断
        defaultClassAssociationClassTeacher.associationEnd(p -> p.getTeacher());
        defaultClassAssociationClassTeacher.toTable("ClassTeacher");

        //endregion

        //对应测试文件core.association文件夹内NoAssociationAttrTest
        //region 无关联冗余属性的关联

        //关联冗余属性即对象上为关联定义的外键等属性
        //对于Obase 不定义这些属性也是可以支持的 只需要映射表内存在即可
        //当然 一般都会保留这些属性 用于查询优化 如A和B为一对多关联 在B上定义A的ID可以简单的检索所有与A有关联的B

        //无关联冗余属性的学校 不符合推断
        var noAttrSchoolCfg = modelBuilder.entity(NoAssociationExtAttrSchool.class);
        //配置主键
        noAttrSchoolCfg.hasKeyAttribute(p -> p.getSchoolId());
        //配置映射表
        noAttrSchoolCfg.toTable("School");

        //无关联冗余属性的班级 不符合推断
        var noAttrClassCfg = modelBuilder.entity(NoAssociationExtAttrClass.class);
        //配置主键
        noAttrClassCfg.hasKeyAttribute(p -> p.getClassId());
        //配置映射表
        noAttrClassCfg.toTable("Class");

        //无关联冗余属性的学生 不符合推断
        var noAttrStudentCfg = modelBuilder.entity(NoAssociationExtAttrStudent.class);
        //配置主键
        noAttrStudentCfg.hasKeyAttribute(p -> p.getStudentId()).hasKeyIsSelfIncreased(true);
        //配置映射表
        noAttrStudentCfg.toTable("Student");

        //无关联冗余属性的老师
        var noAttrTeacherCfg = modelBuilder.entity(NoAssociationExtAttrTeacher.class);
        //配置主键
        noAttrTeacherCfg.hasKeyAttribute(p -> p.getTeacherId()).hasKeyIsSelfIncreased(true);
        //配置映射表
        noAttrTeacherCfg.toTable("Teacher");


        //无关联冗余属性的班级->学校 关联
        var noAttrSchoolSchoolClassAss = modelBuilder.association();
        //配置无关联冗余属性的班级端
        noAttrSchoolSchoolClassAss.associationEnd(NoAssociationExtAttrClass.class)
                //配置相应的关联引用和延迟加载
                .associationReference(p -> p.getSchool()).hasEnableLazyLoading(true);
        //配置无关联冗余属性的学校端
        noAttrSchoolSchoolClassAss.associationEnd(NoAssociationExtAttrSchool.class);

        //无关联冗余属性的班级->老师关联型
        var noAttrSchoolClassTeacherAss = modelBuilder.association(NoAssociationExtAttrClassTeacher.class);
        //配置无关联冗余属性的班级端
        noAttrSchoolClassTeacherAss.associationEnd(p -> p.getClazz())
                //配置相应的关联引用和延迟加载
                .associationReference("ClassTeachers", true)
                .hasEnableLazyLoading(true);
        //配置无关联冗余属性的老师
        noAttrSchoolClassTeacherAss.associationEnd(p -> p.getTeacher());
        //配置特殊的属性
        noAttrSchoolClassTeacherAss.attribute("Subject", String.class)
                .hasValueGetter((NoAssociationExtAttrClassTeacher classTeacher) -> {
                    if (classTeacher.getSubject() != null && classTeacher.getSubject().size() > 0)
                        return String.join(",", classTeacher.getSubject());
                    else
                        return "";
                })
                .hasValueSetter((NoAssociationExtAttrClassTeacher classTeacher, String s) -> {
                    if (s != null && !s.isEmpty())
                        classTeacher.setSubject(new ArrayList<>(Arrays.asList(s.split(","))));
                });
        //配置映射表
        noAttrSchoolClassTeacherAss.toTable("ClassTeacher");

        //无关联冗余属性的学生->班级关联
        var noAttrStudentClassAss = modelBuilder.association();
        noAttrStudentClassAss.associationEnd(NoAssociationExtAttrStudent.class)
                .associationReference(p -> p.getClazz());
        noAttrStudentClassAss.associationEnd(NoAssociationExtAttrClass.class)
                .associationReference(p -> p.getStudents()).hasEnableLazyLoading(true);

        //endregion

        //对应测试文件core.association文件夹内ExplicitlyCompionTest
        //region 显式关联伴随存储

        //对于显式关联型 通常都会使用独立映射表进行存储
        //但也是可以进行伴随存储的

        //配置汽车的实体型 符合推断 无需配置
        //Car

        //配置车轮实体型 符合推断 无需配置
        //Wheel

        //配置汽车车轮 显式关联型 默认是存储在类名的表内 也可以指定为伴随存储
        var carWheelAssociation = modelBuilder.association(CarWheel.class);
        //配置Car端
        carWheelAssociation.associationEnd("Car");
        //配置Wheel端
        carWheelAssociation.associationEnd("Wheel");
        //没有独立映射表 和 Wheel存储在一起
        carWheelAssociation.toTable("Wheel");

        //endregion

        //对应测试文件core.association文件夹内MultiplexAssociationTest
        //region 两个类之间有多种关系

        //当两个类间有多种关系时 只需要分别配置关系即可
        //配置员工实体型 符合推断
        //Employee

        //配置办公室 不符合推断 配置主键
        modelBuilder.entity(OfficeRoom.class).hasKeyAttribute(p -> p.getRoomCode()).hasKeyIsSelfIncreased(false);

        //开启一个新的隐式关联配置
        var manageAssociationTypeConfiguration = modelBuilder.association();
        //配置第一个端
        manageAssociationTypeConfiguration.associationEnd(Employee.class)
                //对于当两个类间有多种关系时 必须配置关联引用以保证不同的引用使用不同的隐式关联型
                .associationReference(p -> p.getManageRooms());
        //另外一个端
        manageAssociationTypeConfiguration.associationEnd(OfficeRoom.class);
        //映射表
        manageAssociationTypeConfiguration.toTable("ManageRoom");

        //开启一个新的隐式关联配置
        var workAssociationTypeConfiguration = modelBuilder.association();
        //配置第一个端
        workAssociationTypeConfiguration.associationEnd(Employee.class)
                //对于当两个类间有多种关系时 必须配置关联引用以保证不同的引用使用不同的隐式关联型
                .associationReference(p -> p.getWorkRoom());
        //另外一个端
        workAssociationTypeConfiguration.associationEnd(OfficeRoom.class)
                .hasMapping("RoomCode", "WorkRoomCode");
        //映射表
        workAssociationTypeConfiguration.toTable("Employee");

        //endregion

        //对应测试文件core.association文件夹内ImplicitMultiSearchTest
        //region 隐式关联的显式化(隐式多对多的搜索优化)

        //多对多的隐式关联 必须使用独立映射表
        //如果需要根据一端的某个值筛选另外一端 或者 需要根据两端的属性来筛选
        //常规的查询需要从一段的映射表连接关联的独立映射表再连接要查询的一端
        //此时可以将关联型配置为显式关联型 查询时就可以从关联的独立映射表进行查询
        //并且可以在此关联型上定义需要的筛选属性 直接进行筛选无需查询其他端


        //将产品配置为实体型 符合推断 无需配置
        //Product

        //将分类配置为实体型 符合推断 无需配置
        //Category

        //配置显式化的隐式多对多关联型
        var implicitMultiAssociation = modelBuilder.association(ProductCategory.class);
        //配置产品关联端 在关联表中映射为主键Code->字段ProductCode 映射符合推断
        var productEnd = implicitMultiAssociation.associationEnd(p -> p.getProduct());
        //配置分类关联端 在关联表中映射为主键CategoryId->字段CategoryId 映射符合推断
        var categoryEnd = implicitMultiAssociation.associationEnd(p -> p.getCategory());
        //多对多 独立关联表 默认的关联表名会被推断为ProductAssCategory
        implicitMultiAssociation.toTable("ProductCategory");

        // 如果在概念建模阶段就注意到需要此种查询 域类已将此关联设置为显示关联时 类内会直接定义关联引用为List<ProductCategory> 则此处不需要做此转换
        //此下的配置为领域模型未将关联引用显式化时的配置方式
        //配置关联引用 注意此处使用的配置方法 使用的是手动配置方法
        productEnd.associationReference("Categories", true)
                //配置取值器 即从对象中取值的方法 此处即为从关联型转换为List<Category>
                .hasValueGetter(new DelegateValueGetter<Product, List<ProductCategory>>(p ->
                {
                    if (p.getCategories() == null || p.getCategories().size() == 0)
                        return null;
                    List<ProductCategory> productCategories = new ArrayList<>();
                    for (Category category : p.getCategories()) {
                        generateProductCategory(p, productCategories, category);
                    }
                    return productCategories;
                }))
                //配置设值器 即为对象设置值 此处即为从关联型转换为List<Category>
                .hasValueSetter((Product p, ProductCategory impValue) ->
                {
                    if (impValue != null) {
                        if (p.getCategories() == null)
                            p.setCategories(new ArrayList<>());
                        //检查是否为自己的关联 以及去重
                        if (p.getCode().equals(impValue.getProductCode()) && p.getCategories().stream().allMatch(q -> q != null && q.getCategoryId() != impValue.getCategoryId()))
                            p.getCategories().add(impValue.getCategory());
                    }
                }, EValueSettingMode.Appending).hasEnableLazyLoading(true);

        //配置关联引用 注意此处使用的配置方法 指定的泛型参数为关联型类型
        categoryEnd.associationReference("Products", true)
                //配置取值器 即从对象中取值的方法 此处即为从关联型转换为List<Product>
                .hasValueGetter(new DelegateValueGetter<Category, List<ProductCategory>>(p ->
                {
                    if (p.getProducts() == null || p.getProducts().size() == 0)
                        return null;
                    List<ProductCategory> productCategories = new ArrayList<>();
                    for (Product product : p.getProducts()) {
                        generateProductCategory(product, productCategories, p);
                    }
                    return productCategories;
                }))
                //配置设值器 即为对象设置值 此处即为从关联型转换为List<Product>
                .hasValueSetter((Category p, ProductCategory impValue) ->
                {
                    if (impValue != null) {
                        if (p.getProducts() == null)
                            p.setProducts(new ArrayList<>());
                        //检查是否为自己的关联 以及去重
                        if (p.getCategoryId() == impValue.getCategoryId()
                                && p.getProducts().stream().allMatch(q -> q != null && !Objects.equals(q.getCode(), impValue.getProductCode())))
                            p.getProducts().add(impValue.getProduct());
                    }
                }, EValueSettingMode.Appending);

        //endregion

        //对应测试文件core.association文件夹内MultiAssociationEndTest
        //region 多方关联(多个关联端的关联)

        //当参与关联的关联方多于2个时 关联称之为多方关联
        //多方关联与一般的两方关联并无特殊的区别 都可以配置为显式或隐式
        //Obase支持将元组解析为隐式多方关联

        //将产品配置为实体型 符合推断
        //Domain.Multi.Product

        //将属性配置为实体型 符合推断
        //Property

        //将属性取值配置为实体型 符合推断
        //PropertyValue


        //多方关联 即关联型上有数个关联端
        //配置显式的多方关联的关联型
        //PropertyTakingValue
        //关联端 映射 是否延迟加载 都符合推断
        //第一个关联端 Product 此关联端在关联表PropertyTakingValue中的映射为Product的主键Code映射为ProductCode
        //第二个关联端 Property 此关联端在关联表PropertyTakingValue中的映射为Property的主键Code映射为PropertyCode
        //第三个关联端 PropertyValue 此关联端在关联表PropertyTakingValue中的映射为PropertyValue的主键Code映射为PropertyValueCode
        var explicitPropertyValue = modelBuilder.association(PropertyTakingValue.class);
        explicitPropertyValue.associationEnd(p -> p.getProduct()).hasMapping("Code", "ProductCode");
        explicitPropertyValue.associationEnd(p -> p.getProperty()).hasMapping("Code", "PropertyCode");
        explicitPropertyValue.associationEnd(p -> p.getPropertyValue()).hasMapping("Code", "PropertyValueCode");
        explicitPropertyValue.toTable("PropertyTakingValue");

        //配置隐式的多方关联的关联型
        //使用元组作为关联引用的类型 即可被解析为隐式的多方关联的关联型
        //关联端 映射 是否延迟加载 都符合推断
        //第一个关联端 Product 此关联端在关联表PropertyTakingValue中的映射为Product的主键Code映射为ProductCode
        //第二个关联端 Property 此关联端在关联表PropertyTakingValue中的映射为Property的主键Code映射为PropertyCode
        //第三个关联端 PropertyValue 此关联端在关联表PropertyTakingValue中的映射为PropertyValue的主键Code映射为PropertyValueCode
        var propertyValue = modelBuilder.association();
        //关联端定义出来即可
        propertyValue.associationEnd(io.obase.test.domain.association.multiAssociationEnd.Product.class)
                //设置关联引用延迟加载为true
                .associationReference(p -> p.getPropertyValues()).hasEnableLazyLoading(true);
        propertyValue.associationEnd(Property.class);
        propertyValue.associationEnd(PropertyValue.class);
        //只需要设置一下关联表
        propertyValue.toTable("PropertyTakingValue");

        //endregion

        //对应测试文件core.association文件夹内ImplementTest
        //region 继承关系

        //定义一个自行车实体配置
        var bikeEntity = modelBuilder.entity(Bike.class);
        bikeEntity.hasKeyAttribute(p -> p.getCode()).hasKeyIsSelfIncreased(false);
        //此处需要配置类型判别器和根据哪个数据源字段的值来判断 不再需要配置自定义的构造器
        //如果此处的具体类型判别器没有特殊逻辑 可以只传入判别字段名 使用Obase内置的判别器
        bikeEntity.hasConcreteTypeDiscriminator("Type");
        //Bike的Type字段是1 这里的类型需要根据具体的类型进行调整
        //如果此基础类型是抽象的 此处可以配置一个如-1一类的值抽象的类型不会被创建 所以配置一个特殊值即可
        bikeEntity.hasConcreteTypeSign(1);

        //定义车灯实体配置
        var bikeLightEntity = modelBuilder.entity(BikeLight.class);
        bikeLightEntity.hasKeyAttribute(p -> p.getCode()).hasKeyIsSelfIncreased(false);

        //定义车轮实体配置
        var bikeWheelEntity = modelBuilder.entity(BikeWheel.class);
        bikeWheelEntity.hasKeyAttribute(p -> p.getCode()).hasKeyIsSelfIncreased(false);

        //定义车旗实体配置
        var bikeFlagEntity = modelBuilder.entity(BikeFlag.class);
        bikeFlagEntity.hasKeyAttribute(p -> p.getCode()).hasKeyIsSelfIncreased(false);

        //定义车筐实体配置
        var bikeBucketEntity = modelBuilder.entity(BikeBucket.class);
        bikeBucketEntity.hasKeyAttribute(p -> p.getCode()).hasKeyIsSelfIncreased(false);

        //定义一个特定的我的自行车A
        var myBikeAEntity = modelBuilder.entity(MyBikeA.class);
        myBikeAEntity.hasKeyAttribute(p -> p.getCode()).hasKeyIsSelfIncreased(false);
        //设置继承关系
        myBikeAEntity.deriveFrom(Bike.class);
        //MyBikeA的Type字段是2 这里的类型需要根据具体的类型进行调整
        myBikeAEntity.hasConcreteTypeSign(2);
        //设置A和C的具体类型区分器  使用Obase内置的判别器
        myBikeAEntity.hasConcreteTypeDiscriminator("Type");
        //此处与父类一起保存于Bike
        myBikeAEntity.toTable("Bike");

        //定义一个特定的我的自行车B
        var myBikeBEntity = modelBuilder.entity(MyBikeB.class);
        myBikeBEntity.hasKeyAttribute(p -> p.getCode()).hasKeyIsSelfIncreased(false);
        //设置继承关系
        myBikeBEntity.deriveFrom(Bike.class);
        //MyBikeB的Type字段是3 这里的类型需要根据具体的类型进行调整
        myBikeBEntity.hasConcreteTypeSign(3);
        //此处与父类一起保存于Bike
        myBikeBEntity.toTable("Bike");

        //定义一个特定的我的自行车C
        var myBikeCEntity = modelBuilder.entity(MyBikeC.class);
        myBikeCEntity.hasKeyAttribute(p -> p.getCode()).hasKeyIsSelfIncreased(false);
        //设置继承关系
        myBikeCEntity.deriveFrom(MyBikeA.class);
        //MyBikeB的Type字段是4 这里的类型需要根据具体的类型进行调整
        myBikeCEntity.hasConcreteTypeSign(4);
        //此处与父类一起保存于Bike
        myBikeCEntity.toTable("Bike");

        //定义车灯的关联
        var bikeAssLight = modelBuilder.association();
        //关联端 关联映射
        var bikeEnd1 = bikeAssLight.associationEnd(Bike.class);
        //启用延迟加载
        bikeEnd1.associationReference(p -> p.getLight()).hasEnableLazyLoading(true);
        bikeEnd1.hasMapping("Code", "Code");
        bikeAssLight.associationEnd(BikeLight.class).hasMapping("Code", "LightCode");
        bikeAssLight.toTable("Bike");

        //定义车轮的关联
        var bikeAssWheel = modelBuilder.association();
        //关联端 关联映射
        var bikeEnd2 = bikeAssWheel.associationEnd(Bike.class);
        //启用延迟加载
        bikeEnd2.associationReference(p -> p.getWheels()).hasEnableLazyLoading(true);
        bikeEnd2.hasMapping("Code", "BikeCode");
        bikeAssWheel.associationEnd(BikeWheel.class).hasMapping("Code", "Code");
        bikeAssWheel.toTable("BikeWheel");

        //定义车旗的关联
        var myBikeAssFlag = modelBuilder.association();
        //关联端 关联映射
        var myBikeEnd1 = myBikeAssFlag.associationEnd(MyBikeA.class);
        myBikeEnd1.associationReference(p -> p.getFlag()).hasEnableLazyLoading(true);
        //启用延迟加载
        myBikeEnd1.hasMapping("Code", "Code");
        myBikeAssFlag.associationEnd(BikeFlag.class).hasMapping("Code", "FlagCode");
        myBikeAssFlag.toTable("Bike");

        //定义车筐的关联
        var myBikeAssBucket = modelBuilder.association();
        //关联端 关联映射
        var myBikeEnd2 = myBikeAssBucket.associationEnd(MyBikeB.class);
        myBikeEnd2.associationReference(p -> p.getBucket()).hasEnableLazyLoading(true);
        //启用延迟加载
        myBikeEnd2.hasMapping("Code", "Code");
        myBikeAssBucket.associationEnd(BikeBucket.class).hasMapping("Code", "BucketCode");
        myBikeAssBucket.toTable("Bike");

        //配置活动实体型
        var activityEntity = modelBuilder.entity(Activity.class);
        //配置主键
        activityEntity.hasKeyAttribute(p -> p.getId());

        //奖品是抽象的 判别值设置一个特殊值即可 因为不会被实际创建出来 同时实现类配置DeriveFrom即可
        //为奖品配置实体型
        var prizeEntity = modelBuilder.entity(Prize.class);
        //配置主键
        prizeEntity.hasKeyAttribute(p -> p.getId());
        // 使用Obase内置的判别器
        //实现见PrizeConcreteTypeDiscriminator中 此处类内没有定义Type Obase会其补充
        prizeEntity.hasConcreteTypeDiscriminator("Type");
        //此类型是抽象的 不会被创建 用一个特殊值即可
        prizeEntity.hasConcreteTypeSign(-1);

        //为实体奖品配置实体型
        var inKindPrizeEntity = modelBuilder.entity(InKindPrize.class);
        //配置主键
        inKindPrizeEntity.hasKeyAttribute(p -> p.getId());
        //配置为从Prize派生而来
        inKindPrizeEntity.deriveFrom(Prize.class);
        //配置一个类型判别属性的值
        inKindPrizeEntity.hasConcreteTypeSign(1);
        //都存储在Prize里
        inKindPrizeEntity.toTable("Prize");

        //为红包配置实体型
        var redEnvelopEntity = modelBuilder.entity(RedEnvelope.class);
        //配置主键
        redEnvelopEntity.hasKeyAttribute(p -> p.getId());
        //配置为从Prize派生而来
        redEnvelopEntity.deriveFrom(Prize.class);
        //配置类型判别器
        redEnvelopEntity.hasConcreteTypeDiscriminator("Type", new RedEnvelopeConcreteTypeDiscriminator(modelBuilder.getContextType()));
        //配置一个判别属性的值
        redEnvelopEntity.hasConcreteTypeSign(2);
        //都存储在Prize里
        redEnvelopEntity.toTable("Prize");

        //为幸运红包配置实体型
        var luckRedEnvelopeEntity = modelBuilder.entity(LuckyRedEnvelope.class);
        //配置主键
        luckRedEnvelopeEntity.hasKeyAttribute(p -> p.getId());
        //配置为从RedEnvelope派生而来
        luckRedEnvelopeEntity.deriveFrom(RedEnvelope.class);
        //配置一个判别属性的值
        luckRedEnvelopeEntity.hasConcreteTypeSign(3);
        //都存储在Prize里
        luckRedEnvelopeEntity.toTable("Prize");

        //配置关联型
        var activityAssPrize = modelBuilder.association();
        //配置关联端 Activity关联端为End1这个属性 在关联表中Activity的主键Id映射为ActivityId 符合推断 无需配置
        activityAssPrize.associationEnd(Activity.class);
        //配置关联端 Prize关联端为End2这个属性 在关联表中Prize的主键Id映射为Id 符合推断 无需配置
        activityAssPrize.associationEnd(Prize.class);
        //关联表是Prize
        activityAssPrize.toTable("Prize");

        //配置对话实体型
        var dialogueEntity = modelBuilder.entity(Dialogue.class);
        //配置主键
        dialogueEntity.hasKeyAttribute(p -> p.getDialogueId()).hasKeyIsSelfIncreased(true);
        //配置映射表
        dialogueEntity.toTable("Dialogue");
        //配置一个具体类型判别器 使用内置的判别器
        dialogueEntity.hasConcreteTypeDiscriminator("Type");
        //此类型是抽象的 不会被创建 用一个特殊值即可
        dialogueEntity.hasConcreteTypeSign(1);

        //配置发言实体型
        var wordsEntity = modelBuilder.entity(Words.class);
        //配置主键
        wordsEntity.hasKeyAttribute(p -> p.getWordsId()).hasKeyIsSelfIncreased(true);
        //配置映射表
        wordsEntity.toTable("Words");

        //配置客户对话实体型
        var customerDialogueEntity = modelBuilder.entity(CustomerDialogue.class);
        //配置主键
        customerDialogueEntity.hasKeyAttribute(p -> p.getDialogueId()).hasKeyIsSelfIncreased(true);
        //配置为从Dialogue派生而来
        customerDialogueEntity.deriveFrom(Dialogue.class);
        //配置一个判别属性的值
        customerDialogueEntity.hasConcreteTypeSign(2);
        //都存储在Prize里
        customerDialogueEntity.toTable("Dialogue");

        //配置对话和发言的关联型
        var dialogueAssWords = modelBuilder.association();
        //配置关联端 Dialogue关联端为End1这个属性 在关联表中Dialogue的主键DialogueId映射为DialogueId
        dialogueAssWords.associationEnd(Dialogue.class).hasMapping("DialogueId", "DialogueId");
        //配置关联端 Words关联端为End2这个属性 在关联表中Words的主键WordsId映射为WordsId
        dialogueAssWords.associationEnd(Words.class).hasMapping("WordsId", "WordsId");
        //关联表是Words
        dialogueAssWords.toTable("Words");

        //endregion

        //对应测试文件core.functional文件夹内DataErrorTest
        //region 数据错误(关联引用是一对一 但数据是一对多)

        //DataErrorStudent 实体型
        var dataErrorStudent = modelBuilder.entity(DataErrorStudent.class);
        //主键 不是类名+id 不自增
        dataErrorStudent.hasKeyAttribute(p -> p.getStudentId()).hasKeyIsSelfIncreased(false);

        //DataErrorStudentInfo实体型
        var dataErrorStudentInfo = modelBuilder.entity(DataErrorStudentInfo.class);
        //主键 不是类名+id 不自增
        dataErrorStudentInfo.hasKeyAttribute(p -> p.getStudentInfoId()).hasKeyIsSelfIncreased(false);

        //DataErrorStudent和DataErrorStudentInfo间的关系
        var dataErrorAssociation = modelBuilder.association();
        //关联端和映射
        dataErrorAssociation.associationEnd(DataErrorStudent.class).hasMapping("StudentId", "StudentId");
        dataErrorAssociation.associationEnd(DataErrorStudentInfo.class).hasMapping("StudentInfoId", "StudentInfoId");
        //根据测试需要 配置成关联表是DataErrorStudentInfo
        dataErrorAssociation.toTable("DataErrorStudentInfo");

        //endregion

        //对应测试文件core.functional文件夹内ComplexAttributeTest
        //region 复杂类型

        //国内地址 配置为实体型
        var domesticAddressConfig = modelBuilder.entity(DomesticAddress.class);
        domesticAddressConfig.hasKeyAttribute(DomesticAddress::getKey);

        //复杂类型 Java中无法推断
        //Province
        modelBuilder.complex(Province.class);
        //City
        modelBuilder.complex(City.class);
        //Region
        modelBuilder.complex(Region.class);

        //对应的属性
        domesticAddressConfig.attribute(DomesticAddress::getCity).hasMappingConnectionChar('_');
        domesticAddressConfig.attribute(DomesticAddress::getRegion).hasMappingConnectionChar('-');

        //endregion

        //对应测试文件core.functional文件夹内SimpleAttributeConcurrentConflictTest
        //region 并发策略 简单属性

        //并发策略适用于对象创建和修改时出现并发的情况
        //Obase将并发冲突分为三种 重复创建 版本冲突 更新幻影
        //重复创建 即尝试创建主键相同的对象
        //版本冲突 在配置了版本键的情况下 修改对象时版本键已被其他线程/进程修改
        //更新幻影 修改对象时对象已被其他线程/进程删除
        //要配置并发策略 需要在实体型上配置

        //配置实体型
        var ignoreConflictConfig = modelBuilder.entity(IgnoreKeyValue.class);
        //配置键属性
        ignoreConflictConfig.hasKeyAttribute(IgnoreKeyValue::getId).hasKeyIsSelfIncreased(false);
        //配置并发处理策略为 忽略
        //忽略策略 当发生并发时 不做任何处理
        ignoreConflictConfig.hasConcurrentConflictHandlingStrategy(EConcurrentConflictHandlingStrategy.Ignore);
        //配置版本键 用于检测修改时的并发冲突 对于忽略策略 可以不配置版本键
        ignoreConflictConfig.hasVersionAttribute(IgnoreKeyValue::getVersionKey);
        //配置映射表
        ignoreConflictConfig.toTable("KeyValues");

        //配置实体型
        var throwExceptionConflictConfig = modelBuilder.entity(ThrowExceptionKeyValue.class);
        //配置键属性
        throwExceptionConflictConfig.hasKeyAttribute(ThrowExceptionKeyValue::getId).hasKeyIsSelfIncreased(false);
        //配置并发处理策略为 抛出异常
        //抛出异常策略 当发生并发异常 会抛出特定的异常
        //分别是
        //NothingUpdatedException 未更新任何记录
        //RepeatInsertionException 重复插入记录
        //默认的处理策略即为抛出异常 故使用此种策略时可以不配置
        throwExceptionConflictConfig.hasConcurrentConflictHandlingStrategy(EConcurrentConflictHandlingStrategy.ThrowException);
        //配置版本键 用于检测修改时的并发冲突 对于抛出异常策略 可以不配置版本键
        throwExceptionConflictConfig.hasVersionAttribute(ThrowExceptionKeyValue::getVersionKey);
        //配置映射表
        throwExceptionConflictConfig.toTable("KeyValues");

        //配置实体型
        var overWriteConflictConfig = modelBuilder.entity(OverwriteKeyValue.class);
        //配置键属性
        overWriteConflictConfig.hasKeyAttribute(OverwriteKeyValue::getId).hasKeyIsSelfIncreased(false);
        //配置并发处理策略为 强制覆盖
        //强制覆盖策略可以处理重复创建 和 版本冲突 两种并发情况
        //强制覆盖策略 当发生并发时 用当前对象覆盖原有对象
        overWriteConflictConfig.hasConcurrentConflictHandlingStrategy(EConcurrentConflictHandlingStrategy
                .Overwrite);
        //配置版本键 用于检测修改时的并发冲突 要想处理版本冲突并发 必须配置版本键
        //版本键可以配置多个
        //可以使用会发生并发冲突的属性 或者使用 时间戳标识最后的修改时间 来作为版本键
        //能区分对象最后被谁修改的属性都可以作为版本键
        overWriteConflictConfig.hasVersionAttribute(OverwriteKeyValue::getVersionKey);
        //配置映射表
        overWriteConflictConfig.toTable("KeyValues");

        //配置实体型
        var reconstructConflictConfig = modelBuilder.entity(ReconstructKeyValue.class);
        //配置键属性
        reconstructConflictConfig.hasKeyAttribute(ReconstructKeyValue::getId).hasKeyIsSelfIncreased(false);
        //配置并发处理策略为 重建对象
        //强制覆盖策略可以处理更新幻影 这种并发情况
        //强制覆盖策略 当发生异常时 将当前对象做为新对象进行创建
        reconstructConflictConfig.hasConcurrentConflictHandlingStrategy(EConcurrentConflictHandlingStrategy.Reconstruct);
        //配置版本键 用于检测修改时的并发冲突 对于重建对象策略 可以不配置版本键
        reconstructConflictConfig.hasVersionAttribute(ReconstructKeyValue::getVersionKey);
        //配置映射表
        reconstructConflictConfig.toTable("KeyValues");

        //配置实体型
        var accumulateCombineConfig = modelBuilder.entity(AccumulateCombineKeyValue.class);
        //配置键属性
        accumulateCombineConfig.hasKeyAttribute(AccumulateCombineKeyValue::getId).hasKeyIsSelfIncreased(false);
        //配置并发处理策略为 版本合并
        //版本合并策略可以处理重复创建和版本冲突 这两种并发情况
        //版本合并策略 当发生异常时 将当前对象与旧对象的属性进行合并
        accumulateCombineConfig.hasConcurrentConflictHandlingStrategy(EConcurrentConflictHandlingStrategy.Combine);
        //配置版本键 用于检测修改时的并发冲突 要想处理版本冲突并发 必须配置版本键
        //版本键可以配置多个
        //可以使用会发生并发冲突的属性 或者使用 时间戳标识最后的修改时间 来作为版本键
        //能区分对象最后被谁修改的属性都可以作为版本键
        accumulateCombineConfig.hasVersionAttribute(AccumulateCombineKeyValue::getVersionKey);
        //配置映射表
        accumulateCombineConfig.toTable("KeyValues");
        //配置要进行合并的属性的合并策略
        accumulateCombineConfig.attribute(AccumulateCombineKeyValue::getValue)
                //设置为累加 即将当前版本中属性值的增量累加到对方版本 只支持数值型的属性
                .hasCombinationHandler(EAttributeCombinationHandlingStrategy.Accumulate);

        //配置实体型
        var ignoreCombineConfig = modelBuilder.entity(IgnoreCombineKeyValue.class);
        //配置键属性
        ignoreCombineConfig.hasKeyAttribute(IgnoreCombineKeyValue::getId).hasKeyIsSelfIncreased(false);
        //配置并发处理策略为 版本合并
        //版本合并策略可以处理重复创建和版本冲突 这两种并发情况
        //版本合并策略 当发生异常时 将当前对象与旧对象的属性进行合并
        ignoreCombineConfig.hasConcurrentConflictHandlingStrategy(EConcurrentConflictHandlingStrategy.Combine);
        //配置版本键 用于检测修改时的并发冲突 要想处理版本冲突并发 必须配置版本键
        //版本键可以配置多个
        //可以使用会发生并发冲突的属性 或者使用 时间戳标识最后的修改时间 来作为版本键
        //能区分对象最后被谁修改的属性都可以作为版本键
        ignoreCombineConfig.hasVersionAttribute(IgnoreCombineKeyValue::getVersionKey);
        //配置映射表
        ignoreCombineConfig.toTable("KeyValues");
        //配置要进行合并的属性的合并策略
        ignoreCombineConfig.attribute(IgnoreCombineKeyValue::getValue)
                //设置为忽略 即使用旧对象(即原有对象)的值
                .hasCombinationHandler(EAttributeCombinationHandlingStrategy.Ignore);

        //配置实体型
        var overwriteCombineConfig = modelBuilder.entity(OverwriteCombineKeyValue.class);
        //配置键属性
        overwriteCombineConfig.hasKeyAttribute(OverwriteCombineKeyValue::getId).hasKeyIsSelfIncreased(false);
        //配置并发处理策略为 版本合并
        //版本合并策略可以处理重复创建和版本冲突 这两种并发情况
        //版本合并策略 当发生异常时 将当前对象与旧对象的属性进行合并
        overwriteCombineConfig.hasConcurrentConflictHandlingStrategy(EConcurrentConflictHandlingStrategy.Combine);
        //配置版本键 用于检测修改时的并发冲突 要想处理版本冲突并发 必须配置版本键
        //版本键可以配置多个
        //可以使用会发生并发冲突的属性 或者使用 时间戳标识最后的修改时间 来作为版本键
        //能区分对象最后被谁修改的属性都可以作为版本键
        overwriteCombineConfig.hasVersionAttribute(OverwriteCombineKeyValue::getVersionKey);
        //配置映射表
        overwriteCombineConfig.toTable("KeyValues");
        //配置要进行合并的属性的合并策略
        overwriteCombineConfig.attribute(OverwriteCombineKeyValue::getValue)
                //设置为覆盖 即使用新对象(即当前)的值 此种策略为默认的策略 可以不配置
                .hasCombinationHandler(EAttributeCombinationHandlingStrategy.Overwrite);

        //endregion

        //对应测试文件core.functional文件夹内ComplexAttributeConcurrentConflictTest
        //region 并发策略 复杂类型属性

        //复杂类型属性的并发策略与简单属性的并发策略基本相同
        //唯一不同的是使用版本合并策略时 具体的属性合并策略需要配置在复杂类型的属性上

        //复杂类型ComplexKeyValue Java无法推断
        //ComplexKeyValue
        modelBuilder.complex(ComplexKeyValue.class);

        //配置实体型
        var complexIgnoreConflictConfig = modelBuilder.entity(ComplexIgnoreKeyValue.class);
        //配置键属性
        complexIgnoreConflictConfig.hasKeyAttribute(ComplexIgnoreKeyValue::getId).hasKeyIsSelfIncreased(false);
        //配置并发处理策略为 忽略
        //忽略策略 当发生并发时 不做任何处理
        //使用忽略策略时 不需要对复杂类型属性进行配置
        complexIgnoreConflictConfig.hasConcurrentConflictHandlingStrategy(EConcurrentConflictHandlingStrategy.Ignore);
        //配置版本键 用于检测修改时的并发冲突 对于忽略策略 可以不配置版本键
        complexIgnoreConflictConfig.hasVersionAttribute(ComplexIgnoreKeyValue::getVersionKey);
        //配置映射表
        complexIgnoreConflictConfig.toTable("KeyValues");

        //配置实体型
        var complexThrowExceptionConflictConfig = modelBuilder.entity(ComplexThrowExceptionKeyValue.class);
        //配置键属性
        complexThrowExceptionConflictConfig.hasKeyAttribute(ComplexThrowExceptionKeyValue::getId).hasKeyIsSelfIncreased(false);
        //配置并发处理策略为 抛出异常
        //抛出异常策略 当发生并发异常 会抛出特定的异常
        //分别是
        //NothingUpdatedException 未更新任何记录
        //RepeatInsertionException 重复插入记录
        //默认的处理策略即为抛出异常 故使用此种策略时可以不配置
        //使用抛出异常策略时 不需要对复杂类型属性进行配置
        complexThrowExceptionConflictConfig.hasConcurrentConflictHandlingStrategy(EConcurrentConflictHandlingStrategy.ThrowException);
        //配置版本键 用于检测修改时的并发冲突 对于抛出异常策略 可以不配置版本键
        complexThrowExceptionConflictConfig.hasVersionAttribute(ComplexThrowExceptionKeyValue::getVersionKey);
        //配置映射表
        complexThrowExceptionConflictConfig.toTable("KeyValues");

        //配置实体型
        var complexOverWriteConflictConfig = modelBuilder.entity(ComplexOverwriteKeyValue.class);
        //配置键属性
        complexOverWriteConflictConfig.hasKeyAttribute(ComplexOverwriteKeyValue::getId).hasKeyIsSelfIncreased(false);
        //配置并发处理策略为 强制覆盖
        //强制覆盖策略可以处理重复创建 和 版本冲突 两种并发情况
        //强制覆盖策略 当发生并发时 用当前对象覆盖原有对象
        complexOverWriteConflictConfig.hasConcurrentConflictHandlingStrategy(EConcurrentConflictHandlingStrategy.Overwrite);
        //配置并发处理策略为 强制覆盖
        //强制覆盖策略可以处理重复创建 和 版本冲突 两种并发情况
        //强制覆盖策略 当发生并发时 用当前对象覆盖原有对象
        complexOverWriteConflictConfig.hasVersionAttribute(ComplexOverwriteKeyValue::getVersionKey);
        //配置映射表
        complexOverWriteConflictConfig.toTable("KeyValues");

        //配置实体型
        var complexReconstructConflictConfig = modelBuilder.entity(ComplexReconstructKeyValue.class);
        //配置键属性
        complexReconstructConflictConfig.hasKeyAttribute(ComplexReconstructKeyValue::getId).hasKeyIsSelfIncreased(false);
        //配置并发处理策略为 重建对象
        //强制覆盖策略可以处理更新幻影 这种并发情况
        //强制覆盖策略 当发生异常时 将当前对象做为新对象进行创建
        complexReconstructConflictConfig.hasConcurrentConflictHandlingStrategy(EConcurrentConflictHandlingStrategy.Reconstruct);
        //配置版本键 用于检测修改时的并发冲突 对于重建对象策略 可以不配置版本键
        complexReconstructConflictConfig.hasVersionAttribute(ComplexReconstructKeyValue::getVersionKey);
        //配置映射表
        complexReconstructConflictConfig.toTable("KeyValues");

        //配置累加合并策略复杂类型
        var accumulateComplexCombineConfig = modelBuilder.complex(AccumulateCombineComplexKeyValue.class);
        //配置具体要合并的属性
        accumulateComplexCombineConfig.attribute("Value", int.class).hasValueGetter(AccumulateCombineComplexKeyValue::getValue).hasValueSetter(AccumulateCombineComplexKeyValue::setValue)
                //设置为累加 即将当前版本中属性值的增量累加到对方版本 只支持数值型的属性
                .hasCombinationHandler(EAttributeCombinationHandlingStrategy.Accumulate);

        //配置实体型
        var complexAccumulateCombineConfig = modelBuilder.entity(ComplexAccumulateCombineKeyValue.class);
        //配置键属性
        complexAccumulateCombineConfig.hasKeyAttribute(ComplexAccumulateCombineKeyValue::getId).hasKeyIsSelfIncreased(false);
        //配置并发处理策略为 版本合并
        //版本合并策略可以处理重复创建和版本冲突 这两种并发情况
        //版本合并策略 当发生异常时 将当前对象与旧对象的属性进行合并
        complexAccumulateCombineConfig.hasConcurrentConflictHandlingStrategy(EConcurrentConflictHandlingStrategy.Combine);
        //配置版本键 用于检测修改时的并发冲突 要想处理版本冲突并发 必须配置版本键
        //版本键可以配置多个
        //可以使用会发生并发冲突的属性 或者使用 时间戳标识最后的修改时间 来作为版本键
        //能区分对象最后被谁修改的属性都可以作为版本键
        complexAccumulateCombineConfig.hasVersionAttribute(ComplexAccumulateCombineKeyValue::getVersionKey);
        //配置映射表
        complexAccumulateCombineConfig.toTable("KeyValues");

        //配置忽略合并策略复杂类型
        var ignoreComplexCombineConfig = modelBuilder.complex(IgnoreCombineComplexKeyValue.class);
        //配置具体要合并的属性
        ignoreComplexCombineConfig.attribute("Value", int.class).hasValueGetter(IgnoreCombineComplexKeyValue::getValue).hasValueSetter(IgnoreCombineComplexKeyValue::setValue)
                .hasCombinationHandler(EAttributeCombinationHandlingStrategy.Ignore);

        //配置实体型
        var complexIgnoreCombineConfig = modelBuilder.entity(ComplexIgnoreCombineKeyValue.class);
        //配置键属性
        complexIgnoreCombineConfig.hasKeyAttribute(ComplexIgnoreCombineKeyValue::getId).hasKeyIsSelfIncreased(false);
        //配置并发处理策略为 版本合并
        //版本合并策略可以处理重复创建和版本冲突 这两种并发情况
        //版本合并策略 当发生异常时 将当前对象与旧对象的属性进行合并
        complexIgnoreCombineConfig.hasConcurrentConflictHandlingStrategy(EConcurrentConflictHandlingStrategy.Combine);
        //配置版本键 用于检测修改时的并发冲突 要想处理版本冲突并发 必须配置版本键
        //版本键可以配置多个
        //可以使用会发生并发冲突的属性 或者使用 时间戳标识最后的修改时间 来作为版本键
        //能区分对象最后被谁修改的属性都可以作为版本键
        complexIgnoreCombineConfig.hasVersionAttribute(ComplexIgnoreCombineKeyValue::getVersionKey);
        //配置映射表
        complexIgnoreCombineConfig.toTable("KeyValues");

        //配置覆盖合并策略复杂类型
        var overWriteComplexCombineConfiguration = modelBuilder.complex(OverWriteCombineComplexKeyValue.class);
        //配置具体要合并的属性
        overWriteComplexCombineConfiguration.attribute("Value", int.class).hasValueGetter(OverWriteCombineComplexKeyValue::getValue).hasValueSetter(OverWriteCombineComplexKeyValue::setValue)
                //设置为覆盖 即使用新对象(即当前)的值 此种策略为默认的策略 可以不配置
                .hasCombinationHandler(EAttributeCombinationHandlingStrategy.Overwrite);

        //配置实体型
        var complexOverWriteCombineConfig = modelBuilder.entity(ComplexOverwriteCombineKeyValue.class);
        //配置键属性
        complexOverWriteCombineConfig.hasKeyAttribute(ComplexOverwriteCombineKeyValue::getId).hasKeyIsSelfIncreased(false);
        //配置并发处理策略为 版本合并
        //版本合并策略可以处理重复创建和版本冲突 这两种并发情况
        //版本合并策略 当发生异常时 将当前对象与旧对象的属性进行合并
        complexOverWriteCombineConfig.hasConcurrentConflictHandlingStrategy(EConcurrentConflictHandlingStrategy.Combine);
        //配置版本键 用于检测修改时的并发冲突 要想处理版本冲突并发 必须配置版本键
        //版本键可以配置多个
        //可以使用会发生并发冲突的属性 或者使用 时间戳标识最后的修改时间 来作为版本键
        //能区分对象最后被谁修改的属性都可以作为版本键
        complexOverWriteCombineConfig.hasVersionAttribute(ComplexOverwriteCombineKeyValue::getVersionKey);
        //配置映射表
        complexOverWriteCombineConfig.toTable("KeyValues");

        //endregion

        //对应测试文件core.functional文件夹内EntityNoticeTest
        //region 实体通知

        //配置实体型NoticeStudentInfo 不符合推断
        var noticeEntityConfig = modelBuilder.entity(NoticeStudentInfo.class);
        //配置主键
        noticeEntityConfig.hasKeyAttribute(p -> p.getStudentId()).hasKeyIsSelfIncreased(false);

        //配置要进行通知的属性 这些属性即此实体型的属性 当发生特定的行为时 这些属性的值会包含在通知消息内
        //noticeEntityConfig.HasNoticeAttributes(new List<string> { "Description", "Background" });
        //无参的方法则表示通知所有的属性
        noticeEntityConfig.hasNoticeAttributes();
        //指示是否在对象被创建时进行通知
        noticeEntityConfig.hasNotifyCreation(true);
        //指示是否在对象被删除时进行通知
        noticeEntityConfig.hasNotifyDeletion(true);
        //指示是否在对象被修改时进行通知
        noticeEntityConfig.hasNotifyUpdate(true);

        //endregion

        //对应测试文件core.functional文件夹内DuplicateMappingTest
        //region 重复映射(独立映射表中关联端映射字段有重复)

        //配置GoodsAttribute实体型
        var goodsAttributeEntity = modelBuilder.entity(GoodsAttribute.class);
        goodsAttributeEntity.hasKeyAttribute(p -> p.getAttributeId()).hasKeyAttribute(p -> p.getGoodsId())
                .hasKeyIsSelfIncreased(false);
        goodsAttributeEntity.toTable("GoodsAttributes");

        //配置SelectableValue实体型
        var selectableValueEntity = modelBuilder.entity(SelectableValue.class);
        selectableValueEntity.hasKeyAttribute(p -> p.getCategoryId()).hasKeyAttribute(p -> p.getAttributeId())
                .hasKeyIsSelfIncreased(false);
        selectableValueEntity.toTable("SelectableValues");

        //配置StandardValue关联型
        var standardValueAssociation = modelBuilder.association(StandardValue.class);
        standardValueAssociation.associationEnd(p -> p.getGoodsAttribute()).hasMapping("AttributeId", "AttributeId")
                .hasMapping("GoodsId", "GoodsId");
        standardValueAssociation.associationEnd(p -> p.getSelectedValue()).hasMapping("CategoryId", "CategoryId")
                .hasMapping("AttributeId", "AttributeId");
        standardValueAssociation.toTable("StandardValue");

        //endregion
    }

    /**
     * 构造ProductCategory
     *
     * @param p                 产品
     * @param productCategories 产品分类
     * @param category          分类
     */
    private static void generateProductCategory(Product p, List<ProductCategory> productCategories, Category category) {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setCategory(category);
        productCategory.setCategoryId(category.getCategoryId());
        productCategory.setProduct(p);
        productCategory.setProductCode(p.getCode());
        productCategory.setCategoryName(category.getName());
        productCategories.add(productCategory);
    }
}
