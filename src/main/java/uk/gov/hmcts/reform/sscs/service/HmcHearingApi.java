package uk.gov.hmcts.reform.sscs.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.sscs.config.FeignClientConfig;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDeleteRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "hmc-hearing", url = "${hmc.url}", configuration = FeignClientConfig.class)
public interface HmcHearingApi {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String HEARING_ENDPOINT = "/hearing";
    String ID = "id";

    @PostMapping(value = HEARING_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    HearingResponse createHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody HearingRequestPayload hearingPayload
    );

    @PutMapping(value = HEARING_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    HearingResponse updateHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam(ID) String id,
        @RequestBody HearingRequestPayload hearingPayload
    );

    @GetMapping(value = HEARING_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    HearingGetResponse getHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam(ID) String id
    );

    @DeleteMapping(value = HEARING_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    HearingResponse deleteHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam(ID) String id,
        @RequestBody HearingDeleteRequestPayload hearingDeletePayload
    );

}
