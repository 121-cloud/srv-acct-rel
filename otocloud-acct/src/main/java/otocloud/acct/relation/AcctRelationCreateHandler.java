/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.relation;

import java.util.List;

import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.CommandMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.acct.dao.AcctRelationDAO;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;


public class AcctRelationCreateHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String DEP_CREATE = "create";

	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public AcctRelationCreateHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	/**
	{
		"from_acct_id":
		"to_acct_id":
		"desc":
	}
	*/
	@Override
	public void handle(CommandMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		componentImpl.getLogger().info(body.toString());
		
		JsonObject acctRel = body.getJsonObject("content");
					
		AcctRelationDAO acctRelationDAO = new AcctRelationDAO(componentImpl.getSysDatasource());
		
		Long from_acct_id = acctRel.getLong("from_acct_id");
		Long to_acct_id = acctRel.getLong("to_acct_id");
		
		Future<ResultSet> existFuture = Future.future();		
		acctRelationDAO.findBizRelation(from_acct_id, to_acct_id, existFuture);
		existFuture.setHandler(existHandler->{
			if(existHandler.succeeded()){
				ResultSet resultSet = existFuture.result();
				List<JsonObject> retObjs = resultSet.getRows();
				if(retObjs == null || retObjs.size() <= 0){				
					acctRelationDAO.create(acctRel, daoRet -> {
	
						if (daoRet.failed()) {
							Throwable err = daoRet.cause();
							String errMsg = err.getMessage();
							componentImpl.getLogger().error(errMsg, err);	
							msg.fail(400, errMsg);
						} else {
							UpdateResult result = daoRet.result();
							if (result.getUpdated() <= 0) {						
								String errMsg = "更新影响行数为0";
								componentImpl.getLogger().error(errMsg);									
								msg.fail(400, errMsg);
									
							} else {
								JsonArray ret = result.getKeys();
								Integer id = ret.getInteger(0);
								acctRel.put("id", id);
	
								msg.reply(acctRel);
	
							}
						}
	
					});
				}else{
					msg.reply(retObjs.get(0));
				}
				
			}else{
				Throwable err = existHandler.cause();
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
