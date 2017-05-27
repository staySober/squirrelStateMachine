package com.waitsober.machine;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

/**
 * Created by sober on 2017/5/27.
 *
 * @author sober
 * @date 2017/05/27
 * @useFrame squirrel 轻量级状态机框架
 * @link https://github.com/hekailiang/squirrel
 */
public class RmaStateMachine {

    public static final Logger logger = LoggerFactory.getLogger(RmaStateMachine.class);

    /**
     * define state machine event
     */
    enum RmaEvent {
        处理申请,

        确认申请,

        上传物流单,

        确认收到退货
    }

    enum RmaStatus {

        新申请_待处理,

        申请已处理_待确认,

        申请已确认_等待退货,

        退货已完成_待确认,

        退货已完成_维权结束
    }

    public static class RmaRequestParam {
        public int rmaId;

        public String addressId;

        public int orderItemId;
    }

    /**
     * define state machine class
     */
    @StateMachineParameters(stateType = String.class, eventType = RmaEvent.class, contextType = RmaRequestParam.class)
    static class RmaStateMachineEngine extends AbstractUntypedStateMachine {

        protected void approveReturnRma(String currentStatus, String targetStatus, RmaEvent event,
                                        RmaRequestParam context) {
            logger.info("Transition from '" + currentStatus + "' to '" + targetStatus + "' on event '" + event
                + "' with rmaId '" + context.rmaId + "'.");
        }

        protected void confirmApply(String currentStatus, String targetStatus, RmaEvent event, RmaRequestParam context) {
            logger.info("Entry State to '" + targetStatus + "'...");
        }
    }

    @Test
    public void testStateMachine() {
        //创建StateMachine Builder transitions
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(RmaStateMachineEngine.class);
        builder.externalTransition().from(RmaStatus.新申请_待处理.name()).to(RmaStatus.申请已处理_待确认.name()).on(RmaEvent.处理申请)
            .callMethod("approveReturnRma");
        builder.externalTransition().from(RmaStatus.申请已处理_待确认.name()).to(RmaStatus.申请已确认_等待退货.name()).on(RmaEvent.确认申请)
            .callMethod("confirmApply");

        //use State Machine
        UntypedStateMachine rmaStateMachine = builder.newStateMachine(RmaStatus.新申请_待处理.name());
        System.out.println("Current state is " + rmaStateMachine.getInitialState());

        RmaRequestParam param = new RmaRequestParam();
        param.rmaId = 1001;
        param.addressId = "上海市";
        param.orderItemId = 11;
        rmaStateMachine.fire(RmaEvent.处理申请, param);
        System.out.println("Current state is " + rmaStateMachine.getCurrentState());
        rmaStateMachine.fire(RmaEvent.确认申请, param);
        System.out.println("Current state is " + rmaStateMachine.getCurrentState());

    }
}
