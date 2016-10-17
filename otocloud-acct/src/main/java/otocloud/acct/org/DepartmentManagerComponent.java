/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org;


import java.util.ArrayList;
import java.util.List;

import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerRegistry;

public class DepartmentManagerComponent extends OtoCloudComponentImpl {
	
	@Override
	public String getName() {
		return "department";
	}

	@Override
	public List<OtoCloudEventHandlerRegistry> registerEventHandlers() {
		
		List<OtoCloudEventHandlerRegistry> ret = new ArrayList<OtoCloudEventHandlerRegistry>();
		
		DepartmentCreateHandler departmentCreateHandler = new DepartmentCreateHandler(this);
		ret.add(departmentCreateHandler);
		
		DepartmentQueryHandler departmentQueryHandler = new DepartmentQueryHandler(this);
		ret.add(departmentQueryHandler);
		
		DepartmentModifyHandler departmentModifyHandler = new DepartmentModifyHandler(this);
		ret.add(departmentModifyHandler);
		
		DepartmentDeleteHandler departmentDeleteHandler = new DepartmentDeleteHandler(this);
		ret.add(departmentDeleteHandler);
		
		return ret;
	}
	
	
}