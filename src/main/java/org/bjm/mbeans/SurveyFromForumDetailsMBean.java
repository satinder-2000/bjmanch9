package org.bjm.mbeans;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bjm.collections.Access;
import org.bjm.collections.Survey;
import org.bjm.collections.SurveyFromForum;
import org.bjm.collections.SurveyFromForumVote;
import org.bjm.collections.SurveyVote;
import org.bjm.collections.VoteType;
import org.bjm.utils.BjmConstants;
import org.bjm.utils.ImageVO;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
@Named(value = "surveyFromForumDetailsMBean")
@ViewScoped
public class SurveyFromForumDetailsMBean implements Serializable{
    
    private static final Logger LOGGER = Logger.getLogger(SurveyFromForumDetailsMBean.class.getName());
    private int commentChars;
    private SurveyFromForum surveyFromForum;
    private Access surveyFromForumCreatorAccess;
    private SurveyFromForumVote surveyFromForumVote;
    private List<SurveyFromForumVote> otherSurveyFromForumVotes;
    
    private int votesTillDate;
    private String agreePct="0.0";
    private String disagreePct="0.0";
    private String undecidedPct="0.0";
    
    
    @PostConstruct
    public void init(){
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String surveyIdStr=request.getParameter("surveyId");
        ObjectId surveyId = new ObjectId(surveyIdStr);
        Bson filter = Filters.eq("_id", surveyId);
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<SurveyFromForum> surveyFromForumColl=mongoDatabase.getCollection("SurveyFromForum", SurveyFromForum.class);
        surveyFromForum=surveyFromForumColl.find(filter).first();
        LOGGER.info(String.format("SurveyFromForum loaded with ID: %s", surveyFromForum.getId()));
        Bson filterSC=Filters.eq("email", surveyFromForum.getSurveyCreatorEmail());
        MongoCollection<Access> accessColl=mongoDatabase.getCollection("Access", Access.class);
        surveyFromForumCreatorAccess=accessColl.find(filterSC).first();
        HttpSession session = request.getSession();
        session.setAttribute(BjmConstants.SURVEY_FROM_FORUM_CREATOR_ACCESS, surveyFromForumCreatorAccess);
        surveyFromForumVote=new SurveyFromForumVote();
    }
    
    public String postSurveyFromForumVote(){
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access = (Access) session.getAttribute("access");
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<SurveyFromForumVote> surveyFromForumVoteColl = mongoDatabase.getCollection("SurveyFromForumVote", SurveyFromForumVote.class);
        Bson filter = Filters.and(Filters.eq("surveyFromForumId", surveyFromForum.getId()), Filters.eq("voterAccessId", access.getId()));
        SurveyFromForumVote userVote = surveyFromForumVoteColl.find(filter).first();
        if (userVote != null) {
            FacesContext.getCurrentInstance().addMessage("surveyFromForume", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Vote casted already. Contact Us to overide", "Vote casted already. Contact Us to overide"));
        } else {
            if (surveyFromForumVote.getComment() == null || surveyFromForumVote.getComment().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage("usercomment", new FacesMessage(FacesMessage.SEVERITY_ERROR, "No comment entered", "No comment entered"));
            } else {
                surveyFromForumVote.setSurveyFromForumId(surveyFromForum.getId());
                surveyFromForumVote.setVoterAccessId(access.getId());
                surveyFromForumVote.setVoterEmail(access.getEmail());
                surveyFromForumVote.setDated(LocalDateTime.now());
                InsertOneResult surveyFromForumVoteResult = surveyFromForumVoteColl.insertOne(surveyFromForumVote);
                LOGGER.info(String.format("New SurveyFromForum Vote created with ID: %s", surveyFromForumVoteResult.getInsertedId()));
                FacesContext.getCurrentInstance().addMessage("surveyFromForumVote", new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Vote casted successfully!!", "Vote casted successfully!!"));
            }

        }
        return null;
    }
    
    private void loadOtherSurveyFromForumVotes(){
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<SurveyFromForumVote> surveyFromForumVoteColl = mongoDatabase.getCollection("SurveyFromForumVote", SurveyFromForumVote.class);
        Bson filter = Filters.eq("surveyFromForumId", surveyFromForum.getId());
        Iterable<SurveyFromForumVote> surveyFromForumVoteItrble=surveyFromForumVoteColl.find(filter);
        Iterator<SurveyFromForumVote> surveyFromForumVoteItr = surveyFromForumVoteItrble.iterator();
        otherSurveyFromForumVotes=new ArrayList<>();
        Map<ObjectId, ImageVO> surveyFromForumVotersImageMap=new HashMap<>();
        int agreeCt=0;
        int disagreeCt=0;
        int undecidedCt=0;
        while(surveyFromForumVoteItr.hasNext()){
            SurveyFromForumVote sv=surveyFromForumVoteItr.next();
            otherSurveyFromForumVotes.add(sv);
            if(sv.getVoteType().equals(VoteType.AGREE.toString())){
                agreeCt++;
            }else if(sv.getVoteType().equals(VoteType.DISAGREE.toString())){
                disagreeCt++;
            }else if(sv.getVoteType().equals(VoteType.UNDECIDED.toString())){
                undecidedCt++;
            }
            Bson filterAccess=Filters.eq("email", sv.getVoterEmail());
            MongoCollection<Access> accessColl=mongoDatabase.getCollection("Access", Access.class);
            Access surveyFromForumVoterAccess=accessColl.find(filterAccess).first();
            String imageType=surveyFromForumVoterAccess.getProfileFile().substring(surveyFromForumVoterAccess.getProfileFile().indexOf('.')+1);
            ImageVO surveyFromForumVoterImageVO=new ImageVO(imageType, surveyFromForumVoterAccess.getImage().getData());
            surveyFromForumVotersImageMap.put(surveyFromForumVoterAccess.getId(), surveyFromForumVoterImageVO);
            
            //Percentages now
            int total = agreeCt + disagreeCt + undecidedCt;
            double agree = agreeCt * 100 / total;
            double disagree = disagreeCt * 100 / total;
            double undecided = undecidedCt * 100 / total;
            agreePct = String.valueOf(agree);
            disagreePct = String.valueOf(disagree);
            undecidedPct = String.valueOf(undecided);
        }
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        session.setAttribute(BjmConstants.SURVEY_FROM_FORUM_VOTER_IMAGE_MAP, surveyFromForumVotersImageMap);
        votesTillDate=otherSurveyFromForumVotes.size();
        
        
        
    }

    public SurveyFromForum getSurveyFromForum() {
        return surveyFromForum;
    }

    public void setSurveyFromForum(SurveyFromForum surveyFromForum) {
        this.surveyFromForum = surveyFromForum;
    }

    public SurveyFromForumVote getSurveyFromForumVote() {
        return surveyFromForumVote;
    }

    public void setSurveyFromForumVote(SurveyFromForumVote surveyFromForumVote) {
        this.surveyFromForumVote = surveyFromForumVote;
    }

    public List<SurveyFromForumVote> getOtherSurveyFromForumVotes() {
        loadOtherSurveyFromForumVotes();
        return otherSurveyFromForumVotes;
    }

    public void setOtherSurveyFromForumVotes(List<SurveyFromForumVote> otherSurveyFromForumVotes) {
        this.otherSurveyFromForumVotes = otherSurveyFromForumVotes;
    }
    
    

    public int getCommentChars() {
        return commentChars;
    }

    public void setCommentChars(int commentChars) {
        this.commentChars = commentChars;
    }

    public int getVotesTillDate() {
        return votesTillDate;
    }

    public void setVotesTillDate(int votesTillDate) {
        this.votesTillDate = votesTillDate;
    }

    public String getAgreePct() {
        return agreePct;
    }

    public void setAgreePct(String agreePct) {
        this.agreePct = agreePct;
    }

    public String getDisagreePct() {
        return disagreePct;
    }

    public void setDisagreePct(String disagreePct) {
        this.disagreePct = disagreePct;
    }

    public String getUndecidedPct() {
        return undecidedPct;
    }

    public void setUndecidedPct(String undecidedPct) {
        this.undecidedPct = undecidedPct;
    }
    
    
    
}
