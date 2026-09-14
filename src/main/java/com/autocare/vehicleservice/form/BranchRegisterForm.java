package com.autocare.vehicleservice.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BranchRegisterForm {

    @NotBlank(message = "Please enter a branch name")
    private String branchName;

    @NotBlank(message = "Please enter a city")
    private String city;

    @NotBlank(message = "Please enter an address")
    private String address;

    @NotBlank(message = "Please enter the manager's name")
    private String managerName;

    @NotBlank(message = "Please enter the manager's email")
    @Email(message = "Enter a valid email address")
    private String managerEmail;

    @NotBlank(message = "Please choose a password")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String managerPassword;

    @NotBlank(message = "Please enter the manager's phone number")
    private String managerPhone;

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }

    public String getManagerPassword() {
        return managerPassword;
    }

    public void setManagerPassword(String managerPassword) {
        this.managerPassword = managerPassword;
    }

    public String getManagerPhone() {
        return managerPhone;
    }

    public void setManagerPhone(String managerPhone) {
        this.managerPhone = managerPhone;
    }
}

