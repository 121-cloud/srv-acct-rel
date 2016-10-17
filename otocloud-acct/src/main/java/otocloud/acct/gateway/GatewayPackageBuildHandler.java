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


public class GatewayPackageBuildHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String GATEWAY_BUILD_GET = "package.build";
	
	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public GatewayPackageBuildHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}


	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		JsonObject sessionInfo = body.getJsonObject("session",null);	
		
		//JsonObject sessionInfo = body.getJsonObject("queryParams");

		Integer accId = sessionInfo.getInteger("acctId");
		
		componentImpl.getLogger().info("accId=" + accId);
		
		GatewayAgent gwAgent = new GatewayAgent(componentImpl.getVertx(), accId);
		gwAgent.genInstallPackage(gwInstallRet->{													
			if(gwInstallRet.failed()){
				String errString = gwInstallRet.getStatusMessage();
				componentImpl.getLogger().error(errString);	
				msg.fail(400, errString);
			}else{
				//String gwUrl = gwInstallRet.getData().getString("installpackagedownloadurl");
				msg.reply(gwInstallRet.getData());
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
		
		ActionURI uri = new ActionURI("package", HttpMethod.GET);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return GATEWAY_BUILD_GET;
	}

}
