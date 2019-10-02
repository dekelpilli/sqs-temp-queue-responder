package com.littlepay.services;


import com.amazonaws.services.sqs.AmazonSQSResponder;
import com.amazonaws.services.sqs.MessageContent;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.util.SQSMessageConsumer;
import com.amazonaws.services.sqs.util.SQSMessageConsumerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;

import static com.amazonaws.services.sqs.util.SQSQueueUtils.MESSAGE_ATTRIBUTE_TYPE_STRING;


@Slf4j
@Service
public class SQSRespondingService {

    private static final String SERIALISED_NULL = ".rO0ABXA=";

    private final AmazonSQSResponder amazonSQSResponder;
    private final SQSMessageConsumer sqsMessageConsumer;

    public SQSRespondingService(AmazonSQSResponder amazonSQSResponder,
                                SQSMessageConsumerBuilder sqsMessageConsumerBuilder,
                                @Value("${responder.queue-url}") String queueUrl) {
        this.amazonSQSResponder = amazonSQSResponder;
        this.sqsMessageConsumer = sqsMessageConsumerBuilder
                .withQueueUrl(queueUrl)
                .withConsumer(this::respond)
                .withExceptionHandler(this::handleException)
                .withPollingThreadCount(20)
                .build();
    }

    @PostConstruct
    void startup() {
        sqsMessageConsumer.start();
    }

    @PreDestroy
    void shutdown() {
        sqsMessageConsumer.shutdown();
        amazonSQSResponder.shutdown();
    }

    public void handleException(Exception exception) {
        log.info("{} gave an exception", exception.getCause() == null
                        ? exception.getMessage()
                        : exception.getCause().getMessage(),
                exception);
    }

    public void respond(Message message) {
        String potentialNumber = message.getBody();

        if (potentialNumber == null || potentialNumber.equals(SERIALISED_NULL)) {
            log.error("\n\nRECEIVED NULL OR NULL-LIKE: {}\n\n", potentialNumber);
        }

        boolean isNumber = StringUtils.isNumeric(potentialNumber);
//        if (!isNumber) {
//            throw new RuntimeException(potentialNumber);
//        }
        log.info("Is {} a number? {}", potentialNumber, isNumber);
        amazonSQSResponder.sendResponseMessage(MessageContent.fromMessage(message),
                new MessageContent(Boolean.toString(isNumber),
                        Collections.singletonMap("Origin",
                                new MessageAttributeValue()
                                        .withStringValue("Responder").withDataType(MESSAGE_ATTRIBUTE_TYPE_STRING))));
    }
}
