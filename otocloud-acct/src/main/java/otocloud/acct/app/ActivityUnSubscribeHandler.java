/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.app;

import otocloud.acct.dao.AppSubscribeDAO;
import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.persistence.dao.TransactionConnection;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;


public class ActivityUnSubscribeHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String DEP_DELETE = "activity-delete";

	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public ActivityUnSubscribeHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	/**
	{
		"acct_app_activity_id":
	}
	 */
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		JsonObject subscribeInfo = body.getJsonObject("content");
		//JsonObject sessionInfo = msg.getSession();		
		
		Long acct_app_activity_id = subscribeInfo.getLong("acct_app_activity_id");
		//Long acct_id = subscribeInfo.getLong("acct_id");
		
		
		JDBCClient jdbcClient = componentImpl.getSysDatasource().getSqlClient();
		jdbcClient.getConnection(conRes -> {
			if (conRes.succeeded()) {				
				SQLConnection conn = conRes.result();
				TransactionConnection.createTransactionConnection(conn, transConnRet->{
					if(transConnRet.succeeded()){
						TransactionConnection transConn = transConnRet.result();
						AppSubscribeDAO appSubscribeDAO = new AppSubscribeDAO(componentImpl.getSysDatasource());
						//订购应用
						appSubscribeDAO.activityUnSubscribe(transConn, acct_app_activity_id, appSubscribeRet->{
							if(appSubscribeRet.succeeded()){	
								//Map<Long, Long> activityMap = appSubscribeRet.result();
								transConn.commitAndClose(closedRet->{
									
									msg.reply(appSubscribeRet.result().toJson());
									
									//应用引擎加载账户应用实例												
/*									String rfbSrvAddress = appInst + ".platform.app_inst.load"; 
										
									JsonObject instLoadMsg = new JsonObject()
										.put("acct_id", acctId)
										.put("app_version_id", appVerId);

									componentImpl.getEventBus().send(rfbSrvAddress,
											instLoadMsg, createInstRet->{
												if(createInstRet.succeeded()){
																				
													msg.reply(subscribeInfo);
												}else{		
													Throwable err = createInstRet.cause();	
													String errMsg = err.getMessage();
													componentImpl.getLogger().error(errMsg,err);																	
													msg.fail(400, errMsg);
												}	
												
									});	*/
									
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
