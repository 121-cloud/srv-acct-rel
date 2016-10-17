/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.baseinfo;

import io.vertx.core.json.JsonArray;

import otocloud.acct.dao.AccountDAO;
import otocloud.common.ActionURI;
import otocloud.common.Command;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.gw.common.GatewayAgent;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;


/**
 * 
 * @author hugw
 *
 */
public class GetERPUserBindingStateHandler extends OtoCloudEventHandlerImpl<Command> {

	public static final String BIND_ENABLE = "erp_user_binding.get";
	public static final String ERP_BIND_APPID = "otocloud-app-common-uid";
	
	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public GetERPUserBindingStateHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}


	@Override
	public void handle(OtoCloudBusMessage<Command> msg) {
		Command cmd = msg.body();
		cmd.setMessageCarrier(msg.getEventMessage());
		
		//componentImpl.getLogger().info(body.toString());
		
		//JsonObject params = body.getJsonObject("queryParams");
		
		
		JsonObject sessionInfo = cmd.getSessions();
		
		Integer accId = sessionInfo.getInteger("acctId");	
		
		AccountDAO accountManagementDAO = new AccountDAO();
		accountManagementDAO.setDataSource(componentImpl.getSysDatasource());
		accountManagementDAO.GetERPUserBindSettingHandler(accId, sessionInfo,
			daoRet -> {	
				if (daoRet.failed()) {
					Throwable err = daoRet.cause();
					String errMsg = err.getMessage();
					componentImpl.getLogger().error(errMsg, err);	
					msg.fail(400, errMsg);
											
				} else {
					ResultSet enabledResult = daoRet.result();
					if (enabledResult == null || enabledResult.getNumColumns() <= 0) {																			
						msg.fail(400, "未设置启用数据。");
					} else {
						JsonObject enabledRetItem = enabledResult.getRows().get(0);
						if(enabledRetItem.getBoolean("enable_erp_user") == true){						
	
							GatewayAgent gwAgent = new GatewayAgent(this.componentImpl.getVertx(), accId);
							gwAgent.getAdapterConfig(ERP_BIND_APPID, gatewayRet -> {
								if (gatewayRet.succeeded()) {
									// 判断适配器状态，若OFFLINE，执行部署
									JsonObject ret = gatewayRet.getData();
									ret.put("isEnabled", true);
									cmd.succeed(componentImpl.getVertx(), new JsonArray().add(ret));
	
								} else {
									String errString = gatewayRet.getStatusMessage();
									componentImpl.getLogger().error(errString);
									cmd.fail(componentImpl.getVertx(), errString);
								}
							});
							
						}else{
							cmd.succeed(componentImpl.getVertx(), new JsonArray().add(new JsonObject().put("isEnabled", false)));
						}
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
		handlerDescriptor.setMessageFormat("command");
		
		//参数
/*		List<ApiParameterDescriptor> paramsDesc = new ArrayList<ApiParameterDescriptor>();
		paramsDesc.add(new ApiParameterDescriptor("targetacc",""));		
		paramsDesc.add(new ApiParameterDescriptor("soid",""));		
		handlerDescriptor.setParamsDesc(paramsDesc);	*/
		
		ActionURI uri = new ActionURI("erp_user_binding", HttpMethod.GET);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return BIND_ENABLE;
	}

}
