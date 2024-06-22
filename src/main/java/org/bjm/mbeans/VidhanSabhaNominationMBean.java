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
import org.bjm.collections.State;
import org.bjm.collections.User;
import org.bjm.collections.VidhanSabhaNominate;
import org.bjm.ejbs.EmailEjbLocal;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

/**
 *
 * @author singh
 */
@Named(value = "vidhanSabhaNominationMBean")
@FlowScoped(value = "VidhanSabhaNominate")
public class VidhanSabhaNominationMBean implements Serializable{
    
    private static final Logger LOGGER = Logger.getLogger(VidhanSabhaNominationMBean.class.getName());
    
    private User user;
    private VidhanSabhaNominate vidhanSabhaNominate;
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
        CodecRegistry pojoCodecRegistery=fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistery);
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute("access");
        Bson filerEmail=Filters.eq("email", access.getEmail());
        MongoCollection<User> userColl=mongoDatabase.getCollection("User", User.class);
        user=userColl.find(filerEmail).first();
        Bson filterState=Filters.eq("code", user.getStateCode());
        MongoCollection<State> stateColl=mongoDatabase.getCollection("State", State.class);
        state=stateColl.find(filterState).first();
        nominatedCandidates=new ArrayList<>();
        if(!user.getVidhanSabha().isEmpty()){//For UT's like CH there is no VidhanSabha
            Bson filterVsNom=Filters.and(Filters.eq("stateCode", user.getStateCode()),Filters.eq("constituency", user.getVidhanSabha()));
            MongoCollection<VidhanSabhaNominate> vidhanSabhaNomColl=mongoDatabase.getCollection("VidhanSabhaNominate", VidhanSabhaNominate.class);
            Iterable<VidhanSabhaNominate> vidhanSabhaNomItrble=vidhanSabhaNomColl.find(filterVsNom);
            Iterator<VidhanSabhaNominate> vidhanSabhaItr=vidhanSabhaNomItrble.iterator();
            nominatedCandidates.add("--Select One--");
            while(vidhanSabhaItr.hasNext()){
                nominatedCandidates.add(vidhanSabhaItr.next().getCandidateName());
            }
            LOGGER.info(String.format("Number of Vidhan Sabha Nominated candidates for User's Constitueny  %s in StateCode %s id %d", user.getVidhanSabha(),user.getStateCode(),nominatedCandidates.size()));
        }else{
            nominatedCandidates.add("No Vidhan Sabha for "+user.getStateCode());
        }
        fuzzyCandidates=new ArrayList<>();
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
                    return "VidhanSabhaNominateConfirm?faces-redirect=true";
                }
            }
            
            //Scenario 2a: User picks from the list - candidate record incremented.
           if(!candidateSelected.equals("--Select One--") && candidateNew.isEmpty()){
               LOGGER.info(String.format("User %s has nominated Candidate for Constituency %s is %s", user.getEmail(), candidateSelected, user.getVidhanSabha()));
               preSubmitSelection();
               return "VidhanSabhaNominateConfirm?faces-redirect=true";
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
                return "VidhanSabhaNominateConfirm?faces-redirect=true";
            }
        
        }else if(reloaded){
            //Scenario 3a: User accepts the item  from the fuzzy match - candidate record incremented.
            if(!candidateSelected.equals("--Select One--") && !forceNomination){
                LOGGER.info(String.format("User %1$s has nominated Candidate for Constituency %2$s is %3$s", user.getEmail(), candidateSelected, user.getVidhanSabha()));
                candidateNew="";
                preSubmitSelection();
                return "VidhanSabhaNominateConfirm?faces-redirect=true";
            }//Scenario 3b: User forces the Nimination if the Candidate and it takes priority.
            else if (forceNomination && !candidateNew.isEmpty()) {
                candidateSelected = " ";
                LOGGER.info(String.format("Forced Nominated Candidate for Constituency %s is %s by User %s", user.getVidhanSabha(), newNomination, user.getEmail()));
                newNomination = true;
                preSubmitNomination();//just prepating the data here, without actual submit in the DB.
                return "VidhanSabhaNominateConfirm?faces-redirect=true";
            }
        
        }
        return null;
    }
    
    
    private void preSubmitNomination(){
        vidhanSabhaNominate = new VidhanSabhaNominate();
        vidhanSabhaNominate.setStateCode(user.getStateCode());
        vidhanSabhaNominate.setConstituency(user.getVidhanSabha());
        vidhanSabhaNominate.setCandidateName(candidateNew);
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute("access");
        vidhanSabhaNominate.setNominatedByAccessId(access.getId());
        vidhanSabhaNominate.setNominatedByEmail(access.getEmail());
        vidhanSabhaNominate.setNominationCount(1);
        vidhanSabhaNominate.setNominatedOn(LocalDateTime.now());
        newNomination=true; //Will need this flag during submit.
    }
    
    private void preSubmitSelection(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry= fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<VidhanSabhaNominate> vidhanSabhaNomiColl=mongoDatabase.getCollection("VidhanSabhaNominate", VidhanSabhaNominate.class);
        Bson filter=Filters.and(Filters.eq("stateCode", user.getStateCode()),Filters.eq("constituency", user.getVidhanSabha()),
                Filters.eq("candidateName", candidateSelected));
        vidhanSabhaNominate= vidhanSabhaNomiColl.find(filter).first();
        vidhanSabhaNominate.setNominationCount(vidhanSabhaNominate.getNominationCount()+1);
    }
    
    private void submitNomination(){
        ServletContext servletContext=(ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider=PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry= fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<VidhanSabhaNominate> vidhanSabhaNomiColl=mongoDatabase.getCollection("VidhanSabhaNominate", VidhanSabhaNominate.class);
            
        if(newNomination){//flag set in preSubmitNomination
            InsertOneResult insertOneResult= vidhanSabhaNomiColl.insertOne(vidhanSabhaNominate);
            LOGGER.info(String.format("StateCode %s - VidhanSabha %s - new Nominated created with ID: %s", user.getStateCode(),user.getVidhanSabha(),
                    insertOneResult.getInsertedId()));
            emailEjbLocal.sendNewVidhanSabhaNominationEmail(user, vidhanSabhaNominate);
        }else{
            Bson filter=Filters.eq("_id", vidhanSabhaNominate.getId());
            UpdateResult updateResult=vidhanSabhaNomiColl.replaceOne(filter, vidhanSabhaNominate);
            LOGGER.info(String.format("VidhanSabhaNominate with ID %s now has nominationCount of %d", vidhanSabhaNominate.getId().toHexString(), vidhanSabhaNominate.getNominationCount()));
            emailEjbLocal.sendVidhanSabhaReNominationEmail(user, vidhanSabhaNominate);
        }
    }
    
    public String getReturnValue(){
        submitNomination();
        return "/flowreturns/VidhanSabhaNominate-return?faces-redirect=true";
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public VidhanSabhaNominate getVidhanSabhaNominate() {
        return vidhanSabhaNominate;
    }

    public void setVidhanSabhaNominate(VidhanSabhaNominate vidhanSabhaNominate) {
        this.vidhanSabhaNominate = vidhanSabhaNominate;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public List<String> getConstituencies() {
        return constituencies;
    }

    public void setConstituencies(List<String> constituencies) {
        this.constituencies = constituencies;
    }

    public List<String> getFuzzyCandidates() {
        return fuzzyCandidates;
    }

    public void setFuzzyCandidates(List<String> fuzzyCandidates) {
        this.fuzzyCandidates = fuzzyCandidates;
    }

    

    public List<String> getNominatedCandidates() {
        return nominatedCandidates;
    }

    public void setNominatedCandidates(List<String> nominatedCandidates) {
        this.nominatedCandidates = nominatedCandidates;
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
