package uk.gov.hmcts.reform.sscs.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcHearingLocation;
import uk.gov.hmcts.reform.sscs.model.single.hearing.LocationType;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnumPatternValidatorTest {

    static Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void given_LocationIdIsNullOrEmpty_thenGiveUnsupportedErrorMessage(String locationId) {
        HmcHearingLocation location = getHearingLocation();
        location.setLocationId(locationId);
        Set<ConstraintViolation<HmcHearingLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals(
            "Unsupported type for locationId",
            violations.stream().collect(Collectors.toList()).get(0).getMessage()
        );
    }

    @Test
    void given_LocationIdIsInvalid_ThenGiveUnsupportedErrorMessage() {
        HmcHearingLocation location = new HmcHearingLocation();
        location.setLocationId("Loc");
        location.setLocationType("LocType");
        Set<ConstraintViolation<HmcHearingLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertThat(violations).extracting(ConstraintViolation::getMessage).contains("Unsupported type for locationId");
    }

    @Test
    void given_LocationIdIsValid_ThenSetEnumValue() {
        HmcHearingLocation location = new HmcHearingLocation();
        location.setLocationId(LocationType.COURT.toString());
        location.setLocationType("LocType");
        Set<ConstraintViolation<HmcHearingLocation>> violations = validator.validate(location);
        assertTrue(violations.isEmpty());
    }

    private HmcHearingLocation getHearingLocation() {
        HmcHearingLocation location = new HmcHearingLocation();
        location.setLocationType("LocType");
        return location;
    }
}
