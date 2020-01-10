package io.tyloo.tcctransaction;

import io.tyloo.api.TylooContext;
import io.tyloo.api.TylooContextLoader;
import io.tyloo.api.Status;
import io.tyloo.api.TransactionXid;

import java.io.Serializable;

/*
 *
 * 事务参与者
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:00 2019/12/4
 *
 */
public class Participant implements Serializable {

    private static final long serialVersionUID = 4127729421281425247L;

    /**
     * 事务编号
     */
    private TransactionXid xid;
    /**
     * 确认执行业务方法调用上下文
     */
    private InvocationContext confirmInvocationContext;
    /**
     * 取消执行业务方法调用上下文
     */
    private InvocationContext cancelInvocationContext;
    /**
     * 事务上下文编辑
     */
    Class<? extends TylooContextLoader> tylooContextLoaderClass;
    /**
     * 执行器
     */
    private Terminator terminator = new Terminator();


    public Participant() {

    }

    public Participant(TransactionXid xid, InvocationContext confirmInvocationContext, InvocationContext cancelInvocationContext, Class<? extends TylooContextLoader> tylooContextLoaderClass) {
        this.xid = xid;
        this.confirmInvocationContext = confirmInvocationContext;
        this.cancelInvocationContext = cancelInvocationContext;
        this.tylooContextLoaderClass = tylooContextLoaderClass;
    }

    public Participant(InvocationContext confirmInvocationContext, InvocationContext cancelInvocationContext, Class<? extends TylooContextLoader> tylooContextLoaderClass) {
        this.confirmInvocationContext = confirmInvocationContext;
        this.cancelInvocationContext = cancelInvocationContext;
        this.tylooContextLoaderClass = tylooContextLoaderClass;
    }

    public void setXid(TransactionXid xid) {
        this.xid = xid;
    }

    /**
     * 回滚参与者事务（在Transaction中被调用）
     */
    public void rollback() {
        terminator.invoke(new TylooContext(xid, Status.CANCELLING.getId()), cancelInvocationContext, tylooContextLoaderClass);
    }

    /**
     * 提交参与者事务（在Transaction中被调用）.
     */
    public void commit() {
        terminator.invoke(new TylooContext(xid, Status.CONFIRMING.getId()), confirmInvocationContext, tylooContextLoaderClass);
    }

    public Terminator getTerminator() {
        return terminator;
    }

    public TransactionXid getXid() {
        return xid;
    }

    public InvocationContext getConfirmInvocationContext() {
        return confirmInvocationContext;
    }

    public InvocationContext getCancelInvocationContext() {
        return cancelInvocationContext;
    }

}
