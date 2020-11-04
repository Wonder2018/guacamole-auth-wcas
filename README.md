# Guacamole Auth WCAS

本项目是 Apache Guacamole（以下简称 Guacamole） 的一个插件，可以让 Guacamole 接入 Apereo CAS 完成认证，并从数据库中读取用户连接。项目扩展了 Guacamole 官方插件 [guacamole-auth-cas](https://github.com/apache/guacamole-client/tree/master/extensions/guacamole-auth-cas)。使用 Apache Maven （以下简称 mvn）管理相关依赖和编译，Java 版本为 1.8。

---

## 使用方法

### 1. 安装插件

1. 将下载到的插件复制到 **\$GUACAMOLE_HOME/extensions/**
2. 由于本项目是 [guacamole-auth-cas](https://github.com/apache/guacamole-client/tree/master/extensions/guacamole-auth-cas) 的扩展，还需要从 [Guacamole 发行网站](http://guacamole.apache.org/releases/) 下载 `guacamole-auth-cas` 并将文件名为 `guacamole-auth-cas-${version}.jar` 的文件复制到 **\$GUACAMOLE_HOME/lib/** 以及 **\$GUACAMOLE_HOME/extensions/**
3. 将数据库驱动复制到 **\$GUACAMOLE_HOME/lib/**

### 2. 配置插件

安装完插件后需要进行一些简单的配置才能开始使用。这些配置都记录在 **\$GUACAMOLE_HOME/guacamole.properties** 中。

首先需要配置的属性是 `auth-provider`，它指定了 guacmole 采用的认证方式，这里应该设置为 **top.imwonder.guacamole.auth.WCASAuthenticationProvider**，接下来是一些插件需要的属性。这里以表格的形式展示出来，它们之中许多都是必须要配置的，也有一些不是必须的。

下面是 官方插件 `guacamole-auth-cas` 提供的配置：

| 属性名                     | 描述                 | 必须属性 | 备注                                                               |
| -------------------------- | :------------------- | :------: | :----------------------------------------------------------------- |
| cas-authorization-endpoint | Cas 服务器的路径     |    是    |                                                                    |
| cas-redirect-uri           | 认证成功后返回的路径 |    是    | 一般设置为 guacamole 首页的地址，如 http://hostname:8080/guacamole |
| cas-clearpass-key          | 数据库连接的密码     |    否    | CAS ClearPass 功能中私钥的位置                                     |

下面的表格记录了本插件提供的配置：

| 属性名                           | 必须属性 |        默认值        | 描述                                   | 备注                                                                 |
| :------------------------------- | :------: | :------------------: | :------------------------------------- | :------------------------------------------------------------------- |
| wcas-database-url                |    是    |          无          | 数据库连接的 Url                       |                                                                      |
| wcas-database-username           |    是    |          无          | 数据库连接的用户名                     |                                                                      |
| wcas-database-password           |    是    |          无          | 数据库连接的密码                       |                                                                      |
| wcas-database-driver-class-name  |    是    |          无          | 数据库驱动的完整类名                   |                                                                      |
| wcas-database-prefix             |    否    |          无          | 数据库中表和字段名的前缀               |                                                                      |
| wcas-database-column-id          |    否    |          无          | 数据表字段名：连接 id                  | 一个用户有多个连接可用时会显示在标签属性上，未设置则为当前记录的行数 |
| wcas-database-column-username    |    否    |       username       | 数据表字段名：用户名                   | 应该与 cas 的用户名一致，用于区分连接属于哪个用户                    |
| wcas-database-column-protocol    |    否    |       protocol       | 数据表字段名：连接协议                 | 远程桌面使用的协议，                                                 |
| wcas-database-column-hostname    |    否    |       hostname       | 数据表字段名：远程桌面服务的 ip 或域名 |                                                                      |
| wcas-database-column-port        |    否    |         port         | 数据表字段名：远程桌面服务的端口号     |                                                                      |
| wcas-database-column-password    |    否    |       password       | 数据表字段名：远程桌面服务的密码       |                                                                      |
| wcas-database-column-description |    否    |          无          | 数据表字段名：描述                     | 一个用户有多个连接可用时会显示在标签属性上，用于对连接进行简短介绍   |
| wcas-database-table-name         |    否    | user_connection_info | 数据表名                               | 用于指定插件使用的数据表                                             |

> 1. 如果你没有在环境变量中配置 `$GUACAMOLE_HOME`, 默认的 `$GUACAMOLE_HOME` 为 `/etc/guacamole`。更多详情请参照官方 Doc：[Chapter 5. Configuring Guacamole](https://guacamole.apache.org/doc/gug/configuring-guacamole.html#initial-setup)
> 2. 配置 `wcas-database-prefix` 属性后，表名和字段名之前会自动带上配置的前缀。如果需要只在表名前加前缀或只在字段名前加前缀请直接设置完整的表名和字段名，去除 `wcas-database-prefix` 属性。

### 3. 准备数据库

本插件使用 JDBC 驱动连接到数据库，理论上可兼容所有提供了 JDBC 驱动程序的数据库。

下表记录了插件需要的字段：

表名：user_connection_info

| 字段        | 类型    | 必须字段 | 要求非空 | 描述                                 |
| ----------- | :------ | :------: | :------- | ------------------------------------ |
| id          | varchar |    否    | 否       | 连接 id，用于区分同一用户的多个连接  |
| username    | varchar |    是    | 是       | cas 用户名                           |
| protocol    | varchar |    是    | 是       | 远程协议                             |
| hostname    | varchar |    是    | 是       | 远程桌面 ip 或域名                   |
| port        | varchar |    是    | 是       | 远程桌面端口                         |
| password    | varchar |    是    | 否       | 远程密码                             |
| description | varchar |    否    | 否       | 连接描述，用于区分同一用户的多个连接 |

> 以上表名和字段名均为缺省值，实际表名和字段名可在配置文件中指定。如需在字段名前添加前缀，可在配置文件中使用 `wcas-database-prefix` 属性。
> 如需使用id和description字段，分别需要在配置文件中配置 `wcas-database-column-id` 和 `wcas-database-column-description` 属性

完成以上配置后即可重启应用容器完成配置。

---

## 自行编译

本项目使用 mvn 管理依赖和编译，所用 Java 版本为 1.8，mvn 版本为 3.6.1，如果按照以下步骤无法完成正常编译请检查 Java 版本和 mvn 版本以及网络环境。

### 1. 安装 guacamole-auth-cas

由于 `guacamole-auth-cas`，并的发行版并未上传至中央仓库，这里需要手动安装，步骤如下。

```bash
# 1. 克隆 guacamole-client 获取 guacamole-auth-cas 源码
git clone https://github.com/apache/guacamole-client.git
# 2. 安装 guacamole-auth-cas
cd guacamole-client/extensions/guacamole-auth-cas
mvn install
```

### 2. 编译

```bash
# 1. 克隆仓库
git clone https://github.com/Wonder2018/guacamole-auth-wcas.git
# 2. 编译
cd guacamole-auth-wcas
mvn package
```

如果顺利，编译得到的 `guacamole-auth-wcas-${version}.jar` 文件就会出现在 ./target 目录中 接下来的步骤就和正常安装一样了。
