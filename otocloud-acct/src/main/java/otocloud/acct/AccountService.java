/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct;

import java.util.ArrayList;
import java.util.List;

import otocloud.acct.app.AcctAppComponent;
import otocloud.acct.baseinfo.AccountManagerComponent;
import otocloud.acct.bizunit.BizUnitComponent;
import otocloud.framework.core.OtoCloudComponent;
import otocloud.framework.core.OtoCloudServiceForVerticleImpl;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public class AccountService extends OtoCloudServiceForVerticleImpl {	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<OtoCloudComponent> createServiceComponents() {
		
		List<OtoCloudComponent> components = new ArrayList<OtoCloudComponent>();
		
		AccountManagerComponent component = new AccountManagerComponent();
		components.add(component);
		
		AcctAppComponent acctAppComponent = new AcctAppComponent();
		components.add(acctAppComponent);
		
		BizUnitComponent bizUnitComponent = new BizUnitComponent();
		components.add(bizUnitComponent);
				
		return components;
	}  
    
	@Override
	public String getServiceName() {
		return "otocloud-acct";
	}

}