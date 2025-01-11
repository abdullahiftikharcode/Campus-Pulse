package com.example.campuspulse;
public class Member {
    private String userId;
    private String memberName;
    private String username;

    public Member(String userId, String memberName, String username) {
        this.userId = userId;
        this.memberName = memberName;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getUsername() {
        return username;
    }
}

