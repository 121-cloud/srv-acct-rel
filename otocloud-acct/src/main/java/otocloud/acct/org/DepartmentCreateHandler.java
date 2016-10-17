/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org;

import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.acct.dao.DepartmentDAO;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;


public class DepartmentCreateHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String DEP_CREATE = "create";

	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public DepartmentCreateHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	/**
		{
		"dept_name":"lenovo",
		"dept_manager":"3911",
		"org_acct_id":100
		}
	 */
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		componentImpl.getLogger().info(body.toString());
		
		JsonObject department = body.getJsonObject("content");
		JsonObject sessionInfo = body.getJsonObject("session",null);		
			
		DepartmentDAO departmentDAO = new DepartmentDAO();
		departmentDAO.setDataSource(componentImpl.getSysDatasource());		
		
		departmentDAO.createDepartment(department, sessionInfo, 
		daoRet -> {

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
					department.put("id", id);

					msg.reply(department);

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
		return DEP_CREATE;
	}

}
