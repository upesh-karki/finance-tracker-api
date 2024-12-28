package com.upkdev.FinancialTrackerAPI.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "member_detail")
public class MemberDetail {

    @Id
    private Long memberid;

    private String occupation;

    @Column(name = "address1")
    private String address1;

    @Column(name = "address2")
    private String address2;

    private String city;
    private String country;

    private Integer zipcode;

    @Column(name = "phon_number")
    private String phoneNumber;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "profile_status")
    private String profileStatus;

    // Getters and Setters
    public Long getMemberid() {
        return memberid;
    }

    public void setMemberid(Long memberid) {
        this.memberid = memberid;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getZipcode() {
        return zipcode;
    }

    public void setZipcode(Integer zipcode) {
        this.zipcode = zipcode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfileStatus() {
        return profileStatus;
    }

    public void setProfileStatus(String profileStatus) {
        this.profileStatus = profileStatus;
    }
}
