/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.dao;

import otocloud.persistence.dao.JdbcDataSource;
import otocloud.persistence.dao.OperatorDAO;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;


public class BizUnitDAO extends OperatorDAO{
	
    public BizUnitDAO(JdbcDataSource dataSource) {
        super(dataSource);
    }
	
    public void getBizUnitList(Long acctId, Future<ResultSet> future) {
        
	   final String sql = "SELECT * FROM view_acct_biz_unit where acct_id=?";
	   JsonArray params = new JsonArray();
	   params.add(acctId);
	
	   Future<ResultSet> innerFuture = Future.future();
	
	   this.queryWithParams(sql, params, innerFuture);
	
	   innerFuture.setHandler(result -> {
	       if (result.succeeded()) {
		       	ResultSet resultSet = result.result();
		       	future.complete(resultSet);	
	       } else {
	       		Throwable err = result.cause();								
	            future.fail(err);                
	       }
	   });    	
    	
    }
	
	public void create(JsonObject department, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		  
	  Future<UpdateResult> retFuture = Future.future();
	  retFuture.setHandler(done);	 
	  
	  Long userId = Long.parseLong(sessionInfo.getString("user_id"));
		  
	  String sql = "INSERT INTO acct_biz_unit(unit_code,unit_name,unit_manager,org_role_id,acct_id,entry_id,entry_datetime)VALUES(?,?,?,?,?,?,now())"; 
	  
	  this.updateWithParams(sql, 
		  	new JsonArray()
				  .add(department.getString("unit_code"))
				  .add(department.getString("unit_name"))	
				  .add(department.getLong("unit_manager"))
				  .add(department.getLong("org_role_id"))
				  .add(department.getLong("acct_id"))
				  .add(userId),  
				  retFuture);	  

	}

	
	public void modify(Long depId, JsonObject department, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		 
	  Future<UpdateResult> retFuture = Future.future();
	  retFuture.setHandler(done);
	  
	  JsonObject whereObj = new JsonObject()
	  		.put("id", depId);
	  
	  Long userId = Long.parseLong(sessionInfo.getString("user_id"));
	  
	  this.updateBy("acct_biz_unit", department, whereObj, userId, retFuture);
	}

	public void delete(Long depId, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		
		  Future<UpdateResult> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
/*		  String sql = "DELETE FROM org_dept WHERE id=?";

		  this.deleteWithParams(sql,  
				  	new JsonArray()
						  .add(depId), retFuture);	  */
		  
		  Long userId = Long.parseLong(sessionInfo.getString("user_id"));
		  
		  String sql = "UPDATE acct_biz_unit SET delete_id=?,delete_datetime=now() WHERE id=?";

		  this.updateWithParams(sql, 
				  	new JsonArray()
						  .add(userId)
						  .add(depId),  
						  retFuture);	  
	}
	

}
