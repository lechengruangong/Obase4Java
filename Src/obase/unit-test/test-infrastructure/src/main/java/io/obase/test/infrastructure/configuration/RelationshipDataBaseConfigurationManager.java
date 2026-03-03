package io.obase.test.infrastructure.configuration;

import com.alibaba.fastjson2.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 关系型数据库配置管理者
 * 会从Obase.Test.Config.json文件内读取配置
 */
public class RelationshipDataBaseConfigurationManager {

    /**
     * 配置文件的字典结构
     */
    private static final Map<String, Object> configMap;

    /*
      初始化关系型数据库配置管理者
     */
    static {
        //获取类加载器
        ClassLoader classLoader = RelationshipDataBaseConfigurationManager.class.getClassLoader();

        // 读取资源文件 Obase.Test.Config.json
        // 此文件不包含在代码里 请参考Obase.Test.Config.example.json格式 放置实际使用的配置文件
        // 示例文件位于uni-test模块下的test子模块内resources文件夹下
        try (var inputStream = classLoader.getResourceAsStream("Obase.Test.Config.json")) {
            if (inputStream == null) {
                throw new RuntimeException("配置文件Obase.Test.Config.json未找到,请参考Obase.Test.Config.example.json格式放置实际使用的配置文件.");
            }
            //读取所有的内容
            var content = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            //用Fastjson2解析为Map
            configMap = JSON.parseObject(content);

        } catch (IOException e) {
            throw new RuntimeException("读取配置文件Obase.Test.Config.json出错.", e);
        }
    }

    /**
     * 获取MySql用户名配置
     *
     * @return MySql用户名配置
     */
    public static String getMySqlUserName() {
        return configMap.get("MySqlUserName").toString();
    }

    /**
     * 获取MySql密码配置
     *
     * @return MySql密码配置
     */
    public static String getMySqlPassWord() {
        return configMap.get("MySqlPassWord").toString();
    }

    /**
     * 获取MySql连接字符串的配置
     *
     * @return MySql连接字符串的配置
     */
    public static String getMySqlConnectionString() {
        return configMap.get("MySqlConnectionString").toString();
    }

    /**
     * 获取Sqlite连接字符串的配置
     *
     * @return Sqlite连接字符串的配置
     */
    public static String getSqliteConnectionString() {
        return configMap.get("SqliteConnectionString").toString();
    }

    /**
     * 获取SqlServer用户名配置
     *
     * @return SqlServer用户名配置
     */
    public static String getSqlServerUserName() {
        return configMap.get("SqlServerUserName").toString();
    }

    /**
     * 获取SqlServer密码配置
     *
     * @return SqlServer密码配置
     */
    public static String getSqlServerPassWord() {
        return configMap.get("SqlServerPassWord").toString();
    }

    /**
     * 获取SqlServer连接字符串的配置
     *
     * @return SqlServer连接字符串的配置
     */
    public static String getSqlServerConnectionString() {
        return configMap.get("SqlServerConnectionString").toString();
    }

    /**
     * 获取PostgreSql的用户名配置
     *
     * @return PostgreSql的用户名配置
     */
    public static String getPostgreSqlUserName() {
        return configMap.get("PostgreSqlUserName").toString();
    }

    /**
     * 获取PostgreSql密码配置
     *
     * @return PostgreSql密码配置
     */
    public static String getPostgreSqlPassWord() {
        return configMap.get("PostgreSqlPassWord").toString();
    }

    /**
     * 获取PostgreSql连接字符串的配置
     *
     * @return PostgreSql连接字符串的配置
     */
    public static String getPostgreSqlConnectionString() {
        return configMap.get("PostgreSqlConnectionString").toString();
    }

    /**
     * 是否需要结构映射的配置
     *
     * @return 是否需要结构映射的配置
     */
    public static Boolean getNeedStructMapping() {
        if (!configMap.containsKey("NeedStructMapping"))
            return null;
        return Boolean.parseBoolean(configMap.get("NeedStructMapping").toString());
    }
}
