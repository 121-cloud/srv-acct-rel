/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.dao;

import java.util.List;

import otocloud.persistence.dao.JdbcDataSource;
import otocloud.persistence.dao.OperatorDAO;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;


public class AcctRelationDAO extends OperatorDAO{
	
    public AcctRelationDAO(JdbcDataSource dataSource) {
        super(dataSource);
    }
	
	
	public void create(JsonObject acctRelation, Handler<AsyncResult<UpdateResult>> done) {
		  
	  Future<UpdateResult> retFuture = Future.future();
	  retFuture.setHandler(done);	 
	  
	  String sql = "INSERT INTO acct_biz_rel(from_acct_id,to_acct_id,description)VALUES(?,?,?)"; 
	  
	  this.updateWithParams(sql, 
		  	new JsonArray()
				  .add(acctRelation.getLong("from_acct_id"))
				  .add(acctRelation.getLong("to_acct_id"))	
				  .add(acctRelation.getString("desc", "")),
				  retFuture);	  

	}
	
	
    public void findBizRelation(Long from_acct_id, Long to_acct_id, Future<ResultSet> retFuture) {        
        final String sql = "SELECT * FROM acct_biz_rel WHERE (from_acct_id=? AND to_acct_id=?) OR (from_acct_id=? AND to_acct_id=?)";
        JsonArray params = new JsonArray();
        params.add(from_acct_id);
        params.add(to_acct_id);
        params.add(to_acct_id);
        params.add(from_acct_id);

        this.queryWithParams(sql, params, retFuture);
	}
    
    public void existBizRelation(Long from_acct_id, Long to_acct_id, Future<Boolean> future) {
        
        final String sql = "SELECT count(*) as num FROM acct_biz_rel WHERE (from_acct_id=? AND to_acct_id=?) OR (from_acct_id=? AND to_acct_id=?)";
        JsonArray params = new JsonArray();
        params.add(from_acct_id);
        params.add(to_acct_id);
        params.add(to_acct_id);
        params.add(from_acct_id);

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


	public void delete(Long id, Handler<AsyncResult<UpdateResult>> done) {
		
		  Future<UpdateResult> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
		  String sql = "DELETE FROM acct_biz_rel WHERE id=?";

		  this.updateWithParams(sql, 
				  	new JsonArray()
						  .add(id),
						  retFuture);	  
	}
	

}
