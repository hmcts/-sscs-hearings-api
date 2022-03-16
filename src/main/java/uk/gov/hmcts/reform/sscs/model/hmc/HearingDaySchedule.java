package uk.gov.hmcts.reform.sscs.model.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.exception.ValidationError;

import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.Size;


@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingDaySchedule {

    private LocalDateTime hearingStartDateTime;
    private LocalDateTime hearingEndDateTime;
    @Size(max = 60, message = ValidationError.LIST_ASSIST_SESSION_ID_MAX_LENGTH)
    private String listAssistSessionID;

    @Size(max = 60, message = ValidationError.HEARING_VENUE_ID_MAX_LENGTH)
    private String hearingVenueId;

    @Size(max = 60, message = ValidationError.HEARING_ROOM_ID_MAX_LENGTH)
    private String hearingRoomId;

    @Size(max = 60, message = ValidationError.HEARING_JUDGE_ID_MAX_LENGTH)
    private String hearingJudgeId;

    @Size(max = 60, message = ValidationError.PANEL_MEMBER_ID_MAX_LENGTH)
    private String panelMemberId;

    private List<Attendees> attendees;

}
