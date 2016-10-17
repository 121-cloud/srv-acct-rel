/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.dao;

import otocloud.persistence.dao.OperatorDAO;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;


public class AccountBizRoleDAO extends OperatorDAO {

	public void AddBizRole(Integer acctId, Integer bizRoleId, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		
	  Future<UpdateResult> retFuture = Future.future();
	  retFuture.setHandler(done);
	  
	  String sql = "INSERT INTO org_acct_biz_role(org_acct_id,biz_role_id,entry_id,entry_datetime)VALUES(?,?,?,now())"; 
	  
	  this.updateWithParams(sql, 
		  	new JsonArray()				  
				  .add(acctId)	
				  .add(bizRoleId)
				  .add(sessionInfo.getInteger("userId")),  
				  retFuture);	  
	}

		

}
