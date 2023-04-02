package com.matzip.server.global.config;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class S3Config {
    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    @Profile("!local")
    public AmazonS3 amazonS3Client() {
        return AmazonS3ClientBuilder
                .standard()
                .withRegion(region)
                .withCredentials(InstanceProfileCredentialsProvider.getInstance())
                .build();
    }

    @Bean
    @Profile("local")
    public AmazonS3 localAmazonS3Client() {
        return AmazonS3ClientBuilder
                .standard()
                .withRegion(region)
                .build();
    }
}
