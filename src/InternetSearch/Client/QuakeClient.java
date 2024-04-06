package InternetSearch.Client;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import InternetSearch.SearchEngine;
import InternetSearch.SearchResultEntry;
import Tools.JSONHandler;
import burp.BurpExtender;
import config.ConfigPanel;

public class QuakeClient extends BaseClient {


	@Override
	public String getEngineName() {
		return SearchEngine.QUAKE_360;
	}

	/**
	 *
	 */
	@Override
	public List<SearchResultEntry> parseResp(String respbody) {
		List<SearchResultEntry> result = new ArrayList<SearchResultEntry>();
		try {
			JSONObject obj = new JSONObject(respbody);
			int code = obj.getInt("code");
			if (code == 0) {
				JSONArray results = obj.getJSONArray("data");
				for (Object item : results) {

					JSONObject entryitem = (JSONObject) item;

					SearchResultEntry entry = new SearchResultEntry();


					entry.getIPSet().add(entryitem.getString("ip"));
					entry.setRootDomain(entryitem.getString("domain"));

					int port = entryitem.getInt("port");
					entry.setPort(port);

					String serviceName = entryitem.getJSONObject("service").getString("name");
					String protocol;
					if (serviceName.equalsIgnoreCase("http/ssl")) {
						protocol = "https";
					}else if (serviceName.equalsIgnoreCase("http")){
						protocol = "http";
					}else {
						protocol = serviceName;
					}

					entry.setProtocol(protocol);

					if (serviceName.equalsIgnoreCase("http/ssl") || serviceName.equalsIgnoreCase("http")) {
						String host= entryitem.getJSONObject("service").getJSONObject("http").getString("host");
						String server = entryitem.getJSONObject("service").getJSONObject("http").getString("server");
						String title = entryitem.getJSONObject("service").getJSONObject("http").getString("title");

						entry.setHost(host);
						entry.setWebcontainer(server);
						entry.setTitle(title);
					}else {
						entry.setHost(entryitem.getString("domain"));
					}

					entry.setSource(getEngineName());
					result.add(entry);
				}
			}
		} catch (Exception e) {
			e.printStackTrace(BurpExtender.getStderr());
		}
		return result;
	}

	@Override
	public boolean hasNextPage(String respbody,int currentPage) {
		// "size":83,"page":1,
		try {
			int size = 500;
			ArrayList<String> result = JSONHandler.grepValueFromJson(respbody, "total");
			if (result.size() >= 1) {
				int total = Integer.parseInt(result.get(0));
				if (total > currentPage * size) {//size=500
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace(BurpExtender.getStderr());
		}
		return false;
	}

	@Override
	public String buildSearchUrl(String searchContent, int page) {
		return "https://quake.360.net/api/v3/search/quake_service";
	}

	@Override
	public byte[] buildRawData(String searchContent, int page) {
		searchContent = URLEncoder.encode(searchContent);
		String key = ConfigPanel.textFieldQuakeAPIKey.getText();
		int size = 500;
		int start = size*(page-1); 
		String raw = "POST /api/v3/search/quake_service HTTP/1.1\r\n" + "Host: quake.360.net\r\n"
				+ "User-Agent: curl/7.81.0\r\n" + "Accept: */*\r\n" + "X-Quaketoken: %s\r\n"
				+ "Content-Type: application/json\r\n" + "Connection: close\r\n" + "\r\n"
				+ "{\"query\": \"domain:%s\", \"start\": %s, \"size\": %s}";
		raw = String.format(raw, key, searchContent,start,size);
		return raw.getBytes();
	}

}
