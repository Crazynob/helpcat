# helpcat

#### 介绍
今日校园签到,查寝,信息采集
B站教程:https://www.bilibili.com/video/BV1kX4y1V7rW/

###程序介绍
采用云端代理服务器的形式获取登录信息,从而避免重复登录,共享登录信息,不影响APP正常使用, 所有任务根据历史任务,自动生成任务以及答案,无需自己手工配置

### 程序配置
1. 首先创建一个数据库,推荐mysql数据库,然后导入helpcat.sql 这个文件执行创建数据表操作
2. 修改项目的数据库配置,在src/main/resources 目录下 有个db.properties文件 在此文件中配置数据库链接
3. 修改项目的前端页面配置 ,同样在src/main/resources 目录下 有个index.html文件,需要配置百度地图的AK值,如何获得AK请自行百度
以上配置修改完成后,把项目打成jar包 上传到服务器即可运行

### 其他的配置修改
1. 代理端口修改在red.fuyun.ProxyServer.InterceptFullHttpProxyServer类种 start方法 

2. 系统每天都会进行状态的重置 ,状态重置时间配置在 red.fuyun.campusphere.impl.ResetState 类中 默认在每天00:00:00 @Scheduled(cron = "0 0 0 1/1 * ?") 重置所有状态  可自行修改 @Scheduled(cron = "此处配置你得cron表达式")

3. 任务执行时机 在red.fuyun.campusphere.impl.WaitSigneQueue类中 @Scheduled(cron = "此处配置你得cron表达式") 默认为@Scheduled(cron = "5/20 * * * * ? ") 从每分钟第五秒开始,间隔二十秒执行一次,你可以根据需要自行定义,注意程序默认在00:00:00 重置所有状态,若任务执行时机在00:00:00可能会发生冲突

4. 代理设置以后,仅放行白名单内的url,其余uri不允许访问,配置白名单在red.fuyun.util.Util类中 matchUri方法和matchHost方法,两个方法的区别如下
matchUri:此方法匹配的是url,仅当url匹配到白名单内的任一路径则放行
matchHost:此方法匹配的是 请求头中的host头的值,仅当匹配到白名单内的任一关键词放行
白名单仅做简单处理,可以自行修改 配置到文件或数据库
