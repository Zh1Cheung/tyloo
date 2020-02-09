package io.tyloo.tcctransaction.common;

import java.util.Date;
import java.util.List;

/*
 *
 * 事务库(连接tcc数据库的dao)
 * 真实对象在本地项目dubbo-order中的config.spring.local中appcontext-service-dao.xml有配置，
 * 其真实类型是SpringJdbcTransactionRepository，继承JdbcTransactionRepository，因为order、capital、redpacket是3个独立的服务，
 * 所以每一个模块中都有appcontext-service-dao.xml来配置连接池
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:00 2019/12/4
 *
 */
public interface TylooTransactionRepository {

    /**
     * 存储事务
     * 将创建的事务对象保存在本地
     *
     * @param tylooTransaction
     * @return
     */
    int create(TylooTransaction tylooTransaction);

    /**
     * 更新事务
     *
     * @param tylooTransaction
     * @return
     */
    int update(TylooTransaction tylooTransaction);

    /**
     * 删除事务
     *
     * @param tylooTransaction
     * @return
     */
    int delete(TylooTransaction tylooTransaction);

    /**
     * 查询事务
     *
     * @param xid
     * @return
     */
    TylooTransaction findByXid(TylooTransactionXid xid);

    List<TylooTransaction> findAllUnmodifiedSince(Date date);
}
