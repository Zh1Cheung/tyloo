package io.tyloo.tcctransaction.disruptor.handler;


import io.tyloo.tcctransaction.disruptor.AbstractDisruptorConsumerExecutor;
import io.tyloo.tcctransaction.disruptor.DisruptorConsumerFactory;

/**
 * TyooTransactionHandler.
 * About the processing of a rotation function.
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 22:00 2020/2/9
 *
 */

public class TylooConsumerTransactionDataHandler extends AbstractDisruptorConsumerExecutor<TylooTransactionHandlerAlbum> implements DisruptorConsumerFactory {


    @Override
    public String fixName() {
        return "TylooConsumerTransactionDataHandler";
    }

    @Override
    public AbstractDisruptorConsumerExecutor create() {
        return this;
    }

    @Override
    public void executor(final TylooTransactionHandlerAlbum data) {
        data.run();
    }
}

