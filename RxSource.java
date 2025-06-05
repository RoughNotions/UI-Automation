package com.medlife.qa.configuration;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.medlife.qa.dataprovider.util.NewMongoDbUtil;
import com.medlife.qa.dataprovider.util.TestDataFactory;
import com.medlife.qa.dbutils.DatabaseUtil;

public class RxSource {
	
	static Logger logger = LogManager.getLogger(RxSource.class.getName());
	public static final List<String> rxSourceList = Arrays.asList("GO_RX", "CORP_RX", "GO_RX_CTO", "SLATE_RX", "ZIP_RX");

	// Get whether rx source is Manual Rx or not 
	// ManulaRx = true ==> DraftRx will be created after Rx submission 
	// ManualRx = false ==> DraftRx [with READY_FOR_DIGITIZATION status] will be pushed to Pending Q in Digidesk app
	public static void isManualRx(TestDataFactory dataFactory) {
		boolean manualRx = false; 
		String source = dataFactory.getTestCaseParameters().optString("source");
		dataFactory.setRxSource(source);
		logger.debug("RxSource ==> " + dataFactory.getRxSource());
			switch (dataFactory.getRxSource()) {
			case "GO_RX": case "CORP_RX": case "SLATE_RX": case "EXTERNAL_RX": case "ONECLICK_DOCTOR_RX":
				manualRx=false;
				break;
			case "GO_RX_CTO": case "WALKIN_RX": case "YM_RX": case "TATA_RX": case "MEDIDAILI_RX":
			case "INAYO_RX": case "ZYWEE_RX": case "BROWNPACKET_RX": case "MADMORNING_RX": case "CORPORATE_RX":
				manualRx=true;
				break;
			default:
				manualRx=true;
				break;
			} logger.debug(source+" isManualRx ? ==> "+manualRx);
		dataFactory.setIsManualRx(manualRx);
	}
}
