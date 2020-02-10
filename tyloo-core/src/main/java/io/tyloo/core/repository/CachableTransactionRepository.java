package io.tyloo.core.repository;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.tyloo.api.common.TylooTransactionXid;
import io.tyloo.api.common.TylooTransaction;
import io.tyloo.core.exception.ConcurrentTransactionException;
import io.tyloo.core.exception.OptimisticLockException;


import javax.transaction.xa.Xid;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 *
 * 缓存事务库
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:08 2019/12/4
 *
 */
public abstract class CachableTransactionRepository implements TransactionRepository {

    /**
     * 到期时间(以秒为单位)
     */
    private int expireDuration = 120;
    /**
     * 事务日志记录缓存<Xid, TylooTransaction>
     */
    private Cache<Xid, TylooTransaction> transactionXidTylooTransactionCache;

    /**
     * 创建事务日志记录
     */
    @Override
    public int create(TylooTransaction tylooTransaction) {
        int result = doCreate(tylooTransaction);
        if (result > 0) {
            putToCache(tylooTransaction);
        } else {
            throw new ConcurrentTransactionException("tylooTransaction xid duplicated. xid:" + tylooTransaction.getXid().toString());
        }

        return result;
    }

    /**
     * 更新事务日志记录
     */
    @Override
    public int update(TylooTransaction tylooTransaction) {
        int result = 0;

        try {
            result = doUpdate(tylooTransaction);
            if (result > 0) {
                putToCache(tylooTransaction);
            } else {
                throw new OptimisticLockException();
            }
        } finally {
            if (result <= 0) {
                removeFromCache(tylooTransaction);
            }
        }

        return result;
    }

    /**
     * 删除事务日志记录
     */
    @Override
    public int delete(TylooTransaction tylooTransaction) {
        int result = 0;

        try {
            result = doDelete(tylooTransaction);

        } finally {
            removeFromCache(tylooTransaction);
        }
        return result;
    }

    /**
     * 根据xid查找事务日志记录.
     *
     * @param tylooTransactionXid
     * @return
     */
    @Override
    public TylooTransaction findByXid(TylooTransactionXid tylooTransactionXid) {
        TylooTransaction tylooTransaction = findFromCache(tylooTransactionXid);

        if (tylooTransaction == null) {
            tylooTransaction = doFindOne(tylooTransactionXid);

            if (tylooTransaction != null) {
                putToCache(tylooTransaction);
            }
        }

        return tylooTransaction;
    }

    /**
     * 找出所有未处理事务日志（从某一时间点开始）.
     *
     * @return
     */
    @Override
    public List<TylooTransaction> findAllUnmodifiedSince(Date date) {

        List<TylooTransaction> tylooTransactions = doFindAllUnmodifiedSince(date);

        for (TylooTransaction tylooTransaction : tylooTransactions) {
            putToCache(tylooTransaction);
        }

        return tylooTransactions;
    }

    public CachableTransactionRepository() {
        transactionXidTylooTransactionCache = CacheBuilder.newBuilder().expireAfterAccess(expireDuration, TimeUnit.SECONDS).maximumSize(1000).build();
    }

    /**
     * 放入缓存.
     *
     * @param tylooTransaction
     */
    protected void putToCache(TylooTransaction tylooTransaction) {
        transactionXidTylooTransactionCache.put(tylooTransaction.getXid(), tylooTransaction);
    }

    /**
     * 从缓存中删除.
     *
     * @param tylooTransaction
     */
    protected void removeFromCache(TylooTransaction tylooTransaction) {
        transactionXidTylooTransactionCache.invalidate(tylooTransaction.getXid());
    }

    /**
     * 从缓存中查找.
     *
     * @param tylooTransactionXid
     * @return
     */
    protected TylooTransaction findFromCache(TylooTransactionXid tylooTransactionXid) {
        return transactionXidTylooTransactionCache.getIfPresent(tylooTransactionXid);
    }

    public void setExpireDuration(int durationInSeconds) {
        this.expireDuration = durationInSeconds;
    }

    /**
     * 创建事务日志记录
     *
     * @param tylooTransaction
     * @return
     */
    protected abstract int doCreate(TylooTransaction tylooTransaction);

    protected abstract int doUpdate(TylooTransaction tylooTransaction);

    protected abstract int doDelete(TylooTransaction tylooTransaction);

    protected abstract TylooTransaction doFindOne(Xid xid);

    protected abstract List<TylooTransaction> doFindAllUnmodifiedSince(Date date);
}
