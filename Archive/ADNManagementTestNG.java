package com.medlife.ui.admin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.medlife.qa.controller.Assertion;
import com.medlife.qa.controller.BaseTest;
import com.medlife.qa.dataprovider.util.ExcelUtility;
import com.medlife.qa.dataprovider.util.OrderDetail;
import com.medlife.qa.driver.CustomWebDriver;
import com.medlife.qa.pages.admin.LogInPage;
import com.medlife.qa.pages.admin.adn.CreateADNPage;
import com.medlife.qa.pages.admin.adn.SearchADNPage;
import com.medlife.qa.pageutil.NotCurrentPageException;
import com.medlife.qa.util.WebTestRunListener;
import com.medlife.service.util.Services;


public class ADNManagementTestNG extends BaseTest {
	
	private String orderId;
	private String adminUserName;
	private String adminPassword;
	private String fcUserName;
	private String fcPassword;
	private String daAgentName;
	private int defaultQuantityIncrease;
	private String cusUserName;
	private String cusPassword;

	@Parameters({ "adminUserName", "adminPassword", "fcUserName", "fcPassword",
			"daAgentName", "cusUserName", "cusPassword",
			"defaultQuantityIncrease" })
	@BeforeClass(alwaysRun = true)
	public void setUpBeforeClass(String adminUserName, String adminPassword,
			String fcuserName, String fcPassword, String daAgentName,
			String cusUserName, String cusPassword, int defaultQuantityIncrease) {
		this.adminUserName = adminUserName;
		this.adminPassword = adminPassword;
		this.fcUserName = fcuserName;
		this.fcPassword = fcPassword;
		this.daAgentName = daAgentName;
		this.cusUserName = cusUserName;
		this.cusPassword = cusPassword;
		this.defaultQuantityIncrease = defaultQuantityIncrease;
		new Services().pharmacistLogin(serviceUrl
				+ pharmacistLoginWebServiceUrl, pharmacistUserName,
				pharmacistPassword);
	}

	@DataProvider(name = "OrderData", parallel = true)
	public static Iterator<Object[]> getItemData(Method m,
			ITestContext testContex) throws Exception {
		LinkedHashMap<String, Class<?>> dataClazzMap = new LinkedHashMap<String, Class<?>>();
		dataClazzMap.put("OrderDetail", OrderDetail.class);
		
		return ExcelUtility.getTestData(dataClazzMap, "src"
				+ File.separatorChar + "main" + File.separatorChar + "java"
				+ File.separatorChar + "com" + File.separatorChar + "medlife"
				+ File.separatorChar + "resources" + File.separatorChar
				+ "OrderDetail.xls", m.getName());
		

	}
	

	
	@Test
	public void adnUpload()
			throws NotCurrentPageException, IOException, InterruptedException, ParseException {
		CustomWebDriver.getWebDriver().get(this.adminAppUrl);// http://192.168.22.184/AdminWebApp");
		int random= (int)(Math.random()*100000);
		LogInPage login= new LogInPage();
		//loginInAdminAppWithFc(this.fcUserName, this.fcPassword);
		login.signIn(this.fcUserName, this.fcPassword);
		login.clickUploadADNLink();
		WebTestRunListener.createAttachment();
		CreateADNPage  cap= new CreateADNPage();
		//cap.selectStockist("Parshva");
		cap.uploadFile("src"
				+ File.separatorChar + "main" + File.separatorChar + "java"
				+ File.separatorChar + "com" + File.separatorChar + "medlife"
				+ File.separatorChar + "qa" + File.separatorChar
				+ "pages"+ File.separatorChar + "admin" + File.separatorChar + "adn" + File.separatorChar + "resources" +File.separatorChar+"FirstUpload.csv"
				);
		cap.assertADNUploadSuccessful();
		login.clickSearchADNLink();
		SearchADNPage sap = new SearchADNPage();
		sap.selectADNStatus("OPEN");
		sap.uploadFile("src"
				+ File.separatorChar + "main" + File.separatorChar + "java"
				+ File.separatorChar + "com" + File.separatorChar + "medlife"
				+ File.separatorChar + "qa" + File.separatorChar
				+ "pages"+ File.separatorChar + "admin" + File.separatorChar + "adn" + File.separatorChar + "resources" +File.separatorChar+"secondUpload.csv");
		sap.assertADNUploadSuccessful();
		Assertion.getVerificationFailures();
		System.out.println("TestFinished");

	}
	
	@Test
	public void adnUploadInvalidExpiryDate()
			throws NotCurrentPageException, IOException, InterruptedException, ParseException {
		CustomWebDriver.getWebDriver().get(this.adminAppUrl);// http://192.168.22.184/AdminWebApp");
		int random= (int)(Math.random()*100000);
		LogInPage login= new LogInPage();
		//loginInAdminAppWithFc(this.fcUserName, this.fcPassword);
		login.signIn(this.fcUserName, this.fcPassword);
		login.clickUploadADNLink();
		WebTestRunListener.createAttachment();
		CreateADNPage  cap= new CreateADNPage();
		//cap.selectStockist("Parshva");
		cap.uploadFile("src"
				+ File.separatorChar + "main" + File.separatorChar + "java"
				+ File.separatorChar + "com" + File.separatorChar + "medlife"
				+ File.separatorChar + "qa" + File.separatorChar
				+ "pages"+ File.separatorChar + "admin" + File.separatorChar + "adn" + File.separatorChar + "resources" +File.separatorChar+"InvalidExpityDate.csv"
				);
		cap.assertInvalidUpload();
		
		Assertion.getVerificationFailures();
		System.out.println("TestFinished");

	}
	
	
	
	
}
