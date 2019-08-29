## 项目描述
基于 SpringBoot+Maven+Mybatis+Redis+RabbitMQ 高并发商城秒杀系统

## 具体优化

1. **使用分布式Seesion，让多台服务器可以响应。**

   实际应用中, 不会只有一个应用服务器, 肯定是分布式多台应用服务器. 假如用户登录是在服务器A，第一个请求到了服务器A，但是第二个请求到了服务器B，这时候服务器B并不存在该Session，所以就会将用户踢到登录页面。这将大大降低用户体验度，导致用户的流失，这种情况是项目绝不应该出现的。

   **处理**: 登录成功之后,给用户生成类似于sessionId的东西token来标识用户, 然后写入cookie当中传递给客户端, 并将此(token -> 用户信息) 信息存入redis中, 客户端在随后的访问中上传该token,然后服务端取到token后,就跟据这个token去取用户对应的session信息(redis中).

2. **使用redis做缓存提高访问速度和并发量，减少数据库压力。**

   **页面缓存(URL缓存)** 

   ( 页面缓存保存的时间不宜过长, 需要折中, 保证数据更新的及时性又能减少服务器的压力 )

   1) 取缓存 2) 如果缓存中不存在, 则手动渲染, 并保存到缓存中

   ```java
   // 1.取缓存
   String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
   if (!StringUtils.isEmpty(html)) {
       return html;
   }
   
   // 2.手动渲染 使用模板引擎 templateName:模板名称 String templateName="goods_list";
   SpringWebContext context = new SpringWebContext(request, response, request.getServletContext(),
           request.getLocale(), model.asMap(), applicationContext);
   html = thymeleafViewResolver.getTemplateEngine().process("goods_list", context);
   // 保存至缓存
   if (!StringUtils.isEmpty(html)) {
       redisService.set(GoodsKey.getGoodsList, "", html);// key---GoodsKey:gl---缓存goodslist这个页面
   }
   return html;
   ```
   
   **对象缓存**
   
   相比页面缓存是更细粒度缓存。在实际项目中， 不会大规模使用页面缓存，对象缓存就是当用到用户数据的时候，可以从缓存中取出。比如：更新用户密码，根据token来获取用户缓存对象。
   
   ```java
   /**
    * 根据id取得对象，先去缓存中取
    * @param id
    * @return
    */
   public MiaoshaUser getById(long id) {
       // 1.取缓存 ---先根据id来取得缓存
       MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, "" + id, MiaoshaUser.class);
       // 能再缓存中拿到
       if (user != null) {
           return user;
       }
       // 2.缓存中拿不到，那么就去取数据库
       user = miaoshaUserDao.getById(id);
       // 3.设置缓存
       if (user != null) {
           redisService.set(MiaoshaUserKey.getById, "" + id, user);
       }
       return user;
   }
   ```
   
   **缓存更新策略是先更新数据库后删除缓存**

3. **使用页面静态化，缓存页面至浏览器，前后端分离降低服务器压力。**

   常用技术: Vue.js, AngularJS

   此处只是简单的实现页面静态化.

   **对比**

   未作页面静态化：请求某一个页面，访问缓存，查看缓存中是否有，缓存中有直接返回，缓存中没有的话，将数据渲染到html页面再存到缓存，再将整个html页面返回给客户端显示。

   做了页面静态化：第一次是去请求后台要渲染好的html页面，之后的请求都是直接访问用户本地浏览器的缓存的html页面 ，静态资源，然后前端通过Ajax来访问后端，只去获取页面需要显示的数据返回即可。

4. **使用消息队列完成异步下单，提升用户体验，削峰和降流。**

   秒杀接口优化

   1)系统初始化,将商品库存数量加载到redis

   2)收到请求, Redis预减库存,库存不足, 直接返回, 否则进入3)

   3)请求入队,立即返回排队中

   4)请求出队,生成订单,减少库存

   5)客户端轮询,是否秒杀成功

5. **安全性优化：双重md5密码校验，秒杀接口地址的隐藏，接口限流防刷，数学公式验证码。**



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
