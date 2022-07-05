package uk.gov.hmcts.reform.sscs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.*;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsRequestMapping;
import uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper;
import uk.gov.hmcts.reform.sscs.model.HearingEvent;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingCancelRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcUpdateResponse;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.buildHearingPayload;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.updateIds;
import static uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper.getHearingId;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.HEARING_REQUESTED;

@SuppressWarnings({"PMD.UnusedFormalParameter"})
// TODO Unsuppress in future
@Slf4j
@Service
@RequiredArgsConstructor
public class HearingsService {

    @Value("${retry.hearing-response-update.max-retries}")
    private static int hearingResponseUpdateMaxRetries;

    private final HmcHearingApiService hmcHearingApiService;

    private final CcdCaseService ccdCaseService;

    private final ReferenceDataServiceHolder referenceDataServiceHolder;

    public void processHearingRequest(HearingRequest hearingRequest) throws GetCaseException, UnhandleableHearingStateException, UpdateCaseException, InvalidMappingException, GetHearingException {
        log.info("Processing Hearing Request for Case ID {}, Hearing State {} and Route {} and Cancellation Reason {}",
                hearingRequest.getCcdCaseId(),
                hearingRequest.getHearingState(),
                hearingRequest.getHearingRoute(),
                hearingRequest.getCancellationReason());

        processHearingWrapper(createWrapper(hearingRequest));
    }

    public void processHearingWrapper(HearingWrapper wrapper)
        throws UnhandleableHearingStateException, UpdateCaseException, InvalidMappingException, GetHearingException {

        log.info("Processing Hearing Wrapper for Case ID {} and Hearing State {}",
                wrapper.getCaseData().getCcdCaseId(),
                wrapper.getState().getState());

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

    private void createHearing(HearingWrapper wrapper) throws UpdateCaseException, InvalidMappingException, GetHearingException {
        updateIds(wrapper);
        HmcUpdateResponse response = createHmcUpdateResponse(wrapper);
        hearingResponseUpdate(wrapper, response);
    }
    private void updateHearing(HearingWrapper wrapper) throws UpdateCaseException, InvalidMappingException {
        updateIds(wrapper);
        HearingRequestPayload hearingPayload = buildHearingPayload(wrapper, referenceDataServiceHolder);
        String hearingId = getHearingId(wrapper);
        HmcUpdateResponse response = hmcHearingApiService.sendUpdateHearingRequest(hearingPayload, hearingId);

        log.debug("Received Update Hearing Request Response for Case ID {}, Hearing State {} and Response:\n{}",
                wrapper.getCaseData().getCcdCaseId(),
                wrapper.getState().getState(),
                response.toString());

        hearingResponseUpdate(wrapper, response);
    }

    private HmcUpdateResponse createHmcUpdateResponse(HearingWrapper wrapper) throws GetHearingException, InvalidMappingException {
        HearingRequestPayload hearingPayload = buildHearingPayload(wrapper, referenceDataServiceHolder);
        String hearingId = getHearingId(wrapper);

        HearingGetResponse getResponse = hmcHearingApiService.getHearingRequest(hearingId);

        HmcUpdateResponse updateResponse;

        if (nonNull(getResponse) && HEARING_REQUESTED == getResponse.getRequestDetails().getStatus()) {
            updateResponse = buildFromHearingGetResponse(getResponse);
        } else {
            updateResponse = hmcHearingApiService.sendCreateHearingRequest(hearingPayload);
        }

        String hearingRequestType = isNull(getResponse) ? "Create" : "Get";

        log.debug(String.format("Received %s Hearing Request Response for Case ID {}, Hearing State {} and Response:\n{}", hearingRequestType),
                  wrapper.getCaseData().getCcdCaseId(),
                  wrapper.getState().getState(),
                  updateResponse.toString());
        return updateResponse;
    }

    private HmcUpdateResponse buildFromHearingGetResponse(HearingGetResponse response) {
        return HmcUpdateResponse.builder()
            .hearingRequestId(Long.parseLong(response.getRequestDetails().getHearingRequestId()))
            .status(response.getRequestDetails().getStatus())
            .timeStamp(response.getRequestDetails().getTimestamp())
            .versionNumber(response.getRequestDetails().getVersionNumber())
            .build();
    }

    private void updatedCase(HearingWrapper wrapper) {
        // TODO implement mapping for the event when a case is updated
    }

    private void cancelHearing(HearingWrapper wrapper) {
        String hearingId = getHearingId(wrapper);
        HearingCancelRequestPayload hearingPayload = HearingsRequestMapping.buildCancelHearingPayload(wrapper);
        HmcUpdateResponse response = hmcHearingApiService.sendCancelHearingRequest(hearingPayload, hearingId);

        log.debug("Received Cancel Hearing Request Response for Case ID {}, Hearing State {} and Response:\n{}",
                wrapper.getCaseData().getCcdCaseId(),
                wrapper.getState().getState(),
                response.toString());
        // TODO process hearing response
    }

    private void partyNotified(HearingWrapper wrapper) {
        // TODO SSCS-10075 - implement mapping for the event when a party has been notified, might not be needed
    }

    @Retryable(
        value = UpdateCaseException.class,
        maxAttemptsExpression = "${retry.hearing-response-update.max-retries}",
        backoff = @Backoff(delayExpression = "${retry.hearing-response-update.backoff}"))
    public void hearingResponseUpdate(HearingWrapper wrapper, HmcUpdateResponse response)
        throws UpdateCaseException {

        SscsCaseData caseData = wrapper.getCaseData();
        Long hearingRequestId = response.getHearingRequestId();
        String caseId = caseData.getCcdCaseId();

        log.info("Updating Case with Hearing Response for Case ID {}, Hearing ID {} and Hearing State {}",
            caseId,
            hearingRequestId,
            wrapper.getState().getState());

        Hearing hearing = HearingsServiceHelper.getHearingById(hearingRequestId, caseData);

        if (isNull(hearing)) {
            hearing = HearingsServiceHelper.createHearing(hearingRequestId);
            HearingsServiceHelper.addHearing(hearing, caseData);
        }

        HearingsServiceHelper.updateHearingId(hearing, response);
        HearingsServiceHelper.updateVersionNumber(hearing, response);

        HearingEvent event = HearingsServiceHelper.getHearingEvent(wrapper.getState());
        ccdCaseService.updateCaseData(
            caseData,
            event.getEventType(),
            event.getSummary(),
            event.getDescription());

        log.info("Case Updated with Hearing Response for Case ID {}, Hearing ID {}, Hearing State {} and CCD Event {}",
            caseId,
            hearingRequestId,
            wrapper.getState().getState(),
            event.getEventType().getCcdType());
    }

    @Recover
    public void hearingResponseUpdateRecover(UpdateCaseException exception, HearingWrapper wrapper, HmcUpdateResponse response) {

        SscsCaseData caseData = wrapper.getCaseData();
        Long hearingRequestId = response.getHearingRequestId();
        String caseId = caseData.getCcdCaseId();

        log.info("Updating Case with Hearing Response has failed {} times, sending cancellation request to HMC, for Case ID {}, Hearing ID {} and Hearing State {} with the exception: {}",
            hearingResponseUpdateMaxRetries,
            caseId,
            hearingRequestId,
            wrapper.getState().getState(),
            exception);

        log.info("Cancellation request Response received, rethrowing exception, for Case ID {}, Hearing ID {} and Hearing State {}",
            caseData.getCcdCaseId(),
            hearingRequestId,
            wrapper.getState().getState());

        throw new ExhaustedRetryException("Cancellation request Response received, rethrowing exception", exception);
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
                .cancellationReason(hearingRequest.getCancellationReason())
                .build();
    }
}
