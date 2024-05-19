package com.example.diplomnabackend.service.Impl

import com.example.diplomnabackend.dto.ListingDTO
import com.example.diplomnabackend.dto.ListingBikeDTO
import com.example.diplomnabackend.dto.WholeListingDTO
import com.example.diplomnabackend.entity.Bike
import com.example.diplomnabackend.mapper.BikeMapper
import com.example.diplomnabackend.mapper.ListingMapper.Companion.LISTINGMAPPER
import com.example.diplomnabackend.mapper.UserMapper
import com.example.diplomnabackend.repository.BikeRepository
import com.example.diplomnabackend.repository.FavouriteRepository
import com.example.diplomnabackend.repository.ListingRepository
import com.example.diplomnabackend.repository.UserRepository
import com.example.diplomnabackend.service.ListingService
import com.google.auth.oauth2.GoogleCredentials
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL


@Service
class ListingServiceImpl (
    private val listingRepository: ListingRepository,
    private val bikeRepository: BikeRepository,
    private val userRepository: UserRepository,
    private val favouriteRepository: FavouriteRepository

) : ListingService {

    val err = NoSuchElementException("Listing not found")

    val BASE_URL = "https://fcm.googleapis.com"
    val FCM_SEND_ENDPOINT = "/v1/projects/fast-pedals/messages:send"
    val FCM_SERVER_KEY_LOCATION = "fast-pedals-firebase-adminsdk.json"
    val FCM_AUTH_ENDPOINT = "https://www.googleapis.com/auth/firebase.messaging"

    override fun findAll(): List<ListingDTO> {
        return listingRepository.findAll().map { LISTINGMAPPER.toDto(it) }
    }

    override fun findById(id: Long): ListingDTO {
        val listing = listingRepository.findById(id).orElseThrow { err }
        return LISTINGMAPPER.toDto(listing)
    }

    override fun getFavouriteListings(): List<ListingDTO> {

        val user = userRepository.findByEmail(SecurityContextHolder.getContext().authentication.name)
        val favourites = favouriteRepository.findAllByUserId(user?.getId()!!).map { it.getListing()?.getId() }

        val listings = mutableListOf<ListingDTO>()

        favourites.forEach { favoriteId ->
            favoriteId?.let { id ->
                listingRepository.findById(id).ifPresent { listing ->
                    listings.add(LISTINGMAPPER.toDto(listing))
                }
            }
        }

        return listings

    }

    override fun getWholeListing(id: Long): WholeListingDTO {

        val listing = listingRepository.findById(id).orElseThrow { err }
        val bike = listing.getBike()
        val owner = listing.getUser()
        val user = userRepository.findByEmail(SecurityContextHolder.getContext().authentication.name)
        val isFavourite = favouriteRepository.existsByUserIdAndListingId(user?.getId()!!, id)

        val listingDTO = LISTINGMAPPER.toDto(listing)
        val bikeDTO = bike?.let { BikeMapper.BIKEMAPPER.toDto(it) }
        val userDTO = owner?.let { UserMapper.USERMAPPER.toDto(it) }

        val isOwner = owner?.getId() == user.getId()

        return WholeListingDTO(
            listingDTO.id,
            listingDTO.title,
            listingDTO.description,
            listingDTO.price,
            listingDTO.location,
            listingDTO.date,
            listingDTO.images,
            bikeDTO?.type!!,
            bikeDTO.brand,
            bikeDTO.model,
            bikeDTO.size,
            bikeDTO.wheelSize,
            bikeDTO.frameMaterial,
            bikeDTO.id,
            userDTO?.id!!,
            userDTO.name,
            userDTO.phoneNumber,
            isFavourite,
            isOwner
            )

    }

    override fun save(listingDTO: ListingDTO): ListingDTO {

        val listingEntity = LISTINGMAPPER.toEntity(listingDTO)
        bikeRepository.findById(listingDTO.bikeId).ifPresent { listingEntity.setBike(it) }
        listingEntity.setUser(userRepository.findByEmail(SecurityContextHolder.getContext().authentication.name))
        val savedListing = listingRepository.save(listingEntity)
        return LISTINGMAPPER.toDto(savedListing)

    }

    override fun saveByUser(listingDTO: ListingBikeDTO): ListingDTO {

        val listingEntity = LISTINGMAPPER.nameToEntity(listingDTO)
        val bikeDTO = BikeMapper.BIKEMAPPER.nameToDto(listingDTO)
        val bikeEntity: Bike = BikeMapper.BIKEMAPPER.toEntity(bikeDTO)
        val savedBike: Bike = bikeRepository.save(bikeEntity)

        listingEntity.setBike(savedBike)
        listingEntity.setUser(userRepository.findByEmail(SecurityContextHolder.getContext().authentication.name))

        val savedListing = listingRepository.save(listingEntity)

        return LISTINGMAPPER.toDto(savedListing)

    }

    override fun update(id: Long, updatedListingDTO: ListingDTO): ListingDTO {

        val existingListing = listingRepository.findById(id).orElseThrow { err }

        existingListing.apply {
            setTitle(updatedListingDTO.title)
            setDescription(updatedListingDTO.description)
            setPrice(updatedListingDTO.price)
            setLocation(updatedListingDTO.location)
            setImages(updatedListingDTO.images)
        }

        val updatedListing = listingRepository.save(existingListing)
        return LISTINGMAPPER.toDto(updatedListing)

    }

    private fun getAccessToken(): String {

        val credentials = GoogleCredentials.fromStream(FileInputStream(FCM_SERVER_KEY_LOCATION))
            .createScoped(listOf(FCM_AUTH_ENDPOINT))
        credentials.refresh()

        return credentials.accessToken.tokenValue
    }

    private fun sendNotification(token: String, title: String, message: String, accessToken: String): String {

        val requestBody = """
        {
           "message":{
              "token":$token,
              "notification":{
                "body":"$message",
                "title":"$title"
              }
           }
        }
    """.trimIndent()

        val url = URL("$BASE_URL$FCM_SEND_ENDPOINT")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Content-Type", "application/json; UTF-8")
        connection.doOutput = true

        val outputStream = connection.outputStream
        outputStream.write(requestBody.toByteArray())
        outputStream.flush()

        val responseCode = connection.responseCode
        val response = if (responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            "Notification error: HTTP $responseCode"
        }

        connection.disconnect()

        return response
    }

    override fun updateByUser(updatedListingDTO: ListingBikeDTO): ListingDTO {

        val existingListing = listingRepository.findById(updatedListingDTO.id).orElseThrow { err }

        val existingBike: Bike = bikeRepository.findById(updatedListingDTO.bikeId!!).orElseThrow { NoSuchElementException("Bike not found") }

        existingBike.apply {
            setType(updatedListingDTO.type)
            setBrand(updatedListingDTO.brand)
            setModel(updatedListingDTO.model)
            setSize(updatedListingDTO.size)
            setWheelSize(updatedListingDTO.wheelSize)
            setFrameMaterial(updatedListingDTO.frameMaterial)
        }

        val updatedBike: Bike = bikeRepository.save(existingBike)

        existingListing.apply {
            setTitle(updatedListingDTO.title)
            setDescription(updatedListingDTO.description)
            setPrice(updatedListingDTO.price)
            setLocation(updatedListingDTO.location)
            setImages(updatedListingDTO.images)
            setBike(updatedBike)
        }

        val updatedListing = listingRepository.save(existingListing)

        val favourites = favouriteRepository.findAllByListingId(updatedListing.getId())

        if(favourites.isEmpty()) return LISTINGMAPPER.toDto(updatedListing)

        val accessToken = getAccessToken()
        
        favourites.forEach { favourite ->
            val user = favourite.getUser()
            val fcmToken = user?.getFcm()
            if (fcmToken != null) {
                val notificationTitle = "Listing ${updatedListingDTO.title} Updated"
                val notificationMessage = "Current price: ${updatedListingDTO.price}"
                sendNotification(fcmToken, notificationTitle, notificationMessage, accessToken)
            }
        }

        return LISTINGMAPPER.toDto(updatedListing)
    }

    override fun deleteById(id: Long) {

        listingRepository.deleteById(id)

    }

    override fun searchListings(
        title: String?,
        minPrice: Double?,
        maxPrice: Double?,
        location: String?,
        description: String?,
        type: String?,
        brand: String?,
        model: String?,
        size: String?,
        wheelSize: Int?,
        frameMaterial: String?,
        userId: Long?
    ): List<ListingDTO?>? {

        val listings =listingRepository.searchListings(
            title, minPrice, maxPrice, location, description, type, brand, model, size, wheelSize, frameMaterial, userId
        )?: throw err

        return listings.map { LISTINGMAPPER.toDto(it!!) }
    }

}