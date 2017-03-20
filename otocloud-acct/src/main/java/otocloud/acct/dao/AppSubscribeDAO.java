/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import otocloud.persistence.dao.JdbcDataSource;
import otocloud.persistence.dao.OperatorDAO;
import otocloud.persistence.dao.TransactionConnection;
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

public class AppSubscribeDAO extends OperatorDAO {	
	
    public AppSubscribeDAO(JdbcDataSource dataSource) {
        super(dataSource);
    }

	/**
	[
		{
			"acct_id":
			"d_app_id":
			"app_version_id":
			"d_app_version":
			"is_platform": false/true
			"app_inst_group": 
			"activities":[
				{
					"app_activity_id":
				}
			]
		}
	]
	*/
	public void subscribeApp(TransactionConnection conn,
			JsonArray subscribeInfos, JsonObject sessionInfo,
			Handler<AsyncResult<Void>> done) {
		
		SQLConnection realConn = conn.getConn();
		
		Future<Void> retFuture = Future.future();
		retFuture.setHandler(done);
		
		List<Future> futures = new ArrayList<Future>();
	  
		subscribeInfos.forEach(item->{	
			JsonObject subscribeInfo = (JsonObject)item;
		  
			Future<Void> itemFuture = Future.future();
			futures.add(itemFuture);		

			Long userId = Long.parseLong(sessionInfo.getString("user_id"));
			Long acctId = subscribeInfo.getLong("acct_id");
			Long appId = subscribeInfo.getLong("d_app_id");
			Long appVerId = subscribeInfo.getLong("app_version_id");
			String d_app_version = subscribeInfo.getString("d_app_version");
			String appInst = subscribeInfo.getString("app_inst_group", "");
			Boolean is_platform = subscribeInfo.getBoolean("is_platform", true);

			
			Map<Long, Long> activityMap = new HashMap<Long, Long>();
			
			String sql0  = "select id from acct_app where acct_id=? and d_app_id=?";
	
			String sql1 = "INSERT INTO acct_app(acct_id,d_app_id,app_version_id,d_app_version,app_inst_group,status,entry_id,entry_datetime)VALUES(?,?,?,?,?,?,?,now())";
			String sql2 = "INSERT INTO acct_app_activity(acct_app_id,d_app_id,app_activity_id,acct_id,entry_id,entry_datetime)VALUES(?,?,?,?,?,now())";

			  realConn.queryWithParams(sql0, 
					  	new JsonArray()
							  .add(acctId)
							  .add(appId),
				  existAppRet -> {		
					  if(existAppRet.succeeded()){
						  
						  ResultSet appRet = existAppRet.result();
						  List<JsonObject> rows = appRet.getRows();
						  if(rows != null && rows.size() > 0){
							  Long id = rows.get(0).getLong("id");	
							  subscribeInfo.put("id", id);
							  
							  JsonArray activities = subscribeInfo.getJsonArray("activities", null);
							  if(activities == null || activities.size() == 0){
								  itemFuture.complete();
							  }else{					  
								  Future<Void> depFuture = Future.future();
								  
								  subscribeActivity(conn, activityMap, userId, acctId, appId, id, sql2, activities, activities.size(), 0, depFuture);
								  
								  depFuture.setHandler(subscribeActivityRet->{
									  if(subscribeActivityRet.succeeded()){
										  itemFuture.complete();
									  }else{
										  Throwable err = subscribeActivityRet.cause();
										  err.printStackTrace();
										  itemFuture.fail(err);
									  }
								  });	
							  }
							  
						  }else{						  

								String status = "A";
								if(appInst == null || appInst.isEmpty()){
									if(!is_platform){
										status = "U";
									}
								}			
						  
								  realConn.updateWithParams(sql1, 
										  	new JsonArray()
												  .add(acctId)
												  .add(appId)
												  .add(appVerId)
												  .add(d_app_version)
												  .add(appInst)
												  .add(status)
												  .add(userId),  
									  ret -> {		
										  if(ret.succeeded()){
											  UpdateResult updateRet = ret.result();
											  //JsonObject acctResult = new JsonObject(); 
											  Long id = updateRet.getKeys().getLong(0);	
											  subscribeInfo.put("id", id);
											  
											  JsonArray activities = subscribeInfo.getJsonArray("activities", null);
											  if(activities == null || activities.size() == 0){
												  itemFuture.complete();
											  }else{					  
												  Future<Void> depFuture = Future.future();
												  
												  subscribeActivity(conn, activityMap, userId, acctId, appId, id, sql2, activities, activities.size(), 0, depFuture);
												  
												  depFuture.setHandler(subscribeActivityRet->{
													  if(subscribeActivityRet.succeeded()){
														  itemFuture.complete();
													  }else{
														  Throwable err = subscribeActivityRet.cause();
														  err.printStackTrace();
														  itemFuture.fail(err);
													  }
												  });	
											  }
						
										  }else{
											  Throwable err = ret.cause();
											  err.printStackTrace();
											  itemFuture.fail(err);
										  }
									  }); 
						  }	  
						  
						  
					  }else{
						  Throwable err = existAppRet.cause();
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
						  retFuture.fail(err);
						  return;
					}
				}
				retFuture.complete();
			}else{
				retFuture.complete();
			}

		}); 
	  		
		
		
	}
	
	//INSERT INTO acct_app_activity(acct_app_id,d_app_id,app_activity_id,acct_id,entry_id,entry_datetime)VALUES(?,?,?,?,?,now())	
	private void subscribeActivity(TransactionConnection conn, Map<Long, Long> activityMap, Long userId, Long acctId, Long appId, Long acct_app_id, String sql, JsonArray activities, Integer size, int index, Future<Void> depFuture) {
		JsonObject activity = activities.getJsonObject(index);
		Long activityId = activity.getLong("app_activity_id",0L);
		
		SQLConnection realConn = conn.getConn();
		  realConn.updateWithParams(sql, 
				  	new JsonArray()
				  		.add(acct_app_id)
				  		.add(appId)
		  				.add(activityId)
		  				.add(acctId)
				  		.add(userId),
		    		  ret2 -> {		
		    			  if(ret2.succeeded()){    	
		    				  Long id = ret2.result().getKeys().getLong(0);
		    				  activity.put("id", id);	
		    				  activityMap.put(activityId, id);
		    				  
		  						Integer nextIdx = index + 1;
		  						if (nextIdx < size)
		  							subscribeActivity(conn, activityMap, userId, acctId, appId, acct_app_id, sql, activities, size, nextIdx, depFuture);
		  						else if (nextIdx >= size) {
		  							depFuture.complete();
		  						}
		  						
		    			  }else{
		    				  Throwable err = ret2.cause();
		    				  err.printStackTrace();	
		    				  
		    				  depFuture.fail(err);
		    			  }
		    			  
		    		  });
		
	}
	
	
	
	public void addBizUnitAndPos(Long acctId, Long userId, Long appId, Map<Long, Long> activityMap, JsonArray bizUnitInfos, 
			Handler<AsyncResult<Void>> done) {

		Future<Void> retFuture = Future.future();
		retFuture.setHandler(done);		
		  
       createDBConnect(conn -> conn.setAutoCommit(true, res -> {
            if (res.failed()) {
            	retFuture.fail(res.cause());
                return;	            
            }

            bizUnitInfos.forEach(item->{
				  JsonObject bizUnitInfo = (JsonObject)item;		
				  addBizUnitAndPos(conn, acctId, userId, appId, activityMap, bizUnitInfo, 
							bizUnitRet->{								
							});

			  });		
			  
			  retFuture.complete();

        }), e ->{
        	retFuture.fail(e);
        	logger.error("连接数据库错误.", e);
        });		  
		
	}
	
	
	/**
		{
			"org_role_id": 
			"unit_code":
			"unit_name":
			"acct_biz_unit_post":[
				{
					auth_role_id：
					post_code:
					post_name:
					acct_biz_unit_post_activity:[
						{
							d_app_activity_id:
							d_app_activity_code:							
						}						
					]					
				
				}				
			]
		}
	*/
	private void addBizUnitAndPos(SQLConnection conn, Long acctId, Long userId, Long appId, Map<Long, Long> activityMap, JsonObject bizUnitInfo, 
			Handler<AsyncResult<Void>> done) {

		Future<Void> retFuture = Future.future();
		retFuture.setHandler(done);
		
		Long org_role_id = bizUnitInfo.getLong("org_role_id");

	  String sql3 = "INSERT INTO acct_biz_unit(unit_code,unit_name,acct_id,org_role_id,entry_id,entry_datetime)VALUES(?,?,?,?,?,now())";
	  String sql4 = "INSERT INTO acct_biz_unit_post(post_code,post_name,d_org_role_id,acct_biz_unit_id,auth_role_id,acct_id,entry_id,entry_datetime)VALUES(?,?,?,?,?,?,?,now())";
	  String sql5 = "INSERT INTO acct_biz_unit_post_activity(acct_biz_unit_post_id,acct_app_activity_id,d_app_id,d_app_activity_id,d_app_activity_code,acct_id,entry_id,entry_datetime)VALUES(?,?,?,?,?,?,?,now())";

        conn.updateWithParams(sql3, 
			  	new JsonArray()
					  .add(bizUnitInfo.getString("unit_code"))
					  .add(bizUnitInfo.getString("unit_name"))
					  .add(acctId)
					  .add(org_role_id)
					  .add(userId),  
		  ret -> {		
			  if(ret.succeeded()){
				  UpdateResult updateRet = ret.result();
				  //JsonObject acctResult = new JsonObject(); 
				  Long bizUnitId = updateRet.getKeys().getLong(0);	
				  bizUnitInfo.put("id", bizUnitId);
				  
				  JsonArray acct_biz_unit_posts = bizUnitInfo.getJsonArray("acct_biz_unit_post", null);
				  if(acct_biz_unit_posts == null || acct_biz_unit_posts.size() == 0){
					  retFuture.complete();
				  }else{	
					  acct_biz_unit_posts.forEach(item->{
						  JsonObject acct_biz_unit_post = (JsonObject)item;		
						  
				            conn.updateWithParams(sql4, 
								  	new JsonArray()
										  .add(acct_biz_unit_post.getString("post_code"))
										  .add(acct_biz_unit_post.getString("post_name"))
										  .add(org_role_id)
										  .add(bizUnitId)
										  .add(acct_biz_unit_post.getLong("auth_role_id"))
										  .add(acctId)												  
										  .add(userId),  
							  postRet -> {		
								  if(postRet.succeeded()){
									  UpdateResult postUpdateRet = postRet.result();
									  //JsonObject acctResult = new JsonObject(); 
									  Long postId = postUpdateRet.getKeys().getLong(0);	
									  acct_biz_unit_post.put("id", postId);
									  
									  JsonArray acct_biz_unit_post_activities = acct_biz_unit_post.getJsonArray("acct_biz_unit_post_activity", null);
									  if(acct_biz_unit_post_activities == null || acct_biz_unit_post_activities.size() == 0){
										  
									  }else{	
										  acct_biz_unit_post_activities.forEach(item2->{
											  JsonObject acct_biz_unit_post_activity = (JsonObject)item2;
											  Long app_activity_id = acct_biz_unit_post_activity.getLong("d_app_activity_id");
											  String app_activity_code = acct_biz_unit_post_activity.getString("d_app_activity_code");
											  
									            conn.updateWithParams(sql5, 
													  	new JsonArray()
															  .add(postId)
															  .add(activityMap.get(app_activity_id))
															  .add(appId)
															  .add(app_activity_id)
															  .add(app_activity_code)
															  .add(acctId)												  
															  .add(userId), 
												  postActRet -> {		
													  if(postActRet.succeeded()){
														  UpdateResult postActUpdateRet = postActRet.result();
														  //JsonObject acctResult = new JsonObject(); 
														  Long postActId = postActUpdateRet.getKeys().getLong(0);	
														  acct_biz_unit_post_activity.put("id", postActId);
													  }else{
														  Throwable err = postActRet.cause();
														  err.printStackTrace();
													  }
												  });
											  
										  });					  

									  }

								  }else{
									  Throwable err = postRet.cause();
									  err.printStackTrace();											  
								  }
							  }); 
						  
					  });		
					  
					  retFuture.complete();
				  }

			  }else{
				  Throwable err = ret.cause();
				  err.printStackTrace();
				  retFuture.fail(err);
			  }
		  }); 

		
	}
	

    public void getAppListByAcct(Long acctId, Future<ResultSet> future) {
        
	   final String sql = "SELECT * FROM view_acct_app WHERE acct_id=?";
	   JsonArray params = new JsonArray();
	   params.add(acctId);
	
	   this.queryWithParams(sql, params, future);
    	
    }
    
    public void getActivityList(Long acct_app_id, Future<ResultSet> future) {
        
	   final String sql = "SELECT * FROM view_acct_activity WHERE acct_app_id=?";
	   JsonArray params = new JsonArray();
	   params.add(acct_app_id);
	
	   this.queryWithParams(sql, params, future);

    }
    
    
    public void permissionVerify(Long acctId, Long appId, Future<Boolean> future) {
        
        final String sql = "SELECT count(*) as num FROM acct_app WHERE acct_id=? AND d_app_id=? AND status='A'";
        JsonArray params = new JsonArray();
        params.add(acctId);
        params.add(appId);

        Future<ResultSet> innerFuture = Future.future();

        this.queryWithParams(sql, params, innerFuture);

        innerFuture.setHandler(result -> {
            if (result.succeeded()) {
            	ResultSet resultSet = result.result();
            	List<JsonObject> retDataArrays = resultSet.getRows();
            	if(retDataArrays != null && retDataArrays.size() > 0){
            		Long num = retDataArrays.get(0).getLong("num");
            		if(num > 0){
            			future.complete(true);
            		}else{
            			future.complete(false);
            		}
            	}else{
            		future.complete(false);
            	}

            } else {
            	Throwable err = result.cause();								
                future.fail(err);                
            }
        });

	}


	public void appUnSubscribe(TransactionConnection conn, Long acct_app_id, 
			Handler<AsyncResult<UpdateResult>> done) {
		
	    Future<UpdateResult> innerFuture = Future.future();
	    innerFuture.setHandler(done);
		
		SQLConnection realConn = conn.getConn();
		
		String sqlStatement1 = "delete from acct_biz_unit_post_activity where d_acct_app_id=?";
		
		String sqlStatement2 = "delete from acct_app_activity where acct_app_id=?";

		String sqlStatement3 = "delete from acct_app where id=?";

		
		JsonArray arg1 = new JsonArray();
		arg1.add(acct_app_id);
	
		realConn.updateWithParams(sqlStatement1, arg1, handler1->{			
			  if(handler1.succeeded()){    	
				  realConn.updateWithParams(sqlStatement2, arg1, handler2->{						
					  if(handler2.succeeded()){    	
						  realConn.updateWithParams(sqlStatement3, arg1, handler3->{						
							  if(handler3.succeeded()){								  
								  UpdateResult ret = handler3.result();								  
								  innerFuture.complete(ret);					
							  }else{
								  Throwable err = handler3.cause();
								  err.printStackTrace();							  
								  innerFuture.fail(err);
							  }							
						});	
					  }else{
						  Throwable err = handler2.cause();
						  err.printStackTrace();						  
						  innerFuture.fail(err);
					  }					
				});
			  }else{
				  Throwable err = handler1.cause();
				  err.printStackTrace();
				  innerFuture.fail(err);
			  }
			
		});

	}
	
	public void activityUnSubscribe(TransactionConnection conn, Long acct_app_activity_id, 
			Handler<AsyncResult<UpdateResult>> done) {
		
	    Future<UpdateResult> innerFuture = Future.future();
	    innerFuture.setHandler(done);

		
		SQLConnection realConn = conn.getConn();
		
		String sqlStatement1 = "delete from acct_biz_unit_post_activity where acct_app_activity_id=?";
		
		String sqlStatement2 = "delete from acct_app_activity where id=?";

		
		JsonArray arg1 = new JsonArray();
		arg1.add(acct_app_activity_id);
	
		realConn.updateWithParams(sqlStatement1, arg1, handler1->{			
			  if(handler1.succeeded()){    	
				  realConn.updateWithParams(sqlStatement2, arg1, handler2->{						
					  if(handler2.succeeded()){    	
						  UpdateResult ret = handler2.result();								  
						  innerFuture.complete(ret);
					  }else{
						  Throwable err = handler2.cause();
						  err.printStackTrace();						  
						  innerFuture.fail(err);
					  }					
				});
			  }else{
				  Throwable err = handler1.cause();
				  err.printStackTrace();
				  innerFuture.fail(err);
			  }
			
		});


	}
	
    public void getNewAppsForAcct(Long acct_id, Future<ResultSet> future) {
        
	   String sql = "SELECT view_app_version.* FROM view_app_version inner join "
	   		+ "(SELECT distinct app_id FROM app_activity WHERE id not in(select app_activity_id from acct_app_activity where acct_id=?)) as a"
	   		+ " on view_app_version.id=a.app_id";
	   JsonArray params = new JsonArray();
	   params.add(acct_id);
	
	   this.queryWithParams(sql, params, future);	
    	
    }

}
