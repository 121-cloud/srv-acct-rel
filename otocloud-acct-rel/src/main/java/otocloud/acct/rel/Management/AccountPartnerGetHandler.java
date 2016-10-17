/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.rel.Management;

import java.util.List;

import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

/**
 * 
 * @author hugw
 *
 */
public class AccountPartnerGetHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String ACCOUNT_PARTNER_GET = "partner.get";
	
	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public AccountPartnerGetHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		componentImpl.getLogger().info(body.toString());
		
		JsonObject acctRegInfo = body.getJsonObject("content");
		JsonObject sessionInfo = body.getJsonObject("session",null);
		
		Integer accId = sessionInfo.getInteger("acctId");
	
		Integer appId = acctRegInfo.getInteger("appId");
		Integer bizRoleRelId = acctRegInfo.getInteger("bizRoleRelId");
		Boolean isReverse = acctRegInfo.getBoolean("isReverse");
		
		
		JDBCClient sqlClient = componentImpl.getSysDatasource().getSqlClient();
		
		sqlClient.getConnection(connRes -> {
			if (connRes.succeeded()) {
				final SQLConnection conn = connRes.result();				
				conn.setAutoCommit(true, res ->{
				  if (res.failed()) {
					  closeDBConnect(conn);
	  	    		  Throwable err = res.cause();
	  	    		  String replyMsg = err.getMessage();
	  	    		  componentImpl.getLogger().error(replyMsg, err);
	  	    		  msg.fail(400, replyMsg);
				  }else{										
					conn.queryWithParams("SELECT to_org_acct_id,acct_name from view_app_inst_acct_rel WHERE org_acct_id=? AND app_module_id=? AND biz_role_rel_id=? AND is_reverse=?", 
							   new JsonArray().add(accId)
											  .add(appId)
											  .add(bizRoleRelId)
											  .add(isReverse ? 1 : 0),											  
					  toRoleAcc->{								  
						  if (toRoleAcc.succeeded()) {
							List<JsonObject> ret = toRoleAcc.result().getRows();
							JsonArray retMsg = new JsonArray(ret);
							msg.reply(retMsg);
						  }else{
			  	    		  Throwable err = toRoleAcc.cause();
			  	    		  String replyMsg = err.getMessage();
			  	    		  componentImpl.getLogger().error(replyMsg, err);
			  	    		  msg.fail(400, replyMsg);
			  	    	  }
						  closeDBConnect(conn);
					  });
					}
				});
		
			}else{
				  Throwable err = connRes.cause();
				  String replyMsg = err.getMessage();
				  componentImpl.getLogger().error(replyMsg, err);
				  msg.fail(400, replyMsg);
		    }
		});
	

	}
	
	private void closeDBConnect(SQLConnection conn){
		conn.close(handler->{
			if (handler.failed()) {
				Throwable conErr = handler.cause();
				componentImpl.getLogger().error(conErr.getMessage(), conErr);
			} else {								
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
		
/*		ActionURI uri = new ActionURI("", HttpMethod.PUT);
		handlerDescriptor.setRestApiURI(uri);*/
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return ACCOUNT_PARTNER_GET;
	}

}
