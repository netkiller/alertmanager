package cn.netkiller.alertmanager.domain;

import java.util.Date;
import java.util.List;

public class Alertmanager {

	public static class Labels {
		public String alertname;
		public String dev;
		public String instance;
		public String msgtype;
	}

	public static class Annotations {
		public String info;
		public String summary;
	}

	public static class Alert {
		public String status;
		public Labels labels;
		public Annotations annotations;
		public String startsAt;
		public Date endsAt;
		public String generatorURL;
		public String fingerprint;
	}

	public static class GroupLabels {
		public String instance;
	}

	public static class CommonLabels {
		public String alertname;
		public String dev;
		public String instance;
		public String msgtype;
	}

	public static class CommonAnnotations {
		public String info;
		public String summary;
	}

	public String receiver;
	public String status;
	public List<Alert> alerts;
	public GroupLabels groupLabels;
	public CommonLabels commonLabels;
	public CommonAnnotations commonAnnotations;
	public String externalURL;
	public String version;
	public String groupKey;
	public int truncatedAlerts;

	public Alertmanager() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "Alertmanager [receiver=" + receiver + ", status=" + status + ", alerts=" + alerts + ", groupLabels=" + groupLabels + ", commonLabels=" + commonLabels + ", commonAnnotations=" + commonAnnotations + ", externalURL=" + externalURL + ", version=" + version + ", groupKey=" + groupKey + ", truncatedAlerts=" + truncatedAlerts + "]";
	}

}
