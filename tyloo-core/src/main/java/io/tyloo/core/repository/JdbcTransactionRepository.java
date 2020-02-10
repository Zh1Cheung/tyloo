package io.tyloo.core.repository;


import io.tyloo.api.common.TylooTransaction;
import io.tyloo.core.exception.TransactionIOException;
import io.tyloo.core.serializer.KryoPoolSerializer;
import io.tyloo.core.serializer.ObjectSerializer;
import io.tyloo.core.utils.CollectionUtils;
import io.tyloo.core.utils.StringUtils;
import io.tyloo.api.Enums.Status;

import javax.sql.DataSource;
import javax.transaction.xa.Xid;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 *
 * JDBC事务库（在应用服务中实例化并注入数据源）
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:26 2019/12/4
 *
 */
public class JdbcTransactionRepository extends CachableTransactionRepository {

    private String domain;

    private String tbSuffix;

    private DataSource dataSource;

    private ObjectSerializer serializer = new KryoPoolSerializer();

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTbSuffix() {
        return tbSuffix;
    }

    public void setTbSuffix(String tbSuffix) {
        this.tbSuffix = tbSuffix;
    }

    public void setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 创建事务日志记录
     *
     * @param tylooTransaction
     * @return
     */
    protected int doCreate(TylooTransaction tylooTransaction) {

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = this.getConnection();

            StringBuilder builder = new StringBuilder();
            builder.append("INSERT INTO " + getTableName() +
                    "(GLOBAL_TX_ID,BRANCH_QUALIFIER,TRANSACTION_TYPE,CONTENT,STATUS,RETRIED_COUNT,CREATE_TIME,LAST_UPDATE_TIME,VERSION");
            builder.append(StringUtils.isNotEmpty(domain) ? ",DOMAIN ) VALUES (?,?,?,?,?,?,?,?,?,?)" : ") VALUES (?,?,?,?,?,?,?,?,?)");

            stmt = connection.prepareStatement(builder.toString());

            stmt.setBytes(1, tylooTransaction.getXid().getGlobalTransactionId());
            stmt.setBytes(2, tylooTransaction.getXid().getBranchQualifier());
            stmt.setInt(3, tylooTransaction.getType().getId());
            stmt.setBytes(4, serializer.serialize(tylooTransaction));
            stmt.setInt(5, tylooTransaction.getStatus().getId());
            stmt.setInt(6, tylooTransaction.getRetriedCount());
            stmt.setTimestamp(7, new java.sql.Timestamp(tylooTransaction.getCreateTime().getTime()));
            stmt.setTimestamp(8, new java.sql.Timestamp(tylooTransaction.getLastUpdateTime().getTime()));
            stmt.setLong(9, tylooTransaction.getVersion());

            if (StringUtils.isNotEmpty(domain)) {
                stmt.setString(10, domain);
            }

            stmt.executeUpdate();
            return 1;
        } catch (SQLException e) {
            if (e instanceof SQLIntegrityConstraintViolationException) {
                return 0;
            } else {
                throw new TransactionIOException(e);
            }
        } catch (Throwable throwable) {
            throw new TransactionIOException(throwable);
        } finally {
            closeStatement(stmt);
            this.releaseConnection(connection);
        }
    }

    /**
     * 更新事务日志记录
     *
     * @param tylooTransaction
     * @return
     */
    protected int doUpdate(TylooTransaction tylooTransaction) {
        Connection connection = null;
        PreparedStatement stmt = null;

        java.util.Date lastUpdateTime = tylooTransaction.getLastUpdateTime();
        long currentVersion = tylooTransaction.getVersion();

        tylooTransaction.updateTime();
        tylooTransaction.updateVersion();

        try {
            connection = this.getConnection();

            StringBuilder builder = new StringBuilder();
            builder.append("UPDATE " + getTableName() + " SET " +
                    "CONTENT = ?,STATUS = ?,LAST_UPDATE_TIME = ?, RETRIED_COUNT = ?,VERSION = VERSION+1 WHERE GLOBAL_TX_ID = ? AND BRANCH_QUALIFIER = ? AND VERSION = ?");

            builder.append(StringUtils.isNotEmpty(domain) ? " AND DOMAIN = ?" : "");

            stmt = connection.prepareStatement(builder.toString());

            stmt.setBytes(1, serializer.serialize(tylooTransaction));
            stmt.setInt(2, tylooTransaction.getStatus().getId());
            stmt.setTimestamp(3, new Timestamp(tylooTransaction.getLastUpdateTime().getTime()));

            stmt.setInt(4, tylooTransaction.getRetriedCount());
            stmt.setBytes(5, tylooTransaction.getXid().getGlobalTransactionId());
            stmt.setBytes(6, tylooTransaction.getXid().getBranchQualifier());
            stmt.setLong(7, currentVersion);

            if (StringUtils.isNotEmpty(domain)) {
                stmt.setString(8, domain);
            }

            int result = stmt.executeUpdate();

            return result;

        } catch (Throwable e) {
            tylooTransaction.setLastUpdateTime(lastUpdateTime);
            tylooTransaction.setVersion(currentVersion);
            throw new TransactionIOException(e);
        } finally {
            closeStatement(stmt);
            this.releaseConnection(connection);
        }
    }

    /**
     * 删除事务日志记录
     *
     * @param tylooTransaction
     * @return
     */
    protected int doDelete(TylooTransaction tylooTransaction) {
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = this.getConnection();

            StringBuilder builder = new StringBuilder();
            builder.append("DELETE FROM " + getTableName() +
                    " WHERE GLOBAL_TX_ID = ? AND BRANCH_QUALIFIER = ?");

            builder.append(StringUtils.isNotEmpty(domain) ? " AND DOMAIN = ?" : "");

            stmt = connection.prepareStatement(builder.toString());

            stmt.setBytes(1, tylooTransaction.getXid().getGlobalTransactionId());
            stmt.setBytes(2, tylooTransaction.getXid().getBranchQualifier());

            if (StringUtils.isNotEmpty(domain)) {
                stmt.setString(3, domain);
            }

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new TransactionIOException(e);
        } finally {
            closeStatement(stmt);
            this.releaseConnection(connection);
        }
    }

    /**
     * 根据ID查找事务
     *
     * @param xid
     * @return
     */
    protected TylooTransaction doFindOne(Xid xid) {

        List<TylooTransaction> tylooTransactions = doFind(Arrays.asList(xid));

        if (!CollectionUtils.isEmpty(tylooTransactions)) {
            return tylooTransactions.get(0);
        }
        return null;
    }

    /**
     * 找出所有未处理事务日志（从某一时间点开始）
     *
     * @param date
     * @return
     */
    @Override
    protected List<TylooTransaction> doFindAllUnmodifiedSince(java.util.Date date) {

        List<TylooTransaction> tylooTransactions = new ArrayList<TylooTransaction>();

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = this.getConnection();

            StringBuilder builder = new StringBuilder();

            builder.append("SELECT GLOBAL_TX_ID, BRANCH_QUALIFIER, CONTENT,STATUS,TRANSACTION_TYPE,CREATE_TIME,LAST_UPDATE_TIME,RETRIED_COUNT,VERSION");
            builder.append(StringUtils.isNotEmpty(domain) ? ",DOMAIN" : "");
            builder.append("  FROM " + getTableName() + " WHERE LAST_UPDATE_TIME < ?");
            builder.append(" AND IS_DELETE = 0 ");
            builder.append(StringUtils.isNotEmpty(domain) ? " AND DOMAIN = ?" : "");

            stmt = connection.prepareStatement(builder.toString());

            stmt.setTimestamp(1, new Timestamp(date.getTime()));

            if (StringUtils.isNotEmpty(domain)) {
                stmt.setString(2, domain);
            }

            ResultSet resultSet = stmt.executeQuery();

            this.constructTransactions(resultSet, tylooTransactions);
        } catch (Throwable e) {
            throw new TransactionIOException(e);
        } finally {
            closeStatement(stmt);
            this.releaseConnection(connection);
        }

        return tylooTransactions;
    }

    /**
     * 查找所有事务
     *
     * @param xids
     * @return
     */
    protected List<TylooTransaction> doFind(List<Xid> xids) {

        List<TylooTransaction> tylooTransactions = new ArrayList<TylooTransaction>();

        if (CollectionUtils.isEmpty(xids)) {
            return tylooTransactions;
        }

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = this.getConnection();

            StringBuilder builder = new StringBuilder();
            builder.append("SELECT GLOBAL_TX_ID, BRANCH_QUALIFIER, CONTENT,STATUS,TRANSACTION_TYPE,CREATE_TIME,LAST_UPDATE_TIME,RETRIED_COUNT,VERSION");
            builder.append(StringUtils.isNotEmpty(domain) ? ",DOMAIN" : "");
            builder.append("  FROM " + getTableName() + " WHERE");

            if (!CollectionUtils.isEmpty(xids)) {
                for (Xid xid : xids) {
                    builder.append(" ( GLOBAL_TX_ID = ? AND BRANCH_QUALIFIER = ? ) OR");
            }

                builder.delete(builder.length() - 2, builder.length());
            }

            builder.append(StringUtils.isNotEmpty(domain) ? " AND DOMAIN = ?" : "");

            stmt = connection.prepareStatement(builder.toString());

            int i = 0;

            for (Xid xid : xids) {
                stmt.setBytes(++i, xid.getGlobalTransactionId());
                stmt.setBytes(++i, xid.getBranchQualifier());
            }

            if (StringUtils.isNotEmpty(domain)) {
                stmt.setString(++i, domain);
            }

            ResultSet resultSet = stmt.executeQuery();

            this.constructTransactions(resultSet, tylooTransactions);
        } catch (Throwable e) {
            throw new TransactionIOException(e);
        } finally {
            closeStatement(stmt);
            this.releaseConnection(connection);
        }

        return tylooTransactions;
    }

    protected void constructTransactions(ResultSet resultSet, List<TylooTransaction> tylooTransactions) throws SQLException {
        while (resultSet.next()) {
            byte[] transactionBytes = resultSet.getBytes(3);
            TylooTransaction tylooTransaction = (TylooTransaction) serializer.deserialize(transactionBytes);
            tylooTransaction.changeStatus(Status.valueOf(resultSet.getInt(4)));
            tylooTransaction.setLastUpdateTime(resultSet.getDate(7));
            tylooTransaction.setVersion(resultSet.getLong(9));
            tylooTransaction.resetRetriedCount(resultSet.getInt(8));
            tylooTransactions.add(tylooTransaction);
        }
    }


    protected Connection getConnection() {
        try {
            return this.dataSource.getConnection();
        } catch (SQLException e) {
            throw new TransactionIOException(e);
        }
    }

    protected void releaseConnection(Connection con) {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            throw new TransactionIOException(e);
        }
    }

    private void closeStatement(Statement stmt) {
        try {
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
            }
        } catch (Exception ex) {
            throw new TransactionIOException(ex);
        }
    }

    /**
     * 从当前TransactionRepository中拿到前缀拼接成表名，这个前缀是在刚刚的
     * appcontext-service-dao.xml中配置，其配置了前缀和连接池(此时的前缀是"_ORD",连接池是tccDataSource)，最终
     * 将数据保存在当前模块中的tcc库中的tcc_transaction_ord表，并且将其设置到缓存中
     *
     * @return
     */
    private String getTableName() {
        return StringUtils.isNotEmpty(tbSuffix) ? "TCC_TRANSACTION" + tbSuffix : "TCC_TRANSACTION";
    }
}
