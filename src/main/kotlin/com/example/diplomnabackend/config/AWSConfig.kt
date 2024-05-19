package com.example.diplomnabackend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region.EU_CENTRAL_1
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI


@Configuration
class AWSConfig (

    @Value("\${do.spaces.key}")
    private val accessKeyId: String,

    @Value("\${do.spaces.secret}")
    private val secretKey: String,

    @Value("\${do.spaces.endpoint}")
    private val endpoint: String

) {

    @Bean
    fun s3Presigner(): S3Presigner {
        val awsCreds = AwsBasicCredentials.create(accessKeyId, secretKey)
        return S3Presigner.builder()
            .region(EU_CENTRAL_1)
            .credentialsProvider { awsCreds }
            .endpointOverride(URI.create(endpoint))
            .build()
    }

}