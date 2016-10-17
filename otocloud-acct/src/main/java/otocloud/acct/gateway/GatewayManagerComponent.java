/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.gateway;


import java.util.ArrayList;
import java.util.List;

import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerRegistry;


public class GatewayManagerComponent extends OtoCloudComponentImpl {
	
	@Override
	public String getName() {
		return "gateway";
	}

	@Override
	public List<OtoCloudEventHandlerRegistry> registerEventHandlers() {
		
		List<OtoCloudEventHandlerRegistry> ret = new ArrayList<OtoCloudEventHandlerRegistry>();
		
		GatewayStatusHandler gatewayStatusHandler = new GatewayStatusHandler(this);
		ret.add(gatewayStatusHandler);
		
		GatewayConfigQueryHandler gatewayConfigQueryHandler = new GatewayConfigQueryHandler(this);
		ret.add(gatewayConfigQueryHandler);
		
		GatewayConfigSettingHandler gatewayConfigSettingHandler = new GatewayConfigSettingHandler(this);
		ret.add(gatewayConfigSettingHandler);
		
		GatewayPackageBuildHandler gatewayPackageBuildHandler = new GatewayPackageBuildHandler(this);
		ret.add(gatewayPackageBuildHandler);
		
		return ret;
	}
}