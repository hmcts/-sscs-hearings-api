package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitCode;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseAccessManagementFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLink;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLinkDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseManagementLocation;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.Issue;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SessionCategory;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsIndustrialInjuriesData;
import uk.gov.hmcts.reform.sscs.model.service.ServiceHearingRequest;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.ServiceLinkedCases;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.UPDATE_CASE_ONLY;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMappingBase.BENEFIT_CODE;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMappingBase.ISSUE_CODE;

@ExtendWith(MockitoExtension.class)
class ServiceHearingsServiceTest {

    private static final long CASE_ID = 12345L;

    @Mock
    private SessionCategoryMapService sessionCategoryMaps;

    @Mock
    private HearingDurationsService hearingDurations;

    @Mock
    private VenueService venueService;

    @Mock
    private ReferenceDataServiceHolder referenceDataServiceHolder;

    @Mock
    private CcdCaseService ccdCaseService;

    @InjectMocks
    private ServiceHearingsService serviceHearingsService;

    private SscsCaseData caseData;
    private SscsCaseDetails caseDetails;


    @BeforeEach
    void setup() {
        caseData = SscsCaseData.builder()
            .ccdCaseId("1234")
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .urgentCase("Yes")
            .adjournCaseCanCaseBeListedRightAway("Yes")
            .dwpResponseDate("2022-07-07")
            .caseManagementLocation(CaseManagementLocation.builder()
                .baseLocation("LIVERPOOL SOCIAL SECURITY AND CHILD SUPPORT TRIBUNAL")
                .region("North West")
                .build())
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
                    .build())
                .rep(Representative.builder()
                    .hasRepresentative("Yes")
                    .name(Name.builder()
                        .title("Mr")
                        .firstName("Harry")
                        .lastName("Potter")
                        .build())
                    .build())
                .build())
            .languagePreferenceWelsh("No")
            .linkedCasesBoolean("No")
            .sscsIndustrialInjuriesData(SscsIndustrialInjuriesData.builder()
                .panelDoctorSpecialism("cardiologist")
                .secondPanelDoctorSpecialism("eyeSurgeon")
                .build())
            .build();

        caseDetails = SscsCaseDetails.builder()
            .data(caseData)
            .build();
    }

    @DisplayName("When a case data is retrieved an entity which does not have a Id, that a new Id will be generated and the method updateCaseData will be called once")
    @Test
    void testGetServiceHearingValuesNoIds() throws Exception {
        ServiceHearingRequest request = ServiceHearingRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .build();

        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE,ISSUE_CODE,true,false))
            .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                false,false, SessionCategory.CATEGORY_03,null));

        given(referenceDataServiceHolder.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);

        given(hearingDurations.getHearingDuration(BENEFIT_CODE,ISSUE_CODE)).willReturn(null);

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        given(venueService.getEpimsIdForVenue(caseData.getProcessingVenue())).willReturn(Optional.of("9876"));

        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);

        given(ccdCaseService.getCaseDetails(String.valueOf(CASE_ID))).willReturn(caseDetails);

        ServiceHearingValues result = serviceHearingsService.getServiceHearingValues(request);

        assertThat(result.getParties())
            .extracting("partyID")
            .doesNotContainNull();

        verify(ccdCaseService, times(1)).updateCaseData(any(SscsCaseData.class), eq(UPDATE_CASE_ONLY),anyString(),anyString());
    }

    @DisplayName("When a case data is retrieved where all valid entities have a Id the method updateCaseData will never be called")
    @Test
    void testGetServiceHearingValuesWithIds() throws Exception {
        ServiceHearingRequest request = ServiceHearingRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .build();

        caseData.getAppeal().getAppellant().setId("1");
        caseData.getAppeal().getRep().setId("2");

        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE,ISSUE_CODE,true,false))
            .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                false,false, SessionCategory.CATEGORY_03,null));

        given(referenceDataServiceHolder.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);

        given(hearingDurations.getHearingDuration(BENEFIT_CODE,ISSUE_CODE)).willReturn(null);

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        given(venueService.getEpimsIdForVenue(caseData.getProcessingVenue())).willReturn(Optional.of("9876"));

        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);

        given(ccdCaseService.getCaseDetails(String.valueOf(CASE_ID))).willReturn(caseDetails);

        ServiceHearingValues result = serviceHearingsService.getServiceHearingValues(request);

        assertThat(result.getParties())
            .extracting("partyID")
            .doesNotContainNull();

        verify(ccdCaseService, never()).updateCaseData(any(SscsCaseData.class), any(EventType.class),anyString(),anyString());
    }

    @ParameterizedTest
    @MethodSource("invalidCasesParameters")
    @DisplayName("One case should be returned when looking up case data for one case")
    void testGetServiceLinkedCasesIncorrectNumberOfCasesReturned(List<SscsCaseDetails> searchResult) throws Exception {
        ServiceHearingRequest request = ServiceHearingRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .build();

        given(ccdCaseService.getCasesViaElastic(List.of(String.valueOf(CASE_ID)))).willReturn(searchResult);

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> serviceHearingsService.getServiceLinkedCases(request));
    }

    @Test
    void testGetServiceLinkedCasesNoLinkedCases() throws Exception {
        ServiceHearingRequest request = ServiceHearingRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .build();

        given(ccdCaseService.getCasesViaElastic(List.of(String.valueOf(CASE_ID)))).willReturn(
            List.of(SscsCaseDetails.builder()
                .data(SscsCaseData.builder()
            .build()).build()));

        List<ServiceLinkedCases> result = serviceHearingsService.getServiceLinkedCases(request);

        assertThat(result).isEmpty();
    }

    @Test
    void testGetServiceLinkedCaseReturnsLinkedCases() throws Exception {
        String linkedCaseReference = "1234";
        String linkedCaseNamePublic = "Some Name";

        ServiceHearingRequest request = ServiceHearingRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .build();

        SscsCaseDetails linkedSscsCaseData = SscsCaseDetails.builder()
            .id(Long.valueOf(linkedCaseReference))
            .data(SscsCaseData.builder()
            .ccdCaseId(linkedCaseReference)
            .caseReference(linkedCaseReference)
            .caseAccessManagementFields(CaseAccessManagementFields.builder()
                .caseNamePublic(linkedCaseNamePublic)
                .build())
            .build())
            .build();

        SscsCaseDetails sscsCaseData = SscsCaseDetails.builder()
            .id(CASE_ID)
            .data(SscsCaseData.builder()
            .ccdCaseId(String.valueOf(CASE_ID))
            .caseReference(String.valueOf(CASE_ID))
            .linkedCase(List.of(CaseLink.builder()
                .value(CaseLinkDetails.builder()
                    .caseReference(linkedCaseReference).build())
                .build()))
            .build())
            .build();

        given(ccdCaseService.getCasesViaElastic(List.of(String.valueOf(CASE_ID)))).willReturn(List.of(sscsCaseData));

        given(ccdCaseService.getCasesViaElastic(List.of(linkedCaseReference))).willReturn(List.of(linkedSscsCaseData));

        List<ServiceLinkedCases> result = serviceHearingsService.getServiceLinkedCases(request);

        assertThat(result).hasSize(1);

        ServiceLinkedCases expected = ServiceLinkedCases.builder()
            .caseReference(linkedCaseReference)
            .caseName(linkedCaseNamePublic)
            .reasonsForLink(new ArrayList<>())
            .build();

        assertThat(result.get(0)).isEqualTo(expected);
    }



    private static Stream<Arguments> invalidCasesParameters() {
        return Stream.of(
            null,
            Arguments.of(new ArrayList<>()),
            Arguments.of(List.of(SscsCaseDetails.builder()
                .data(SscsCaseData.builder()
                .ccdCaseId("1")
                .build())
                    .build(),
                SscsCaseDetails.builder()
                    .data(SscsCaseData.builder()
                    .ccdCaseId("2")
                    .build())
                    .build()
            ))
        );
    }
}
