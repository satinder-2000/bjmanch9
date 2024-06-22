package org.bjm.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.bjm.utils.BjmConstants;
import org.bjm.utils.ImageVO;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
@WebServlet(name = "ForumCommenterImageServlet", urlPatterns = {"/ForumCommenterImageServlet"})
public class ForumCommenterImageServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session=request.getSession();
        Map<ObjectId, ImageVO> forumCommenterImageMap=(Map) session.getAttribute(BjmConstants.FORUM_COMMENTER_IMAGE_MAP);
        String forumCommenterId=request.getParameter("forumCommenterId");
        ObjectId forumCommenterIdObj=new ObjectId(forumCommenterId);
        ImageVO forumCommenterImageVO=forumCommenterImageMap.get(forumCommenterIdObj);
        response.setContentType("image/"+forumCommenterImageVO.getImgType());
        response.getOutputStream().write(forumCommenterImageVO.getImage());
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
