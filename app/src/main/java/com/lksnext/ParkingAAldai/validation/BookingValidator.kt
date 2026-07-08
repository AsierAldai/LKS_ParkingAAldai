package com.lksnext.ParkingAAldai.validation

import com.lksnext.ParkingAAldai.data.models.ReservationEntity
import com.lksnext.ParkingAAldai.data.models.VehicleEntity
import com.lksnext.ParkingAAldai.ui.screens.SpotType
import java.util.Calendar
import java.util.TimeZone

object BookingValidator {
    private const val MIN_RESERVATION_MINUTES = 1
    private const val MAX_RESERVATION_MINUTES = 9 * 60
    private const val OPENING_MINUTES = 8 * 60
    private const val CLOSING_MINUTES = 19 * 60

    enum class ReservationStatus {
        UPCOMING,
        ACTIVE,
        FINISHED
    }

    fun validateReservation(
        spotIndex: Int,
        spotType: SpotType,
        selectedDateMillis: Long,
        vehicle: VehicleEntity,
        start: String,
        end: String,
        todayMillis: Long
    ): String? {
        if (spotIndex < 0) return "Plaza inválida"

        val maxDateMillis = todayMillis + (7L * 24 * 60 * 60 * 1000L)
        if (selectedDateMillis !in todayMillis..maxDateMillis) {
            return "Solo puedes reservar entre hoy y los próximos 7 días"
        }

        val startMinutes = timeToMinutesOrNull(start) ?: return "Formato de hora inválido"
        val endMinutes = timeToMinutesOrNull(end) ?: return "Formato de hora inválido"

        if (startMinutes < OPENING_MINUTES || endMinutes > CLOSING_MINUTES) {
            return "El horario de reserva debe estar entre 08:00 y 19:00"
        }

        if (endMinutes <= startMinutes) {
            return "La hora de fin debe ser posterior a la hora de inicio"
        }

        if (endMinutes - startMinutes > MAX_RESERVATION_MINUTES) {
            return "La reserva no puede superar las 9 horas"
        }

        if (!isVehicleCompatible(vehicle, spotType)) {
            return "El vehículo no es compatible con el tipo de plaza seleccionada"
        }

        return null
    }

    fun isTimeRangeValid(start: String, end: String): Boolean {
        val startMinutes = timeToMinutesOrNull(start) ?: return false
        val endMinutes = timeToMinutesOrNull(end) ?: return false
        val duration = endMinutes - startMinutes

        return startMinutes >= OPENING_MINUTES &&
                endMinutes <= CLOSING_MINUTES &&
                duration >= MIN_RESERVATION_MINUTES &&
                duration <= MAX_RESERVATION_MINUTES
    }

    fun compatibleVehicle(
        vehicles: List<VehicleEntity>,
        spotType: SpotType
    ): List<VehicleEntity> {
        return vehicles.filter { isVehicleCompatible(it, spotType) }
    }

    fun isVehicleCompatible(vehicle: VehicleEntity, spotType: SpotType): Boolean {
        return when (spotType) {
            SpotType.MOTORCYCLE -> vehicle.type == SpotType.MOTORCYCLE.name
            SpotType.DISABLED -> vehicle.type == SpotType.DISABLED.name
            SpotType.COMBUSTION,
            SpotType.ELECTRIC -> vehicle.type == SpotType.COMBUSTION.name ||
                    vehicle.type == SpotType.ELECTRIC.name
        }
    }

    fun hasOverlap(
        start: String,
        end: String,
        reservations: List<ReservationEntity>
    ): Boolean {
        return reservations.any {
            overlaps(start, end, it.startTime, it.endTime)
        }
    }

    fun overlaps(
        newStart: String,
        newEnd: String,
        existingStart: String,
        existingEnd: String
    ): Boolean {
        val start = timeToMinutesOrNull(newStart) ?: return true
        val end = timeToMinutesOrNull(newEnd) ?: return true
        val otherStart = timeToMinutesOrNull(existingStart) ?: return true
        val otherEnd = timeToMinutesOrNull(existingEnd) ?: return true

        return start < otherEnd && end > otherStart
    }

    fun isSpotOccupied(
        reservation: ReservationEntity,
        selectedDateMillis: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        return getReservationStatus(reservation, selectedDateMillis, nowMillis) == ReservationStatus.ACTIVE
    }

    fun getReservationStatus(
        reservation: ReservationEntity,
        selectedDateMillis: Long = reservation.dateMillis,
        nowMillis: Long = System.currentTimeMillis()
    ): ReservationStatus {
        if (reservation.dateMillis != selectedDateMillis) return ReservationStatus.FINISHED

        val selectedDay = normalizeToStartOfDay(selectedDateMillis)
        val today = normalizeToStartOfDay(nowMillis)

        if (selectedDay > today) return ReservationStatus.UPCOMING
        if (selectedDay < today) return ReservationStatus.FINISHED

        val reservationStart = combineLocalDateAndTime(reservation.dateMillis, reservation.startTime)
        val reservationEnd = combineLocalDateAndTime(reservation.dateMillis, reservation.endTime)

        return when {
            nowMillis < reservationStart -> ReservationStatus.UPCOMING
            nowMillis < reservationEnd -> ReservationStatus.ACTIVE
            else -> ReservationStatus.FINISHED
        }
    }

    fun timeToMinutesOrNull(time: String): Int? {
        val parts = time.split(":")
        if (parts.size != 2) return null

        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null

        if (hour !in 0..23 || minute !in 0..59) return null

        return hour * 60 + minute
    }

    fun normalizeToStartOfDay(millis: Long): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun combineLocalDateAndTime(dateMillis: Long, time: String): Long {
        val parts = time.split(":")
        val hour = parts[0].toIntOrNull() ?: return dateMillis
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return dateMillis

        val cal = Calendar.getInstance()
        cal.timeInMillis = dateMillis
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}