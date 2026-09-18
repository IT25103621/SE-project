package com.autocare.vehicleservice.form;

import com.autocare.vehicleservice.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

//Staff details form
public class StaffForm {

    @NotBlank(message = "Please enter a name")
    private String name;

    @NotBlank(message = "Please enter an email")
    @Email(message = "Enter a valid email address")
    private String email;

    //  Must be ≥6 chars when provided.
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Please enter a phone number")
    private String phone;

    @NotNull(message = "Please choose a role")
    private Role role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
