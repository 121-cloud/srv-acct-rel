/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.app;

import otocloud.acct.dao.AppSubscribeDAO;
import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.CommandMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.persistence.dao.TransactionConnection;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;


public class AppUnSubscribeHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String DEP_DELETE = "delete";

	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public AppUnSubscribeHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	/**
	{
		"is_platform":
		"app_code":
		"app_inst_group":
		"acct_id":
		
		"acct_app_id":
	}
	 */
	@Override
	public void handle(CommandMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		JsonObject subscribeInfo = body.getJsonObject("content");
		//JsonObject sessionInfo = msg.getSession();		
		
		Long acct_app_id = subscribeInfo.getLong("acct_app_id");
		//Long acct_id = subscribeInfo.getLong("acct_id");
		
		Long acct_id = subscribeInfo.getLong("acct_id");
		String app_code = subscribeInfo.getString("app_code");
		String app_inst_group = subscribeInfo.getString("app_inst_group", "");
		Boolean is_platform = subscribeInfo.getBoolean("is_platform", true);
		
		
		JDBCClient jdbcClient = componentImpl.getSysDatasource().getSqlClient();
		jdbcClient.getConnection(conRes -> {
			if (conRes.succeeded()) {				
				SQLConnection conn = conRes.result();
				TransactionConnection.createTransactionConnection(conn, transConnRet->{
					if(transConnRet.succeeded()){
						TransactionConnection transConn = transConnRet.result();
						AppSubscribeDAO appSubscribeDAO = new AppSubscribeDAO(componentImpl.getSysDatasource());
						//订购应用
						appSubscribeDAO.appUnSubscribe(transConn, acct_app_id, appSubscribeRet->{
							if(appSubscribeRet.succeeded()){	
								//Map<Long, Long> activityMap = appSubscribeRet.result();
								transConn.commitAndClose(closedRet->{
									
									msg.reply(appSubscribeRet.result().toJson());
									
									if(!is_platform){							
									
										//停用账户应用实例											
										String srvAddress = app_code + "." + app_inst_group + ".platform.appinst_status.control"; 
											
										JsonObject instLoadMsg = new JsonObject()
											.put("account", acct_id.toString())
											.put("status", "stop");
	
										componentImpl.getEventBus().publish(srvAddress,
												instLoadMsg);	
										
										
										//通知删除用户的功能菜单缓存
										String portal_service = componentImpl.getDependencies().getJsonObject("portal_service").getString("service_name","");
										String address = portal_service + ".acct-menu-del.delete";

										JsonObject contentObject = new JsonObject().put("acct_id", acct_id.toString());
										 
										JsonObject commandObject = new JsonObject().put("content", contentObject);
										
										componentImpl.getEventBus().send(address,
												commandObject, cleanUserMenuRet->{
													if(cleanUserMenuRet.succeeded()){
						
													}else{		
														Throwable err = cleanUserMenuRet.cause();						
														String errMsg = err.getMessage();
														componentImpl.getLogger().error(errMsg, err);								
														
													}	
													
										});	
										
									}

								});

							}else{
								Throwable err = appSubscribeRet.cause();
								String errMsg = err.getMessage();
								componentImpl.getLogger().error(errMsg, err);									

								transConn.rollbackAndClose(closedRet->{												
									msg.fail(400, errMsg);
								});	
							}							
						});					
					}else{
						Throwable err = transConnRet.cause();
						String errMsg = err.getMessage();
						componentImpl.getLogger().error(errMsg, err);	
						conn.close(closedRet->{
							msg.fail(400, errMsg);
						});			
					}
				});
			}else{
				Throwable err = conRes.cause();
				String errMsg = err.getMessage();
				componentImpl.getLogger().error(errMsg, err);	
				msg.fail(400, errMsg);
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
		
		ActionURI uri = new ActionURI(DEP_DELETE, HttpMethod.POST);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return DEP_DELETE;
	}

}
