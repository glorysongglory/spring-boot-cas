# spring-boot-cas

## 运行前需要做的事
```
１. 要Cas-Server端的项目能运行起来，需要下载[cas-server-webapp-tomcat-5.2.1.war](https://oss.sonatype.org/content/repositories/releases/org/apereo/cas/)到server项目的src/webapp/WEB-INF/lib目录下

2. 需要运行server项目下的./build.sh gencert 命令来生成https证书
```

## cas-server-rest 


CAS之5.2x版本之REST验证ticket

```
1. 可运行　cas-client-springboot项目中CasServerUtil类的main方法测试使用REST验证ticket

```



[cas-server-rest:CAS之5.2x版本之REST验证ticket](https://github.com/louisliaoxh1989/spring-boot-cas/tree/master/cas-server-rest)



## 参考
[java生成证书](https://blog.csdn.net/yelllowcong/article/details/78805420)
