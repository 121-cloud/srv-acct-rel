/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.bizunit;


import java.util.ArrayList;
import java.util.List;

import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerRegistry;

public class BizUnitComponent extends OtoCloudComponentImpl {
	
	@Override
	public String getName() {
		return "bizunit";
	}

	@Override
	public List<OtoCloudEventHandlerRegistry> registerEventHandlers() {
		
		List<OtoCloudEventHandlerRegistry> ret = new ArrayList<OtoCloudEventHandlerRegistry>();
		
		BizUnitCreateHandler bizUnitCreateHandler = new BizUnitCreateHandler(this);
		ret.add(bizUnitCreateHandler);
		
		BizUnitQueryHandler bizUnitQueryHandler = new BizUnitQueryHandler(this);
		ret.add(bizUnitQueryHandler);
		
		BizUnitModifyHandler bizUnitModifyHandler = new BizUnitModifyHandler(this);
		ret.add(bizUnitModifyHandler);
		
		BizUnitDeleteHandler bizUnitDeleteHandler = new BizUnitDeleteHandler(this);
		ret.add(bizUnitDeleteHandler);
		
		BizUnitQueryByOrgRoleHandler bizUnitQueryByOrgRoleHandler = new BizUnitQueryByOrgRoleHandler(this);
		ret.add(bizUnitQueryByOrgRoleHandler);
		
		return ret;
	}
	
	
}