/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 */
package com.farao_community.farao.gridcapa.cgm_data_bridge;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.aws.outbound.S3MessageHandler;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@Configuration
public class MinioSink {
    public static final String AWS_CLIENT_SIGNER_TYPE = "AWSS3V4SignerType";
    @Value("${cgm-data-bridge.sinks.minio.url}")
    private String url;
    @Value("${cgm-data-bridge.sinks.minio.access-key}")
    private String accessKey;
    @Value("${cgm-data-bridge.sinks.minio.secret-key}")
    private String secretKey;
    @Value("${cgm-data-bridge.sinks.minio.bucket}")
    private String bucket;
    @Value("${cgm-data-bridge.sinks.minio.base-directory}")
    private String baseDirectory;

    @Bean
    public MessageChannel minioSinkChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "cgmFilesChannel")
    public MessageHandler s3MessageHandler() {
        S3MessageHandler s3MessageHandler = new S3MessageHandler(amazonS3(), bucket);
        Expression keyExpression = new SpelExpressionParser().parseExpression("'" + baseDirectory + "/' + headers.file_name");
        s3MessageHandler.setKeyExpression(keyExpression);
        return s3MessageHandler;
    }

    private AmazonS3 amazonS3() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSignerOverride(AWS_CLIENT_SIGNER_TYPE);

        return AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(url, Regions.US_EAST_1.name()))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

}
