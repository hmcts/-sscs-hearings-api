package uk.gov.hmcts.reform.sscs.helper.mapping;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appointee;
import uk.gov.hmcts.reform.sscs.ccd.domain.Entity;
import uk.gov.hmcts.reform.sscs.ccd.domain.Interpreter;
import uk.gov.hmcts.reform.sscs.ccd.domain.JointParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.EntityRoleCode;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping.buildHearingCaseDetails;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsDetailsMapping.buildHearingDetails;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsPartiesMapping.buildHearingPartiesDetails;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsRequestMapping.buildHearingRequestDetails;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.EntityRoleCode.APPELLANT;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.EntityRoleCode.APPOINTEE;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.EntityRoleCode.INTERPRETER;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.EntityRoleCode.JOINT_PARTY;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.EntityRoleCode.OTHER_PARTY;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.EntityRoleCode.REPRESENTATIVE;

@Slf4j
@SuppressWarnings({"PMD.TooManyStaticImports","PMD.ExcessiveImports"})
public final class HearingsMapping {

    public static final String DWP_ID = "DWP";
    public static final String DWP_ORGANISATION_TYPE = "OGD";

    private HearingsMapping() {
    }

    public static HearingRequestPayload buildHearingPayload(HearingWrapper wrapper, ReferenceDataServiceHolder referenceDataServiceHolder)
        throws InvalidMappingException {
        return HearingRequestPayload.builder()
            .requestDetails(buildHearingRequestDetails(wrapper))
            .hearingDetails(buildHearingDetails(wrapper, referenceDataServiceHolder))
            .caseDetails(buildHearingCaseDetails(wrapper, referenceDataServiceHolder))
            .partiesDetails(buildHearingPartiesDetails(wrapper, referenceDataServiceHolder))
            .build();
    }

    public static SessionCategoryMap getSessionCaseCode(SscsCaseData caseData,
                                                        ReferenceDataServiceHolder referenceDataServiceHolder) {
        boolean doctorSpecialistSecond = isNotBlank(caseData.getSscsIndustrialInjuriesData().getSecondPanelDoctorSpecialism());
        boolean fqpmRequired = isYes(caseData.getIsFqpmRequired());
        return referenceDataServiceHolder.getSessionCategoryMaps()
                .getSessionCategory(caseData.getBenefitCode(), caseData.getIssueCode(),
                        doctorSpecialistSecond, fqpmRequired);
    }

    public static EntityRoleCode getEntityRoleCode(Entity entity) {
        if (entity instanceof Appellant) {
            return APPELLANT;
        }
        if (entity instanceof Appointee) {
            return APPOINTEE;
        }
        if (entity instanceof Interpreter) {
            return INTERPRETER;
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
