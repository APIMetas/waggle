# APIMetas Waggle
A true and lightweighted microservice framework. 一款真正的微服务框架。

Waggle is a designing and meshing tool for docker based microservices. Leveraging embed databases such as [Berkekey DB](https://en.wikipedia.org/wiki/Berkeley_DB) and [SQLite](https://www.sqlite.org/index.html), comes into a unified key-value based tree store. It also provides a API-centric service developing way, let developers focus on object hierarchy and programming interfaces. With built in orchestration and metric tools. Microservices can be tested and mesaured easily. 

Waggle是一款“微服务”开发框架，为开发Docker化的微服务提供了轻量的连接。同时基于Berkeley DB或者SQLite, Waggle库内建了“多租户”的、“REST”型数据存储。省去了连接远程服务器的麻烦，使得数据和代码过程在一起，实现了真正的“微服务”。Operon库为API提供了编排和测试入口，开发者可以通过Operon库自动测试所设计的API功能和性能。

## Online Documentation（在线文档）

You can find the latest waggle documentation, including a product specification, a programing guide and a startup example on the [product web page](https://www.apimetas.com/docs/waggle.html). This README file only contains basic instructions.

最新的文档请参照[产品说明](https://www.apimetas.com/docs/waggle.html),此处仅就基本设计理念做出澄清。

## Data as Data （数据就是数据）
JSON is wildly used for data exchange. 
![](https://apimetas-1255930917.cos.ap-beijing.myqcloud.com/json.png)
Waggle assuming data is presented in JSON format. O/R mapping might be a utopian, as this may slower applications response time and made applications hard to scale and maintian. Data is only useful when analyse it. Waggle treat data as data, store data and its hierarchy as REST-style. Waggle do not parse the entire [JSON](https://www.json.org) object to Java class, only parse its key and compose the REST hierarchy key. The naming convention is as follows.

数据的格式有很多种，Waggle使用JSON来表示和交换数据。它并不会完全解析JSON格式并转换成Java类。取而代之，Waggle只会解析数据的主键，然后按照REST的风格存储在数据库里。数据只有在处理的时候才需要被解释，O/R转换会浪费额外的时间，并且导致代码到处都是无用的set和get函数。数据存储时，如何确定Key-Value的键值，请参照下面章节。

## Key Naming Convention （键的设计）
Normally a json file contains a hierarchy of object. But RESTful API treat resources as a url path. Waggle use REST-style path as key-value stores key. Here are details.

![](https://apimetas-1255930917.cos.ap-beijing.myqcloud.com/rest.png)

    {UserId}/{FatherKeyName}s/{FatherKeyValue}/...

Let's say a user(10002334) has a documment and the document titled "waggle" has 3 parts. So the first part naming key should be like this,

    10002334/Documents/waggle/Parts/0

Public cloud provider's core assuming is multi-tenancy, waggle is born multi-tenant. Resources all belong to a specific user.

数据在Waggle里组织的方式key-value的，对象体系的键映射使用了REST风格的路径。举个例子，假设每个用户都有“文章”，每个文章都有3个部分。要取得文章名为waggle的第一个部分，其REST的表现路径为：

    10002334/Documents/waggle/Parts/0

所有公有云的核心实现方式都是“多租户”的，Waggle天然的将UserId作为每个资源键的开始部分，所以资源都属于某个特定的用户，天然的实现了多租户。同时每个“键”的奇数段表示值，偶数段表示对象概念，理论上讲这种设计可以表示无穷对象。

## Built in API

    SubscribeResource
    		Parameter：ResourceKey 
    GetAPIMetrics
    		Parameter：ActionName

Waggle's server working at API and Pub/Sub models. Client can subsrcirbe some topics and if resource changed, client will get a notification with a request id "000000000000". As Waggle‘s key's designing philosophy, each object can be mapped as a Waggle Key. So it is natural that you can subscribe anything inside Waggle.
Waggle's server monitored each api's performance, such as Max Response Time、QPS、MRT(mean response time). "GetAPIMetrics" retrieves performance data and returns to the client. The buffer cleared when read. So it is a best practise to read metrics per second.

Waggle 会自动注册两个API，以便于监控API性能和工作在Pub/Sub模型。由于天然的Waggle使用REST路径来表示对象，这个路径就变成了一个一个的Topic。一个客户端可以“订阅”多个“话题”，可以订阅通配符话题，话题的键类型参照[MQTT](http://mqtt.org/)协议。订阅的客户端收到“通知”时，RequestId会被置成“000000000000”，以便于区分主动调用或者通知。Waggle服务会自动监控API的性能参数，比如：最大响应时间、每秒流量和平均响应时间。由于性能缓存被设计成“读清”，所以每次的读取时间最好不要小于一秒。


## Tcp or Udp or Unix Domain Socket
Waggle's serving model working under both Tcp and Udp protocol. Waggle will open a unix Domain Socket located directory you specified. Monitering agent such as APM agent can access API metrics from Unix Domain Socket. It's up to you to choose your service serving model. But for small and concurrent requests, Udp protocol is recommended.

Waggle可以监听Tcp或者Udp端口，Waggle会启动一个Unix域套接字以供本地的监控代理监听性能。TCP和UDP协议的选择完全自由，但是如果是小数据量但频繁的请求，Udp服务是推荐的。请参照另外一个项目[river](https://github.com/APIMetas/river)来查看UDP协议下，Waggle的性能表现。

## More to come

1, High Availability model inside a docker cluster.

Docker集群中的高可用特性，基于Pub/Sub模型，保证服务的“双活”。

2, Service sharding according to multitenanted user.

服务分片，比如按照用户服务分片的demo会在近期更新。

3, Performance testing, please refer to [product web page](https://www.apimetas.com/docs/waggle.html).

