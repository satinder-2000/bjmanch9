package org.bjm.ejbs;

import jakarta.ejb.Local;
import org.bjm.collections.Access;
import org.bjm.collections.Blog;
import org.bjm.collections.Forum;
import org.bjm.collections.LokSabhaNominate;
import org.bjm.collections.Survey;
import org.bjm.collections.SurveyFromForum;
import org.bjm.collections.User;
import org.bjm.collections.UserBlog;
import org.bjm.collections.VidhanSabhaNominate;

/**
 *
 * @author singh
 */
@Local
public interface BjManchEmailEjbLocal {
    
    public void sendUserRegisteredEmail(Access access);
    public void sendAccessCreatedEmail(Access access);
    public void sendForumCreatedEmail(Access access, Forum forum);
    public void sendSurveyCreatedEmail(Access access, Survey survey);
    public void sendSurveyCreatedFromForumEmail(Access access, SurveyFromForum surveyFromForum);
    public void sendNewLokSabhaNominationEmail(User user, LokSabhaNominate lokSabhaNominate);
    public void sendLokSabhaReNominationEmail(User user, LokSabhaNominate lokSabhaNominate);
    public void sendNewVidhanSabhaNominationEmail(User user, VidhanSabhaNominate vidhanSabhaNominate);
    public void sendVidhanSabhaReNominationEmail(User user, VidhanSabhaNominate vidhanSabhaNominate);
    public void sendContactUsEmail(String adminEmail, String userEmail, String subject, String message);
    public void sendBlogCreatedEmail(Access access, Blog blog);
}
