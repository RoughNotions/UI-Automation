package com.medlife.qa.configuration;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.medlife.qa.controller.ContextManager;
import com.medlife.qa.controller.EnvironmentSetup;
import com.medlife.qa.dataprovider.util.TestDataFactory;
import com.medlife.qa.inventoryutils.InventoryUtil;
import com.medlife.service.ApiHeader;
import com.medlife.service.DigideskAppService;
import io.restassured.response.Response;

public class RxPickupConfig {

	static Logger logger = LogManager.getLogger(RxPickupConfig.class.getName());

	public static EnvironmentSetup envSetupUtil = new EnvironmentSetup().getInstance();
	static String env = ContextManager.getGlobalContext().getSuiteParameter("env");
	static String mongoDbHost = ContextManager.getGlobalContext().getSuiteParameter("mongoDbHost");
	static String digideskUrl = new EnvironmentSetup().envSetup(env, mongoDbHost).getDigideskAppUrl();
	static String oAuthLoginUrl = digideskUrl + ContextManager.getGlobalContext().getSuiteParameter("oAuthLoginUrl");
	static String scheduledDrugTypesForPickupUrl = digideskUrl
			+ ContextManager.getGlobalContext().getSuiteParameter("scheduledDrugTypesForPickupUrl");

	public static final List<String> drugTypeList = Arrays.asList("C", "C1", "H", "H1", "H1P", "X");

	public static boolean rxPickupConfig(TestDataFactory dataFactory)
			throws JSONException, UnknownHostException, ParseException {
		JSONObject data = dataFactory.getTestCaseParameters();
		if (data.optString("source").equalsIgnoreCase("GO_RX") || data.optString("source").equalsIgnoreCase("CORP_RX")
				|| data.optString("source").equalsIgnoreCase("GO_RX_CTO")) {
			boolean rxPickUpReqd = false;
			boolean isValidRx = data.optBoolean("validRx");
			dataFactory.setIsValidRx(isValidRx);
			logger.debug("ValidRx ==> " + dataFactory.isValidRx());
			if (new InventoryUtil().orderContainsNonOtcSKUs(dataFactory)) {
				// Get Schedule Drug RxPckup Config
				LinkedList<String> Header=new LinkedList<>();
				Header.add("Content-Type");
				Header.add("X-Code");
				Response scheduledDrugTypesForPickupResponse = new DigideskAppService().getScheduledDrugTypesForPickup(dataFactory,
						scheduledDrugTypesForPickupUrl, Header);
				// {"C":[],"H1P":["ALL"],"H":["Mumbai","Pune"],"X":["ALL"],"H1":["ALL"],"C1":[]}
				JSONArray medicineListAndQuantity = new InventoryUtil().medicineArray(dataFactory);
				String drugType = "";
				ArrayList<String> rxPickUpReqdList = new ArrayList<String>();
				for (int skuindex = 0; skuindex < medicineListAndQuantity.length(); skuindex++) {
					JSONObject medicineObjFromData = medicineListAndQuantity.getJSONObject(skuindex);
					drugType = medicineObjFromData.optString("drugType");
					String getCity = scheduledDrugTypesForPickupResponse.jsonPath().getString(drugType);
					logger.debug(drugType + " ==> " + getCity);
					if (getCity.contains("ALL") || getCity.contains(dataFactory.getCity())) {
						rxPickUpReqd = true;
						rxPickUpReqdList.add(Boolean.toString(rxPickUpReqd));
						logger.debug(
								"DRUGTYPE " + drugType + " ==> CONFIGURED FOR RXPICKUP IN " + dataFactory.getCity());
					} else {
						rxPickUpReqd = false;
						rxPickUpReqdList.add(Boolean.toString(rxPickUpReqd));
						logger.debug("DRUGTYPE " + drugType + " ==> NOT CONFIGURED FOR RXPICKUP IN "
								+ dataFactory.getCity());
					}
				}
				if (rxPickUpReqdList.contains("true") || dataFactory.isCustomerBlackListed()) {
					rxPickUpReqd = true;
					logger.debug("RXPICKUP REQD FOR THIS ORDER ==> " + rxPickUpReqd);
				} else {
					rxPickUpReqd = false;
					logger.debug("RXPICKUP NOT REQD FOR THIS ORDER ==> " + rxPickUpReqd);
				}
				dataFactory.setRxPickUpRequired(rxPickUpReqd);
				return rxPickUpReqd;
			}
			dataFactory.setRxPickUpRequired(rxPickUpReqd);
			return rxPickUpReqd;
		}
		return false;
	}
}