package io.tyloo.spring.repository;


import io.tyloo.repository.JdbcTransactionRepository;
import org.springframework.jdbc.datasource.DataSourceUtils;

import java.sql.Connection;

/*
 *
 * SpringJdbcÊÂÎñ¿â
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 20:18 2019/10/30
 *
 */

public class SpringJdbcTransactionRepository extends JdbcTransactionRepository {

    @Override
    protected Connection getConnection() {
        return DataSourceUtils.getConnection(this.getDataSource());
    }

    @Override
    protected void releaseConnection(Connection con) {
        DataSourceUtils.releaseConnection(con, this.getDataSource());
    }
}
