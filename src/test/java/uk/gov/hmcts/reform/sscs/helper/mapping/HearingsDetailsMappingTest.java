package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingLocations;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingWindow;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelRequirements;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingTypeLov;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsDetailsMapping.DAYS_TO_ADD_HEARING_WINDOW_TODAY;

class HearingsDetailsMappingTest extends HearingsMappingBase {

    @DisplayName("When a valid hearing wrapper is given buildHearingDetails returns the correct Hearing Details")
    @Test
    void buildHearingDetails() {
        given(hearingDurations.getHearingDuration(BENEFIT_CODE,ISSUE_CODE))
                .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                        60,75,30));
        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE,ISSUE_CODE,false,false))
                .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                        false,false,SessionCategory.CATEGORY_03,null));

        given(referenceData.getHearingDurations()).willReturn(hearingDurations);
        given(referenceData.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);

        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .benefitCode(BENEFIT_CODE)
                .issueCode(ISSUE_CODE)
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
                        .build())
                .caseManagementLocation(CaseManagementLocation.builder()
                                            .baseLocation(EPIMS_ID)
                                            .region(REGION)
                                            .build())
                .build();

        HearingWrapper wrapper = HearingWrapper.builder()
                .caseData(caseData)
                .build();

        HearingDetails hearingDetails = HearingsDetailsMapping.buildHearingDetails(wrapper, referenceData);

        assertNotNull(hearingDetails.getHearingType());
        assertNotNull(hearingDetails.getHearingWindow());
        assertNotNull(hearingDetails.getDuration());
        assertNotNull(hearingDetails.getHearingPriorityType());
        assertEquals(0, hearingDetails.getNumberOfPhysicalAttendees());
        assertNotNull(hearingDetails.getHearingLocations());
        assertNull(hearingDetails.getListingComments());
        assertNull(hearingDetails.getHearingRequester());
        assertNull(hearingDetails.getLeadJudgeContractType());
        assertNotNull(hearingDetails.getPanelRequirements());
    }

    @DisplayName("When there are linked cases, shouldBeAutoListed returns false")
    @Test
    void testShouldBeAutoListedFalse() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .linkedCase(List.of(CaseLink.builder()
                        .value(CaseLinkDetails.builder()
                                .caseReference("123456")
                                .build())
                        .build()))
                .build();
        boolean result = HearingsDetailsMapping.shouldBeAutoListed(caseData);

        assertThat(result).isFalse();
    }

    @DisplayName("When there are no linked cases, shouldBeAutoListed returns true")
    @Test
    void testShouldBeAutoListedTrue() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .build();
        boolean result = HearingsDetailsMapping.shouldBeAutoListed(caseData);

        assertThat(result).isTrue();
    }

    @DisplayName("When urgentCase is not No, null or Blank, shouldBeAutoListed should return True")
    @ParameterizedTest
    @ValueSource(strings = {"No"})
    @NullAndEmptySource
    void testShouldBeAutoListed(String urgentCase) {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .urgentCase(urgentCase)
                .build();

        boolean result = HearingsDetailsMapping.shouldBeAutoListed(caseData);

        assertThat(result).isTrue();
    }

    @DisplayName("When urgentCase is Yes, shouldBeAutoListed should return False")
    @Test
    void testShouldBeAutoListed() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .urgentCase("Yes")
                .build();

        boolean result = HearingsDetailsMapping.shouldBeAutoListed(caseData);

        assertThat(result).isFalse();
    }

    @DisplayName("shouldBeHearingsInWelshFlag Test")
    @Test
    void shouldBeHearingsInWelshFlag() {
        boolean result = HearingsDetailsMapping.shouldBeHearingsInWelshFlag();

        assertFalse(result);
    }

    @DisplayName("When case has a linked case isCaseLinked returns true")
    @Test
    void testIsCaseLinked() {
        SscsCaseData caseData = SscsCaseData.builder()
                .linkedCase(List.of(CaseLink.builder()
                        .value(CaseLinkDetails.builder()
                                .caseReference("123456")
                                .build())
                        .build()))
                .build();
        boolean result = HearingsDetailsMapping.isCaseLinked(caseData);

        assertThat(result).isTrue();
    }

    @DisplayName("When case has multiple linked cases isCaseLinked returns true")
    @Test
    void testIsCaseLinkedMultiple() {
        SscsCaseData caseData = SscsCaseData.builder()
                .linkedCase(List.of(CaseLink.builder()
                                .value(CaseLinkDetails.builder()
                                        .caseReference("123456")
                                        .build())
                                .build(),
                        CaseLink.builder()
                                .value(CaseLinkDetails.builder()
                                        .caseReference("654321")
                                        .build())
                                .build()))
                .build();
        boolean result = HearingsDetailsMapping.isCaseLinked(caseData);

        assertThat(result).isTrue();
    }

    @DisplayName("When case has empty linkedCase isCaseLinked returns true")
    @Test
    void testIsCaseLinkedEmpty() {
        SscsCaseData caseData = SscsCaseData.builder()
                .linkedCase(List.of())
                .build();
        boolean result = HearingsDetailsMapping.isCaseLinked(caseData);

        assertThat(result).isFalse();
    }

    @DisplayName("When case has null linkedCase isCaseLinked returns true")
    @Test
    void testIsCaseLinkedNull() {
        SscsCaseData caseData = SscsCaseData.builder()
                .build();
        boolean result = HearingsDetailsMapping.isCaseLinked(caseData);

        assertThat(result).isFalse();
    }

    @DisplayName("Hearing type should be substantive.")
    @Test
    void getHearingType() {
        String result = HearingsDetailsMapping.getHearingType();

        assertEquals(result, HearingTypeLov.SUBSTANTIVE.getHmcReference());
    }

    @DisplayName("When case with valid DWP_RESPOND event and is auto-listable is given buildHearingWindow returns a window starting within 1 month of the event's date")
    @ParameterizedTest
    @CsvSource(value = {
        "2021-12-01,Yes,2021-12-15",
        "2021-12-01,No,2021-12-29",
    }, nullValues = {"null"})
    void testBuildHearingWindow(String dwpResponded, String isUrgent, LocalDate expected) {
        SscsCaseData caseData = SscsCaseData.builder()
                .dwpResponseDate(dwpResponded)
                .urgentCase(isUrgent)
                .build();

        HearingWindow result = HearingsDetailsMapping.buildHearingWindow(caseData, true);

        assertThat(result).isNotNull();

        assertThat(result.getDateRangeStart()).isEqualTo(expected);

        assertThat(result.getFirstDateTimeMustBe()).isNull();
        assertThat(result.getDateRangeEnd()).isNull();
    }

    @DisplayName("When case is autolist but dwpResponseDate is blank, buildHearingWindow returns start date of tomorrow")
    @Test
    void testBuildHearingWindowResponseBlank() {
        SscsCaseData caseData = SscsCaseData.builder().build();

        HearingWindow result = HearingsDetailsMapping.buildHearingWindow(caseData, true);

        assertThat(result).isNotNull();

        LocalDate expected = LocalDate.now().plusDays(DAYS_TO_ADD_HEARING_WINDOW_TODAY);
        assertThat(result.getDateRangeStart()).isEqualTo(expected);

        assertThat(result.getFirstDateTimeMustBe()).isNull();
        assertThat(result.getDateRangeEnd()).isNull();
    }

    @DisplayName("When case when not autolist and not an urgent case, buildHearingWindow returns start date of tomorrow")
    @Test
    void testBuildHearingWindowNotAutoListUrgent() {
        SscsCaseData caseData = SscsCaseData.builder()
                .dwpResponseDate(LocalDate.now().toString())
                .build();
        HearingWindow result = HearingsDetailsMapping.buildHearingWindow(caseData, false);

        assertThat(result).isNotNull();

        LocalDate expected = LocalDate.now().plusDays(DAYS_TO_ADD_HEARING_WINDOW_TODAY);
        assertThat(result.getDateRangeStart()).isEqualTo(expected);

        assertThat(result.getFirstDateTimeMustBe()).isNull();
        assertThat(result.getDateRangeEnd()).isNull();
    }

    @DisplayName("When case when not autolist and an urgent case, buildHearingWindow returns start date of tomorrow")
    @Test
    void testBuildHearingWindowNotAutoListIsUrgent() {
        SscsCaseData caseData = SscsCaseData.builder()
                .dwpResponseDate("2021-12-01")
                .urgentCase("Yes")
                .build();
        HearingWindow result = HearingsDetailsMapping.buildHearingWindow(caseData, false);

        assertThat(result).isNotNull();

        LocalDate expected = LocalDate.now().plusDays(DAYS_TO_ADD_HEARING_WINDOW_TODAY);
        assertThat(result.getDateRangeStart()).isEqualTo("2021-12-15");

        assertThat(result.getFirstDateTimeMustBe()).isNull();
        assertThat(result.getDateRangeEnd()).isNull();
    }

    @DisplayName("When .. is given getFirstDateTimeMustBe returns the valid LocalDateTime")
    @Test
    void testBetFirstDateTimeMustBe() {
        // TODO Finish Test when method done
        LocalDateTime result = HearingsDetailsMapping.getFirstDateTimeMustBe();

        assertThat(result).isNull();
    }

    @DisplayName("getHearingPriority Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes,Yes,high", "Yes,No,high", "No,Yes,high",
        "No,No,normal",
        "Yes,null,high", "No,null,normal",
        "null,Yes,high", "null,No,normal",
        "null,null,normal",
        "Yes,,high", "No,,normal",
        ",Yes,high", ",No,normal",
        ",,normal"
    }, nullValues = {"null"})
    void getHearingPriority(String isAdjournCase, String isUrgentCase, String expected) {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .urgentCase(isUrgentCase)
                .adjournCasePanelMembersExcluded(isAdjournCase)
                .build();
        String result = HearingsDetailsMapping.getHearingPriority(caseData);

        assertEquals(expected, result);
    }

    @DisplayName("getNumberOfPhysicalAttendees Test")
    @Test
    void shouldGetNumberOfPhysicalAttendees() {
        // given
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        Appeal appeal = Mockito.mock(Appeal.class);
        HearingSubtype hearingSubtype = Mockito.mock(HearingSubtype.class);
        HearingOptions hearingOptions = Mockito.mock(HearingOptions.class);
        Representative representative = Mockito.mock(Representative.class);
        // when
        Mockito.when(representative.getHasRepresentative()).thenReturn(YesNo.YES.getValue());
        Mockito.when(hearingOptions.getWantsToAttend()).thenReturn(YesNo.YES.getValue());
        Mockito.when(hearingSubtype.isWantsHearingTypeFaceToFace()).thenReturn(true);
        Mockito.when(appeal.getRep()).thenReturn(representative);
        Mockito.when(appeal.getHearingOptions()).thenReturn(hearingOptions);
        Mockito.when(appeal.getHearingSubtype()).thenReturn(hearingSubtype);
        Mockito.when(sscsCaseData.getAppeal()).thenReturn(appeal);
        //then
        assertEquals(3, HearingsDetailsMapping.getNumberOfPhysicalAttendees(sscsCaseData));
    }

    @DisplayName("getHearingLocations Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {"219164,court"}, nullValues = {"null"})
    void getHearingLocations(String baseLocation, String region) {
        CaseManagementLocation managementLocation = CaseManagementLocation.builder()
                .baseLocation(baseLocation)
                .region(region)
                .build();
        List<HearingLocations> result = HearingsDetailsMapping.getHearingLocations(managementLocation);

        assertEquals(1, result.size());
        assertEquals("219164", result.get(0).getLocationId());
        assertEquals("court", result.get(0).getLocationType());
    }

    @DisplayName("When .. is given getFacilitiesRequired return the correct facilities Required")
    @Test
    void getFacilitiesRequired() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();
        List<String> result = HearingsDetailsMapping.getFacilitiesRequired(caseData);
        List<String> expected = new ArrayList<>();

        assertEquals(0, result.size());
        assertEquals(expected, result);
    }

    @DisplayName("When appellant and other parties Hearing Options other comments are given "
            + "getListingComments returns all the comments separated by newlines")
    @ParameterizedTest
    @CsvSource(value = {
        "AppellantComments,'OpComments1',Appellant - Mx Test Appellant:\\nAppellantComments\\n\\nOther Party - Mx Test OtherParty:\\nOpComments1",
        "AppellantComments,'',Appellant - Mx Test Appellant:\\nAppellantComments",
        "AppellantComments,null,Appellant - Mx Test Appellant:\\nAppellantComments",
    }, nullValues = {"null"})
    void getListingComments(String appellant, String otherPartiesComments, String expected) {
        List<CcdValue<OtherParty>> otherParties = null;

        if (nonNull(otherPartiesComments)) {
            otherParties = new ArrayList<>();
            otherParties.add(new CcdValue<>(OtherParty.builder()
                    .name(Name.builder()
                            .title("Mx")
                            .firstName("Test")
                            .lastName("OtherParty")
                            .build())
                    .hearingOptions(HearingOptions.builder()
                            .other(otherPartiesComments)
                            .build())
                    .build()));
        }

        Appeal appeal = Appeal.builder().appellant(Appellant.builder()
                        .name(Name.builder()
                                .title("Mx")
                                .firstName("Test")
                                .lastName("Appellant")
                                .build())
                        .build())
                .build();
        if (nonNull(appellant)) {
            appeal.setHearingOptions(HearingOptions.builder().other(appellant).build());
        }

        String result = HearingsDetailsMapping.getListingComments(appeal, otherParties);

        assertThat(result).isEqualToNormalizingNewlines(expected.replace("\\n",String.format("%n")));
    }

    @DisplayName("When all null or empty comments are given getListingComments returns null")
    @ParameterizedTest
    @CsvSource(value = {
        "'',''",
        "null,''",
        "null,null",
        "'','||'",
    }, nullValues = {"null"})
    void getListingCommentsReturnsNull(String appellant, String otherPartiesComments) {
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        if (nonNull(otherPartiesComments)) {
            for (String otherPartyComment : splitCsvParamArray(otherPartiesComments)) {
                otherParties.add(new CcdValue<>(OtherParty.builder()
                        .hearingOptions(HearingOptions.builder().other(otherPartyComment).build())
                        .build()));
            }
        } else {
            otherParties = null;
        }
        Appeal appeal = Appeal.builder().build();
        if (nonNull(appellant)) {
            appeal.setHearingOptions(HearingOptions.builder().other(appellant).build());
        }

        String result = HearingsDetailsMapping.getListingComments(appeal, otherParties);

        assertNull(result);
    }


    @DisplayName("getPartyRole with Role Test")
    @Test
    void getPartyRole() {
        Party party = Appellant.builder()
                .role(Role.builder()
                        .name("Test Role")
                        .build())
                .build();
        String result = HearingsDetailsMapping.getPartyRole(party);

        assertThat(result).isEqualTo("Test Role");
    }

    @DisplayName("getPartyRole without Role Test")
    @Test
    void getPartyRoleNoRole() {
        Party party = Appellant.builder().build();
        String result = HearingsDetailsMapping.getPartyRole(party);

        assertThat(result).isEqualTo("Appellant");
    }

    @DisplayName("getPartyRole with empty/null Role Name Test")
    @ParameterizedTest
    @NullAndEmptySource
    void getPartyRoleNoRole(String role) {
        Party party = Appellant.builder()
                .role(Role.builder()
                        .name(role)
                        .build())
                .build();
        String result = HearingsDetailsMapping.getPartyRole(party);

        assertThat(result).isEqualTo("Appellant");
    }

    @DisplayName("getPartyName where Appointee is null Test")
    @ParameterizedTest
    @ValueSource(strings = {"Yes", "No"})
    @NullAndEmptySource
    void getPartyName(String isAppointee) {
        Party party = Appellant.builder()
                .isAppointee(isAppointee)
                .name(Name.builder()
                        .title("Mx")
                        .firstName("Test")
                        .lastName("Appellant")
                        .build())
                .build();
        String result = HearingsDetailsMapping.getEntityName(party);

        assertThat(result).isEqualTo("Mx Test Appellant");
    }

    @DisplayName("getPartyName No Appointee Test")
    @ParameterizedTest
    @ValueSource(strings = {"No"})
    @NullAndEmptySource
    void getPartyNameAppellant(String isAppointee) {
        Party party = Appellant.builder()
                .isAppointee(isAppointee)
                .appointee(Appointee.builder()
                        .name(Name.builder()
                                .title("Mx")
                                .firstName("Test")
                                .lastName("Appointee")
                                .build())
                        .build())
                .name(Name.builder()
                        .title("Mx")
                        .firstName("Test")
                        .lastName("Appellant")
                        .build())
                .build();
        String result = HearingsDetailsMapping.getEntityName(party);

        assertThat(result).isEqualTo("Mx Test Appellant");
    }

    @DisplayName("getHearingRequester Test")
    @Test
    void getHearingRequester() {
        String result = HearingsDetailsMapping.getHearingRequester();

        assertNull(result);
    }

    @DisplayName("When .. is given getLeadJudgeContractType returns the correct LeadJudgeContractType")
    @Test
    void getLeadJudgeContractType() {
        // TODO Finish Test when method done
        String result = HearingsDetailsMapping.getLeadJudgeContractType();

        assertNull(result);
    }

    @DisplayName("When no data is given getPanelRequirements returns the valid but empty PanelRequirements")
    @Test
    void getPanelRequirements() {
        // TODO Finish Test when method done

        given(referenceData.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);

        SscsCaseData caseData = SscsCaseData.builder().build();

        PanelRequirements result = HearingsDetailsMapping.getPanelRequirements(caseData, referenceData);

        assertThat(result).isNotNull();
        assertThat(result.getRoleTypes()).isEmpty();
        assertThat(result.getAuthorisationTypes()).isEmpty();
        assertThat(result.getAuthorisationSubTypes()).isEmpty();
        assertThat(result.getPanelPreferences()).isEmpty();
        assertThat(result.getPanelSpecialisms()).isEmpty();
    }

    @DisplayName("When a case is given with a second doctor getPanelRequirements returns the valid PanelRequirements")
    @ParameterizedTest
    @CsvSource(value = {
        "cardiologist,eyeSurgeon,BBA3-MQPM1-001|BBA3-MQPM2-003",
        "null,carer,BBA3-MQPM1|BBA3-MQPM2-002",
    }, nullValues = {"null"})
    void getPanelSpecialisms(String doctorSpecialism, String doctorSpecialismSecond, String expected) {

        SessionCategoryMap sessionCategoryMap = new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                true,false,SessionCategory.CATEGORY_06,null);

        SscsCaseData caseData = SscsCaseData.builder()
                .benefitCode(BENEFIT_CODE)
                .issueCode(ISSUE_CODE)
                .sscsIndustrialInjuriesData(SscsIndustrialInjuriesData.builder()
                        .panelDoctorSpecialism(doctorSpecialism)
                        .secondPanelDoctorSpecialism(doctorSpecialismSecond)
                        .build())
                .build();

        List<String> result = HearingsDetailsMapping.getPanelSpecialisms(caseData, sessionCategoryMap);

        List<String> expectedList = splitCsvParamArray(expected);
        assertThat(result)
                .containsExactlyInAnyOrderElementsOf(expectedList);

    }

    @DisplayName("When a case is given with no second doctor getPanelRequirements returns the valid PanelRequirements")
    @ParameterizedTest
    @CsvSource(value = {
        "generalPractitioner,BBA3-MQPM1-004",
        "null,BBA3-MQPM1",
    }, nullValues = {"null"})
    void getPanelSpecialisms(String doctorSpecialism,String expected) {

        SessionCategoryMap sessionCategoryMap = new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                false,false,SessionCategory.CATEGORY_05,null);

        SscsCaseData caseData = SscsCaseData.builder()
                .benefitCode(BENEFIT_CODE)
                .issueCode(ISSUE_CODE)
                .sscsIndustrialInjuriesData(SscsIndustrialInjuriesData.builder()
                        .panelDoctorSpecialism(doctorSpecialism)
                        .build())
                .build();

        List<String> result = HearingsDetailsMapping.getPanelSpecialisms(caseData, sessionCategoryMap);

        List<String> expectedList = splitCsvParamArray(expected);
        assertThat(result)
                .containsExactlyInAnyOrderElementsOf(expectedList);

    }

    @DisplayName("When .. is given getPanelPreferences returns the correct List of PanelPreferences")
    @Test
    void getPanelPreferences() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();
        List<PanelPreference> result = HearingsDetailsMapping.getPanelPreferences(caseData);
        List<PanelPreference> expected = new ArrayList<>();

        assertEquals(0, result.size());
        assertEquals(expected, result);
    }

    @DisplayName("when a invalid adjournCaseDuration or adjournCaseDurationUnits is given getHearingDuration returns the default duration Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "null,null",
        "null,60",
        "1,test",
    }, nullValues = {"null"})
    void getHearingDuration(String adjournCaseDuration, String adjournCaseDurationUnits) {
        // TODO Finish Test when method done
        given(hearingDurations.getHearingDuration(BENEFIT_CODE,ISSUE_CODE))
                .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                        60,75,30));

        given(referenceData.getHearingDurations()).willReturn(hearingDurations);

        SscsCaseData caseData = SscsCaseData.builder()
                .benefitCode(BENEFIT_CODE)
                .issueCode(ISSUE_CODE)
                .adjournCaseNextHearingListingDuration(adjournCaseDuration)
                .adjournCaseNextHearingListingDurationUnits(adjournCaseDurationUnits)
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
                        .build())
                .build();
        int result = HearingsDetailsMapping.getHearingDuration(caseData, referenceData);

        assertEquals(30, result);
    }

    @DisplayName("when a valid adjournCaseDuration and adjournCaseDurationUnits is given getHearingDuration returns the correct duration Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "2,hours,120",
        "1,sessions,165",
    }, nullValues = {"null"})
    void getHearingDuration(String adjournCaseDuration, String adjournCaseDurationUnits, int expected) {
        // TODO Finish Test when method done

        SscsCaseData caseData = SscsCaseData.builder()
                .benefitCode(BENEFIT_CODE)
                .issueCode(ISSUE_CODE)
                .adjournCaseNextHearingListingDuration(adjournCaseDuration)
                .adjournCaseNextHearingListingDurationUnits(adjournCaseDurationUnits)
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
                        .build())
                .build();
        int result = HearingsDetailsMapping.getHearingDuration(caseData, referenceData);

        assertEquals(expected, result);
    }

    @DisplayName("When the benefit or issue code is null getHearingDurationBenefitIssueCodes returns null Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "null,null",
        "002,null",
        "null,DD",
    }, nullValues = {"null"})
    void getHearingDurationBenefitIssueCodesPaper(String benefitCode, String issueCode) {

        given(hearingDurations.getHearingDuration(benefitCode,issueCode)).willReturn(null);

        given(referenceData.getHearingDurations()).willReturn(hearingDurations);

        SscsCaseData caseData = SscsCaseData.builder()
                .benefitCode(benefitCode)
                .issueCode(issueCode)
                .appeal(Appeal.builder()
                        .hearingSubtype(HearingSubtype.builder().build())
                        .hearingOptions(HearingOptions.builder().build())
                        .build())
                .build();

        Integer result = HearingsDetailsMapping.getHearingDurationBenefitIssueCodes(caseData, referenceData);

        assertThat(result).isNull();
    }

    @DisplayName("When wantsToAttend for the Appeal is null and the hearing type is paper "
            + "getHearingDurationBenefitIssueCodes return the correct paper durations")
    @Test
    void getHearingDurationBenefitIssueCodesPaper() {

        given(hearingDurations.getHearingDuration(BENEFIT_CODE,ISSUE_CODE))
                .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                        60,75,30));

        given(referenceData.getHearingDurations()).willReturn(hearingDurations);

        SscsCaseData caseData = SscsCaseData.builder()
                .benefitCode(BENEFIT_CODE)
                .issueCode(ISSUE_CODE)
                .appeal(Appeal.builder()
                        .hearingType("paper")
                        .hearingSubtype(HearingSubtype.builder().build())
                        .hearingOptions(HearingOptions.builder().build())
                        .build())
                .build();

        Integer result = HearingsDetailsMapping.getHearingDurationBenefitIssueCodes(caseData, referenceData);

        assertThat(result).isEqualTo(30);
    }

    @DisplayName("When wantsToAttend for the Appeal is Yes and languageInterpreter is null "
            + "getHearingDurationBenefitIssueCodes return the correct face to face durations")
    @Test
    void getHearingDurationBenefitIssueCodesFaceToFace() {
        given(hearingDurations.getHearingDuration(BENEFIT_CODE,ISSUE_CODE))
                .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                        60,75,30));

        given(hearingDurations.addExtraTimeIfNeeded(eq(60),eq(BenefitCode.PIP_NEW_CLAIM),eq(Issue.DD),any()))
                .willReturn(60);

        given(referenceData.getHearingDurations()).willReturn(hearingDurations);

        SscsCaseData caseData = SscsCaseData.builder()
                .benefitCode(BENEFIT_CODE)
                .issueCode(ISSUE_CODE)
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder()
                                .wantsToAttend("Yes")
                                .build())
                        .build())
                .build();

        Integer result = HearingsDetailsMapping.getHearingDurationBenefitIssueCodes(caseData, referenceData);

        assertThat(result).isEqualTo(60);
    }

    @DisplayName("When wantsToAttend for the Appeal is Yes "
            + "getHearingDurationBenefitIssueCodes return the correct interpreter durations")
    @Test
    void getHearingDurationBenefitIssueCodesInterpreter() {
        given(hearingDurations.getHearingDuration(BENEFIT_CODE,ISSUE_CODE))
                .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                        60,75,30));

        given(hearingDurations.addExtraTimeIfNeeded(eq(75),eq(BenefitCode.PIP_NEW_CLAIM),eq(Issue.DD),any()))
                .willReturn(75);

        given(referenceData.getHearingDurations()).willReturn(hearingDurations);

        SscsCaseData caseData = SscsCaseData.builder()
                .benefitCode(BENEFIT_CODE)
                .issueCode(ISSUE_CODE)
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder()
                                .wantsToAttend("Yes")
                                .languageInterpreter("Yes")
                                .build())
                        .build())
                .build();

        Integer result = HearingsDetailsMapping.getHearingDurationBenefitIssueCodes(caseData, referenceData);

        assertThat(result).isEqualTo(75);
    }

    @DisplayName("getElementsDisputed returns empty list when elementDisputed is Null")
    @Test
    void getElementsDisputedNull() {
        SscsCaseData caseData = SscsCaseData.builder().build();

        List<String> result = HearingsDetailsMapping.getElementsDisputed(caseData);

        assertThat(result).isEmpty();
    }

    @DisplayName("getElementsDisputed returns a List of elements of all elements in each of the elementDisputed fields in SscsCaseData")
    @Test
    void getElementsDisputed() {
        ElementDisputed elementDisputed = ElementDisputed.builder()
                .value(ElementDisputedDetails.builder()
                        .issueCode("WC")
                        .outcome("Test")
                        .build())
                .build();
        SscsCaseData caseData = SscsCaseData.builder()
                .elementsDisputedGeneral(List.of(elementDisputed))
                .elementsDisputedSanctions(List.of(elementDisputed))
                .elementsDisputedOverpayment(List.of(elementDisputed))
                .elementsDisputedHousing(List.of(elementDisputed))
                .elementsDisputedChildCare(List.of(elementDisputed))
                .elementsDisputedCare(List.of(elementDisputed))
                .elementsDisputedChildElement(List.of(elementDisputed))
                .elementsDisputedChildDisabled(List.of(elementDisputed))
                .elementsDisputedLimitedWork(List.of(elementDisputed))
                .build();
        List<String> result = HearingsDetailsMapping.getElementsDisputed(caseData);

        assertThat(result)
                .hasSize(9)
                .containsOnly("WC");
    }
}
