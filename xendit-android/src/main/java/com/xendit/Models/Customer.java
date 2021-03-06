package com.xendit.Models;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class Customer {

    // Xendit ID of the customer
    @SerializedName("id")
    private String id;

    // Merchant's ID of the customer
    @SerializedName("reference_id")
    private String referenceId;

    @SerializedName("email")
    private String email;

    @SerializedName("given_names")
    private String givenNames;

    @SerializedName("surname")
    private String surname;

    @SerializedName("description")
    private String description;

    @SerializedName("mobile_number")
    private String mobileNumber;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("nationality")
    private String nationality;

    @SerializedName("date_of_birth")
    private String dateOfBirth; // e.g. 1990-04-13

    @SerializedName("card_info")
    private Map<String, String> card_info;

    @SerializedName("addresses")
    private Address[] addresses;

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGivenNames() {
        return givenNames;
    }

    public void setGivenNames(String givenNames) {
        this.givenNames = givenNames;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Map<String, String> getCardInfo() {
        return card_info;
    }

    public void setCardInfo(Map<String, String> card_info) {
        this.card_info = card_info;
    }
    
    public Address[] getAddresses() {
        return addresses;
    }

    public void setAddresses(Address[] addresses) {
        this.addresses = addresses;
    }
}
