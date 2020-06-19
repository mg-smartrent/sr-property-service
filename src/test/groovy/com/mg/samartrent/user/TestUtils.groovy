package com.mg.samartrent.user

import com.mg.smartrent.domain.enums.EnBuildingType
import com.mg.smartrent.domain.enums.EnCurrency
import com.mg.smartrent.domain.enums.EnPropertyCondition
import com.mg.smartrent.domain.enums.EnRentalApplicationStatus
import com.mg.smartrent.domain.enums.EnUserStatus
import com.mg.smartrent.domain.models.Property
import com.mg.smartrent.domain.models.PropertyListing
import com.mg.smartrent.domain.models.RentalApplication
import com.mg.smartrent.domain.models.User

class TestUtils {

    static Property generateProperty() {

        Property property = new Property()

        property.setUserId("userId1234")
        property.setBuildingType(EnBuildingType.Condo.name())
        property.setCondition(EnPropertyCondition.Normal.name())
        property.setTotalRooms(10)
        property.setTotalBathRooms(5)
        property.setTotalBalconies(1)
        property.setThumbnail(null)
        property.setParkingAvailable(true)

        return property
    }

    static PropertyListing generatePropertyListing() {
        PropertyListing listing = new PropertyListing()
        listing.setUserId("mockedUserId")
        listing.setPropertyId("mockedPropertyId")
        listing.setListed(true)
        listing.setPrice(100)
        listing.setTotalViews(3)
        listing.setCheckInDate(new Date(System.currentTimeMillis()))
        listing.setCheckOutDate(new Date(System.currentTimeMillis() + 10000000000))
        return listing
    }

    static User generateUser() {

        User user = new User()
        user.setFirstName("FName")
        user.setLastName("LName")
        user.setEmail("test.user@domain.com")
        user.setPassword("12341234")
        user.setEnabled(true)
        user.setStatus(EnUserStatus.Pending)

        return user
    }

    static RentalApplication generateRentalApplication() {

        RentalApplication model = new RentalApplication()
        model.setRenterUserId("mockedUserId")
        model.setPropertyId("mockedPropertyId")
        model.setCheckInDate(new Date(System.currentTimeMillis() - 1000000000))
        model.setCheckOutDate(new Date(System.currentTimeMillis() + 1000000000))
        model.setPrice(100)
        model.setCurrency(EnCurrency.USD)
        return model
    }

}
