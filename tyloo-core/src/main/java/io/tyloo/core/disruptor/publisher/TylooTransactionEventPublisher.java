/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.tyloo.core.disruptor.publisher;

import io.tyloo.core.Config.TylooConfig;
import io.tyloo.api.common.TylooTransaction;
import io.tyloo.api.common.TylooTransactionRepository;
import io.tyloo.api.Enums.EventType;
import io.tyloo.core.concurrent.ConsistentHashSelector;
import io.tyloo.core.concurrent.SingletonExecutor;
import io.tyloo.core.disruptor.event.TylooTransactionEvent;
import io.tyloo.core.disruptor.handler.TylooConsumerLogDataHandler;
import io.tyloo.core.disruptor.DisruptorProviderManage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 *
 * event publisher.
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 22:03 2020/2/9
 *
 */
@Component
public class TylooTransactionEventPublisher implements SmartApplicationListener {

    private volatile AtomicBoolean isInit = new AtomicBoolean(false);

    private DisruptorProviderManage<TylooTransactionEvent> disruptorProviderManage;

    private final TylooTransactionRepository transactionRepository;

    private final TylooConfig tylooConfig;

    @Autowired
    public TylooTransactionEventPublisher(final TylooTransactionRepository transactionRepository,
                                          final TylooConfig hmilyConfig) {
        this.transactionRepository = transactionRepository;
        this.tylooConfig = hmilyConfig;
    }

    /**
     * disruptor start.
     *
     * @param bufferSize this is disruptor buffer size.
     * @param threadSize this is disruptor consumer thread size.
     */
    private void start(final int bufferSize, final int threadSize) {
        List<SingletonExecutor> selects = new ArrayList<>();
        for (int i = 0; i < threadSize; i++) {
            selects.add(new SingletonExecutor("hmily-log-disruptor" + i));
        }
        ConsistentHashSelector selector = new ConsistentHashSelector(selects);
        disruptorProviderManage =
                new DisruptorProviderManage<>(
                        new TylooConsumerLogDataHandler(selector, transactionRepository), 1, bufferSize);
        disruptorProviderManage.startup();
    }

    /**
     * publish disruptor event.
     *
     * @param tylooTransaction {@linkplain TylooTransaction }
     * @param type             {@linkplain EventType}
     */
    public void publishEvent(final TylooTransaction tylooTransaction, final int type) {
        TylooTransactionEvent event = new TylooTransactionEvent();
        event.setType(type);
        event.setTylooTransaction(tylooTransaction);
        disruptorProviderManage.getProvider().onData(event);
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> aClass) {
        return aClass == ContextRefreshedEvent.class;
    }

    @Override
    public boolean supportsSourceType(Class<?> aClass) {
        return true;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (!isInit.compareAndSet(false, true)) {
            return;
        }
        start(tylooConfig.getBufferSize(), tylooConfig.getConsumerThreads());
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE - 1;
    }
}
