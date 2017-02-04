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
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;


public class AccountDAO extends OperatorDAO {

	public void registerAccount(TransactionConnection conn, JsonObject acctRegInfo, JsonObject sessionInfo, Handler<AsyncResult<JsonObject>> done) {
		SQLConnection realConn = conn.getConn();
		
		Long userId = Long.parseLong(sessionInfo.getString("user_id"));
		
	  Future<JsonObject> retFuture = Future.future();
	  retFuture.setHandler(done);
	  
	  String sql1 = "INSERT INTO acct(acct_name,acct_type,status,entry_id,entry_datetime)VALUES(?,?,?,?,now())";
	  String sql2 = "INSERT INTO acct_org_info(id,industry_code,ownership_code,area_code,address,invitation_code,tel,email,website_url,description,entry_id,entry_datetime)VALUES(?,?,?,?,?,?,?,?,?,?,?,now())";
	  String sql3 = "INSERT INTO acct_biz_unit(unit_code,unit_name,acct_id,org_role_id,entry_id,entry_datetime)VALUES('IT','IT部门',?,1,?,now())";
	  String sql4 = "INSERT INTO acct_biz_unit_post(post_code,post_name,d_org_role_id,acct_biz_unit_id,auth_role_id,is_manager,acct_id,entry_id,entry_datetime)VALUES('IT001','IT管理员',1,?,1,1,?,?,now())";

	  realConn.updateWithParams(sql1, 
		  	new JsonArray()
				  .add(acctRegInfo.getString("acct_name"))
				  .add("PARTNER")
				  .add("A")
				  .add(userId),  
	  ret -> {		
		  if(ret.succeeded()){
			  UpdateResult updateRet = ret.result();
			  JsonObject acctResult = new JsonObject(); 
			  Long accId = updateRet.getKeys().getLong(0);	
			  acctResult.put("acct_id", accId);
			  
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
    				  				.add(userId),

    		    		  ret2 -> {		
    		    			  if(ret2.succeeded()){    		    				  
    		    				  realConn.updateWithParams(sql3, 
    		    						  	new JsonArray()
    		    						  		.add(accId)
    		    						  		.add(userId),
    		    	    		    		  ret3 -> {		
    		    	    		    			  if(ret3.succeeded()){ 
    		    	    		    				  UpdateResult bizUnitRet = ret3.result();    		    	    		    				  
    		    	    		    				  Long bizUnitId = bizUnitRet.getKeys().getLong(0);
    		    	    		    				  acctResult.put("biz_unit_id", bizUnitId);
    		    	    		    				  
    		    	    		    				  realConn.updateWithParams(sql4, 
    		    	    		    						  	new JsonArray()
    		    	    		    				  				.add(bizUnitId)
    		    	    		    						  		.add(accId)
    		    	    		    						  		.add(userId),
    		    	    		    	    		    		  ret4 -> {		
    		    	    		    	    		    			  if(ret4.succeeded()){ 
    		    	    		    	    		    				  UpdateResult bizUnitPostRet = ret4.result();    		    	    		    				  
    		    	    		    	    		    				  Long postRet = bizUnitPostRet.getKeys().getLong(0);
    		    	    		    	    		    				  acctResult.put("mgr_post_id", postRet);
    		    	    		    	    		    				  
    		    						    		    				  retFuture.complete(acctResult);
    		    	    		    	    		    			  }else{
    		    	    		    	    		    				  Throwable err = ret4.cause();
    		    	    		    	    		    				  err.printStackTrace();
    		    	    		    	    		    				  retFuture.fail(err);
    		    	    		    	    		    			  }
    		    	    		    	    		    		  });
					    		    				  
    		    	    		    			  }else{
    		    	    		    				  Throwable err = ret3.cause();
    		    	    		    				  err.printStackTrace();
    		    	    		    				  retFuture.fail(err);
    		    	    		    			  }
    		    	    		    		  });
	    				  
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

	
	public void modifyAccountInfo(Long acctId, JsonObject acctInfo, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		
		  Future<UpdateResult> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
		  JsonObject whereObj = new JsonObject().put("id", acctId);
		  
		  Long userId = Long.parseLong(sessionInfo.getString("user_id"));
		  
		  this.updateBy("acct_org_info", acctInfo, whereObj, userId, retFuture);
	  
	}
	
/*	public void EnableERPUserBindingHandler(Integer acctId, Boolean isEnabled, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		
		  Future<UpdateResult> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
		  JsonObject whereObj = new JsonObject().put("id", acctId);
		  
		  this.updateBy("acct", new JsonObject().put("enable_erp_user", isEnabled ? 1 : 0), whereObj, sessionInfo.getLong("user_id", 0L), retFuture);
		  
	}*/
	
/*	public void GetERPUserBindSettingHandler(Integer acctId, JsonObject sessionInfo, Handler<AsyncResult<ResultSet>> done) {
		
		  Future<ResultSet> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
		  JsonObject whereObj = new JsonObject().put("id", acctId);
		  
		  String[] columns = new String[]{"enable_erp_user"};
		  
		  this.queryBy("acct", columns, whereObj, retFuture);
		  
	}*/

	public void unregisterAccount(Long accId, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		
		  Future<UpdateResult> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
		  Long userId = Long.parseLong(sessionInfo.getString("user_id"));
		  
		  String sql = "UPDATE acct SET status='D',update_id=?,update_datetime=now() WHERE id=?";

		  this.updateWithParams(sql, 
				  	new JsonArray()
						  .add(userId)
						  .add(accId),  
						  retFuture);	  
	}
	
		

}
