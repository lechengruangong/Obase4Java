# Obase4Java

本仓库为Obase在Java平台上的实现.

# 如何安装

本项目发布于Maven,共有以下构件包.

| 包名                          | 地址                                                                                                                                                                   | 简介                                            |
|-----------------------------| -------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------- |
| io.obase:core               | [![Maven central](https://maven-badges.herokuapp.com/maven-central/io.obase/core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.obase/core)         | Obase存储抽象层框架中间件（Java版).             |
| io.obase:logic.deletion     | [![Maven central](https://maven-badges.herokuapp.com/maven-central/io.obase/logical.deletion/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.obase/logical.deletion) | Obase存储抽象层框架中间件逻辑删除扩展.          |
| io.obase:multi.tenant       | [![Maven central](https://maven-badges.herokuapp.com/maven-central/io.obase/multi.tenant/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.obase/multi.tenant) | Obase存储抽象层框架中间件多租户扩展.            |
| io.obase:odm.annotation     | [![Maven central](https://maven-badges.herokuapp.com/maven-central/io.obase/odm.annotation/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.obase/odm.annotation ) | Obase存储抽象层框架中间件标注建模扩展.          |
| io.obase:providers.sql      | [![Maven central](https://maven-badges.herokuapp.com/maven-central/io.obase/providers.sql/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.obase/providers.sql ) | 适用于SQL数据库的Obase存储提供程序中间件.       |
| io.obase:providers.mysql    | [![Maven central](https://maven-badges.herokuapp.com/maven-central/io.obase/providers.mysql/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.obase/providers.mysql) | 适用于MySql数据库的Obase存储提供程序中间件.     |
| io.oabse:providers.oracle   | [![Maven central](https://maven-badges.herokuapp.com/maven-central/io.obase/providers.oracle/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.obase/providers.oracle) | 适用于Oracle数据库的Obase存储提供程序中间件.    |
| io.obase:providers.sqlite   | [![Maven central](https://maven-badges.herokuapp.com/maven-central/io.obase/providers-sqlite/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.obase/providers.sqlite) | 适用于Sqlite数据库的Obase存储提供程序中间件.    |
| io.obase:providers.sqlserver | [![Maven central](https://maven-badges.herokuapp.com/maven-central/io.obase/providers.sqlserver/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.obase/providers.sqlserver) | 适用于SqlServer数据库的Obase存储提供程序中间件. |

# 如何使用

请参考[ObaseDoc](https://github.com/lechengruangong/ObaseDoc)项目.

# 引用的第三方软件包

本项目使用了以下第三方软件包:

### io.obase:core

- org.ow2.asm:asm-9.4.0
- net.bytebuddy:byte-buddy-1.14.2
- org.jinq:analysis-2.0.3
- org.apache.commons:commons-lang3-3.18.0

### io.obase:providers.sql

- com.zaxxer:HikariCP-4.0.3

### io.obase:providers.mysql

- com.mysql:mysql-connector-j-8.3.0

### io.oabse:providers.oracle

- com.oracle.database.jdbc:ojdbc8-23.2.0.0

### io.obase:providers.sqlserver

- org.xerial:sqlite-jdbc-3.43.2.1

### io.obase:providers-sqlserver

- com.microsoft.sqlserver:mssql-jdbc-12.2.0.jre8

# 如何提出问题和需求

欢迎向我们提出Issue来协助我们改进,对应语言版本发现的问题请提交到对应的仓库.
