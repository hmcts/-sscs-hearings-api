package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.ccd.domain.AmendReason;
import uk.gov.hmcts.reform.sscs.model.HearingLocation;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("PMD.TooManyFields")
public class HearingDetails {

    private boolean autolistFlag;

    private String hearingType;

    private HearingWindow hearingWindow;

    private Number duration;

    private List<String> nonStandardHearingDurationReasons;

    private String hearingPriorityType;

    private Number numberOfPhysicalAttendees;

    private boolean hearingInWelshFlag;

    private List<HearingLocation> hearingLocations;

    private List<String> facilitiesRequired;

    private String listingComments;

    private String hearingRequester;

    private Boolean privateHearingRequiredFlag;

    private String leadJudgeContractType;

    private PanelRequirements panelRequirements;

    private boolean hearingIsLinkedFlag;

    private List<AmendReason> amendReasonCodes;

    private boolean multiDayHearing;

    private List<String> hearingChannels;

}
