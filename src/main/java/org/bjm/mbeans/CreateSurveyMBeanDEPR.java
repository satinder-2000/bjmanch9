package org.bjm.mbeans;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.flow.FlowScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import org.bjm.collections.Access;
import org.bjm.collections.Activity;
import org.bjm.collections.ActivityType;
import org.bjm.collections.Survey;
import org.bjm.collections.SurveyCategory;
import org.bjm.dtos.SurveyDto;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bjm.ejbs.BjManchEmailEjbLocal;

/**
 *
 * @author singh
 */
@Named(value = "createSurveyMBeanDEPR")
@FlowScoped(value = "CreateSurveyDEPR")
public class CreateSurveyMBeanDEPR implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(CreateSurveyMBeanDEPR.class.getName());
    
    private SurveyDto surveyDto;
    
    @Inject
    private ActivityMBean activityMBean;
    @Inject
    private BjManchEmailEjbLocal emailEjbLocal;
    
    private int surveyDescriptionChars;
    
    @PostConstruct
    public void init(){
        surveyDto=new SurveyDto();
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<SurveyCategory> surveyCatColl=mongoDatabase.getCollection("SurveyCategory", SurveyCategory.class);
        Iterable<SurveyCategory> surveyCatItrble=surveyCatColl.find();
        Iterator<SurveyCategory> surveyCatItr=surveyCatItrble.iterator();
        surveyDto.setSurveyCategoryMap(new HashMap<>());
        surveyDto.getSurveyCategoryMap().put("--Select One--", null);
        while(surveyCatItr.hasNext()){
            SurveyCategory sc=surveyCatItr.next();
            Set<String> mapKeys=surveyDto.getSurveyCategoryMap().keySet();
            if(!mapKeys.contains(sc.getType())){
                Set<String> valueSet=new HashSet<>();
                valueSet.add("--Select One--");
                valueSet.add(sc.getSubType());
                surveyDto.getSurveyCategoryMap().put(sc.getType(), valueSet);
            }else{
                Set<String> valueSet=surveyDto.getSurveyCategoryMap().get(sc.getType());
                valueSet.add(sc.getSubType());
            }
        }
        surveyDto.setCategoryTypes(surveyDto.getSurveyCategoryMap().keySet());
        LOGGER.info(String.format("SurveyDto surveyCatgoryMap populated with %d records", surveyDto.getSurveyCategoryMap().size()));
    }
    
    public void ajaxTypeListener(AjaxBehaviorEvent abe){
        surveyDto.setCategorySubTypes(surveyDto.getSurveyCategoryMap().get(surveyDto.getCategoryType()));
    }

    public String prepareSurvey(){
        return "CreateSurveyConfirm?faces-redirect=true";
    }
    
    public String amendSurvey(){
        return "CreateSurvey?faces-redirect=true";
    }
    
    private void submitSurvey(){
        Survey survey=new Survey();
        survey.setCategoryType(surveyDto.getCategoryType());
        survey.setCategorySubType(surveyDto.getCategorySubType());
        survey.setTitle(surveyDto.getTitle());
        survey.setDescription(surveyDto.getDescription());
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session= request.getSession();
        Access access=(Access) session.getAttribute("access");
        survey.setSurveyCreatorEmail(access.getEmail());
        survey.setCreatedOn(LocalDateTime.now());
        survey.setUpdatedOn(LocalDateTime.now());
        
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Survey> surveyColl = mongoDatabase.getCollection("Survey", Survey.class);
        InsertOneResult surveyResult =surveyColl.insertOne(survey);
        LOGGER.info(String.format("Survey created with ID: %s",surveyResult.getInsertedId()));
        
        //Activity now
        Activity activity=new Activity();
        activity.setActivityType(ActivityType.SURVEY_CREATED.toString());
        activity.setActivityId(surveyResult.getInsertedId().asObjectId().getValue());
        activity.setDescription("Survey Created "+survey.getTitle());
        activity.setOwnerEmail(access.getEmail());
        activity.setCreatedOn(LocalDateTime.now());
        activityMBean.addActivity(activity);
        //Last Step - send Email
        emailEjbLocal.sendSurveyCreatedEmail(access, survey);
        surveyDto=new SurveyDto();//To erase the previous data as we are working in a session.
    }
    
    public String getReturnValue(){
        submitSurvey();
        return "/flowreturns/CreateSurvey-return?faces-redirect=true";
    }
    
    public SurveyDto getSurveyDto() {
        return surveyDto;
    }

    public void setSurveyDto(SurveyDto surveyDto) {
        this.surveyDto = surveyDto;
    }

    public int getSurveyDescriptionChars() {
        return surveyDescriptionChars;
    }

    public void setSurveyDescriptionChars(int surveyDescriptionChars) {
        this.surveyDescriptionChars = surveyDescriptionChars;
    }

    
}
