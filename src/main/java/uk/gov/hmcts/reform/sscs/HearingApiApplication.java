package uk.gov.hmcts.reform.sscs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class HearingApiApplication {

    public static void main(final String[] args) {
        SpringApplication.run(HearingApiApplication.class, args);
    }
}
