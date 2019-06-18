package io.tyloo;

import io.tyloo.api.TransactionContext;
import io.tyloo.api.TransactionContextEditor;
import io.tyloo.api.TransactionStatus;
import io.tyloo.api.TransactionXid;

import java.io.Serializable;

/*
 *
 * 事务参与者
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 20:00 2019/6/5
 *
 */

public class Participant implements Serializable {

    private static final long serialVersionUID = 4127729421281425247L;

    private TransactionXid xid;

    private InvocationContext confirmInvocationContext;

    private InvocationContext cancelInvocationContext;

    Class<? extends TransactionContextEditor> transactionContextEditorClass;

    public Participant() {

    }

    public Participant(TransactionXid xid, InvocationContext confirmInvocationContext, InvocationContext cancelInvocationContext, Class<? extends TransactionContextEditor> transactionContextEditorClass) {
        this.xid = xid;
        this.confirmInvocationContext = confirmInvocationContext;
        this.cancelInvocationContext = cancelInvocationContext;
        this.transactionContextEditorClass = transactionContextEditorClass;
    }

    public Participant(InvocationContext confirmInvocationContext, InvocationContext cancelInvocationContext, Class<? extends TransactionContextEditor> transactionContextEditorClass) {
        this.confirmInvocationContext = confirmInvocationContext;
        this.cancelInvocationContext = cancelInvocationContext;
        this.transactionContextEditorClass = transactionContextEditorClass;
    }

    public void setXid(TransactionXid xid) {
        this.xid = xid;
    }

    public void rollback() {
        Terminator.invoke(new TransactionContext(xid, TransactionStatus.CANCELLING.getId()), cancelInvocationContext, transactionContextEditorClass);
    }

    public void commit() {
        Terminator.invoke(new TransactionContext(xid, TransactionStatus.CONFIRMING.getId()), confirmInvocationContext, transactionContextEditorClass);
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
