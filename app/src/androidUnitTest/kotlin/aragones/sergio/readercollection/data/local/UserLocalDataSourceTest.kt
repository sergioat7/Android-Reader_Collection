@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.data.local

import aragones.sergio.readercollection.data.local.model.AuthData
import aragones.sergio.readercollection.data.local.model.UserData
import aragones.sergio.readercollection.data.remote.model.ALL_FORMATS
import aragones.sergio.readercollection.data.remote.model.ALL_GENRES
import aragones.sergio.readercollection.data.remote.model.ALL_STATES
import aragones.sergio.readercollection.data.remote.model.FORMATS
import aragones.sergio.readercollection.data.remote.model.FormatResponse
import aragones.sergio.readercollection.data.remote.model.GENRES
import aragones.sergio.readercollection.data.remote.model.GenreResponse
import aragones.sergio.readercollection.data.remote.model.STATES
import aragones.sergio.readercollection.data.remote.model.StateResponse
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class UserLocalDataSourceTest {

    private val appInfoProvider: AppInfoProvider = mockk()
    private val preferences: SharedPreferencesHandler = mockk()
    private val dataSource = UserLocalDataSource(appInfoProvider, preferences)

    @Test
    fun `GIVEN username in preferences WHEN get username THEN return value from preferences`() {
        val expectedUsername = "testUser"
        every { preferences.userData } returns UserData(expectedUsername, "testPassword")

        val result = dataSource.username

        assertEquals(expectedUsername, result)
        verify(exactly = 1) { preferences.userData }
        confirmVerified(preferences)
    }

    @Test
    fun `GIVEN userData in preferences WHEN get userData THEN return value from preferences`() {
        val expectedUserData = UserData("testUser", "testPassword")
        every { preferences.userData } returns expectedUserData

        val result = dataSource.userData

        assertEquals(expectedUserData, result)
        verify(exactly = 1) { preferences.userData }
        confirmVerified(preferences)
    }

    @Test
    fun `GIVEN userId in preferences WHEN get userId THEN return value from preferences`() {
        val expectedUserId = "testUserId"
        every { preferences.credentials } returns AuthData(uuid = expectedUserId)

        val result = dataSource.userId

        assertEquals(expectedUserId, result)
        verify(exactly = 1) { preferences.credentials }
        confirmVerified(preferences)
    }

    @Test
    fun `GIVEN isProfilePublic in preferences WHEN get isProfilePublic THEN return value from preferences`() {
        every { preferences.isProfilePublic } returns true

        val result = dataSource.isProfilePublic

        assertEquals(true, result)
        verify(exactly = 1) { preferences.isProfilePublic }
        confirmVerified(preferences)
    }

    @Test
    fun `GIVEN isAutomaticSyncEnabled in preferences WHEN get isAutomaticSyncEnabled THEN return value from preferences`() {
        every { preferences.isAutomaticSyncEnabled } returns true

        val result = dataSource.isAutomaticSyncEnabled

        assertEquals(true, result)
        verify(exactly = 1) { preferences.isAutomaticSyncEnabled }
        confirmVerified(preferences)
    }

    @Test
    fun `GIVEN language in preferences WHEN get language THEN return value from preferences`() {
        val expectedLanguage = "en"
        every { preferences.language } returns expectedLanguage

        val result = dataSource.language

        assertEquals(expectedLanguage, result)
        verify(exactly = 1) { preferences.language }
        confirmVerified(preferences)
    }

    @Test
    fun `GIVEN value WHEN set language THEN save value in preferences`() {
        val newLanguage = "es"
        every { preferences.language = newLanguage } just Runs

        dataSource.language = newLanguage

        verify(exactly = 1) { preferences.language = newLanguage }
        confirmVerified(preferences)
    }

    @Test
    fun `GIVEN isLoggedIn true in preferences WHEN check if is logged in THEN return true`() {
        every { preferences.isLoggedIn } returns true

        val result = dataSource.isLoggedIn

        assertEquals(true, result)
        verify(exactly = 1) { preferences.isLoggedIn }
        confirmVerified(preferences)
    }

    @Test
    fun `GIVEN isLoggedIn false in preferences WHEN check if is logged in THEN return false`() {
        every { preferences.isLoggedIn } returns false

        val result = dataSource.isLoggedIn

        assertEquals(false, result)
        verify(exactly = 1) { preferences.isLoggedIn }
        confirmVerified(preferences)
    }

    @Test
    fun `GIVEN sortParam in preferences WHEN get sortParam THEN return value from preferences`() {
        val expectedSortParam = "title"
        every { preferences.sortParam } returns expectedSortParam

        val result = dataSource.sortParam

        assertEquals(expectedSortParam, result)
        verify(exactly = 1) { preferences.sortParam }
        confirmVerified(preferences)
    }

    @Test
    fun `GIVEN no sortParam in preferences WHEN get sortParam THEN return null`() {
        val expectedSortParam = null
        every { preferences.sortParam } returns expectedSortParam

        val result = dataSource.sortParam

        assertEquals(expectedSortParam, result)
        verify(exactly = 1) { preferences.sortParam }
        confirmVerified(preferences)
    }

    @Test
    fun `GIVEN isSortDescending in preferences WHEN get isSortDescending THEN return value from preferences`() {
        val expectedIsSortDescending = true
        every { preferences.isSortDescending } returns expectedIsSortDescending

        val result = dataSource.isSortDescending

        assertEquals(expectedIsSortDescending, result)
        verify(exactly = 1) { preferences.isSortDescending }
        confirmVerified(preferences)
    }

    @Test
    fun `GIVEN themeMode in preferences WHEN get themeMode THEN return value from preferences`() {
        val expectedThemeMode = 1
        every { preferences.themeMode } returns expectedThemeMode

        val result = dataSource.themeMode

        assertEquals(expectedThemeMode, result)
        verify(exactly = 1) { preferences.themeMode }
        confirmVerified(preferences)
    }

    @Test
    fun `WHEN logout is called THEN preferences are invoked to remove data`() {
        every { preferences.removeUserPreferences() } just Runs
        every { preferences.removePassword() } just Runs
        every { preferences.removeCredentials() } just Runs

        dataSource.logout()

        verify(exactly = 1) { preferences.removeUserPreferences() }
        verify(exactly = 1) { preferences.removePassword() }
        verify(exactly = 1) { preferences.removeCredentials() }
        confirmVerified(preferences)
    }

    @Test
    fun `WHEN storeLoginData is called THEN preferences are invoked to save data`() {
        val userData = UserData("testUser", "testPassword")
        val authData = AuthData(uuid = "testUserId")
        every { preferences.userData = userData } just Runs
        every { preferences.credentials = authData } just Runs

        dataSource.storeLoginData(userData, authData)

        verify(exactly = 1) { preferences.userData = userData }
        verify(exactly = 1) { preferences.credentials = authData }
        confirmVerified(preferences)
    }

    @Test
    fun `WHEN storeCredentials is called THEN preferences are invoked to save data`() {
        val authData = AuthData(uuid = "testUserId")
        every { preferences.credentials = authData } just Runs

        dataSource.storeCredentials(authData)

        verify(exactly = 1) { preferences.credentials = authData }
        confirmVerified(preferences)
    }

    @Test
    fun `WHEN removeCredentials is called THEN preferences are invoked to remove data`() {
        every { preferences.removeCredentials() } just Runs

        dataSource.removeCredentials()

        verify(exactly = 1) { preferences.removeCredentials() }
        confirmVerified(preferences)
    }

    @Test
    fun `WHEN storePassword is called THEN preferences are invoked to save data`() {
        val newPassword = "newPassword"
        every { preferences.storePassword(newPassword) } just Runs

        dataSource.storePassword(newPassword)

        verify(exactly = 1) { preferences.storePassword(newPassword) }
        confirmVerified(preferences)
    }

    @Test
    fun `WHEN removeUserData is called THEN preferences are invoked to remove data`() {
        every { preferences.removeUserData() } just Runs

        dataSource.removeUserData()

        verify(exactly = 1) { preferences.removeUserData() }
        confirmVerified(preferences)
    }

    @Test
    fun `WHEN storePublicProfile is called THEN preferences are invoked to save data`() {
        val newVale = true
        every { preferences.isProfilePublic = newVale } just Runs

        dataSource.storePublicProfile(newVale)

        verify(exactly = 1) { preferences.isProfilePublic = newVale }
        confirmVerified(preferences)
    }

    @Test
    fun `WHEN storeAutomaticSync is called THEN preferences are invoked to save data`() {
        val newVale = true
        every { preferences.isAutomaticSyncEnabled = newVale } just Runs

        dataSource.storeAutomaticSync(newVale)

        verify(exactly = 1) { preferences.isAutomaticSyncEnabled = newVale }
        confirmVerified(preferences)
    }

    @Test
    fun `WHEN storeLanguage is called THEN preferences are invoked to save data and formats and genres and states are stored for that language`() {
        val newVale = "es"
        val formats = listOf(
            FormatResponse("1", ""),
            FormatResponse("2", ""),
        )
        val genres = listOf(
            GenreResponse("1", ""),
            GenreResponse("2", ""),
        )
        val states = listOf(
            StateResponse("1", ""),
            StateResponse("2", ""),
        )
        ALL_FORMATS = mapOf(
            "en" to formats.map { it.copy(name = "English") },
            "es" to formats.map { it.copy(name = "Español") },
        )
        ALL_GENRES = mapOf(
            "en" to genres.map { it.copy(name = "English") },
            "es" to genres.map { it.copy(name = "Español") },
        )
        ALL_STATES = mapOf(
            "en" to states.map { it.copy(name = "English") },
            "es" to states.map { it.copy(name = "Español") },
        )
        every { preferences.language = newVale } just Runs
        every { appInfoProvider.changeLocale(any()) } just Runs

        dataSource.storeLanguage(newVale)

        assertEquals(formats.map { it.copy(name = "Español") }, FORMATS)
        assertEquals(genres.map { it.copy(name = "Español") }, GENRES)
        assertEquals(states.map { it.copy(name = "Español") }, STATES)
        verify(exactly = 1) { preferences.language = newVale }
        verify(exactly = 1) { appInfoProvider.changeLocale(newVale) }
        confirmVerified(preferences)
    }

    @Test
    fun `WHEN storeSortParam is called THEN preferences are invoked to save data`() {
        val sortParam = "author"
        every { preferences.sortParam = sortParam } just Runs

        dataSource.storeSortParam(sortParam)

        verify(exactly = 1) { preferences.sortParam = sortParam }
        confirmVerified(preferences)
    }

    @Test
    fun `WHEN storeIsSortDescending is called THEN preferences are invoked to save data`() {
        val newVale = true
        every { preferences.isSortDescending = newVale } just Runs

        dataSource.storeIsSortDescending(newVale)

        verify(exactly = 1) { preferences.isSortDescending = newVale }
        confirmVerified(preferences)
    }

    @Test
    fun `WHEN storeThemeMode is called THEN preferences are invoked to save data`() {
        val themeMode = 2
        every { preferences.themeMode = themeMode } just Runs

        dataSource.storeThemeMode(themeMode)

        verify(exactly = 1) { preferences.themeMode = themeMode }
        confirmVerified(preferences)
    }

    @Test
    fun `WHEN applyTheme is called THEN appInfoProvider is invoked to apply theme`() {
        val themeMode = 2
        every { preferences.themeMode } returns themeMode
        every { appInfoProvider.applyTheme(any()) } just Runs

        dataSource.applyTheme()

        verify(exactly = 1) { appInfoProvider.applyTheme(themeMode) }
        confirmVerified(appInfoProvider)
    }

    @Test
    fun `GIVEN version name is correct WHEN getCalculatedCurrentVersion is called THEN returns version code`() {
        every { appInfoProvider.getVersion() } returns "1.2.3"

        val result = dataSource.getCalculatedCurrentVersion()

        assertEquals(102030, result)
    }

    @Test
    fun `GIVEN version name is malformed WHEN getCalculatedCurrentVersion is called THEN returns 0`() {
        every { appInfoProvider.getVersion() } returns "1.2"

        val result = dataSource.getCalculatedCurrentVersion()

        assertEquals(Int.MAX_VALUE, result)
    }

    @Test
    fun `GIVEN getPackageInfo throws exception WHEN getCalculatedCurrentVersion is called THEN returns 0`() {
        every { appInfoProvider.getVersion() } returns null

        val result = dataSource.getCalculatedCurrentVersion()

        assertEquals(Int.MAX_VALUE, result)
    }

    @Test
    fun `WHEN getCurrentVersion is called THEN returns version`() {
        every { appInfoProvider.getVersion() } returns "1.2.3"

        val result = dataSource.getCurrentVersion()

        assertEquals("1.2.3", result)
    }
}