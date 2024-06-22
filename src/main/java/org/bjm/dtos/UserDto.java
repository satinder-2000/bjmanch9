package org.bjm.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.bjm.collections.LokSabha;
import org.bjm.collections.State;
import org.bjm.collections.VidhanSabha;

/**
 *
 * @author singh
 */
public class UserDto {
    
    @Size(min = 2, max=45,message = "First name must be 2-45 chars long.")
    private String firstName;
    @Size(min = 2, max=45, message = "Last name must be 2-45 chars long.")
    private String lastName;
    @Email(regexp = "^(.+)@(.+)$", message = "Invalid Email")
    private String email;
    @Pattern(regexp = "\\d{1,2}\\/\\d{1,2}\\/\\d{4}", message = "Incorrect DOB format")
    private String dob;
    private String gender;
    //@Pattern(regexp = "\\d{5}([- ]*)\\d{6}", message = "Incorrect Phone number")
    private String phone;
    //@Pattern(regexp = "^[6-9]\\d{9}$", message = "Incorrect Mobile number")
    private String mobile;
    private String stateCode;
    private List<State> allStates;
    private State state;
    private String lokSabha;
    private List<LokSabha> lokSabhas;
    private String vidhanSabha;
    private List<VidhanSabha> vidhanSabhas;
    private String profileFile;
    private byte[] image;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public List<State> getAllStates() {
        return allStates;
    }

    public void setAllStates(List<State> allStates) {
        this.allStates = allStates;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
    
    

    public String getLokSabha() {
        return lokSabha;
    }

    public void setLokSabha(String lokSabha) {
        this.lokSabha = lokSabha;
    }

    public List<LokSabha> getLokSabhas() {
        return lokSabhas;
    }

    public void setLokSabhas(List<LokSabha> lokSabhas) {
        this.lokSabhas = lokSabhas;
    }

    public String getVidhanSabha() {
        return vidhanSabha;
    }

    public void setVidhanSabha(String vidhanSabha) {
        this.vidhanSabha = vidhanSabha;
    }

    public List<VidhanSabha> getVidhanSabhas() {
        return vidhanSabhas;
    }

    public void setVidhanSabhas(List<VidhanSabha> vidhanSabhas) {
        this.vidhanSabhas = vidhanSabhas;
    }

    public String getProfileFile() {
        return profileFile;
    }

    public void setProfileFile(String profileFile) {
        this.profileFile = profileFile;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
    
    
}
