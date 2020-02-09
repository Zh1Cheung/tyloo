package io.tyloo.tcctransaction.context;

import io.tyloo.api.Context.TylooContext;
import io.tyloo.api.Context.TylooContextLoader;
import io.tyloo.tcctransaction.utils.TylooMethodUtils;

import java.lang.reflect.Method;

/*
 * 如果TransactionContext通过服务提供方方法参数形式传递，
 * 则可设置tylooContextLoader为MethodtylooContextLoader.class
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 15:30 2019/12/4
 *
 */
@Deprecated
public class MethodTylooContextLoader implements TylooContextLoader {

    @Override
    public TylooContext get(Object target, Method method, Object[] args) {
        int position = TylooMethodUtils.getTransactionContextParamPosition(method.getParameterTypes());

        if (position >= 0) {
            return (TylooContext) args[position];
        }
        
        return null;
    }

    @Override
    public void set(TylooContext tylooContext, Object target, Method method, Object[] args) {

        int position = TylooMethodUtils.getTransactionContextParamPosition(method.getParameterTypes());
        if (position >= 0) {
            args[position] = tylooContext;
        }
    }
}
