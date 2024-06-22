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
import org.bjm.collections.SurveyVote;
import org.bjm.collections.VoteType;
import org.bjm.dtos.SurveyDto;
import org.bjm.utils.BjmConstants;
import org.bjm.utils.ImageVO;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;

/**
 *
 * @author singh
 */
@Named(value = "surveyDetailsMBean")
@ViewScoped
public class SurveyDetailsMBean implements Serializable {
    
    private static final Logger LOGGER= Logger.getLogger(SurveyDetailsMBean.class.getName());
    private int commentChars;
    private Survey survey;
    private Access surveyCreatorAccess;
    private SurveyVote surveyVote;
    private int votesTillDate;
    
    private String agreePct="0.0";
    private String disagreePct="0.0";
    private String undecidedPct="0.0";
    
    private List<SurveyVote> otherSurveyVotes;
        
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
        MongoCollection<Survey> surveyColl=mongoDatabase.getCollection("Survey", Survey.class);
        survey=surveyColl.find(filter).first();
        LOGGER.info(String.format("Survey loaded with ID: %s", survey.getId()));
        Bson filterSC=Filters.eq("email", survey.getSurveyCreatorEmail());
        MongoCollection<Access> accessColl=mongoDatabase.getCollection("Access", Access.class);
        surveyCreatorAccess=accessColl.find(filterSC).first();
        HttpSession session = request.getSession();
        session.setAttribute(BjmConstants.SURVEY_CREATOR_ACCESS, surveyCreatorAccess);
        surveyVote=new SurveyVote();
        loadOtherSurveyVotes();
    }
    
    public String postSurveyVote() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access = (Access) session.getAttribute("access");
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<SurveyVote> surveyVoteColl = mongoDatabase.getCollection("SurveyVote", SurveyVote.class);
        Bson filter = Filters.and(Filters.eq("surveyId", survey.getId()), Filters.eq("voterAccessId", access.getId()));
        SurveyVote userVote = surveyVoteColl.find(filter).first();
        if (userVote != null) {
            FacesContext.getCurrentInstance().addMessage("surveyVote", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Vote casted already. Contact Us to overide", "Vote casted already. Contact Us to overide"));
        } else {
            if (surveyVote.getComment() == null || surveyVote.getComment().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage("usercomment", new FacesMessage(FacesMessage.SEVERITY_ERROR, "No comment entered", "No comment entered"));
            } else {
                surveyVote.setSurveyId(survey.getId());
                surveyVote.setVoterAccessId(access.getId());
                surveyVote.setVoterEmail(access.getEmail());
                surveyVote.setDated(LocalDateTime.now());
                InsertOneResult surveyVoteResult = surveyVoteColl.insertOne(surveyVote);
                LOGGER.info(String.format("New Survey Vote created with ID: %s", surveyVoteResult.getInsertedId()));
                FacesContext.getCurrentInstance().addMessage("surveyVote", new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Vote casted successfully!!", "Vote casted successfully!!"));
            }

        }
        return null;
    }
    
    private void loadOtherSurveyVotes(){
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<SurveyVote> surveyVoteColl = mongoDatabase.getCollection("SurveyVote", SurveyVote.class);
        Bson filter = Filters.eq("surveyId", survey.getId());
        Iterable<SurveyVote> surveyVoteItrble=surveyVoteColl.find(filter);
        Iterator<SurveyVote> surveyVoteItr = surveyVoteItrble.iterator();
        otherSurveyVotes=new ArrayList<>();
        Map<ObjectId, ImageVO> surveyVotersImageMap=new HashMap<>();
        int agreeCt=0;
        int disagreeCt=0;
        int undecidedCt=0;
        while(surveyVoteItr.hasNext()){
            SurveyVote sv=surveyVoteItr.next();
            otherSurveyVotes.add(sv);
            if(sv.getVoteType().equals(VoteType.AGREE.toString())){
                agreeCt++;
            }else if(sv.getVoteType().equals(VoteType.DISAGREE.toString())){
                disagreeCt++;
            }else if(sv.getVoteType().equals(VoteType.UNDECIDED.toString())){
                undecidedCt++;
            }
            Bson filterAccess=Filters.eq("email", sv.getVoterEmail());
            MongoCollection<Access> accessColl=mongoDatabase.getCollection("Access", Access.class);
            Access surveyVoterAccess=accessColl.find(filterAccess).first();
            String imageType=surveyVoterAccess.getProfileFile().substring(surveyVoterAccess.getProfileFile().indexOf('.')+1);
            ImageVO surveyVoterImageVO=new ImageVO(imageType, surveyVoterAccess.getImage().getData());
            surveyVotersImageMap.put(surveyVoterAccess.getId(), surveyVoterImageVO);
            
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
        session.setAttribute(BjmConstants.SURVEY_VOTER_IMAGE_MAP, surveyVotersImageMap);
        votesTillDate=otherSurveyVotes.size();
        
        
        
    }

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    public SurveyVote getSurveyVote() {
        return surveyVote;
    }

    public void setSurveyVote(SurveyVote surveyVote) {
        this.surveyVote = surveyVote;
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

    public List<SurveyVote> getOtherSurveyVotes() {
        loadOtherSurveyVotes();
        return otherSurveyVotes;
    }

    public void setOtherSurveyVotes(List<SurveyVote> otherSurveyVotes) {
        this.otherSurveyVotes = otherSurveyVotes;
    }
    
    
    
    
}
