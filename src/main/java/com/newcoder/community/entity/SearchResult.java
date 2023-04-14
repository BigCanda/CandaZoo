package com.newcoder.community.entity;

import java.util.List;

public class SearchResult {
    private long rows;
    private List<DiscussPost> posts;

    public SearchResult(long rows, List<DiscussPost> posts) {
        this.rows = rows;
        this.posts = posts;
    }

    public long getRows() {
        return rows;
    }

    public void setRows(long rows) {
        this.rows = rows;
    }

    public List<DiscussPost> getPosts() {
        return posts;
    }

    public void setPosts(List<DiscussPost> posts) {
        this.posts = posts;
    }
}
