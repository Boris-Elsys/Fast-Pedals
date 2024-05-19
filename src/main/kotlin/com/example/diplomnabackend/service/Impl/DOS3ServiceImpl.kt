package com.example.diplomnabackend.service.Impl

import com.example.diplomnabackend.service.DOS3Service
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

@Service
class DOS3ServiceImpl(

    private val s3Presigner: S3Presigner,

    @Value("\${do.spaces.bucket}")
    private val bucketName: String

) : DOS3Service {

    private val timeSeconds = 3600L

    override fun getPreSignedPutUrl(key: String, contentType: String): String {

        val time = timeSeconds
        val bucket = bucketName

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build()

        val putObjectPresignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(time))
            .putObjectRequest(putObjectRequest)
            .build()

        return s3Presigner.presignPutObject(putObjectPresignRequest).url().toString()
    }

    override fun getPreSignedGetUrl(key: String): String {

        val time = timeSeconds
        val bucket = bucketName

        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        val getObjectPresignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(time))
            .getObjectRequest(getObjectRequest)
            .build()

        return s3Presigner.presignGetObject(getObjectPresignRequest).url().toString()
    }
}