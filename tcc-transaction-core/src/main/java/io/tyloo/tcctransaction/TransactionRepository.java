package io.tyloo.tcctransaction;

import io.tyloo.api.TransactionXid;

import java.util.Date;
import java.util.List;

/*
 *
 *
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:00 2019/12/4
 *
 */
public interface TransactionRepository {

    /**
     * 存储事务
     *
     * @param transaction
     * @return
     */
    int create(Transaction transaction);

    /**
     * 更新事务
     *
     * @param transaction
     * @return
     */
    int update(Transaction transaction);

    /**
     * 删除事务
     *
     * @param transaction
     * @return
     */
    int delete(Transaction transaction);

    /**
     * 查询事务
     *
     * @param xid
     * @return
     */
    Transaction findByXid(TransactionXid xid);

    List<Transaction> findAllUnmodifiedSince(Date date);
}
