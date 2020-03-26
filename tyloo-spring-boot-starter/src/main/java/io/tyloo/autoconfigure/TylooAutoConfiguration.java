package io.tyloo.autoconfigure;

import io.tyloo.api.common.TylooTransaction;
import io.tyloo.core.interceptor.TylooAspect;
import io.tyloo.core.interceptor.TylooCoordinatorAspect;
import io.tyloo.core.recover.TylooTransactionRecoverConfig;
import io.tyloo.core.recover.TylooTransactionRecovery;
import io.tyloo.core.repository.TransactionRepository;
import io.tyloo.core.serializer.KryoPoolSerializer;
import io.tyloo.core.serializer.ObjectSerializer;
import io.tyloo.core.support.TransactionConfigurator;
import io.tyloo.core.utils.CollectionUtils;
import io.tyloo.spring.aspect.ConfigurableCoordinatorAspect;
import io.tyloo.spring.aspect.ConfigurableTransactionAspect;
import io.tyloo.spring.recover.DefaultTylooTransactionRecoverConfig;
import io.tyloo.spring.recover.RecoverScheduledJob;
import io.tyloo.spring.repository.SpringJdbcTransactionRepository;
import io.tyloo.spring.support.SpringBeanFactory;
import io.tyloo.spring.support.SpringTransactionConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.quartz.Scheduler;


import javax.sql.DataSource;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 22:47 2020/3/20
 *
 */

@Configuration
@EnableScheduling
@EnableConfigurationProperties(TylooConfigProperties.class)
@ConditionalOnProperty(value = "tyloo.enabled", havingValue = "true", matchIfMissing = true)
public class TylooAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(TylooAutoConfiguration.class);

    private final TylooConfigProperties props;

    public TylooAutoConfiguration(TylooConfigProperties props) {
        this.props = props;
    }

    @Bean
    @ConditionalOnMissingBean(TransactionRepository.class)
    public TransactionRepository transactionRepository(ObjectSerializer<TylooTransaction> serializer) {

        SpringJdbcTransactionRepository transactionRepository = new SpringJdbcTransactionRepository();
        transactionRepository.setDataSource(buildTccDataSource());
        TylooConfigProperties.DataSource dataSourceAutoConfig = props.getDatasource();
        if (StringUtils.isEmpty(dataSourceAutoConfig.getDomain())) {
            LOG.warn("Property tcc.data-source-config.domain does not config");
        }
        transactionRepository.setDomain(dataSourceAutoConfig.getDomain());
        if (StringUtils.isEmpty(dataSourceAutoConfig.getTableSuffix())) {
            LOG.warn("Property tcc.data-source-config.table-suffix does not config");
        }
        transactionRepository.setTbSuffix(dataSourceAutoConfig.getTableSuffix());
        transactionRepository.setSerializer(serializer);
        return transactionRepository;
    }

    private DataSource buildTccDataSource() {

        TylooConfigProperties.DataSource dataSourceAutoConfig = props.getDatasource();

        Assert.notNull(dataSourceAutoConfig,
                "Properties tyloo.data-source-config.* must be config in application.properties/application.yml");
        Assert.notNull(dataSourceAutoConfig.getDataSourceProvider(),
                "Property tyloo.data-source-config.data-source-provider must be config in application.properties/application.yml");
        Assert.hasText(dataSourceAutoConfig.getDriverClassName(),
                "Property tyloo.data-source-config.driver-class-name must be config in application.properties/application.yml");
        Assert.hasText(dataSourceAutoConfig.getUrl(),
                "Property tyloo.data-source-config.url must be config in application.properties/application.yml");
        Assert.hasText(dataSourceAutoConfig.getUsername(),
                "Property tyloo.data-source-config.username must be config in application.properties/application.yml");
        Assert.hasText(dataSourceAutoConfig.getPassword(),
                "Property tyloo.data-source-config.password must be config in application.properties/application.yml");

        return DataSourceBuilder.create()
                .type(dataSourceAutoConfig.getDataSourceProvider())
                .driverClassName(dataSourceAutoConfig.getDriverClassName())
                .url(dataSourceAutoConfig.getUrl())
                .username(dataSourceAutoConfig.getUsername())
                .password(dataSourceAutoConfig.getPassword())
                .build();
    }

    @Bean
    public SpringBeanFactory springBeanFactory() {
        return new SpringBeanFactory();
    }

    @Bean
    public TransactionConfigurator transactionConfigurator(TylooTransactionRecoverConfig tylooTransactionRecoverConfig,
                                                           TransactionRepository transactionRepository) {
        SpringTransactionConfigurator transactionConfigurator = new SpringTransactionConfigurator();
        transactionConfigurator.setTylooTransactionRecoverConfig(tylooTransactionRecoverConfig);
        transactionConfigurator.setTransactionRepository(transactionRepository);
        transactionConfigurator.init();
        return transactionConfigurator;
    }


    @Bean
    public TylooAspect TransactionAspect(TransactionConfigurator transactionConfigurator) {
        ConfigurableTransactionAspect aspect = new ConfigurableTransactionAspect();
        aspect.setTransactionConfigurator(transactionConfigurator);
        aspect.init();
        return aspect;
    }

    @Bean
    public TylooCoordinatorAspect resourceCoordinatorAspect(TransactionConfigurator transactionConfigurator) {
        ConfigurableCoordinatorAspect aspect = new ConfigurableCoordinatorAspect();
        aspect.setTransactionConfigurator(transactionConfigurator);
        aspect.init();
        return aspect;
    }

    @Bean
    public TylooTransactionRecovery transactionRecovery(TransactionConfigurator transactionConfigurator) {
        TylooTransactionRecovery transactionTylooTransactionRecovery = new TylooTransactionRecovery();
        transactionTylooTransactionRecovery.setTransactionConfigurator(transactionConfigurator);
        return transactionTylooTransactionRecovery;
    }

    @Bean
    public SchedulerFactoryBean recoverScheduler() {
        return new SchedulerFactoryBean();
    }

    @Bean
    public RecoverScheduledJob recoverScheduledJob(TylooTransactionRecovery transactionTylooTransactionRecovery,
                                                   TransactionConfigurator transactionConfigurator,
                                                   Scheduler recoverScheduler) {
        RecoverScheduledJob recoverScheduledJob = new RecoverScheduledJob();
        recoverScheduledJob.setTransactionTylooTransactionRecovery(transactionTylooTransactionRecovery);
        recoverScheduledJob.setTransactionConfigurator(transactionConfigurator);
        recoverScheduledJob.setScheduler(recoverScheduler);
        recoverScheduledJob.init();
        return recoverScheduledJob;
    }

    @Bean
    @ConditionalOnMissingBean(ObjectSerializer.class)
    public ObjectSerializer<?> objectSerializer() {
        return new KryoPoolSerializer();
    }

    @Bean
    @ConditionalOnMissingBean(TylooTransactionRecoverConfig.class)
    public TylooTransactionRecoverConfig recoverConfig() {

        TylooConfigProperties.Recover recoverAutoConfig = props.getRecover();
        DefaultTylooTransactionRecoverConfig recoverConfig = new DefaultTylooTransactionRecoverConfig();
        if (recoverAutoConfig.getMaxRetryCount() > 0) {
            recoverConfig.setMaxRetryCount(recoverAutoConfig.getMaxRetryCount());
        }
        if (recoverAutoConfig.getRecoverDuration() > 0) {
            recoverConfig.setRecoverDuration(recoverAutoConfig.getRecoverDuration());
        }
        if (!StringUtils.isEmpty(recoverAutoConfig.getCronExpression())) {
            recoverConfig.setCronExpression(recoverAutoConfig.getCronExpression());
        }
        if (recoverAutoConfig.getAsyncTerminateThreadPoolSize() > 0) {
            recoverConfig.setAsyncTerminateThreadPoolSize(recoverAutoConfig.getAsyncTerminateThreadPoolSize());
        }
        if (!CollectionUtils.isEmpty(recoverAutoConfig.getDelayCancelExceptions())) {
            if (recoverAutoConfig.isAppendDelayCancelException()) {
                for (Class<? extends Exception> exClassType : recoverAutoConfig.getDelayCancelExceptions()) {
                    recoverConfig.getDelayCancelExceptions().add(exClassType);
                }
            } else {
                recoverConfig.getDelayCancelExceptions().clear();
                recoverConfig.getDelayCancelExceptions().addAll(recoverAutoConfig.getDelayCancelExceptions());
            }
        }
        return recoverConfig;
    }
}