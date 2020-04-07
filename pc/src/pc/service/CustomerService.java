package pc.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import pc.bean.CampnInfoRequest;
import pc.bean.CampnInfoResponse;
import pc.bean.ExOutputBean;
import pc.bean.LoginBean;
import pc.bean.LoginRespBean;
import pc.bean.UserProfileRequest;
import pc.bean.UserProfileResponse;
import pc.util.HttpClientUtil;

public class CustomerService extends SwingWorker<String, Integer> {

	private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
	private static String loginUrl  = "http://h5.sd.chinamobile.com/cmtj/login/app/common/refreshToken";
	private static String campnInfoUrl  = "http://h5.sd.chinamobile.com/cmtj/openingCard/consumer/campnInfo/getCampnInfo";
	private static String userProfileUrl = "http://h5.sd.chinamobile.com/cmtj/shake/server/userProfile/userProfile";
	private static String appIdG = "100000056";
	private File inputFile;
	private File outputPath;
	private String password;
	private String accessToken;
	private String pubArgs;
	private String userId;
	private String userName;
	// remote
	private String syn;
	private int threadCount = 1;
	private String remoteServerUrl;
	private String reLoginJson = "{\"header\" :{\"user\" : #########, \"logicAction\" : \"login\"}}";
	private final String placeholder = "#########";
	private String remoteUser;
	
	@Override
	protected String doInBackground() throws Exception {
		List<String> phones = ImportPhoneService.readFromFile(inputFile);
		if (StringUtils.isBlank(syn)) {
			if (phones.size() > 2000) {
				logger.error("Not support so much data");
				return "Not support so much data";
			}
		}
		List<ExOutputBean> exList = new ArrayList<>();
		boolean logined;
		try {
			logined = login(appIdG, userId, userName, password, accessToken, pubArgs);
		} catch (IOException e) {
			logger.error("Login error", e);
			return "Login error";
		}
		
		if (logined) {
			if (StringUtils.isBlank(syn)) {
//				int count = 0;
				int i = 0;
				for (String phone : phones) {
					logger.trace(phone + " are getting data..............");
					ExOutputBean exBean = new ExOutputBean();
					logger.info(phone + " read CampnInfo.");
					CampnInfoResponse camp = null;
					try {
						camp = getCampnInfo(phone, appIdG, userId, accessToken, pubArgs);
					} catch (IOException e) {
						logger.error(phone + " is error in getCampnInfo method.", e);
						e.printStackTrace();
					}
					logger.info(phone + " read UserProfile.");
					UserProfileResponse userProfile = null;
					try {
						userProfile = getUserProfile(phone, appIdG, userId, accessToken, pubArgs);
					} catch (IOException e) {
						logger.error(phone + " is error in getUserProfile method.", e);
						e.printStackTrace();
					}
					exBean.setPhoneNum(phone);
					if (camp != null) {
						exBean.setCampnInfoRecord(camp.getRecords());
					}
					if (userProfile != null) {
						exBean.setUserProfileRecord(userProfile.getRecords());
					}
					exList.add(exBean);
					i++;

	                publish(i * 200/phones.size());
				}
				logger.info("Export data to Excel......................");
				try {
					ExOuputService.exportToExcel(exList, outputPath);
				} catch (Exception e) {
					logger.error("Export data error.....................", e);
					e.printStackTrace();
					return "导出文件错误";
				}
				logger.info("Export data to Excel Finished......................");
			} else {
				// mutli process
				String resultStr = mutilProcess(phones);
				publish(200);
				return resultStr;
			}
			
		} else {
			logger.error("Login failed");
			return "登录失败";
		}
		publish(200);
		return "获取完成";
	}
	
	private String mutilProcess(List<String> phones) throws Exception {
		logger.info("In Mutli threads way......................");
		// Login to the remote server
		String orgCode = "";
		CloseableHttpClient loginHttpClient = HttpClients.createDefault();
		try {
			String loginResp = loginRemote(remoteServerUrl, reLoginJson.replace(placeholder, remoteUser), loginHttpClient);
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> tmpMap = mapper.readValue(loginResp, Map.class);
			orgCode = (String) tmpMap.get("orgCode");
		} catch (Exception e) {
			logger.error("Login remote server error.....................", e);
			e.printStackTrace();
		} finally {
			loginHttpClient.close();
		}
		if (StringUtils.isBlank(orgCode)) {
			return "登录远程错误";
		}
		List<List<String>> mutliPhones = new ArrayList<>();
		int subCount = phones.size() / threadCount;
		for (int i = 0; i < threadCount; i++) {
			List<String> subList = null;
			if (i == threadCount - 1) {
				subList = phones.subList(i * subCount, phones.size());
			} else {
				subList = phones.subList(i * subCount, (i * subCount) + subCount);
			}
			mutliPhones.add(subList);
		}
		// create a connection pool
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		// Increase max total connection to 200
		cm.setMaxTotal(threadCount * 3);
		// Increase default max connection per route to 40
		cm.setDefaultMaxPerRoute(threadCount + 20);
		RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(60000)                     // 设置连接超时
                .setSocketTimeout(300000)                       // 设置读取超时
                .setConnectionRequestTimeout(600000)   // 设置从连接池获取连接实例的超时
                .build();
		HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception,
                    int executionCount, HttpContext context) {
                if (executionCount >= 3) {// 如果已经重试了5次，就放弃
                	logger.error("Output error file down.....................", exception);
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                    return false;
                }
                if (exception instanceof InterruptedIOException) {// 超时
                    return false;
                }
                if (exception instanceof UnknownHostException) {// 目标服务器不可达
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {
                    return true;
                }
                if (exception instanceof SSLException) {// SSL
                    return false;
                }
 
                HttpClientContext clientContext = HttpClientContext
                        .adapt(context);
                HttpRequest request = clientContext.getRequest();
                // 如果请求是幂等的，就再次尝试
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            }
        };
		
		CountDownLatch doneSignal = new CountDownLatch(threadCount);
		// create service
		List<MutliProcessService> mutliservice = new ArrayList<>();
		List<CloseableHttpClient> httpClients = new ArrayList<>();
		int counts = 1;
		for (List<String> subPhones : mutliPhones) {
			CloseableHttpClient httpclient = HttpClients.custom()
	                .setConnectionManager(cm)
	                .setDefaultRequestConfig(requestConfig)
//	                .setRetryHandler(httpRequestRetryHandler)
	                .build();
			MutliProcessService serivce = new MutliProcessService(appIdG, userId, accessToken, pubArgs,
				campnInfoUrl, userProfileUrl, orgCode, remoteServerUrl, doneSignal, httpclient, loginUrl);
			logger.info("Thread " + counts + " size: " + subPhones.size());
			serivce.setPhones(subPhones);
			mutliservice.add(serivce);
			counts++;
		}
		// create thread and start
		ThreadPoolExecutor pool = new ThreadPoolExecutor(threadCount + 10, threadCount + 30, 1000, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(),Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());
		for (MutliProcessService service : mutliservice) {
			pool.execute(service);
		}
		IdleConnectionMonitorThread idlThread = new IdleConnectionMonitorThread(cm);
		idlThread.start();
		doneSignal.await();
		for (CloseableHttpClient httpclient : httpClients) {
			httpclient.close();
		}
		idlThread.shutdown();
		cm.close();
		List<String> allErrorPhones = new ArrayList<>();
		for (MutliProcessService service : mutliservice) {
			allErrorPhones.addAll(service.getErrorPhones());
		}
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		File file = new File(outputPath + File.separator + "Error" + sdf.format(date) + ".txt");
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file);
			for (String errorPhone : allErrorPhones) {
				pw.println(errorPhone);
			}
			pw.flush();
		} catch (Exception e) {
			logger.error("Output error file down.....................", e);
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
		return "获取完成";
	}
	
//	public static void process(File inputFile, File outputPath, JProgressBar jProgressBar) throws UserDefineException {
//		
//	}
	

//	private static String userIdG = "1238004473233604610";
//	private static String accessTokenG = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJkZXZpY2VUeXBlIjoiTUkgNVgiLCJzdWIiOiJmZW5nX3FpbmdxdWFuIiwibG9naW5Vc2VybmFtZSI6ImZlbmdfcWluZ3F1YW4iLCJjcmVhdGVkIjoxNTg1MTk1Mzg5MTM4LCJleHAiOjE1ODc3ODczODksImRldmljZUlkIjoiMzU3NjE3Njk1NjU3MjQ3IiwidXNlcklkIjpudWxsfQ.JSqd7fMOeg_tJjnHBFcNYygyLFEKQ0hVmWx4aUnEB5k";
//	private static String accessTokenG = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJkZXZpY2VUeXBlIjoiTUkgNVgiLCJzdWIiOiJ3YW5nX3lhbmcxNyIsImxvZ2luVXNlcm5hbWUiOiJ3YW5nX3lhbmcxNyIsImNyZWF0ZWQiOjE1ODU0ODI0MjQ1MzYsImV4cCI6MTU4ODA3NDQyNCwiZGV2aWNlSWQiOiIzNTc2MTc2OTU2NTcyNDciLCJ1c2VySWQiOm51bGx9.T5nP6w7FYhl0vIRZaHd-dYX4gSc86JGe5UQu9niQumY";
//	private static String pubArgs = "WdfwXerWgEklUoptZ+yC0j7ATMOuR5hyJXN7/272YWZbomUUN9Fs2vkO6W9IntieNZ243DEWIkB1vTDJvBjzW6ESxgXYT4ljMJIsGZlBq7jW1J9YnJ91bElwRU9D3SXRDGk975kqSx7OmeBQj30p3OAxf+IuQ1Ris1TaTTB91ZT4r3KmX88xcwIoAZk6t9d3xAD++/w/zXmmhKglwp3iwCBtQBowkBfhonQ7i3Qv3HgBDp7Oueb0lflsE2ehCrjC+u+h0eK5I6z95bs+7rnXHY3yBzV2FslkKi2KJiRd4vo=";
//	private static String pubArgsG = "WdfwXerWgEklUoptZ+yC0j7ATMOuR5hyJXN7/272YWZbomUUN9Fs2vkO6W9IntieNZ243DEWIkB1vTDJvBjzW7g309IROI3s7P4sx3Tkei+5U6/TJOslVOm7DbhY3nJFCaWytI3oO7sEhhJIc/82/5fJMOpKXXHVJmqBiIUAIYyVss9U8u18s0toB2Uev4HiBs8U/3EJHPzdZEMKA9qVSk0/qkk6idOjP8v9TeoaIwVkOyuic/IsB/itduoZJoNVFyLD+OoXlFTmoG7E8oxeknwTWma2qk93QF0/l8AeQKG5JbnmMz/cTg==";
	private boolean login(String appId, String userId, String account, String password, String accessToken, String pubArgs) throws IOException {
		LoginBean loginB = new LoginBean();
		loginB.setAccessToken(accessToken);
		loginB.setAccount(account);
		loginB.setAppId(appId);
		loginB.setPassword(password);
		loginB.setUserId(userId);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String jsonReq = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(loginB);
		logger.trace(jsonReq);
		String jsonResp = request(loginUrl, jsonReq, accessToken, pubArgs);
		logger.trace(jsonResp);
		LoginRespBean resp = mapper.readValue(jsonResp, LoginRespBean.class);
		if (StringUtils.equalsIgnoreCase("1000", resp.getReturnCode())) {
			return true; 
		}
		return false;
	}
	
	private String loginRemote(String url, String json, CloseableHttpClient httpClient) throws IOException {
		logger.trace(json);
		return HttpClientUtil.sendRequest(url, json, httpClient);
	}
	
	private CampnInfoResponse getCampnInfo(String serialNumber, String appId, String userId, String accessToken, String pubArgs)  throws IOException {
		CampnInfoRequest req = new CampnInfoRequest();
		req.setAppId(appId);
		req.setSerialNumber(serialNumber);
		req.setUserId(userId);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String jsonReq = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(req);
		logger.trace(jsonReq);
		String jsonResp = request(campnInfoUrl, jsonReq, accessToken, pubArgs);
		logger.trace(jsonResp);
		CampnInfoResponse camResp = mapper.readValue(jsonResp, CampnInfoResponse.class);
		return camResp;
	}
	
	private UserProfileResponse getUserProfile(String telNum, String appId, String userId, String accessToken, String pubArgs) throws IOException {
		UserProfileRequest req = new UserProfileRequest();
		req.setAppId(appId);
		req.setIsSplice("1");
		req.setTelNum(telNum);
		req.setUserId(userId);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String jsonReq = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(req);
		logger.trace(jsonReq);
		String jsonResp = request(userProfileUrl, jsonReq, accessToken, pubArgs);
		logger.trace(jsonResp);
		UserProfileResponse userProfileResp = mapper.readValue(jsonResp, UserProfileResponse.class);
		return userProfileResp;
	}
	
	private String request(String url, String jsonReqStr, String accessToken, String pubArgs) throws IOException {
		return HttpClientUtil.getData(url, jsonReqStr, accessToken, pubArgs);
	}

	public static void main(String[] arg) throws Exception {
		if (arg.length != 3) {
			System.out.println("Parameter error");
			return;
		}
		File configFile = new File("config.properties");
		System.out.println(configFile.getAbsolutePath());
		logger.info("Load config.properties: " + configFile.getAbsolutePath());
		InputStream in = null;
		Properties p;
		try {
			in = new BufferedInputStream(new FileInputStream(configFile));
			p = new Properties();
			p.load(in);
		} catch (FileNotFoundException e2) {
			logger.error("config.properties load error......", e2);
			System.out.println("Init file lose : " + configFile.getAbsolutePath());
			return;
		} catch (IOException e1) {
			logger.error("config.properties load error......", e1);
			System.out.println("Init error : " + configFile.getAbsolutePath());
			return;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		//init mutli-process
		String syn = p.getProperty("syn");
		String threadCount = p.getProperty("threadCount");
		String remoteServerUrl = p.getProperty("remoteServerUrl");
		String remoteUser = p.getProperty("remoteUser");
		String accessToken = p.getProperty(arg[0]+ ".accessToken");
		if (accessToken == null) {
			System.out.println("Lack user : " + arg[0] + "accessToken");
			return;
		}
		String pubArgs = p.getProperty(arg[0] + ".pubArgs");
		if (pubArgs == null) {
			System.out.println("Lack user : " + arg[0] + "pubArgs");
			return;
		}
		String password = p.getProperty(arg[0] + ".password");
		if (password == null) {
			System.out.println("Lack user : " + arg[0] + "password");
			return;
		}
		String userId = p.getProperty(arg[0] + ".userId");
		if (userId == null) {
			System.out.println("Lack user : " + arg[0] + "userId");
			return;
		}
		File inputFile = new File(arg[1]);
		if (!inputFile.exists()) {
			System.out.println("Input File not exists:  " + arg[1]);
			return;
		}
		File outputPath = new File(arg[2]);
		if (!outputPath.isDirectory()) {
			System.out.println("Output File not exists:  " + arg[2]);
			return;
		}
		List<String> phones = ImportPhoneService.readFromFile(inputFile);
		CustomerService task = new CustomerService();
		task.setInputFile(inputFile);
		task.setOutputPath(outputPath);
		task.setAccessToken(accessToken);
		task.setPassword(password);
		task.setPubArgs(pubArgs);
		task.setUserId(userId);
		task.setUserName(arg[0]);
		task.setSyn(syn);
		task.setCyclical(Integer.valueOf(threadCount));
		task.setRemoteUser(remoteUser);
		task.setRemoteServerUrl(remoteServerUrl);
		System.out.println(task.mutilProcess(phones));
	}
	
	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public File getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(File outputPath) {
		this.outputPath = outputPath;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getPubArgs() {
		return pubArgs;
	}

	public void setPubArgs(String pubArgs) {
		this.pubArgs = pubArgs;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSyn() {
		return syn;
	}

	public void setSyn(String syn) {
		this.syn = syn;
	}

	public int getCyclical() {
		return threadCount;
	}

	public void setCyclical(int cyclical) {
		this.threadCount = cyclical;
	}

	public String getRemoteServerUrl() {
		return remoteServerUrl;
	}

	public void setRemoteServerUrl(String remoteServerUrl) {
		this.remoteServerUrl = remoteServerUrl;
	}

	public String getRemoteUser() {
		return remoteUser;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

}
