package io.tyloo.sample.capital.infrastructure.dao;

import io.tyloo.sample.capital.domain.entity.CapitalAccount;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:28 2019/12/5
 *
 */
public interface CapitalAccountDao {

    CapitalAccount findByUserId(long userId);

    int update(CapitalAccount capitalAccount);
}
