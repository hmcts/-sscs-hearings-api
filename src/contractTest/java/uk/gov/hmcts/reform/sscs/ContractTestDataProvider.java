package uk.gov.hmcts.reform.sscs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseCategory;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDeleteRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingWindow;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcHearingLocation;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcHearingCaseDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcHearingDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcHearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcRequestDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.IndividualDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.OrganisationDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelRequirements;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RelatedParty;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityDayOfWeek;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityRange;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ContractTestDataProvider {

    public static final String CONSUMER_NAME = "hmcHearingServiceConsumer";

    protected static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    protected static final String IDAM_OAUTH2_TOKEN = "pact-test-idam-token";
    protected static final String UNAUTHORISED_IDAM_OAUTH2_TOKEN = "unauthorised-pact-test-idam-token";
    protected static final String SERVICE_AUTHORIZATION_TOKEN = "pact-test-s2s-token";
    protected static final String UNAUTHORISED_SERVICE_AUTHORIZATION_TOKEN = "unauthorised-pact-test-s2s-token";

    public static final String MSG_200_HEARING = "Success (with content)";
    public static final String MSG_400_HEARING = "Invalid hearing state for DELETE";
    public static final String MSG_401_HEARING = "Unauthorised request";
    public static final String MSG_403_HEARING = "Forbidden request";
    public static final String MSG_404_HEARING = "Not Found request";

    protected static final String CONTENT_TYPE = "Content-Type";
    protected static final String APPLICATION_JSON = "application/json";

    protected static final String HEARING_PATH = "/hearing";
    protected static final String FIELD_STATUS = "status";
    protected static final String BAD_REQUEST = "BAD_REQUEST";
    protected static final String FIELD_MESSAGE = "message";
    protected static final String FIELD_ERRORS = "errors";
    protected static final int ZERO_LENGTH = 0;
    protected static final Number ZERO_NUMBER_LENGTH = 0;

    protected static final Map<String, String> authorisedHeaders = Map.of(
        HttpHeaders.AUTHORIZATION, IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, SERVICE_AUTHORIZATION_TOKEN,
        CONTENT_TYPE, APPLICATION_JSON
    );

    protected static final Map<String, String> unauthorisedHeaders = Map.of(
        HttpHeaders.AUTHORIZATION, UNAUTHORISED_IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, UNAUTHORISED_SERVICE_AUTHORIZATION_TOKEN,
        CONTENT_TYPE, APPLICATION_JSON
    );

    protected HmcHearingRequestPayload generateHearingRequest() {
        HmcHearingRequestPayload request = new HmcHearingRequestPayload();
        request.setHmcRequestDetails(requestDetails());
        request.setHmcHearingDetails(hearingDetails());
        request.setHmcHearingCaseDetails(caseDetails());
        request.setPartiesDetails(partyDetails1());

        return request;
    }

    protected HmcHearingRequestPayload generateInvalidHearingRequest() {
        HmcHearingRequestPayload request = new HmcHearingRequestPayload();
        request.setHmcHearingDetails(hearingDetails());
        request.setPartiesDetails(partyDetails1());
        request.setHmcRequestDetails(requestDetails());
        return request;
    }

    protected HearingDeleteRequestPayload generateHearingDeleteRequest() {
        HearingDeleteRequestPayload request = new HearingDeleteRequestPayload();
        request.setCancellationReasonCode("Cancel reason");
        return request;
    }

    protected HearingDeleteRequestPayload generateInvalidHearingDeleteRequest() {
        HearingDeleteRequestPayload request = new HearingDeleteRequestPayload();
        request.setCancellationReasonCode("");
        return request;
    }

    protected String toJsonString(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String jsonString = "";
        try {
            jsonString = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    protected HmcRequestDetails requestDetails() {
        HmcRequestDetails hmcRequestDetails = new HmcRequestDetails();
        hmcRequestDetails.setRequestTimeStamp(LocalDateTime.parse("2022-03-17T14:08:41"));
        hmcRequestDetails.setVersionNumber(123);
        return hmcRequestDetails;
    }

    protected HmcHearingDetails hearingDetails() {
        HmcHearingDetails hmcHearingDetails = new HmcHearingDetails();
        hmcHearingDetails.setAutolistFlag(true);
        hmcHearingDetails.setHearingType("Some hearing type");
        hmcHearingDetails.setHearingWindow(hearingWindow());
        hmcHearingDetails.setDuration(1);
        hmcHearingDetails.setNonStandardHearingDurationReasons(Arrays.asList("First reason", "Second reason"));
        hmcHearingDetails.setHearingPriorityType("Priority type");
        HmcHearingLocation location1 = new HmcHearingLocation();
        location1.setLocationId("court");
        location1.setLocationType("Location type");
        List<HmcHearingLocation> hmcHearingLocations = new ArrayList<>();
        hmcHearingLocations.add(location1);
        hmcHearingDetails.setHmcHearingLocations(hmcHearingLocations);
        hmcHearingDetails.setPanelRequirements(panelRequirements1());
        return hmcHearingDetails;
    }

    protected HearingWindow hearingWindow() {
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setDateRangeStart(LocalDate.parse("2020-02-01"));
        hearingWindow.setDateRangeEnd(LocalDate.parse("2020-02-12"));

        return hearingWindow;
    }

    protected HmcHearingCaseDetails caseDetails() {
        HmcHearingCaseDetails hmcHearingCaseDetails = new HmcHearingCaseDetails();
        hmcHearingCaseDetails.setHmctsServiceCode("ABBA1");
        hmcHearingCaseDetails.setCaseRef("ba12");
        hmcHearingCaseDetails.setRequestTimeStamp(LocalDateTime.parse("2022-03-17T14:08:41"));
        hmcHearingCaseDetails.setCaseDeepLink("https://www.google.com");
        hmcHearingCaseDetails.setHmctsInternalCaseName("Internal case name");
        hmcHearingCaseDetails.setPublicCaseName("Public case name");
        hmcHearingCaseDetails.setCaseManagementLocationCode("CMLC123");
        hmcHearingCaseDetails.setCaseRestrictedFlag(false);
        hmcHearingCaseDetails.setCaseSlaStartDate("2030-08-20");
        CaseCategory category = new CaseCategory();
        category.setCategoryType("caseType");
        category.setCategoryValue("PROBATE");
        List<CaseCategory> caseCategories = new ArrayList<>();
        caseCategories.add(category);
        hmcHearingCaseDetails.setCaseCategories(caseCategories);
        return hmcHearingCaseDetails;
    }

    protected PanelRequirements panelRequirements1() {
        List<String> roleType = new ArrayList<>();
        roleType.add("role 1");
        roleType.add("role 2");
        List<String> authorisationTypes = new ArrayList<>();
        authorisationTypes.add("authorisation type 1");
        authorisationTypes.add("authorisation type 2");
        authorisationTypes.add("authorisation type 3");
        List<String> authorisationSubType = new ArrayList<>();
        authorisationSubType.add("authorisation sub 1");
        authorisationSubType.add("authorisation sub 2");
        authorisationSubType.add("authorisation sub 3");
        authorisationSubType.add("authorisation sub 4");
        final PanelPreference panelPreference1 = new PanelPreference();
        panelPreference1.setMemberID("Member 1");
        panelPreference1.setMemberType("Member Type 1");
        panelPreference1.setRequirementType("MUSTINC");
        final PanelPreference panelPreference2 = new PanelPreference();
        panelPreference2.setMemberID("Member 2");
        panelPreference2.setMemberType("Member Type 2");
        panelPreference2.setRequirementType("OPTINC");
        final PanelPreference panelPreference3 = new PanelPreference();
        panelPreference3.setMemberID("Member 3");
        panelPreference3.setMemberType("Member Type 3");
        panelPreference3.setRequirementType("EXCLUDE");
        List<PanelPreference> panelPreferences = new ArrayList<>();
        panelPreferences.add(panelPreference1);
        panelPreferences.add(panelPreference2);
        panelPreferences.add(panelPreference3);
        List<String> panelSpecialisms = new ArrayList<>();
        panelSpecialisms.add("Specialism 1");
        panelSpecialisms.add("Specialism 2");
        panelSpecialisms.add("Specialism 3");
        panelSpecialisms.add("Specialism 4");
        panelSpecialisms.add("Specialism 5");

        PanelRequirements panelRequirements = new PanelRequirements();
        panelRequirements.setRoleType(roleType);
        panelRequirements.setAuthorisationSubType(authorisationSubType);
        panelRequirements.setPanelPreferences(panelPreferences);
        panelRequirements.setPanelSpecialisms(panelSpecialisms);

        return panelRequirements;
    }

    protected List<PartyDetails> partyDetails1() {
        ArrayList<PartyDetails> partyDetailsArrayList = new ArrayList<>();
        partyDetailsArrayList.add(createPartyDetails("P1", "IND", "DEF", null, createOrganisationDetails()));
        partyDetailsArrayList.add(createPartyDetails("P2", "IND", "DEF2", createIndividualDetails(), null));
        partyDetailsArrayList.add(createPartyDetails("P3", "IND", "DEF3", createIndividualDetails(),
                                                     createOrganisationDetails()
        ));
        return partyDetailsArrayList;
    }

    private OrganisationDetails createOrganisationDetails() {
        OrganisationDetails organisationDetails = new OrganisationDetails();
        organisationDetails.setName("name");
        organisationDetails.setOrganisationType("organisationType");
        organisationDetails.setCftOrganisationID("cftOrganisationId01001");
        return organisationDetails;
    }

    private IndividualDetails createIndividualDetails() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle("Master");
        individualDetails.setFirstName("Harry");
        individualDetails.setLastName("Styles");
        individualDetails.setHearingChannelEmail("harry.styles.neveragin@gmailsss.com");
        individualDetails.setInterpreterLanguage("German");
        individualDetails.setPreferredHearingChannel("CBeebies");
        individualDetails.setReasonableAdjustments(createReasonableAdjustments());
        individualDetails.setRelatedParties(createRelatedParties());
        individualDetails.setVulnerableFlag(false);
        individualDetails.setVulnerabilityDetails("Vulnerability details 1");
        return individualDetails;
    }

    private List<RelatedParty> createRelatedParties() {
        RelatedParty relatedParty1 = new RelatedParty();
        relatedParty1.setRelatedPartyID("relatedParty1111");
        relatedParty1.setRelationshipType("Family");
        RelatedParty relatedParty2 = new RelatedParty();
        relatedParty2.setRelatedPartyID("relatedParty3333");
        relatedParty2.setRelationshipType("Blood Brother");

        List<RelatedParty> relatedParties = new ArrayList<>();
        relatedParties.add(relatedParty1);
        relatedParties.add(relatedParty2);
        return relatedParties;
    }

    private PartyDetails createPartyDetails(String partyID, String partyType, String partyRole,
                                            IndividualDetails individualDetails,
                                            OrganisationDetails organisationDetails) {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID(partyID);
        partyDetails.setPartyType(partyType);
        partyDetails.setPartyRole(partyRole);
        partyDetails.setIndividualDetails(individualDetails);
        partyDetails.setOrganisationDetails(organisationDetails);
        partyDetails.setUnavailabilityRanges(createUnavailableDateRanges());
        partyDetails.setUnavailabilityDayOfWeek(createUnavailabilityDows());
        return partyDetails;
    }

    private List<String> createReasonableAdjustments() {
        List<String> reasonableAdjustments = new ArrayList<>();
        reasonableAdjustments.add("adjust 1");
        reasonableAdjustments.add("adjust 2");
        reasonableAdjustments.add("adjust 3");
        return reasonableAdjustments;
    }

    private List<UnavailabilityDayOfWeek> createUnavailabilityDows() {
        List<UnavailabilityDayOfWeek> unavailabilityDows = new ArrayList<>();
        UnavailabilityDayOfWeek unavailabilityDow1 = new UnavailabilityDayOfWeek();
        unavailabilityDow1.setDayOfWeek("DOW1");
        unavailabilityDow1.setDayOfWeekUnavailabilityType("TYPE1");
        unavailabilityDows.add(unavailabilityDow1);
        UnavailabilityDayOfWeek unavailabilityDow2 = new UnavailabilityDayOfWeek();
        unavailabilityDow2.setDayOfWeek("DOW1");
        unavailabilityDow2.setDayOfWeekUnavailabilityType("TYPE1");
        unavailabilityDows.add(unavailabilityDow2);
        return unavailabilityDows;
    }

    private List<UnavailabilityRange> createUnavailableDateRanges() {
        UnavailabilityRange unavailabilityRanges1 = new UnavailabilityRange();
        unavailabilityRanges1.setUnavailableFromDate(LocalDate.parse("2021-01-01"));
        unavailabilityRanges1.setUnavailableToDate(LocalDate.parse("2021-01-15"));
        UnavailabilityRange unavailabilityRanges2 = new UnavailabilityRange();
        unavailabilityRanges2.setUnavailableFromDate(LocalDate.parse("2021-06-01"));
        unavailabilityRanges2.setUnavailableToDate(LocalDate.parse("2021-06-21"));

        List<UnavailabilityRange> listUnavailabilityRanges = new ArrayList<>();
        listUnavailabilityRanges.add(unavailabilityRanges1);
        listUnavailabilityRanges.add(unavailabilityRanges2);
        return listUnavailabilityRanges;
    }

}
