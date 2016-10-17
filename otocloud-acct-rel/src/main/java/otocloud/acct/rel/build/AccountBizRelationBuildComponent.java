/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.rel.build;


import java.util.ArrayList;
import java.util.List;

import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerRegistry;


public class AccountBizRelationBuildComponent extends OtoCloudComponentImpl {
	
	@Override
	public String getName() {
		return "biz-rel-builder";
	}

	@Override
	public List<OtoCloudEventHandlerRegistry> registerEventHandlers() {
		
		List<OtoCloudEventHandlerRegistry> ret = new ArrayList<OtoCloudEventHandlerRegistry>();
			
		AccountBizRelationBuildHandler accountBizRelationBuildHandler = new AccountBizRelationBuildHandler(this);
		ret.add(accountBizRelationBuildHandler);		
		
		return ret;
	}
}