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
	private static String sheet1Name = "Ӫ��";
	private static String sheet2Name = "�ͻ�";
	private static String sheet3Name = "�Ż�";
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
					titlCell.setCellValue("����" + (j + 1));
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
					// �Ż���Ϣ
					if (StringUtils.containsAny(record.getItemTypeValue(), "�Ż���Ϣ")) {
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
		sh2TitRow.createCell(1).setCellValue("���䣨�£�");
		sh2TitRow.createCell(2).setCellValue("�ͻ��Ǽ�");
		sh2TitRow.createCell(3).setCellValue("��ΣЯת�û�");
		sh2TitRow.createCell(4).setCellValue("����Яת�û�");
		sh2TitRow.createCell(5).setCellValue("�ն�Ʒ��");
		sh2TitRow.createCell(6).setCellValue("�ն��ͺ�");
		sh2TitRow.createCell(7).setCellValue("����ARPU(Ԫ)");
		sh2TitRow.createCell(8).setCellValue("��������ƽ��ARPU(Ԫ)");
		sh2TitRow.createCell(9).setCellValue("����DOU��M��");
		sh2TitRow.createCell(10).setCellValue("��������ƽ��DOU");
		sh2TitRow.createCell(11).setCellValue("����MOU�����ӣ�");
		sh2TitRow.createCell(12).setCellValue("��������ƽ��MOU");
		sh2TitRow.createCell(13).setCellValue("�����������׽��");
		sh2TitRow.createCell(14).setCellValue("�����������׽��");
		sh2TitRow.createCell(15).setCellValue("�Ƿ���ںϿ��");
		sh2TitRow.createCell(16).setCellValue("UE-MR���ʶ��_�û���פ��������");
//		sh2TitRow.createCell(17).setCellValue("�Ż�1");
//		sh2TitRow.createCell(18).setCellValue("�Ż�2");
//		sh2TitRow.createCell(19).setCellValue("�Ż�3");
//		sh2TitRow.createCell(20).setCellValue("�Ż�4");
//		sh2TitRow.createCell(21).setCellValue("�Ż�5");
//		sh2TitRow.createCell(22).setCellValue("�Ż�6");
//		sh2TitRow.createCell(23).setCellValue("�Ż�7");
//		sh2TitRow.createCell(24).setCellValue("�Ż�8");
//		sh2TitRow.createCell(25).setCellValue("�Ż�9");
//		sh2TitRow.createCell(26).setCellValue("�Ż�10");
//		sh2TitRow.createCell(27).setCellValue("�Ż�11");
//		sh2TitRow.createCell(28).setCellValue("�Ż�12");
//		sh2TitRow.createCell(29).setCellValue("�Ż�13");
	}
	
	private static void initdiscountHeader(Row sh3TitRow) {
		sh3TitRow.createCell(0).setCellValue("�绰");
		sh3TitRow.createCell(1).setCellValue("��ֹʱ��");
		sh3TitRow.createCell(2).setCellValue("�Ż�");
	}
	
	private static int getCellColumn(String label) {
		switch (label) {
		case "���䣨�£�":
			return 1;
		case "�ͻ��Ǽ�":
			return 2;
		case "��ΣЯת�û�":
			return 3;
		case "����Яת�û�":
			return 4;
		case "�ն�Ʒ��":
			return 5;
		case "�ն��ͺ�":
			return 6;
		case "����ARPU(Ԫ)":
			return 7;
		case "��������ƽ��ARPU(Ԫ)":
			return 8;
		case "����DOU��M��":
			return 9;
		case "��������ƽ��DOU":
			return 10;
		case "����MOU�����ӣ�":
			return 11;
		case "��������ƽ��MOU":
			return 12;
		case "�����������׽��":
			return 13;
		case "�����������׽��":
			return 14;
		case "�Ƿ���ںϿ��":
			return 15;
		case "UE-MR���ʶ��_�û���פ��������":
			return 16;
		}
		return 17;
	}
}
