/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.gateway;

import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.gw.common.GatewayAgent;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;


public class GatewayStatusHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String GATEWAY_STATUS_GET = "state.query";
	
	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public GatewayStatusHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}


	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		JsonObject sessionInfo = body.getJsonObject("session",null);	
		
		//JsonObject sessionInfo = body.getJsonObject("queryParams");

		Integer accId = sessionInfo.getInteger("acctId");
		
		GatewayAgent gwAgent = new GatewayAgent(componentImpl.getVertx(), accId);
		gwAgent.queryState(gwStateRet->{													
			if(gwStateRet.failed()){
				String errString = gwStateRet.getStatusMessage();
				componentImpl.getLogger().error(errString);	
				msg.fail(400, errString);
			}else{
				msg.reply(gwStateRet.getData());
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
		
		ActionURI uri = new ActionURI("state", HttpMethod.GET);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return GATEWAY_STATUS_GET;
	}

}
