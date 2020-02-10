package io.tyloo.core.repository;

import io.tyloo.api.common.TylooTransaction;
import io.tyloo.core.exception.TransactionIOException;
import io.tyloo.core.repository.helper.TransactionSerializer;
import io.tyloo.core.serializer.KryoPoolSerializer;
import io.tyloo.core.serializer.ObjectSerializer;

import javax.transaction.xa.Xid;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 * 文件系统事务库
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:22 2019/12/4
 *
 */
public class FileSystemTransactionRepository extends CachableTransactionRepository {

    private String rootPath = "/tcc";

    private volatile boolean initialized;

    private ObjectSerializer serializer = new KryoPoolSerializer();

    public void setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    protected int doCreate(TylooTransaction tylooTransaction) {
        return createFile(tylooTransaction);
    }

    @Override
    protected int doUpdate(TylooTransaction tylooTransaction) {

        tylooTransaction.updateVersion();
        tylooTransaction.updateTime();

        writeFile(tylooTransaction);
        return 1;
    }

    @Override
    protected int doDelete(TylooTransaction tylooTransaction) {

        String fullFileName = getFullFileName(tylooTransaction.getXid());
        File file = new File(fullFileName);
        if (file.exists()) {
            return file.delete() ? 1 : 0;
        }
        return 1;
    }

    @Override
    protected TylooTransaction doFindOne(Xid xid) {

        String fullFileName = getFullFileName(xid);
        File file = new File(fullFileName);

        if (file.exists()) {
            return readTransaction(file);
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
        File path = new File(rootPath);
        File[] files = path.listFiles();

        for (File file : files) {
            TylooTransaction tylooTransaction = readTransaction(file);
            tylooTransactions.add(tylooTransaction);
        }

        return tylooTransactions;
    }

    private String getFullFileName(Xid xid) {
        return String.format("%s/%s", rootPath, xid);
    }

    private void makeDirIfNecessary() {
        if (!initialized) {
            synchronized (FileSystemTransactionRepository.class) {
                if (!initialized) {
                    File rootPathFile = new File(rootPath);
                    if (!rootPathFile.exists()) {

                        boolean result = rootPathFile.mkdir();

                        if (!result) {
                            throw new TransactionIOException("cannot create root path, the path to create is:" + rootPath);
                        }

                        initialized = true;
                    } else if (!rootPathFile.isDirectory()) {
                        throw new TransactionIOException("rootPath is not directory");
                    }
                }
            }
        }
    }


    private int createFile(TylooTransaction tylooTransaction) {
        makeDirIfNecessary();

        String filePath = getFullFileName(tylooTransaction.getXid());

        FileChannel channel = null;
        RandomAccessFile raf = null;
        File file = null;

        byte[] content = TransactionSerializer.serialize(serializer, tylooTransaction);

        try {

            file = new File(filePath);

            boolean result = file.createNewFile();

            if (!result) {
                return 0;
            }

            raf = new RandomAccessFile(file, "rw");
            channel = raf.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(content.length);
            buffer.put(content);
            buffer.flip();

            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }

            channel.force(true);

            return 1;

        } catch (FileNotFoundException e) {
            throw new TransactionIOException(e);
        } catch (IOException e) {
            throw new TransactionIOException(e);
        } finally {
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException e) {
                    throw new TransactionIOException(e);
                }
            }
        }
    }

    private void writeFile(TylooTransaction tylooTransaction) {

        makeDirIfNecessary();

        String filePath = getFullFileName(tylooTransaction.getXid());

        FileChannel channel = null;
        RandomAccessFile raf = null;

        byte[] content = TransactionSerializer.serialize(serializer, tylooTransaction);

        try {

            raf = new RandomAccessFile(filePath, "rw");
            channel = raf.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(content.length);
            buffer.put(content);
            buffer.flip();

            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }

            channel.force(true);

        } catch (Exception e) {
            throw new TransactionIOException(e);
        } finally {
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException e) {
                    throw new TransactionIOException(e);
                }
            }
        }
    }

    private TylooTransaction readTransaction(File file) {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);

            byte[] content = new byte[(int) file.length()];

            fis.read(content);

            if (content != null) {
                return TransactionSerializer.deserialize(serializer, content);
            }
        } catch (Exception e) {
            throw new TransactionIOException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new TransactionIOException(e);
                }
            }
        }

        return null;
    }
}
