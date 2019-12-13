package io.tyloo.tcctransaction.spring.repository;


import io.tyloo.tcctransaction.repository.JdbcTransactionRepository;
import org.springframework.jdbc.datasource.DataSourceUtils;

import java.sql.Connection;

/*
 *
 * SpringJdbc事务库
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:18 2019/12/4
 *
 */
public class SpringJdbcTransactionRepository extends JdbcTransactionRepository {

    protected Connection getConnection() {
        return DataSourceUtils.getConnection(this.getDataSource());
    }

    protected void releaseConnection(Connection con) {
        DataSourceUtils.releaseConnection(con, this.getDataSource());
    }
}
