package io.tyloo.core.repository;

import io.tyloo.api.common.TylooTransaction;
import io.tyloo.core.exception.TransactionIOException;
import io.tyloo.core.repository.helper.TransactionSerializer;
import io.tyloo.core.serializer.KryoPoolSerializer;
import io.tyloo.core.serializer.ObjectSerializer;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import javax.transaction.xa.Xid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 *
 * ZooKeeper事务库
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:27 2019/12/4
 *
 */
public class ZooKeeperTransactionRepository extends CachableTransactionRepository {

    private String zkServers;

    private int zkTimeout;

    private String zkRootPath = "/tcc";

    private volatile ZooKeeper zk;

    private ObjectSerializer serializer = new KryoPoolSerializer();

    public ZooKeeperTransactionRepository() {
        super();
    }

    public void setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    public void setZkRootPath(String zkRootPath) {
        this.zkRootPath = zkRootPath;
    }

    public void setZkServers(String zkServers) {
        this.zkServers = zkServers;
    }

    public void setZkTimeout(int zkTimeout) {
        this.zkTimeout = zkTimeout;
    }

    @Override
    protected int doCreate(TylooTransaction tylooTransaction) {

        try {
            getZk().create(getTxidPath(tylooTransaction.getXid()),
                    TransactionSerializer.serialize(serializer, tylooTransaction), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            return 1;
        } catch (KeeperException e) {

            if (e.code().equals(KeeperException.Code.NODEEXISTS)) {
                return 0;
            } else {
                throw new TransactionIOException(e);
            }

        } catch (InterruptedException e) {
            throw new TransactionIOException(e);
        }
    }

    @Override
    protected int doUpdate(TylooTransaction tylooTransaction) {

        try {

            tylooTransaction.updateTime();
            tylooTransaction.updateVersion();
            Stat stat = getZk().setData(getTxidPath(tylooTransaction.getXid()), TransactionSerializer.serialize(serializer, tylooTransaction), (int) tylooTransaction.getVersion() - 2);
            return 1;
        } catch (Exception e) {
            throw new TransactionIOException(e);
        }
    }

    @Override
    protected int doDelete(TylooTransaction tylooTransaction) {
        try {
            getZk().delete(getTxidPath(tylooTransaction.getXid()), (int) tylooTransaction.getVersion() - 1);
            return 1;
        } catch (Exception e) {
            throw new TransactionIOException(e);
        }
    }

    @Override
    protected TylooTransaction doFindOne(Xid xid) {

        byte[] content = null;
        try {
            Stat stat = new Stat();
            content = getZk().getData(getTxidPath(xid), false, stat);
            TylooTransaction tylooTransaction = TransactionSerializer.deserialize(serializer, content);
            return tylooTransaction;
        } catch (KeeperException.NoNodeException e) {

        } catch (Exception e) {
            throw new TransactionIOException(e);
        }
        return null;
    }

    @Override
    protected List<TylooTransaction> doFindAllUnmodifiedSince(Date date) {

        List<TylooTransaction> allTylooTransactions = doFindAll();

        List<TylooTransaction> allUnmodifiedSince = new ArrayList<TylooTransaction>();

        for (TylooTransaction tylooTransaction : allTylooTransactions) {
            if (tylooTransaction.getLastUpdateTime().compareTo(date) < 0) {
                allUnmodifiedSince.add(tylooTransaction);
            }
        }

        return allUnmodifiedSince;
    }

    protected List<TylooTransaction> doFindAll() {

        List<TylooTransaction> tylooTransactions = new ArrayList<TylooTransaction>();

        List<String> znodePaths = null;
        try {
            znodePaths = getZk().getChildren(zkRootPath, false);
        } catch (Exception e) {
            throw new TransactionIOException(e);
        }

        for (String znodePath : znodePaths) {

            byte[] content = null;
            try {
                Stat stat = new Stat();
                content = getZk().getData(getTxidPath(znodePath), false, stat);
                TylooTransaction tylooTransaction = TransactionSerializer.deserialize(serializer, content);
                tylooTransactions.add(tylooTransaction);
            } catch (Exception e) {
                throw new TransactionIOException(e);
            }
        }

        return tylooTransactions;
    }

    private ZooKeeper getZk() {

        if (zk == null) {
            synchronized (ZooKeeperTransactionRepository.class) {
                if (zk == null) {
                    try {
                        zk = new ZooKeeper(zkServers, zkTimeout, new Watcher() {
                            @Override
                            public void process(WatchedEvent watchedEvent) {

                            }
                        });

                        Stat stat = zk.exists(zkRootPath, false);

                        if (stat == null) {
                            zk.create(zkRootPath, zkRootPath.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                        }
                    } catch (Exception e) {
                        throw new TransactionIOException(e);
                    }
                }
            }
        }
        return zk;
    }

    private String getTxidPath(Xid xid) {
        return String.format("%s/%s", zkRootPath, xid);
    }

    private String getTxidPath(String znodePath) {
        return String.format("%s/%s", zkRootPath, znodePath);
    }


}
