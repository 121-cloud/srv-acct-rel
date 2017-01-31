/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.baseinfo;


import java.util.ArrayList;
import java.util.List;

import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerRegistry;


public class AccountManagerComponent extends OtoCloudComponentImpl {
	
	@Override
	public String getName() {
		return "account";
	}

	@Override
	public List<OtoCloudEventHandlerRegistry> registerEventHandlers() {
		
		List<OtoCloudEventHandlerRegistry> ret = new ArrayList<OtoCloudEventHandlerRegistry>();
		
		AccountRegisterHandler accountRegisterHandler = new AccountRegisterHandler(this);
		ret.add(accountRegisterHandler);
		
		AccountUnregisterHandler accountUnregHandler = new AccountUnregisterHandler(this);
		ret.add(accountUnregHandler);
		
		AccountModifyHandler accountModifyHandler = new AccountModifyHandler(this);
		ret.add(accountModifyHandler);		
		
/*		EnableERPUserBindingHandler enableERPUserBindingHandler = new EnableERPUserBindingHandler(this);
		ret.add(enableERPUserBindingHandler);
		
		GetERPUserBindingStateHandler getERPUserBindingStateHandler = new GetERPUserBindingStateHandler(this);
		ret.add(getERPUserBindingStateHandler);*/
		
		return ret;
	}
	
}