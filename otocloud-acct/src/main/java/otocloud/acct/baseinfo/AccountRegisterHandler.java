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
import otocloud.persistence.dao.TransactionConnection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;


public class AccountRegisterHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String ACCOUNT_REGISTER = "register";//响应用户注册	
	
	
/*	public static final int RFB_BIZROLE_REL_ID = 1; //保理商业角色关系
	public static final boolean RFB_FROM_BIZROLE_REL_IsReverse = true; //在核心企业看是否反向关系
	public static final int RFB_APP_ID = 2; //保理应用ID
	public static final int RFB_APP_VER_ID = 2; //保理应用版本	
	public static final int RFB_ENT_BIZROLE_ID = 2; //保理应用中的核心企业角色	
	
	public static final int RFB_CANA_ID = 1; //凯拿账户号
	public static final int RFB_CANA_APP_ID = 3; //凯拿应用ID
*/	
	
	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public AccountRegisterHandler(OtoCloudComponentImpl componentImpl) {
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
			"manager": {
			   "name": "lj",
			   "password":"www",
			   "cell_no":"15110284698",
			   "email":"lj@lenovo.com"
			}
		}
	 */
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		JsonObject acctRegInfo = body.getJsonObject("content");
		JsonObject sessionInfo = msg.getSession();
		
		//Long accId = Long.parseLong(sessionInfo.getString("acct_id"));
		
/*		String accname = acctRegInfo.getString("acct_name");
		String industry = acctRegInfo.getString("industry_code","");
		String ownership = acctRegInfo.getString("ownership_code", "");
		String areaCode = acctRegInfo.getString("area_code", "");
		String address = acctRegInfo.getString("address", "");		
		String invitationCode = acctRegInfo.getString("invitation_code", "");
		String tel = acctRegInfo.getString("tel", "");
		String email = acctRegInfo.getString("email", "");
		String websiteUrl = acctRegInfo.getString("website_url", "");
		String description = acctRegInfo.getString("description", "");*/
		
	
		JDBCClient jdbcClient = componentImpl.getSysDatasource().getSqlClient();
		jdbcClient.getConnection(conRes -> {
			if (conRes.succeeded()) {				
				SQLConnection conn = conRes.result();
				TransactionConnection.createTransactionConnection(conn, transConnRet->{
					if(transConnRet.succeeded()){
						TransactionConnection transConn = transConnRet.result();
						AccountDAO accountManagementDAO = new AccountDAO();
						accountManagementDAO.registerAccount(transConn, acctRegInfo, sessionInfo, 
						daoRet -> {

							if (daoRet.failed()) {
								Throwable err = daoRet.cause();
								String errMsg = err.getMessage();
								componentImpl.getLogger().error(errMsg, err);									
								transConn.rollbackAndClose(closedRet->{												
									msg.fail(400, errMsg);
								});									
/*								transConn.close(closedRet->{
									msg.fail(400, errMsg);
								});	*/													
							} else {
								JsonObject result = daoRet.result();
								result.put("user", acctRegInfo.getJsonObject("manager"));
/*								Long accId = result.getLong("acct_id");
								acctRegInfo.put("id", accId);
								
								JsonObject managerInfo = acctRegInfo.getJsonObject("manager");
								managerInfo.put("org_acct_id", accId);*/
								
								registerUser(result, regUser->{
									if(regUser.succeeded()){
										
										//sessionInfo.put("user_id", regUser.result());
										
										transConn.commitAndClose(closedRet->{
											msg.reply(acctRegInfo);												
											
											//创建凯拿应付保理应用实例,构建账户关系
/*											sessionInfo.put("acct_id", accId);					
											createCanaBizRelationForAcc(accId, sessionInfo, createInstRet->{
												
											});		*/										

										});												
									}else{
										componentImpl.getLogger().info("注册用户失败，账户注册回滚!");
										Throwable err = regUser.cause();
										String errMsg = err.getMessage();
										componentImpl.getLogger().error(errMsg, err);	
										transConn.rollbackAndClose(closedRet->{												
											msg.fail(400, errMsg);
										});			
									}
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
	
	private void registerUser(JsonObject userRegInfo, Handler<AsyncResult<Long>> next){
		String authSrvName = componentImpl.getDependencies().getJsonObject("auth_service").getString("service_name","");
		String address = authSrvName + ".user-management.register";
		
		JsonObject msg = new JsonObject();
		msg.put("content", userRegInfo);
		
		Future<Long> ret = Future.future();
		ret.setHandler(next);		
		
		componentImpl.getEventBus().send(address,
				msg, regUserRet->{
					if(regUserRet.succeeded()){
						JsonObject userInfo = (JsonObject)regUserRet.result().body(); 
						//userRegInfo.put("id", userInfo.getJsonObject("data").getInteger("userId"));						
						ret.complete(userInfo.getLong("id", 0L));											
					}else{		
						Throwable err = regUserRet.cause();						
						err.printStackTrace();		
						ret.fail(err);
					}	
					
		});	
		
	}
	

/*	private void createCanaBizRelationForAcc(Integer acctId, JsonObject sessionInfo, Handler<AsyncResult<Void>> next){
		
		Future<Void> ret = Future.future();
		ret.setHandler(next);
		
		AccountBizRoleDAO accountBizRoleDAO = new AccountBizRoleDAO();
		accountBizRoleDAO.setDataSource(componentImpl.getSysDatasource());		
		
		//添加账户应用配套角色
		accountBizRoleDAO.AddBizRole(acctId, RFB_ENT_BIZROLE_ID, sessionInfo, daoRet -> {
			if (daoRet.failed()) {
				Throwable err = daoRet.cause();
				String errMsg = err.getMessage();
				componentImpl.getLogger().error(errMsg, err);	
				ret.fail(err);
			} else {
				UpdateResult result = daoRet.result();
				if (result.getUpdated() <= 0) {						
					String errMsg = "更新账户业务角色影响行数为0";
					componentImpl.getLogger().error(errMsg);									
					ret.fail(errMsg);						
				} else {					
					
					AppSubscriptionDAO appSubscriptionDAO = new AppSubscriptionDAO();
					appSubscriptionDAO.setDataSource(componentImpl.getSysDatasource());		
					
					//添加账户应用资产
					appSubscriptionDAO.subscribeApp(acctId, RFB_APP_VER_ID, sessionInfo, appSubRet -> {
						if (appSubRet.failed()) {
							Throwable err = appSubRet.cause();
							String errMsg = err.getMessage();
							componentImpl.getLogger().error(errMsg, err);	
							ret.fail(err);
						} else {
							UpdateResult appSubResult = appSubRet.result();
							if (appSubResult.getUpdated() <= 0) {						
								String errMsg = "添加账户应用资产影响行数为0";
								componentImpl.getLogger().error(errMsg);									
								ret.fail(errMsg);						
							} else {
								
								//通知金服桥应用引擎加载账户应用实例
								String rfbSrvName = componentImpl.getDependencies().getJsonObject("rfb_service").getString("service_name","");
								String rfbSrvAddress = rfbSrvName + ".platform.app_inst.load"; 
									
								JsonObject instLoadMsg = new JsonObject()
									.put("org_acct_id", acctId)
									.put("biz_role_id", RFB_ENT_BIZROLE_ID);

								componentImpl.getEventBus().send(rfbSrvAddress,
										instLoadMsg, createInstRet->{
											if(createInstRet.succeeded()){
												//componentImpl.getLogger().info("账户[" + acctId.toString() + "]的凯拿应付保理APP实例创建成功!");
												//ret.complete();											
											}else{		
												Throwable err = createInstRet.cause();		
												//componentImpl.getLogger().info("账户[" + acctId.toString() + "]的凯拿应付保理APP实例创建失败!");
												err.printStackTrace();		
												//ret.fail(err);
											}	
											
								});	
								
								//默认使用凯拿保理服务,构建账户关系
								
								String accRelSrvName = componentImpl.getDependencies().getJsonObject("acct_rel_service").getString("service_name","");
								String address = accRelSrvName + ".biz-rel-builder.build"; 
								
								JsonObject msg = new JsonObject();
								msg.put("session", sessionInfo);
								msg.put("content", new JsonObject().put("bizRoleRelId", RFB_BIZROLE_REL_ID)
																   .put("isReverse", RFB_FROM_BIZROLE_REL_IsReverse)
																   .put("fromAppId", RFB_APP_ID)
																   .put("toAppId", RFB_CANA_APP_ID)
																   .put("toAccount", RFB_CANA_ID)
										
										);
								
								
								componentImpl.getEventBus().send(address,
										msg, regAcctRelRet->{
											if(regAcctRelRet.succeeded()){
												ret.complete();	
												
											}else{		
												Throwable err = regAcctRelRet.cause();						
												err.printStackTrace();		
												ret.fail(err);
											}	
											
								});	
								
								
								

							}
						}

					});
					

				}
			}

		});
				
	}*/
	
	
	
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
		
		ActionURI uri = new ActionURI("", HttpMethod.POST);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return ACCOUNT_REGISTER;
	}

}
