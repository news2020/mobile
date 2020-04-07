package pc.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pc.bean.CampnInfoRecord;
import pc.bean.ExOutputBean;
import pc.bean.UserProfileItem;
import pc.bean.UserProfileRecord;

public class ExOuputService {

	private static final Logger logger = LoggerFactory.getLogger(ExOuputService.class);
	private static String sheet1Name = "营销";
	private static String sheet2Name = "客户";
	private static String sheet3Name = "优惠";
//	private static int extendCount = 0;
	public static void exportToExcel(List<ExOutputBean> items, File filePath) throws IOException {
		logger.trace("exportToExcel method: Output file path {} ", filePath.getAbsoluteFile());
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		File file = new File(filePath.getAbsolutePath() + File.separator + "Work" + sdf.format(date) + ".xlsx");
//		if (file.exists()) {
//			file = new File(filePath.getAbsolutePath() + File.separator + "Work" + extendCount + ".xlsx");
//		}
		Workbook wb = new XSSFWorkbook();
		Sheet sheet1 = wb.createSheet(sheet1Name);
		Sheet sheet2 = wb.createSheet(sheet2Name);
		Sheet sheet3 = wb.createSheet(sheet3Name);
		Row sh1TitRow = sheet1.createRow(0);
		Row sh2TitRow = sheet2.createRow(0);
		Row sh3TitRow = sheet3.createRow(0);
		initUserProfileHeader(sh2TitRow);
		initdiscountHeader(sh3TitRow);
		int sh3StartIndex = 1;
		for (int i = 0; i < items.size(); i++) {
			ExOutputBean item = items.get(i);
			Row sh1Row = sheet1.createRow(i + 1);
			Row sh2Row = sheet2.createRow(i + 1);
			Cell sj1Cell = sh1Row.createCell(0);
			sj1Cell.setCellValue(item.getPhoneNum());
			Cell sj2Cell = sh2Row.createCell(0);
			sj2Cell.setCellValue(item.getPhoneNum());
			if (item.getCampnInfoRecord() == null) {
				logger.info("{} lack the CampnInfo Record...................", item.getPhoneNum());
			} else {
				for (int j = 0; j < item.getCampnInfoRecord().size(); j++) {
					CampnInfoRecord record = item.getCampnInfoRecord().get(j);
					Cell titlCell = sh1TitRow.createCell(j + 1);
					titlCell.setCellValue("弹窗" + (j + 1));
					Cell campCell = sh1Row.createCell(j + 1);
					campCell.setCellValue(record.getOfferName() + ";" + record.getDesc() + ";" + record.getEventAttrMapList().getCampaignName()
						+ ";" + record.getEventAttrMapList().getMarketingInfo());
				}
			}
			
//			int start = 0;
			if (item.getUserProfileRecord() == null) {
				logger.info("{} lack the UserProfile Record...................", item.getPhoneNum());
			} else {
				for (int j = 0; j < item.getUserProfileRecord().size(); j++) {
					UserProfileRecord record = item.getUserProfileRecord().get(j);
					// 优惠信息
					if (StringUtils.containsAny(record.getItemTypeValue(), "优惠信息")) {
						List<UserProfileItem> disaccountItemL = record.getItemList();
						if (disaccountItemL == null) {
							logger.info("{} lack the disaccount inform...................", item.getPhoneNum());
							continue;
						}
						for (int u = 0; u < disaccountItemL.size(); u++) {
							UserProfileItem disaccountItem = disaccountItemL.get(u);
							Row sh3ItemRow = sheet3.createRow(u+sh3StartIndex);
							sh3ItemRow.createCell(0).setCellValue(item.getPhoneNum());
							sh3ItemRow.createCell(1).setCellValue(disaccountItem.getItemValue());
							sh3ItemRow.createCell(2).setCellValue(disaccountItem.getItemLableName());
						}
						sh3StartIndex = sh3StartIndex + disaccountItemL.size();
					} else {
						if (record.getItemList() == null) {
							logger.info("{} lack the {} inform...................", item.getPhoneNum(), record.getItemTypeValue());
							continue;
						}
						for (int z = 0; z < record.getItemList().size(); z++) {
							UserProfileItem upItem = record.getItemList().get(z);
							int index = getCellColumn(upItem.getItemLableName());
							Cell usCell = sh2Row.createCell(index);
							usCell.setCellValue(upItem.getItemValue());
							/*
							if (index == 17) {
								usCell = sh2Row.createCell(index + start);
								usCell.setCellValue(upItem.getItemLableName() + ":" + upItem.getItemValue());
								start++;
							} else {
								usCell = sh2Row.createCell(index);
								usCell.setCellValue(upItem.getItemValue());
							}
							*/
						}
					}

					// start = start + record.getItemList().size();
				}
			}
		}
		logger.trace("exportToExcel data exchanges completed....................");
		try {
			FileOutputStream fileOut = new FileOutputStream(file);
		    wb.write(fileOut);
		    fileOut.flush();
		    fileOut.close();
		} finally {
			wb.close();
		}
	}
	
	private static void initUserProfileHeader(Row sh2TitRow) {
		sh2TitRow.createCell(1).setCellValue("网龄（月）");
		sh2TitRow.createCell(2).setCellValue("客户星级");
		sh2TitRow.createCell(3).setCellValue("高危携转用户");
		sh2TitRow.createCell(4).setCellValue("正在携转用户");
		sh2TitRow.createCell(5).setCellValue("终端品牌");
		sh2TitRow.createCell(6).setCellValue("终端型号");
		sh2TitRow.createCell(7).setCellValue("上月ARPU(元)");
		sh2TitRow.createCell(8).setCellValue("近三个月平均ARPU(元)");
		sh2TitRow.createCell(9).setCellValue("上月DOU（M）");
		sh2TitRow.createCell(10).setCellValue("近三个月平均DOU");
		sh2TitRow.createCell(11).setCellValue("上月MOU（分钟）");
		sh2TitRow.createCell(12).setCellValue("近三个月平均MOU");
		sh2TitRow.createCell(13).setCellValue("上月流量超套金额");
		sh2TitRow.createCell(14).setCellValue("当月语音超套金额");
		sh2TitRow.createCell(15).setCellValue("是否绑定融合宽带");
		sh2TitRow.createCell(16).setCellValue("UE-MR软采识别_用户常驻居民名称");
//		sh2TitRow.createCell(17).setCellValue("优惠1");
//		sh2TitRow.createCell(18).setCellValue("优惠2");
//		sh2TitRow.createCell(19).setCellValue("优惠3");
//		sh2TitRow.createCell(20).setCellValue("优惠4");
//		sh2TitRow.createCell(21).setCellValue("优惠5");
//		sh2TitRow.createCell(22).setCellValue("优惠6");
//		sh2TitRow.createCell(23).setCellValue("优惠7");
//		sh2TitRow.createCell(24).setCellValue("优惠8");
//		sh2TitRow.createCell(25).setCellValue("优惠9");
//		sh2TitRow.createCell(26).setCellValue("优惠10");
//		sh2TitRow.createCell(27).setCellValue("优惠11");
//		sh2TitRow.createCell(28).setCellValue("优惠12");
//		sh2TitRow.createCell(29).setCellValue("优惠13");
	}
	
	private static void initdiscountHeader(Row sh3TitRow) {
		sh3TitRow.createCell(0).setCellValue("电话");
		sh3TitRow.createCell(1).setCellValue("起止时间");
		sh3TitRow.createCell(2).setCellValue("优惠");
	}
	
	private static int getCellColumn(String label) {
		switch (label) {
		case "网龄（月）":
			return 1;
		case "客户星级":
			return 2;
		case "高危携转用户":
			return 3;
		case "正在携转用户":
			return 4;
		case "终端品牌":
			return 5;
		case "终端型号":
			return 6;
		case "上月ARPU(元)":
			return 7;
		case "近三个月平均ARPU(元)":
			return 8;
		case "上月DOU（M）":
			return 9;
		case "近三个月平均DOU":
			return 10;
		case "上月MOU（分钟）":
			return 11;
		case "近三个月平均MOU":
			return 12;
		case "上月流量超套金额":
			return 13;
		case "当月语音超套金额":
			return 14;
		case "是否绑定融合宽带":
			return 15;
		case "UE-MR软采识别_用户常驻居民名称":
			return 16;
		}
		return 17;
	}
}
