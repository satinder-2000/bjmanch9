package org.bjm.mbeans;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.FlowScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import org.apache.commons.text.similarity.FuzzyScore;
import org.bjm.collections.Access;
import org.bjm.collections.LokSabha;
import org.bjm.collections.LokSabhaNominate;
import org.bjm.collections.State;
import org.bjm.collections.User;
import org.bjm.ejbs.EmailEjbLocal;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

/**
 *
 * @author singh
 */
@Named(value = "lokSabhaNominationMBean")
@FlowScoped(value = "LokSabhaNominate")
public class LokSabhaNominationMBean implements Serializable {
    
    private static final Logger LOGGER=Logger.getLogger(LokSabhaNominationMBean.class.getName());
    
    private User user;
    private LokSabhaNominate lokSabhaNominate;
    private State state;
    private List<String> constituencies;
    private List<String> fuzzyCandidates;
    private List<String> nominatedCandidates;
    
    private String candidateSelected;
    private String candidateNew;
    private boolean reloaded;
    private boolean newNomination;
    private boolean forceNomination;
    
    
    
    @Inject
    private EmailEjbLocal emailEjbLocal;
    
    @PostConstruct
    public void init(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry= fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute("access");
        Bson filterEmail=Filters.eq("email", access.getEmail());
        MongoCollection<User> userColl=mongoDatabase.getCollection("User", User.class);
        user=userColl.find(filterEmail).first();
        Bson filterState=Filters.eq("code", user.getStateCode());
        MongoCollection<State> stateColl=mongoDatabase.getCollection("State", State.class);
        state=stateColl.find(filterState).first();
        //Bson filterStateCode=Filters.and(Filters.eq("stateCode", user.getStateCode(),Filters.eq("constituency", user.getLokSabha()));
        Bson filterLsNom=Filters.and(Filters.eq("stateCode", user.getStateCode()), Filters.eq("constituency", user.getLokSabha()));
        MongoCollection<LokSabhaNominate> lokSabhaNomiColl=mongoDatabase.getCollection("LokSabhaNominate", LokSabhaNominate.class);
        Iterable<LokSabhaNominate> lokSabhaNomiItrble=lokSabhaNomiColl.find(filterLsNom);
        Iterator<LokSabhaNominate> lokSabhaNomiItr=lokSabhaNomiItrble.iterator();
        nominatedCandidates=new ArrayList<>();
        if(lokSabhaNomiItr.hasNext()){
            nominatedCandidates.add("--Select One--");
            while(lokSabhaNomiItr.hasNext()){
                nominatedCandidates.add(lokSabhaNomiItr.next().getCandidateName());
            }
        }else{
            nominatedCandidates.add("--None Found--");
        }
        fuzzyCandidates=new ArrayList<>();
        LOGGER.info(String.format("Number of Lok Sabha Nominated candidates for User's Constitueny  %s in StateCode %s id %d", user.getLokSabha(),user.getStateCode(),nominatedCandidates.size()));
    }
    
    /**
     * The following logic has been coded in this method. 
     * Step 1: No candidate has been nominated so far and the newNomination is the first one.
     *
     * Step 2a: User picks the existing Candidate and nominates it - the
     * nominationCount field is incremented by 1.
     *
     * Step 2b: User's newNomination clears the fuzzy match and the
     * newNomination is added as a Candidate.
     *
     * Step 2c: newNomination closely matches the existing name(s) and are
     * displayed on the front end for User to take further action (flag reloaded
     * is also set here)
     *
     * Step 3a: From the Step 2c, User accepts the existing Candidate and
     * nominates it. the nominationCount field is incremented by 1.
     *
     * Step 3b: User forces the newNomination and it is added as a new Candidate.
     *
     * @return
     */
    public String processNomination(){
        if((candidateSelected.equals("--Select One--")||candidateSelected.equals("--None Found--")) && candidateNew.isEmpty()){
            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Either select a Candidate or Nominate another.", "Either select a Candidate or Nominate another."));
            return null;
        }
        if(!reloaded){ //These scenarios will take place in the main path of nomination i.e before the page reloading
            //Scenario 1: No nomination made so far, hence, this is the first one.
            if (nominatedCandidates.size()==1){//the first and only value is "--None Found--"
                if (!candidateNew.isEmpty()){
                    newNomination=true;
                    candidateSelected = "";
                    preSubmitNomination();
                    return "LokSabhaNominateConfirm?faces-redirect=true";
                }
            }
            
            //Scenario 2a: User picks from the list - candidate record incremented.
           if(!candidateSelected.equals("--Select One--") && candidateNew.isEmpty()){
               LOGGER.info(String.format("User %s has nominated Candidate for Constituency %s is %s", user.getEmail(), candidateSelected, user.getLokSabha()));
               preSubmitSelection();
               return "LokSabhaNominateConfirm?faces-redirect=true";
           } 
           //Scenario 2b: User's newNomination fails the fizzy test (score>10)  All the fizzyCandidates (the matches ones)
           //are displayed on the front end for the User to make desicion.
           fuzzyCandidates.clear();
           FuzzyScore fuzzyScore=new FuzzyScore(Locale.ENGLISH);
           for(String ec:nominatedCandidates){
               int score=fuzzyScore.fuzzyScore(ec, candidateNew);
               if(score>10){
                   fuzzyCandidates.add(ec);
               }
           }
           
           if (!fuzzyCandidates.isEmpty()) {
                reloaded = true;
                return null;
            } else {
                candidateSelected = " ";
                newNomination = true;
                preSubmitNomination();//just preparing the data here, without actual submit in the DB.
                return "LokSabhaNominateConfirm?faces-redirect=true";
            }
        
        }else if(reloaded){
            //Scenario 3a: User accepts the item  from the fuzzy match - candidate record incremented.
            if(!candidateSelected.equals("--Select One--") && !forceNomination){
                LOGGER.info(String.format("User %1$s has nominated Candidate for Constituency %2$s is %3$s", user.getEmail(), candidateSelected, user.getLokSabha()));
                candidateNew="";
                preSubmitSelection();
                return "LokSabhaNominateConfirm?faces-redirect=true";
            }//Scenario 3b: User forces the Nimination if the Candidate and it takes priority.
            else if (forceNomination && !candidateNew.isEmpty()) {
                candidateSelected = " ";
                LOGGER.info(String.format("Forced Nominated Candidate for Constituency %s is %s by User %s", user.getLokSabha(), newNomination, user.getEmail()));
                newNomination = true;
                preSubmitNomination();//just prepating the data here, without actual submit in the DB.
                return "LokSabhaNominateConfirm?faces-redirect=true";
            }
        
        }
        return null;
    }
    
    private void preSubmitNomination(){
        lokSabhaNominate = new LokSabhaNominate();
        lokSabhaNominate.setStateCode(user.getStateCode());
        lokSabhaNominate.setConstituency(user.getLokSabha());
        lokSabhaNominate.setCandidateName(candidateNew);
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute("access");
        lokSabhaNominate.setNominatedByAccessId(access.getId());
        lokSabhaNominate.setNominatedByEmail(access.getEmail());
        lokSabhaNominate.setNominationCount(1);
        lokSabhaNominate.setNominatedOn(LocalDateTime.now());
        newNomination=true; //Will need this flag during submit.
    }
    
    private void preSubmitSelection(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry= fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<LokSabhaNominate> lokSabhaNomiColl=mongoDatabase.getCollection("LokSabhaNominate", LokSabhaNominate.class);
        Bson filter=Filters.and(Filters.eq("stateCode", user.getStateCode()),Filters.eq("constituency", user.getLokSabha()),
                Filters.eq("candidateName", candidateSelected));
        lokSabhaNominate= lokSabhaNomiColl.find(filter).first();
        lokSabhaNominate.setNominationCount(lokSabhaNominate.getNominationCount()+1);
    }
    
    private void submitNomination(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry= fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<LokSabhaNominate> lokSabhaNomiColl=mongoDatabase.getCollection("LokSabhaNominate", LokSabhaNominate.class);
            
        if(newNomination){//flag set in preSubmitNomination
            InsertOneResult insertOneResult= lokSabhaNomiColl.insertOne(lokSabhaNominate);
            LOGGER.info(String.format("StateCode %s - LokSabha %s - new Nominated created with ID: %s", user.getStateCode(),user.getLokSabha(),
                    insertOneResult.getInsertedId()));
            emailEjbLocal.sendNewLokSabhaNominationEmail(user, lokSabhaNominate);
        }else{
            Bson filter=Filters.eq("_id", lokSabhaNominate.getId());
            UpdateResult updateResult=lokSabhaNomiColl.replaceOne(filter, lokSabhaNominate);
            LOGGER.info(String.format("LokSabhaNominate with ID %s now has nominationCount of %d", lokSabhaNominate.getId().toHexString(), lokSabhaNominate.getNominationCount()));
            emailEjbLocal.sendLokSabhaReNominationEmail(user, lokSabhaNominate);
        }
    }
    
    public String getReturnValue(){
        submitNomination();
        return "/flowreturns/LokSabhaNominate-return?faces-redirect=true";
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    
    
    public List<String> getNominatedCandidates() {
        return nominatedCandidates;
    }

    public void setNominatedCandidates(List<String> nominatedCandidates) {
        this.nominatedCandidates = nominatedCandidates;
    }

    public List<String> getFuzzyCandidates() {
        return fuzzyCandidates;
    }

    public void setFuzzyCandidates(List<String> fuzzyCandidates) {
        this.fuzzyCandidates = fuzzyCandidates;
    }

    public String getCandidateSelected() {
        return candidateSelected;
    }

    public void setCandidateSelected(String candidateSelected) {
        this.candidateSelected = candidateSelected;
    }

    public String getCandidateNew() {
        return candidateNew;
    }

    public void setCandidateNew(String candidateNew) {
        this.candidateNew = candidateNew;
    }

    public boolean isReloaded() {
        return reloaded;
    }

    public void setReloaded(boolean reloaded) {
        this.reloaded = reloaded;
    }

    public boolean isNewNomination() {
        return newNomination;
    }

    public void setNewNomination(boolean newNomination) {
        this.newNomination = newNomination;
    }

    public boolean isForceNomination() {
        return forceNomination;
    }

    public void setForceNomination(boolean forceNomination) {
        this.forceNomination = forceNomination;
    }

}
