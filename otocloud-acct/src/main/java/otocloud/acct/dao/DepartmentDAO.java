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
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;


public class DepartmentDAO extends OperatorDAO{
	
	public void createDepartment(JsonObject department, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		  
	  Future<UpdateResult> retFuture = Future.future();
	  retFuture.setHandler(done);	  
		  
	  String sql = "INSERT INTO org_dept(dept_name,dept_manager,org_acct_id,entry_id,entry_datetime)VALUES(?,?,?,?,now())"; 
	  
	  this.updateWithParams(sql, 
		  	new JsonArray()
				  .add(department.getString("dept_name"))
				  .add(department.getString("dept_manager"))	
				  .add(department.getInteger("org_acct_id"))
				  .add(sessionInfo.getInteger("userId")),  
				  retFuture);	  

	}

	public void queryDepartments(Integer accId, Handler<AsyncResult<ResultSet>> done) {
		  
		  Future<ResultSet> retFuture = Future.future();
		  retFuture.setHandler(done);
			  
		  String sql = "SELECT * FROM org_dept WHERE delete_id is null and org_acct_id=?";

		  this.queryWithParams(sql, 
			  	new JsonArray()
					  .add(accId),  
					  retFuture);	  

	}
	
	public void modifyDepartment(Integer depId, JsonObject department, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		 
	  Future<UpdateResult> retFuture = Future.future();
	  retFuture.setHandler(done);
	  
	  JsonObject whereObj = new JsonObject()
	  		.put("id", depId);
	  
	  this.updateBy("org_dept", department, whereObj, sessionInfo.getInteger("userId", 0), retFuture);
	}

	public void deleteDepartment(Integer depId, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		
		  Future<UpdateResult> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
/*		  String sql = "DELETE FROM org_dept WHERE id=?";

		  this.deleteWithParams(sql,  
				  	new JsonArray()
						  .add(depId), retFuture);	  */
		  
		  String sql = "UPDATE org_dept SET delete_id=?,delete_datetime=now() WHERE id=?";

		  this.updateWithParams(sql, 
				  	new JsonArray()
						  .add(sessionInfo.getInteger("userId"))
						  .add(depId),  
						  retFuture);	  
	}
	

}
