package cn.netkiller.alertmanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class Test {

	public Test() {

	}

	public static void main(String[] args) throws ParseException {
		String dt = "2021-08-26T09:17:23.859291804+08:00";
		String rfc3339 = dt.replaceAll("\\.([0-9]+)", "");
		System.out.println(dt);
		System.out.println(rfc3339);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		System.out.println(simpleDateFormat.parse(rfc3339).toLocaleString());
	}

}
