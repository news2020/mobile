package pc.bean;

import java.util.List;

public class ExOutputBean {

	private String phoneNum;
	private List<CampnInfoRecord> campnInfoRecord;
	private List<UserProfileRecord> userProfileRecord;
	public String getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	public List<CampnInfoRecord> getCampnInfoRecord() {
		return campnInfoRecord;
	}
	public void setCampnInfoRecord(List<CampnInfoRecord> campnInfoRecord) {
		this.campnInfoRecord = campnInfoRecord;
	}
	public List<UserProfileRecord> getUserProfileRecord() {
		return userProfileRecord;
	}
	public void setUserProfileRecord(List<UserProfileRecord> userProfileRecord) {
		this.userProfileRecord = userProfileRecord;
	}
}
