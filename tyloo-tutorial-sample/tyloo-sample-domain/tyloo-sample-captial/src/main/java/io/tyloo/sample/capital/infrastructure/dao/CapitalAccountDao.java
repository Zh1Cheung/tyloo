package io.tyloo.sample.capital.infrastructure.dao;

import io.tyloo.sample.capital.domain.entity.CapitalAccount;


public interface CapitalAccountDao {

    CapitalAccount findByUserId(long userId);

    int update(CapitalAccount capitalAccount);
}
