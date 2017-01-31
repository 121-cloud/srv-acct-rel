/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 
package otocloud.acct.baseinfo;

import otocloud.acct.dao.AccountDAO;
import otocloud.common.ActionURI;
import otocloud.common.Command;
import otocloud.common.CommandResult;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.gw.common.AdapterState;
import otocloud.gw.common.GatewayAgent;
import otocloud.gw.common.GatewaySchema;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;

*//**
 * 
 * @author hugw
 *
 *//*
public class EnableERPUserBindingHandler extends OtoCloudEventHandlerImpl<Command> {

	public static final String BIND_ENABLE = "erp_user_binding.enable";
	public static final String ERP_BIND_APPID = "otocloud-app-common-uid";
	
	*//**
	 * Constructor.
	 *
	 * @param componentImpl
	 *//*
	public EnableERPUserBindingHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}


	@Override
	public void handle(OtoCloudBusMessage<Command> msg) {
		Command cmd = msg.body();
		cmd.setMessageCarrier(msg.getEventMessage());
		
		//componentImpl.getLogger().info(body.toString());
		
		//JsonObject params = body.getJsonObject("queryParams");
		
		JsonObject enabledInfo = cmd.getContent();
		JsonObject sessionInfo = cmd.getSessions();
		
		Integer accId = sessionInfo.getInteger("acctId");
		
		Boolean isEnabled = enabledInfo.getBoolean("isEnabled");
		
		AccountDAO accountManagementDAO = new AccountDAO();
		accountManagementDAO.setDataSource(componentImpl.getSysDatasource());
		accountManagementDAO.EnableERPUserBindingHandler(accId, isEnabled, sessionInfo,
			daoRet -> {	
				if (daoRet.failed()) {
					Throwable err = daoRet.cause();
					String errMsg = err.getMessage();
					componentImpl.getLogger().error(errMsg, err);	
					msg.fail(400, errMsg);
											
				} else {
					UpdateResult enabledResult = daoRet.result();
					if (enabledResult.getUpdated() <= 0) {						
						String errMsg = "更新影响行数为0";
						componentImpl.getLogger().error(errMsg);									
						msg.fail(400, errMsg);
					} else {
						//msg.reply(enabledInfo);
						CommandResult result = cmd.createResultObject();
						
						JsonObject adaCfg = new JsonObject();
						adaCfg.put("adapterversion", "1.0.0-SNAPSHOT");
						adaCfg.put("options", new JsonObject().put("config", new JsonObject()));
						
						if(!isEnabled){
							// 卸载适配器
							GatewayAgent gwAgent = new GatewayAgent(this.componentImpl.getVertx(), accId);
							gwAgent.unDeployAdapter(
									ERP_BIND_APPID,
									deployRet -> {
										if (deployRet.succeeded()) {

											result.put(
													GatewaySchema.ADAPTER_STATE_TAG,
													"UNDEPLOYMENT");
											cmd.reply(componentImpl.getVertx(), result);

											componentImpl.getLogger().info(
													"部署成功：" + result.toString());
											
										} else {

											String errString = deployRet
													.getStatusMessage();
											componentImpl.getLogger().error(errString);
											
											result.put(
													GatewaySchema.ADAPTER_STATE_TAG,
													"UNDEP_FAULT");
											
											result.put("err_info",  errString);

											cmd.reply(componentImpl.getVertx(), result);

											componentImpl.getLogger().info(
													result.toString());
											// deployRet.fail(errString);
										}
									});

							
						}else{
							GatewayAgent gwAgent = new GatewayAgent(this.componentImpl.getVertx(), accId);
							gwAgent.setAdapterConfig(ERP_BIND_APPID, adaCfg, gatewayRet -> {
								if (gatewayRet.succeeded()) {
									// 判断适配器状态，若OFFLINE，执行部署
									result.addData(gatewayRet.getData());
	
									if (((JsonObject) gatewayRet.getData()).getValue(
											GatewaySchema.ADAPTER_STATE_TAG).equals(
											AdapterState.OFFLINE.toString())) {
										// 部署适配器
										gwAgent.deployAdapter(
												ERP_BIND_APPID,
												deployRet -> {
													if (deployRet.succeeded()) {
	
														result.getData().put(
																GatewaySchema.ADAPTER_STATE_TAG,
																AdapterState.ONLINE.toString());
														cmd.reply(componentImpl.getVertx(), result);
	
														componentImpl.getLogger().info(
																"部署成功：" + result.toString());
														// deployRet.succeed(gatewayRet.getData());
													} else {
	
														String errString = deployRet
																.getStatusMessage();
														componentImpl.getLogger().error(errString);
														
														result.getData().put(
																GatewaySchema.ADAPTER_STATE_TAG,
																"DEP_FAULT");
														
														result.getData().put("err_info",  errString);
	
														cmd.reply(componentImpl.getVertx(), result);
	
														componentImpl.getLogger().info(
																result.toString());
														// deployRet.fail(errString);
													}
												});
	
									} else {
										System.out
												.println("corpsys_setting：Adapter is aready ready！");
										cmd.reply(componentImpl.getVertx(), result);
									}
	
								} else {
									String errString = gatewayRet.getStatusMessage();
									componentImpl.getLogger().error(errString);
									cmd.fail(componentImpl.getVertx(), errString);
								}
							});
						}
						
					}
				}
	
			});

	}
	
	
	*//**
	 * {@inheritDoc}
	 *//*
	@Override
	public HandlerDescriptor getHanlderDesc() {		
		
		HandlerDescriptor handlerDescriptor = super.getHanlderDesc();
		handlerDescriptor.setMessageFormat("command");
		
		//参数
		List<ApiParameterDescriptor> paramsDesc = new ArrayList<ApiParameterDescriptor>();
		paramsDesc.add(new ApiParameterDescriptor("targetacc",""));		
		paramsDesc.add(new ApiParameterDescriptor("soid",""));		
		handlerDescriptor.setParamsDesc(paramsDesc);	
		
		ActionURI uri = new ActionURI("erp_user_binding", HttpMethod.PUT);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	*//**
	 * {@inheritDoc}
	 *//*
	@Override
	public String getEventAddress() {
		return BIND_ENABLE;
	}

}
*/