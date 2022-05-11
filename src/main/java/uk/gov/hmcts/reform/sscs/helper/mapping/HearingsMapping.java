package uk.gov.hmcts.reform.sscs.helper.mapping;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.reference.data.mappings.EntityRoleCode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping.buildHearingCaseDetails;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsDetailsMapping.buildHearingDetails;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsPartiesMapping.buildHearingPartiesDetails;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsRequestMapping.buildHearingRequestDetails;
import static uk.gov.hmcts.reform.sscs.reference.data.mappings.EntityRoleCode.APPELLANT;
import static uk.gov.hmcts.reform.sscs.reference.data.mappings.EntityRoleCode.APPOINTEE;
import static uk.gov.hmcts.reform.sscs.reference.data.mappings.EntityRoleCode.JOINT_PARTY;
import static uk.gov.hmcts.reform.sscs.reference.data.mappings.EntityRoleCode.OTHER_PARTY;
import static uk.gov.hmcts.reform.sscs.reference.data.mappings.EntityRoleCode.REPRESENTATIVE;

@Slf4j
public final class HearingsMapping {

    public static final String DWP_ID = "DWP";
    public static final String DWP_ORGANISATION_TYPE = "OGD";

    private HearingsMapping() {
    }

    public static HearingRequestPayload buildHearingPayload(HearingWrapper wrapper) {
        return HearingRequestPayload.builder()
            .requestDetails(buildHearingRequestDetails(wrapper))
            .hearingDetails(buildHearingDetails(wrapper))
            .caseDetails(buildHearingCaseDetails(wrapper))
            .partiesDetails(buildHearingPartiesDetails(wrapper))
            .build();
    }

    public static void updateIds(HearingWrapper wrapper) {

        log.info("Updating entity IDs for Case ID {}", wrapper.getCaseData().getCcdCaseId());

        SscsCaseData caseData = wrapper.getCaseData();
        Appeal appeal = caseData.getAppeal();
        Appellant appellant = appeal.getAppellant();

        int maxId = getMaxId(caseData.getOtherParties(), appellant, appeal.getRep());

        maxId = updatePartyIds(appellant, appeal.getRep(), maxId);
        updateOtherPartiesIds(caseData.getOtherParties(), maxId);
    }

    private static void updateOtherPartiesIds(List<CcdValue<OtherParty>> otherParties, int maxId) {
        int newMaxId = maxId;
        if (nonNull(otherParties)) {
            for (CcdValue<OtherParty> otherPartyCcdValue : otherParties) {
                OtherParty otherParty = otherPartyCcdValue.getValue();
                newMaxId = updatePartyIds(otherParty, otherParty.getRep(), newMaxId);
            }
        }
    }

    private static int updatePartyIds(Party party, Representative rep, int maxId) {
        int newMaxId = maxId;
        newMaxId = updateEntityId(party, newMaxId);
        if (nonNull(party.getAppointee())) {
            newMaxId = updateEntityId(party.getAppointee(), newMaxId);
        }
        if (nonNull(rep)) {
            newMaxId = updateEntityId(rep, newMaxId);
        }
        return newMaxId;
    }

    public static int updateEntityId(Entity entity, int maxId) {
        String id = entity.getId();
        int newMaxId = maxId;
        if (isBlank(id)) {
            entity.setId(String.valueOf(++newMaxId));
        }
        return newMaxId;
    }

    public static int getMaxId(List<CcdValue<OtherParty>> otherParties, Appellant appellant, Representative rep) {
        return getAllIds(otherParties, appellant, rep).stream().max(Comparator.naturalOrder()).orElse(0);
    }

    public static List<Integer> getAllIds(List<CcdValue<OtherParty>> otherParties, Appellant appellant, Representative rep) {
        List<Integer> currentIds = new ArrayList<>();
        if (nonNull(otherParties)) {
            for (CcdValue<OtherParty> ccdOtherParty : otherParties) {
                currentIds.addAll(getAllPartyIds(ccdOtherParty.getValue(), ccdOtherParty.getValue().getRep()));
            }
        }

        currentIds.addAll(getAllPartyIds(appellant, rep));

        return currentIds;
    }

    public static List<Integer> getAllPartyIds(Party party, Representative rep) {
        List<Integer> currentIds = new ArrayList<>();

        if (party.getId() != null) {
            currentIds.add(Integer.parseInt(party.getId()));
        }
        if (party.getAppointee() != null && party.getAppointee().getId() != null) {
            currentIds.add(Integer.parseInt(party.getAppointee().getId()));
        }
        if (rep != null && rep.getId() != null) {
            currentIds.add(Integer.parseInt(rep.getId()));
        }

        return currentIds;
    }

    public static SessionCategoryMap getSessionCaseCode(SscsCaseData caseData) {
        boolean doctorSpecialistSecond = isNotBlank(caseData.getSscsIndustrialInjuriesData().getSecondPanelDoctorSpecialism());
        boolean fqpmRequired = isYes(caseData.getIsFqpmRequired());
        return SessionCategoryMap.getSessionCategory(caseData.getBenefitCode(), caseData.getIssueCode(), doctorSpecialistSecond, fqpmRequired);
    }

    public static EntityRoleCode getEntityRoleCode(Entity entity) {
        // TODO Future work - handle interpreter
        if (entity instanceof Appellant) {
            return APPELLANT;
        }
        if (entity instanceof Appointee) {
            return APPOINTEE;
        }
        if (entity instanceof Representative) {
            return REPRESENTATIVE;
        }
        if (entity instanceof JointParty) {
            return JOINT_PARTY;
        }
        return OTHER_PARTY;
    }

}
