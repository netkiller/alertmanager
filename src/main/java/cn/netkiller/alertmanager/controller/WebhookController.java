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
		logger.info(json);
		return Mono.just(json);
	}

	/*
	 * curl -XPOST -d'{"receiver":"webhook","status":"firing","alerts":[{"status":"firing","labels":{"alertname":"磁盘满111","dev":"sda1","instance":"example","msgtype":"testing"},"annotations":{"info":"/dev/vdb2 磁盘空间满","summary":"/dev/vdb1 磁盘空间满"},"startsAt":"2021-08-24T10:14:48.4008794+08:00","endsAt":"0001-01-01T00:00:00Z","generatorURL":"","fingerprint":"6e0ca60a542b6b60"}],"groupLabels":{"alertname":"磁盘满111"},"commonLabels":{"alertname":"磁盘满111","dev":"sda1","instance":"example","msgtype":"testing"},"commonAnnotations":{"info":"/dev/vdb2 磁盘空间满","summary":"/dev/vdb1 磁盘空间满"},"externalURL":"http://alertmanager:9093","version":"4","groupKey":"{}:{alertname=\"磁盘满111\"}","truncatedAlerts":0}' -H "Content-Type: application/json" http://localhost:8080/webhook curl -XPOST
	 * -d'{"receiver":"webhook","status":"firing","alerts":[{"status":"firing","labels":{"alertname":"磁盘满","dev":"sda1","instance":"example","msgtype":"testing"},"annotations":{"info":"/dev/vdb1 磁盘空间满","summary":"/dev/vdb1 磁盘空间满"},"startsAt":"2021-08-23T11:56:27.57942Z","endsAt":"0001-01-01T00:00:00Z","generatorURL":"","fingerprint":"95e73d7772e5237f"}],"groupLabels":{"instance":"example"},"commonLabels":{"alertname":"磁盘满","dev":"sda1","instance":"example","msgtype":"testing"},"commonAnnotations":{"info":"/dev/vdb1 磁盘空间满","summary":"/dev/vdb1 磁盘空间满"},"externalURL":"http://alertmanager:9093","version":"4","groupKey":"{}:{instance=\"example\"}","truncatedAlerts":0}' -H "Content-Type: application/json" http://localhost:8080/webhook
	 */

	@PostMapping("/webhook")
	public Flux<Alertmanager> webhook(@RequestBody Alertmanager alertmanager) throws ParseException {
		// https://datatracker.ietf.org/doc/html/rfc3339
		logger.debug(alertmanager.toString());
		for (Alert alert : alertmanager.alerts) {
			String status = alert.status;
			// String info = alert.annotations.info;
			String summary = alert.annotations.summary;
			String rfc3339 = "";
			String format = "";
			if (alert.startsAt.indexOf("+") == -1) {
				// 来自 Prometheus 推送的报警
				rfc3339 = alert.startsAt;
				format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
			} else {
				// Alertmanager BUG 通过 curl 请求产生的日志 2021-08-26T09:37:46.012506149+08:00
				// rfc3339 = alert.startsAt.replaceAll(".[0-9]{7}", "");
				rfc3339 = alert.startsAt.replaceAll("\\.([0-9]+)", "");
				format = "yyyy-MM-dd'T'HH:mm:ssXXX";
			}

			// SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
			simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			Date datetime = simpleDateFormat.parse(rfc3339);
			Notification notification = new Notification(status, datetime, summary);
			logger.debug("startsAt Data: {}", alert.startsAt);
			logger.debug("RFC3339 Data: {}", rfc3339);
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
