package uk.gov.hmcts.reform.sscs.helper.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.ccd.domain.SchedulingAndListingFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingEvent;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class HearingsServiceHelperTest {

    private HearingWrapper wrapper;
    private ZonedDateTime testStart;

    @BeforeEach
    void setup() {
        wrapper = HearingWrapper.builder()
                .caseData(SscsCaseData.builder().build())
                .caseData(SscsCaseData.builder().build())
                .build();
    }


    @DisplayName("updateHearingId Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "1,2,2",
        "1,1,1",
        "2,1,1",
        "null,2,2",
        "1,null,null",
        "null,null,null",
    }, nullValues = {"null"})
    void updateHearingId(Long original, Long updated, Long expected) {
        wrapper.getCaseData().setSchedulingAndListingFields(SchedulingAndListingFields.builder()
                .activeHearingId(original)
                .build());
        HearingResponse response = HearingResponse.builder()
                .hearingRequestId(updated)
                .build();

        HearingsServiceHelper.updateHearingId(wrapper, response);

        assertThat(wrapper.getCaseData().getSchedulingAndListingFields().getActiveHearingId()).isEqualTo(expected);
    }

    @DisplayName("updateVersionNumber Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "1,2,2",
        "1,1,1",
        "2,1,1",
        "null,2,2",
        "1,null,null",
        "null,null,null",
    }, nullValues = {"null"})
    void updateVersionNumber(Long original, Long updated, Long expected) {
        wrapper.getCaseData().setSchedulingAndListingFields(SchedulingAndListingFields.builder()
                .activeHearingVersionNumber(original)
                .build());
        HearingResponse response = HearingResponse.builder()
                .versionNumber(updated)
                .build();

        HearingsServiceHelper.updateVersionNumber(wrapper, response);

        assertThat(wrapper.getCaseData().getSchedulingAndListingFields().getActiveHearingVersionNumber()).isEqualTo(expected);
    }

    @DisplayName("addEvent Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "CREATE_HEARING,CREATE_HEARING",
        "UPDATED_CASE,UPDATED_CASE",
    }, nullValues = {"null"})
    void addEvent(HearingState state, HearingEvent event) {
        testStart = ZonedDateTime.now().minusMinutes(65);

        wrapper.setState(state);
        wrapper.getCaseData().setEvents(new ArrayList<>());

        HearingsServiceHelper.addEvent(wrapper);

        assertThat(wrapper.getCaseData().getEvents()).isNotEmpty();
        EventDetails eventDetails = wrapper.getCaseData().getEvents().get(0).getValue();
        assertThat(eventDetails.getType()).isEqualTo(event.getEventType().getType());
        assertThat(eventDetails.getDate()).isNotEmpty();
        assertThat(eventDetails.getDateTime()).isAfter(testStart);
        assertThat(eventDetails.getDateTime()).isBefore(ZonedDateTime.now().plusMinutes(65));
        assertThat(eventDetails.getDescription()).isEqualTo(event.getDescription());
    }

    @DisplayName("addEvent with null Event List Test")
    @Test
    void addEvent() {
        testStart = ZonedDateTime.now().minusMinutes(65);

        wrapper.setState(HearingState.CREATE_HEARING);

        HearingsServiceHelper.addEvent(wrapper);

        assertThat(wrapper.getCaseData().getEvents()).isNotEmpty();
        EventDetails eventDetails = wrapper.getCaseData().getEvents().get(0).getValue();
        assertThat(eventDetails.getType()).isEqualTo(HearingEvent.CREATE_HEARING.getEventType().getType());
        assertThat(eventDetails.getDate()).isNotEmpty();
        assertThat(eventDetails.getDateTime()).isAfter(testStart);
        assertThat(eventDetails.getDateTime()).isBefore(ZonedDateTime.now().plusMinutes(65));
        assertThat(eventDetails.getDescription()).isEqualTo(HearingEvent.CREATE_HEARING.getDescription());
    }
}
