package cn.netkiller.alertmanager.controller;

import java.util.List;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.netkiller.alertmanager.domain.Alertmanager;
import cn.netkiller.alertmanager.domain.Alertmanager.Alert;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;

@Slf4j
@RestController
public class WebhookController {
	private  final Logger logger = LoggerFactory.getLogger(WebhookController.class);
	@Value("${alertmanager.email}")
	private String email;

	@Value("#{'${alertmanager.mobile}'.split(',')}")
	private List<String> mobiles;

	@Value("${aliyun.sms.regionId}")
	private String regionId;
	@Value("${aliyun.sms.accessKeyId}")
	private String accessKeyId;
	@Value("${aliyun.sms.accessSecret}")
	private String accessSecret;
	@Value("${aliyun.sms.signName}")
	private String signName;
	@Value("${aliyun.sms.templateCode}")
	private String templateCode;

	public WebhookController() {
		// TODO Auto-generated constructor stub
	}

	@GetMapping("/ping")
	@ResponseBody
	public Publisher<String> index() {
		return Mono.just("pong\r\n");
	}

	@PostMapping("/debug")
	public Mono<String> debug(@RequestBody String json) {
		// curl -XPOST -d'{"code":"200000"}' -H "Content-Type: application/json" http://localhost:8080/debug
		logger.debug(json);
		for (String mobile : this.mobiles) {
			this.send(mobile, json);
		}
		return Mono.just(json);
	}

	/*
	 * curl -XPOST -d'{"receiver":"webhook","status":"firing","alerts":[{"status":"firing","labels":{"alertname":"DiskRunning","dev":"sda1","instance":"example","msgtype":"testing"},"annotations":{"info":"The disk sda1 is running full","summary":"please check the instance example1"},"startsAt":"2021-08-23T06:24:41.576051221Z","endsAt":"0001-01-01T00:00:00Z","generatorURL":"","fingerprint":"f4dafa71f4a9edb8"}],"groupLabels":{"instance":"example"},"commonLabels":{"alertname":"DiskRunning","dev":"sda1","instance":"example","msgtype":"testing"},"commonAnnotations":{"info":"The disk sda1 is running full","summary":"please check the instance example1"},"externalURL":"http://alertmanager:9093","version":"4","groupKey":"{}:{instance=\"example\"}","truncatedAlerts":0}' -H "Content-Type: application/json" http://localhost:8080/send
	 */
	/*
	 * {"receiver":"webhook","status":"firing","alerts":[{"status":"firing","labels":{"alertname":"DiskRunning","dev":"sda1","instance":"example","msgtype":"testing"},"annotations":{"info":"The disk sda1 is running full","summary":"please check the instance example1"},"startsAt":"2021-08-23T06:24:41.576051221Z","endsAt":"0001-01-01T00:00:00Z","generatorURL":"","fingerprint":"f4dafa71f4a9edb8"}],"groupLabels":{"instance":"example"},"commonLabels":{"alertname":"DiskRunning","dev":"sda1","instance":"example","msgtype":"testing"},"commonAnnotations":{"info":"The disk sda1 is running full","summary":"please check the instance example1"},"externalURL":"http://alertmanager:9093","version":"4","groupKey":"{}:{instance=\"example\"}","truncatedAlerts":0}
	 */
	@PostMapping("/webhook")
	public Flux<Alertmanager> webhook(@RequestBody Alertmanager alertmanager) {
		System.out.println(alertmanager.toString());
		for (Alert alert : alertmanager.alerts) {
			System.out.println(alert.toString());
			String status = alert.status;
			String info = alert.annotations.info;
			String summary = alert.annotations.summary;
			String time = alert.startsAt;
		}
		return Flux.just(alertmanager);
	}

	// @PostMapping(value = "/webhook", produces = "application/json;charset=UTF-8")
	public boolean send(String number, String jsonMessage) {

		DefaultProfile profile = DefaultProfile.getProfile(this.regionId, this.accessKeyId, this.accessSecret);
		IAcsClient client = new DefaultAcsClient(profile);

		CommonRequest request = new CommonRequest();
		request.setSysMethod(MethodType.POST);
		request.setSysDomain("dysmsapi.aliyuncs.com");
		request.setSysVersion("2017-05-25");
		request.setSysAction("SendSms");
		request.putQueryParameter("PhoneNumbers", number);
		request.putQueryParameter("SignName", this.signName);
		request.putQueryParameter("TemplateCode", this.templateCode);
		request.putQueryParameter("TemplateParam", jsonMessage);
		try {
			CommonResponse response = client.getCommonResponse(request);
			logger.debug(response.getData());
			if (response.getHttpStatus() == 200) {
				return true;
			}
		} catch (ServerException e) {
			e.printStackTrace();
		} catch (ClientException e) {
			e.printStackTrace();
		}
		return false;

	}

}
