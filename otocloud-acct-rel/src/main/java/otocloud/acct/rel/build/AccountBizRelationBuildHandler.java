/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.rel.build;

import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import io.vertx.core.json.JsonObject;

/**
 * 
 * @author hugw
 *
 */
public class AccountBizRelationBuildHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String ACCOUNT_BUILD = "build";
	
	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public AccountBizRelationBuildHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		componentImpl.getLogger().info(body.toString());
		
		//JsonObject params = body.getJsonObject("queryParams");
		
		JsonObject acctRegInfo = body.getJsonObject("content");
		JsonObject sessionInfo = body.getJsonObject("session",null);
		
		Integer accId = sessionInfo.getInteger("acctId");
	
		Integer fromAppId = acctRegInfo.getInteger("fromAppId");
		Integer bizRoleRelId = acctRegInfo.getInteger("bizRoleRelId");
		Boolean isReverse = acctRegInfo.getBoolean("isReverse");
		Integer fromAccount = accId;
		Integer toAccount = acctRegInfo.getInteger("toAccount");
		Integer toAppId = acctRegInfo.getInteger("toAppId");
		
		AccountBizRelationBuilderImpl accountBizRelationBuilder = new AccountBizRelationBuilderImpl(this.componentImpl, 
				componentImpl.getSysDatasource().getSqlClient());
		
		accountBizRelationBuilder.createAccountRelation(fromAppId, bizRoleRelId, isReverse, fromAccount, toAccount, toAppId, retHandler->{
	
				if (retHandler.failed()) {
					Throwable err = retHandler.cause();
					String errMsg = err.getMessage();
					componentImpl.getLogger().error(errMsg, err);	
					msg.fail(400, errMsg);
											
				} else {
					JsonObject retObj = new JsonObject().put("result", "ok");
					msg.reply(retObj);
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
		
/*		ActionURI uri = new ActionURI("", HttpMethod.PUT);
		handlerDescriptor.setRestApiURI(uri);*/
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return ACCOUNT_BUILD;
	}

}
