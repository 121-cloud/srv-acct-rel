/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.rel.build;

import otocloud.framework.core.OtoCloudComponentImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月16日
 * @author lijing@yonyou.com
 */
public class AccountBizRelationBuilderImpl implements AccountBizRelationBuilder {	
	
	protected OtoCloudComponentImpl component;	
	protected JDBCClient sqlClient;
	
	public AccountBizRelationBuilderImpl(OtoCloudComponentImpl component, JDBCClient sqlClient){
		this.component = component;
		this.sqlClient = sqlClient;
	}

	public void createAccountRelation(Integer fromAppId, Integer fromRoleRelid, Boolean isReverse, Integer fromAccount, Integer toAccount, Integer toAppId,
			Handler<AsyncResult<Boolean>> retHandler){
		
		Future<Boolean> ret = Future.future();
		ret.setHandler(retHandler);
		
		sqlClient.getConnection(connRes -> {
			if (connRes.succeeded()) {
				final SQLConnection conn = connRes.result();				
				conn.setAutoCommit(true, res ->{
				  if (res.failed()) {
					  closeDBConnect(conn);
	  	    		  Throwable err = res.cause();
	  	    		  String replyMsg = err.getMessage();
	  	    		  component.getLogger().error(replyMsg, err);
					  ret.fail(err);
				  }else{										
					conn.updateWithParams("INSERT INTO app_inst_acct_rel(org_acct_id,app_module_id,biz_role_rel_id,is_reverse,to_org_acct_id)VALUES(?,?,?,?,?)", 
							   new JsonArray().add(fromAccount)
											  .add(fromAppId)
											  .add(fromRoleRelid)
											  .add(isReverse ? 1 : 0)	
											  .add(toAccount),
					  toRoleAcc->{								  
						  if (toRoleAcc.succeeded()) {
								conn.updateWithParams("INSERT INTO app_inst_acct_rel(org_acct_id,app_module_id,biz_role_rel_id,is_reverse,to_org_acct_id)VALUES(?,?,?,?,?)", 
										   new JsonArray().add(toAccount)
														  .add(toAppId)
														  .add(fromRoleRelid)
														  .add(isReverse ? 0 : 1)	
														  .add(fromAccount),
								  toRoleAcc2->{								  
									  if (toRoleAcc2.succeeded()) {
										  ret.complete(true);				  
									  }else{
						  	    		  Throwable err = toRoleAcc2.cause();
						  	    		  String replyMsg = err.getMessage();
						  	    		  component.getLogger().error(replyMsg, err);
						  	    		  ret.fail(err);
									  }
									  closeDBConnect(conn);
								  });		  
						  }else{
			  	    		  Throwable err = toRoleAcc.cause();
			  	    		  String replyMsg = err.getMessage();
			  	    		  component.getLogger().error(replyMsg, err);
			  	    		  ret.fail(err);
						  }
						  closeDBConnect(conn);
					  });
				}
			});

		}else{
    		  Throwable err = connRes.cause();
    		  String replyMsg = err.getMessage();
    		  component.getLogger().error(replyMsg, err);
			  ret.fail(err);
			}
		});
	

	}
	
	private void closeDBConnect(SQLConnection conn){
		conn.close(handler->{
			if (handler.failed()) {
				Throwable conErr = handler.cause();
				component.getLogger().error(conErr.getMessage(), conErr);
			} else {								
			}
		});				
	}
	
	
}
