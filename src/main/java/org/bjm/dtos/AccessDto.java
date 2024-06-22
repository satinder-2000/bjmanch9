package org.bjm.dtos;

import jakarta.validation.constraints.Pattern;

/**
 *
 * @author singh
 */
public class AccessDto {
    
    private String email;
    @Pattern(regexp="^(?=.*\\d).{8,14}$")
    private String password;
    @Pattern(regexp="^(?=.*\\d).{8,14}$")
    private String confirmPassword;

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    
    
    
}
