package pc.bean;

import java.util.List;

public class UserProfileRecord {

	private String itemTypeValue;
	private List<UserProfileItem> itemList;

	public List<UserProfileItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<UserProfileItem> itemList) {
		this.itemList = itemList;
	}

	public String getItemTypeValue() {
		return itemTypeValue;
	}

	public void setItemTypeValue(String itemTypeValue) {
		this.itemTypeValue = itemTypeValue;
	}
}
