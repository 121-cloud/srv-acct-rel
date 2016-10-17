/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.operator;

import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;


public class OperatorCreateHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String OP_CREATE = "create";
	
	public static final String USER_REGISTER = "user-management.users.post";

	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public OperatorCreateHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	/**
		{
		   "name": "lj",
		   "password":"www",
		   "cell_no":"15110284698",
		   "email":"lj@lenovo.com"
		}
	 */
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		//JsonObject operator = body.getJsonObject("content");
		//JsonObject sessionInfo = body.getJsonObject("session",null);	
		
		componentImpl.getLogger().info(body.toString());
			
		registerUser(body, regUser->{
			if(regUser.succeeded()){
				msg.reply(body);	
			}else{
				Throwable err = regUser.cause();
				String errMsg = err.getMessage();
				componentImpl.getLogger().error(errMsg, err);	
				msg.fail(400, errMsg);
			}
		});


	}
	
	private void registerUser(JsonObject userRegInfo, Handler<AsyncResult<Void>> next){
		String authSrvName = componentImpl.config().getJsonObject("auth_service").getString("service_name","");
		String address = authSrvName + "." + USER_REGISTER; 
				
		Future<Void> ret = Future.future();
		ret.setHandler(next);		
		
		componentImpl.getEventBus().send(address,
				userRegInfo, regUserRet->{
					if(regUserRet.succeeded()){
						JsonObject userInfo = (JsonObject)regUserRet.result().body(); 
						userRegInfo.put("id", userInfo.getJsonObject("data").getString("userOpenId"));						
						ret.complete();											
					}else{						
						Throwable err = regUserRet.cause();
						err.printStackTrace();		
						ret.fail(err);
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
		
		ActionURI uri = new ActionURI("", HttpMethod.POST);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return OP_CREATE;
	}

}
