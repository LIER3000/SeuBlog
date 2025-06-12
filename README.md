# SeuBlog
SEUBlog 是一个基于 Spring Boot + MyBatis + Shiro + Vue 的前后端分离博客系统，提供了用户注册登录、文章发布与管理、评论系统、标签分类、后台管理等功能模块。本报告旨在提供对该项目的需求分析、系统设计、程序设计、功能测试的全面说明。
<br>项目使用腾讯云服务器，技术栈为spring boot和nodejs，将打包好的jar文件通过filezilla运至服务器运行，在服务器的mysql数据库导入测试数据即可通过网站[http://124.222.17.7:8081/index.html](http://124.222.17.7:8081/index.html)访问SeuBlog。
注意：GitHub链接中提供的是项目本地运行版本。如需访问云端部署版本，请直接访问上述网址。
## 注意
>GitHub中提供的是项目本地运行版本。如需访问云端部署版本，请直接访问上述网址。

若需要本地运行，将sql文件导入本地数据库，并找到application.properties，配置数据库，设置成本地数据库的用户和密码。
