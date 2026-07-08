package com.lksnext.ParkingAAldai

import com.lksnext.ParkingAAldai.data.models.ReservationEntity
import com.lksnext.ParkingAAldai.data.models.VehicleEntity
import com.lksnext.ParkingAAldai.ui.screens.SpotType
import com.lksnext.ParkingAAldai.validation.BookingValidator
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class BookingValidatorTest {
    private val today = 1_700_000_000_000L

    private val combustionVehicle = VehicleEntity(
        ownerEmail = "user@lksnext.com",
        plate = "1234ABC",
        brand = "Toyota",
        type = SpotType.COMBUSTION.name
    )

    private val motorcycleVehicle = VehicleEntity(
        ownerEmail = "user@lksnext.com",
        plate = "9999MOTO",
        brand = "Yamaha",
        type = SpotType.MOTORCYCLE.name
    )

    private val baseDay = BookingValidator.normalizeToStartOfDay(1_700_000_000_000L)

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

    @Test
     fun endedReservation_isNotOccupiedAnymore() {
        val reservation = ReservationEntity(
            spotIndex = 3,
            dateMillis = baseDay,
            startTime = "08:00",
            endTime = "09:00"
        )

        val nowMillis = baseDay + (10L * 60 * 60 * 1000)

        assertFalse(
            BookingValidator.isSpotOccupied(
                reservation = reservation,
                selectedDateMillis = baseDay,
                nowMillis = nowMillis
            )
        )
    }

    @Test
    fun activeReservation_isOccupied() {
        val reservation = ReservationEntity(
            spotIndex = 3,
            dateMillis = baseDay,
            startTime = "09:00",
            endTime = "11:00"
        )

        val nowMillis = Calendar.getInstance().apply {
            timeInMillis = baseDay
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        assertTrue(
            BookingValidator.isSpotOccupied(
                reservation = reservation,
                selectedDateMillis = baseDay,
                nowMillis = nowMillis
            )
        )
    }

    @Test
    fun activeEveningReservation_usesLocalTimeForOccupancy() {
        val originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Madrid"))
        try {
            val reservationDay = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                set(2026, Calendar.JULY, 8, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val nowDuringReservation = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid")).apply {
                set(2026, Calendar.JULY, 8, 18, 50, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val reservation = ReservationEntity(
                spotIndex = 3,
                dateMillis = reservationDay,
                startTime = "18:00",
                endTime = "19:00"
            )

            assertTrue(
                BookingValidator.isSpotOccupied(
                    reservation = reservation,
                    selectedDateMillis = reservationDay,
                    nowMillis = nowDuringReservation
                )
            )
        } finally {
            TimeZone.setDefault(originalTimeZone)
        }
    }

    @Test
    fun futureReservation_isOccupiedForSelectedFutureDate() {
        val reservation = ReservationEntity(
            spotIndex = 3,
            dateMillis = baseDay + (24L * 60 * 60 * 1000),
            startTime = "09:00",
            endTime = "11:00"
        )

        assertFalse(
            BookingValidator.isSpotOccupied(
                reservation = reservation,
                selectedDateMillis = baseDay + (24L * 60 * 60 * 1000),
                nowMillis = baseDay
            )
        )
    }

    @Test
    fun futureReservation_hasUpcomingStatus() {
        val reservation = ReservationEntity(
            spotIndex = 3,
            dateMillis = baseDay + (24L * 60 * 60 * 1000),
            startTime = "09:00",
            endTime = "11:00"
        )

        assertEquals(
            BookingValidator.ReservationStatus.UPCOMING,
            BookingValidator.getReservationStatus(
                reservation = reservation,
                selectedDateMillis = reservation.dateMillis,
                nowMillis = baseDay
            )
        )
    }

    @Test
    fun endedReservation_hasFinishedStatus() {
        val reservation = ReservationEntity(
            spotIndex = 3,
            dateMillis = baseDay,
            startTime = "08:00",
            endTime = "09:00"
        )

        val nowMillis = Calendar.getInstance().apply {
            timeInMillis = baseDay
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        assertEquals(
            BookingValidator.ReservationStatus.FINISHED,
            BookingValidator.getReservationStatus(
                reservation = reservation,
                selectedDateMillis = baseDay,
                nowMillis = nowMillis
            )
        )
    }
}