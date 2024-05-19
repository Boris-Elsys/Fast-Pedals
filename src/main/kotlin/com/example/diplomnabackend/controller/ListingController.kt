package com.example.diplomnabackend.controller

import com.example.diplomnabackend.dto.ListingBikeDTO
import com.example.diplomnabackend.dto.ListingDTO
import com.example.diplomnabackend.dto.WholeListingDTO
import com.example.diplomnabackend.service.DOS3Service
import com.example.diplomnabackend.service.ListingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/listing")
class ListingController (
    private val listingService: ListingService
) {

    @Autowired
    private lateinit var doS3Service: DOS3Service

    data class PresignedUrlResponse(val presignedUrl: String)

    @PostMapping("/generate-presigned-upload-url")
    fun generatePresignedUploadUrl(
        @RequestParam key: String,
        @RequestParam contentType: String
    ): ResponseEntity<PresignedUrlResponse> {

        val presignedUrl = doS3Service.getPreSignedPutUrl(key, contentType)
        val response = PresignedUrlResponse(presignedUrl)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/generate-presigned-download-url")
    fun generatePresignedDownloadUrl(
        @RequestParam key: String
    ): ResponseEntity<PresignedUrlResponse> {

        val presignedUrl = doS3Service.getPreSignedGetUrl(key)
        val response = PresignedUrlResponse(presignedUrl)

        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun getAllListings(): ResponseEntity<List<ListingDTO>> {
        return ResponseEntity.ok(listingService.findAll())
    }

    @GetMapping("/{id}")
    fun getListingById(@PathVariable id: Long): ResponseEntity<ListingDTO> {
        return ResponseEntity.ok(listingService.findById(id))
    }

    @GetMapping("/user/favourites")
    fun getFavouriteListings(): ResponseEntity<List<ListingDTO>> {
        return ResponseEntity.ok(listingService.getFavouriteListings())
    }

    @GetMapping("/user/whole/{id}")
    fun getWholeListing(@PathVariable id: Long): ResponseEntity<WholeListingDTO> {
        return ResponseEntity.ok(listingService.getWholeListing(id))
    }

    @PostMapping
    fun saveListing(@RequestBody listingDTO: ListingDTO): ResponseEntity<ListingDTO> {
        return ResponseEntity.ok(listingService.save(listingDTO))
    }

    @PostMapping("/user")
    fun saveListingByUser(@RequestBody listingDTO: ListingBikeDTO): ResponseEntity<ListingDTO> {
        return ResponseEntity.ok(listingService.saveByUser(listingDTO))
    }

    @PutMapping("/{id}")
    fun updateListing(@PathVariable id: Long, @RequestBody updatedListingDTO: ListingDTO): ResponseEntity<ListingDTO> {
        return ResponseEntity.ok(listingService.update(id, updatedListingDTO))
    }

    @PutMapping("/user/edit")
    fun updateListingByUser(@RequestBody updatedListingDTO: ListingBikeDTO): ResponseEntity<ListingDTO> {
        return ResponseEntity.ok(listingService.updateByUser(updatedListingDTO))
    }

    @DeleteMapping("/{id}")
    fun deleteListing(@PathVariable id: Long): ResponseEntity<Any> {
        listingService.deleteById(id)
        return ResponseEntity.ok("Listing with id: $id deleted successfully")
    }

    @GetMapping("/search")
    fun searchListings(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) minPrice: Double?,
        @RequestParam(required = false) maxPrice: Double?,
        @RequestParam(required = false) location: String?,
        @RequestParam(required = false) description: String?,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) brand: String?,
        @RequestParam(required = false) model: String?,
        @RequestParam(required = false) size: String?,
        @RequestParam(required = false) wheelSize: Int?,
        @RequestParam(required = false) frameMaterial: String?,
        @RequestParam(required = false) userId: Long?
    ): ResponseEntity<List<ListingDTO?>?> {
        val result = listingService.searchListings(
            title, minPrice, maxPrice, location, description, type, brand, model, size, wheelSize, frameMaterial, userId
        )
        return ResponseEntity.ok(result)
    }

}