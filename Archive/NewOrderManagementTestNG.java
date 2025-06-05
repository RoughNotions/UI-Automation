package com.medlife.ui.admin;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.medlife.qa.controller.Assertion;
import com.medlife.qa.controller.BaseTest;
import com.medlife.qa.dataprovider.util.NewMongoDbUtil;
import com.medlife.qa.dataprovider.util.TestDataFactory;
import com.medlife.qa.driver.CustomWebDriver;
import com.medlife.qa.pages.admin.LeftNavigationPage;
import com.medlife.qa.pages.admin.LogoutPage;
import com.medlife.qa.pages.admin.TSAServicecalls;
import com.medlife.qa.pages.admin.fcmanagement.FCManagement;
import com.medlife.qa.pages.admin.fcmanagement.FCSearchOrdersPage;
import com.medlife.qa.pages.admin.fcmanagement.FCViewAssignedOrderPage;
import com.medlife.qa.pages.admin.fcmanagement.FinanceMgntCashAndReturnsPage;
import com.medlife.qa.pages.admin.finanacemanagement.SettleFunds;
import com.medlife.qa.pages.admin.finanacemanagement.TransferfundPaid;
import com.medlife.qa.pages.admin.finanacemanagement.TransferfundReceived;
import com.medlife.qa.pages.admin.finanacemanagement.searchtransaction;
import com.medlife.qa.pages.admin.ordermanagement.CreateGPCSRequestPage;
import com.medlife.qa.pages.admin.ordermanagement.CustomerCarePageUtil;
import com.medlife.qa.pages.admin.ordermanagement.CustomerCareSearchPageUtil;
import com.medlife.qa.pages.admin.ordermanagement.PartialResidualPage;
import com.medlife.qa.pages.admin.ordermanagement.RefillOrder;
import com.medlife.qa.pages.admin.ordermanagement.SearchOrderPage;
import com.medlife.qa.pages.admin.trips.CreateTripPage;
import com.medlife.qa.pages.admin.trips.CreateTripPageUtil;
import com.medlife.qa.pages.admin.trips.SearchTripPage;
import com.medlife.qa.pages.admin.trips.TripPageUtil;
import com.medlife.qa.pageutil.NotCurrentPageException;
import com.medlife.qa.pageutil.WebPage;
import com.medlife.qa.util.SMSValidateUtil;
import com.medlife.qa.util.TestRetryAnalyzer;
import com.medlife.qa.util.WebTestRunListener;
import com.medlife.service.util.Services;

public class NewOrderManagementTestNG extends BaseTest {

	protected String orderId;
	protected String secondOrderId;
	protected String adminUserName;
	protected String adminPassword;
	protected String daAgentNameTwo = "testdagent";
	protected int defaultQtyIncrease;
	protected String mobileNo;
	protected String cxUsername;

	@Parameters({ "adminUserName", "adminPassword","cxUsername","defaultQtyIncrease", 
		"fcUserName", "dlUserName","aeUserName", "type","addressLine1", "addressLine2",
		"password","tsaUserName"})
	@BeforeClass(alwaysRun = true)
	public void setUpBeforeClass(String adminUserName, String adminPassword, String cxUsername, int defaultQtyIncrease,
		String fcUserName, String dlUserName, String aeUserName,
		String type, String addressLine1, String addressLine2,  
		String password, String tsaUserName) {
		this.adminUserName = adminUserName; this.adminPassword = adminPassword;
		this.cxUsername = cxUsername; this.defaultQtyIncrease = defaultQtyIncrease;
		this.fcUserName = fcUserName; this.dlUserName = dlUserName; this.aeUserName = aeUserName;
		this.password = password; this.type = type; this.addressLine1 = addressLine1; 
		this.addressLine2 = addressLine2; this.tsaUserName = tsaUserName; 
		
		new Services().pharmacistLogin(serviceUrl
				+ pharmacistLoginWebServiceUrl, pharmacistUserName,
				pharmacistPassword);
	}

	/*
	 * Creaet Rx and order for all sources
	 */
	@Test(dataProvider = "TestData")
	public void createOrder(TestDataFactory dataFactory)
			throws FileNotFoundException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, 100);
		System.out.println("OrderId------>: " + orderId);
		// prepareForPrintOut(dataFactory,orderId);
	}

	@Test(dataProvider = "TestData")
	public void validatePrintouts(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, 100);
		System.out.println("OrderId------>: " + orderId);
		getLoginCredentials(dataFactory);
		FCViewAssignedOrderPage printscreen = new FCViewAssignedOrderPage();
		loginInAdminApp(dataFactory.getFCUsername(), dataFactory.getPassword(),
				true);
		CustomWebDriver.getWebDriver().get(
				deliveryChallanPrintUrl + "/" + orderId);
		CustomWebDriver.getWebDriver().get(insurancePrintUrl + "/" + orderId);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("DC Title = " + printscreen.getDCTitle());
		// System.out.println("DC Title = "+printscreen.getNoInsurValidatation());
		// http://192.168.22.147:8080/AdminWebApp/order/fcassigned/view/pickList/OR6809B-001A
		// http://192.168.22.147:8080/AdminWebApp/order/fcassigned/view/invoice/OR6809B-001A
		// http://192.168.22.147:8080/AdminWebApp/order/fcassigned/view/insurance/OR6809B-001A
		// http://192.168.22.147:8080/AdminWebApp/order/fcassigned/view/prescription/OR6809B-001A
	}

	private void prepareForPrintOut(TestDataFactory data, String orderId) {
		if (data.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("ZIP_RX")
				|| data.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("SLATE_RX")
				|| data.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("YM_RX")) {

			NewMongoDbUtil.updateAddress(orderId);
			NewMongoDbUtil.updateOrderState(orderId, "PendingForStock");
		} else if (data.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("CORPORATE_RX")
				|| data.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("WALKIN_RX")) {
		} else {
			NewMongoDbUtil.updateOrderState(orderId, "PendingForStock");
			NewMongoDbUtil.getInstance().updateValue("order", "_id", orderId,
					"confirmed", true);
		}
	}

	// @Test(dataProvider = "TestData")
	public void updateCustomerDetails(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cutil = new CustomerCarePageUtil();
		cutil.clickViewSearchOrders();
		cutil.seachOrderForCustomerCare(orderId);
		JSONObject testCaseParameters = dataFactory.getTestCaseParameters();
		cutil.enterCustomerDetails(testCaseParameters.getJSONObject("customer"));
		cutil.updateOrder();
		cutil.validateUpdateCustomerDeatails(orderId,
				testCaseParameters.getJSONObject("customer"));
		Assertion.getVerificationFailures();
	}

	/*
	 * Update SlateRx order in CX screen with customer details and without
	 * firstname
	 */
	@Test(dataProvider = "TestData")
	public void updateCustomerDetailsWithoutFirstName(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewSearchOrders();
		cxscreen.seachOrderForCustomerCare(orderId);
		JSONObject testCaseParameters = dataFactory.getTestCaseParameters();
		cxscreen.enterCustomerDetailsWithoutFirstName(testCaseParameters
				.getJSONObject("customer"));
		cxscreen.clickUpdateOrderWithoutFillingMendatoryField();
		Assertion.getVerificationFailures();
	}

	/*
	 * Update order with 5% additional discount and validate whether order is
	 * updated with 5%
	 */
	@Test(dataProvider = "TestData")
	public void updateAdditionalDiscount(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewSearchOrders();
		cxscreen.seachOrderForCustomerCare(orderId);
		cxscreen.enterAdditionDiscount("5.0");
		cxscreen.updateOrder();
		cxscreen.validateAdditionalDiscount(orderId, 5.0);
		Assertion.getVerificationFailures();
	}

	/*
	 * Validate ZipRx order
	 */
	@Test(dataProvider = "TestData")
	public void validateZipRxOrder(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.validateOrderDetails(orderId);
		Assertion.getVerificationFailures();
	}

	/*
	 * Validate SlateRx order details
	 */
	@Test(dataProvider = "TestData")
	public void validateOrderDetails(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.validateOrderDetails(orderId);
		Assertion.getVerificationFailures();
	}

	/*
	 * Update delivery time in received state and validate
	 */
	@Test(dataProvider = "TestData")
	public void validateUpdateDeliveryTimeInRecievedState(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.verifyTimeSlotUpdate(orderId);
		verifyRecievedState(orderId);
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void validateIncreaseQuanityInReceivedState(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.increaseOrderQuantityInvalidCase("100");
		loginInAdminApp(cxUsername, password, true);
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.validateNewQuantityNotUpdated("100");
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void validateDecreaseQuanityInReceivedStateAndMakeItFCAssigned(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.decreaseOrderQuantity(dataFactory.getTestCaseParameters()
				.optInt("decreaseQuantity"));
		cxscreen.fillCustomerAddress(dataFactory, true);
		cxscreen.customerDontWantResidualOrder();
		cxscreen.updateOrder();
		verifyFCAssignedState(orderId);
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void validateFirstDiscount(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.verifyFirstOrderDiscount(orderId, dataFactory);
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	// TO REVISIT
	public void validateMaximumDiscount(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		CustomerCarePageUtil pendingorder = new CustomerCarePageUtil();
		pendingorder.verifyMaxDiscount(
				orderId,
				dataFactory.getTestCaseParameters().optDouble(
						"maxDiscountPercentage"),
				dataFactory.getTestCaseParameters().optDouble(
						"defaultDiscountPercentage"));
		Assertion.getVerificationFailures();

	}

	// @Test(dataProvider = "TestData")
	public void validateAutoFillOfStateAndCity(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		// ccpu.fillCustomerAddress(dataFactory.getTestCaseParameters().getJSONObject("address"),
		// true);
		cxscreen.validateCheckAutoFillCityAndState(dataFactory
				.getTestCaseParameters().getJSONObject("address"));
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void validateCancelOrderCancellationReason(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.cancelOrder();
		verifyCancelledState(orderId);
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void validateNewOrdersAreShownAutomatically(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void validateCorporateRxOrderOnFcManagementScreen(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(fcUserName, password, true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.clickViewAssignedOrderLink();
		viewassignedorder.clickViewEditOrderLink(orderId);
		verifyFCAssignedState(orderId);
		Assertion.getVerificationFailures();
	}

	// @Test(dataProvider = "TestData", retryAnalyzer = TestRetryAnalyzer.class)
	// TO REVISIT
	public void validateLocalityRecommendations(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(cxUsername, password, true);
		updateInventoryAndCreateOrderId(dataFactory, defaultQtyIncrease);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewAssignedOrderLink();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.clickAddAddress();
		cxscreen.selectLocalityFromAutoSuggestion(dataFactory
				.getTestCaseParameters().optString("locality"));
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void validateCorporateRxOrderDetails(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(fcUserName, password, true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.clickViewAssignedOrderLink();
		viewassignedorder.clickViewEditOrderLink(orderId);
		validateState(orderId, "FCAssigned");
		commonVerificationForFCassignned(orderId);
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void validateCorporateRxOrderInPendingForStocks(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, 0);
		verifyPendingForStockState(orderId);
		loginInAdminApp(fcUserName, password, true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.clickViewAssignedOrderLink();
		viewassignedorder.clickPendingOrderLink(orderId);
		viewassignedorder.verifyOrderInformation(orderId);
		updateInventoryForMedicineId(dataFactory, defaultQtyIncrease);
		WebPage.sleep(70000);
		verifyFCAssignedState(orderId);
		// commonVerificationForFCassignned(orderId);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		viewassignedorder.clickAssignedOrderTab();
		viewassignedorder.clickViewEditOrderLink(orderId);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		viewassignedorder.verifyOrderInformation(orderId);
		viewassignedorder.verifyMedicineDetails(orderId);
		viewassignedorder.verifyMaxDispenseDays(orderId);
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void updateAddressAndVerifyOrderDetails(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		if (dataFactory.isOrderToBeCreated()) {
			String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
					dataFactory, defaultQtyIncrease);
			dataFactory.setOrderid(orderId);
		}
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(dataFactory.getOrderid());
		cxscreen.fillCustomerAddress(dataFactory, true);
		cxscreen.updateOrder();
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX")) {
			verifyFCAssignedState(dataFactory.getOrderid());
			Assertion.getVerificationFailures();
		} else if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("YM_RX")) {
			verifyRxPendingState(dataFactory.getOrderid());
		}
	}

	@Test(dataProvider = "TestData")
	public String updateAddressAndVerifyRxAndOrderDetails(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException {
		if (dataFactory.isOrderToBeCreated()) {
			String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
					dataFactory, defaultQtyIncrease);
			dataFactory.setOrderid(orderId);
		}
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(dataFactory.getOrderid());
		cxscreen.fillCustomerAddress(dataFactory, true);
		cxscreen.updateOrder();
		verifyRxPendingState(dataFactory.getOrderid());
		return dataFactory.getOrderid();
	}

	@Test(dataProvider = "TestData")
	public void cancelOrderFromCXInReceivedState(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.cancelOrder();
		verifyCancelledState(orderId);
		SMSValidateUtil.getInstance().validateCancelledByCXMessage(orderId,
				"" + dataFactory.getMobileNo());
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void updateQuantityAlongWithAddressAndVerifyStateAndOrderDetails(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.fillCustomerAddress(dataFactory, true);
		cxscreen.decreaseOrderQuantity(dataFactory.getTestCaseParameters()
				.getInt("decreaseQuantity"));
		cxscreen.updateOrder();
		verifyFCAssignedState(orderId);
		verifyUpdatedQuantityInDb(orderId, dataFactory.getTestCaseParameters()
				.getInt("decreaseQuantity"));
		commonVerificationForFCassignned(orderId);
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void updateDeliveryTimeAlongWithAddressAndVerifyStateAndOrderDetails(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.fillCustomerAddress(dataFactory, true);
		cxscreen.verifyTimeSlotUpdate(orderId);
		// ccp.changeTimeSlot();
		commonVerificationForScheduled(orderId);
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void updateScheduleTimeAfterFCAccepted(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewSearchOrders();
		SearchOrderPage sop = new SearchOrderPage();
		sop.searchOrder(dataFactory.getOrderid());
		sop.clickViewEditOrderLink(dataFactory.getOrderid());
		sop.verifyTimeSlotUpdate(dataFactory.getOrderid());
		verifyFCAcceptedState(dataFactory.getOrderid());
		new LogoutPage().logout();
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void updateScheduleTimeAfterOrderIsDAAssignmentPending(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException {
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		CreateTripPageUtil ctp = new CreateTripPageUtil();
		ctp.checkAndClickCreateTripLink();
		ctp.clickAddToTrip(dataFactory.getOrderid());
		new LogoutPage().logout();
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewSearchOrders();
		SearchOrderPage sop = new SearchOrderPage();// TO REVISIT
		sop.searchOrder(dataFactory.getOrderid());
		sop.clickViewEditOrderLink(dataFactory.getOrderid());
		verifyDAAssignmentPendingState(dataFactory.getOrderid());
		sop.verifyTimeSlotUpdate(dataFactory.getOrderid());
		verifyFCAcceptedState(dataFactory.getOrderid());
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void updateScheduleTimeAfterOrderIsDAPicked(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException {
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		CreateTripPageUtil ctp = new CreateTripPageUtil();
		ctp.clickCreateTripLink();
		String tripid = ctp.clickAddToTrip(dataFactory.getOrderid());
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsalogin(serviceUrl, this.tsaAgentName);
		tsaaction.tsafree(serviceUrl);
		ctp.clickViewTrip();
		TripPageUtil tpu = new TripPageUtil();
		tpu.searchAndClickATrip(tripid);
		tpu.addDAAgentToTrip(tsaAgentName);
		tsaaction.tsapicktrip(serviceUrl, tripid);
		WebTestRunListener.createAttachment();
		LogoutPage lop = new LogoutPage();
		lop.logout();
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewSearchOrders();
		SearchOrderPage sop = new SearchOrderPage();
		sop.searchOrder(dataFactory.getOrderid());
		sop.clickViewEditOrderLink(dataFactory.getOrderid());
		verifyDAPickedState(dataFactory.getOrderid());
		sop.verifyTimeSlotUpdate(dataFactory.getOrderid());
		verifyFCAcceptedState(dataFactory.getOrderid());
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void updateScheduleTimeAfterFCAssigned(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, 10);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil ccpu = new CustomerCarePageUtil();
		ccpu.clickViewPendingOrders();
		ccpu.clickViewEditOrderLink(orderId);
		ccpu.fillCustomerAddress(dataFactory, true);
		ccpu.updateOrder();
		verifyFCAssignedState(orderId);
		LogoutPage lop = new LogoutPage();
		lop.logout();
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewSearchOrders();
		WebTestRunListener.createAttachment();
		SearchOrderPage sop = new SearchOrderPage();
		sop.searchOrder(orderId);
		sop.clickViewEditOrderLink(orderId);
		sop.verifyTimeSlotUpdate(orderId);
		WebTestRunListener.createAttachment();
		verifyFCAssignedState(orderId);
		Assertion.getVerificationFailures();
	}

	// ran with new framework and working (1 failure due to time shown in UI
	// girish is fixing)
	@Test(dataProvider = "TestData")
	public void deliverOrder(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		getLoginCredentials(dataFactory);
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId,
				dataFactory.getDAAgentName());
		validateState(orderId, "DAAssigned");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		verifyTripDeliverAndAccount(dataFactory.getAEUsername(),
				dataFactory.getPassword(), serviceUrl,
				dataFactory.getDAAgentName(), tripid, orderId,
				"" + dataFactory.getMobileNo(), dataFactory);
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void deliverCorporateRxOrder(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		SMSValidateUtil.getInstance().validateWelcomeMessageCorpRx(
				"" + dataFactory.getMobileNo(), orderId);
		getLoginCredentials(dataFactory);
		loginInAdminApp(dataFactory.getFCUsername(), dataFactory.getPassword(),
				true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.clickViewAssignedOrderLink();
		verifyFCAssignedState(orderId);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		viewassignedorder.acceptOrder(orderId);
		SMSValidateUtil.getInstance().validateFCAcceptedMessageForCorpRx(
				orderId, "" + dataFactory.getMobileNo());
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId,
				dataFactory.getDAAgentName());
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		verifyTripDeliverAndAccount(dataFactory.getAEUsername(),
				dataFactory.getPassword(), serviceUrl,
				dataFactory.getDAAgentName(), tripid, orderId,
				"" + dataFactory.getMobileNo(), dataFactory);
		Assertion.getVerificationFailures();
	}

	// Deselect feature is removed
	// @Test(dataProvider = "TestData")
	// public void verifyDeselectMedicine(TestDataFactory dataFactory) throws
	// NotCurrentPageException, IOException,
	// InterruptedException {
	// JSONObject testCaseParameters = dataFactory.getTestCaseParameters();
	// String meddetails = testCaseParameters.optString("medicineId");
	// int count = StringUtils.countMatches(meddetails, "medid");
	// String orderId = createPrescriptionAndReturnOrderId(dataFactory);
	// loginInAdminAppWithSuper(this.cxUsername, this.password);
	// ViewAssignedOrderPage vaol = new ViewAssignedOrderPage();
	// vaol.clickViewAssignedOrderLink();
	// vaol.clickViewEditOrderLink(orderId);
	// vaol.verifyDeselectButton();
	// vaol.verifyDeselectCheckbox(count);
	// vaol.verifyDeselectMedicines();
	// vaol.clickViewEditOrderLink(orderId);
	// vaol.verifyDeselectNoCheckbox();
	// Assertion.getVerificationFailures();
	// }

	@Test(dataProvider = "TestData")
	public void verifyOrderPagination(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		loginInAdminApp(this.cxUsername, this.password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		// cxscreen.validateOrderTableHeaders();
		cxscreen.clickViewPendingOrders();
		cxscreen.verifyPagination();
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void verifyOrderAcceptedState(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		loginAsFCAndAcceptOrder(dataFactory);
		verifyFCAcceptedState(dataFactory.getOrderid());
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void verifyChangedTimeSlotValues(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(this.cxUsername, this.password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.changeTimeSlot();
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void searchTransactions(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		JSONObject testCaseParameters = dataFactory.getTestCaseParameters();
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		LeftNavigationPage leftNavPage = new LeftNavigationPage();
		leftNavPage.clickTransactions();
		searchtransaction search = new searchtransaction();
		search.SearchTransactions(testCaseParameters.optString("fdate"),
				testCaseParameters.optString("tdate"));
		Assertion.softAssertTrue(search.isConfirmationPresent(),
				testCaseParameters.optString("expoutput"));
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void transferFundPaidToTsa(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		JSONObject testCaseParameters = dataFactory.getTestCaseParameters();
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		LeftNavigationPage leftNavPage = new LeftNavigationPage();
		leftNavPage.clickViewtransferfundpaid();
		TransferfundPaid tfund = new TransferfundPaid();
		tfund.Transferfund(testCaseParameters.optString("id"),
				testCaseParameters.optString("amount"),
				testCaseParameters.optString("comment"));
		tfund.assertResultsPresent(testCaseParameters.optString("expoutput"));
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void transferFundPaidToMedlife(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		transferFundPaidToTsa(dataFactory);
	}

	@Test(dataProvider = "TestData")
	public void transferFundReceived(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		JSONObject testCaseParameters = dataFactory.getTestCaseParameters();
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		LeftNavigationPage leftNavPage = new LeftNavigationPage();
		leftNavPage.clickViewtransferfundreceived();
		TransferfundReceived tfund = new TransferfundReceived();
		tfund.Transferfund(testCaseParameters.optString("id"),
				testCaseParameters.optString("amount"),
				testCaseParameters.optString("comment"));
		tfund.assertResultsPresent(testCaseParameters.optString("expoutput"));
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void updateDispositionReason(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		// TO REVISIT
		JSONObject testCaseParameters = dataFactory.getTestCaseParameters();
		String orderId = createPrescriptionAndReturnOrderId(dataFactory);
		loginInAdminApp(this.cxUsername, this.password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewAssignedOrderLink();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.selectDispositionReason(testCaseParameters
				.optString("dreason"));
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.verifyDispositionReason(testCaseParameters
				.optString("dreason"));
		SearchOrderPage search = new SearchOrderPage();
		search.assertDispositionReason(orderId,
				testCaseParameters.optString("dreason"));
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData", retryAnalyzer = TestRetryAnalyzer.class)
	// TO REVISIT
	public void validateCustomLocality(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		loginInAdminApp(cxUsername, password, true);
		updateInventoryAndCreateOrderId(dataFactory, defaultQtyIncrease);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewAssignedOrderLink();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.clickAddAddress();
		cxscreen.verifyCustomLocality();
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	// TO REVISIT
	public void validatePartialOrder(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, 4);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewAssignedOrderLink();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.validateOrderDetails(orderId);
		cxscreen.clickAddAddress();
		cxscreen.fillPartialAddress(dataFactory);
		cxscreen.validateDescreasedQuantityForPartialOrder(dataFactory);
		cxscreen.updateOrderAndClickOkButton();
		verifyFCAssignedState(orderId);
		commonVerificationForFCassignned(this.orderId);// TEST IT and RECTIFY
														// orderId
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.acceptOrder(orderId);
		verifyFCAcceptedState(orderId);
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void validatePharmacistCanIncreaseOrDecreaseQuantity(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException {
		updateAddressAndVerifyOrderDetails(dataFactory);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.changeQuantity(dataFactory.getTestCaseParameters()
				.optInt("increaseQty"));
		viewassignedorder.updateOrder();
		verifyUpdatedQuantityInDb(dataFactory.getOrderid(), dataFactory
				.getTestCaseParameters().optInt("increaseQty"));
		viewassignedorder.verifyMedicineDetails(dataFactory.getOrderid());
		viewassignedorder.changeQuantity(dataFactory.getTestCaseParameters()
				.optInt("decreaseQuantity"));
		viewassignedorder.updateOrder();
		verifyUpdatedQuantityInDb(dataFactory.getOrderid(), dataFactory
				.getTestCaseParameters().optInt("decreaseQuantity"));
		viewassignedorder.verifyMedicineDetails(dataFactory.getOrderid());
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void validatePharmacistCanUpdateBatchInformation(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException {
		// TO REVISIT inventory update fails at FC
		// In findInventoryWithMultipleBatch
		// 01:35:13.428 [http-nio-8080-exec-10] INFO
		// c.m.w.s.impl.InventoryServiceImpl - List>>>>>[]
		updateAddressAndVerifyOrderDetails(dataFactory);
		loginInAdminApp(this.fcUserName, this.password, true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.clickViewAssignedOrderLink();
		viewassignedorder.clickViewEditOrderLink(dataFactory.getOrderid());
		String changedBatchNo = viewassignedorder.changeBatch();
		verifyChangedBatchNumber(dataFactory.getOrderid(), changedBatchNo);
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void cancelOrderFromFC(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		updateAddressAndVerifyOrderDetails(dataFactory);
		new LogoutPage().logout();
		loginInAdminApp(this.fcUserName, this.password, true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.clickViewEditOrderLink(dataFactory.getOrderid());
		viewassignedorder.cancelOrder();
		SMSValidateUtil.getInstance().validateFCCanceledMessage(
				dataFactory.getOrderid(), "" + dataFactory.getMobileNo());
		verifyCancelledState(dataFactory.getOrderid());
		new LogoutPage().logout();
	}

	// finished manas till now 20th
	protected void loginAsFCAndAcceptOrder(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		getLoginCredentials(dataFactory);
		loginInAdminApp(dataFactory.getFCUsername(), dataFactory.getPassword(),
				true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.clickViewAssignedOrderLink();
		if (dataFactory.getTestCaseParameters().optString("fcType")
				.equalsIgnoreCase("POJO")) {
			viewassignedorder.pojoAcceptOrder(dataFactory.getOrderid());
		} else {
			viewassignedorder.acceptOrder(dataFactory.getOrderid());
		}
		// SMSValidateUtil.getInstance().validateFCAcceptedMessage(
		// dataFactory.getOrderid(), "" + dataFactory.getMobileNo());
		// new LogoutPage().logout();
	}

	protected void loginAsCXAndUpdateOrder(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		loginInAdminApp(this.cxUsername, this.password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(dataFactory.getOrderid());
		cxscreen.fillCustomerAddress(dataFactory, true);
		cxscreen.updateOrder();
	}

	protected void commonVerificationForFCassignned(String orderId)
			throws NotCurrentPageException, IOException {
		// loginInAdminApp(this.fcUserName, this.password,true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// viewassignedorder.clickViewAssignedOrderLink();
		// viewassignedorder.clickViewEditOrderLink(orderId);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		viewassignedorder.verifyOrderInformation(orderId);
		viewassignedorder.verifyMedicineDetails(orderId);
		viewassignedorder.verifyMaxDispenseDays(orderId);
	}

	protected void commonVerificationForScheduled(String orderId)
			throws NotCurrentPageException, IOException {
		loginInAdminApp(this.fcUserName, this.password, true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		viewassignedorder.clickViewAssignedOrderLink();
		viewassignedorder.clickRescheduledLink(orderId);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		viewassignedorder.verifyOrderInformation(orderId);
		viewassignedorder.verifyMedicineDetailsRescheduledOrder(orderId);
		viewassignedorder.verifyMaxDispenseDays(orderId);
	}

	protected String updateInventoryAndCreateOrderId(
			TestDataFactory dataFactory, int quantity)
			throws FileNotFoundException, IOException {
		updateInventoryForMedicineId(dataFactory, quantity);
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		this.orderId = orderId;
		System.out.println("Order created ==" + orderId);
		dataFactory.setOrderid(orderId);
		return orderId;
	}

	protected void verifyUpdatedQuantityInDb(String orderId, int quantityToCheck) {
		String myDoc = mongoDb.getData("order", "_id", orderId);
		JSONArray orderItems = new JSONObject(myDoc).optJSONArray("orderItems");
		for (int index = 0; index < orderItems.length(); index++) {
			Assertion.assertEquals(orderItems.getJSONObject(index)
					.optInt("qty"), quantityToCheck,
					"Updated quantity does not get updated in the database");
		}
	}

	protected void verifyChangedBatchNumber(String orderId, String batchNumber) {
		String myDoc = mongoDb.getData("order", "_id", orderId);
		JSONArray orderItems = new JSONObject(myDoc).optJSONArray("orderItems");
		for (int index = 0; index < orderItems.length(); index++) {
			Assertion.assertEquals(orderItems.getJSONObject(index)
					.optJSONObject("item").optString("batchNo"), batchNumber,
					"Batch does not get updated in the order collection");
			Assertion
					.assertEquals(orderItems.getJSONObject(index)
							.optJSONObject("item").optDouble("mrpPerUnit"),
							getPriceForBatchFromInventory(batchNumber),
							"Mrp price does not get updated in Database in order collection");

		}
	}

	protected double getPriceForBatchFromInventory(String batchNumber) {
		String myDoc = mongoDb
				.getData("Inventory", "item.batchNo", batchNumber);
		double mrpPerUnit = new JSONObject(myDoc).optJSONObject("item")
				.optDouble("mrpPerUnit");
		return mrpPerUnit;

	}

	@Test(dataProvider = "TestData")
	public void validateCustomerOrderForAllMedicines(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		int instockqty = 10;
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, instockqty);
		loginInAdminApp(cxUsername, password, true);
		PartialResidualPage cxscreen = new PartialResidualPage();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		validateState(orderId, "Received");
		validateOrderType(orderId, "Partial");
		cxscreen.clickCheckBox(1);
		String medid = cxscreen.getMedIdFromJson(dataFactory);
		int qty = cxscreen.getqtyFromJson(dataFactory);
		cxscreen.validateUIPrescribedQuantity(medid, qty);
		cxscreen.validateUIInstockQuantity(medid, instockqty);
		cxscreen.validateUIOrderQuantity(medid, qty);
		cxscreen.fillCustomerAddress(dataFactory, true);
		cxscreen.updateOrder();
		validateState(orderId, "PendingForStock");
		validateOrderType(orderId, "Partial_Residual");
		Assertion.getVerificationFailures();
		cleanupData(orderId);

	}

	@Test(dataProvider = "TestData")
	public void validateCustomerOrderForAvailableMedicinesAndRemainingLater(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		int instockqty = 10;
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, instockqty);
		loginInAdminApp(cxUsername, password, true);
		PartialResidualPage cxscreen = new PartialResidualPage();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		validateOrderType(orderId, "Partial");
		validateState(orderId, "Received");
		String medid = cxscreen.getMedIdFromJson(dataFactory);
		int qty = cxscreen.getqtyFromJson(dataFactory);
		cxscreen.clickCheckBox(2);
		// validate partial order
		cxscreen.validateUIPrescribedQuantity(medid, qty);
		cxscreen.validateUIInstockQuantity(medid, instockqty);
		cxscreen.validateUIOrderQuantity(medid, instockqty);
		// validate residual order
		cxscreen.validateUIPrescribedQuantityForResidualOrder(medid, qty);
		cxscreen.validateUIInstockQuantityForResidualOrder(medid, instockqty);
		cxscreen.validateUIOrderQuantityForResidualOrder(medid, qty
				- instockqty);
		// TOREVISIT add methods to get current date few days
		cxscreen.enterResidualDateAndTime(cxscreen.getFutureDate(3),
				"17:00 to 20:00");
		// cxscreen.fillCustomerAddress(dataFactory.getTestCaseParameters()
		// .getJSONObject("address"), true);
		cxscreen.updateOrder();
		validateState(orderId, "FCAssigned");
		validateOrderType(orderId, "Partial");
		String secondOrderId = orderId.replace("-001A", "-002A");
		System.out.println("Second Order Id Created ->" + secondOrderId);
		validateState(secondOrderId, "PendingForStock");
		validateOrderType(secondOrderId, "Residual");
		Assertion.getVerificationFailures();
		cleanupData(orderId);
		cleanupData(secondOrderId);
	}

	@Test(dataProvider = "TestData")
	public void validateCustomerOrderForNoRemainingMedicines(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		int instockqty = 10;
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, instockqty);
		loginInAdminApp(cxUsername, password, true);
		PartialResidualPage cxscreen = new PartialResidualPage();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		validateOrderType(orderId, "Partial");
		validateState(orderId, "Received");
		cxscreen.clickCheckBox(3);
		String medid = cxscreen.getMedIdFromJson(dataFactory);
		int qty = cxscreen.getqtyFromJson(dataFactory);
		cxscreen.validateUIPrescribedQuantity(medid, qty);
		cxscreen.validateUIInstockQuantity(medid, instockqty);
		cxscreen.validateUIOrderQuantity(medid, instockqty);
		cxscreen.fillCustomerAddress(dataFactory, true);
		cxscreen.updateOrder();
		validateState(orderId, "FCAssigned");
		validateOrderType(orderId, "Residual_cancelled");
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void validateCustomerOrderForAllMedicinesAndMakeOrderFCAssigned(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		int instockqty = 10;
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, instockqty);
		loginInAdminApp(cxUsername, password, true);
		PartialResidualPage cxscreen = new PartialResidualPage();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		validateState(orderId, "Received");
		validateOrderType(orderId, "Partial");
		cxscreen.clickCheckBox(1);
		String medid = cxscreen.getMedIdFromJson(dataFactory);
		int qty = cxscreen.getqtyFromJson(dataFactory);
		cxscreen.validateUIPrescribedQuantity(medid, qty);
		cxscreen.validateUIInstockQuantity(medid, instockqty);
		cxscreen.validateUIOrderQuantity(medid, qty);
		cxscreen.fillCustomerAddress(dataFactory, true);
		cxscreen.updateOrder();
		validateState(orderId, "PendingForStock");
		validateOrderType(orderId, "Partial_Residual");
		// updateInventoryForMedicineId(dataFactory, 30);
		updateInventoryForMedicineIdBangalore(medid, qty);
		WebPage.sleep(70000);// wait for 1 min auto check for inventory
		validateState(orderId, "FCAssigned");
		// String orderState = getOrderStatus(orderId);
		// if ( orderState.contains("FCAssigned") ) {
		// validateState(orderId, "FCAssigned");
		// } else {
		// loginInAdminAppWithFc(fcUserName, password,true);
		// FCAssignedCancelledOrderPage fcaco= new
		// FCAssignedCancelledOrderPage();
		// fcaco.clickPendingOrderLink(orderId);
		// fcaco.clickCheckInventory(orderId);
		// WebPage.sleep(2000);
		// validateState(orderId, "FCAssigned");
		// }
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData", retryAnalyzer = TestRetryAnalyzer.class)
	public void deliverWalkinRxOrder(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		verifyFCAssignedState(orderId);
		loginInAdminApp(this.fcUserName, this.password, true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.clickViewAssignedOrderLink();
		viewassignedorder.acceptOrder(orderId);
		verifyOrderDeliveredState(orderId);
		SMSValidateUtil.getInstance().validateNOSMSSentWalkinRx(
				"" + dataFactory.getMobileNo(), orderId);
		Assertion.getVerificationFailures();
	}

	// finished manas till now 20th

	@Test(dataProvider = "TestData", /* retryAnalyzer = TestRetryAnalyzer.class, */description = "change order quantity after FCAssigned")
	public void modifyOrderAddMultipleAddress(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		this.orderId = createPrescriptionAndReturnOrderId(dataFactory);
		System.out.println("Order Created: " + this.orderId);
		// TO REVISIT
		loginInAdminApp(this.cxUsername, this.password, true);
		CustomerCarePageUtil viewpendingorder = new CustomerCarePageUtil();
		viewpendingorder.clickViewAssignedOrderLink();
		viewpendingorder.clickViewEditOrderLink(this.orderId);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.fillCustomerAddress(dataFactory, true);
		viewpendingorder.updateOrderAndClickOkButton();
		verifyFCAssignedState(this.orderId);
		viewpendingorder = null;
		loginInAdminApp(this.cxUsername, this.password, true);
		LeftNavigationPage omserachorder = new LeftNavigationPage();
		omserachorder.clickSearchOrder();
		SearchOrderPage sop = new SearchOrderPage();
		sop.searchOrder(this.orderId);
		sop.clickViewEditOrderLink(this.orderId);
		sop.addAddressExtra(dataFactory);
		sop.updateOrderAndClickOkButton();
		verifyFCAssignedState(this.orderId);

		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void modifyOrderChangeAddress(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		// TO REVISIT
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		loginInAdminAppWithSuper(cxUsername, password);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewSearchOrders();
		cxscreen.seachOrderForCustomerCare(orderId);
		cxscreen.validateOrderTableHeaders();
		cxscreen.clickAddAddressButton();
		cxscreen.enterAddressType("add type");
		cxscreen.enterAddressLine1("add line 1");
		cxscreen.enterAddressLine2("add line 2");
		cxscreen.enterCityName("Bangalore");
		cxscreen.enterStateName("Karnataka");
		cxscreen.enterPinCode("560049");
		cxscreen.enterLocality("Bangalore, Karnataka, India");
		cxscreen.enterLandmark("nothing");
		cxscreen.clickOrderQtyTextField();
		cxscreen.clickUpdateOrderButton();
	}

	@Test(dataProvider = "TestData", retryAnalyzer = TestRetryAnalyzer.class)
	public void addOrderToDaAssginementPendingTrip(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("First order->" + orderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		CreateTripPageUtil ctpu = new CreateTripPageUtil();
		ctpu.clickAddToTrip(orderId);
		validateState(orderId, "DAAssignmentPending");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		LogoutPage logout = new LogoutPage();
		// logout.logout();
		// second order creation
		dataFactory.setOrderToBeCreated(true);
		String secondOrderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("System Generated second order-> " + secondOrderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(secondOrderId);
		// through UI
		// loginAsFCAndAcceptOrder(dataFactory);

		// --though DB update
		updateOrderToFCAssigned(dataFactory.getTestCaseParameters(),
				secondOrderId);
		updateOrderState(secondOrderId, "FCAccepted");
		// Adding Second Order to trip Code
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		SearchTripPage stp = new SearchTripPage();
		stp.addOrderToTrip(tripid, secondOrderId, "Trip/Order status updated");
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		CreateTripPage ctp = new CreateTripPage();
		ctp.assertOrderAdded(tripid, secondOrderId);
		validateState(secondOrderId, "DAAssignmentPending");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void addOrderToDAAssignedTripAndDeliverOrder(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {

		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("First order->" + orderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		String tripid = createTripAndAssignDAAgent(serviceUrl, orderId,
				this.tsaAgentName);
		validateState(orderId, "DAAssigned");
		// String tripid = getTripId(orderId);
		System.out.println(tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		dataFactory.setOrderToBeCreated(true);
		// Order to be Added created here
		String secondOrderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("second order->" + secondOrderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(secondOrderId);
		// through UI
		// loginAsFCAndAcceptOrder(dataFactory);

		// --though DB update
		updateOrderToFCAssigned(dataFactory.getTestCaseParameters(),
				secondOrderId);
		updateOrderState(secondOrderId, "FCAccepted");
		// Add Order code
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		SearchTripPage stp = new SearchTripPage();
		stp.addOrderToTrip(tripid, secondOrderId, "Trip/Order status updated");
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		CreateTripPage ctp = new CreateTripPage();
		ctp.assertOrderAdded(tripid, secondOrderId);

		validateState(secondOrderId, "DAAssigned");
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsaActionPickTrip(serviceUrl, this.tsaAgentName, tripid,
				orderId);
		tsaaction.tsadeliver(serviceUrl, orderId, tripid);
		tsaaction.tsadeliver(serviceUrl, secondOrderId, tripid);
		String tripStatus = getTripStatus(tripid);
		Assertion.softAssertTrue(tripStatus.contains("Closed"),
				"Trip not closed succesfully after delivering orders in it");
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData", retryAnalyzer = TestRetryAnalyzer.class)
	public void addOrderToDAPickedTripAndDeliverOrder(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {

		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("First order->" + orderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, tsaAgentName);
		validateState(orderId, "DAAssigned");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsaActionPickTrip(serviceUrl, this.tsaAgentName, tripid,
				orderId);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// Order to be Added created here
		dataFactory.setOrderToBeCreated(true);
		String secondOrderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(secondOrderId);
		// through UI
		// loginAsFCAndAcceptOrder(dataFactory);

		// --though DB update
		updateOrderToFCAssigned(dataFactory.getTestCaseParameters(),
				secondOrderId);
		updateOrderState(secondOrderId, "FCAccepted");
		// Add Order code
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		SearchTripPage stp = new SearchTripPage();
		stp.addOrderToTrip(tripid, secondOrderId, "Trip/Order status updated");
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		CreateTripPage ctp = new CreateTripPage();
		ctp.assertOrderAdded(tripid, secondOrderId);
		validateState(secondOrderId, "DAPicked");
		tripid = getTripId(orderId);
		tsaaction.tsadeliver(serviceUrl, orderId, tripid);
		tsaaction.tsadeliver(serviceUrl, secondOrderId, tripid);
		String tripStatus = getTripStatus(tripid);
		Assertion.softAssertTrue(tripStatus.contains("Closed"),
				"Trip not closed succesfully after delivering orders in it");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData", retryAnalyzer = TestRetryAnalyzer.class)
	public void removeOrderFromDaAssginementPendingTrip(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {

		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("First order->" + orderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		CreateTripPageUtil ctpu = new CreateTripPageUtil();
		ctpu.clickAddToTrip(orderId);
		// createTripOnly(dataFactory.getDLUsername(),
		// dataFactory.getPassword(), orderId);
		validateState(orderId, "DAAssignmentPending");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// remove order code
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		SearchTripPage stp = new SearchTripPage();
		stp.removeOrderFromTrip(tripid, orderId, "Trip/Order status updated");
		validateState(orderId, "FCAccepted");
		String tripStatus = getTripStatus(tripid);
		Assertion
				.softAssertTrue(tripStatus.contains("Closed"),
						"Trip not closed succesfully after removing only order in trip");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData", retryAnalyzer = TestRetryAnalyzer.class)
	public void removeOrderFromDAAssignedTrip(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {

		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("First order->" + orderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, tsaAgentName);
		System.out.println("check->" + orderId);
		validateState(orderId, "DAAssigned");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// remove order code
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		SearchTripPage stp = new SearchTripPage();
		stp.removeOrderFromTrip(tripid, orderId, "Trip/Order status updated");
		validateState(orderId, "FCAccepted");
		String tripStatus = getTripStatus(tripid);
		Assertion
				.softAssertTrue(tripStatus.contains("Closed"),
						"Trip not closed succesfully after removing only order in trip");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData", retryAnalyzer = TestRetryAnalyzer.class)
	public void removeOrderFromDAPickedTrip(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {

		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("First order->" + orderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, tsaAgentName);
		validateState(orderId, "DAAssigned");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsaActionPickTrip(serviceUrl, this.tsaAgentName, tripid,
				orderId);
		validateState(orderId, "DAPicked");
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// remove order code
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		SearchTripPage stp = new SearchTripPage();
		stp.removeOrderFromTrip(tripid, orderId, "Trip/Order status updated");
		validateState(orderId, "FCAccepted");
		String tripStatus = getTripStatus(tripid);
		Assertion
				.softAssertTrue(tripStatus.contains("Closed"),
						"Trip not closed succesfully after removing only order in trip");
		Assertion.getVerificationFailures();

	}

	/*
	 * @Test(dataProvider = "TestData") public void
	 * addOrderToDAAssignedTripAndDeliverOrder(TestDataFactory dataFactory)
	 * throws NotCurrentPageException, IOException, InterruptedException {
	 * addOrderToDAAssignedTrip(dataFactory); TSAServicecalls tsaaction = new
	 * TSAServicecalls(); String tripid = getTripId(orderId);
	 * tsaaction.tsaActionPickTrip(serviceUrl, this.tsaAgentName, tripid,
	 * orderId); tsaaction.tsadeliver(serviceUrl, orderId, tripid);
	 * tsaaction.tsadeliver(serviceUrl, secondOrderId, tripid); String
	 * tripStatus=getTripStatus(tripid);
	 * Assertion.softAssertTrue(tripStatus.contains("Closed"),
	 * "Trip not closed succesfully after delivering orders in it");
	 * Assertion.getVerificationFailures();
	 * 
	 * }
	 */

	/*
	 * @Test(dataProvider = "TestData") public void
	 * addOrderToDAPickedTripAndDeliverOrder(TestDataFactory dataFactory) throws
	 * NotCurrentPageException, IOException, InterruptedException {
	 * addOrderToDAPickedTrip(dataFactory); TSAServicecalls tsaaction = new
	 * TSAServicecalls(); String tripid = getTripId(orderId);
	 * tsaaction.tsadeliver(serviceUrl, orderId, tripid);
	 * tsaaction.tsadeliver(serviceUrl, secondOrderId, tripid); String
	 * tripStatus=getTripStatus(tripid);
	 * Assertion.softAssertTrue(tripStatus.contains("Closed"),
	 * "Trip not closed succesfully after delivering orders in it");
	 * Assertion.getVerificationFailures();
	 * 
	 * }
	 */

	@Test(dataProvider = "TestData")
	public void changeTsaForDAAssingedTrip(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {

		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("First order->" + orderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, tsaAgentName);
		validateState(orderId, "DAAssigned");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsalogin(serviceUrl, daAgentNameTwo);
		tsaaction.tsafree(serviceUrl);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		CreateTripPage ctp = new CreateTripPage();
		ctp.clickViewTrip();
		TripPageUtil tpu = new TripPageUtil();
		tpu.searchAndClickATrip(tripid);
		tpu.addDAAgentToTrip(daAgentNameTwo);
		String tsaId = getTsaFromTrip(tripid);
		String tsaName = getTsaName(tsaId);
		Assertion.softAssertTrue(tsaName.contains(daAgentNameTwo),
				"New Da agent  is not assinged to the trip");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData", retryAnalyzer = TestRetryAnalyzer.class)
	public void changeTsaForDAAssingedTripAndDeliverOrder(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		changeTsaForDAAssingedTrip(dataFactory);
		String orderId = dataFactory.getOrderid();
		TSAServicecalls tsaaction = new TSAServicecalls();
		String tripid = getTripId(dataFactory.getOrderid());
		tsaaction.tsaActionPickTrip(serviceUrl, this.daAgentNameTwo, tripid,
				orderId);
		tsaaction.tsadeliver(serviceUrl, orderId, tripid);
		String tripStatus = getTripStatus(tripid);
		Assertion.softAssertTrue(tripStatus.contains("Closed"),
				"Trip not closed succesfully after delivering orders in it");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void changeTsaForDAPickedTrip(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {

		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("First order->" + orderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, tsaAgentName);
		validateState(orderId, "DAAssigned");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsaActionPickTrip(serviceUrl, this.tsaAgentName, tripid,
				orderId);
		validateState(orderId, "DAPicked");
		LogoutPage logout = new LogoutPage();
		logout.logout();
		tsaaction.tsalogin(serviceUrl, daAgentNameTwo);
		tsaaction.tsafree(serviceUrl);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		CreateTripPage ctp = new CreateTripPage();
		ctp.clickViewTrip();
		TripPageUtil tpu = new TripPageUtil();
		tpu.searchAndClickATrip(tripid);
		tpu.addDAAgentToTrip(daAgentNameTwo);
		validateState(orderId, "DAAssigned");
		String tsaId = getTsaFromTrip(tripid);
		String tsaName = getTsaName(tsaId);
		Assertion.softAssertTrue(tsaName.contains(daAgentNameTwo),
				"New Da agent  is not assinged to the trip");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData", retryAnalyzer = TestRetryAnalyzer.class)
	public void changeTsaForDAPickedTripAndDeliverOrder(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		changeTsaForDAPickedTrip(dataFactory);
		String orderId = dataFactory.getOrderid();
		TSAServicecalls tsaaction = new TSAServicecalls();
		String tripid = getTripId(orderId);
		tsaaction.tsaActionPickTrip(serviceUrl, this.daAgentNameTwo, tripid,
				orderId);
		tsaaction.tsadeliver(serviceUrl, orderId, tripid);
		validateState(orderId, "Delivered");
		String tripStatus = getTripStatus(tripid);
		Assertion.softAssertTrue(tripStatus.contains("Closed"),
				"Trip not closed succesfully after delivering orders in it");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void validateReservedAndActualQuantity(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {

		// create order
		JSONObject testDataJson = dataFactory.getTestCaseParameters();
		String medid = testDataJson.getJSONArray("medicineId").getJSONObject(0)
				.optString("medid");
		String orderqty = testDataJson.getJSONArray("medicineId")
				.getJSONObject(0).optString("quantity");
		int oqty = Integer.parseInt(orderqty);
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		int reservedQty = getReservedQty(medid);
		validateActualQuantity(medid, defaultQtyIncrease);
		// update address
		updateAddressAndVerifyOrderDetails(dataFactory);
		validateActualQuantity(medid, (defaultQtyIncrease - oqty));
		validateReservedQuantity(medid, (reservedQty + oqty));
		new LogoutPage().logout();
		validateState(orderId, "FCAssigned");
		// FCAccept
		reservedQty = getReservedQty(medid);
		loginInAdminApp(this.fcUserName, this.password, true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.clickViewAssignedOrderLink();
		viewassignedorder.acceptOrder(dataFactory.getOrderid());
		validateState(orderId, "FCAccepted");
		validateActualQuantity(medid, (defaultQtyIncrease - oqty));
		validateReservedQuantity(medid, (reservedQty - oqty));
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void validateReservedAndActualQuantityAfterCancellation(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		// create order
		JSONObject testDataJson = dataFactory.getTestCaseParameters();
		String medid = testDataJson.getJSONArray("medicineId").getJSONObject(0)
				.optString("medid");
		int oqty = Integer.parseInt(testDataJson.getJSONArray("medicineId")
				.getJSONObject(0).optString("quantity"));
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		int reservedQty = getReservedQty(medid);
		validateActualQuantity(medid, defaultQtyIncrease);
		// update address
		updateAddressAndVerifyOrderDetails(dataFactory);
		validateActualQuantity(medid, (defaultQtyIncrease - oqty));
		validateReservedQuantity(medid, (reservedQty + oqty));
		new LogoutPage().logout();
		loginInAdminApp(this.fcUserName, this.password, true);
		validateState(orderId, "FCAssigned");
		// FCAccept
		reservedQty = getReservedQty(medid);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.clickViewAssignedOrderLink();
		viewassignedorder.acceptOrder(dataFactory.getOrderid());
		validateState(orderId, "FCAccepted");
		validateActualQuantity(medid, (defaultQtyIncrease - oqty));
		validateReservedQuantity(medid, (reservedQty - oqty));
		// Cancel order
		loginInAdminApp(this.cxUsername, this.password, true);
		CustomerCareSearchPageUtil cuspage = new CustomerCareSearchPageUtil();
		cuspage.cancelOrder(orderId);
		validateState(orderId, "CancelledPendingReturnWithFC");
		// Acknowledge CancelledReturnWithFC Order
		loginInAdminApp(dataFactory.getAEUsername(), dataFactory.getPassword(),
				true);
		FCManagement acknowledgeOrder = new FCManagement();
		acknowledgeOrder.acknowledgeCancelledOrder(orderId);
		Thread.sleep(1000);
		validateActualQuantity(medid, defaultQtyIncrease);
		validateReservedQuantity(medid, (reservedQty - oqty));
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void validateReservedAndActualQuantityForTwoBatches(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		JSONObject testDataJson = dataFactory.getTestCaseParameters();
		String medid = testDataJson.getJSONArray("medicineId").getJSONObject(0)
				.optString("medid");
		int oqty = Integer.parseInt(testDataJson.getJSONArray("medicineId")
				.getJSONObject(0).optString("quantity"));
		int len = testDataJson.getJSONArray("inventoryBatch").length();
		String[] batch = new String[len];
		Integer[] resQty = new Integer[len];

		for (int i = 0; i <= len - 1; i++) {
			System.out.println(testDataJson.getJSONArray("inventoryBatch")
					.getJSONObject(i).optString("batch"));
			batch[i] = testDataJson.getJSONArray("inventoryBatch")
					.getJSONObject(i).optString("batch");

		}

		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		for (int i = 0; i <= testDataJson.getJSONArray("inventoryBatch")
				.length() - 1; i++) {
			resQty[i] = getReservedQty(medid, batch[i]);
		}
		for (int i = 0; i <= testDataJson.getJSONArray("inventoryBatch")
				.length() - 1; i++) {
			validateActualQuantity(medid, batch[i], defaultQtyIncrease);
		}
		updateAddressAndVerifyOrderDetails(dataFactory);
		int[] qty = {
				(defaultQtyIncrease + defaultQtyIncrease) - oqty, 0 };
		for (int i = 0; i <= testDataJson.getJSONArray("inventoryBatch")
				.length() - 1; i++) {
			validateActualQuantity(medid, batch[i], qty[i]);
		}
		int rqty[] = { oqty - defaultQtyIncrease, defaultQtyIncrease };
		for (int i = 0; i <= testDataJson.getJSONArray("inventoryBatch")
				.length() - 1; i++) {
			validateReservedQuantity(medid, batch[i], resQty[i] + rqty[i]);
		}
		new LogoutPage().logout();
		loginInAdminApp(this.fcUserName, this.password, true);
		FCViewAssignedOrderPage fc = new FCViewAssignedOrderPage();
		fc.clickViewAssignedOrderLink();
		fc.validateMultipleBatchUIQty(orderId, batch);
		new LogoutPage().logout();
		validateState(orderId, "FCAssigned");
		for (int i = 0; i <= testDataJson.getJSONArray("inventoryBatch")
				.length() - 1; i++) {
			resQty[i] = getReservedQty(medid, batch[i]);
		}
		loginInAdminApp(this.fcUserName, this.password, true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.clickViewAssignedOrderLink();
		viewassignedorder.acceptOrder(dataFactory.getOrderid());
		validateState(orderId, "FCAccepted");
		for (int i = 0; i <= testDataJson.getJSONArray("inventoryBatch")
				.length() - 1; i++) {
			validateActualQuantity(medid, batch[i], qty[i]);
		}

		for (int i = 0; i <= testDataJson.getJSONArray("inventoryBatch")
				.length() - 1; i++) {
			validateReservedQuantity(medid, batch[i], resQty[i] - rqty[i]);
		}

		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void validateReservedAndActualQuantityForTwoBatchesAndCancelOrder(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		JSONObject testDataJson = dataFactory.getTestCaseParameters();
		String medid = testDataJson.getJSONArray("medicineId").getJSONObject(0)
				.optString("medid");
		int oqty = Integer.parseInt(testDataJson.getJSONArray("medicineId")
				.getJSONObject(0).optString("quantity"));
		int len = testDataJson.getJSONArray("inventoryBatch").length();
		String[] batch = new String[len];
		Integer[] resQty = new Integer[len];

		for (int i = 0; i <= len - 1; i++) {
			System.out.println(testDataJson.getJSONArray("inventoryBatch")
					.getJSONObject(i).optString("batch"));
			batch[i] = testDataJson.getJSONArray("inventoryBatch")
					.getJSONObject(i).optString("batch");
		}
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		for (int i = 0; i <= testDataJson.getJSONArray("inventoryBatch")
				.length() - 1; i++) {
			resQty[i] = getReservedQty(medid, batch[i]);
		}
		for (int i = 0; i <= testDataJson.getJSONArray("inventoryBatch")
				.length() - 1; i++) {
			validateActualQuantity(medid, batch[i], defaultQtyIncrease);
		}
		updateAddressAndVerifyOrderDetails(dataFactory);
		int[] qty = {
				(defaultQtyIncrease + defaultQtyIncrease) - oqty, 0 };
		for (int i = 0; i <= testDataJson.getJSONArray("inventoryBatch")
				.length() - 1; i++) {
			validateActualQuantity(medid, batch[i], qty[i]);
		}
		int rqty[] = { oqty - defaultQtyIncrease, defaultQtyIncrease };
		for (int i = 0; i <= testDataJson.getJSONArray("inventoryBatch")
				.length() - 1; i++) {
			validateReservedQuantity(medid, batch[i], resQty[i] + rqty[i]);
		}

		/*
		 * FCAssignedCancelledOrderPage fc=new FCAssignedCancelledOrderPage();
		 * fc.validateMultipleBatchUIQty(orderId,batch);
		 */
		new LogoutPage().logout();
		validateState(orderId, "FCAssigned");
		for (int i = 0; i <= testDataJson.getJSONArray("inventoryBatch")
				.length() - 1; i++) {
			resQty[i] = getReservedQty(medid, batch[i]);
		}
		loginInAdminApp(this.fcUserName, this.password, true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.clickViewAssignedOrderLink();
		viewassignedorder.acceptOrder(dataFactory.getOrderid());
		validateState(orderId, "FCAccepted");
		for (int i = 0; i <= testDataJson.getJSONArray("inventoryBatch")
				.length() - 1; i++) {
			validateActualQuantity(medid, batch[i], qty[i]);
		}

		for (int i = 0; i <= testDataJson.getJSONArray("inventoryBatch")
				.length() - 1; i++) {
			validateReservedQuantity(medid, batch[i], resQty[i] - rqty[i]);
		}

		// Cancel order
		loginInAdminApp(this.cxUsername, this.password, true);
		CustomerCareSearchPageUtil cxscreen = new CustomerCareSearchPageUtil();
		cxscreen.cancelOrder(orderId);
		validateState(orderId, "CancelledPendingReturnWithFC");
		// Acknowledge CancelledReturnWithFC Order
		loginInAdminApp(dataFactory.getAEUsername(), dataFactory.getPassword(),
				true);
		FCManagement acknowledgeOrder = new FCManagement();
		acknowledgeOrder.acknowledgeCancelledOrder(orderId);
		Thread.sleep(1000);
		for (int i = 0; i <= testDataJson.getJSONArray("inventoryBatch")
				.length() - 1; i++) {
			validateActualQuantity(medid, batch[i], defaultQtyIncrease);
		}
		for (int i = 0; i <= testDataJson.getJSONArray("inventoryBatch")
				.length() - 1; i++) {
			validateReservedQuantity(medid, batch[i], resQty[i] - rqty[i]);
		}
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void cancelOrderByCustomerSupportInDAAssignedPending(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("First order->" + orderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		loginAsFCAndAcceptOrder(dataFactory);
		createTripOnly(dataFactory.getDLUsername(), dataFactory.getPassword(),
				orderId);
		validateState(orderId, "DAAssignmentPending");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// Cancel Order
		loginInAdminApp(this.cxUsername, this.password, true);
		CustomerCareSearchPageUtil cuspage = new CustomerCareSearchPageUtil();
		cuspage.cancelOrder(orderId);
		validateState(orderId, "CancelledPendingReturnWithFC");
		loginInAdminApp(dataFactory.getAEUsername(), dataFactory.getPassword(),
				true);
		FCManagement acknowledgeOrder = new FCManagement();
		acknowledgeOrder.acknowledgeCancelledOrder(orderId);
		Thread.sleep(1000);
		String tripStatus = getTripStatus(tripid);
		Assertion.softAssertTrue(tripStatus.contains("Closed"),
				"Trip not closed succesfully after delivering orders in it");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void cancelOrderByCustomerSupportInDAAssigned(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {

		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("First order->" + orderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, tsaAgentName);
		validateState(orderId, "DAAssigned");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// Cancel Order
		loginInAdminApp(this.cxUsername, this.password, true);
		CustomerCareSearchPageUtil cxscreen = new CustomerCareSearchPageUtil();
		cxscreen.cancelOrder(orderId);
		validateState(orderId, "CancelledPendingReturnWithFC");
		loginInAdminApp(dataFactory.getAEUsername(), dataFactory.getPassword(),
				true);
		FCManagement acknowledgeOrder = new FCManagement();
		acknowledgeOrder.acknowledgeCancelledOrder(orderId);
		Thread.sleep(1000);
		String tripStatus = getTripStatus(tripid);
		Assertion.softAssertTrue(tripStatus.contains("Closed"),
				"Trip not closed succesfully after delivering orders in it");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void cancelOrderByCustomerSupportInDAPicked(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {

		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("First order->" + orderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			if (!dataFactory.getTestCaseParameters().optString("source")
					.equalsIgnoreCase("SLATE_RX"))
				SMSValidateUtil.getInstance().validateWelcomeMessage(
						"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, tsaAgentName);
		validateState(orderId, "DAAssigned");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsaActionPickTrip(serviceUrl, this.tsaAgentName, tripid,
				orderId);
		validateState(orderId, "DAPicked");
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// Cancel Order
		loginInAdminApp(this.cxUsername, this.password, true);
		CustomerCareSearchPageUtil cxscreen = new CustomerCareSearchPageUtil();
		cxscreen.cancelOrder(orderId);
		validateState(orderId, "CancelledPendingReturnWithDA");
		loginInAdminApp(dataFactory.getAEUsername(), dataFactory.getPassword(),
				true);
		FCManagement acknowledgeOrder = new FCManagement();
		acknowledgeOrder.acknowledgeCancelledOrder(orderId);
		Thread.sleep(1000);
		String tripStatus = getTripStatus(tripid);
		Assertion.softAssertTrue(tripStatus.contains("Closed"),
				"Trip not closed succesfully after delivering orders in it");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void cancelOrderByTSAAfterDAPicked(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {

		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("First order->" + orderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, tsaAgentName);
		validateState(orderId, "DAAssigned");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsaActionPickTrip(serviceUrl, this.tsaAgentName, tripid,
				orderId);
		validateState(orderId, "DAPicked");
		tsaaction.tsaCancelOrder(serviceUrl, orderId, tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		validateState(orderId, "CancelledPendingReturnWithDA");
		loginInAdminApp(dataFactory.getAEUsername(), dataFactory.getPassword(),
				true);
		FCManagement acknowledgeOrder = new FCManagement();
		acknowledgeOrder.acknowledgeCancelledOrder(orderId);
		Thread.sleep(1000);
		String tripStatus = getTripStatus(tripid);
		Assertion.softAssertTrue(tripStatus.contains("Closed"),
				"Trip not closed succesfully after delivering orders in it");
		Assertion.getVerificationFailures();
	}

	public String getDraftOrderId(TestDataFactory testDataFactory,
			String customerMobile) {
		String myDoc = mongoDb.getData("Customers", "mobile", customerMobile);
		JSONObject document = new JSONObject(myDoc);
		String custId = document.optString("_id");
		String orderId = getOrderIdFromCustomerId(custId);
		testDataFactory.setOrderid(orderId);
		Assertion.assertNotNull(orderId, "Order Id is null");
		System.out.println("Order Id = " + orderId);
		return orderId;
	}

	@Test(dataProvider = "TestData")
	public void cancelGoRxPrecriptionByTSA(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		SMSValidateUtil.getInstance().validateWelcomeMessageGoRx(
				"" + dataFactory.getMobileNo(), orderId);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, this.tsaAgentName);
		// validateState(orderId,"DAAssigned");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsaActionPickTrip(serviceUrl, this.tsaAgentName, tripid,
				orderId);
		tsaaction.tsaCancelPrescription(serviceUrl, orderId, tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		validateState(orderId, "Cancelled");
		String tripStatus = getTripStatus(tripid);
		Assertion
				.softAssertTrue(tripStatus.contains("Closed"),
						"Trip not closed succesfully after removing only order in trip");
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void rescheduleGoRxOrderInDAAssignmentPendingRxPending(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		SMSValidateUtil.getInstance().validateWelcomeMessageGoRx(
				"" + dataFactory.getMobileNo(), orderId);
		createTripOnly(dataFactory.getDLUsername(), dataFactory.getPassword(),
				orderId);
		validateState(orderId, "DAAssignmentPendingRxPending");
		String tripid = getTripId(orderId);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		SearchOrderPage cxsearchorder = new SearchOrderPage();
		cxsearchorder
				.rescheduleOrder(this.cxUsername, this.password, orderId);
		validateState(orderId, "RxPending");
		String tripStatus = getTripStatus(tripid);
		Assertion
				.softAssertTrue(tripStatus.contains("Closed"),
						"Trip not closed succesfully after removing only order in trip");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void rescheduleGoRxOrderInDAAssignedRxPending(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		SMSValidateUtil.getInstance().validateWelcomeMessageGoRx(
				"" + dataFactory.getMobileNo(), orderId);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, this.tsaAgentName);
		validateState(orderId, "DAAssignedRxPending");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		SearchOrderPage cxsearchorder = new SearchOrderPage();
		cxsearchorder
				.rescheduleOrder(this.cxUsername, this.password, orderId);
		logout.logout();
		validateState(orderId, "RxPending");
		String tripStatus = getTripStatus(tripid);
		Assertion
				.softAssertTrue(tripStatus.contains("Closed"),
						"Trip not closed succesfully after removing only order in trip");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void rescheduleGoRxOrderInDAPickedRxPending(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {

		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		SMSValidateUtil.getInstance().validateWelcomeMessageGoRx(
				"" + dataFactory.getMobileNo(), orderId);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, this.tsaAgentName);
		validateState(orderId, "DAAssignedRxPending");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsaActionPickTrip(serviceUrl, this.tsaAgentName, tripid,
				orderId);
		validateState(orderId, "DAPickedRxPending");
		LogoutPage logout = new LogoutPage();
		logout.logout();
		SearchOrderPage cxscreen = new SearchOrderPage();
		cxscreen.rescheduleOrder(this.cxUsername, this.password, orderId);
		logout.logout();
		validateState(orderId, "RxPending");
		String tripStatus = getTripStatus(tripid);
		Assertion
				.softAssertTrue(tripStatus.contains("Closed"),
						"Trip not closed succesfully after removing only order in trip");
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void removePrescriptionFromDAAssginementPendingTrip(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		SMSValidateUtil.getInstance().validateWelcomeMessageGoRx(
				"" + dataFactory.getMobileNo(), orderId);
		createTripOnly(dataFactory.getDLUsername(), dataFactory.getPassword(),
				orderId);
		validateState(orderId, "DAAssignmentPendingRxPending");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// remove order code
		loginInAdminApp(dataFactory.getPassword(), dataFactory.getPassword(),
				true);
		SearchTripPage stp = new SearchTripPage();
		stp.removeOrderFromTrip(tripid, orderId, "Trip/Order status updated");
		validateState(orderId, "RxPending");
		String tripStatus = getTripStatus(tripid);
		Assertion
				.softAssertTrue(tripStatus.contains("Closed"),
						"Trip not closed succesfully after removing only order in trip");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void removePrescriptionFromDAAssginedTrip(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		SMSValidateUtil.getInstance().validateWelcomeMessageGoRx(
				"" + dataFactory.getMobileNo(), orderId);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, this.tsaAgentName);
		validateState(orderId, "DAAssignedRxPending");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// remove order code
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		SearchTripPage stp = new SearchTripPage();
		stp.removeOrderFromTrip(tripid, orderId, "Trip/Order status updated");
		validateState(orderId, "RxPending");
		String tripStatus = getTripStatus(tripid);
		Assertion
				.softAssertTrue(tripStatus.contains("Closed"),
						"Trip not closed succesfully after removing only order in trip");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void rescheduleOrderByCustomerSupportInDAAssignedPending(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("First order->" + orderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		loginAsFCAndAcceptOrder(dataFactory);
		createTripOnly(dataFactory.getDLUsername(), dataFactory.getPassword(),
				orderId);
		validateState(orderId, "DAAssignmentPending");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// reschedule Order
		SearchOrderPage cxsearchorder = new SearchOrderPage();
		cxsearchorder
				.rescheduleOrder(this.cxUsername, this.password, orderId);
		validateState(orderId, "FCAccepted");
		logout.logout();
		Thread.sleep(1000);
		String tripStatus = getTripStatus(tripid);
		Assertion
				.softAssertTrue(tripStatus.contains("Closed"),
						"Trip not closed succesfully after rescheduling  only order in it");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void rescheduleOrderByCustomerSupportInDAAssigned(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("First order->" + orderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, this.tsaAgentName);
		validateState(orderId, "DAAssigned");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// reschedule Order
		SearchOrderPage cxsearchorder = new SearchOrderPage();
		cxsearchorder
				.rescheduleOrder(this.cxUsername, this.password, orderId);
		validateState(orderId, "FCAccepted");
		logout.logout();
		Thread.sleep(1000);
		String tripStatus = getTripStatus(tripid);
		Assertion
				.softAssertTrue(tripStatus.contains("Closed"),
						"Trip not closed succesfully after rescheduling only order in it");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void rescheduleOrderByCustomerSupportInDAPicked(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {

		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("First order->" + orderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		loginAsFCAndAcceptOrder(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, this.tsaAgentName);
		validateState(orderId, "DAAssigned");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsaActionPickTrip(serviceUrl, this.tsaAgentName, tripid,
				orderId);
		validateState(orderId, "DAPicked");
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// reschedule Order
		SearchOrderPage cxsearchorder = new SearchOrderPage();
		cxsearchorder
				.rescheduleOrder(this.cxUsername, this.password, orderId);
		validateState(orderId, "FCAccepted");
		logout.logout();
		Thread.sleep(1000);
		String tripStatus = getTripStatus(tripid);
		Assertion.softAssertTrue(tripStatus.contains("Closed"),
				"Trip not closed succesfully after delivering orders in it");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void removePrescriptionFromDAPickedTrip(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {

		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		SMSValidateUtil.getInstance().validateWelcomeMessageGoRx(
				"" + dataFactory.getMobileNo(), orderId);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, this.tsaAgentName);
		validateState(orderId, "DAAssignedRxPending");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsaActionPickTrip(serviceUrl, this.tsaAgentName, tripid,
				orderId);
		validateState(orderId, "DAPickedRxPending");
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// remove order code
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		SearchTripPage searchtrip = new SearchTripPage();
		searchtrip.removeOrderFromTrip(tripid, orderId,
				"Trip/Order status updated");
		validateState(orderId, "RxPending");
		String tripStatus = getTripStatus(tripid);
		Assertion
				.softAssertTrue(tripStatus.contains("Closed"),
						"Trip not closed succesfully after removing only order in trip");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void addPrescriptionToDAAssginementPendingTrip(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		// SMSValidateUtil.getInstance().validateWelcomeMessageGoRx("" +
		// dataFactory.getMobileNo(),orderId);
		createTripOnly(dataFactory.getDLUsername(), dataFactory.getPassword(),
				orderId);
		validateState(orderId, "DAAssignmentPendingRxPending");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// second order creation
		dataFactory.setOrderToBeCreated(true);
		String secondOrderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		// SMSValidateUtil.getInstance().validateWelcomeMessage(""+dataFactory.getMobileNo());
		// Adding Second Order to trip Code
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		SearchTripPage stp = new SearchTripPage();
		stp.addOrderToTrip(tripid, secondOrderId, "Trip/Order status updated");
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		CreateTripPage ctp = new CreateTripPage();
		ctp.assertOrderAdded(tripid, secondOrderId);
		validateState(secondOrderId, "DAAssignmentPendingRxPending");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void addPrescriptionToDAAssginedTrip(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		SMSValidateUtil.getInstance().validateWelcomeMessageGoRx(
				"" + dataFactory.getMobileNo(), orderId);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, this.tsaAgentName);
		validateState(orderId, "DAAssignedRxPending");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// second order creation
		dataFactory.setOrderToBeCreated(true);
		String secondOrderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		// SMSValidateUtil.getInstance().validateWelcomeMessage(""+dataFactory.getMobileNo());
		// Adding Second Order to trip Code
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		SearchTripPage stp = new SearchTripPage();
		stp.addOrderToTrip(tripid, secondOrderId, "Trip/Order status updated");
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		CreateTripPage ctp = new CreateTripPage();
		ctp.assertOrderAdded(tripid, secondOrderId);
		validateState(secondOrderId, "DAAssignedRxPending");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void addPrescriptionToDAPickedTrip(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		SMSValidateUtil.getInstance().validateWelcomeMessageGoRx(
				"" + dataFactory.getMobileNo(), orderId);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, this.tsaAgentName);
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsaActionPickTrip(serviceUrl, this.tsaAgentName, tripid,
				orderId);
		validateState(orderId, "DAPickedRxPending");
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// second order creation
		dataFactory.setOrderToBeCreated(true);
		String secondOrderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		// SMSValidateUtil.getInstance().validateWelcomeMessage(""+dataFactory.getMobileNo());
		// Adding Second Order to trip Code
		loginInAdminApp(dataFactory.getDLUsername(), this.password, true);
		SearchTripPage stp = new SearchTripPage();
		stp.addOrderToTrip(tripid, secondOrderId, "Trip/Order status updated");
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		CreateTripPage ctp = new CreateTripPage();
		ctp.assertOrderAdded(tripid, secondOrderId);
		validateState(secondOrderId, "DAPickedRxPending");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void cancelGoRxOrderInDAAssignmentPendingRxPending(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		SMSValidateUtil.getInstance().validateWelcomeMessageGoRx(
				"" + dataFactory.getMobileNo(), orderId);
		createTripOnly(dataFactory.getDLUsername(), dataFactory.getPassword(),
				orderId);
		validateState(orderId, "DAAssignmentPendingRxPending");
		String tripid = getTripId(orderId);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// Cancel Order
		loginInAdminApp(this.cxUsername, this.password, true);
		CustomerCareSearchPageUtil cxscreen = new CustomerCareSearchPageUtil();
		cxscreen.cancelOrder(orderId);
		validateState(orderId, "Cancelled");
		String tripStatus = getTripStatus(tripid);
		Assertion
				.softAssertTrue(tripStatus.contains("Closed"),
						"Trip not closed succesfully after removing only order in trip");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void cancelGoRxOrderInDAAssignedRxPending(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		SMSValidateUtil.getInstance().validateWelcomeMessageGoRx(
				"" + dataFactory.getMobileNo(), orderId);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, this.tsaAgentName);
		validateState(orderId, "DAAssignedRxPending");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// Cancel Order
		loginInAdminApp(this.cxUsername, this.password, true);
		CustomerCareSearchPageUtil cxscreen = new CustomerCareSearchPageUtil();
		cxscreen.cancelOrder(orderId);
		validateState(orderId, "Cancelled");
		String tripStatus = getTripStatus(tripid);
		Assertion
				.softAssertTrue(tripStatus.contains("Closed"),
						"Trip not closed succesfully after removing only order in trip");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void cancelGoRxOrderInDAPickedRxPending(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {

		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		SMSValidateUtil.getInstance().validateWelcomeMessageGoRx(
				"" + dataFactory.getMobileNo(), orderId);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, this.tsaAgentName);
		validateState(orderId, "DAAssignedRxPending");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsaActionPickTrip(serviceUrl, this.tsaAgentName, tripid,
				orderId);
		validateState(orderId, "DAPickedRxPending");
		LogoutPage logout = new LogoutPage();
		logout.logout();
		// Cancel Order
		loginInAdminApp(this.cxUsername, this.password, true);
		CustomerCareSearchPageUtil cxscreen = new CustomerCareSearchPageUtil();
		cxscreen.cancelOrder(orderId);
		validateState(orderId, "Cancelled");
		String tripStatus = getTripStatus(tripid);
		Assertion
				.softAssertTrue(tripStatus.contains("Closed"),
						"Trip not closed succesfully after removing only order in trip");

		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void changeTsaForDAAssignedRxPendingAndDeliverOrder(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {

		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		SMSValidateUtil.getInstance().validateWelcomeMessageGoRx(
				"" + dataFactory.getMobileNo(), orderId);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, this.tsaAgentName);
		validateState(orderId, "DAAssignedRxPending");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsalogin(serviceUrl, daAgentNameTwo);
		tsaaction.tsafree(serviceUrl);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		CreateTripPageUtil ctp = new CreateTripPageUtil();
		ctp.clickViewTrip();
		TripPageUtil tpu = new TripPageUtil();
		tpu.searchAndClickATrip(tripid);
		tpu.addDAAgentToTrip(daAgentNameTwo);
		validateState(orderId, "DAAssignedRxPending");
		String tsaId = getTsaFromTrip(tripid);
		String tsaName = getTsaName(tsaId);
		Assertion.softAssertTrue(tsaName.contains(daAgentNameTwo),
				"New Da agent  is not assinged to the trip");
		tripid = getTripId(orderId);
		tsaaction.tsaActionPickTrip(serviceUrl, this.daAgentNameTwo, tripid,
				orderId);
		tsaaction.tsaPickPPrescription(serviceUrl, orderId, tripid);
		String tripStatus = getTripStatus(tripid);
		Assertion.softAssertTrue(tripStatus.contains("Closed"),
				"Trip not closed succesfully after delivering orders in it");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void changeTsaForDAPickedRxPendingAndDeliverOrder(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		// Trip Creation Code
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		validateState(orderId, "RxPending");
		SMSValidateUtil.getInstance().validateWelcomeMessageGoRx(
				"" + dataFactory.getMobileNo(), orderId);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, orderId, this.tsaAgentName);
		validateState(orderId, "DAAssignedRxPending");
		String tripid = getTripId(orderId);
		System.out.println(tripid);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsaActionPickTrip(serviceUrl, this.tsaAgentName, tripid,
				orderId);
		validateState(orderId, "DAPickedRxPending");
		LogoutPage logout = new LogoutPage();
		logout.logout();
		tsaaction.tsalogin(serviceUrl, daAgentNameTwo);
		tsaaction.tsafree(serviceUrl);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		CreateTripPageUtil createtrip = new CreateTripPageUtil();
		createtrip.clickViewTrip();
		TripPageUtil tpu = new TripPageUtil();
		tpu.searchAndClickATrip(tripid);
		tpu.addDAAgentToTrip(daAgentNameTwo);
		validateState(orderId, "DAAssignedRxPending");
		String tsaId = getTsaFromTrip(tripid);
		String tsaName = getTsaName(tsaId);
		Assertion.softAssertTrue(tsaName.contains(daAgentNameTwo),
				"New Da agent  is not assinged to the trip");
		tripid = getTripId(orderId);
		tsaaction.tsaActionPickTrip(serviceUrl, this.daAgentNameTwo, tripid,
				orderId);
		tsaaction.tsaPickPPrescription(serviceUrl, orderId, tripid);
		String tripStatus = getTripStatus(tripid);
		Assertion.softAssertTrue(tripStatus.contains("Closed"),
				"Trip not closed succesfully after delivering orders in it");
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void createOrderFromAllSource(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.fillCustomerAddress(dataFactory, true);
		cxscreen.verifyTimeSlotUpdate(orderId);
		commonVerificationForScheduled(orderId);
		Assertion.getVerificationFailures();

	}

	@Test(dataProvider = "TestData")
	public void settleFundsFromFCToTsa(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		JSONObject testCaseParameters = dataFactory.getTestCaseParameters();
		loginInAdminApp(dataFactory.getAEUsername(), dataFactory.getPassword(),
				true);
		LeftNavigationPage leftNavPage = new LeftNavigationPage();
		leftNavPage.clickSettleFunds();
		SettleFunds sfund = new SettleFunds();
		String comment = generateRandomName();
		sfund.settleFunds(testCaseParameters.optString("settlementType"),
				testCaseParameters.optString("fromAccount"),
				testCaseParameters.optString("toAccount"),
				testCaseParameters.optString("amount"), comment);
		sfund.assertResultsPresent(testCaseParameters.optString("expoutput"));
		String tType = getTransactionType(comment);
		Assertion.softAssertTrue(tType.contentEquals("SETTLEMENT"),
				"Transaction type does not match");
		float amt = getTransactionAmount(comment);
		Assertion.softAssertEquals(amt,
				Float.parseFloat(testCaseParameters.optString("amount")),
				"Transaction amount does not match");
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void settleFundsFromTsaToFC(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		JSONObject testCaseParameters = dataFactory.getTestCaseParameters();
		loginInAdminApp(dataFactory.getAEUsername(), dataFactory.getPassword(),
				true);
		LeftNavigationPage leftNavPage = new LeftNavigationPage();
		leftNavPage.clickSettleFunds();
		SettleFunds sfund = new SettleFunds();
		String comment = generateRandomName();
		System.out.println(comment);
		sfund.settleFunds(testCaseParameters.optString("settlementType"),
				testCaseParameters.optString("fromAccount"),
				testCaseParameters.optString("toAccount"),
				testCaseParameters.optString("amount"), comment);
		sfund.assertResultsPresent(testCaseParameters.optString("expoutput"));
		Thread.sleep(1000);
		String tType = getTransactionType(comment);
		Assertion.softAssertTrue(tType.contentEquals("SETTLEMENT"),
				"Transaction type does not match");
		float amt = getTransactionAmount(comment);
		Assertion.softAssertEquals(amt,
				Float.parseFloat(testCaseParameters.optString("amount")),
				"Transaction amount does not match");
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void settleFundsFromFCToMedlife(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		JSONObject testCaseParameters = dataFactory.getTestCaseParameters();
		loginInAdminApp(dataFactory.getAEUsername(), dataFactory.getPassword(),
				true);
		LeftNavigationPage leftNavPage = new LeftNavigationPage();
		leftNavPage.clickSettleFunds();
		SettleFunds sfund = new SettleFunds();
		String comment = generateRandomName();
		sfund.settleFunds(testCaseParameters.optString("settlementType"),
				testCaseParameters.optString("fromAccount"),
				testCaseParameters.optString("toAccount"),
				testCaseParameters.optString("amount"), comment);
		sfund.assertResultsPresent(testCaseParameters.optString("expoutput"));
		String tType = getTransactionType(comment);
		Assertion.softAssertTrue(tType.contentEquals("SETTLEMENT"),
				"Transaction type does not match");
		float amt = getTransactionAmount(comment);
		Assertion.softAssertEquals(amt,
				Float.parseFloat(testCaseParameters.optString("amount")),
				"Transaction amount does not match");
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void settleFundsFromMedlifeToFC(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		JSONObject testCaseParameters = dataFactory.getTestCaseParameters();
		loginInAdminApp(dataFactory.getAEUsername(), dataFactory.getPassword(),
				true);
		LeftNavigationPage leftNavPage = new LeftNavigationPage();
		leftNavPage.clickSettleFunds();
		SettleFunds sfund = new SettleFunds();
		String comment = "settle" + generateRandomName();
		sfund.settleFunds(testCaseParameters.optString("settlementType"),
				testCaseParameters.optString("fromAccount"),
				testCaseParameters.optString("toAccount"),
				testCaseParameters.optString("amount"), comment);
		sfund.assertResultsPresent(testCaseParameters.optString("expoutput"));
		String tType = getTransactionType(comment);
		Assertion.softAssertTrue(tType.contentEquals("SETTLEMENT"),
				"Transaction type does not match");
		float amt = getTransactionAmount(comment);
		Assertion.softAssertEquals(amt,
				Float.parseFloat(testCaseParameters.optString("amount")),
				"Transaction amount does not match");
		Assertion.getVerificationFailures();
	}

	// Order Flow though DB till FC Accepted

	public void orderFlowThroughDb(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, defaultQtyIncrease);
		System.out.println("First order->" + orderId);
		if (!dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX"))
			SMSValidateUtil.getInstance().validateWelcomeMessage(
					"" + dataFactory.getMobileNo(), dataFactory);
		// update order to FC Assigned
		updateOrderToFCAssigned(dataFactory.getTestCaseParameters(), orderId);
		// update order to FC Accepted
		updateOrderState(orderId, "FCAccepted");
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void createRefillOrder(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, 30);
		System.out.println("OrderId------>: " + orderId);
		String rxID = orderId.replace("OR", "Rx");
		System.out.println("Prescription ID==" + rxID);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.fillCustomerAddress(dataFactory, true);
		cxscreen.updateOrder();
		loginInAdminApp(cxUsername, password, true);
		cxscreen.clickViewPendingOrders();
		cxscreen = new CustomerCarePageUtil();
		cxscreen.clickRefillTab();
		RefillOrder refillordertab = new RefillOrder();
		refillordertab.clickViewEditPrescriptionLink(rxID);
		refillordertab.clickUpdateOrderButton();
		refillordertab.clickPopupOkButton();
		String secondOrderID = refillordertab.validateRefillOrderCreated(rxID);
		validateState(secondOrderID, "FCAssigned");
		SMSValidateUtil.getInstance().validateWelcomeMessagewithOrderID(
				secondOrderID, dataFactory.getMobileNo() + "");
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void cancelCurrentRefillOrder(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, 30);
		System.out.println("OrderId------>: " + orderId);
		String rxID = orderId.replace("OR", "Rx");
		System.out.println("Prescription ID==" + rxID);
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickViewEditOrderLink(orderId);
		cxscreen.fillCustomerAddress(dataFactory, true);
		cxscreen.updateOrder();
		loginInAdminApp(cxUsername, password, true);
		cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickRefillTab();
		RefillOrder refillordertab = new RefillOrder();
		refillordertab.clickViewEditPrescriptionLink(rxID);
		refillordertab.clickCancelCurrentRefillButton();
		SMSValidateUtil.getInstance().validateCancelCurrentRefillOrderMessage(
				dataFactory.getMobileNo() + "");
		Assertion.getVerificationFailures();
	}

	@Test(dataProvider = "TestData")
	public void cancelAllRefillOrder(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, 30);
		System.out.println("OrderId------>: " + orderId);
		String rxID = orderId.replace("OR", "Rx");
		System.out.println("Prescription ID==" + rxID);
		// loginInAdminAppWithSuper(cxUsername, password);
		// CustomerCarePageUtil ccpu = new CustomerCarePageUtil();
		// ccpu.clickViewEditOrderLink(orderId);
		// ccpu.fillCustomerAddress(dataFactory.getTestCaseParameters().getJSONObject("address"),
		// true);
		// ccpu.updateOrder();
		loginInAdminApp(cxUsername, password, true);
		CustomerCarePageUtil cxscreen = new CustomerCarePageUtil();
		cxscreen.clickViewPendingOrders();
		cxscreen.clickRefillTab();
		RefillOrder refillordertab = new RefillOrder();
		refillordertab.clickViewEditPrescriptionLink(rxID);
		refillordertab.clickCancelAllRefillButton();
		SMSValidateUtil.getInstance().validateCancelCurrentRefillOrderMessage(
				dataFactory.getMobileNo() + "");
		Assertion.getVerificationFailures();
	}

	// Pickup Rx, Deliver and Close SlateRx, ZipRx, GoRx, GPCS, CorporateRx,
	// YMRx, TATARx for Bangalore & Mumbai orders
	/*
	 * @Test(dataProvider = "TestData") public void
	 * pickRxAndOrderDeliveryFlow1(TestDataFactory dataFactory) throws
	 * NotCurrentPageException, IOException, InterruptedException {
	 * getLoginCredentials(dataFactory); if
	 * (dataFactory.getTestCaseParameters().
	 * optString("source").equalsIgnoreCase("GO_RX_CTO") ||
	 * dataFactory.getTestCaseParameters
	 * ().optString("source").equalsIgnoreCase("GO_RX_WTO")) {
	 * createGPCSRequestAndUploadImagetoDigiQ(dataFactory);
	 * //SMSValidateUtil.getInstance
	 * ().validateGPCSRxChecklistMessage(dataFactory);
	 * //SMSValidateUtil.getInstance
	 * ().validateGPCSRxSubmitReqMessage(dataFactory); } String orderId =
	 * addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory,100); if
	 * (dataFactory
	 * .getTestCaseParameters().optString("source").equalsIgnoreCase(
	 * "GO_RX_CTO") ||
	 * dataFactory.getTestCaseParameters().optString("source").equalsIgnoreCase
	 * ("GO_RX_WTO")) { validateState(dataFactory.getOrderid(), "Cancelled");
	 * checkCancelReasonForDraftOrderId(dataFactory.getOrderid());
	 * validateState(orderId, "FCAssigned");
	 * dataFactory.setOrderToBeCreated(false); dataFactory.setOrderid(orderId);
	 * } //System.out.println("Order Id = " + orderId);
	 * //SMSValidateUtil.getInstance().validateWelcomeMessage( // "" +
	 * dataFactory.getMobileNo(),dataFactory);
	 * dataFactory.setOrderToBeCreated(false); dataFactory.setOrderid(orderId);
	 * if
	 * (dataFactory.getTestCaseParameters().optString("source").equalsIgnoreCase
	 * ("SLATE_RX") ||
	 * dataFactory.getTestCaseParameters().optString("source").equalsIgnoreCase
	 * ("ZIP_RX")) { validateState(orderId, "Received");
	 * loginAsCXAndUpdateOrder(dataFactory); if
	 * (dataFactory.getTestCaseParameters().getJSONObject("address")
	 * .optString("city").equalsIgnoreCase("Mumbai")) { validateState(orderId,
	 * "RxPending"); pickPrescriptionFlow(dataFactory); } } if
	 * (dataFactory.getTestCaseParameters
	 * ().optString("source").equalsIgnoreCase("YM_RX") ||
	 * dataFactory.getTestCaseParameters
	 * ().optString("source").equalsIgnoreCase("TATA_RX")) {
	 * validateState(orderId, "Received"); loginAsCXAndUpdateOrder(dataFactory);
	 * validateState(orderId, "RxPending"); pickPrescriptionFlow(dataFactory); }
	 * if
	 * (dataFactory.getTestCaseParameters().optString("source").equalsIgnoreCase
	 * ("ONEMG_RX") ||
	 * dataFactory.getTestCaseParameters().optString("source").equalsIgnoreCase
	 * ("MEDIDAILI_RX")) { validateState(orderId, "RxPending");
	 * pickPrescriptionFlow(dataFactory); validateState(orderId, "Rescheduled");
	 * loginInAdminApp(dataFactory.getFCUsername(), dataFactory.getPassword(),
	 * true); FCViewAssignedOrderPage viewassignedorder = new
	 * FCViewAssignedOrderPage();
	 * viewassignedorder.clickViewAssignedOrderLink();
	 * viewassignedorder.moveRescheduledOrdertoFC(orderId); } if
	 * (dataFactory.getTestCaseParameters
	 * ().optString("source").equalsIgnoreCase("CORPORATE_RX")) {
	 * validateState(orderId, "FCAssigned"); } if
	 * (dataFactory.getTestCaseParameters
	 * ().optString("source").equalsIgnoreCase("GO_RX")) {
	 * validateState(orderId, "RxPending"); pickPrescriptionFlow(dataFactory); }
	 * validateState(orderId, "FCAssigned");
	 * loginAsFCAndAcceptOrder(dataFactory); validateState(orderId,
	 * "FCAccepted");
	 * //SMSValidateUtil.getInstance().validateFCAcceptedMessage(dataFactory);
	 * loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
	 * true); createTripAndAssignDAAgent(serviceUrl, orderId,
	 * dataFactory.getDAAgentName()); validateState(orderId, "DAAssigned");
	 * String tripid = getTripId(orderId); System.out.println(tripid);
	 * TSAServicecalls tsaaction = new TSAServicecalls();
	 * tsaaction.tsaDeliverOrder(serviceUrl, tripid, dataFactory);
	 * validateState(orderId, "Delivered"); String tripStatus =
	 * getTripStatus(tripid);
	 * Assertion.softAssertTrue(tripStatus.contains("Closed"),
	 * "Trip not closed succesfully after delivering orders in it");
	 * loginInAdminApp(dataFactory.getAEUsername(), dataFactory.getPassword(),
	 * true); FinanceMgntCashAndReturnsPage cashreturns = new
	 * FinanceMgntCashAndReturnsPage();
	 * System.out.println("Order to be closed = "+orderId);
	 * cashreturns.updateOrder(orderId); validateState(orderId, "Closed");
	 * Assertion.getVerificationFailures(); }
	 */

	/*
	 * Pick Rx flow
	 */
	public void pickPrescriptionFlow(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		getLoginCredentials(dataFactory);
		if (dataFactory.getTestCaseParameters().optString("fcType")
				.equalsIgnoreCase("POJO")) {
			loginInAdminApp(dataFactory.getFCUsername(),
					dataFactory.getPassword(), true);
			FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
			viewassignedorder.clickViewAssignedOrderLink();
			viewassignedorder.moveRxPendingToFC(dataFactory.getOrderid());
			validateState(dataFactory.getOrderid(), "FCAssigned");
		} else {
			loginInAdminApp(dataFactory.getDLUsername(),
					dataFactory.getPassword(), true);
			createTripAndAssignDAAgent(serviceUrl, dataFactory.getOrderid(),
					dataFactory.getDAAgentName());
			validateState(dataFactory.getOrderid(), "DAAssignedRxPending");
			String tripid = getTripId(dataFactory.getOrderid());
			System.out.println(tripid);
			TSAServicecalls tsaaction = new TSAServicecalls();
			tsaaction.tsaActionPickTrip(serviceUrl,
					dataFactory.getDAAgentName(), tripid,
					dataFactory.getOrderid());
			tsaaction.tsaPickPPrescription(serviceUrl,
					dataFactory.getOrderid(), tripid);
			LogoutPage logout = new LogoutPage();
			logout.logout();
			// validateState(dataFactory.getOrderid(), "FCAssigned");
			String tripStatus = getTripStatus(tripid);
			Assertion
					.softAssertTrue(tripStatus.contains("Closed"),
							"Trip not closed succesfully after removing only order in trip");
		}
		Assertion.getVerificationFailures();
	}

	public void createGPCSRequestAndUploadImagetoDigiQ(
			TestDataFactory dataFactory) throws NotCurrentPageException,
			IOException, InterruptedException {
		loginInAdminApp(cxUsername, password, true);
		CreateGPCSRequestPage createreq = new CreateGPCSRequestPage();
		String mobileNo = createreq.createRequest(dataFactory);
		String draftOrderId = getDraftOrderId(dataFactory, mobileNo);
		System.out.println("Mobile No. = " + mobileNo);
		validateState(draftOrderId, "RxPending");
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		String tripId = createTripAndAssignDAAgent(serviceUrl, draftOrderId,
				dataFactory.getDAAgentName());
		validateState(draftOrderId, "DAAssignedRxPending");
		dataFactory.setTripId(tripId);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsaPickRx(serviceUrl, draftOrderId, dataFactory.getTripId());
		validateState(draftOrderId, "DAPickedRxPending");
		uploadGoRxGPCSImagetoDigiQ(dataFactory);
		dataFactory.setOrderid(draftOrderId);
	}

	/*
	 * PickUp Rx and Order delivery for both Non-OTC and OTC medicines for all
	 * Rx source
	 */
	@Test(dataProvider = "TestData")
	public void pickRxAndOrderDeliveryFlow1(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		getLoginCredentials(dataFactory);
		// Create GPCS from Admin Web App
		// JSONArray
		// arr1=dataFactory.getTestCaseParameters().getJSONArray("medicineId");
		/*
		 * if
		 * (dataFactory.getTestCaseParameters().optString("source").equalsIgnoreCase
		 * ("GO_RX_CTO") ||
		 * dataFactory.getTestCaseParameters().optString("source"
		 * ).equalsIgnoreCase("GO_RX_WTO")) {
		 * createGPCSRequestAndUploadImagetoDigiQ(dataFactory);
		 * //SMSValidateUtil
		 * .getInstance().validateGPCSRxChecklistMessage(dataFactory);
		 * //SMSValidateUtil
		 * .getInstance().validateGPCSRxSubmitReqMessage(dataFactory); }
		 */
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, 100);
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("GO_RX_CTO")
				|| dataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("GO_RX_WTO")) {
			dataFactory.setOrderid(orderId);
			validateState(dataFactory.getOrderid(), "RxPending");
			pickPrescriptionFlow(dataFactory);
			// checkCancelReasonForDraftOrderId(dataFactory.getOrderid());
			// validateState(orderId, "FCAssigned");
			dataFactory.setOrderToBeCreated(false);
		}
		// System.out.println("Order Id = " + orderId);
		// SMSValidateUtil.getInstance().validateWelcomeMessage(
		// "" + dataFactory.getMobileNo(),dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX")
				|| dataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("ZIP_RX")
				|| dataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("EXTERNAL_RX")) {
			validateState(orderId, "Received");
			loginAsCXAndUpdateOrder(dataFactory);
			if (dataFactory.getTestCaseParameters().getJSONObject("address")
					.optString("city").equalsIgnoreCase("Mumbai")) {
				validateState(orderId, "RxPending");
				pickPrescriptionFlow(dataFactory);
				if (getOrderState(orderId) == "Rescheduled") {
					validateState(orderId, "Rescheduled");
					loginInAdminApp(dataFactory.getFCUsername(),
							dataFactory.getPassword(), true);
					FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
					viewassignedorder.clickViewAssignedOrderLink();
					viewassignedorder.moveRescheduledOrdertoFC(orderId);
				}
			}
			// validateState(orderId, "FCAssigned");
		}
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("YM_RX")
				|| dataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("TATA_RX")) {
			validateState(orderId, "Received");
			loginAsCXAndUpdateOrder(dataFactory);
			validateState(orderId, "RxPending");
			pickPrescriptionFlow(dataFactory);
		}
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("ONEMG_RX")
				|| dataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("MEDIDAILI_RX")) {
			validateState(orderId, "RxPending");
			pickPrescriptionFlow(dataFactory);
			if (getOrderState(dataFactory.getOrderid()).equalsIgnoreCase(
					"Rescheduled")) {
				validateState(dataFactory.getOrderid(), "Rescheduled");
				loginInAdminApp(dataFactory.getFCUsername(),
						dataFactory.getPassword(), true);
				FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
				viewassignedorder.clickViewAssignedOrderLink();
				viewassignedorder.moveRescheduledOrdertoFC(orderId);
			}
		}
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("GO_RX")) {
			validateState(orderId, "RxPending");
			pickPrescriptionFlow(dataFactory);
		}
		if (getOrderState(dataFactory.getOrderid()).equalsIgnoreCase(
				"Rescheduled")) {
			validateState(dataFactory.getOrderid(), "Rescheduled");
			loginInAdminApp(dataFactory.getFCUsername(),
					dataFactory.getPassword(), true);
			FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
			viewassignedorder.clickViewAssignedOrderLink();
			viewassignedorder.moveRescheduledOrdertoFC(orderId);
		}
		validateState(dataFactory.getOrderid(), "FCAssigned");
		loginAsFCAndAcceptOrder(dataFactory);
		validateState(dataFactory.getOrderid(), "FCAccepted");
		// SMSValidateUtil.getInstance().validateFCAcceptedMessage(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		if (dataFactory.getTestCaseParameters().optString("fcType")
				.equalsIgnoreCase("POJO")) {
			deliverEPOrder(dataFactory.getOrderid());
		} else {
			createTripAndAssignDAAgent(serviceUrl, dataFactory.getOrderid(),
					dataFactory.getDAAgentName());
			validateState(dataFactory.getOrderid(), "DAAssigned");
			String tripid = getTripId(dataFactory.getOrderid());
			System.out.println(tripid);
			TSAServicecalls tsaaction = new TSAServicecalls();
			tsaaction.tsaDeliverOrder(serviceUrl, tripid, dataFactory);
			validateState(dataFactory.getOrderid(), "Delivered");
			String tripStatus = getTripStatus(tripid);
			Assertion
					.softAssertTrue(tripStatus.contains("Closed"),
							"Trip not closed succesfully after delivering orders in it");
		}
		loginInAdminApp(dataFactory.getAEUsername(), dataFactory.getPassword(),
				true);
		FinanceMgntCashAndReturnsPage cashreturns = new FinanceMgntCashAndReturnsPage();
		System.out.println("Order to be closed = " + dataFactory.getOrderid());
		cashreturns.closeOrder(dataFactory.getOrderid());
		validateState(dataFactory.getOrderid(), "Closed");
		Assertion.getVerificationFailures();
	}

	/*
	 * Order delivery flow for OTC medicines for all Rx source
	 */
	@Test(dataProvider = "TestData")
	public void OTCOrderDeliveryFlow(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		getLoginCredentials(dataFactory);
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(
				dataFactory, 100);
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("GO_RX_CTO")
				|| dataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("GO_RX_WTO")) {
			dataFactory.setOrderid(orderId);
			validateState(dataFactory.getOrderid(), "RxPending");
			pickPrescriptionFlow(dataFactory);
			dataFactory.setOrderToBeCreated(false);
		}
		// SMSValidateUtil.getInstance().validateWelcomeMessage(
		// "" + dataFactory.getMobileNo(),dataFactory);
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX")
				|| dataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("ZIP_RX")
				|| dataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("EXTERNAL_RX")) {
			validateState(orderId, "Received");
			loginAsCXAndUpdateOrder(dataFactory);
			if (getOrderState(dataFactory.getOrderid()).equalsIgnoreCase(
					"Rescheduled")) {
				validateState(dataFactory.getOrderid(), "Rescheduled");
				loginInAdminApp(dataFactory.getFCUsername(),
						dataFactory.getPassword(), true);
				FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
				viewassignedorder.clickViewAssignedOrderLink();
				viewassignedorder.moveRescheduledOrdertoFC(orderId);
			}
			// validateState(orderId, "FCAssigned");
		}
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("YM_RX")
				|| dataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("TATA_RX")) {
			validateState(orderId, "Received");
			loginAsCXAndUpdateOrder(dataFactory);
		}
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("ONEMG_RX")
				|| dataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("MEDIDAILI_RX")) {
			if (getOrderState(dataFactory.getOrderid()).equalsIgnoreCase(
					"Rescheduled")) {
				validateState(dataFactory.getOrderid(), "Rescheduled");
				loginInAdminApp(dataFactory.getFCUsername(),
						dataFactory.getPassword(), true);
				FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
				viewassignedorder.clickViewAssignedOrderLink();
				viewassignedorder.moveRescheduledOrdertoFC(orderId);
			}
		}
		if (getOrderState(dataFactory.getOrderid()).equalsIgnoreCase(
				"Rescheduled")) {
			validateState(dataFactory.getOrderid(), "Rescheduled");
			loginInAdminApp(dataFactory.getFCUsername(),
					dataFactory.getPassword(), true);
			FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
			viewassignedorder.clickViewAssignedOrderLink();
			viewassignedorder.moveRescheduledOrdertoFC(orderId);
		}
		validateState(dataFactory.getOrderid(), "FCAssigned");
		loginAsFCAndAcceptOrder(dataFactory);
		validateState(dataFactory.getOrderid(), "FCAccepted");
		// SMSValidateUtil.getInstance().validateFCAcceptedMessage(dataFactory);
		loginInAdminApp(dataFactory.getDLUsername(), dataFactory.getPassword(),
				true);
		createTripAndAssignDAAgent(serviceUrl, dataFactory.getOrderid(),
				dataFactory.getDAAgentName());
		validateState(dataFactory.getOrderid(), "DAAssigned");
		String tripid = getTripId(dataFactory.getOrderid());
		System.out.println(tripid);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsaDeliverOrder(serviceUrl, tripid, dataFactory);
		validateState(dataFactory.getOrderid(), "Delivered");
		String tripStatus = getTripStatus(tripid);
		Assertion.softAssertTrue(tripStatus.contains("Closed"),
				"Trip not closed succesfully after delivering orders in it");
		loginInAdminApp(dataFactory.getAEUsername(), dataFactory.getPassword(),
				true);
		FinanceMgntCashAndReturnsPage cashreturns = new FinanceMgntCashAndReturnsPage();
		System.out.println("Order to be closed = " + dataFactory.getOrderid());
		cashreturns.closeOrder(dataFactory.getOrderid());
		validateState(dataFactory.getOrderid(), "Closed");
		Assertion.getVerificationFailures();
	}

	/*
	 * Rx return flow when Order is cancelled
	 */
	@Test(dataProvider = "TestData")
	public void rxReturnFlowForCancelledOrder(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException, InterruptedException {
		getLoginCredentials(dataFactory);
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("GO_RX_CTO")
				|| dataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("GO_RX_WTO")) {
			createGPCSRequestAndUploadImagetoDigiQ(dataFactory);
			SMSValidateUtil.getInstance().validateGPCSRxChecklistMessage(
					dataFactory);
			SMSValidateUtil.getInstance().validateGPCSRxSubmitReqMessage(
					dataFactory);
		}
		String orderId = addInventoryAndCreatePrescriptionAndReturnOrderId(dataFactory);
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("GO_RX_CTO")
				|| dataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("GO_RX_WTO")) {
			validateState(dataFactory.getOrderid(), "Cancelled");
			checkCancelReasonForDraftOrderId(dataFactory.getOrderid());
			validateState(orderId, "FCAssigned");
			dataFactory.setOrderToBeCreated(false);
			dataFactory.setOrderid(orderId);
		}
		dataFactory.setOrderToBeCreated(false);
		dataFactory.setOrderid(orderId);
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX")
				|| dataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("ZIP_RX")) {
			validateState(orderId, "Received");
			loginAsCXAndUpdateOrder(dataFactory);
			if (dataFactory.getTestCaseParameters().getJSONObject("address")
					.optString("city").equalsIgnoreCase("Mumbai")) {
				validateState(orderId, "RxPending");
				pickPrescriptionFlow(dataFactory);
			}
		}
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("YM_RX")
				|| dataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("TATA_RX")) {
			validateState(orderId, "Received");
			loginAsCXAndUpdateOrder(dataFactory);
			validateState(orderId, "RxPending");
			pickPrescriptionFlow(dataFactory);
		}
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("ONEMG_RX")) {
			validateState(orderId, "RxPending");
			pickPrescriptionFlow(dataFactory);
			validateState(orderId, "Rescheduled");
			loginInAdminApp(dataFactory.getFCUsername(),
					dataFactory.getPassword(), true);
			FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
			viewassignedorder.clickViewAssignedOrderLink();
			viewassignedorder.moveRescheduledOrdertoFC(orderId);
		}
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("CORPORATE_RX")) {
			validateState(orderId, "FCAssigned");
		}
		if (dataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("GO_RX")) {
			validateState(orderId, "RxPending");
			pickPrescriptionFlow(dataFactory);
		}
		orderId = dataFactory.getOrderid();
		System.out.println(orderId);
		loginInAdminApp(dataFactory.getFCUsername(), dataFactory.getPassword(),
				true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.clickViewAssignedOrderLink();
		viewassignedorder.clickViewEditOrderLink(orderId);
		viewassignedorder.cancelOrder();
		validateState(orderId, "Cancelled");
		Assertion.softAssertTrue(checkIsRxRequiredFlag(orderId), true);
		SMSValidateUtil.getInstance().validateFCCanceledMessage(orderId,
				"" + dataFactory.getMobileNo());
		loginInAdminApp(dataFactory.getAEUsername(), dataFactory.getPassword(),
				true);
		FCSearchOrdersPage fcsearchorderscreen = new FCSearchOrdersPage();
		fcsearchorderscreen.clickSearchOrderLink();
		fcsearchorderscreen.returnRx(orderId);
		validateState(orderId, "Cancelled");
		checkIsRxRequiredFlag(orderId);
		Assertion.softAssertTrue(checkIsRxRequiredFlag(orderId), false);
		Assertion.getVerificationFailures();
	}

	@Test
	public void compareWarFiles() throws IOException {
		System.out.println("Running WAR comaprision for " + newWARFile + " \n"
				+ oldWARFile);
		WARComparisionUtility warComp = new WARComparisionUtility();
		warComp.report(newWARFile, oldWARFile);
	}
}
