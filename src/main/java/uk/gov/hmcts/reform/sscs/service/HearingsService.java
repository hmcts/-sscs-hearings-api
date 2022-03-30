package uk.gov.hmcts.reform.sscs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingStateException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsRequestMapping;
import uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.model.HearingEvent;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.*;
import static uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper.getHearingId;

@SuppressWarnings({"PMD.UnusedFormalParameter", "PMD.LawOfDemeter"})
// TODO Unsuppress in future
@Slf4j
@Service
@RequiredArgsConstructor
public class HearingsService {
    private final HmcHearingApi hmcHearingApi;

    private final CcdCaseService ccdCaseService;

    private final IdamService idamService;

    public void processHearingRequest(HearingRequest hearingRequest) throws GetCaseException, UnhandleableHearingStateException, UpdateCaseException {
        processHearingWrapper(createWrapper(hearingRequest));
    }

    public void processHearingWrapper(HearingWrapper wrapper) throws UnhandleableHearingStateException, UpdateCaseException {
        switch (wrapper.getState()) {
            case CREATE_HEARING:
                createHearing(wrapper);
                break;
            case UPDATE_HEARING:
                updateHearing(wrapper);
                break;
            case UPDATED_CASE:
                updatedCase(wrapper);
                break;
            case CANCEL_HEARING:
                cancelHearing(wrapper);
                break;
            case PARTY_NOTIFIED:
                partyNotified(wrapper);
                break;
            default:
                UnhandleableHearingStateException err = new UnhandleableHearingStateException(wrapper.getState());
                log.error(err.getMessage(),err);
                throw err;
        }
    }

    private void createHearing(HearingWrapper wrapper) throws UpdateCaseException {
        updateIds(wrapper);
        HearingResponse response = sendCreateHearingRequest(wrapper);
        hearingResponseUpdate(wrapper, response);
    }


    private void updateHearing(HearingWrapper wrapper) throws UpdateCaseException {
        updateIds(wrapper);
        HearingResponse response = sendUpdateHearingRequest(wrapper);
        hearingResponseUpdate(wrapper, response);
    }

    private void updatedCase(HearingWrapper wrapper) {
        // TODO implement mapping for the event when a case is updated
    }

    private void cancelHearing(HearingWrapper wrapper) {
        sendCancelHearingRequest(wrapper); // TODO: Get Reason in Ticket: SSCS-10366
    }

    private void partyNotified(HearingWrapper wrapper) {
        // TODO SSCS-10075 - implement mapping for the event when a party has been notified, might not be needed
    }

    private HearingResponse sendCreateHearingRequest(HearingWrapper wrapper) {
        return hmcHearingApi.createHearingRequest(
                idamService.getIdamTokens().getIdamOauth2Token(),
                idamService.getIdamTokens().getServiceAuthorization(),
                buildHearingPayload(wrapper)
        );
    }

    private HearingResponse sendUpdateHearingRequest(HearingWrapper wrapper) {
        return hmcHearingApi.updateHearingRequest(
                idamService.getIdamTokens().getIdamOauth2Token(),
                idamService.getIdamTokens().getServiceAuthorization(),
                getHearingId(wrapper),
                buildHearingPayload(wrapper)
        );
    }

    public HearingResponse sendCancelHearingRequest(HearingWrapper wrapper) {
        return hmcHearingApi.cancelHearingRequest(
                idamService.getIdamTokens().getIdamOauth2Token(),
                idamService.getIdamTokens().getServiceAuthorization(),
                String.valueOf(wrapper.getCaseData().getSchedulingAndListingFields().getActiveHearingId()),
                HearingsRequestMapping.buildCancelHearingPayload(null) // TODO: Get Reason in Ticket: SSCS-10366
        );
    }

    public void hearingResponseUpdate(HearingWrapper wrapper, HearingResponse response) throws UpdateCaseException {
        HearingsServiceHelper.updateHearingId(wrapper, response);
        HearingsServiceHelper.updateVersionNumber(wrapper, response);
        HearingsServiceHelper.addEvent(wrapper);

        HearingEvent event = HearingsServiceHelper.getHearingEvent(wrapper.getState());
        ccdCaseService.updateCaseData(
                wrapper.getCaseData(),
                event.getEventType(),
                event.getSummary(),
                event.getDescription());
    }

    private HearingWrapper createWrapper(HearingRequest hearingRequest) throws GetCaseException, UnhandleableHearingStateException {
        if (isNull(hearingRequest.getHearingState())) {
            UnhandleableHearingStateException err = new UnhandleableHearingStateException();
            log.error(err.getMessage(), err);
            throw err;
        }

        return HearingWrapper.builder()
                .caseData(ccdCaseService.getCaseDetails(hearingRequest.getCcdCaseId()).getData())
                .state(hearingRequest.getHearingState())
                .build();
    }
}
