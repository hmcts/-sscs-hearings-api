package uk.gov.hmcts.reform.sscs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLink;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLinkDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.ServiceHearingValuesMapping;
import uk.gov.hmcts.reform.sscs.model.service.ServiceHearingRequest;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.ServiceLinkedCases;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceHearingsService {

    private final CcdCaseService ccdCaseService;

    private final ReferenceDataServiceHolder referenceDataServiceHolder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ServiceHearingValues getServiceHearingValues(ServiceHearingRequest request)
        throws GetCaseException, UpdateCaseException, InvalidMappingException, JsonProcessingException {
        SscsCaseDetails caseDetails = ccdCaseService.getCaseDetails(request.getCaseId());

        SscsCaseData caseData = caseDetails.getData();
        String originalCaseData = objectMapper.writeValueAsString(caseData);

        HearingsMapping.updateIds(caseData);
        ServiceHearingValues model = ServiceHearingValuesMapping.mapServiceHearingValues(caseData, referenceDataServiceHolder);

        String updatedCaseData = objectMapper.writeValueAsString(caseData);

        if (!originalCaseData.equals(updatedCaseData)) {
            ccdCaseService.updateCaseData(
                caseData,
                EventType.UPDATE_CASE_ONLY,
                "Updating caseDetails IDs",
                "IDs updated for caseDetails due to ServiceHearingValues request");
        }

        return model;
    }

    public List<ServiceLinkedCases> getServiceLinkedCases(ServiceHearingRequest request)
        throws GetCaseException {

        String caseId = request.getCaseId();
        List<SscsCaseData> mainCaseData = ccdCaseService.getCases(List.of(request.getCaseId()));

        if (mainCaseData.size() != 1) {
            throw new IllegalStateException(
                "Invalid search data returned: one case is required. Attempted to fetch data for " + caseId);
        }

        SscsCaseData caseData = mainCaseData.get(0);

        List<String> linkedReferences = Optional.ofNullable(caseData.getLinkedCase())
            .orElseGet(Collections::emptyList).stream()
            .filter(Objects::nonNull)
            .map(CaseLink::getValue)
            .filter(Objects::nonNull)
            .map(CaseLinkDetails::getCaseReference)
            .collect(Collectors.toList());

        log.info("{} linked case references found for case: {}", linkedReferences.size(), caseData.getCaseReference());

        List<SscsCaseData> linkedCases = ccdCaseService.getCases(linkedReferences);

        List<ServiceLinkedCases> serviceLinkedCases = new ArrayList<>();

        for (SscsCaseData linkedCase : linkedCases) {
            serviceLinkedCases.add(
                ServiceLinkedCases.builder()
                    .caseReference(linkedCase.getCaseReference())
                    .caseName(linkedCase.getCaseAccessManagementFields().getCaseNamePublic())
                    .reasonsForLink(HearingsCaseMapping.getReasonsForLink(caseData))
                    .build());
        }

        return serviceLinkedCases;
    }
}
