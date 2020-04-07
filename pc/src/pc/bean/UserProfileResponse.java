package pc.bean;

import java.util.List;

public class UserProfileResponse {

	private String returnCode;
	private String message;
	private List<UserProfileRecord> records;
	public String getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public List<UserProfileRecord> getRecords() {
		return records;
	}
	public void setRecords(List<UserProfileRecord> records) {
		this.records = records;
	}
}
