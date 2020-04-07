package pc.bean;

import java.util.List;

public class CampnInfoResponse {

	private String returnCode;
	private String message;
	private List<CampnInfoRecord> records;
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
	public List<CampnInfoRecord> getRecords() {
		return records;
	}
	public void setRecords(List<CampnInfoRecord> records) {
		this.records = records;
	}
}
