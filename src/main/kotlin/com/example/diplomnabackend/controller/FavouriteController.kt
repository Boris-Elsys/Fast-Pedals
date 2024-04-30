package com.example.diplomnabackend.controller

import com.example.diplomnabackend.dto.FavouriteDTO
import com.example.diplomnabackend.dto.ListingListDTO
import com.example.diplomnabackend.service.FavouriteService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/favourite")
class FavouriteController (
    private val favouriteService: FavouriteService
) {

    @GetMapping
    fun getAllFavourites(): ResponseEntity<List<FavouriteDTO>> {
        return ResponseEntity.ok(favouriteService.findAll())
    }

    @GetMapping("/{id}")
    fun getFavouriteById(@PathVariable id: Long): ResponseEntity<FavouriteDTO> {
        return ResponseEntity.ok(favouriteService.findById(id))
    }

    @GetMapping("/user")
    fun getFavouritesByUser(): ResponseEntity<List<ListingListDTO>> {
        return ResponseEntity.ok(favouriteService.findByUser())
    }

    @PostMapping
    fun saveFavourite(@RequestBody favouriteDTO: FavouriteDTO): ResponseEntity<FavouriteDTO> {
        return ResponseEntity.ok(favouriteService.save(favouriteDTO))
    }

    @PostMapping("/{id}")
    fun saveFavouriteByName(@PathVariable id: Long): ResponseEntity<FavouriteDTO> {
        return ResponseEntity.ok(favouriteService.saveByName(id))
    }

    @PostMapping("/check/{id}")
    fun checkFavourite(@PathVariable id: Long): ResponseEntity<Boolean> {
        return ResponseEntity.ok(favouriteService.check(id))
    }

    @DeleteMapping("/{id}")
    fun deleteFavourite(@PathVariable id: Long): ResponseEntity<Any> {
        favouriteService.deleteById(id)
        return ResponseEntity.ok("Favourite with id: $id deleted successfully")
    }

    @DeleteMapping("/delete/{id}")
    fun deleteFavouriteByUserIdAndListingId(@PathVariable id: Long): ResponseEntity<Any> {
        favouriteService.deleteByName(id)
        return ResponseEntity.ok("Favourite with listingId: $id deleted successfully")
    }

}