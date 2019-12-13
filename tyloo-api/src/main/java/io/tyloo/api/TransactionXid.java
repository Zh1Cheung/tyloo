package io.tyloo.api;


import javax.transaction.xa.Xid;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

/*
 * xid，事务编号( TransactionXid )，用于唯一标识一个事务。使用 UUID 算法生成，保证唯一性。
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 14:57 2019/12/4
 *
 */

/**
 * Created by changmingxie on 10/26/15.
 * Xid 接口是 X/Open 事务标识符 XID 结构的 Java 映射。
 * 此接口指定三个访问器方法，以检索全局事务格式 ID、全局事务 ID 和分支限定符。
 * Xid 接口供事务管理器和资源管理器使用。此接口对应用程序不可见。
 * http://www.zgqxb.com.cn/mydoc/j2se_api_cn/javax/transaction/xa/Xid.html
 * <p>
 * Xid： 指一个XA事务。不同的数据库要不同的 Xid（每个数据库连接（分支）一个）
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

    /**
     * 克隆事务ID.
     */
    public TransactionXid clone() {

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

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.getFormatId();
        result = prime * result + Arrays.hashCode(branchQualifier);
        result = prime * result + Arrays.hashCode(globalTransactionId);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        TransactionXid other = (TransactionXid) obj;
        if (this.getFormatId() != other.getFormatId()) {
            return false;
        } else if (!Arrays.equals(branchQualifier, other.branchQualifier)) {
            return false;
        } else if (!Arrays.equals(globalTransactionId, other.globalTransactionId)) {
            return false;
        }
        return true;
    }

    private static byte[] uuidToByteArray(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private static UUID byteArrayToUUID(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    /**
     * 测试用：
     *
     * @param args
     */
    public static void main(String[] args) {
        byte[] gt = UUID.randomUUID().toString().getBytes();

        byte[] bt = UUID.randomUUID().toString().getBytes();

        String str = UUID.nameUUIDFromBytes(gt).toString() + "|" + UUID.nameUUIDFromBytes(bt).toString();
        System.out.println(str);

    }
}


