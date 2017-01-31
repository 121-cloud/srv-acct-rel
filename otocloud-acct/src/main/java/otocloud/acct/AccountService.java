/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct;

import java.util.ArrayList;
import java.util.List;

import otocloud.acct.baseinfo.AccountManagerComponent;
import otocloud.acct.bizunit.BizUnitComponent;
import otocloud.acct.bizunit.post.BizUnitPostComponent;
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
		
/*		GatewayManagerComponent gwManagerComponent = new GatewayManagerComponent();
		components.add(gwManagerComponent);*/
		
		BizUnitComponent bizUnitComponent = new BizUnitComponent();
		components.add(bizUnitComponent);
		
		BizUnitPostComponent bizUnitPostComponent = new BizUnitPostComponent();
		components.add(bizUnitPostComponent);
		
/*		OperatorManagerComponent operatorManagerComponent = new OperatorManagerComponent();
		components.add(operatorManagerComponent);*/
				
		return components;
	}  
    
	@Override
	public String getServiceName() {
		return "otocloud-acct";
	}

}