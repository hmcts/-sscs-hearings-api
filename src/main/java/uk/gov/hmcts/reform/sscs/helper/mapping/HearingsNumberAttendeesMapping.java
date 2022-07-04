package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.JointParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping.isInterpreterRequired;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.FACE_TO_FACE;

public final class HearingsNumberAttendeesMapping {

    private HearingsNumberAttendeesMapping() {

    }

    public static int getNumberOfPhysicalAttendees(@Valid SscsCaseData caseData) {
        int numberOfAttendees = 0;

        if (FACE_TO_FACE != HearingsChannelMapping.getHearingChannel(caseData)) {
            return numberOfAttendees;
        }

        numberOfAttendees += getNumberOfAppellantAttendees(caseData.getAppeal(), caseData.getJointParty());
        numberOfAttendees += getNumberOfOtherPartyAttendees(caseData.getOtherParties());

        if (HearingsDetailsMapping.isPoOfficerAttending(caseData)) {
            numberOfAttendees++;
        }

        if (isInterpreterRequired(caseData)) {
            numberOfAttendees++;
        }

        return numberOfAttendees;
    }

    public static int getNumberOfAppellantAttendees(Appeal appeal, JointParty jointParty) {
        int numberOfAttendees = 0;
        if (isTrue(appeal.getHearingOptions().isWantsToAttendHearing())) {
            numberOfAttendees++;
            if (nonNull(appeal.getRep()) && isYes(appeal.getRep().getHasRepresentative())) {
                numberOfAttendees++;
            }
            if (isYes(jointParty.getHasJointParty())) {
                numberOfAttendees++;
            }
        }
        return numberOfAttendees;
    }

    public static long getNumberOfOtherPartyAttendees(List<CcdValue<OtherParty>> otherParties) {
        return Optional.ofNullable(otherParties).orElse(Collections.emptyList()).stream()
            .map(CcdValue::getValue)
            .map(OtherParty::getHearingOptions)
            .map(HearingOptions::getWantsToAttend)
            .filter(YesNo::isYes)
            .count();
    }
}
