/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.app;


import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.persistence.dao.TransactionConnection;
import otocloud.acct.dao.AppSubscribeDAO;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;



public class AppSubscribeHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String DEP_CREATE = "create";

	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public AppSubscribeHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	/**
	
		[
			{
				"acct_id":
				"d_app_id":
				"app_version_id":
				"d_app_version":
				"is_platform":
				"app_inst_group": 应用实例分组
				"activities":[
					{
						"app_activity_id":
					}
				]
			}
		]
	
	*/
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		componentImpl.getLogger().info(body.toString());
		
		JsonArray subscribeInfos = body.getJsonArray("content");
		JsonObject sessionInfo = msg.getSession();		
		
		Long acctId = subscribeInfos.getJsonObject(0).getLong("acct_id");
		
		//Long acctId = subscribeInfo.getLong("acct_id");
		//Long appId = subscribeInfo.getLong("d_app_id");
		//Long appVerId = subscribeInfo.getLong("app_version_id");
		//String appInst = subscribeInfo.getString("app_inst_group");
		
		//Long userId = Long.parseLong(sessionInfo.getString("user_id"));
		
		JDBCClient jdbcClient = componentImpl.getSysDatasource().getSqlClient();
		jdbcClient.getConnection(conRes -> {
			if (conRes.succeeded()) {				
				SQLConnection conn = conRes.result();
				TransactionConnection.createTransactionConnection(conn, transConnRet->{
					if(transConnRet.succeeded()){
						TransactionConnection transConn = transConnRet.result();
						AppSubscribeDAO appSubscribeDAO = new AppSubscribeDAO(componentImpl.getSysDatasource());
						//订购应用
						appSubscribeDAO.subscribeApp(transConn, subscribeInfos, sessionInfo, appSubscribeRet->{
							if(appSubscribeRet.succeeded()){	
								//Map<Long, Long> activityMap = appSubscribeRet.result();
								transConn.commitAndClose(closedRet->{	
									
									msg.reply(subscribeInfos);		
									
									//通知删除用户的功能菜单缓存
									String portal_service = componentImpl.getDependencies().getJsonObject("portal_service").getString("service_name","");
									String address = portal_service + ".acct-menu-del.delete";

									JsonObject contentObject = new JsonObject().put("acct_id", acctId.toString());
									 
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
		
		ActionURI uri = new ActionURI(DEP_CREATE, HttpMethod.POST);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return DEP_CREATE;
	}

}
