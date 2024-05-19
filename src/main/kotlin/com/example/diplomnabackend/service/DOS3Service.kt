package com.example.diplomnabackend.service

interface DOS3Service {

    fun getPreSignedPutUrl(key: String, contentType: String): String
    fun getPreSignedGetUrl(key: String): String
}