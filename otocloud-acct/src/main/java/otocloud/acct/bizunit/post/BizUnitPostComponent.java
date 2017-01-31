/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.bizunit.post;


import java.util.ArrayList;
import java.util.List;

import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerRegistry;

public class BizUnitPostComponent extends OtoCloudComponentImpl {
	
	@Override
	public String getName() {
		return "bizunit-post";
	}

	@Override
	public List<OtoCloudEventHandlerRegistry> registerEventHandlers() {
		
		List<OtoCloudEventHandlerRegistry> ret = new ArrayList<OtoCloudEventHandlerRegistry>();
		
		BizUnitPostCreateHandler bizUnitCreateHandler = new BizUnitPostCreateHandler(this);
		ret.add(bizUnitCreateHandler);
		
		BizUnitPostQueryHandler bizUnitQueryHandler = new BizUnitPostQueryHandler(this);
		ret.add(bizUnitQueryHandler);
		
		BizUnitPostModifyHandler bizUnitModifyHandler = new BizUnitPostModifyHandler(this);
		ret.add(bizUnitModifyHandler);
		
		BizUnitPostDeleteHandler bizUnitDeleteHandler = new BizUnitPostDeleteHandler(this);
		ret.add(bizUnitDeleteHandler);
		
		return ret;
	}
	
	
}