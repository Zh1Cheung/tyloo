package io.tyloo.api;


import javax.transaction.xa.Xid;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

import cn.hutool.core.lang.UUID;

/*
 * xid，事务编号，用于唯一标识一个事务。使用 UUID 算法生成，保证唯一性。
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 14:57 2019/4/10
 *
 */
public class TransactionXid implements Xid, Serializable {

    private static final long serialVersionUID = -6817267250789142043L;
    /**
     * XID 的格式标识符
     * 是一个数字，用于标识由globalTransactionId和branchQualifier值使用的格式，默认值是1。
     */
    private int formatId = 1;
    /**
     * 全局事务ID.
     * 相同的分布式事务应该使用相同的globalTransactionId，这样可以明确知道XA事务属于哪个分布式任务
     */
    private byte[] globalTransactionId;
    /**
     * 分支限定符.
     * 默认值是空串；对于一个分布式事务中的每个分支事务，bqual的值必须唯一
     */
    private byte[] branchQualifier;

    private static byte[] CUSTOMIZED_TRANSACTION_ID = "UniqueIdentity".getBytes();

    public TransactionXid() {
        globalTransactionId = uuidToByteArray(UUID.randomUUID());
        branchQualifier = uuidToByteArray(UUID.randomUUID());
    }

    public void setGlobalTransactionId(byte[] globalTransactionId) {
        this.globalTransactionId = globalTransactionId;
    }

    public void setBranchQualifier(byte[] branchQualifier) {
        this.branchQualifier = branchQualifier;
    }

    public TransactionXid(Object uniqueIdentity) {

        if (uniqueIdentity == null) {

            globalTransactionId = uuidToByteArray(UUID.randomUUID());
            branchQualifier = uuidToByteArray(UUID.randomUUID());

        } else {

            this.globalTransactionId = CUSTOMIZED_TRANSACTION_ID;

            this.branchQualifier = uniqueIdentity.toString().getBytes();
        }
    }

    public TransactionXid(byte[] globalTransactionId) {
        this.globalTransactionId = globalTransactionId;
        this.branchQualifier = uuidToByteArray(UUID.randomUUID());
    }

    public TransactionXid(byte[] globalTransactionId, byte[] branchQualifier) {
        this.globalTransactionId = globalTransactionId;
        this.branchQualifier = branchQualifier;
    }

    /**
     * 获取 XID 的格式标识符部分。
     */
    @Override
    public int getFormatId() {
        return formatId;
    }

    /**
     * 获取 XID 的全局事务标识符部分作为字节数组。
     */
    @Override
    public byte[] getGlobalTransactionId() {
        return globalTransactionId;
    }

    /**
     * 获取 XID 的事务分支标识符部分作为字节数组。
     */
    @Override
    public byte[] getBranchQualifier() {
        return branchQualifier;
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        if (Arrays.equals(CUSTOMIZED_TRANSACTION_ID, globalTransactionId)) {

            stringBuilder.append(new String(globalTransactionId));
            stringBuilder.append(":").append(new String(branchQualifier));

        } else {

            stringBuilder.append(UUID.nameUUIDFromBytes(globalTransactionId).toString());
            stringBuilder.append(":").append(UUID.nameUUIDFromBytes(branchQualifier).toString());
        }

        return stringBuilder.toString();
    }

    @Override
    public TransactionXid clone() throws CloneNotSupportedException {

        TransactionXid clone = (TransactionXid) super.clone();

        byte[] cloneGlobalTransactionId = null;
        byte[] cloneBranchQualifier = null;

        if (globalTransactionId != null) {
            cloneGlobalTransactionId = new byte[globalTransactionId.length];
            System.arraycopy(globalTransactionId, 0, cloneGlobalTransactionId, 0, globalTransactionId.length);
        }

        if (branchQualifier != null) {
            cloneBranchQualifier = new byte[branchQualifier.length];
            System.arraycopy(branchQualifier, 0, cloneBranchQualifier, 0, branchQualifier.length);
        }

        return new TransactionXid(cloneGlobalTransactionId, cloneBranchQualifier);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.getFormatId();
        result = prime * result + Arrays.hashCode(branchQualifier);
        result = prime * result + Arrays.hashCode(globalTransactionId);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;

    }

    private static byte[] uuidToByteArray(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

}


