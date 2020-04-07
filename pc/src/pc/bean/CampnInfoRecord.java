package pc.bean;

public class CampnInfoRecord {

	private String offerId;
	private String offerName;
	private String desc;
	private String offerType;
	private CampnInfoeventAttr eventAttrMapList;
	public String getOfferId() {
		return offerId;
	}
	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}
	public String getOfferName() {
		return offerName;
	}
	public void setOfferName(String offerName) {
		this.offerName = offerName;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getOfferType() {
		return offerType;
	}
	public void setOfferType(String offerType) {
		this.offerType = offerType;
	}
	public CampnInfoeventAttr getEventAttrMapList() {
		return eventAttrMapList;
	}
	public void setEventAttrMapList(CampnInfoeventAttr eventAttrMapList) {
		this.eventAttrMapList = eventAttrMapList;
	}

}
