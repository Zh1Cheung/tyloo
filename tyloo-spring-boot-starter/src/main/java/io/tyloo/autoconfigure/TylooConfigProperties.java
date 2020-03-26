package io.tyloo.autoconfigure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;


/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 22:47 2020/3/20
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "tyloo")
public class TylooConfigProperties {

    private boolean enabled = true;
    private DataSource datasource;
    private Recover recover = new Recover();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class DataSource {

        private Class<? extends javax.sql.DataSource> dataSourceProvider;

        private String driverClassName;

        private String url;

        private String username;

        private String password;

        private String domain;

        private String tableSuffix;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Recover {

        private int maxRetryCount = 30;

        // 120 seconds
        private int recoverDuration = 120;

        private String cronExpression = "0 */1 * * * ?";

        private int asyncTerminateThreadPoolSize = 1024;

        private boolean appendDelayCancelException = true;

        private Set<Class<? extends Exception>> delayCancelExceptions = new HashSet<Class<? extends Exception>>();
    }
}