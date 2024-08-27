package org.bjm.collections;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class User {
    
    private ObjectId _id;
    private String email;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dob;
    private String mobile;
    private String phone;
    private String stateName;
    private String lokSabha;
    private String vidhanSabha;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getLokSabha() {
        return lokSabha;
    }

    public void setLokSabha(String lokSabha) {
        this.lokSabha = lokSabha;
    }

    public String getVidhanSabha() {
        return vidhanSabha;
    }

    public void setVidhanSabha(String vidhanSabha) {
        this.vidhanSabha = vidhanSabha;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }
    
    
    
}
