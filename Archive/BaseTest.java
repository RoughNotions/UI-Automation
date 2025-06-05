package com.medlife.qa.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.SimpleTimeZone;

import junit.framework.Assert;
import jxl.Workbook;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gargoylesoftware.htmlunit.util.StringUtils;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.Base64;
import com.medlife.qa.dataprovider.util.NewExcelUtil;
import com.medlife.qa.dataprovider.util.NewMongoDbUtil;
import com.medlife.qa.dataprovider.util.TestDataFactory;
import com.medlife.qa.driver.CustomWebDriver;
import com.medlife.qa.pages.admin.LogInPage;
import com.medlife.qa.pages.slaterxui.SlateRxUIHomePage;
import com.medlife.qa.pages.slaterxui.SlateRxUILoginPage;
import com.medlife.qa.pageutil.NotCurrentPageException;
import com.medlife.qa.pageutil.WebPage;
import com.medlife.qa.util.SMSValidateUtil;
import com.medlife.qa.util.TestDataGenerator;
import com.medlife.qa.util.WebTestRunListener;
import com.medlife.service.util.Services;
import com.medlife.qa.pages.admin.LeftNavigationPage;
import com.medlife.qa.pages.admin.LogoutPage;
import com.medlife.qa.pages.admin.TSAServicecalls;
import com.medlife.qa.pages.admin.fcmanagement.FCViewAssignedOrderPage;
import com.medlife.qa.pages.admin.fcmanagement.FinanceMgntCashAndReturnsPage;
import com.medlife.qa.pages.admin.finanacemanagement.AccountsummaryFC;
import com.medlife.qa.pages.admin.finanacemanagement.AccountsummaryTSA;
import com.medlife.qa.pages.admin.ordermanagement.CustomerCarePageUtil;
import com.medlife.qa.pages.admin.trips.CreateTripPage;
import com.medlife.qa.pages.admin.trips.CreateTripPageUtil;
import com.medlife.qa.pages.admin.trips.TripPageUtil;
import com.medlife.qa.pages.digidesk.DigideskLoginPage;
import com.medlife.qa.pages.doctorapp.DoctorAppLoginPage;

public class BaseTest extends TestPlan {

	public static String serviceUrl;
	public String mongoDbUrl;
	public String mongoDbPort;
	protected String doctorLoginWebServiceUrl;
	protected String doctorInfoWebServiceUrl;
	protected static String pharmacistLoginWebServiceUrl;
	public String adminAppUrl;
	protected String doctorAppUrl;
	protected String createPrescriptionUrl;
	public static NewMongoDbUtil mongoDb;
	protected String doctorAppMobileNo;
	protected String doctorAppPassword;
	protected static String pharmacistUserName;
	protected static String pharmacistPassword;
	protected String oneMgPharmacistUserName;
	protected String oneMgPharmacistPassword;
	protected String oneMgCXUserName;
	protected String oneMgCXPassword;
	protected static String pharmacistCreatePrescription;
	protected String pharmacistEditPrescription;
	protected String digideskAppUrl;
	protected String slateRxUrl;
	protected String oneMgDigixUrl;
	protected String noofpages;
	private String mobileNumber;
	protected String doctorCreatePrescription;
	// GORX RELATED
	private String createOtpUrl;
	private String verifyOtpUrl;
	private String createDraftRxUrl;
	private String uploadImageUrl;
	private String corporateRxImage1Data;
	private String corporateRxImage2Data;
	private String corporateRxImage3Data;
	private String corporateRxImage4Data;
	private String goRxImage0;
	private String goRxImage1;
	private String goRxImage2;
	private String goRxImage3;
	private String goRxCTOImageData;
	private String goRxWTOImageData;
	protected String downloadedPath;
	protected String customerMobileNumber;
	protected String customerName;
	private String tsaLoginUrl;
	private String addCorporateUrl;
	private String createSlatePDFUrl;
	private String uploadCorpImageUrl;
	public String tsaUserName;
	private String getDraftRxIDUrl;
	private String uploadImagesToDraftRxUrl;
	public String deliveryChallanPrintUrl;
	public String insurancePrintUrl;
	public String fcUserName,dlUserName,aeUserName,tsaAgentName;
	public String password;
	public String type,addressLine1,addressLine2,locality;
	public String city, state, pincode, stdcode;
	public String cxUsername;
	protected String sheetName;
	protected String doctorId;
	public static Properties CONFIG = null;
	public String oldWARFile;
	public String newWARFile;
	
	public static final String slateRxPDF
    = "src/main/java/com/medlife/resources/slaterx.pdf";
    public static final String externalRxPDF
    = "src/main/java/com/medlife/resources/externalrx.pdf";

	@BeforeSuite(alwaysRun = true)
	public void setupBeforeSuite(ITestContext context) {
		String mongoDbHost;
		int mongoDbPort;
		if (System.getProperty("hostName") != null
				|| System.getProperty("doctorAppUrl") != null) {
			System.out.println(System.getProperty("hostName"));
			this.adminAppUrl = System.getProperty("hostName")
					+ ":8080/AdminWebApp";
			this.doctorAppUrl = System.getProperty("doctorAppUrl");
			System.out.println("DoctorApp url=" + doctorAppUrl);
			this.digideskAppUrl = System.getProperty("digideskAppUrl");
			System.out.println("PharmacistApp url=" + digideskAppUrl);
			this.slateRxUrl = System.getProperty("slateRxUrl");
			System.out.println("SlateRxUI url=" + slateRxUrl);
			this.oneMgDigixUrl = System.getProperty("oneMgDigixUrl");
			System.out.println("1MG Digix url=" + oneMgDigixUrl);
			this.downloadedPath = System.getProperty("downloadedPath");
			System.out.println("downloadedPath =" + downloadedPath);
			this.customerMobileNumber = System
					.getProperty("customerMobileNumber");
			System.out.println("customerMobileNumber =" + customerMobileNumber);
			this.doctorId = System
					.getProperty("doctorId");
			System.out.println("doctorId =" + doctorId);
			this.noofpages = System.getProperty("noofpages");
			this.serviceUrl = System.getProperty("hostName") + ":8080/";
			mongoDbHost = System.getProperty("mongoHost");// .replace("http://",
															// "");
			mongoDbPort = Integer.parseInt(System.getProperty("mongoPort"));
			oldWARFile=System.getProperty("oldWARFile");
			newWARFile = System.getProperty("newWARFile");

		} else {
			this.adminAppUrl = context.getCurrentXmlTest().getParameter(
					"adminAppUrl");
			this.doctorAppUrl = context.getCurrentXmlTest().getParameter(
					"doctorAppURL");
			mongoDbHost = context.getCurrentXmlTest()
					.getParameter("mongoDbUrl");
			this.serviceUrl = context.getCurrentXmlTest().getParameter(
					"serviceUrl");
			this.digideskAppUrl = context.getCurrentXmlTest().getParameter(
					"digideskAppUrl");
			this.slateRxUrl = context.getCurrentXmlTest().getParameter(
					"slateRxURL");
			this.oneMgDigixUrl = context.getCurrentXmlTest().getParameter(
					"oneMgDigixURL");
			this.downloadedPath = context.getCurrentXmlTest().getParameter(
					"downloadedPath");
			mongoDbHost = context.getCurrentXmlTest()
					.getParameter("mongoDbUrl");
			mongoDbPort = Integer.parseInt(context.getCurrentXmlTest()
					.getParameter("mongoDbPort"));
			this.doctorId = context.getCurrentXmlTest().getParameter(
					"doctorId");
		}

		// GORX RELATED
		Services.registrationId = context.getCurrentXmlTest().getParameter(
				"registrationId");
		this.createOtpUrl = serviceUrl
				+ context.getCurrentXmlTest().getParameter("createOtpUrl");
		this.verifyOtpUrl = serviceUrl
				+ context.getCurrentXmlTest().getParameter("verifyOtpUrl");
		this.createDraftRxUrl = serviceUrl
				+ context.getCurrentXmlTest().getParameter("createDraftRxUrl");
		this.uploadImageUrl = serviceUrl
				+ context.getCurrentXmlTest().getParameter("uploadImageUrl");
		this.corporateRxImage1Data = context.getCurrentXmlTest().getParameter(
				"corporateRxImage1Data");
		this.corporateRxImage2Data = context.getCurrentXmlTest().getParameter(
				"corporateRxImage2Data");
		this.corporateRxImage3Data = context.getCurrentXmlTest().getParameter(
				"corporateRxImage3Data");
		this.corporateRxImage4Data = context.getCurrentXmlTest().getParameter(
				"corporateRxImage4Data");
		this.goRxImage0 = context.getCurrentXmlTest().getParameter(
				"goRxImage0");
		this.goRxImage1 = context.getCurrentXmlTest().getParameter(
				"goRxImage1");
		this.goRxImage2 = context.getCurrentXmlTest().getParameter(
				"goRxImage2");
		this.goRxImage3 = context.getCurrentXmlTest().getParameter(
				"goRxImage3");
		this.goRxCTOImageData = context.getCurrentXmlTest().getParameter(
				"goRxCTOImageData");
		this.goRxWTOImageData = context.getCurrentXmlTest().getParameter(
				"goRxWTOImageData");

		context.setAttribute(adminAppUrl, this.adminAppUrl);
				
		System.out.println(serviceUrl);
		this.doctorAppMobileNo = context.getCurrentXmlTest().getParameter(
				"doctorUserName");
		this.doctorAppPassword = context.getCurrentXmlTest().getParameter(
				"doctorPassword");
		this.pharmacistLoginWebServiceUrl = context.getCurrentXmlTest()
				.getParameter("pharmacistUrl");

		this.doctorLoginWebServiceUrl = context.getCurrentXmlTest()
				.getParameter("doctorLogin");
		this.doctorInfoWebServiceUrl = context.getCurrentXmlTest()
				.getParameter("doctorInfo");
		this.doctorCreatePrescription = context.getCurrentXmlTest()
				.getParameter("doctorCreatePrescription");
		this.createPrescriptionUrl = context.getCurrentXmlTest().getParameter(
				"createPrescrition");
		this.pharmacistCreatePrescription = context.getCurrentXmlTest()
				.getParameter("pharmacistCreatePrescription");
		this.pharmacistEditPrescription = context.getCurrentXmlTest()
				.getParameter("pharmacistEditPrescription");
		this.pharmacistUserName = context.getCurrentXmlTest().getParameter(
				"pharmacistUserName");
		this.pharmacistPassword = context.getCurrentXmlTest().getParameter(
				"pharmacistPassword");
		this.oneMgPharmacistUserName = context.getCurrentXmlTest()
				.getParameter("oneMgPharmacistUserName");
		this.oneMgPharmacistPassword = context.getCurrentXmlTest()
				.getParameter("oneMgPharmacistPassword");

		// Upload Corporate Rx Image to Digi Q
		this.tsaLoginUrl = serviceUrl
				+ context.getCurrentXmlTest().getParameter("tsaLoginUrl");
		this.addCorporateUrl = serviceUrl
				+ context.getCurrentXmlTest().getParameter("addCorporateUrl");
		this.uploadCorpImageUrl = serviceUrl
				+ context.getCurrentXmlTest()
						.getParameter("uploadCorpImageUrl");
		this.tsaUserName = context.getCurrentXmlTest().getParameter(
				"TSAUserName");
		
		//Upload SlateRx PDF
		this.createSlatePDFUrl = serviceUrl
				+ context.getCurrentXmlTest().getParameter("createSlatePDFUrl");

		// Upload Go Rx CTO & WTO Images to Digi Q
		this.getDraftRxIDUrl = serviceUrl
				+ context.getCurrentXmlTest().getParameter("getDraftRxIDUrl");
		this.uploadImagesToDraftRxUrl = serviceUrl
				+ context.getCurrentXmlTest().getParameter(
						"uploadImagesToDraftRxUrl");
		//Printout Urls
		this.deliveryChallanPrintUrl = serviceUrl
				+ context.getCurrentXmlTest()
						.getParameter("deliveryChallanPrintUrl");
		this.insurancePrintUrl = serviceUrl
						+ context.getCurrentXmlTest()
								.getParameter("insurancePrintUrl");
		
		this.sheetName = context.getCurrentXmlTest().getParameter("sheetName");
		
		mongoDb = NewMongoDbUtil.getInstance();
		mongoDb.initiate(mongoDbHost, mongoDbPort);

	}

	@DataProvider(name = "TestData")
	public static Iterator<Object[]> getItemData(Method m,
			ITestContext testContex) throws Exception {
		Class<?> declaringClass = m.getDeclaringClass();
		String className = declaringClass.getSimpleName();
		String filePath = "src/main/java/com/medlife/resources/" + className
				+ ".xls";
		return NewExcelUtil.getTestData(m.getName(), filePath);

	}
	
	public void initialize() throws Exception {
		CONFIG = new Properties();
		FileInputStream ip = new FileInputStream(
			System.getProperty("user.dir")
					+ "//src//main//java//com//medlife//resources//CONFIG.properties");
		CONFIG.load(ip);
	}

	public static Iterator<Object[]> getItemData(String fileName,
			String columnName) throws Exception {
		String filePath = "src/main/java/com/medlife/resources/" + fileName
				+ ".xls";
	    //Close and free allocated memory 
	    return NewExcelUtil.getData(columnName, filePath);
	}
	
	protected void getLoginCredentials(TestDataFactory dataFactory) {
			dataFactory.setCXUsername(cxUsername);
			dataFactory.setFCUsername(dataFactory.getTestCaseParameters()
					.optString("fcid")+fcUserName);
			dataFactory.setDLUsername(dataFactory.getTestCaseParameters()
					.optString("fcid")+dlUserName);
			dataFactory.setAEUsername(dataFactory.getTestCaseParameters()
					.optString("fcid")+aeUserName);
			dataFactory.setDAAgentName(dataFactory.getTestCaseParameters()
					.optString("fcid"));
			dataFactory.setTSAUsername(dataFactory.getTestCaseParameters()
					.optString("fcid")+tsaUserName);
			dataFactory.setPassword(password);
			dataFactory.setAddressLine1(addressLine1);
			dataFactory.setAddressLine2(addressLine2);
			dataFactory.setPincode("560030");
			//getPincodeByfcId(dataFactory);
			//getLandmarkByfcId(dataFactory);
			getCityByfcId(dataFactory);
			getStateByfcId(dataFactory);
			getFCTypeByfcId(dataFactory);
	}

	public String createPrescriptionAndReturnOrderId(
			TestDataFactory testDataFactory) {
		if (testDataFactory.isOrderToBeCreated()) {
			JSONObject createPharmascistPrescriptionResponse = new Services()
					.createPharmascistPrescription(serviceUrl
							+ pharmacistCreatePrescription, testDataFactory);
			System.out.println(serviceUrl + pharmacistCreatePrescription);
			String orderId = getOrderIdFromPrescriptionId(createPharmascistPrescriptionResponse
					.optString("pid"));
			Assertion.assertNotNull(orderId, "Order Id is null");
			return orderId;
		} else {
			return testDataFactory.getOrderid();
		}
	}

	protected void updateInventoryForMedicineId(
			TestDataFactory testDataFactory, int quantity) {
		JSONObject data = testDataFactory.getTestCaseParameters();
		JSONArray medicineListAndQuantity = data.optJSONArray("medicineId");
		for (int index = 0; index < medicineListAndQuantity.length(); index++) {
			JSONObject medicineObjFromData = medicineListAndQuantity
					.getJSONObject(index);
			String medicineId = medicineObjFromData.optString("medid");
			mongoDb.updateValue("Inventory", "item.product.$id", medicineId,
					"actualQty", quantity, "Bangalore");
		}
	}

	protected void updateInventoryForMedicineId(TestDataFactory testDataFactory) {
		JSONObject data = testDataFactory.getTestCaseParameters();
		JSONArray medicineListAndQuantity = data.optJSONArray("inventoryQty");
		if (medicineListAndQuantity != null) {
			String city = testDataFactory.getTestCaseParameters().getString(
					"city");
			for (int index = 0; index < medicineListAndQuantity.length(); index++) {
				JSONObject jsonObj = medicineListAndQuantity
						.getJSONObject(index);
				for (String productId : jsonObj.getNames(jsonObj)) {
					JSONObject batchAndQty = (JSONObject) jsonObj
							.get(productId);
					for (String batch : batchAndQty.getNames(batchAndQty)) {
						System.out.println("ProductId: " + productId
								+ " Batch: " + batch + " Qty:"
								+ batchAndQty.get(batch) + " City:" + city);
						mongoDb.updateValue("Inventory", "item.batchNo", batch,
								"actualQty", (Integer) batchAndQty.get(batch),
								city);
					}
				}
			}
		}
	}

	protected void updateInventoryForMedicineIdBangalore(String medid,
			int quantity) {
		mongoDb.updateValue("Inventory", "item.product.$id", medid,
				"actualQty", quantity, "Bangalore");
	}

	protected void updateInventoryIdBangalore(String inventoryId, int quantity) {
		mongoDb.updateActualQty(inventoryId, quantity);
	}

	protected void updateInventoryForMedicineIdAllMumbai(String medid,
			int quantity) {
		mongoDb.updateValue("Inventory", "item.product.$id", medid,
				"actualQty", quantity, "Mumbai");
	}

	protected void updateDiscontinueFeild(String medid, boolean flag) {
		mongoDb.updateValue("Product", "_id", medid, "discontinued", flag);
	}

	protected void updateDisableFeild(String medid, boolean flag) {

		mongoDb.updateValue("Product", "_id", medid, "disable", flag);

	}

	public void updateMedicineBeforeDiscontinue(String medicinename,
			boolean discontinue, boolean disable, int qty) {

		String medid = getMedId(medicinename);
		updateInventoryForMedicineIdBangalore(medid, qty);
		updateInventoryForMedicineIdAllMumbai(medid, qty);
		updateDiscontinueFeild(medid, discontinue);
		updateDisableFeild(medid, disable);

	}

	public void updateOrderState(String orderId, String state) {
		String orderState = "com.medlife.order.state." + state;
		mongoDb.updateValue("order", "_id", orderId, "state._class", orderState);
	}

	public void updateOrderToFCAssigned(JSONObject testDataJson, String orderId) {
		updateOrderAddressthroughDB(testDataJson, orderId);
		mongoDb.updateValue("order", "_id", orderId, "fcId",
				testDataJson.getJSONObject("address").optString("fcId")
						.equalsIgnoreCase("") ? "BLR07" : testDataJson
						.getJSONObject("address").optString("fcId"));
		mongoDb.updateValue("order", "_id", orderId, "confirmed", true);
		mongoDb.updateValue("order", "_id", orderId, "invoiceNum",
				"BLR17-2015-4300");
		updateOrderState(orderId, "FCAssigned");

	}

	public void updateOrderAddressthroughDB(JSONObject testDataJson,
			String orderId) {
		JSONObject address = testDataJson.getJSONObject("address");

		mongoDb.updateValue("order", "_id", orderId,
				"deliveryInfo.address.type", "test");
		mongoDb.updateValue("order", "_id", orderId,
				"deliveryInfo.address.addressLine1", "303,block a");
		mongoDb.updateValue("order", "_id", orderId,
				"deliveryInfo.address.addressLine2", "5th cross");
		mongoDb.updateValue("order", "_id", orderId,
				"deliveryInfo.address.locality",
				"HAL II Stage, Bengaluru, Karnataka, India");
		mongoDb.updateValue("order", "_id", orderId,
				"deliveryInfo.address.city", "Bangalore");
		mongoDb.updateValue("order", "_id", orderId,
				"deliveryInfo.address.state", "Karnataka");
		mongoDb.updateValue("order", "_id", orderId,
				"deliveryInfo.address.pinCode", address.optString("pincode"));
		mongoDb.updateValue("order", "_id", orderId,
				"deliveryInfo.address.customLocation",
				"HAL II Stage, Bengaluru, Karnataka, India");
		mongoDb.updateValue("order", "_id", orderId,
				"deliveryInfo.address.defaultAddress", true);
		mongoDb.updateValue("order", "_id", orderId,
				"deliveryInfo.address.pinCode", address.optString("pincode"));
		mongoDb.updateValue("order", "_id", orderId,
				"deliveryInfo.address.coordinates.latitude", "12.9673053");
		mongoDb.updateValue("order", "_id", orderId,
				"deliveryInfo.address.coordinates.longitude",
				"77.64330889999997");

	}

	protected void loginInAdminAppWithSuper(String userName, String password)
			throws NotCurrentPageException, IOException {
		CustomWebDriver.getWebDriver().get(this.adminAppUrl);
		LogInPage loginPage = new LogInPage();
		loginPage.signIn(userName, password);
		loginPage.clickViewAssignedOrderLink();
		WebTestRunListener.createAttachment();
	}

	protected void loginInAdminAppAsCX(String userName, String password)
			throws NotCurrentPageException, IOException {
		CustomWebDriver.getWebDriver().get(this.adminAppUrl);
		LogInPage loginPage = new LogInPage();
		loginPage.signIn(userName, password);
		WebTestRunListener.createAttachment();
	}
	
	// finished manas till now 20th
	protected void loginAsFCAndAcceptOrder(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		getLoginCredentials(dataFactory);
		loginInAdminApp(dataFactory.getFCUsername(), dataFactory.getPassword(), true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.clickViewAssignedOrderLink();
		if (getFCTypeByfcId(dataFactory).equalsIgnoreCase("POJO")) {
			viewassignedorder.pojoAcceptOrder(dataFactory.getOrderid());
		} else {
			viewassignedorder.acceptOrder(dataFactory.getOrderid());
		}
		//SMSValidateUtil.getInstance().validateFCAcceptedMessage(dataFactory);
		// new LogoutPage().logout();
	}
		
	protected void loginAsPOJOFCAndDeliverOrder(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		getLoginCredentials(dataFactory);
		loginInAdminApp(dataFactory.getFCUsername(), dataFactory.getPassword(), true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.clickViewAssignedOrderLink();
		viewassignedorder.markAsDeliver(dataFactory.getOrderid());
	}
		
	protected void loginAsPOJOFCAndCloseOrder(TestDataFactory dataFactory)
			throws NotCurrentPageException, IOException {
		getLoginCredentials(dataFactory);
		loginInAdminApp(dataFactory.getFCUsername(), dataFactory.getPassword(), true);
		FCViewAssignedOrderPage viewassignedorder = new FCViewAssignedOrderPage();
		viewassignedorder.clickViewAssignedOrderLink();
		viewassignedorder.acknowledgeDeliveredEPOrders(dataFactory.getOrderid());
	}

	public static void loginInAdminApp(String userName, String password,
			boolean openNewBrowser) throws NotCurrentPageException, IOException {
		if (openNewBrowser) {
			CustomWebDriver.cleanUp();
		}
		CustomWebDriver.getWebDriver().get(ContextManager.getGlobalContext().getAttribute("adminAppUrl").toString());
		LogInPage loginPage = new LogInPage();
		loginPage.signIn(userName, password);
		WebTestRunListener.createAttachment();
		LeftNavigationPage leftnav = new LeftNavigationPage();
		leftnav.clickHamburgerMenu();
		WebTestRunListener.createAttachment();
	}

	protected void loginInAdminAppWithSuperRole(String userName,
			String password, boolean openNewBrowser)
			throws NotCurrentPageException, IOException {
		if (openNewBrowser) {
			CustomWebDriver.cleanUp();
		}
		CustomWebDriver.getWebDriver().get(this.adminAppUrl);
		LogInPage loginPage = new LogInPage();
		loginPage.signIn(userName, password);
		loginPage.clickViewAssignedOrderLink();
		WebTestRunListener.createAttachment();
	}

	protected void loginInAdminAppWithFc(String userName, String password,
			boolean createNewBrowser) throws NotCurrentPageException,
			IOException {
		if (createNewBrowser) {
			CustomWebDriver.cleanUp();
		}
		CustomWebDriver.getWebDriver().get(this.adminAppUrl);
		LogInPage loginPage = new LogInPage();
		loginPage.signIn(userName, password);
		WebPage.sleep(3000);
		loginPage.clickFCAssignedAndCancelledLink();
		WebTestRunListener.createAttachment();
	}

	protected void loginInDoctorApp(String userName, String password)
			throws NotCurrentPageException, IOException {
		CustomWebDriver.cleanUp();
		CustomWebDriver.getWebDriver().get(this.doctorAppUrl);
		DoctorAppLoginPage login = new DoctorAppLoginPage();
		login.signIn(userName, password);
		WebTestRunListener.createAttachment();
	}

	protected void loginInPharmacistApp(String userName, String password)
			throws NotCurrentPageException, IOException {
		CustomWebDriver.cleanUp();
		CustomWebDriver.getWebDriver().get(this.digideskAppUrl);
		DigideskLoginPage login = new DigideskLoginPage();
		login.signIn(userName, password);
		WebTestRunListener.createAttachment();
	}

	protected void loginInSlateRxUI() throws NotCurrentPageException,
			IOException {
		CustomWebDriver.cleanUp();
		CustomWebDriver.getWebDriver().get(this.slateRxUrl);
		SlateRxUILoginPage login = new SlateRxUILoginPage();
		login.signIn("9880265666", "Password1$");
		WebTestRunListener.createAttachment();
	}

	public String getRandomNumber() {
		int random = 1000000000 + (int) (Math.random() * 100000);
		mobileNumber = "" + random;
		return mobileNumber;

	}

	public String getRandomId() {
		int random = 10000 + (int) (Math.random() * 100000);
		String id = "" + random;
		return id;

	}

	public static String generateRandomName() {
		String random = RandomStringUtils.randomAlphabetic(6);
		return random;
	}

	public String getTransactionType(String comment) {

		String transactionType = mongoDb.getFeildValue("Transaction",
				"comment", comment, "transactionType");
		return transactionType;

	}

	public float getTransactionAmount(String comment) {
		String amount = mongoDb.getFeildValue("Transaction", "comment",
				comment, "amount");
		float amt = Float.parseFloat(amount);
		return amt;
	}

	public String getDoctorStatus(String mob) {
		String status = mongoDb.getFeildValue("Doctors", "mobile", mob,
				"docStatus");
		return status;
	}
	
	public String getDoctorBrand(String mob) {
		String brand = mongoDb.getFeildValue("Doctors", "mobile", mob,
				"brand");
		return brand;
	}
	
	public String getDoctorBrandById(String id) {
		String brand = mongoDb.getFeildValue("Doctors", "_id", id,
				"brand");
		return brand;
	}
	public boolean isDoctorRegistered(String mob) {
		String status = mongoDb.getFeildValue("Doctors", "mobile", mob,
				"isRegistered");
		return (new Boolean(status)).booleanValue();
	}

	public String getMedId(String medname) {
		String medid = mongoDb.getFeildValue("Product", "brandDesc", medname,
				"_id");
		return medid;
	}

	public String getDiscontinueFlag(String medname) {
		String flag = mongoDb.getFeildValue("Product", "brandDesc", medname,
				"discontinued");
		return flag;
	}

	public String getDisableFlag(String medname) {
		String flag = mongoDb.getFeildValue("Product", "brandDesc", medname,
				"disable");
		return flag;
	}

	public String getOrderIdFromCustomerId(String customerId) {
		String myDoc = mongoDb.getLatestUpdatedData("order", "custid",
				customerId, "date");
		JSONObject document = new JSONObject(myDoc);
		return document.optString("_id");
	}
	
	public String getPincodeByfcId(TestDataFactory dataFactory) {
		String myDoc = mongoDb.getData("fcData", "fcId",
				dataFactory.getTestCaseParameters().optString("fcid").toUpperCase());
		JSONObject document = new JSONObject(myDoc);
		dataFactory.setPincode(document.optString("pincode"));
		return document.optString("pincode");
	}
	
	public String getLandmarkByfcId(TestDataFactory dataFactory) {
		String myDoc = mongoDb.getData("fcData", "fcId",
				dataFactory.getTestCaseParameters().optString("fcid").toUpperCase());
		JSONObject document = new JSONObject(myDoc);
		dataFactory.setLocality(document.optString("mainLocation"));
		return document.optString("mainLocation");
	}
	
	public String getCityByfcId(TestDataFactory dataFactory) {
		String myDoc = mongoDb.getData("fcData", "fcId",
				dataFactory.getTestCaseParameters().optString("fcid").toUpperCase());
		JSONObject document = new JSONObject(myDoc);
		dataFactory.setCity(document.optString("city"));
		return document.optString("city");
	}
	
	public String getStateByfcId(TestDataFactory dataFactory) {
		String myDoc = mongoDb.getData("fcData", "fcId",
				dataFactory.getTestCaseParameters().optString("fcid").toUpperCase());
		JSONObject document = new JSONObject(myDoc);
		dataFactory.setState(document.optString("state"));
		return document.optString("state");
	}
	
	public String getFCTypeByfcId(TestDataFactory dataFactory) {
		String myDoc = mongoDb.getData("FulfillmentCenter", "_id",
				dataFactory.getTestCaseParameters().optString("fcid"));
		JSONObject document = new JSONObject(myDoc);
		return document.optString("type");
	}

	public String checkDraftRxIdCreated(String source) {
		String myDoc = mongoDb.getLatestUpdatedData("DraftRx", "source",
				source, "createTime");
		JSONObject document = new JSONObject(myDoc);
		System.out.println("DraftRxId for " + source + " = "
				+ document.optString("_id"));
		return document.optString("_id");
	}

	public String getDraftRxStatus(String draftrxid) {
		String myDoc = mongoDb.getLatestUpdatedData("DraftRx", "_id",
				draftrxid, "createTime");
		JSONObject document = new JSONObject(myDoc);
		System.out.println("DraftRx Status = " + document.optString("status"));
		return document.optString("status");
	}

	public String getRxIdFromCustomerId(String customerId) {
		String myDoc = mongoDb.getLatestUpdatedData("Prescription",
				"customer.$id", customerId, "date");
		JSONObject document = new JSONObject(myDoc);
		return document.optString("_id");
	}
	
	public String getCustIdFromDraftRxId(String draftRxId) {
		String myDoc = mongoDb.getLatestUpdatedData("DraftRx",
				"_id", draftRxId, "createTime");
		JSONObject document = new JSONObject(myDoc);
		return document.optString("customerId");
	}
	
	public void checkCancelReasonForDraftOrderId(String orderId) {
		String myDoc = mongoDb.getLatestUpdatedData("OrderLog",
				"order._id", orderId, "createdDate");
		JSONObject document = new JSONObject(myDoc);
		System.out.println("Cancelled Reason for "+orderId+" = "+document.optString("cancellationReason"));
		Assertion.softAssertEquals(document.optString("cancellationReason"), "Draft Order for GPCS ( New Order Created )", "Expected '"
				+ "Draft Order for GPCS" + "' but found '" + document.optString("cancellationReason"));
		Assertion.getVerificationFailures();
	}
	
	public String getMobileNoFromCustomerId(String customerId) {
		String myDoc = mongoDb.getData("Customers", "_id", customerId);
		JSONObject document = new JSONObject(myDoc);
		return document.optString("mobile");
	}
	
	public String getOTCorNonOTCFlag(TestDataFactory dataFactory) {
		String myDoc = mongoDb.getData("Product", "_id", dataFactory.getTestCaseParameters().
				getJSONObject("medicineId").optString("medid"));
		JSONObject document = new JSONObject(myDoc);
		System.out.println("RxDrug Flag = "+document.optString("rxDrug"));
		return document.optString("rxDrug");
	}

	public String getDoctorIdFromCustomerId(String doctorId) {
		String myDoc = mongoDb.getLatestUpdatedData("Doctors", "mobile",
				doctorId, "date");
		JSONObject document = new JSONObject(myDoc);
		return document.optString("_id");
	}

	public String getTsaFromTrip(String tripId) {
		String tsaId = mongoDb.getFeildValue("Trips", "_id", tripId, "daID");
		return tsaId;
	}

	public String getTsaName(String tsaId) throws UnknownHostException {
		String tsaName = NewMongoDbUtil.getInstance().getData("Admins", "_id",
				tsaId, "firstName");
		// System.out.println(tsaName);
		return tsaName;

	}

	public int getActualQty(String productId) throws UnknownHostException {
		String aQty = NewMongoDbUtil.getInstance().getData("Inventory",
				"item.product.$id", productId, "actualQty");
		// System.out.println(aQty);
		int actQty = Integer.parseInt(aQty);
		return actQty;

	}

	public int getReservedQty(String productId) throws UnknownHostException {
		String rQty = NewMongoDbUtil.getInstance().getData("Inventory",
				"item.product.$id", productId, "reservedQty");
		// System.out.println(rQty);
		int resQty = Integer.parseInt(rQty);
		return resQty;

	}

	public int getReservedQty(String productId, String batch)
			throws UnknownHostException {
		String data = getBatchdata(productId, batch);
		JSONObject batchData = new JSONObject(data);
		int resQty = Integer.parseInt(batchData.optString("reservedQty"));
		return resQty;

	}

	public String getBatchdata(String productId, String batch)
			throws UnknownHostException {
		String doc = NewMongoDbUtil.getInstance().getDocumentFromAndQuery(
				"Inventory", "item.product.$id", productId, "item.batchNo",
				batch);
		return doc;

	}

	public String getTSAStatus(String tsaName) throws UnknownHostException {
		String status = NewMongoDbUtil.getInstance().getData("Admins",
				"firstName", tsaName, "status");
		return status;
	}

	public void validateTSAStatus(String tsaName, String tsaStatus)
			throws UnknownHostException {
		String status = getTSAStatus(tsaName);
		Assertion.softAssertTrue(status.contentEquals(tsaStatus),
				"TSA status does not match.Expected->" + tsaStatus
						+ "But found ->" + status);
	}

	public void validateActualQuantity(String productId, int qty)
			throws UnknownHostException {
		int actQty = getActualQty(productId);
		Assertion.assertEquals(actQty, qty, "Actual Quantity does not match");
		// System.out.println("DB actual qty is->"+actQty);

	}

	public void validateActualQuantity(String productId, String batch, int qty)
			throws UnknownHostException {
		String data = getBatchdata(productId, batch);
		JSONObject batchData = new JSONObject(data);
		int actQty = Integer.parseInt(batchData.optString("actualQty"));
		Assertion.assertEquals(actQty, qty, "Actual Quantity does not match");
		// System.out.println("DB actual qty is->"+actQty);

	}

	public void validateReservedQuantity(String productId, int qty)
			throws UnknownHostException {
		int resQty = getReservedQty(productId);
		Assertion.assertEquals(resQty, qty, "Reserved Quantity does not match");
		// System.out.println("DB reserved qty is->"+resQty);

	}

	public void validateReservedQuantity(String productId, String batch, int qty)
			throws UnknownHostException {
		String data = getBatchdata(productId, batch);
		JSONObject batchData = new JSONObject(data);
		int resQty = Integer.parseInt(batchData.optString("reservedQty"));
		Assertion.assertEquals(resQty, qty, "Reserved Quantity does not match");
		// System.out.println("DB reserved qty is->"+resQty);
	}

	public static void validateState(String orderId, String stateToValidate) {
		String state1 = getOrderStatus(orderId);
		String state2 = state1.replaceAll("com.medlife.order.state.", "");
		String state3 = state2.replaceAll("[^\\w]", "");
		String orderState = state3.replaceAll("_class", "");
		System.out.println(orderId + " = " + orderState);
		Assertion.softAssertEquals(orderState, stateToValidate, "Expected '"
				+ stateToValidate + "' but found '" + orderState);
		Assertion.getVerificationFailures();
	}
	
	public static String getOrderState(String orderId) {
		String state1 = getOrderStatus(orderId);
		String state2 = state1.replaceAll("com.medlife.order.state.", "");
		String state3 = state2.replaceAll("[^\\w]", "");
		String orderState = state3.replaceAll("_class", "");
		return orderState;
	}
		

	public void validateOrderType(String orderId, String typeToValidate) {
		String orderType = getOrderType(orderId);
		Assertion.softAssertTrue(orderType.contains(typeToValidate),
				"Order Type is not valid expected " + typeToValidate
						+ " but found " + orderType + " for order id="
						+ orderId);
	}

	public void verifyCancelledState(String orderId) {
		validateState(orderId, "Cancelled");
	}

	public void verifyFCAcceptedState(String orderId) {
		validateState(orderId, "FCAccepted");
	}

	public void verifyPendingForStockState(String orderId) {
		validateState(orderId, "PendingForStock");
	}

	public void verifyFCAssignedState(String orderId) {
		validateState(orderId, "FCAssigned");
	}

	public void verifyRecievedState(String orderId) {
		validateState(orderId, "Received");
	}

	public void verifyRxPendingState(String orderId) {
		validateState(orderId, "RxPending");
	}

	public void verifyDAAssignmentPendingState(String orderId) {
		validateState(orderId, "DAAssignmentPending");
	}

	public void verifyDAAssignedState(String orderId) {
		validateState(orderId, "DAAssigned");
	}

	public void verifyDAPickedState(String orderId) {
		validateState(orderId, "DAPicked");
	}

	public void verifyOrderDeliveredState(String orderId) {
		validateState(orderId, "Delivered");
	}

	public void verifyTimeSlot(String orderId, String changedTimeSlot) {
		String timeSlotFromDb = mongoDb.getTimeSlot(orderId);
		Assertion.assertEquals(changedTimeSlot, timeSlotFromDb,
				"Time slot does not match after updation");
	}

	public String getOrderIdFromPrescriptionId(String presId) {
		String feildValue = mongoDb.getFeildValue("order", "prescriptionId",
				presId, "_id");
		return feildValue;
	}
	
	public static String getLatestOrderIdFromPrescriptionId(String presId) {
		String myDoc = mongoDb.getLatestUpdatedData("order",
				"prescriptionId", presId, "date");
		JSONObject document = new JSONObject(myDoc);
		return document.optString("_id");
	}

	public static String getOrderStatus(String orderId) {
		String orderstatus = NewMongoDbUtil.getInstance().getFeildValue("order", "_id", orderId,
				"state");
		return orderstatus.toString();
	}

	public boolean checkIsRxRequiredFlag(String orderId) {
		String flagStatus = mongoDb.getFeildValue("order", "_id", orderId,
				"rxReturnRequired");
		System.out.println(flagStatus);
		return true;
	}

	public String getOrderType(String orderId) {
		String ordertype = mongoDb.getFeildValue("order", "_id", orderId,
				"orderType");
		return ordertype;
	}

	public String getTripStatus(String tripId) {
		String status = NewMongoDbUtil.getInstance().getFeildValue("Trips", "_id", tripId, "status");
		return status;
	}

	public String getOrderAmt(String oid) {
		String oamt = mongoDb.getFeildValue("order", "_id", oid,
				"payableAmount");
		return oamt;
	}

	public String getTripId(String oid) {
		String tripId = NewMongoDbUtil.getInstance().getFeildValue("Trips",
				"tripList.tripList.orderId", oid, "_id");
		return tripId;
	}

	public String getmeddetail(String oid) {
		String meddetail = mongoDb.getFeildValue("order", "_id", oid,
				"orderItems");
		return meddetail;
	}

	public String getPassword(String adminid) {
		String mail = mongoDb.getFeildValue("MailNotification",
				"mail.body.userName", adminid, "mail");
		JSONObject tempObj1 = new JSONObject(mail);
		JSONObject tempObj2 = new JSONObject(tempObj1.get("body").toString());
		String pwd = tempObj2.get("password").toString();
		return pwd;
	}

	public String getOrderFromDb(String source, String state) {
		try {
			String orderState = "com.medlife.order.state." + state;
			String orderDetail = mongoDb.getDocumentFromAndQuery("order",
					"state._class", orderState, "prescriptionSource", source);
			JSONObject document = new JSONObject(orderDetail);

			return document.optString("_id");
		} catch (Exception Null) {
			return null;
		}
	}

	protected void logout() throws NotCurrentPageException, IOException {
		CustomWebDriver.cleanUp();
		CustomWebDriver.getWebDriver().get(this.doctorAppUrl);
		LogoutPage logout = new LogoutPage();
		logout.logout();
		logout.assertlogout();
		WebTestRunListener.createAttachment();
	}

	protected void AdminWebApplogout() throws NotCurrentPageException,
			IOException {
		LogoutPage logout = new LogoutPage();
		logout.logout();
		logout.assertlogout();
		WebTestRunListener.createAttachment();
	}

	public String createTrip(String serviceURL, String loginid, String passwd,
			String orderid, String daAgentName) throws InterruptedException,
			NotCurrentPageException, IOException {
		loginInAdminAppWithFc(loginid, passwd, true);
		CreateTripPageUtil ctp = new CreateTripPageUtil();
		String tripId = ctp.clickAddToTrip(orderid);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsalogin(serviceURL, daAgentName);
		tsaaction.tsafree(serviceURL);
		validateTSAStatus(daAgentName, "Free");
		ctp.clickViewTrip();
		TripPageUtil tpu = new TripPageUtil();
		tpu.searchAndClickATrip(tripId);
		tpu.addDAAgentToTrip(daAgentName);
		validateTSAStatus(daAgentName, "Assigned");
		Thread.sleep(2000);
		return tripId;
	}

	public String createTripAndAssignDAAgent(String serviceURL, String orderid,
			String daAgentName) throws InterruptedException,
			NotCurrentPageException, IOException {
		CreateTripPageUtil ctp = new CreateTripPageUtil();
		String tripId = ctp.clickAddToTrip(orderid);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsalogin(serviceURL, daAgentName);
		tsaaction.tsafree(serviceURL);
		validateTSAStatus(daAgentName, "Free");
		ctp.clickViewTrip();
		TripPageUtil tpu = new TripPageUtil();
		tpu.searchAndClickATrip(tripId);
		tpu.addDAAgentToTrip(daAgentName);
		validateTSAStatus(daAgentName, "Assigned");
		Thread.sleep(2000);
		return tripId;
	}
	
	public void deliverEPOrder(String orderid) throws InterruptedException,
			NotCurrentPageException, IOException {
		CreateTripPageUtil ctp = new CreateTripPageUtil();
		ctp.clickDeliveredButton(orderid);
	}


	public void daAssignementPendingState(String serviceURL, String loginid,
			String passwd, String orderid, String daAgentName)
			throws InterruptedException, NotCurrentPageException, IOException {
		loginInAdminAppWithFc(loginid, passwd, true);
		CreateTripPageUtil ctp = new CreateTripPageUtil();
		ctp.clickAddToTrip(orderid);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsalogin(serviceURL, daAgentName);
		tsaaction.tsafree(serviceURL);
		validateTSAStatus(daAgentName, "Free");
		ctp.clickViewTrip();
	}

	public void verifyTripDeliverAndAccount(String loginid, String passwd,
			String serviceUrl, String daAgentName, String tripid,
			String orderid, String mobileNo, TestDataFactory dataFactory) throws InterruptedException,
			NotCurrentPageException, IOException {
		HashMap<String, Float> FCvalue = new HashMap<String, Float>();
		HashMap<String, Float> TSAvalue = new HashMap<String, Float>();
		loginInAdminApp(loginid, passwd, true);
		LeftNavigationPage leftNavPage = new LeftNavigationPage();
		leftNavPage.clickAccountSummary();
		AccountsummaryFC asFC = new AccountsummaryFC();
		AccountsummaryTSA asTSA = new AccountsummaryTSA();
		TSAServicecalls tsaaction = new TSAServicecalls();
		FCvalue = asFC.captureFCdetails();
		asTSA.assertDA(tsaaction.getTSAid(daAgentName));
		TSAvalue = asTSA.captureTSAdetails();
		// leftNavPage.clickFinanceheader();
		tsaaction.tsaDeliverOrder(serviceUrl, tripid, dataFactory);
		LeftNavigationPage leftnav = new LeftNavigationPage();
		leftnav.clickHamburgerMenu();
		leftNavPage.clickAccountSummary();
		asTSA.assertDA(tsaaction.getTSAid(daAgentName));
		String orderamt = getOrderAmt(orderid);
		Float pamt = Float.parseFloat(orderamt);
		asFC.assertFCdetails(FCvalue.get("FCopeningbal") + 0,
				FCvalue.get("orderdelivered") + 1, FCvalue.get("orderamt")
						+ pamt, FCvalue.get("tomedlife"), FCvalue.get("netpay")
						+ pamt);
		asTSA.assertDAdetails(TSAvalue.get("TSAopeningbal") + 0,
				TSAvalue.get("advancereceived") + 0,
				TSAvalue.get("orderdelivered") + 1, TSAvalue.get("orderamt")
						+ pamt, TSAvalue.get("toFC") + 0,
				TSAvalue.get("netpay") + pamt);
		FCvalue = asFC.captureFCdetails();
		TSAvalue = asTSA.captureTSAdetails();
		// leftNavPage.clickUpdateOrder();
		// FinanceMgntCashAndReturnsPage orderupdate = new
		// FinanceMgntCashAndReturnsPage();
		// leftnav.clickHamburgerMenu();
		// orderupdate.updateOrder(orderid);
		CustomWebDriver.getWebDriver().manage().deleteAllCookies();
		acknowledgeDeliveredOrder(loginid, passwd, orderid);
	}

	public void acknowledgeDeliveredOrder(String loginid, String passwd,
			String orderId) throws InterruptedException,
			NotCurrentPageException, IOException {
		loginInAdminApp(loginid, passwd, true);
		FinanceMgntCashAndReturnsPage acknowledgeorder = new FinanceMgntCashAndReturnsPage();
		acknowledgeorder.clickCashAndReturnsLink();
		acknowledgeorder.aknowledgeOrder(orderId);
	}

	public String addInventoryAndCreatePrescriptionAndReturnOrderId(
			TestDataFactory testDataFactory) throws FileNotFoundException, IOException {
		updateInventoryForMedicineId(testDataFactory);
		Services service = new Services();
		Long mobileNo = TestDataGenerator.generateRandomMobileNumber();
		service.doctorLogin(serviceUrl + doctorLoginWebServiceUrl,
				doctorAppMobileNo, doctorAppPassword);
		JSONObject testDataJson = testDataFactory.getTestCaseParameters();
		String doctorId = service.doctorInformation(serviceUrl
				+ doctorInfoWebServiceUrl, "docid");
		testDataJson.put("mobile", mobileNo + "");
		if (testDataFactory.getMobileNo() == -1)
			testDataFactory.setMobileNo(mobileNo);
		testDataJson.put("docId", doctorId);
		if (testDataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("GO_RX_CTO") ||
				testDataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("GO_RX_WTO")) {
			return createPrescriptionForGPCSAndReturnOrderId(testDataFactory);
		} else if (testDataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("MANUAL_SLATE_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("WALKIN_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("YM_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("TATA_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("ONEMG_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("MEDIDAILI_RX")) {
			return createPrescriptionForSlateYmTataOneMgRxAndReturnOrderId(testDataFactory);
		} else if (testDataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("EXTERNAL_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("CORPORATE_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("GO_RX")) {
			return createPrescriptionForSlateExtCorpGoRxAndReturnOrderId(testDataFactory);
		} else {
			// ZIP RX prescription creation
			JSONObject responseFromPres = service.createPrescription(serviceUrl
					+ createPrescriptionUrl, new Services()
					.getCreatedPrescriptionString(testDataFactory));
			String customerId = responseFromPres.optString("cid");
			String orderId = getOrderIdFromCustomerId(customerId);
			Assertion.assertNotNull(orderId, "Order Id is null");
			System.out.println("Order Id :-->" + orderId);
			testDataFactory.setOrderid(orderId);
			return orderId;
		}
		/*
		 * updateInventoryForMedicineId(testDataFactory); if
		 * (testDataFactory.getTestCaseParameters().optString("source")
		 * .equalsIgnoreCase("GO_RX") ||
		 * testDataFactory.getTestCaseParameters().optString("source")
		 * .equalsIgnoreCase("ONEMG_RX")) { return
		 * createPrescriptionForGORXAndReturnOrderId(testDataFactory); } else if
		 * (testDataFactory.getTestCaseParameters().optString("source")
		 * .equalsIgnoreCase("SLATE_RX") ||
		 * testDataFactory.getTestCaseParameters().optString("source")
		 * .equalsIgnoreCase("CORPORATE_RX")) { return
		 * createPrescriptionForSlateOrCoporateRxAndReturnOrderId
		 * (testDataFactory); } else { return
		 * createPrescriptionForZipRxAndReturnOrderId(testDataFactory); }
		 */
	}

	public String getOrderIdfromCustomerMobile(TestDataFactory testDataFactory,
			String customerMobile) {
		String myDoc = mongoDb.getData("Customers", "mobile", customerMobile);
		JSONObject document = new JSONObject(myDoc);
		String customerId = document.optString("_id");
		String orderId = getOrderIdFromCustomerId(customerId);
		testDataFactory.setOrderid(orderId);
		Assertion.assertNotNull(orderId, "Order Id is null");
		testDataFactory.setOrderid(orderId);
		return orderId;
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

	public String getOrderStateOld(String orderId) {
		String myDoc = mongoDb.getData("order", "_id", orderId);
		JSONObject document = new JSONObject(myDoc);
		String state = document.optString("state.");
		System.out.println("Order State test = " + state);
		return state;
	}

	public String getPrescriptionIdfromCustomerMobile(TestDataFactory testDataFactory,String customerMobile) {
		String myDoc = mongoDb.getData("Customers", "mobile", customerMobile);
		JSONObject document = new JSONObject(myDoc);
		String customerId = document.optString("_id");
		String RxId = getRxIdFromCustomerId(customerId);
		Assertion.assertNotNull(RxId, "Rx Id is null");
		testDataFactory.setPrescriptionId(RxId);
		return RxId;
	}
	
	public String getDraftRxfromPrescriptionId(TestDataFactory testDataFactory,String rxId) {
		String myDoc = mongoDb.getData("Prescription", "_id", rxId);
		JSONObject document = new JSONObject(myDoc);
		String draftRxId = document.optString("draftRxId");
		testDataFactory.setDraftRxId(draftRxId);
		return draftRxId;
	}

	public String getDoctorDetails(TestDataFactory testDataFactory,
			String docMobile) {
		String myDoc = mongoDb.getData("Doctors", "mobile", docMobile);
		JSONObject document = new JSONObject(myDoc);
		String doctorId = document.optString("_id");
		String draftRxId = getDoctorStatus(doctorId);
		Assertion.assertNotNull(draftRxId, "Rx Id is null");
		System.out.println("DraftRx Id = " + draftRxId);
		return draftRxId;
	}

	public String checkMaxDispenseDays(TestDataFactory testDataFactory,
			String customerMobile) {
		String myDoc = mongoDb.getData("Customers", "mobile", customerMobile);
		JSONObject document = new JSONObject(myDoc);
		String customerId = document.optString("_id");
		String orderId = getOrderIdFromCustomerId(customerId);
		testDataFactory.setOrderid(orderId);
		Assertion.assertNotNull(orderId, "Order Id is null");
		String myDoc1 = mongoDb.getData("order", "_id", orderId);
		JSONObject document1 = new JSONObject(myDoc1);
		String MDDays = document1.optString("maxDispenseDays");
		System.out.println("Max dispense days = " + MDDays);
		return MDDays;
	}
	
	public String addInventoryAndCreatePrescriptionAndReturnOrderId(
			TestDataFactory testDataFactory, int quantity) throws FileNotFoundException, IOException {
		updateInventoryForMedicineId(testDataFactory,quantity);
		Services service = new Services();
		Long mobileNo = TestDataGenerator.generateRandomMobileNumber(); 
		service.doctorLogin(serviceUrl + doctorLoginWebServiceUrl,
				doctorAppMobileNo, doctorAppPassword);
		JSONObject testDataJson = testDataFactory.getTestCaseParameters();
		String doctorId = service.doctorInformation(serviceUrl
				+ doctorInfoWebServiceUrl, "docid");
		testDataJson.put("mobile", mobileNo + "");
		if (testDataFactory.getMobileNo() == -1)
			testDataFactory.setMobileNo(mobileNo);
		testDataJson.put("docId", doctorId);
		if (testDataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("GO_RX_CTO_AWA") ||
				testDataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("GO_RX_WTO_AWA")) {
			return createPrescriptionForGPCSAndReturnOrderId(testDataFactory);
		} else if (testDataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("MANUAL_SLATE_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("WALKIN_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("YM_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("TATA_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("ONEMG_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("MEDIDAILI_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("GO_RX_CTO")
				|| testDataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("INAYO_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
						.equalsIgnoreCase("ZYWEE_RX")) {
			return createPrescriptionForSlateYmTataOneMgRxAndReturnOrderId(testDataFactory);
		} else if (testDataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("EXTERNAL_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("SLATE_RX") 
				|| testDataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("CORPORATE_RX")
				|| testDataFactory.getTestCaseParameters().optString("source")
				.equalsIgnoreCase("GO_RX")) {
			return createPrescriptionForSlateExtCorpGoRxAndReturnOrderId(testDataFactory);
		} else {
			// ZIP RX prescription creation
			JSONObject responseFromPres = service.createPrescription(serviceUrl
					+ createPrescriptionUrl, new Services()
					.getCreatedPrescriptionString(testDataFactory));
			String customerId = responseFromPres.optString("cid");
			String orderId = getOrderIdFromCustomerId(customerId);
			Assertion.assertNotNull(orderId, "Order Id is null");
			System.out.println("Order Id :-->" + orderId);
			testDataFactory.setOrderid(orderId);
			return orderId;
		}
	}

	// public String addInventoryAndCreatePrescriptionForDoctorAndReturnOrderId(
	// TestDataFactory testDataFactory) {
	// updateInventoryForMedicineId(testDataFactory);
	// Services service = new Services();
	// service.doctorLogin(serviceUrl + doctorLoginWebServiceUrl,
	// doctorAppMobileNo, doctorAppPassword);
	// JSONObject testDataJson = testDataFactory.getTestCaseParameters();
	// String doctorId = service.doctorInformation(serviceUrl
	// + doctorInfoWebServiceUrl, "docid");
	// testDataJson.put("mobile",
	// TestDataGenerator.generateRandomMobileNumber());
	// testDataJson.put("docId", doctorId);
	// JSONObject responseFromPres = service.createPrescription(serviceUrl
	// + createPrescriptionUrl,
	// getCreatedPrescriptionString(testDataJson));
	// String customerId = responseFromPres.optString("cid");
	// String orderId = getOrderIdFromCustomerId(customerId);
	// Assertion.assertNotNull(orderId, "Order Id is null");
	// System.out.println("Order Id :-->" + orderId);
	// return orderId;
	// }

	public String createPrescriptionForDoctorAndReturnOrderId(
			TestDataFactory testDataFactory) {
		Services service = new Services();
		service.doctorLogin(serviceUrl + doctorLoginWebServiceUrl,
				doctorAppMobileNo, doctorAppPassword);
		JSONObject testDataJson = testDataFactory.getTestCaseParameters();
		String doctorId = service.doctorInformation(serviceUrl
				+ doctorInfoWebServiceUrl, "docid");
		testDataJson.put("mobile",
				TestDataGenerator.generateRandomMobileNumber());
		testDataJson.put("docId", doctorId);
		JSONObject responseFromPres = service.createPrescription(serviceUrl
				+ createPrescriptionUrl,
				new Services().getCreatedPrescriptionString(testDataFactory));
		String customerId = responseFromPres.optString("cid");
		String orderId = getOrderIdFromCustomerId(customerId);
		Assertion.assertNotNull(orderId, "Order Id is null");
		System.out.println("Order Id :-->" + orderId);
		return orderId;
	}

	// private String getCreatedPrescriptionString(JSONObject data) {
	//
	// if(data.optString("source").equalsIgnoreCase("ZIP_RX")){
	// return getCreatedPrescriptionStringZipRx(data);
	// }else{
	// return
	// }
	//
	//
	//
	// }
	protected String getOtpFromDatabase(String mobileNumber) {
		String otp = null;
		try {
			otp = mongoDb.getFeildValue("OTPs", "mobileNumber", mobileNumber,
					"otpNumber");
		} catch (Exception e) {
			Assertion.softAssertTrue(false, "Something wrong with database");
			e.printStackTrace();
		}
		return otp;
	}

	public String getOtpFromDB(String customerMobile) {
		String myDoc = mongoDb.getData("OTPs", "mobileNumber", customerMobile);
		JSONObject document = new JSONObject(myDoc);
		String otp = document.optString("otpNumber");
		// String orderId = getOrderIdFromCustomerId(customerId);
		// testDataFactory.setOrderid(orderId);
		// Assertion.assertNotNull(orderId, "Order Id is null");
		// System.out.println("OTP = " + otp);
		return otp;
	}
	/*
	 * Upload images to images and PDF to DraftRx and get DraftRx ID
	 *
	 */
	public String createPrescriptionForSlateExtCorpGoRxAndReturnOrderId(
			TestDataFactory testDataFactory) throws FileNotFoundException, IOException {
		String orderId = null;
		if (testDataFactory.getTestCaseParameters().optString("source") 
				.equalsIgnoreCase("EXTERNAL_RX")) {
			try {
				createExternalRxPdf(externalRxPDF);
				String draftRxId = sendPDFtoDigiQ(testDataFactory,convertToByteArray(), doctorId);
				testDataFactory.setDraftRxId(draftRxId);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (testDataFactory.getTestCaseParameters().optString("source") 
				.equalsIgnoreCase("SLATE_RX")) {
			try {
				createSlateRxPdf(slateRxPDF);
				String draftRxId = sendPDFtoDigiQ(testDataFactory,convertToByteArray(), doctorId);
				testDataFactory.setDraftRxId(draftRxId);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (testDataFactory.getTestCaseParameters().optString("source") 
				.equalsIgnoreCase("CORPORATE_RX")) {
			String draftRxId = uploadCorpRxImagetoDigiQ();
			testDataFactory.setDraftRxId(draftRxId);
		} else if (testDataFactory.getTestCaseParameters().optString("source") 
				.equalsIgnoreCase("GO_RX")) {
			String draftRxId = uploadGoRxImagetoDigiQ(testDataFactory);
			testDataFactory.setDraftRxId(draftRxId);
		}
		JSONObject testCaseParameters = testDataFactory.getTestCaseParameters();
		testCaseParameters.put("draftRxId", testDataFactory.getDraftRxId());
		orderId = createPrescriptionForSlateYmTataOneMgRxAndReturnOrderId(testDataFactory);
		String custId = getCustIdFromDraftRxId(testDataFactory.getDraftRxId());
		System.out.println(custId);
		testDataFactory.setCustomerId(custId);
		return orderId;
	}

	public String uploadGoRxImagetoDigiQ(TestDataFactory testDataFactory) {
		Services service = new Services();
		String mobileNumber = TestDataGenerator.generateRandomMobileNumber()
				+ "";
		service.createOtpForMobileNumber(createOtpUrl, mobileNumber);
		String otpFromDatabase = getOtpFromDB(mobileNumber);
		Services.deviceId = service.getDeviceIdForMobileNumber(verifyOtpUrl,
				mobileNumber, otpFromDatabase);
		String draftRxId = service.getCupIdForMobileNumber(testDataFactory, createDraftRxUrl,
				mobileNumber);
			service.uploadImageToGivenDraftRxId(uploadImageUrl, draftRxId,
					goRxImage0, 0);
			service.uploadImageToGivenDraftRxId(uploadImageUrl, draftRxId,
					goRxImage1, 1);
			service.uploadImageToGivenDraftRxId(uploadImageUrl, draftRxId,
					goRxImage2, 2);
			service.uploadImageToGivenDraftRxId(uploadImageUrl, draftRxId,
					goRxImage2, 3);
		System.out.println("GoRx images uploaded successfully to DigiQ with DraftRxId = "+draftRxId);
		return draftRxId;
	}

	public String uploadCorpRxImagetoDigiQ() {
		Services service = new Services();
		service.tsaLogin(tsaLoginUrl, tsaUserName, password);
		String draftRxId = service.getCupIdforCorpImage(addCorporateUrl);
			service.uploadCorpImageToGivenDraftRxId(uploadCorpImageUrl,
					draftRxId, corporateRxImage1Data,0);
			service.uploadCorpImageToGivenDraftRxId(uploadCorpImageUrl,
					draftRxId, corporateRxImage2Data,1);
			service.uploadCorpImageToGivenDraftRxId(uploadCorpImageUrl,
					draftRxId, corporateRxImage3Data,2);
			service.uploadCorpImageToGivenDraftRxId(uploadCorpImageUrl,
					draftRxId, corporateRxImage4Data,3);
		System.out.println("CorpRx images uploaded successfully to DigiQ with DraftRxId = "+draftRxId);
		return draftRxId;
	}
	
	public String sendPDFtoDigiQ(TestDataFactory dataFactory, String pdfContent, String doctorId) {
		Services service = new Services();
		service.pharmacistLogin(serviceUrl
				+ pharmacistLoginWebServiceUrl, pharmacistUserName, pharmacistPassword);
		String draftRxId = service.getDraftRxIdforSlateRxExtRxPDF(dataFactory,createSlatePDFUrl, pdfContent,doctorId, getDoctorBrandById(doctorId));
		System.out.println("PDF uploaded successfully to DigiQ");
		System.out.println(draftRxId);
		return draftRxId;
	}
	
	public String createPrescriptionForGPCSAndReturnOrderId(TestDataFactory testDataFactory) {
		String draftRxId = uploadGoRxGPCSImagetoDigiQ(testDataFactory);
		JSONObject testCaseParameters = testDataFactory.getTestCaseParameters();
		testCaseParameters.put("draftRxId", draftRxId);
		new Services().pharmacistLogin(serviceUrl
				+ pharmacistLoginWebServiceUrl, pharmacistUserName,
				pharmacistPassword);
		String custId = getCustIdFromDraftRxId(draftRxId);
		System.out.println(custId);
		testDataFactory.setCustId(custId);
		JSONObject createPharmascistPrescriptionResponse = new Services()
				.createPharmascistPrescription(serviceUrl
						+ pharmacistCreatePrescription, testDataFactory);
		System.out.println(serviceUrl + pharmacistCreatePrescription);
		String orderId = getOrderIdFromPrescriptionId(createPharmascistPrescriptionResponse
				.optString("pid"));
		// System.out.println("Digitised Order = "+orderId);
		return orderId;
	}

	public String uploadGoRxGPCSImagetoDigiQ(
			TestDataFactory dataFactory) {
		Services service = new Services();
		service.tsaLogin(tsaLoginUrl, tsaUserName, password);
		String orderId = dataFactory.getOrderid();
		String draftRxId = service.getCupIdforCTOWTO(getDraftRxIDUrl,
				orderId);
		/*System.out.println("Trip Id = "+dataFactory.getTripId());
		String tripStatus = getTripStatus(dataFactory.getTripId());
		System.out.println("Trip status = "+tripStatus);
		TSAServicecalls tsaaction = new TSAServicecalls();
		tsaaction.tsaPickPPrescription(serviceUrl, orderId, dataFactory.getTripId());
		validateState(orderId, "DAPickedRxPending");*/
		//validateState(orderId, "RxPicked");
		dataFactory.setDraftRxId(draftRxId);
		if (dataFactory.getTestCaseParameters().optString("ordersource")
				.equalsIgnoreCase("CTO")) {
			service.uploadGPCSImageToGivenDraftRxId(uploadImagesToDraftRxUrl,
					dataFactory.getDraftRxId(), goRxCTOImageData);
		} else if (dataFactory.getTestCaseParameters().optString("ordersource")
				.equalsIgnoreCase("WTO")) {
			service.uploadGPCSImageToGivenDraftRxId(uploadImagesToDraftRxUrl,
					dataFactory.getDraftRxId(), goRxWTOImageData);
		}
		System.out.println("Prescription uploaded successfully");
		System.out.println("DraftRx Id = " + dataFactory.getDraftRxId());
		return dataFactory.getDraftRxId();
	}

	public String uploadGoRxImages(String mobileNumber,
			String customerName, String city, String state, String pincode, int noofimages) {
		Services service = new Services();
		service.createOtpForMobileNumber(createOtpUrl, mobileNumber);
		String otpFromDatabase = getOtpFromDB(mobileNumber);
		Services.deviceId = service.getDeviceIdForMobileNumber(verifyOtpUrl,
					mobileNumber, otpFromDatabase);
		String draftRxId = service.getDraftRxIdForMobileNumberGoRx(createDraftRxUrl,
				mobileNumber, customerName, city, state, pincode, noofimages);
		for (int index = 0; index < noofimages; index++) {
			service.uploadImageToGivenDraftRxId(uploadImageUrl, draftRxId,
					ContextManager.getGlobalContext().getSuiteParameter("goRxImage"+index), index);
		}
		/*	service.uploadImageToGivenDraftRxId(uploadImageUrl, draftRxId,
					goRxImage0, 0);
			service.uploadImageToGivenDraftRxId(uploadImageUrl, draftRxId,
					goRxImage1, 1);
			service.uploadImageToGivenDraftRxId(uploadImageUrl, draftRxId,
					goRxImage2, 2);
			service.uploadImageToGivenDraftRxId(uploadImageUrl, draftRxId,
					goRxImage3, 3);*/
			System.out.println("GoRx Images uploaded successfully with DraftRxId = "+draftRxId);
			return draftRxId;
		}


	public String createPrescriptionForSlateYmTataOneMgRxAndReturnOrderId(
			TestDataFactory testDataFactory) {
		if (testDataFactory.isOrderToBeCreated()) {
			new Services().pharmacistLogin(serviceUrl
					+ pharmacistLoginWebServiceUrl, pharmacistUserName,
					pharmacistPassword);
			JSONObject createPharmascistPrescriptionResponse = new Services()
					.createPharmascistPrescription(serviceUrl
							+ pharmacistCreatePrescription, testDataFactory);
			System.out.println(serviceUrl + pharmacistCreatePrescription);
			testDataFactory.setPrescriptionId(createPharmascistPrescriptionResponse
					.optString("pid"));
			System.out.println("Prescription Id = "+createPharmascistPrescriptionResponse
					.optString("pid"));
			String orderId = getOrderIdFromPrescriptionId(createPharmascistPrescriptionResponse
					.optString("pid"));
			Assertion.assertNotNull(orderId, "Order Id is null");
			System.out.println("Order id " + orderId);
			return orderId;
		} else {
			return testDataFactory.getOrderid();
		}
	}
	


	public static String editRxAndReturnOrderId(
			TestDataFactory testDataFactory) {
			new Services().pharmacistLogin(serviceUrl
					+ pharmacistLoginWebServiceUrl, pharmacistUserName,
					pharmacistPassword);
			System.out.println("Edit Prescription Id = "+testDataFactory.getPrescriptionId());
			System.out.println("Customer Id = "+testDataFactory.getCustomerId());
			testDataFactory.setPrescriptionId(testDataFactory.getPrescriptionId());
			JSONObject editPharmascistPrescriptionResponse = new Services()
			.createPharmascistPrescription(serviceUrl
					+ pharmacistCreatePrescription, testDataFactory);
			System.out.println(serviceUrl + pharmacistCreatePrescription);
			String orderId = getLatestOrderIdFromPrescriptionId(testDataFactory.getPrescriptionId());
			System.out.println("Previous Order Id --> "+testDataFactory.getOrderid());
			testDataFactory.setOrderid(orderId);
			System.out.println("Edit Order id --> " + testDataFactory.getOrderid());
			return testDataFactory.getOrderid();
	}

	public String createPrescriptionForZipRxAndReturnOrderId(
			TestDataFactory testDataFactory) {
		if (testDataFactory.isOrderToBeCreated()) {
			/*
			 * Services service = new Services(); service.doctorLogin(serviceUrl
			 * + doctorLoginWebServiceUrl, doctorAppMobileNo,
			 * doctorAppPassword); JSONObject testDataJson =
			 * testDataFactory.getTestCaseParameters(); String doctorId =
			 * service.doctorInformation(serviceUrl + doctorInfoWebServiceUrl,
			 * "docid"); testDataJson.put("mobile",
			 * TestDataGenerator.generateRandomMobileNumber());
			 * testDataJson.put("docId", doctorId); //Services service = new
			 * Services(); //new Services().doctorLogin(serviceUrl +
			 * doctorLoginWebServiceUrl, // doctorAppMobileNo,
			 * doctorAppPassword);
			 */
			JSONObject createDoctorPrescriptionResponse = new Services()
					.createPrescription(
							serviceUrl + createPrescriptionUrl,
							new Services()
									.getCreatedPrescriptionStringZipRx(testDataFactory));
			// String customerId =
			// createDoctorPrescriptionResponse.optString("cid");
			// String orderId = getOrderIdFromCustomerId(customerId);
			String orderId = getOrderIdFromPrescriptionId(createDoctorPrescriptionResponse
					.optString("pid"));
			testDataFactory.setOrderid(orderId);
			Assertion.assertNotNull(orderId, "Order Id is null");
			System.out.println("Order Id :-->" + orderId);
			return orderId;
		} else {
			return testDataFactory.getOrderid();
		}

	}

	/*
	 * String customerId = createDoctorPrescriptionResponse.optString("cid");
	 * String orderId = getOrderIdFromCustomerId(customerId);
	 * testDataFactory.setOrderid(orderId); Assertion.assertNotNull(orderId,
	 * "Order Id is null"); System.out.println("Order Id :-->" + orderId);
	 * return orderId; } else { return testDataFactory.getOrderid(); }
	 */

	// protected String updateInventoryAndCreatePrescription(
	// TestDataFactory testDataFactory, int updteqty) {
	// updateInventoryForMedicineId(testDataFactory, updteqty);
	// String orderId =
	// createPrescriptionForDoctorAndReturnOrderId(testDataFactory);
	// System.out.println("Order Id Created -->"+orderId);
	// return orderId;
	// }

	public static String getISTDateFromISOFormat(String isoDate) {
		String[] date = isoDate.split("\"");
		String dbDOB = date[date.length - 2];

		// SimpleDateFormat sdf = new
		// SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		DateTimeZone timeZone = DateTimeZone.forID("Asia/Calcutta");
		DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy")
				.withZone(timeZone);

		DateTime dateTime2 = new DateTime(dbDOB, timeZone);
		System.out.println(dateTime2);
		return dateTime2.getDayOfMonth() + "/" + dateTime2.getMonthOfYear()
				+ "/" + dateTime2.getYear();

	}

	public void cleanupData(String orderId) {
		System.out.println("Removing order after test" + orderId);
		NewMongoDbUtil.getInstance().clearOrder(orderId);
	}

	public void createTripOnly(String loginid, String passwd, String orderid)
			throws NotCurrentPageException, IOException {
		loginInAdminApp(loginid, passwd, true);
		CreateTripPageUtil ctpu = new CreateTripPageUtil();
		ctpu.clickCreateTripLink();
		CreateTripPage ctp = new CreateTripPage();
		ctp.clickAvailableOrders();
		ctpu.clickAddToTrip(orderid);
	}

	public void StartBrowser() {
		// Create object of FirefoxProfile in built class to access Its
		// properties.
		FirefoxProfile fprofile = new FirefoxProfile();
		// Set Location to store files after downloading.
		fprofile.setPreference("browser.download.dir", "c:\\medlife\\digest");
		fprofile.setPreference("browser.download.folderList", 2);
		// Set Preference to not show file download confirmation dialogue using
		// MIME types Of different file extension types.
		fprofile.setPreference("browser.helperApps.neverAsk.saveToDisk",
				"text/plain;");
		fprofile.setPreference("browser.download.manager.showWhenStarting",
				false);
		// Pass fprofile parameter In webdriver to use preferences to download
		// file.
		WebDriver driver = new FirefoxDriver(fprofile);
		driver.get(slateRxUrl);
	}

	public void uploadSlateRxPDFtoDigiQ(int noofpages)
			throws NotCurrentPageException, IOException, InterruptedException {
		SlateRxUIHomePage homepage = new SlateRxUIHomePage();
		homepage.assertHomePageText();
		for (int i = 1; i <= noofpages; i++) {
			homepage.writeActionForSinglePage(5);
			homepage.clickAddPageButton();
		}
		homepage.clickSubmitRxButton();
	}

	public String getReceiveTime() {
		try {
			DateFormat dateFormat = new SimpleDateFormat(
					"dd/MM/yyyy 'at' hh:mma");
			Date currentDate = new Date();
			final Date date = dateFormat.parse(dateFormat.format(currentDate));
			final Calendar time = Calendar.getInstance();
			time.setTime(date);
			// System.out.println(dateFormat.format(currentDate)+" at "+time.getTime());
			return dateFormat.format(time.getTime());
		} catch (Exception e) {
			Assertion.fail("Something wrong while parsing data");
		}
		return "";
	}
	
	public String getCurrentDate() {
		try {
			DateFormat dateFormat = new SimpleDateFormat(
					"dd/MM/yyyy");
			Date currentDate = new Date();
			System.out.println(dateFormat.format(currentDate));
			return dateFormat.format(currentDate);
		} catch (Exception e) {
			Assertion.fail("Something wrong while parsing data");
		}
		return "";
	}

	/*
	 * public String getDate() { try { DateFormat dateFormat = new
	 * SimpleDateFormat("dd/MM/yyyy"); Date currentDate = new Date();
	 * System.out.println(dateFormat.format(currentDate)); final Date date =
	 * dateFormat.parse(dateFormat.format(currentDate)); final Calendar calendar
	 * = Calendar.getInstance(); calendar.setTime(date);
	 * calendar.add(Calendar.DAY_OF_YEAR, 1);
	 * //dateSlotRescheduled.sendKeys(dateFormat.format(calendar.getTime()));
	 * System.out.println(calendar.getTime()); return
	 * dateFormat.format(calendar.getTime()); } catch (Exception e) {
	 * Assertion.fail("Something wrong while parsing data"); } return ""; }
	 */
	
	/*public void processEvent(TestDataFactory dataFactory, String stateOrEvent) 
			throws NotCurrentPageException, IOException {
		NewOrderManagementTestNG cx = new NewOrderManagementTestNG();
		cx.loginAsCXAndUpdateOrder(dataFactory);
	}*/
	
	/**
     * Creates a PDF document.
     */
    public void createSlateRxPdf(String filename) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        //document.addTitle("SlateRx PDF");
        document.open();
        Image img1 = Image.getInstance("src/main/java/com/medlife/resources/slaterx_img_1.png");
        Image img2 = Image.getInstance("src/main/java/com/medlife/resources/slaterx_img_2.png");
        Image img3 = Image.getInstance("src/main/java/com/medlife/resources/slaterx_img_3.png");
        //img1.setCompressionLevel(100);
        //img2.setCompressionLevel(100);
        //img3.setCompressionLevel(100);
        document.add(img1);
        document.add(img2);
        document.add(img3);
        document.close();
    }
    
    public String convertToByteArray() throws FileNotFoundException, IOException {
        Path pdf = Paths.get("src/main/java/com/medlife/resources/slaterx.pdf");
        byte[] pdfContent = Files.readAllBytes(pdf);
        String stringToStore = Base64.encodeBytes(pdfContent);
		return stringToStore;
    }
    
    public void createExternalRxPdf(String filename) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.addTitle("ExternalRx PDF");
        document.open();
        Image img = Image.getInstance("src/main/java/com/medlife/resources/externalrx_img.png");
        img.setCompressionLevel(100);
        document.add(img);
        document.add(img);
        document.add(img);
        document.close();
    }
    
    public String convertExtRxToByteArray() throws FileNotFoundException, IOException {
        Path pdf = Paths.get("src/main/java/com/medlife/resources/externalrx.pdf");
        byte[] pdfContent = Files.readAllBytes(pdf);
        String stringToStore = Base64.encodeBytes(pdfContent);
		return stringToStore;
    }
    
    public void getRxIdDrafIdOrderId(TestDataFactory datafactory, String mobileNumber) {
		getPrescriptionIdfromCustomerMobile(datafactory, mobileNumber);
		getDraftRxfromPrescriptionId(datafactory,datafactory.getPrescriptionId());
		getOrderIdfromCustomerMobile(datafactory, mobileNumber);
		System.out.println("Rx Id = "+datafactory.getPrescriptionId());
		System.out.println("DraftRx Id = "+datafactory.getDraftRxId());
		System.out.println("Order Id = "+datafactory.getOrderid());
	}
    
    
    ///// added newly
    
    
    

}
