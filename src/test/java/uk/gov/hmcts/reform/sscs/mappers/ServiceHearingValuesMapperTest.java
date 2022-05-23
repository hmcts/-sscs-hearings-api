package uk.gov.hmcts.reform.sscs.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingWindow;
import uk.gov.hmcts.reform.sscs.model.single.hearing.IndividualDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.OrganisationDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RelatedParty;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityRange;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.sscs.reference.data.mappings.HearingTypeLov.SUBSTANTIVE;

class ServiceHearingValuesMapperTest {

    private static final ServiceHearingValuesMapper mapper = new ServiceHearingValuesMapper();
    private static SscsCaseDetails sscsCaseDetails;

    private static final String NOTE_FROM_OTHER_PARTY = "party_role - Mr Barny Boulderstone:\n";
    private static final String NOTE_FROM_OTHER_APPELLANT = "Appellant - Mr Fred Flintstone:\n";
    public static final String FACE_TO_FACE = "faceToFace";

    @BeforeEach
    public void setUp() {
        sscsCaseDetails = SscsCaseDetails.builder()
            .data(SscsCaseData.builder()
                      .ccdCaseId("1234")
                      .benefitCode("001")
                      .issueCode("DD")
                      .urgentCase("Yes")
                      .adjournCaseCanCaseBeListedRightAway("Yes")
                      .appeal(Appeal.builder()
                                  .hearingType("final")
                                  .appellant(Appellant.builder()
                                                 .name(Name.builder()
                                                           .firstName("Fred")
                                                           .lastName("Flintstone")
                                                           .title("Mr")
                                                           .build())
                                                 .build())
                                  .hearingSubtype(HearingSubtype.builder()
                                                      .hearingTelephoneNumber("0999733733")
                                                      .hearingVideoEmail("test@gmail.com")
                                                      .wantsHearingTypeFaceToFace("Yes")
                                                      .wantsHearingTypeTelephone("No")
                                                      .wantsHearingTypeVideo("No")
                                                      .build())
                                  .hearingOptions(HearingOptions.builder()
                                                      .wantsToAttend("Yes")
                                                      .wantsSupport("Yes")
                                                      .languageInterpreter("Yes")
                                                      .languages("Telugu")
                                                      .signLanguageType("Sign language")
                                                      .arrangements(Arrays.asList(
                                                          "signLanguageInterpreter",
                                                          "hearingLoop",
                                                          "disabledAccess"
                                                      ))
                                                      .scheduleHearing("No")
                                                      .excludeDates(getExcludeDates())
                                                      .agreeLessNotice("No")
                                                      .other(NOTE_FROM_OTHER_APPELLANT)
                                                      .build())
                                  .rep(Representative.builder()
                                           .id("12321")
                                           .hasRepresentative("Yes")
                                           .name(Name.builder()
                                                     .title("Mr")
                                                     .firstName("Harry")
                                                     .lastName("Potter")
                                                     .build())
                                           .address(Address.builder()
                                                        .line1("123 Hairy Lane")
                                                        .line2("Off Hairy Park")
                                                        .town("Town")
                                                        .county("County")
                                                        .postcode("CM14 4LQ")
                                                        .build())
                                           .contact(Contact.builder()
                                                        .email("harry.potter@wizards.com")
                                                        .mobile("07411999999")
                                                        .phone(null)
                                                        .build())
                                           .organisation("HP Ltd")
                                           .build())
                                  .build())
                      .events(getEventsOfCaseData())
                      .languagePreferenceWelsh("No")
                      .otherParties(getOtherParties())
                      .linkedCasesBoolean("No")
                      .build())
            .build();
    }

    @Test
    void shouldMapServiceHearingValuesSuccessfully() {
        // given
        SscsCaseData sscsCaseData = sscsCaseDetails.getData();
        // when
        final ServiceHearingValues serviceHearingValues = mapper.mapServiceHearingValues(sscsCaseDetails);
        final HearingWindow expectedHearingWindow = HearingWindow.builder()
            .dateRangeStart(LocalDate.of(2022, 2, 26))
            .build();
        //then
        assertEquals(serviceHearingValues.getCaseName(), sscsCaseData.getAppeal().getAppellant().getName().getFullName());
        assertTrue(serviceHearingValues.isAutoListFlag()); //
        assertEquals(45, serviceHearingValues.getDuration());
        assertEquals(SUBSTANTIVE.getHmcReference(), serviceHearingValues.getHearingType());
        assertEquals(sscsCaseData.getBenefitCode(), serviceHearingValues.getCaseType());
        assertEquals(sscsCaseData.getIssueCode(), String.join("", serviceHearingValues.getCaseSubTypes()));
        assertEquals(expectedHearingWindow, serviceHearingValues.getHearingWindow());
        assertEquals(HearingPriorityType.HIGH.getType(), serviceHearingValues.getHearingPriorityType());
        assertEquals(3, serviceHearingValues.getNumberOfPhysicalAttendees());
        assertFalse(serviceHearingValues.isHearingInWelshFlag());
        assertEquals(1, serviceHearingValues.getHearingLocations().size());
        assertTrue(serviceHearingValues.getCaseAdditionalSecurityFlag());
        assertEquals(Arrays.asList("signLanguageInterpreter",
            "hearingLoop",
            "disabledAccess"
        ), serviceHearingValues.getFacilitiesRequired());
        assertEquals(NOTE_FROM_OTHER_APPELLANT + NOTE_FROM_OTHER_APPELLANT + "\n" + "\n" + NOTE_FROM_OTHER_PARTY + NOTE_FROM_OTHER_PARTY, serviceHearingValues.getListingComments());
        assertNull(serviceHearingValues.getHearingRequester());
        assertFalse(serviceHearingValues.isPrivateHearingRequiredFlag());
        assertNull(serviceHearingValues.getLeadJudgeContractType());
        assertNull(serviceHearingValues.getJudiciary());
        assertFalse(serviceHearingValues.isHearingIsLinkedFlag());
        assertEquals(3, serviceHearingValues.getParties().size());
        assertEquals("BBA3-appellant", serviceHearingValues.getParties().stream().findFirst().orElseThrow().getPartyRole());
        assertEquals("BBA3-Representative", serviceHearingValues.getParties().stream().filter(partyDetails -> PartyType.ORG.getPartyLabel().equals(partyDetails.getPartyType())).findFirst().orElseThrow().getPartyRole());
        assertEquals("BBA3-otherParty", serviceHearingValues.getParties().stream().filter(partyDetails -> "party_id_1".equals(partyDetails.getPartyID())).findFirst().orElseThrow().getPartyRole());
        assertEquals(getCaseFlags(), serviceHearingValues.getCaseFlags());
        assertNull(serviceHearingValues.getVocabulary());
    }

    private List<Event> getEventsOfCaseData() {
        return new ArrayList<>() {{
                add(Event.builder()
                        .value(EventDetails.builder()
                                   .date("2022-02-12T20:30:00")
                                   .type("responseReceived")
                                   .description("Dwp respond")
                                   .build())
                        .build());
            }
        };
    }


    private List<CcdValue<OtherParty>> getOtherParties() {
        return new ArrayList<>() {
            {
                add(new CcdValue<>(OtherParty.builder()
                                   .id("party_id_1")
                                   .name(Name.builder()
                                             .firstName("Barny")
                                             .lastName("Boulderstone")
                                             .title("Mr")
                                             .build())
                                   .address(Address.builder().build())
                                   .confidentialityRequired(YesNo.NO)
                                   .unacceptableCustomerBehaviour(YesNo.YES)
                                   .hearingSubtype(HearingSubtype.builder()
                                                       .hearingTelephoneNumber("0999733735")
                                                       .hearingVideoEmail("test2@gmail.com")
                                                       .wantsHearingTypeFaceToFace("Yes")
                                                       .wantsHearingTypeTelephone("No")
                                                       .wantsHearingTypeVideo("No")
                                                       .build())
                                   .hearingOptions(HearingOptions.builder()
                                                       .wantsToAttend("Yes")
                                                       .wantsSupport("Yes")
                                                       .languageInterpreter("Yes")
                                                       .languages("Telugu")
                                                       .scheduleHearing("No")
                                                       .excludeDates(getExcludeDates())
                                                       .agreeLessNotice("No")
                                                       .other(NOTE_FROM_OTHER_PARTY)
                                                       .build())
                                   .isAppointee("No")
                                   .appointee(Appointee.builder().build())
                                   .rep(Representative.builder().build())
                                   .otherPartySubscription(Subscription.builder().build())
                                   .otherPartyAppointeeSubscription(Subscription.builder().build())
                                   .otherPartyRepresentativeSubscription(Subscription.builder().build())
                                   .sendNewOtherPartyNotification(YesNo.NO)
                                   .reasonableAdjustment(ReasonableAdjustmentDetails.builder()
                                                             .reasonableAdjustmentRequirements("Some adjustments...")
                                                             .wantsReasonableAdjustment(YesNo.YES)
                                                             .build())
                                   .appointeeReasonableAdjustment(ReasonableAdjustmentDetails.builder().build())
                                   .repReasonableAdjustment(ReasonableAdjustmentDetails.builder().build())
                                   .role(Role.builder()
                                             .name("party_role")
                                             .description("description")
                                             .build())
                                   .build()));
            }
        };
    }



    private List<PartyDetails> getParties() {
        return new ArrayList<>() {{
                add(PartyDetails.builder()
                        .partyID(null)
                        .partyType(PartyType.IND.getPartyLabel())
                        .partyChannelSubType(FACE_TO_FACE)
                        .partyRole("BBA3-appellant")
                        .individualDetails(getIndividualDetails())
                        .organisationDetails(OrganisationDetails.builder().build())
                        .unavailabilityDayOfWeek(null)
                        .unavailabilityRanges(getUnavailabilityRanges())
                        .build());
                add(PartyDetails.builder()
                    .partyID("party_id_1")
                    .partyType(PartyType.IND.getPartyLabel())
                    .partyChannelSubType(FACE_TO_FACE)
                    .partyRole("party_role")
                    .individualDetails(getIndividualDetails())
                    .organisationDetails(OrganisationDetails.builder().build())
                    .unavailabilityDayOfWeek(null)
                    .unavailabilityRanges(getUnavailabilityRanges())
                    .build());
            }
        };
    }

    private List<UnavailabilityRange> getUnavailabilityRanges() {
        return new ArrayList<>() {
            {
                add(UnavailabilityRange.builder()
                    .unavailableFromDate(LocalDate.of(2022, 1,12))
                    .unavailableToDate(LocalDate.of(2022,1,19))
                    .build());
            }};
    }

    private IndividualDetails getIndividualDetails() {
        return IndividualDetails.builder()
            .firstName("Barny")
            .lastName("Boulderstone")
            .preferredHearingChannel(FACE_TO_FACE)
            .interpreterLanguage("tel")
            .reasonableAdjustments(new ArrayList<>())
            .vulnerableFlag(false)
            .vulnerabilityDetails(null)
            .hearingChannelEmail(Collections.singletonList("test2@gmail.com"))
            .hearingChannelPhone(Collections.singletonList("0999733735"))
            .relatedParties(getRelatedParties()) // TODO this field would be populated when the corresponding method is finished
            .build();
    }

    private List<RelatedParty> getRelatedParties() {
        return new ArrayList<>();
    }


    private List<ExcludeDate> getExcludeDates() {
        return new ArrayList<>() {
            {
                add(ExcludeDate.builder()
                    .value(DateRange.builder()
                               .start("2022-01-12")
                               .end("2022-01-19")
                               .build())
                    .build());
            }
        };
    }

    // TODO it will be populated when the method is provided
    private CaseFlags getCaseFlags() {
        return CaseFlags.builder()
            .flags(getPartyFlags())
            .flagAmendUrl("")
            .build();
    }

    private List<PartyFlags> getPartyFlags() {
        return new ArrayList<>() {{
                add(PartyFlags.builder()
                    .partyName(null)
                    .flagParentId("10")
                    .flagId("44")
                    .flagDescription("Sign Language Interpreter")
                    .flagStatus(null)
                    .build());
                add(PartyFlags.builder()
                    .partyName(null)
                    .flagParentId("6")
                    .flagId("21")
                    .flagDescription("Step free / wheelchair access")
                    .flagStatus(null)
                    .build());
                add(PartyFlags.builder()
                    .partyName(null)
                    .flagParentId("11")
                    .flagId("45")
                    .flagDescription("Hearing loop (hearing enhancement system)")
                    .flagStatus(null)
                    .build());
                add(PartyFlags.builder()
                    .partyName(null)
                    .flagParentId("1")
                    .flagId("67")
                    .flagDescription("Urgent flag")
                    .flagStatus(null)
                    .build());
            }
        };
    }
}
