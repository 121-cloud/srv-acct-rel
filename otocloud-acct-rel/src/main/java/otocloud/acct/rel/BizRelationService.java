package otocloud.acct.rel;

import java.util.ArrayList;
import java.util.List;

import otocloud.acct.rel.Management.AccountBizRelationManagement;
import otocloud.acct.rel.build.AccountBizRelationBuildComponent;
import otocloud.framework.core.OtoCloudComponent;
import otocloud.framework.core.OtoCloudServiceForVerticleImpl;


/**
 * Hello world!
 *
 */
public class BizRelationService extends OtoCloudServiceForVerticleImpl {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<OtoCloudComponent> createServiceComponents() {
		
		List<OtoCloudComponent> components = new ArrayList<OtoCloudComponent>();
		
		components.add(new AccountBizRelationBuildComponent());	
		components.add(new AccountBizRelationManagement());
		
		return components;
	}  
    
	@Override
	public String getServiceName() {
		return "otocloud-acct-rel";
	}
	
}
