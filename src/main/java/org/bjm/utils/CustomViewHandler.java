package org.bjm.utils;

import jakarta.faces.application.ViewHandler;
import jakarta.faces.application.ViewHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;
import java.util.List;
import java.util.Map;

/**
 *
 * @author singh
 * Copied from: https://stackoverflow.com/questions/9905946/url-rewriting-solution-needed-for-jsf
 * 
 */
public class CustomViewHandler extends ViewHandlerWrapper {
    
    private ViewHandler wrapped;

    public CustomViewHandler(ViewHandler wrapped) {
        super();
        this.wrapped = wrapped;
    }

    @Override
    public ViewHandler getWrapped() {
        return super.getWrapped(); 
    }

    @Override
    public String getActionURL(FacesContext context, String viewId) {
        String url = super.getActionURL(context, viewId);
        return removeContextPath(context, url);
    }

    @Override
    public String getRedirectURL(FacesContext context, String viewId, Map<String, List<String>> parameters, boolean includeViewParams) {
        String url =  super.getRedirectURL(context, viewId, parameters, includeViewParams);
        return removeContextPath(context, url);
    }

    
    
    @Override
    public String getResourceURL(FacesContext context, String path) {
        String url = super.getResourceURL(context, path);
        return removeContextPath(context, url);
    }

    private String removeContextPath(FacesContext context, String url) {
        ServletContext servletContext = (ServletContext) context.getExternalContext().getContext();
        String contextPath = servletContext.getContextPath();
        if("".equals(contextPath)) return url; // root context path, nothing to remove
        return url.startsWith(contextPath) ? url.substring(contextPath.length()) : url;
    }
    
    
    
    
    
    
    
}
