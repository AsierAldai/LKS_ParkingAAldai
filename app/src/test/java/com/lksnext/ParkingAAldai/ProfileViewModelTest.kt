package com.lksnext.ParkingAAldai

import com.lksnext.ParkingAAldai.data.models.UserEntity
import com.lksnext.ParkingAAldai.data.models.VehicleEntity
import com.lksnext.ParkingAAldai.ui.screens.SpotType
import com.lksnext.ParkingAAldai.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadUserDataWithoutSession_resetsState() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = ProfileViewModel(
            repository = FakeParkingRepository(),
            authManager = FakeAuthDataSource(currentEmail = null)
        )

        viewModel.loadUserData()
        testScheduler.advanceUntilIdle()

        assertEquals("Usuario", viewModel.name.value)
        assertEquals("", viewModel.email.value)
        assertEquals("", viewModel.username.value)
    }

    @Test
    fun loadUserDataWithExistingUser_updatesState() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeParkingRepository()
        repo.users["user@lks.com"] = UserEntity(
            email = "user@lks.com",
            name = "Asier",
            username = "asier",
            phone = "666777888"
        )

        val viewModel = ProfileViewModel(repo, FakeAuthDataSource("user@lks.com"))
        testScheduler.advanceUntilIdle()

        assertEquals("user@lks.com", viewModel.email.value)
        assertEquals("Asier", viewModel.name.value)
        assertEquals("asier", viewModel.username.value)
        assertEquals("666777888", viewModel.phone.value)
    }

    @Test
    fun addVehicle_insertsVehicleForCurrentUser() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeParkingRepository()
        val viewModel = ProfileViewModel(repo, FakeAuthDataSource("user@lks.com"))

        viewModel.addVehicle(
            plate = "1234ABC",
            brand = "Toyota",
            color = "Rojo",
            type = SpotType.COMBUSTION
        )
        testScheduler.advanceUntilIdle()

        assertEquals(1, repo.vehicles.size)
        assertEquals("user@lks.com", repo.vehicles.first().ownerEmail)
        assertEquals("1234ABC", repo.vehicles.first().plate)
    }

    @Test
    fun deleteVehicle_deletesVehicle() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeParkingRepository()
        val vehicle = VehicleEntity(
            id = 1,
            plate = "1234ABC"
        )
        val viewModel = ProfileViewModel(repo, FakeAuthDataSource())

        viewModel.deleteVehicle(vehicle)
        testScheduler.advanceUntilIdle()

        assertEquals(vehicle, repo.deletedVehicle)
    }

    @Test
    fun updateProfileSuccess_updatesUserAndVehicleOwnerEmail() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeParkingRepository()
        val auth = FakeAuthDataSource("old@lks.com")
        val viewModel = ProfileViewModel(repo, auth)

        viewModel.updateProfile(
            newName = "Nuevo",
            newUsername = "nuevo",
            newEmail = "new@lks.com",
            newPhone = "999888777",
            currentPassword = "123456"
        )
        testScheduler.advanceUntilIdle()

        assertEquals("", viewModel.errorMessage.value)
        assertEquals("old@lks.com" to "new@lks.com", repo.updatedVehiclesOwnerEmail)
        assertEquals("old@lks.com", repo.deletedUserEmail)
        assertEquals("Nuevo", repo.users["new@lks.com"]?.name)
    }

    @Test
    fun updateProfileFailure_setsError() {
        val repo = FakeParkingRepository()
        val auth = FakeAuthDataSource("user@lks.com").apply {
            updateSuccess = false
            error = "Contraseña incorrecta"
        }
        val viewModel = ProfileViewModel(repo, auth)

        viewModel.updateProfile(
            newName = "Nuevo",
            newUsername = "nuevo",
            newEmail = "new@lks.com",
            newPhone = "999888777",
            currentPassword = "bad"
        )

        assertEquals("Contraseña incorrecta", viewModel.errorMessage.value)
    }

    @Test
    fun logout_clearsStateAndCallsAuth() {
        val auth = FakeAuthDataSource("user@lks.com")
        val viewModel = ProfileViewModel(FakeParkingRepository(), auth)

        viewModel.name.value = "Asier"
        viewModel.email.value = "user@lks.com"

        viewModel.logout()

        assertTrue(auth.logoutCalled)
        assertEquals("Usuario", viewModel.name.value)
        assertEquals("", viewModel.email.value)
    }
}