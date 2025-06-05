package com.medlife.qa.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import com.medlife.qa.controller.ContextManager;
import com.medlife.qa.dataprovider.util.NewMongoDbUtil;
import com.medlife.qa.dataprovider.util.TestDataFactory;
import com.medlife.qa.dbutils.DatabaseUtil;

public class FulfilmentCenter {

	static Logger logger = LogManager.getLogger(FulfilmentCenter.class.getName());
	
	public static String cxUsername = ContextManager.getGlobalContext().getSuiteParameter("cxUsername");
	public static String fcUserName = ContextManager.getGlobalContext().getSuiteParameter("fcUserName");
	public static String dlUserName = ContextManager.getGlobalContext().getSuiteParameter("dlUserName");
	public static String aeUserName = ContextManager.getGlobalContext().getSuiteParameter("aeUserName");
	public static String password = ContextManager.getGlobalContext().getSuiteParameter("password");

	public  void setFulfilmentCenterDetails(TestDataFactory dataFactory) {
		String fcid = (dataFactory.getTestCaseParameters().optString("fcid") != null)
				&& (dataFactory.getTestCaseParameters().optString("fcid") == "")
						? ContextManager.getGlobalContext().getSuiteParameter("fcid")
						: dataFactory.getTestCaseParameters().optString("fcid");
		/**
		 * @author ankuma(Anil kumar)
		 * Code checks enableLocationGuidance keyword in Test Parameter if not not found it takes from Parameter.XMl file
		 */
		String enableLocationGuidance = (dataFactory.getTestCaseParameters().optString("enableLocationGuidance") != null)
				&& (dataFactory.getTestCaseParameters().optString("enableLocationGuidance") == "")
				? ContextManager.getGlobalContext().getSuiteParameter("enableLocationGuidance")
			    : dataFactory.getTestCaseParameters().optString("enableLocationGuidance");
		logger.debug("FcId ==> " + fcid);
		dataFactory.setFcId(fcid);
		dataFactory.setCXUsername(cxUsername);
		dataFactory.setFCUsername(dataFactory.getFcId() + fcUserName);
		dataFactory.setDLUsername(dataFactory.getFcId() + dlUserName);
		dataFactory.setAEUsername(dataFactory.getFcId() + aeUserName);
		dataFactory.setPassword(password);
		dataFactory.setLocationGuidanceStatus(enableLocationGuidance);
		new DatabaseUtil().getCityByfcId(dataFactory);
		new DatabaseUtil().getStateByfcId(dataFactory);
		String fcType = new DatabaseUtil().getFCTypeByfcId(dataFactory);
		dataFactory.setFCType(fcType);
		setCorporateId(dataFactory);
		new NewMongoDbUtil().applyPromocode(dataFactory);
	}

	// Set Corporate accounts with CorpId & CorpDiscount for CORPORATE_RX & CORP_RX
	public  String setCorporateId(TestDataFactory dataFactory) {
		JSONObject data = dataFactory.getTestCaseParameters();
		if (dataFactory.getRxSource().equalsIgnoreCase("CORPORATE_RX")
				|| dataFactory.getRxSource().equalsIgnoreCase("CORP_RX")) {
			boolean isCreditEligible = data.optBoolean("isCreditEligible");
			boolean isCreditOrder = data.optBoolean("creditOrder");
			if (dataFactory.getCity().equalsIgnoreCase("Bangalore")) {
				if ((isCreditEligible && isCreditOrder) || (isCreditEligible && !isCreditOrder)) {
					dataFactory.setCorpId("CORP02A");
					new DatabaseUtil().isCreditSalesEligible(dataFactory);
				} else {
					dataFactory.setCorpId("CORP01A");
				}
			}
			if (dataFactory.getCity().equalsIgnoreCase("Mumbai")) {
				if ((isCreditEligible && isCreditOrder) || (isCreditEligible && !isCreditOrder)) {
					dataFactory.setCorpId("CORP04A");
					new DatabaseUtil().isCreditSalesEligible(dataFactory);
				} else {
					dataFactory.setCorpId("CORP03A");
				}
			}
			if (dataFactory.getCity().equalsIgnoreCase("Pune")) {
				if ((isCreditEligible && isCreditOrder) || (isCreditEligible && !isCreditOrder)) {
					dataFactory.setCorpId("CORP06A");
					new DatabaseUtil().isCreditSalesEligible(dataFactory);
				} else {
					dataFactory.setCorpId("CORP05A");
				}
			}
			if (dataFactory.getCity().equalsIgnoreCase("Bhopal")) {
				if ((isCreditEligible && isCreditOrder) || (isCreditEligible && !isCreditOrder)) {
					dataFactory.setCorpId("CORP08A");
					new DatabaseUtil().isCreditSalesEligible(dataFactory);
				} else {
					dataFactory.setCorpId("CORP07A");
				}
			}
			if (dataFactory.getCity().equalsIgnoreCase("Varanasi")) {
				if ((isCreditEligible && isCreditOrder) || (isCreditEligible && !isCreditOrder)) {
					dataFactory.setCorpId("CORP12A");
					new DatabaseUtil().isCreditSalesEligible(dataFactory);
				} else {
					dataFactory.setCorpId("CORP11A");
				}
			}
			logger.debug("Corporate Id ==> " + dataFactory.getCorpId());
			new DatabaseUtil().getCorpDiscount(dataFactory);
			return dataFactory.getCorpId();
		}
		return "";
	}
}
