/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.gateway;

import otocloud.common.ActionURI;
import otocloud.common.Command;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.gw.common.GatewayAgent;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class GatewayConfigSettingHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String GATEWAY_CONFIG_PUT = "config.put";
	
	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public GatewayConfigSettingHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}


	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		JsonObject sessionInfo = body.getJsonObject("session",null);	
		
		JsonObject gwCfg = body.getJsonObject("content");

		Integer accId = sessionInfo.getInteger("acctId");
		
		GatewayAgent gwAgent = new GatewayAgent(componentImpl.getVertx(), accId);
		gwAgent.setGatewayConfig(gwCfg, gwCfgRet->{													
			if(gwCfgRet.failed()){
				String errString = gwCfgRet.getStatusMessage();
				componentImpl.getLogger().error(errString);	
				msg.fail(400, errString);
			}else{
				JsonObject gwCfgData = gwCfgRet.getData();
				
				//默认下载金福桥应用适配器
				String rfbSrvName = accId.toString() + "." + componentImpl.getDependencies().getJsonObject("rfb_service").getString("service_name","");
				String rfbAdapterCfgGetAddress = rfbSrvName + ".adapter_setting.get"; 
					
				Command adaCfgGetCmd = new Command(accId, rfbAdapterCfgGetAddress);
				adaCfgGetCmd.execute(componentImpl.getVertx(), result->{
    				if(result.succeeded()) {
    					JsonObject adaCfgData = result.getData();    					
    					if(adaCfgData != null && adaCfgData.containsKey("state")) {
    						String state = adaCfgData.getString("state");
    						if(state.equals("OFFLINE")){
    							
    							String rfbAdapterInstallAddress = rfbSrvName + ".adapter_setting.set"; 
    							
    							JsonObject adaCfg = new JsonObject();
    							adaCfg.put("adapterversion", "1.0.0-SNAPSHOT");
    							adaCfg.put("options", new JsonObject().put("config", new JsonObject()));
    							
    							Command adaInstallCmd = new Command(accId, rfbAdapterInstallAddress);
    							adaInstallCmd.setContents(new JsonArray().add(adaCfg));
    							
    							adaInstallCmd.execute(componentImpl.getVertx(), adaInstallRet->{
    			    				if(adaInstallRet.succeeded()) {
    			    					msg.reply(gwCfgData);
    			    				}else{
    			    					String errString = adaInstallRet.getStatusMessage();
    			    					componentImpl.getLogger().error(errString);	
    			    					msg.fail(400, errString);
    			    				}
    							});
    							
    						}else{
    							msg.reply(gwCfgData);
    						}
    					}else{
							String rfbAdapterInstallAddress = rfbSrvName + ".adapter_setting.set"; 
							
							JsonObject adaCfg = new JsonObject();
							adaCfg.put("adapterversion", "1.0.0-SNAPSHOT");
							adaCfg.put("options", new JsonObject().put("config", new JsonObject()));
							
							Command adaInstallCmd = new Command(accId, rfbAdapterInstallAddress);
							adaInstallCmd.setContents(new JsonArray().add(adaCfg));
							
							adaInstallCmd.execute(componentImpl.getVertx(), adaInstallRet->{
			    				if(adaInstallRet.succeeded()) {
			    					msg.reply(gwCfgData);
			    				}else{
			    					String errString = adaInstallRet.getStatusMessage();
			    					componentImpl.getLogger().error(errString);	
			    					msg.fail(400, errString);
			    				}
							});
    						
    					}
    					
    				}else{
    					String errString = result.getStatusMessage();
    					componentImpl.getLogger().error(errString);	
    					msg.fail(400, errString);    					
    				}
    				
				});	
				
				
				
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
		
		ActionURI uri = new ActionURI("config", HttpMethod.PUT);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return GATEWAY_CONFIG_PUT;
	}

}
