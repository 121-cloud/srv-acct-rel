/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.relation;


import java.util.ArrayList;
import java.util.List;

import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerRegistry;

public class AcctRelationComponent extends OtoCloudComponentImpl {
	
	@Override
	public String getName() {
		return "acct-relation";
	}

	@Override
	public List<OtoCloudEventHandlerRegistry> registerEventHandlers() {
		
		List<OtoCloudEventHandlerRegistry> ret = new ArrayList<OtoCloudEventHandlerRegistry>();
		
		AcctRelationCreateHandler acctRelationCreateHandler = new AcctRelationCreateHandler(this);
		ret.add(acctRelationCreateHandler);
				
		return ret;
	}
	
	
}