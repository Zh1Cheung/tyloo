package io.tyloo.core.disruptor.handler;

import io.tyloo.api.common.TylooTransaction;
import io.tyloo.api.common.TylooTransactionRepository;
import io.tyloo.api.Enums.EventType;
import io.tyloo.core.concurrent.ConsistentHashSelector;
import io.tyloo.core.disruptor.event.TylooTransactionEvent;
import io.tyloo.core.disruptor.AbstractDisruptorConsumerExecutor;
import io.tyloo.core.disruptor.DisruptorConsumerFactory;

import javax.transaction.xa.Xid;


/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 22:02 2020/2/9
 *
 */


public class TylooConsumerLogDataHandler extends AbstractDisruptorConsumerExecutor<TylooTransactionEvent> implements DisruptorConsumerFactory {

    private ConsistentHashSelector executor;

    private final TylooTransactionRepository transactionRepository;

    public TylooConsumerLogDataHandler(final ConsistentHashSelector executor, final TylooTransactionRepository transactionRepository) {
        this.executor = executor;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public String fixName() {
        return "HmilyConsumerDataHandler";
    }

    @Override
    public AbstractDisruptorConsumerExecutor create() {
        return this;
    }

    @Override
    public void executor(final TylooTransactionEvent event) {
        Xid transId = event.getTylooTransaction().getXid();
        byte[] globalTransactionId = transId.getGlobalTransactionId();
        String transID = globalTransactionId.toString();
        executor.select(transID).execute(() -> {
            EventType eventTypeEnum = EventType.buildByCode(event.getType());
            switch (eventTypeEnum) {
                case SAVE:
                    transactionRepository.create(event.getTylooTransaction());
                    break;
                case DELETE:
                    transactionRepository.delete(event.getTylooTransaction());
                    break;
                case UPDATE:
                    final TylooTransaction tylooTransaction = event.getTylooTransaction();
                    transactionRepository.update(event.getTylooTransaction());
                    break;
                default:
                    break;
            }
            event.clear();
        });
    }

}
