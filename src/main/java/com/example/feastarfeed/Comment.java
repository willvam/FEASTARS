package com.example.feastarfeed;

public class Comment {
    String key, content, user;

    public Comment(String content, String user) {
        this.content = content;
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public String getUser() { return user; }

    public String getKey() {
        return key;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUser(String user) { this.user = user; }

    public void setKey(String key) {
        this.key = key;
    }
}
