## 项目描述
基于 SpringBoot+Maven+Mybatis+Redis+RabbitMQ 高并发商城秒杀系统

## 具体优化

1. 使用分布式Seesion，让多台服务器可以响应。

   实际应用中, 不会只有一个应用服务器, 肯定是分布式多台应用服务器. 假如用户登录是在第一个服务器，第一个请求到了第一台服务器，但是第二个请求到了第二个服务器，这时候B服务器并不存在Session，所以就会将用户踢到登录页面。这将大大降低用户体验度，导致用户的流失，这种情况是项目绝不应该出现的。

   **处理**: 登录成功之后,给用户生成类似于sessionId的东西token来标识用户,然后写入cookie当中传递给客户端,客户端在随后的访问中上传该token,然后服务端取到token后,就跟据这个token取到用户对应的session信息.

   

2. 使用redis做缓存提高访问速度和并发量，减少数据库压力。

3. 使用页面静态化，缓存页面至浏览器，前后端分离降低服务器压力。

4. 使用消息队列完成异步下单，提升用户体验，削峰和降流。

5. 安全性优化：双重md5密码校验，秒杀接口地址的隐藏，接口限流防刷，数学公式验证码。



## 运行说明

1.需要启动redis

```
通过配置文件启动redis
redis-server ./redis.conf

查看Redis进程
ps -e|grep redis

关闭redis进程
> redis-cli
127.0.0.1:6379> SHUTDOWN save
not connected> exit
```

2.需要启动rabbitmq

```
启动rabbitmq
root@ubuntu:/usr/local/rabbitmq/sbin# ./rabbitmq-server

查看rabbitmq启动日志
root@ubuntu:/usr/local/rabbitmq/sbin# tail -f /usr/local/rabbitmq/var/log/rabbitmq/rabbit\@ubuntu.log

关闭rabbitmq
root@ubuntu:/usr/local/rabbitmq/sbin# ./rabbitmqctl stop
```



## 演示

登录界面

![][1]

秒杀商品界面

![][2]

秒杀商品详情界面

![][3]

秒杀成功界面

![][4]

秒杀订单详情

![][5]





[1]: https://raw.githubusercontent.com/Cqh-i/JavaMiaosha/master/%E6%BC%94%E7%A4%BA%E5%9B%BE%E7%89%87/%E7%99%BB%E5%BD%95%E7%95%8C%E9%9D%A2.png
[2]: https://raw.githubusercontent.com/Cqh-i/JavaMiaosha/master/%E6%BC%94%E7%A4%BA%E5%9B%BE%E7%89%87/%E7%A7%92%E6%9D%80%E5%95%86%E5%93%81%E7%95%8C%E9%9D%A2.png
[3]: https://raw.githubusercontent.com/Cqh-i/JavaMiaosha/master/%E6%BC%94%E7%A4%BA%E5%9B%BE%E7%89%87/%E7%A7%92%E6%9D%80%E5%95%86%E5%93%81%E8%AF%A6%E6%83%85%E7%95%8C%E9%9D%A2.png
[4]: https://raw.githubusercontent.com/Cqh-i/JavaMiaosha/master/演示图片/秒杀成功.png
[5]: https://raw.githubusercontent.com/Cqh-i/JavaMiaosha/master/演示图片/秒杀订单详情.png
