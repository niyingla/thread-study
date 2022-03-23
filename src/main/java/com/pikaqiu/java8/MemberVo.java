package com.pikaqiu.java8;

public class MemberVo {
    private Integer memberId;

    private String name;

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MemberVo(Integer memberId, String name) {
        this.memberId = memberId;
        this.name = name;
    }

    @Override
    public String toString() {
        return "MemberVo{" +
                "memberId=" + memberId +
                ", name='" + name + '\'' +
                '}';
    }
}
