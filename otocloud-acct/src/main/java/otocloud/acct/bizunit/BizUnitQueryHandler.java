package otocloud.acct.bizunit;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import otocloud.acct.dao.BizUnitDAO;
import otocloud.common.ActionURI;
import otocloud.common.SessionSchema;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;


/**
 * 查询指定企业账户下的业务单元列表.
 * zhangyef@yonyou.com on 2015-12-16.
 */
public class BizUnitQueryHandler extends OtoCloudEventHandlerImpl<JsonObject> {

    public BizUnitQueryHandler(OtoCloudComponentImpl componentImpl) {
        super(componentImpl);
    }

    /* 
     */
    @Override
    public void handle(OtoCloudBusMessage<JsonObject> msg) {
        
        JsonObject body = msg.body();
        JsonObject session = body.getJsonObject(SessionSchema.SESSION);

        Long acctId = session.getLong(SessionSchema.ORG_ACCT_ID);

        Future<ResultSet> getFuture = Future.future();
        
        BizUnitDAO userDAO = new BizUnitDAO(this.componentImpl.getSysDatasource());
        userDAO.getBizUnitList(acctId, getFuture);
        
        getFuture.setHandler(ret -> {
            if (ret.succeeded()) {
                msg.reply(ret.result().getRows());
            } else {
            	Throwable errThrowable = ret.cause();
    			String errMsgString = errThrowable.getMessage();
    			this.componentImpl.getLogger().error(errMsgString, errThrowable);
    			msg.fail(100, errMsgString);
            }
        });

    }

    /**
     * "服务名".user-management.department.query
     *
     * @return
     */
    @Override
    public String getEventAddress() {
        return "query";
    }

    /**
     * 注册REST API。
     *
     * @return get /api/"服务名"/"组件名"/departments
     */
    @Override
    public HandlerDescriptor getHanlderDesc() {
        HandlerDescriptor handlerDescriptor = super.getHanlderDesc();
        ActionURI uri = new ActionURI("", HttpMethod.GET);
        handlerDescriptor.setRestApiURI(uri);
        return handlerDescriptor;
    }
}
