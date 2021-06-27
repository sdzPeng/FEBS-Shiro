### 编辑器记得安装lombok插件

### BS运维 2.0
![https://img.shields.io/badge/license-Apache%202.0-blue.svg?longCache=true&style=flat-square](https://img.shields.io/badge/license-Apache%202.0-blue.svg?longCache=true&style=flat-square)
![https://img.shields.io/badge/springboot-2.2.1-yellow.svg?style=flat-square](https://img.shields.io/badge/springboot-2.2.1-yellow.svg?style=flat-square)
![https://img.shields.io/badge/shiro-1.4.2-orange.svg?longCache=true&style=flat-square](https://img.shields.io/badge/shiro-1.4.2-orange.svg?longCache=true&style=flat-square)
![https://img.shields.io/badge/layui-2.5.5-brightgreen.svg?longCache=true&style=flat-square](https://img.shields.io/badge/layui-2.5.5-brightgreen.svg?longCache=true&style=flat-square)

### 演示地址

[http://47.96.126.248:8081/login](http://47.96.126.248:8081/login)

演示环境账号密码：

账号 | 密码| 权限
---|---|---
yanglong | 1234qwer | 注册账户，拥有查看，新增权限（新增用户除外）和导出Excel权限


本地部署账号密码：

账号 | 密码| 权限
---|---|---
zhangyp | passw0rd |超级管理员，拥有所有增删改查权限
yanglong | 1234qwer | 注册账户，拥有查看，新增权限（新增用户除外）和导出Excel权限

### 系统模块
系统功能模块组成如下所示：
```
├─系统管理
│  ├─用户管理
│  ├─角色管理
│  ├─菜单管理
│  └─部门管理
└─系统监控
   ├─在线用户
   ├─系统日志
   ├─登录日志
   ├─请求追踪
   └─系统信息
      ├─JVM信息
      ├─TOMCAT信息
      └─服务器信息

```
### 系统特点

1. 前后端请求参数校验

2. 支持Excel导入导出

3. 前端页面布局多样化，主题多样化

4. 支持多数据源，代码生成

5. 多Tab页面，适合企业应用

6. 用户权限动态刷新

7. 浏览器兼容性好，页面支持PC，Pad和移动端。

8. 代码简单，结构清晰

### 技术选型

#### 后端
- [Spring Boot 2.2.1](http://spring.io/projects/spring-boot/)
- [Mybatis-Plus](https://mp.baomidou.com/guide/)
- [MySQL 5.7.x](https://dev.mysql.com/downloads/mysql/5.7.html#downloads),[Hikari](https://brettwooldridge.github.io/HikariCP/),[Redis](https://redis.io/)
- [Shiro 1.4.2](http://shiro.apache.org/)
- [easyExcel](https://www.yuque.com/easyexcel)
- [mongo](https://docs.mongodb.com/)

#### 前端
- [Layui 2.5.5](https://www.layui.com/)
- [Nepadmin](https://gitee.com/june000/nep-admin)
- [formSelects 4.x 多选框](https://hnzzmsf.github.io/example/example_v4.html)
- [eleTree 树组件](https://layuiextend.hsianglee.cn/eletree/)
- [formSelect.js树形下拉](https://wujiawei0926.gitee.io/treeselect/docs/doc.html)
- [Apexcharts图表](https://apexcharts.com/)

### 系统截图

#### PC端
![screenshot](screenshot/pc_screenshot_1.jpg)