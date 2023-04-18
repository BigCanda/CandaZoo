package com.newcoder.community.entity;

import java.util.List;

public class SearchUserResult {


    private long rows;
    private List<User> users;

    public SearchUserResult(long rows, List<User> users) {
        this.rows = rows;
        this.users = users;
    }

    public long getRows() {
        return rows;
    }

    public void setRows(long rows) {
        this.rows = rows;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
