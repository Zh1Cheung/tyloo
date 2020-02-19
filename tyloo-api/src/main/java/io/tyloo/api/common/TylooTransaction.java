package io.tyloo.api.common;


import io.tyloo.api.Context.TylooTransactionContext;
import io.tyloo.api.Enums.TransactionStatus;
import io.tyloo.api.Enums.TransactionType;

import javax.transaction.xa.Xid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 *
 * 事务类
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:02 2019/12/4
 *
 */
public class TylooTransaction implements Serializable {

    private static final long serialVersionUID = 7291423944314337931L;
    /**
     * 事务编号
     */
    private TylooTransactionXid xid;
    /**
     * 事务状态
     */
    private TransactionStatus transactionStatus;
    /**
     * 事务类型
     */
    private TransactionType transactionType;
    /**
     * 重试次数
     */
    private volatile int retriedCount = 0;
    /**
     * 创建时间
     */
    private Date createTime = new Date();
    /**
     * 最后更新时间
     */
    private Date lastUpdateTime = new Date();
    /**
     * 版本号
     */
    private long version = 1;
    /**
     * 参与者集合
     */
    private List<Participant> participants = new ArrayList<Participant>();
    /**
     * 附带属性映射
     */
    private Map<String, Object> attachments = new ConcurrentHashMap<String, Object>();

    public TylooTransaction() {

    }

    /**
     * 创建分支事务
     *
     * @param tylooTransactionContext 事务上下文
     */
    public TylooTransaction(TylooTransactionContext tylooTransactionContext) {
        this.xid = tylooTransactionContext.getXid();
        this.transactionStatus = TransactionStatus.TRYING;
        this.transactionType = TransactionType.BRANCH;
    }

    /**
     * 创建指定类型的事务
     *
     * @param transactionType 事务类型
     */
    public TylooTransaction(TransactionType transactionType) {
        this.xid = new TylooTransactionXid();
        this.transactionStatus = TransactionStatus.TRYING;
        this.transactionType = transactionType;
    }

    /**
     * 创建指定类型和制定Xid的事务
     *
     * @param uniqueIdentity
     * @param transactionType
     */
    public TylooTransaction(Object uniqueIdentity, TransactionType transactionType) {

        this.xid = new TylooTransactionXid(uniqueIdentity);
        this.transactionStatus = TransactionStatus.TRYING;
        this.transactionType = transactionType;
    }

    /**
     * 添加参与者
     *
     * @param participant 参与者
     */
    public void enlistParticipant(Participant participant) {
        participants.add(participant);
    }


    public Xid getXid() {
        return xid.clone();
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }


    public List<Participant> getParticipants() {
        return participants;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void changeStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    /**
     * 提交 TCC 事务
     */
    public void commit() {

        for (Participant participant : participants) {
            participant.commit();
        }
    }

    /**
     * 回滚 TCC 事务
     */
    public void rollback() {
        for (Participant participant : participants) {
            participant.rollback();
        }
    }

    public int getRetriedCount() {
        return retriedCount;
    }

    public void addRetriedCount() {
        this.retriedCount++;
    }

    public void resetRetriedCount(int retriedCount) {
        this.retriedCount = retriedCount;
    }

    public Map<String, Object> getAttachments() {
        return attachments;
    }

    public long getVersion() {
        return version;
    }

    public void updateVersion() {
        this.version++;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date date) {
        this.lastUpdateTime = date;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void updateTime() {
        this.lastUpdateTime = new Date();
    }


}
