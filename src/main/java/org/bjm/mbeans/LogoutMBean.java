package org.bjm.mbeans;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;

/**
 *
 * @author singh
 */
@Named(value = "logoutMBean")
@ViewScoped
public class LogoutMBean implements Serializable {
    
    public String cancelLogout(){
        return "/home/UserHome?faces-redirect=true";
    }
    
    public String logout(){
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        session.removeAttribute("access");
        return "/logoutConfirm?faces-redirect=true";
    }
    
}
