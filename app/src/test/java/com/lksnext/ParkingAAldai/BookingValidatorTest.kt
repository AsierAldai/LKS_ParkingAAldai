package com.lksnext.ParkingAAldai

import com.lksnext.ParkingAAldai.data.models.ReservationEntity
import com.lksnext.ParkingAAldai.data.models.VehicleEntity
import com.lksnext.ParkingAAldai.ui.screens.SpotType
import com.lksnext.ParkingAAldai.validation.BookingValidator
import org.junit.Assert.*
import org.junit.Test

class BookingValidatorTest {
    private val today = 1_700_000_000_000L

    private val combustionVehicle = VehicleEntity(
        ownerEmail = "user@lks.com",
        plate = "1234ABC",
        brand = "Toyota",
        type = SpotType.COMBUSTION.name
    )

    private val motorcycleVehicle = VehicleEntity(
        ownerEmail = "user@lks.com",
        plate = "9999MOTO",
        brand = "Yamaha",
        type = SpotType.MOTORCYCLE.name
    )

    @Test
    fun validReservation_returnsNoError() {
        val result = BookingValidator.validateReservation(
            spotIndex = 3,
            spotType = SpotType.COMBUSTION,
            selectedDateMillis = today,
            vehicle = combustionVehicle,
            start = "08:00",
            end = "17:00",
            todayMillis = today
        )

        assertNull(result)
    }

    @Test
    fun endBeforeStart_returnsError() {
        val result = BookingValidator.validateReservation(
            spotIndex = 3,
            spotType = SpotType.COMBUSTION,
            selectedDateMillis = today,
            vehicle = combustionVehicle,
            start = "17:00",
            end = "08:00",
            todayMillis = today
        )

        assertEquals("La hora de fin debe ser posterior a la hora de inicio", result)
    }

    @Test
    fun reservationLongerThanNineHours_returnsError() {
        val result = BookingValidator.validateReservation(
            spotIndex = 3,
            spotType = SpotType.COMBUSTION,
            selectedDateMillis = today,
            vehicle = combustionVehicle,
            start = "08:00",
            end = "18:30",
            todayMillis = today
        )

        assertEquals("La reserva no puede superar las 9 horas", result)
    }

    @Test
    fun incompatibleVehicle_returnsError() {
        val result = BookingValidator.validateReservation(
            spotIndex = 3,
            spotType = SpotType.MOTORCYCLE,
            selectedDateMillis = today,
            vehicle = combustionVehicle,
            start = "08:00",
            end = "10:00",
            todayMillis = today
        )

        assertEquals("El vehículo no es compatible con el tipo de plaza seleccionada", result)
    }

    @Test
    fun motorcycleVehicle_isCompatibleWithMotorcycleSpot() {
        assertTrue(
            BookingValidator.isVehicleCompatible(
                vehicle = motorcycleVehicle,
                spotType = SpotType.MOTORCYCLE
            )
        )
    }

    @Test
    fun overlappingReservations_areDetected() {
        val reservations = listOf(
            ReservationEntity(
                spotIndex = 3,
                dateMillis = today,
                startTime = "09:00",
                endTime = "11:00",
            )
        )

        assertTrue(
            BookingValidator.hasOverlap(
                start = "10:00",
                end = "12:00",
                reservations = reservations
            )
        )
    }

    @Test
    fun adjacentReservations_doNotOverlap() {
        assertFalse(
            BookingValidator.overlaps(
                newStart = "11:00",
                newEnd = "12:00",
                existingStart = "09:00",
                existingEnd = "11:00"
            )
        )
    }

    @Test
    fun invalidSpot_returnsError() {
        val result = BookingValidator.validateReservation(
            spotIndex = -1,
            spotType = SpotType.COMBUSTION,
            selectedDateMillis = today,
            vehicle = combustionVehicle,
            start = "08:00",
            end = "10:00",
            todayMillis = today
        )
        assertEquals("Plaza inválida", result)
    }

    @Test
    fun invalidTimeFormat_returnsError() {
        val result = BookingValidator.validateReservation(
            spotIndex = 3,
            spotType = SpotType.COMBUSTION,
            selectedDateMillis = today,
            vehicle = combustionVehicle,
            start = "08:AA",
            end = "10:00",
            todayMillis = today
        )
        assertEquals("Formato de hora inválido", result)
    }

    @Test
    fun reservationBeforeOpening_returnsError() {
        val result = BookingValidator.validateReservation(
            spotIndex = 3,
            spotType = SpotType.COMBUSTION,
            selectedDateMillis = today,
            vehicle = combustionVehicle,
            start = "07:30",
            end = "10:00",
            todayMillis = today
        )
        assertEquals("El horario de reserva debe estar entre 08:00 y 19:00", result)
    }

    @Test
    fun reservationAfterClosing_returnsError() {
        val result = BookingValidator.validateReservation(
            spotIndex = 3,
            spotType = SpotType.COMBUSTION,
            selectedDateMillis = today,
            vehicle = combustionVehicle,
            start = "18:00",
            end = "19:30",
            todayMillis = today
        )
        assertEquals("El horario de reserva debe estar entre 08:00 y 19:00", result)
    }

    @Test
    fun reservationMoreThanSevenDaysAhead_returnsError() {
        val result = BookingValidator.validateReservation(
            spotIndex = 3,
            spotType = SpotType.COMBUSTION,
            selectedDateMillis = today + (8L * 24 * 60 * 60 * 1000),
            vehicle = combustionVehicle,
            start = "08:00",
            end = "10:00",
            todayMillis = today
        )
        assertEquals("Solo puedes reservar entre hoy y los próximos 7 días", result)
    }

    @Test
    fun shortValidTimeRange_isValid() {
        assertTrue(BookingValidator.isTimeRangeValid("08:00", "09:00"))
    }

    @Test
    fun timeRangeLongerThanNineHours_isInvalid() {
        assertFalse(BookingValidator.isTimeRangeValid("08:00", "18:30"))
    }

    @Test
    fun invalidTimeValues_returnNull() {
        assertNull(BookingValidator.timeToMinutesOrNull("25:00"))
        assertNull(BookingValidator.timeToMinutesOrNull("08:70"))
        assertNull(BookingValidator.timeToMinutesOrNull("wrong"))
    }
}