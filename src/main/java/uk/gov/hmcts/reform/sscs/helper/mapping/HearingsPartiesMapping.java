package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.EntityRoleCode;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RelatedParty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.*;
import static uk.gov.hmcts.reform.sscs.model.EntityRoleCode.RESPONDENT;
import static uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType.IND;
import static uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType.ORG;

@SuppressWarnings({"PMD.UnnecessaryLocalBeforeReturn","PMB.LawOfDemeter","PMD.ReturnEmptyCollectionRatherThanNull", "PMD.GodClass"})
// TODO Unsuppress in future
public final class HearingsPartiesMapping {

    private HearingsPartiesMapping() {

    }

    public static List<PartyDetails> buildHearingPartiesDetails(HearingWrapper wrapper) {

        SscsCaseData caseData = wrapper.getCaseData();
        Appeal appeal = caseData.getAppeal();
        Appellant appellant = appeal.getAppellant();

        List<PartyDetails> partiesDetails = new ArrayList<>();

        if (isYes(caseData.getDwpIsOfficerAttending())) { // TODO SSCS-10243 - Might need to change
            partiesDetails.add(createDwpPartyDetails());
        }

        if (isYes(caseData.getJointParty().getHasJointParty())) {
            partiesDetails.add(createJointPartyDetails(caseData));
        }

        partiesDetails.addAll(buildHearingPartiesPartyDetails(
                appellant, appeal.getRep(), appeal.getHearingOptions(), appeal.getHearingType(), appeal.getHearingSubtype(), appellant.getId()));

        List<CcdValue<OtherParty>> otherParties = caseData.getOtherParties();

        if (nonNull(otherParties)) {
            for (CcdValue<OtherParty> ccdOtherParty : otherParties) {
                OtherParty otherParty = ccdOtherParty.getValue();
                partiesDetails.addAll(buildHearingPartiesPartyDetails(
                        otherParty, otherParty.getRep(), otherParty.getHearingOptions(), null, otherParty.getHearingSubtype(), appellant.getId()));
            }
        }

        return partiesDetails;
    }

    public static List<PartyDetails> buildHearingPartiesPartyDetails(Party party, Representative rep, HearingOptions hearingOptions, String hearingType, HearingSubtype hearingSubtype, String appellantId) {
        List<PartyDetails> partyDetails = new ArrayList<>();
        partyDetails.add(createHearingPartyDetails(party, hearingOptions, hearingType, hearingSubtype, party.getId(), appellantId));
        if (nonNull(party.getAppointee()) && isYes(party.getIsAppointee())) {
            partyDetails.add(createHearingPartyDetails(party.getAppointee(), hearingOptions, hearingType, hearingSubtype, party.getId(), appellantId));
        }
        if (nonNull(rep) && isYes(rep.getHasRepresentative())) {
            partyDetails.add(createHearingPartyDetails(rep, hearingOptions, hearingType, hearingSubtype, party.getId(), appellantId));
        }
        return partyDetails;
    }

    public static PartyDetails createHearingPartyDetails(Entity entity, HearingOptions hearingOptions, String hearingType, HearingSubtype hearingSubtype, String partyId, String appellantId) {
        PartyDetails.PartyDetailsBuilder partyDetails = PartyDetails.builder();

        partyDetails.partyID(getPartyId(entity));
        partyDetails.partyType(getPartyType(entity));
        partyDetails.partyRole(getPartyRole(entity));
        partyDetails.individualDetails(getPartyIndividualDetails(entity, hearingOptions, hearingType, hearingSubtype, partyId, appellantId));
        partyDetails.partyChannelSubType(getPartyChannelSubType());
        partyDetails.organisationDetails(getPartyOrganisationDetails());
        partyDetails.unavailabilityDayOfWeek(getPartyUnavailabilityDayOfWeek());
        partyDetails.unavailabilityRanges(getPartyUnavailabilityRange(hearingOptions));

        return partyDetails.build();
    }

    public static PartyDetails createDwpPartyDetails() {
        PartyDetails.PartyDetailsBuilder partyDetails = PartyDetails.builder();

        partyDetails.partyID(DWP_ID);
        partyDetails.partyType(ORG.name());
        partyDetails.partyRole(RESPONDENT.getKey()); // TODO Depends on SSCS-10273 - replace with common object
        partyDetails.organisationDetails(getDwpOrganisationDetails());
        partyDetails.unavailabilityDayOfWeek(getDwpUnavailabilityDayOfWeek());
        partyDetails.unavailabilityRanges(getDwpUnavailabilityRange());

        return partyDetails.build();
    }

    public static PartyDetails createJointPartyDetails(SscsCaseData caseData) {
        PartyDetails.PartyDetailsBuilder partyDetails = PartyDetails.builder();

        buildHearingPartiesPartyDetails(caseData.getJointParty(),caseData.getAppeal().getRep(),caseData.getAppeal().getHearingOptions()
                                        ,caseData.getAppeal().getHearingType(),caseData.getAppeal().getHearingSubtype(),
                                        caseData.getAppeal().getAppellant().getId());

        return PartyDetails.builder().build();
    }

    public static String getPartyId(Entity entity) {
        return entity.getId();
    }

    public static String getPartyType(Entity entity) {
        return isNotBlank(entity.getOrganisation()) ? ORG.name() : IND.name();
    }

    public static String getPartyRole(Entity entity) {
        return getEntityRoleCode(entity).getKey();
    }

    public static IndividualDetails getPartyIndividualDetails(Entity entity, HearingOptions hearingOptions, String hearingType, HearingSubtype hearingSubtype, String partyId, String appellantId) {
        return IndividualDetails.builder()
                .firstName(getIndividualFirstName(entity))
                .lastName(getIndividualLastName(entity))
                .preferredHearingChannel(getIndividualPreferredHearingChannel(hearingType, hearingSubtype))
                .interpreterLanguage(getIndividualInterpreterLanguage(hearingOptions))
                .reasonableAdjustments(getIndividualReasonableAdjustments(hearingOptions))
                .vulnerableFlag(isIndividualVulnerableFlag())
                .vulnerabilityDetails(getIndividualVulnerabilityDetails())
                .hearingChannelEmail(getIndividualHearingChannelEmail(entity))
                .hearingChannelPhone(getIndividualHearingChannelPhone(entity))
                .relatedParties(getIndividualRelatedParties(entity, partyId, appellantId))
                .custodyStatus(getIndividualCustodyStatus())
                .otherReasonableAdjustmentDetails(getIndividualOtherReasonableAdjustmentDetails())
                .build();
    }

    public static String getIndividualFirstName(Entity entity) {

        return entity.getName().getFirstName();
    }

    public static String getIndividualLastName(Entity entity) {
        return entity.getName().getLastName();
    }

    public static String getIndividualPreferredHearingChannel(String hearingType, HearingSubtype hearingSubtype) {
        // TODO Depends on SSCS-10273 - Needs to implement for Reference data of valid Hearing Channel codes
        return null;
    }

    public static String getIndividualInterpreterLanguage(HearingOptions hearingOptions) {
        // TODO Depends on SSCS-10273 - Needs to implement for Reference data to convert from SSCS Languages/Sign Languages to Reference languages
        // if (isYes(hearingOptions.getLanguageInterpreter())) {
        //     String signLanguageType = hearingOptions.getSignLanguageType();
        //     String languages = hearingOptions.getLanguages();
        // }
        return null;
    }

    public static List<String> getIndividualReasonableAdjustments(HearingOptions hearingOptions) {
        List<String> hmcArrangements = new ArrayList<>();
        // TODO Andrew Looking into - Needs to implement for Reference data to convert from SSCS Arrangements to Reference Arrangements
        // List<String> sscsArrangements = hearingOptions.getArrangements();
        return hmcArrangements;
    }

    public static boolean isIndividualVulnerableFlag() {
        // TODO Future Work
        return false;
    }

    public static String getIndividualVulnerabilityDetails() {
        // TODO Future Work
        return null;
    }

    public static List<String> getIndividualHearingChannelEmail(Entity entity) {
        List<String> emails = new ArrayList<>();
        if (nonNull(entity.getContact()) && isNotBlank(entity.getContact().getEmail())) {
            emails.add(entity.getContact().getEmail());
        }
        return emails;
    }

    public static List<String> getIndividualHearingChannelPhone(Entity entity) {
        List<String> phoneNumbers = new ArrayList<>();
        if (nonNull(entity.getContact())) {
            if (isNotBlank(entity.getContact().getMobile())) {
                phoneNumbers.add(entity.getContact().getMobile());
            }
            if (isNotBlank(entity.getContact().getPhone())) {
                phoneNumbers.add(entity.getContact().getPhone());
            }
        }
        return phoneNumbers;
    }

    public static List<RelatedParty> getIndividualRelatedParties(Entity entity, String partyId, String appellantId) {
        List<RelatedParty> relatedParties = new ArrayList<>();
        EntityRoleCode roleCode = getEntityRoleCode(entity);
        switch (roleCode) {
            case APPOINTEE:
            case REPRESENTATIVE:
                relatedParties.add(getRelatedParty(partyId, roleCode.getParentRole()));
                break;
            case OTHER_PARTY:
            case JOINT_PARTY:
                relatedParties.add(getRelatedParty(appellantId, roleCode.getParentRole()));
                break;
            default:
                break;
        }
        return relatedParties;
    }

    public static RelatedParty getRelatedParty(String id, String relationshipType) {
        return RelatedParty.builder()
                .relatedPartyId(id)
                .relationshipType(relationshipType)
                .build();
    }

    public static String getIndividualCustodyStatus() {
        // TODO Future work
        return null;
    }

    public static String getIndividualOtherReasonableAdjustmentDetails() {
        // TODO Future work
        return null;
    }

    public static String getPartyChannelSubType() {
        // TODO Future work
        return null;
    }

    public static OrganisationDetails getDwpOrganisationDetails() {
        return getOrganisationDetails(DWP_ID, DWP_ORGANISATION_TYPE, null);
    }

    public static OrganisationDetails getPartyOrganisationDetails() {
        // Not used as of now
        return null;
    }

    public static OrganisationDetails getOrganisationDetails(String name, String type, String id) {
        OrganisationDetails.OrganisationDetailsBuilder organisationDetails = OrganisationDetails.builder();
        organisationDetails.name(name);
        organisationDetails.organisationType(type);
        organisationDetails.cftOrganisationID(id);
        return organisationDetails.build();
    }

    public static List<UnavailabilityDayOfWeek> getPartyUnavailabilityDayOfWeek() {
        // Not used as of now
        return null;
    }

    public static List<UnavailabilityDayOfWeek> getDwpUnavailabilityDayOfWeek() {
        // Not used as of now
        return getPartyUnavailabilityDayOfWeek();
    }

    public static List<UnavailabilityRange> getPartyUnavailabilityRange(HearingOptions hearingOptions) {
        // TODO unavailabilityType - Future work
        if (nonNull(hearingOptions) && nonNull(hearingOptions.getExcludeDates())) {
            List<UnavailabilityRange> unavailabilityRanges = new ArrayList<>();
            for (ExcludeDate excludeDate : hearingOptions.getExcludeDates()) {
                DateRange dateRange = excludeDate.getValue();
                UnavailabilityRange.UnavailabilityRangeBuilder unavailabilityRange = UnavailabilityRange.builder();
                unavailabilityRange.unavailableFromDate(LocalDate.parse(dateRange.getStart()));
                unavailabilityRange.unavailableToDate(LocalDate.parse(dateRange.getEnd()));
                unavailabilityRanges.add(unavailabilityRange.build());
            }
            return unavailabilityRanges;
        } else {
            return null;
        }
    }

    public static List<UnavailabilityRange> getDwpUnavailabilityRange() {
        // Not used as of now
        return getPartyUnavailabilityRange(null);
    }
}


