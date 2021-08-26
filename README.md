# Alertmanager Webhoook

## 编译

	neo@MacBook-Pro-Neo ~/workspace/alertmanager % mvn clean package docker:build

## Alertmanager 配置

	global:
	
	route:
	  group_by: ["alertname"]
	  group_wait: 10s
	  group_interval: 10s
	  repeat_interval: 1h
	  receiver: webhook
	
	receivers:
	- name: 'webhook'
	  webhook_configs:
	    - url: 'http://alertmanager-webhook:8080/webhook'
	
## docker-compose.yaml 容器编排文件

	version: '3.9'	
	services:
	  alertmanager-webhook:
	    image: netkiller/alertmanager
	    container_name: alertmanager-webhook
	    restart: always
	    hostname: alertmanager-webhook
	    extra_hosts:
	      - dysmsapi.aliyuncs.com:106.11.45.35
	    environment:
	      TZ: Asia/Shanghai
	      JAVA_OPTS: -Xms256m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m
	    ports:
	      - 8080:8080
	    volumes:
	      - ${PWD}/alertmanager/application.properties:/app/application.properties
	      - /tmp/alertmanager:/tmp
	    working_dir: /app
	    command:
	      --spring.config.location=/app/application.properties
	    
## application.properties 配置文件	    

	# 开启调试模式
	logging.level.cn.netkiller=DEBUG
	# 暂未使用
	alertmanager.email=netkiller@email.com
	# 报警手机，多个手机好吗使用逗号分割
	alertmanager.mobile=13113666600
	
	# 阿里云区域
	aliyun.sms.regionId=cn-shanghai	
	# accessKeyId 和 accessSecret
	aliyun.sms.accessKeyId=
	aliyun.sms.accessSecret=
	# 签名
	aliyun.sms.signName=
	# 模版号
	aliyun.sms.templateCode=
	
## Debug 调试信息

	neo@MacBook-Pro-Neo ~/workspace/docker/prometheus % docker-compose up alertmanager-webhook  
	[+] Running 0/1
	[+] Running 1/1ertmanager-webhook  Recreate                                                                                                                                                            0.1s
	 ⠿ Container alertmanager-webhook  Started                                                                                                                                                             0.9s
	Attaching to alertmanager-webhook
	alertmanager-webhook  | Alertmanager Webhook...
	alertmanager-webhook  | 
	alertmanager-webhook  |   .   ____          _            __ _ _
	alertmanager-webhook  |  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
	alertmanager-webhook  | ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
	alertmanager-webhook  |  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
	alertmanager-webhook  |   '  |____| .__|_| |_|_| |_\__, | / / / /
	alertmanager-webhook  |  =========|_|==============|___/=/_/_/_/
	alertmanager-webhook  |  :: Spring Boot ::                (v2.5.4)
	alertmanager-webhook  | 
	alertmanager-webhook  | 2021-08-24 11:44:28.024  INFO 1 --- [           main] cn.netkiller.alertmanager.Application    : Starting Application v0.0.1 using Java 16.0.2 on alertmanager-webhook with PID 1 (/app/alertmanager-0.0.1.jar started by root in /app)
	alertmanager-webhook  | 2021-08-24 11:44:28.031 DEBUG 1 --- [           main] cn.netkiller.alertmanager.Application    : Running with Spring Boot v2.5.4, Spring v5.3.9
	alertmanager-webhook  | 2021-08-24 11:44:28.032  INFO 1 --- [           main] cn.netkiller.alertmanager.Application    : No active profile set, falling back to default profiles: default
	alertmanager-webhook  | 2021-08-24 11:44:31.307  INFO 1 --- [           main] o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port 8080
	alertmanager-webhook  | 2021-08-24 11:44:31.328  INFO 1 --- [           main] cn.netkiller.alertmanager.Application    : Started Application in 4.233 seconds (JVM running for 5.199)
	
	
	alertmanager-webhook  | 2021-08-24 11:44:33.739 DEBUG 1 --- [or-http-epoll-2] c.n.a.controller.WebhookController       : Alertmanager [receiver=webhook, status=resolved, alerts=[Alert [status=resolved, labels=Labels [alertname=磁盘满22, dev=sda1, instance=example, msgtype=testing], annotations=Annotations [info=/dev/vdb2 磁盘空间满, summary=/dev/vdb1 磁盘空间满], startsAt=2021-08-24T11:33:19.3561516+08:00, endsAt=2021-08-24T11:38:19.3561516+08:00, generatorURL=, fingerprint=19ad02392a8479d3]], groupLabels=GroupLabels [instance=null], commonLabels=CommonLabels [alertname=磁盘满22, dev=sda1, instance=example, msgtype=testing], commonAnnotations=CommonAnnotations [info=/dev/vdb2 磁盘空间满, summary=/dev/vdb1 磁盘空间满], externalURL=http://alertmanager:9093, version=4, groupKey={}:{alertname="磁盘满22"}, truncatedAlerts=0]
	alertmanager-webhook  | 2021-08-24 11:44:33.742 DEBUG 1 --- [or-http-epoll-2] c.n.a.controller.WebhookController       : RFC3339 Data: 2021-08-24T11:33:19+08:00
	alertmanager-webhook  | 2021-08-24 11:44:33.756  INFO 1 --- [or-http-epoll-2] c.n.a.controller.WebhookController       : Notification [status=resolved, time=Tue Aug 24 11:33:19 CST 2021, summary=/dev/vdb1 磁盘空间满]
	alertmanager-webhook  | 2021-08-24 11:44:34.964 DEBUG 1 --- [         task-1] c.n.alertmanager.service.MessageService  : {"RequestId":"6C893328-921B-52A3-83AD-BB9962F57877","Message":"OK","BizId":"629618129776674776^0","Code":"OK"}
	alertmanager-webhook  | 2021-08-24 11:44:34.965  INFO 1 --- [         task-1] c.n.alertmanager.service.MessageService  : 发送短信 13113668890 => {"status":"resolved","time":"2021-08-24 11:33:19","summary":"/dev/vdb1 磁盘空间满"}
	alertmanager-webhook  | 2021-08-24 11:44:38.685 DEBUG 1 --- [or-http-epoll-2] c.n.a.controller.WebhookController       : Alertmanager [receiver=webhook, status=firing, alerts=[Alert [status=firing, labels=Labels [alertname=磁盘满22, dev=sda1, instance=example, msgtype=testing], annotations=Annotations [info=/dev/vdb2 磁盘空间满, summary=/dev/vdb1 磁盘空间满], startsAt=2021-08-24T11:44:36.6812117+08:00, endsAt=0001-01-01T00:00:00Z, generatorURL=, fingerprint=19ad02392a8479d3]], groupLabels=GroupLabels [instance=null], commonLabels=CommonLabels [alertname=磁盘满22, dev=sda1, instance=example, msgtype=testing], commonAnnotations=CommonAnnotations [info=/dev/vdb2 磁盘空间满, summary=/dev/vdb1 磁盘空间满], externalURL=http://alertmanager:9093, version=4, groupKey={}:{alertname="磁盘满22"}, truncatedAlerts=0]
	alertmanager-webhook  | 2021-08-24 11:44:38.686 DEBUG 1 --- [or-http-epoll-2] c.n.a.controller.WebhookController       : RFC3339 Data: 2021-08-24T11:44:36+08:00
	alertmanager-webhook  | 2021-08-24 11:44:38.687  INFO 1 --- [or-http-epoll-2] c.n.a.controller.WebhookController       : Notification [status=firing, time=Tue Aug 24 11:44:36 CST 2021, summary=/dev/vdb1 磁盘空间满]
	alertmanager-webhook  | 2021-08-24 11:44:38.849 DEBUG 1 --- [         task-2] c.n.alertmanager.service.MessageService  : {"RequestId":"109AB2F7-3796-562D-9881-EDDDD12A75F1","Message":"OK","BizId":"836814229776678788^0","Code":"OK"}
	alertmanager-webhook  | 2021-08-24 11:44:38.849  INFO 1 --- [         task-2] c.n.alertmanager.service.MessageService  : 发送短信 13113668890 => {"status":"firing","time":"2021-08-24 11:44:36","summary":"/dev/vdb1 磁盘空间满"}