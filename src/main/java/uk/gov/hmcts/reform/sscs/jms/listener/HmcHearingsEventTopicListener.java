package uk.gov.hmcts.reform.sscs.jms.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.exception.CaseException;
import uk.gov.hmcts.reform.sscs.exception.HmcEventProcessingException;
import uk.gov.hmcts.reform.sscs.exception.MessageProcessingException;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.service.hmc.topic.ProcessHmcMessageService;

import java.nio.charset.StandardCharsets;
import javax.jms.JMSException;

@Slf4j
@Component
@ConditionalOnExpression("!${flags.bypass-hearing-api-service.enabled:false} && ${flags.hmc-to-hearings-api.enabled:true}")
public class HmcHearingsEventTopicListener {

    private final ObjectMapper objectMapper;

    private final String sscsServiceCode;

    private final ProcessHmcMessageService processHmcMessageService;

    @Value("${hmc.deployment-id}")
    private String hmctsDeploymentId;

    @Value("${flags.deployment-filter.enabled}")
    private boolean isDeploymentFilterEnabled;

    private static final String HMCTS_DEPLOYMENT_ID = "hmctsDeploymentId";

    public HmcHearingsEventTopicListener(@Value("${sscs.serviceCode}") String sscsServiceCode,
                                         ProcessHmcMessageService processHmcMessageService) {
        this.sscsServiceCode = sscsServiceCode;
        this.processHmcMessageService = processHmcMessageService;
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @JmsListener(
        destination = "${azure.service-bus.hmc-to-hearings-api.topicName}",
        subscription = "${azure.service-bus.hmc-to-hearings-api.subscriptionName}",
        containerFactory = "hmcHearingsEventTopicContainerFactory"
    )
    public void onMessage(JmsBytesMessage message) throws JMSException, HmcEventProcessingException {

        if (isDeploymentFilterEnabled && !isMessageReleventForDeployment(message)) {
            return;
        }
        byte[] messageBytes = new byte[(int) message.getBodyLength()];
        message.readBytes(messageBytes);
        String convertedMessage = new String(messageBytes, StandardCharsets.UTF_8);

        try {
            HmcMessage hmcMessage = objectMapper.readValue(convertedMessage, HmcMessage.class);

            if (isMessageRelevantForService(hmcMessage)) {
                Long caseId = hmcMessage.getCaseId();
                String hearingId = hmcMessage.getHearingId();

                log.info(
                    "Attempting to process message from HMC hearings topic for event {}, Case ID {}, and Hearing ID {}.",
                    hmcMessage.getHearingUpdate().getHmcStatus(),
                    caseId,
                    hearingId
                );

                processHmcMessageService.processEventMessage(hmcMessage);
            }
        } catch (JsonProcessingException | CaseException | MessageProcessingException ex) {
            throw new HmcEventProcessingException(String.format(
                "Unable to successfully deliver HMC message: %s",
                convertedMessage
            ), ex);
        }

    }

    private boolean isMessageRelevantForService(HmcMessage hmcMessage) {
        return sscsServiceCode.equals(hmcMessage.getHmctsServiceCode());
    }

    private boolean isMessageReleventForDeployment(JmsBytesMessage message) throws JMSException {
        return hmctsDeploymentId.isEmpty()
            && message.getStringProperty(HMCTS_DEPLOYMENT_ID) == null
            || message.getStringProperty(HMCTS_DEPLOYMENT_ID) != null
            && message.getStringProperty(HMCTS_DEPLOYMENT_ID).equals(hmctsDeploymentId);
    }
}
