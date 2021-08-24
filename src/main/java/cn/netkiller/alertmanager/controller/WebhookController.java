package cn.netkiller.alertmanager.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.netkiller.alertmanager.domain.Alertmanager;
import cn.netkiller.alertmanager.domain.Alertmanager.Alert;
import cn.netkiller.alertmanager.domain.Notification;
import cn.netkiller.alertmanager.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@RestController
public class WebhookController {
	private final Logger logger = LoggerFactory.getLogger(WebhookController.class);
	@Value("${alertmanager.email}")
	private String email;

	@Value("#{'${alertmanager.mobile}'.split(',')}")
	private List<String> mobiles;

	@Autowired
	private MessageService messageService;

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
		return Mono.just(json);
	}

	/*
	 * curl -XPOST -d'{"receiver":"webhook","status":"firing","alerts":[{"status":"firing","labels":{"alertname":"磁盘满111","dev":"sda1","instance":"example","msgtype":"testing"},"annotations":{"info":"/dev/vdb2 磁盘空间满","summary":"/dev/vdb1 磁盘空间满"},"startsAt":"2021-08-24T10:14:48.4008794+08:00","endsAt":"0001-01-01T00:00:00Z","generatorURL":"","fingerprint":"6e0ca60a542b6b60"}],"groupLabels":{"alertname":"磁盘满111"},"commonLabels":{"alertname":"磁盘满111","dev":"sda1","instance":"example","msgtype":"testing"},"commonAnnotations":{"info":"/dev/vdb2 磁盘空间满","summary":"/dev/vdb1 磁盘空间满"},"externalURL":"http://alertmanager:9093","version":"4","groupKey":"{}:{alertname=\"磁盘满111\"}","truncatedAlerts":0}' -H "Content-Type: application/json" http://localhost:8080/webhook curl -XPOST
	 * -d'{"receiver":"webhook","status":"firing","alerts":[{"status":"firing","labels":{"alertname":"磁盘满","dev":"sda1","instance":"example","msgtype":"testing"},"annotations":{"info":"/dev/vdb1 磁盘空间满","summary":"/dev/vdb1 磁盘空间满"},"startsAt":"2021-08-23T11:56:27.57942Z","endsAt":"0001-01-01T00:00:00Z","generatorURL":"","fingerprint":"95e73d7772e5237f"}],"groupLabels":{"instance":"example"},"commonLabels":{"alertname":"磁盘满","dev":"sda1","instance":"example","msgtype":"testing"},"commonAnnotations":{"info":"/dev/vdb1 磁盘空间满","summary":"/dev/vdb1 磁盘空间满"},"externalURL":"http://alertmanager:9093","version":"4","groupKey":"{}:{instance=\"example\"}","truncatedAlerts":0}' -H "Content-Type: application/json" http://localhost:8080/webhook
	 */
	/*
	 * {"receiver":"webhook","status":"firing","alerts":[{"status":"firing","labels":{"alertname":"DiskRunning","dev":"sda1","instance":"example","msgtype":"testing"},"annotations":{"info":"The disk sda1 is running full","summary":"please check the instance example1"},"startsAt":"2021-08-23T06:24:41.576051221Z","endsAt":"0001-01-01T00:00:00Z","generatorURL":"","fingerprint":"f4dafa71f4a9edb8"}],"groupLabels":{"instance":"example"},"commonLabels":{"alertname":"DiskRunning","dev":"sda1","instance":"example","msgtype":"testing"},"commonAnnotations":{"info":"The disk sda1 is running full","summary":"please check the instance example1"},"externalURL":"http://alertmanager:9093","version":"4","groupKey":"{}:{instance=\"example\"}","truncatedAlerts":0}
	 * {"receiver":"webhook","status":"firing","alerts":[{"status":"firing","labels":{"alertname":"磁盘满","dev":"sda1","instance":"example","msgtype":"testing"},"annotations":{"info":"/dev/vdb1 磁盘空间满","summary":"/dev/vdb1 磁盘空间满"},"startsAt":"2021-08-23T11:56:27.57942Z","endsAt":"0001-01-01T00:00:00Z","generatorURL":"","fingerprint":"95e73d7772e5237f"}],"groupLabels":{"instance":"example"},"commonLabels":{"alertname":"磁盘满","dev":"sda1","instance":"example","msgtype":"testing"},"commonAnnotations":{"info":"/dev/vdb1 磁盘空间满","summary":"/dev/vdb1 磁盘空间满"},"externalURL":"http://alertmanager:9093","version":"4","groupKey":"{}:{instance=\"example\"}","truncatedAlerts":0}
	 */

	@PostMapping("/webhook")
	public Flux<Alertmanager> webhook(@RequestBody Alertmanager alertmanager) throws ParseException {
		// https://datatracker.ietf.org/doc/html/rfc3339
		// yyyy-MM-dd'T'h:m:ss.SZ
		// yyyy-MM-dd'T'HH:mm:ss.SSSXXX
		// yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
		// SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		logger.info(alertmanager.toString());

		for (Alert alert : alertmanager.alerts) {
			String status = alert.status;
			// String info = alert.annotations.info;
			String summary = alert.annotations.summary;
			String rfc3339 = alert.startsAt.replaceAll(".[0-9]{7}", "");
			Date datetime = simpleDateFormat.parse(rfc3339);
			Notification notification = new Notification(status, datetime, summary);
			logger.info(rfc3339);
			logger.info(notification.toString());
			ObjectMapper objectMapper = new ObjectMapper();
			String json;
			try {
				json = objectMapper.writeValueAsString(notification);
				for (String mobile : this.mobiles) {
					messageService.send(mobile, json);
				}
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return Flux.just(alertmanager);
	}
}
