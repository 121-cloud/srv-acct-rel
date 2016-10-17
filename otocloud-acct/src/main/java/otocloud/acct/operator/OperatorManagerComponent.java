/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.operator;


import java.util.ArrayList;
import java.util.List;

import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerRegistry;

/**
 * 
 * @author hugw
 *
 */
public class OperatorManagerComponent extends OtoCloudComponentImpl {
	
	@Override
	public String getName() {
		return "operator";
	}

	@Override
	public List<OtoCloudEventHandlerRegistry> registerEventHandlers() {
		
		List<OtoCloudEventHandlerRegistry> ret = new ArrayList<OtoCloudEventHandlerRegistry>();
		
		OperatorCreateHandler operatorCreateHandler = new OperatorCreateHandler(this);
		ret.add(operatorCreateHandler);
		
		OperatorModifyHandler operatorModifyHandler = new OperatorModifyHandler(this);
		ret.add(operatorModifyHandler);
		
		return ret;
	}
}