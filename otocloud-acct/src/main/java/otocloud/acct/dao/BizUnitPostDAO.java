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


public class BizUnitPostDAO extends OperatorDAO{
	
    public BizUnitPostDAO(JdbcDataSource dataSource) {
        super(dataSource);
    }
	
    public void getList(Long bizUnitId, Future<ResultSet> future) {
        
	   final String sql = "SELECT * FROM acct_biz_unit_post where acct_biz_unit_id=?";
	   JsonArray params = new JsonArray();
	   params.add(bizUnitId);
	
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
		  
	  String sql = "INSERT INTO acct_biz_unit_post(post_code,post_name,d_org_role_id,acct_biz_unit_id,auth_role_id,acct_id,entry_id,entry_datetime)VALUES(?,?,?,?,?,?,?,now())"; 
	  
	  this.updateWithParams(sql, 
		  	new JsonArray()
				  .add(department.getString("post_code"))
				  .add(department.getString("post_name"))	
				  .add(department.getLong("d_org_role_id"))
				  .add(department.getLong("acct_biz_unit_id"))
				  .add(department.getLong("auth_role_id"))
				  .add(department.getLong("acct_id"))				  
				  .add(userId),  
				  retFuture);	  

	}

	
	public void modify(Long id, JsonObject department, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		 
	  Future<UpdateResult> retFuture = Future.future();
	  retFuture.setHandler(done);
	  
	  JsonObject whereObj = new JsonObject()
	  		.put("id", id);
	  
	  Long userId = Long.parseLong(sessionInfo.getString("user_id"));
	  
	  this.updateBy("acct_biz_unit_post", department, whereObj, userId, retFuture);
	}

	public void delete(Long id, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		
		  Future<UpdateResult> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
/*		  String sql = "DELETE FROM org_dept WHERE id=?";

		  this.deleteWithParams(sql,  
				  	new JsonArray()
						  .add(depId), retFuture);	  */
		  
		  Long userId = Long.parseLong(sessionInfo.getString("user_id"));
		  
		  String sql = "UPDATE acct_biz_unit_post SET delete_id=?,delete_datetime=now() WHERE id=?";

		  this.updateWithParams(sql, 
				  	new JsonArray()
						  .add(userId)
						  .add(id),  
						  retFuture);	  
	}
	

}
