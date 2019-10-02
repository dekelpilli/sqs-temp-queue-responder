package com.littlepay.configurations;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSResponder;
import com.amazonaws.services.sqs.AmazonSQSResponderClientBuilder;
import com.amazonaws.services.sqs.util.SQSMessageConsumerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class SQSConfiguration {

    @Bean
    public AWSCredentials awsCredentials(@Value("${keys.access}") String access,
                                         @Value("${keys.secret}") String secret) {
        return new BasicAWSCredentials(access, secret);
    }

    @Bean
    public AWSCredentialsProvider awsCredentialsProvider(AWSCredentials awsCredentials) {
        return new AWSStaticCredentialsProvider(awsCredentials);
    }

    @Bean
    public AmazonSQSResponder amazonSQSResponder(AmazonSQS amazonSQS,
                                                 @Value("${responder.queue-prefix}") String prefix) {

        return AmazonSQSResponderClientBuilder.standard()
                .withAmazonSQS(amazonSQS)
                .withInternalQueuePrefix(prefix)
                .build();
    }

    @Bean
    public AmazonSQS amazonSQS(AWSCredentialsProvider awsCredentialsProvider) {
        return AmazonSQSClientBuilder.standard()
                .withCredentials(awsCredentialsProvider)
                .withRegion(Regions.AP_SOUTHEAST_2)
                .build();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SQSMessageConsumerBuilder sqsMessageConsumerBuilder(AmazonSQS amazonSQS) {
        return SQSMessageConsumerBuilder.standard()
                .withAmazonSQS(amazonSQS);
    }
}
