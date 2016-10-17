/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org;

import java.util.List;

import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.acct.dao.DepartmentDAO;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class DepartmentQueryHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String DEP_QUERY = "getAll";

	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public DepartmentQueryHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	/**
		{
		"dept_name":"lenovo",
		"dept_manager":"3911",
		"org_acct_id":"100"
		}
	 */
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		componentImpl.getLogger().info(body.toString());
		
		JsonObject sessionInfo = body.getJsonObject("session",null);		
		
		Integer accId = sessionInfo.getInteger("acctId");
			
		DepartmentDAO departmentDAO = new DepartmentDAO();
		departmentDAO.setDataSource(componentImpl.getSysDatasource());		
		
		departmentDAO.queryDepartments(accId, 
		daoRet -> {

			if (daoRet.failed()) {
				Throwable err = daoRet.cause();
				String errMsg = err.getMessage();
				componentImpl.getLogger().error(errMsg, err);	
				msg.fail(400, errMsg);
			} else {
				List<JsonObject> ret = daoRet.result().getRows();
				JsonArray retMsg = new JsonArray(ret);

				msg.reply(retMsg);
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
		
		ActionURI uri = new ActionURI("", HttpMethod.GET);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return DEP_QUERY;
	}

}
