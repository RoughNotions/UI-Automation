package com.medlife.qa.configuration;

import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import com.medlife.qa.dataprovider.util.NewMongoDbUtil;
import com.medlife.qa.dataprovider.util.TestDataFactory;


public class InterFcConfig {
	
	static Logger logger = LogManager.getLogger(InterFcConfig.class.getName());
	public static NewMongoDbUtil mongoDb = new NewMongoDbUtil().getInstance();
	
	public static void setFcStockistPreferenceConfig(TestDataFactory dataFactory) {
		
		new NewMongoDbUtil().checkAndAddFcStockistPreferenceConfig(dataFactory);
	}
	
	public static void setProductStockist(TestDataFactory dataFactory ) throws UnknownHostException {
		
		new NewMongoDbUtil().checkAndAddProductInProductStockist(dataFactory);
	}
	
	
	public static void setSupplier(TestDataFactory dataFactory) {
		new NewMongoDbUtil().checkAndAddSupplier(dataFactory);
	}
	
	public static void setStockistThreshold(TestDataFactory dataFactory) {
		new NewMongoDbUtil().checkAndAddStockistThreshold(dataFactory);
	}
	
	

}
