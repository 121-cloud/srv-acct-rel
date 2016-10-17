/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.dao;

import otocloud.persistence.dao.OperatorDAO;
import otocloud.persistence.dao.TransactionConnection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;


public class AccountDAO extends OperatorDAO {

	public void registerAccount(TransactionConnection conn, JsonObject acctRegInfo, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		SQLConnection realConn = conn.getConn();
		
	  Future<UpdateResult> retFuture = Future.future();
	  retFuture.setHandler(done);
	  
	  String sql1 = "INSERT INTO org_acct(acct_name,acct_type,status,entry_id,entry_datetime)VALUES(?,?,?,?,now())";
	  String sql2 = "INSERT INTO org_partner(id,industry_code,ownership_code,area_code,address,invitation_code,tel,email,website_url,description,entry_id,entry_datetime)VALUES(?,?,?,?,?,?,?,?,?,?,?,now())";

	  realConn.updateWithParams(sql1, 
		  	new JsonArray()
				  .add(acctRegInfo.getString("acct_name"))
				  .add("PARTNER")
				  .add("A")
				  .add(sessionInfo.getInteger("userId", 0)),  
	  ret -> {		
		  if(ret.succeeded()){
			  UpdateResult updateRet = ret.result();
			  Integer accId = updateRet.getKeys().getInteger(0);	
			  
			  realConn.updateWithParams(sql2, 
					  	new JsonArray()
					  		.add(accId)
					  		.add(acctRegInfo.getString("industry_code",""))
			  				.add(acctRegInfo.getString("ownership_code",""))
			  				.add(acctRegInfo.getString("area_code",""))
			  				.add(acctRegInfo.getString("address",""))
			  				.add(acctRegInfo.getString("invitation_code",""))
			  				.add(acctRegInfo.getString("tel",""))
			  				.add(acctRegInfo.getString("email",""))
			  				.add(acctRegInfo.getString("website_url",""))
			  				.add(acctRegInfo.getString("description",""))
    				  				.add(1),

    		    		  ret2 -> {		
    		    			  if(ret2.succeeded()){   		    				  
  				  
    		    				  retFuture.complete(updateRet);
	    				  
	    			  }else{
	    				  Throwable err = ret2.cause();
	    				  err.printStackTrace();
	    				  retFuture.fail(err);
	    			  }
	    		  });
		  }else{
			  Throwable err = ret.cause();
			  err.printStackTrace();
			  retFuture.fail(err);
		  }
	  }); 
	}

	
	public void modifyAccountInfo(Integer acctId, JsonObject acctInfo, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		
		  Future<UpdateResult> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
		  JsonObject whereObj = new JsonObject().put("id", acctId);
		  
		  this.updateBy("org_partner", acctInfo, whereObj, sessionInfo.getInteger("userId", 0), retFuture);
	  
	}
	
	public void EnableERPUserBindingHandler(Integer acctId, Boolean isEnabled, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		
		  Future<UpdateResult> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
		  JsonObject whereObj = new JsonObject().put("id", acctId);
		  
		  this.updateBy("org_acct", new JsonObject().put("enable_erp_user", isEnabled ? 1 : 0), whereObj, sessionInfo.getInteger("userId", 0), retFuture);
		  
	}
	
	public void GetERPUserBindSettingHandler(Integer acctId, JsonObject sessionInfo, Handler<AsyncResult<ResultSet>> done) {
		
		  Future<ResultSet> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
		  JsonObject whereObj = new JsonObject().put("id", acctId);
		  
		  String[] columns = new String[]{"enable_erp_user"};
		  
		  this.queryBy("org_acct", columns, whereObj, retFuture);
		  
	}

	public void unregisterAccount(Integer accId, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		
		  Future<UpdateResult> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
		  String sql = "UPDATE org_acct SET status='D',update_id=?,update_datetime=now() WHERE id=?";

		  this.updateWithParams(sql, 
				  	new JsonArray()
						  .add(sessionInfo.getInteger("userId"))
						  .add(accId),  
						  retFuture);	  
	}
	
		

}
