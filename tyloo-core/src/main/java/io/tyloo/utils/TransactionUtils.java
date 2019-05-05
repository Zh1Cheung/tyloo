package io.tyloo.utils;

import io.tyloo.api.Propagation;
import io.tyloo.interceptor.TylooMethodContext;

/*
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 19:49 2019/5/30
 *
 */
public class TransactionUtils {

    public static boolean isLegalTransactionContext(boolean isTransactionActive, TylooMethodContext tylooMethodContext) {


        if (tylooMethodContext.getPropagation().equals(Propagation.MANDATORY) && !isTransactionActive && tylooMethodContext.getTransactionContext() == null) {
            return false;
        }

        return true;
    }
}
