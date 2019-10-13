package io.tyloo.unittest.entity;


public enum AccountStatus {

    NORMAL(1),

    TRANSFERING(2);

    private int id;

    AccountStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
