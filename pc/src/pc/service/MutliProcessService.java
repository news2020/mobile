package pc.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
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

public class MutliProcessService implements Runnable {

	private List<String> phones = new ArrayList<>();
	private List<ExOutputBean> result = new ArrayList<>();
	private final Logger logger = LoggerFactory.getLogger(MutliProcessService.class);
	private String campnInfoUrl;
	private String userProfileUrl;
	private String loginUrl;
	private String appIdG;
	private String accessToken;
	private String pubArgs;
	private String userId;
	private String orgCode;
	private CountDownLatch doneSignal;
	private String remoteServerUrl;
	private List<String> errorPhones = new ArrayList<>();
	private CloseableHttpClient httpClient;
	private String requestData = "{\"header\" :{\"user\" : 18722259789, \"telphone\" : TXXXXXXXXX, \"logicAction\" : \"LXXXXXXXXX\", \"orgCode\" : \"OXXXXXXXXX\"}, \"body\":BXXXXXXXXX}";
	
	public MutliProcessService(String appIdG, String userId, String accessToken, String pubArgs,
		String campnInfoUrl, String userProfileUrl, String orgCode, String remoteServerUrl, CountDownLatch doneSignal, CloseableHttpClient httpClient, String loginUrl) {
		this.campnInfoUrl = campnInfoUrl;
		this.userProfileUrl = userProfileUrl;
		this.appIdG = appIdG;
		this.accessToken = accessToken;
		this.pubArgs = pubArgs;
		this.userId = userId;
		this.doneSignal = doneSignal;
		this.orgCode = orgCode;
		this.remoteServerUrl = remoteServerUrl;
		this.httpClient = httpClient;
		this.loginUrl = loginUrl;
	}
	
	public List<ExOutputBean> getResult() {
		return result;
	}

	public void setPhones(List<String> phones) {
		this.phones = phones;
	}

	@Override
	public void run() {
		logger.info("MutliProcessService start..................");
		if (httpClient == null) {
			logger.warn("Lack httpClient instance..................");
			return;
		}
		try {
			for (String phone : phones) {
				logger.trace("{} are getting data..............", phone);
				/* comment temp
				logger.info("{} read CampnInfo.", phone);
				try {
					String campJson = getCampnInfoRemote(phone, appIdG, userId, accessToken, pubArgs);
					if (StringUtils.equals("504error", campJson)) {
						logger.info("ReLogin for {} CampnInfoRemote...................", phone);
						reLogin(appIdG, userId, "guo_yakun", "DZqIcqwNYkM4x+r/EsnFGQ==", accessToken, pubArgs);
						campJson = getCampnInfoRemote(phone, appIdG, userId, accessToken, pubArgs);
						if (StringUtils.equals("504error", campJson)) {
							errorPhones.add(phone);
							logger.error("{} is 504error in CampnInfo.", phone);
						}
						
					} else {
						String resp = sendData(remoteServerUrl, requestData.replace("TXXXXXXXXX", phone).replace("LXXXXXXXXX", "offer")
							.replace("OXXXXXXXXX", orgCode).replace("BXXXXXXXXX", campJson));
						logger.trace(resp);
					}
					
				} catch (Exception e) {
					errorPhones.add(phone);
					logger.error(phone + " is error in CampnInfo.", e);
					e.printStackTrace();
				}
				*/

				logger.info("{} read UserProfile.", phone);
				try {
					String userProfileJson = getUserProfileRemote(phone, appIdG, userId, accessToken, pubArgs);
					if (StringUtils.equals("504error", userProfileJson)) {
						logger.info("ReLogin for {} UserProfileRemote...................", phone);
						reLogin(appIdG, userId, "guo_yakun", "DZqIcqwNYkM4x+r/EsnFGQ==", accessToken, pubArgs);
						userProfileJson = getUserProfileRemote(phone, appIdG, userId, accessToken, pubArgs);
						if (StringUtils.equals("504error", userProfileJson)) {
							errorPhones.add(phone);
							logger.error("{} is 504error in UserProfile.", phone);
						}
					} else {
						String resp = sendData(remoteServerUrl, requestData.replace("TXXXXXXXXX", phone).replace("LXXXXXXXXX", "detail")
								.replace("OXXXXXXXXX", orgCode).replace("BXXXXXXXXX", userProfileJson));
						logger.trace(resp);
					}
					
				} catch (Exception e) {
					errorPhones.add(phone);
					logger.error(phone + " is error in UserProfile.", e);
					e.printStackTrace();
				}
				
//				logger.info("{} read UserProfile.", phone);
				/* 
				logger.trace("{} are getting data..............", phone);
				ExOutputBean exBean = new ExOutputBean();
				logger.info("{} read CampnInfo.", phone);
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
				result.add(exBean);
				*/
			}
			doneSignal.countDown();
			logger.info("MutliProcessService end..................");
		} catch (Exception ex) {
			logger.error("MutliProcessService: ", ex);
		}
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
//		logger.trace(jsonResp);
		CampnInfoResponse camResp = mapper.readValue(jsonResp, CampnInfoResponse.class);
		return camResp;
	}
	
	private String getCampnInfoRemote(String serialNumber, String appId, String userId, String accessToken, String pubArgs)  throws IOException {
		CampnInfoRequest req = new CampnInfoRequest();
		req.setAppId(appId);
		req.setSerialNumber(serialNumber);
		req.setUserId(userId);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String jsonReq = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(req);
		logger.trace(jsonReq);
		String jsonResp = request(campnInfoUrl, jsonReq, accessToken, pubArgs);
//		logger.trace(jsonResp);
		return jsonResp;
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
//		logger.trace(jsonResp);
		UserProfileResponse userProfileResp = mapper.readValue(jsonResp, UserProfileResponse.class);
		return userProfileResp;
	}
	
	private String getUserProfileRemote(String telNum, String appId, String userId, String accessToken, String pubArgs) throws IOException {
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
//		logger.trace(jsonResp);
		return jsonResp;
	}
	
	private boolean reLogin(String appId, String userId, String account, String password, String accessToken, String pubArgs) throws IOException {
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
	
	private String request(String url, String jsonReqStr, String accessToken, String pubArgs) throws IOException {
		return HttpClientUtil.getDataSyn(url, jsonReqStr, accessToken, pubArgs, httpClient);
	}

	private String sendData(String url, String jsonStr) throws IOException {
		logger.debug("Send to remote json {}", jsonStr);
		return HttpClientUtil.sendRequest(url, jsonStr, httpClient);
	}

	public List<String> getErrorPhones() {
		return errorPhones;
	}

}
