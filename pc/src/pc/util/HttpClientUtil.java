package pc.util;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientUtil {

	private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
	private static CloseableHttpClient httpClientS = HttpClients.createDefault();
	private static RequestConfig requestConfig = RequestConfig.custom()  
            .setSocketTimeout(400000)  
            .setConnectTimeout(90000)  
            .build();
	public static String getData(String urlStr, String jsonStr, String accessTokenStr, String pubArgsStr) throws IOException {
		return requestData(urlStr, jsonStr, accessTokenStr, pubArgsStr, httpClientS);
	}
	
	public static String getDataSyn(String urlStr, String jsonStr, String accessTokenStr, String pubArgsStr, CloseableHttpClient httpClient) throws IOException {
//		CloseableHttpClient httpClient = HttpClients.createDefault();
		return requestData(urlStr, jsonStr, accessTokenStr, pubArgsStr, httpClient);
	}
	
	public static String sendRequest(String urlStr, String jsonStr, CloseableHttpClient httpClient) throws IOException {
		logger.info("step into sendRequest..........................");
//		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost post = new HttpPost(urlStr);
		StringEntity entity = new StringEntity(jsonStr,"utf-8");
        entity.setContentEncoding("UTF-8");      
        entity.setContentType("application/json");      
        post.setEntity(entity);
        logger.trace(post.toString());
        CloseableHttpResponse  httpResponse = null;
        try {
			httpResponse = httpClient.execute(post);
			String strResult = EntityUtils.toString(httpResponse.getEntity(), "utf-8");   
			logger.trace(strResult);
			return strResult;
		} finally {
			if (httpResponse != null) {
				httpResponse.close();
			}
//			httpClient.close();
		}
	}
	
	private static String requestData(String urlStr, String jsonStr, String accessTokenStr, String pubArgsStr, CloseableHttpClient client) throws IOException {
		logger.trace("step into executePostMethod");
	     String strResult = ""; 
	     HttpPost post = new HttpPost(urlStr);
//	     post.setConfig(requestConfig);
	     post.setHeader("Content-Type", "application/json");
	     post.setHeader("accessToken", accessTokenStr);
	     post.setHeader("pubArgs", pubArgsStr);
	     StringEntity entity = new StringEntity(jsonStr,"utf-8");
        entity.setContentEncoding("UTF-8");      
        entity.setContentType("application/json");      
        post.setEntity(entity);
        logger.trace(post.toString());
        CloseableHttpResponse  httpResponse = null;
        try {
			httpResponse = client.execute(post);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
	        if (statusCode != HttpStatus.SC_OK ) {
//	        	logger.info("wait for 30sec......................");
//	        	
//	        	for (int i = 0; i < 2; i++) {
//	        		try {
//						Thread.currentThread().sleep(10000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//	        		logger.info("re-try json {}", jsonStr);
//	        		CloseableHttpClient httpClientL = HttpClients.createDefault();
//	        		httpResponse = httpClientL.execute(post);
//	        		if (statusCode == HttpStatus.SC_OK ) {
//	        			logger.info("re-try json success {}", jsonStr);
//	        			httpClientL.close();
//	        			return EntityUtils.toString(httpResponse.getEntity(), "utf-8"); 
//	        		}
//	        	}
	        	return "504error";
	        }
			strResult = EntityUtils.toString(httpResponse.getEntity(), "utf-8");   
			logger.trace(strResult);
		} finally {
			if (httpResponse != null) {
				httpResponse.close();
			}
		}
	     return strResult;
	}
	
}
