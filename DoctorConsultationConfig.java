package com.medlife.qa.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import io.restassured.response.Response;
import com.medlife.qa.controller.ContextManager;
import com.medlife.qa.controller.EnvironmentSetup;
import com.medlife.qa.dataprovider.util.NewMongoDbUtil;
import com.medlife.qa.dataprovider.util.TestDataFactory;
import com.medlife.qa.dbutils.DatabaseUtil;
import com.medlife.qa.inventoryutils.InventoryUtil;
import com.medlife.qa.pageutil.NotCurrentPageException;
import com.medlife.service.DigideskAppService;

public class DoctorConsultationConfig {

	static Logger logger = LogManager.getLogger(DoctorConsultationConfig.class.getName());
	public static NewMongoDbUtil mongoDb = new NewMongoDbUtil().getInstance();

	public static EnvironmentSetup envSetupUtil = new EnvironmentSetup().getInstance();
	static String env = ContextManager.getGlobalContext().getSuiteParameter("env");
	static String mongoDbHost = ContextManager.getGlobalContext().getSuiteParameter("mongoDbHost");
	static String digideskUrl = new EnvironmentSetup().envSetup(env, mongoDbHost).getDigideskAppUrl();
	static String getBlacklistedDrugTypeUrl = digideskUrl
			+ ContextManager.getGlobalContext().getSuiteParameter("getBlacklistedDrugTypeUrl");

	// Eligible for Doctor Consultation
	public static void doctorConsultationConfig(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		String isEnabled = mongoDb.getFieldValue("FeatureStatus", "name", "consultation", "isEnabled");
		logger.debug("Consultation flag in FeatureStatus ==> " + isEnabled);
		boolean consultationEnabled = Boolean.valueOf(isEnabled);
		if (consultationEnabled) {
			String isRxSourceExist = mongoDb.getFieldValue("FeatureStatus", "name", "consultation",
					"presciptionSource");
			String source = dataFactory.getTestCaseParameters().optString("source");
			boolean rxSourceEligible = false;
			if (isRxSourceExist.contains(source)) {
				rxSourceEligible = true;
			}
			String fcPincode = new DatabaseUtil().getPincodeByfcId(dataFactory);
			String getBlacklistedDrugType = getBlacklistedDrugTypeUrl + fcPincode + "&mobile="
					+ dataFactory.getCustomerMobileNumber();
			// Get Blacklisted Drugtype based on pincode & customer mobile #
			LinkedList<String> Header=new LinkedList<>();
			Header.add("Content-Type");
			Header.add("X-Code");
			
			Response blackListedScheduledDrugTypesResponse =new DigideskAppService().getBlacklistedDrugTypeBasedonPincodeAndMobile(
					dataFactory, getBlacklistedDrugType, Header);
			String blackListedScheduledDrugTypes = blackListedScheduledDrugTypesResponse.jsonPath()
					.getString("drugTypes");
			logger.debug("Blacklisted DrugTypes " + dataFactory.getFcId() + " ==> " + blackListedScheduledDrugTypes);
			boolean dcpBlackListed = false;
			if (rxSourceEligible) {
				JSONArray medicineListAndQuantity = new InventoryUtil().medicineArray(dataFactory);
				String drugType = "";
				for (int skuindex = 0; skuindex < medicineListAndQuantity.length(); skuindex++) {
					JSONObject medicineObjFromData = medicineListAndQuantity.getJSONObject(skuindex);
					drugType = medicineObjFromData.optString("drugType");
					if (blackListedScheduledDrugTypes.contains(drugType)) {
						dcpBlackListed = true;
						logger.debug("DRUGTYPE '" + drugType + "' ==> BLACKLISTED FOR [DCP] DOCTOR CONSULTATION "
								+ dataFactory.getFcId());
						break;
					} else {
						dcpBlackListed = false;
						logger.debug("DRUGTYPE " + drugType + " ==> NOT BLACKLISTED FOR [DCP] DOCTOR CONSULTATION "
								+ dataFactory.getFcId());
					}
				}
			}
			dataFactory.setDCPBlackListed(dcpBlackListed);
			logger.debug("DCP Blacklisted ? ==> " + dataFactory.isDCPBlackListed());
		}
	}

	// Deprecated old logic - NOT REQD FOR NOW
	public static void doctorConsultationConfigOldLogic(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		String isEnabled = mongoDb.getFieldValue("FeatureStatus", "name", "consultation", "isEnabled");
		logger.debug("Consultation flag in FeatureStatus ==> " + isEnabled);
		boolean consultationEnabled = Boolean.valueOf(isEnabled);
		if (consultationEnabled) {
			String isRxSourceExist = mongoDb.getFieldValue("FeatureStatus", "name", "consultation",
					"presciptionSource");
			String source = dataFactory.getTestCaseParameters().optString("source");
			boolean rxSourceEligible = false;
			if (isRxSourceExist.contains(source)) {
				rxSourceEligible = true;
			}
			String getBlacklistedDrugType = getBlacklistedDrugTypeUrl + dataFactory.getPincode() + "&mobile="
					+ dataFactory.getCustomerMobileNumber();
			// Get Blacklisted Drugtype based on pincode & customer mobile #
			LinkedList<String> Header=new LinkedList<>();
			Header.add("Content-Type");
			Header.add("X-Code");
			
			Response blacklistedDrugTypeResponse =new  DigideskAppService().getBlacklistedDrugTypeBasedonPincodeAndMobile(dataFactory,
					getBlacklistedDrugType, Header);
			logger.debug("Blacklisted DrugType ==> " + blacklistedDrugTypeResponse);
			boolean dcpBlackListed = false;
			if (rxSourceEligible) {
				String isBlackListedScheduledDrugTypes = mongoDb.getFieldValue("FeatureStatus", "name", "consultation",
						"blackListedScheduledDrugTypes");
				logger.debug("BlackListedScheduledDrugTypes ==> " + isBlackListedScheduledDrugTypes);
				JSONArray medicineListAndQuantity = new InventoryUtil().medicineArray(dataFactory);
				String drugType = "";
				ArrayList<String> doctorConsultationReqdList = new ArrayList<String>();
				for (int skuindex = 0; skuindex < medicineListAndQuantity.length(); skuindex++) {
					JSONObject medicineObjFromData = medicineListAndQuantity.getJSONObject(skuindex);
					drugType = medicineObjFromData.optString("drugType");
					String isDrugTypeBlackListed = new NewMongoDbUtil().getJSONArray("FeatureStatus", "name", "consultation",
							"blackListedScheduledDrugTypes", drugType);
					logger.debug("'" + drugType + "' ==> drugType ==> " + isDrugTypeBlackListed);
					if (isDrugTypeBlackListed.contains("ALL")
							|| isDrugTypeBlackListed.contains(dataFactory.getFcId())) {
						dcpBlackListed = true;
						doctorConsultationReqdList.add(Boolean.toString(dcpBlackListed));
						logger.debug("DRUGTYPE '" + drugType + "' ==> BLACKLISTED FOR DOCTOR CONSULTATION "
								+ dataFactory.getFcId());
					} else {
						dcpBlackListed = false;
						doctorConsultationReqdList.add(Boolean.toString(dcpBlackListed));
						logger.debug("DRUGTYPE " + drugType + " ==> NOT BLACKLISTED FOR DOCTOR CONSULTATION "
								+ dataFactory.getFcId());
					}
				}
				if (doctorConsultationReqdList.contains("true") || dataFactory.isCustomerBlackListed()) {
					dcpBlackListed = true;
					logger.debug("BLACKLISTED FOR DOCTOR CONSULTATION ==> " + dcpBlackListed);
				} else {
					dcpBlackListed = false;
					logger.debug("NOT BLACKLISTED FOR DOCTOR CONSULTATION ==> " + dcpBlackListed);
				}
			}
			dataFactory.setDCPBlackListed(dcpBlackListed);
		}
	}
}