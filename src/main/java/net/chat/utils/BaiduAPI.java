package net.chat.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

public class BaiduAPI {
	private static String ak = "5ef2641d89438a6e708db122820cf1d2";

	public static Map<String, String> testPost(String x, String y)
			throws IOException {
		URL url = new URL("http://api.map.baidu.com/geocoder/v2/?ak=" + ak
				+ "&callback=renderReverse&location=" + x + "," + y
				+ "&output=json&pois=0");
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(
				connection.getOutputStream(), "utf-8");
		// remember to clean up
		out.flush();
		out.close();
		String res;
		InputStream l_urlStream;
		l_urlStream = connection.getInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				l_urlStream, "UTF-8"));
		StringBuilder sb = new StringBuilder("");
		while ((res = in.readLine()) != null) {
			sb.append(res.trim());
		}
		String str = sb.toString();
		Map<String, String> map = null;
		if (StringUtils.isNotEmpty(str)) {
			int addStart = str.indexOf("formatted_address\":");
			int addEnd = str.indexOf("\",\"business");
			if (addStart > 0 && addEnd > 0) {
				String address = str.substring(addStart + 20, addEnd);
				map = new HashMap<String, String>();
				map.put("address", address);
				return map;
			}
		}
		return null;
	}

	public static String navagation(String origin, String destination,
			String city) throws IOException {
		URL url = new URL(
				"http://api.map.baidu.com/direction/v1?mode=driving&origin="
						+ origin + "&destination=" + destination
						+ "&origin_region=" + city + "&destination_region="
						+ city
						+ "&output=json&ak=5ef2641d89438a6e708db122820cf1d2");
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(
				connection.getOutputStream(), "utf-8");
		// remember to clean up
		out.flush();
		out.close();
		String res;
		InputStream l_urlStream;
		l_urlStream = connection.getInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				l_urlStream, "UTF-8"));
		StringBuilder sb = new StringBuilder("");
		while ((res = in.readLine()) != null) {
			sb.append(res.trim());
		}
		String str = sb.toString();
		StringBuilder st = new StringBuilder();
		JSONObject jsonObject = JSONObject.fromObject(str);
		JSONObject ob = jsonObject.getJSONObject("result")
				.getJSONArray("routes").getJSONObject(0);
		JSONArray steps = ob.getJSONArray("steps");
		for (int i = 0; i < steps.size(); i++) {
			String instruction = (String) steps.getJSONObject(i).get(
					"instructions");
			st.append(instruction);
		}
		return st.toString();

	}
}
