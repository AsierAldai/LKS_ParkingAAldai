package com.lksnext.ParkingAAldai

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import org.junit.Rule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lksnext.ParkingAAldai.data.models.ReservationEntity
import com.lksnext.ParkingAAldai.data.models.VehicleEntity
import com.lksnext.ParkingAAldai.ui.components.ReservationSheet
import com.lksnext.ParkingAAldai.ui.screens.SpotType
import org.junit.Assert.*
import org.junit.Test

class ReservationFlowInstrumentedTest {

    @get: Rule
    val composeRule = createComposeRule()

    private val selectedDate = 1_700_000_000_000L

    private val combustionVehicle = VehicleEntity(
        ownerEmail = "user@lksnext.com",
        plate = "1234ABC",
        brand = "Toyota",
        type = SpotType.COMBUSTION.name
    )

    @Test
    fun validReservation_confirmsReservation() {
        var confirmed = false

        composeRule.setContent {
            MaterialTheme {
                ReservationSheet(
                    selectedSpotIndex = 5,
                    spotType = SpotType.COMBUSTION,
                    selectedDateMillis = selectedDate,
                    userVehicles = listOf(combustionVehicle),
                    futureReservations = emptyList(),
                    onDismiss = {},
                    onConfirm = { vehicle, start, end ->
                        confirmed = vehicle.plate == "1234ABC" &&
                                start == "08:00" &&
                                end == "17:00"
                    }
                )
            }
        }

        composeRule.onNodeWithTag("vehicle_1234ABC").performClick()
        composeRule.onNodeWithTag("res_button").assertIsEnabled()
        composeRule.onNodeWithTag("res_button").performClick()

        assertTrue(confirmed)
    }

    @Test
    fun incompatibleVehicle_disablesReservation() {
        composeRule.setContent {
            MaterialTheme {
                ReservationSheet(
                    selectedSpotIndex = 5,
                    spotType = SpotType.MOTORCYCLE,
                    selectedDateMillis = selectedDate,
                    userVehicles = listOf(combustionVehicle),
                    futureReservations = emptyList(),
                    onDismiss = {},
                    onConfirm = { _, _, _ -> }
                )
            }
        }

        composeRule.onNodeWithTag("res_button").assertIsNotEnabled()
    }

    @Test
    fun occupiedTime_disablesReservation() {
        val existingReservation = ReservationEntity(
            spotIndex = 5,
            spotType = SpotType.COMBUSTION.name,
            dateMillis = selectedDate,
            startTime = "09:00",
            endTime = "10:00",
            vehiclePlate = "0000OLD"
        )

        composeRule.setContent {
            MaterialTheme {
                ReservationSheet(
                    selectedSpotIndex = 5,
                    spotType = SpotType.COMBUSTION,
                    selectedDateMillis = selectedDate,
                    userVehicles = listOf(combustionVehicle),
                    futureReservations = listOf(existingReservation),
                    onDismiss = {},
                    onConfirm = { _, _, _ -> }
                )
            }
        }

        composeRule.onNodeWithTag("vehicle_1234ABC").performClick()
        composeRule.onNodeWithTag("res_button").assertIsNotEnabled()
    }

    @Test
    fun noCompatibleVehicles_showsWarningAndDisablesReservation() {
        composeRule.setContent {
            MaterialTheme {
                ReservationSheet(
                    selectedSpotIndex = 5,
                    spotType = SpotType.DISABLED,
                    selectedDateMillis = selectedDate,
                    userVehicles = listOf(combustionVehicle),
                    futureReservations = emptyList(),
                    onDismiss = {},
                    onConfirm = { _, _, _ -> }
                )
            }
        }

        composeRule
            .onNodeWithText("No tienes vehículos compatibles", substring = true)
            .fetchSemanticsNode()
        composeRule.onNodeWithTag("res_button").assertIsNotEnabled()
    }

    @Test
    fun occupiedReservation_showsOccupiedWarning() {
        val existingReservation = ReservationEntity(
            spotIndex = 5,
            spotType = SpotType.COMBUSTION.name,
            dateMillis = selectedDate,
            startTime = "09:00",
            endTime = "10:00",
            vehiclePlate = "0000OLD"
        )

        composeRule.setContent {
            MaterialTheme {
                ReservationSheet(
                    selectedSpotIndex = 5,
                    spotType = SpotType.COMBUSTION,
                    selectedDateMillis = selectedDate,
                    userVehicles = listOf(combustionVehicle),
                    futureReservations = listOf(existingReservation),
                    onDismiss = {},
                    onConfirm = { _, _, _ -> }
                )
            }
        }

        composeRule.onNodeWithTag("vehicle_1234ABC").performClick()
        composeRule
            .onNodeWithText("Esta plaza ya está ocupada en ese horario.", substring = true)
            .fetchSemanticsNode()
        composeRule.onNodeWithTag("res_button").assertIsNotEnabled()
    }
}