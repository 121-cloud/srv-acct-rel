/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.rel.Management;


import java.util.ArrayList;
import java.util.List;

import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerRegistry;


public class AccountBizRelationManagement extends OtoCloudComponentImpl {
	
	@Override
	public String getName() {
		return "account_rel_mg";
	}

	@Override
	public List<OtoCloudEventHandlerRegistry> registerEventHandlers() {
		
		List<OtoCloudEventHandlerRegistry> ret = new ArrayList<OtoCloudEventHandlerRegistry>();
			
		AccountPartnerGetHandler accountPartnerGetHandler = new AccountPartnerGetHandler(this);
		ret.add(accountPartnerGetHandler);		
		
		return ret;
	}
}