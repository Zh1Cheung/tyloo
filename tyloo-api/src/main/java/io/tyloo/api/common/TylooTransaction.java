package io.tyloo.api.common;


import io.tyloo.api.Context.TylooTransactionContext;
import io.tyloo.api.Enums.TransactionStatus;
import io.tyloo.api.Enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private final List<Participant> participants = new ArrayList<Participant>();
    /**
     * 附带属性映射
     */
    private Map<String, Object> attachments = new ConcurrentHashMap<String, Object>();



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


    /**
     * 提交 TCC 事务
     */
    public void commit() {

        participants.forEach(Participant::commit);
    }

    /**
     * 回滚 TCC 事务
     */
    public void rollback() {
        participants.forEach(Participant::rollback);
    }


}
