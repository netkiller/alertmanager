# Alertmanager Webhoook

## 编译

	neo@MacBook-Pro-Neo ~/workspace/alertmanager % mvn clean package docker:build
	
## docker-compose.yaml 容器编排文件

	version: '3.9'
	services:
	  alertmanager-webhook:
	    image: netkiller/alertmanager
	    container_name: alertmanager-webhook
	    restart: always
	    hostname: alertmanager.netkiller.cn
	    extra_hosts:
	      - dysmsapi.aliyuncs.com:106.11.45.35
	    environment:
	      TZ: Asia/Shanghai
	      JAVA_OPTS: -Xms256m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m
	    ports:
	      - 8080:8080
	    volumes:
	      - /tmp/alertmanager:/tmp
	    working_dir: /app
	    command:
	      --spring.config.location=/opt/config/application.properties
	    
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