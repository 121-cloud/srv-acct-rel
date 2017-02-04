/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.baseinfo;

import otocloud.acct.dao.AccountDAO;
import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;

/**
 * 
 * @author hugw
 *
 */
public class AccountModifyHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String ACCOUNT_MODIFIY = "modify";
	
	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public AccountModifyHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	/**
		{
		"acct_name":"lenovo",
		"industry_code":"3911",
		"ownership_code":"100",
		"area_code":"110108",
		"address":"上地7街38号",
		"tel":"010-6956789",
		"email":"admin@lenovo.com",
		"website_url":"www.lenovo.com",
		"description":"世界五百强，PC业老大。",
		}
	 */
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		componentImpl.getLogger().info(body.toString());
		
		//JsonObject params = body.getJsonObject("queryParams");
		
		JsonObject acctRegInfo = body.getJsonObject("content");
		JsonObject sessionInfo = msg.getSession();
		
		Long accId = Long.parseLong(sessionInfo.getString("acct_id"));
		
		AccountDAO accountManagementDAO = new AccountDAO();
		accountManagementDAO.setDataSource(componentImpl.getSysDatasource());
		accountManagementDAO.modifyAccountInfo(accId, acctRegInfo, sessionInfo,
			daoRet -> {
	
				if (daoRet.failed()) {
					Throwable err = daoRet.cause();
					String errMsg = err.getMessage();
					componentImpl.getLogger().error(errMsg, err);	
					msg.fail(400, errMsg);
											
				} else {
					UpdateResult result = daoRet.result();
					if (result.getUpdated() <= 0) {						
						String errMsg = "更新影响行数为0";
						componentImpl.getLogger().error(errMsg);									
						msg.fail(400, errMsg);
					} else {
						msg.reply(acctRegInfo);
					}
				}
	
			});

	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public HandlerDescriptor getHanlderDesc() {		
		
		HandlerDescriptor handlerDescriptor = super.getHanlderDesc();
		
		//参数
/*		List<ApiParameterDescriptor> paramsDesc = new ArrayList<ApiParameterDescriptor>();
		paramsDesc.add(new ApiParameterDescriptor("targetacc",""));		
		paramsDesc.add(new ApiParameterDescriptor("soid",""));		
		handlerDescriptor.setParamsDesc(paramsDesc);	*/
		
		ActionURI uri = new ActionURI("", HttpMethod.PUT);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return ACCOUNT_MODIFIY;
	}

}
