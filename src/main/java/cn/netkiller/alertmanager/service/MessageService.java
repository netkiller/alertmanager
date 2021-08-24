package cn.netkiller.alertmanager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;

@Component
public class MessageService {

	private final Logger logger = LoggerFactory.getLogger(MessageService.class);

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

	public MessageService() {
		// TODO Auto-generated constructor stub
	}

	@Async
	public void send(String number, String jsonMessage) {

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
				// return true;
				logger.info("发送短信 {} => {}", number, jsonMessage);
			}
		} catch (ServerException e) {
			e.printStackTrace();
		} catch (ClientException e) {
			e.printStackTrace();
		}
		// return false;

	}
}
