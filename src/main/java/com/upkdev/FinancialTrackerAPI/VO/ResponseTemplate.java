package com.upkdev.FinancialTrackerAPI.VO;

import com.upkdev.FinancialTrackerAPI.entity.Member;


public class ResponseTemplate {

    public Member getUser() {
        return member;
    }

    public void setUser(Member users) {
        this.member = member;
    }

    private Member member;

}