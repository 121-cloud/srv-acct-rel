/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.dao;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.CompositeFutureImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

import java.util.ArrayList;
import java.util.List;

import otocloud.persistence.dao.JdbcDataSource;
import otocloud.persistence.dao.OperatorDAO;
import otocloud.persistence.dao.TransactionConnection;


public class AccountDAO extends OperatorDAO {
	
    public AccountDAO() {
        super();
    }
	
    public AccountDAO(JdbcDataSource dataSource) {
        super(dataSource);
    }

	public void registerAccount(TransactionConnection conn, JsonObject acctRegInfo, JsonObject sessionInfo, Handler<AsyncResult<JsonObject>> done) {
		SQLConnection realConn = conn.getConn();
		
		Long userId = Long.parseLong(sessionInfo.getString("user_id"));
		
	  Future<JsonObject> retFuture = Future.future();
	  retFuture.setHandler(done);
	  
	  String sql1 = "INSERT INTO acct(acct_code,acct_name,acct_type,status,entry_id,entry_datetime)VALUES(?,?,?,?,?,now())";
	  String sql2 = "INSERT INTO acct_org_info(id,industry_code,ownership_code,area_code,address,invitation_code,tel,email,website_url,description,entry_id,entry_datetime)VALUES(?,?,?,?,?,?,?,?,?,?,?,now())";
	  
	  //添加IT管理业务单元
	  String sql3 = "INSERT INTO acct_biz_unit(unit_code,unit_name,acct_id,org_role_id,entry_id,entry_datetime)VALUES('IT','IT部门',?,1,?,now())";
	  String sql4 = "INSERT INTO acct_biz_unit_post(post_code,post_name,d_org_role_id,acct_biz_unit_id,auth_role_id,acct_id,entry_id,entry_datetime)VALUES('IT001','IT管理员',1,?,1,?,?,now())";
	  

	  realConn.updateWithParams(sql1, 
		  	new JsonArray()
	  			  .add(acctRegInfo.getString("acct_code"))
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
    		    	    		    				  acctResult.put("d_org_role_id", 1L);    		    	    		    				  
    		    	    		    				  
    		    	    		    				  
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
    		    	    		    	    		    				  acctResult.put("auth_role_id", 1L); //IT管理员固定角色ID
    		    	    		    	    		    				  
    		    	    		    	    		    				  subscribeDefaultApps(realConn, accId, postRet, userId, subDeaultAppsRet->{
    		    	    		    	    		    					  if(subDeaultAppsRet.succeeded()){ 
    		    	    		    	    		    						  retFuture.complete(acctResult);
    		    	    		    	    		    					  }else{
    	    		    	    		    	    		    				  Throwable err = subDeaultAppsRet.cause();
    	    		    	    		    	    		    				  err.printStackTrace();
    	    		    	    		    	    		    				  retFuture.fail(err);
    	    		    	    		    	    		    			  }
    		    	    		    	    		    				  });    		    	    		    	    		    				  
    		    						    		    				  //retFuture.complete(acctResult);
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
	
	public void subscribeDefaultApps(SQLConnection realConn, Long acctId, Long adminPost, Long userId, Handler<AsyncResult<Void>> done) {
	  
	  Long appId = 10L;
	  
	  //默认订阅企业空间APP
	  String appSql = "INSERT INTO acct_app(acct_id,d_app_id,app_version_id,d_app_version,status,entry_id,entry_datetime)VALUES(?,10,10,'1.0.0-SNAPSHOT','A',?,now())";

	  //添加企业空间APP的业务活动，并分配给IT管理员岗位
	  List<JsonObject> app_activity_ids = new ArrayList<>();
	  app_activity_ids.add(new JsonObject().put("activity_id", 23L).put("activity_code", "my-app-subscribe"));
	  app_activity_ids.add(new JsonObject().put("activity_id", 24L).put("activity_code", "my-account-mgr"));
	  app_activity_ids.add(new JsonObject().put("activity_id", 25L).put("activity_code", "my-bizunit"));
	  app_activity_ids.add(new JsonObject().put("activity_id", 26L).put("activity_code", "my-bizunit-post"));
	  app_activity_ids.add(new JsonObject().put("activity_id", 27L).put("activity_code", "my-user"));

	  
	  Future<Void> retFuture = Future.future();
	  retFuture.setHandler(done);


	  realConn.updateWithParams(appSql, 
		  	new JsonArray()
	  			  .add(acctId)
				  .add(userId),  
	  ret -> {		
		  if(ret.succeeded()){
			  UpdateResult updateRet = ret.result();			  
			  Long acct_app_id = updateRet.getKeys().getLong(0);		
			  
			  List<Future> futures = new ArrayList<Future>();
			  
			  app_activity_ids.forEach(app_activity->{	
				  
				  	Long app_activity_id = app_activity.getLong("activity_id");
				  	String app_activity_code = app_activity.getString("activity_code");
				  
				  
					Future<Void> itemFuture = Future.future();
					futures.add(itemFuture);
					
					  String activitySql = "INSERT INTO acct_app_activity(acct_app_id,d_app_id,app_activity_id,acct_id,entry_id,entry_datetime)VALUES(?,?,?,?,?,now())";
					  realConn.updateWithParams(activitySql, 
						  	new JsonArray()
						  		.add(acct_app_id)
						  		.add(appId)
						  		.add(app_activity_id)
						  		.add(acctId)
						  		.add(userId),
	    		    		  ret2 -> {		
	    		    			  if(ret2.succeeded()){
	    		    				  UpdateResult updateRet2 = ret2.result();			  
	    		    				  Long acct_app_activity_id = updateRet2.getKeys().getLong(0);		
	    		    				  
	    		  					  String assignPostSql = "INSERT INTO acct_biz_unit_post_activity(acct_biz_unit_post_id,acct_app_activity_id,acct_id,d_app_id,d_app_activity_id,d_app_activity_code,entry_id,entry_datetime)VALUES(?,?,?,?,?,?,?,now())";
	    							  realConn.updateWithParams(assignPostSql, 
	    								  	new JsonArray()
	    							  			.add(adminPost)
	    							  			.add(acct_app_activity_id)
	    							  			.add(acctId)
	    							  			.add(appId)	    								  		    								  		
	    								  		.add(app_activity_id)
	    								  		.add(app_activity_code)
	    								  		.add(userId),
	    			    		    		  ret3 -> {		
	    			    		    			  if(ret3.succeeded()){    		    				  
	    			    		    				  itemFuture.complete();				    				  
	    						    			  }else{
	    						    				  Throwable err = ret3.cause();
	    						    				  err.printStackTrace();
	    						    				  itemFuture.fail(err);
	    						    			  }
	    			    		    		  });    				  
				    			  }else{
				    				  Throwable err = ret2.cause();
				    				  err.printStackTrace();
				    				  itemFuture.fail(err);
				    			  }
	    		    		  });

				  
				  
			  });
			  
				CompositeFuture.join(futures).setHandler(ar -> { // 合并所有for循环结果，返回外面					
					CompositeFutureImpl comFutures = (CompositeFutureImpl)ar;
					if(comFutures.size() > 0){										
						for(int i=0;i<comFutures.size();i++){
							if(comFutures.succeeded(i)){								
							}else{
								  Throwable err = comFutures.cause();
								  err.printStackTrace();
								  retFuture.fail(err);
								  return;
							}
						}
						retFuture.complete();
					}else{
						retFuture.complete();
					}

				}); 
			  
			  

		  }else{
			  Throwable err = ret.cause();
			  err.printStackTrace();
			  retFuture.fail(err);
		  }
	  }); 
	}
	
	public void modifyAccount(TransactionConnection conn, Long acctId, JsonObject acct, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		
		  Future<UpdateResult> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
		  JsonObject whereObj = new JsonObject().put("id", acctId);
		  
		  Long userId = Long.parseLong(sessionInfo.getString("user_id"));
		  
		  this.updateBy(conn, "acct", acct, whereObj, userId, retFuture);
	  
	}

	
	public void modifyAccountInfo(TransactionConnection conn, Long acctId, JsonObject acctInfo, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		
		  Future<UpdateResult> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
		  JsonObject whereObj = new JsonObject().put("id", acctId);
		  
		  Long userId = Long.parseLong(sessionInfo.getString("user_id"));
		  
		  this.updateBy(conn, "acct_org_info", acctInfo, whereObj, userId, retFuture);
	  
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
	
	
   public void getAccountListByPage(JsonObject pagingOptions, Future<ResultSet> future) {
       
    	String sortField = pagingOptions.getString("sort_field");
    	Integer sortDirection = pagingOptions.getInteger("sort_direction");
    	String sortStr = (sortDirection==1)?"ASC":"DESC";    
    	
        int pageNo = pagingOptions.getInteger("page_number");
        int pageSize = pagingOptions.getInteger("page_size");
        int startIndex = (pageNo-1) * pageSize;
       
	   final String sql = "SELECT * FROM view_acct WHERE status='A' order by " +
			   sortField + " " + sortStr + " limit ?,?";
	   JsonArray params = new JsonArray();
	   params.add(startIndex);
	   params.add(pageSize);
	
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
	

   public void countAccount(Future<Integer> future) {
       
       final String sql = "SELECT count(id) as total_num FROM view_acct WHERE status='A'";
       JsonArray params = new JsonArray();

       Future<ResultSet> innerFuture = Future.future();

       queryWithParams(sql, params, innerFuture);

       innerFuture.setHandler(result -> {
           if (result.succeeded()) {
           	ResultSet resultSet = result.result();
           	List<JsonObject> retDataArrays = resultSet.getRows();
           	Integer totalNum = retDataArrays.get(0).getInteger("total_num");
           	future.complete(totalNum);
           } else {
           	Throwable err = result.cause();								
               future.fail(err);                
           }
       });

	}
   		

}
