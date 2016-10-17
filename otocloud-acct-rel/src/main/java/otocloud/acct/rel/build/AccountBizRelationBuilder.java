/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.rel.build;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月16日
 * @author lijing@yonyou.com
 */
public interface AccountBizRelationBuilder {
	void createAccountRelation(Integer fromAppId, Integer fromRoleRelid, Boolean isReverse, Integer fromAccount, Integer toAccount, Integer toAppId,
			Handler<AsyncResult<Boolean>> retHandler);
}
