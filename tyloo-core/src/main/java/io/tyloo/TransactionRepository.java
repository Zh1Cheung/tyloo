package io.tyloo;

import io.tyloo.api.TransactionXid;

import java.util.Date;
import java.util.List;

/*
 *
 * 事务库(连接tcc数据库的dao)
 * 真实对象在本地项目dubbo-order中的config.spring.local中appcontext-service-dao.xml有配置，
 * 其真实类型是SpringJdbcTransactionRepository，继承JdbcTransactionRepository，因为order、capital、redpacket是3个独立的服务，
 * 所以每一个模块中都有appcontext-service-dao.xml来配置连接池
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 19:00 2019/6/12
 *
 */

public interface TransactionRepository {

    int create(Transaction transaction) throws CloneNotSupportedException;

    int update(Transaction transaction);

    int delete(Transaction transaction);

    Transaction findByXid(TransactionXid xid);

    List<Transaction> findAllUnmodifiedSince(Date date);
}
