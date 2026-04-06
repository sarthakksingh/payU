package com.sarthak.payu.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "payu_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_TOTAL_BALANCE = doublePreferencesKey("total_balance")
    }

    val userName: Flow<String> = context.dataStore.data.map { it[KEY_USER_NAME] ?: "" }
    val userEmail: Flow<String> = context.dataStore.data.map { it[KEY_USER_EMAIL] ?: "" }
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_DARK_MODE] ?: true }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_LOGGED_IN] ?: false }
    val totalBalance: Flow<Double> = context.dataStore.data.map { it[KEY_TOTAL_BALANCE] ?: 0.0 }

    suspend fun saveUser(name: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun toggleDarkMode(isDark: Boolean) {
        context.dataStore.edit { it[KEY_IS_DARK_MODE] = isDark }
    }

    suspend fun updateBalance(balance: Double) {
        context.dataStore.edit { it[KEY_TOTAL_BALANCE] = balance }
    }

    suspend fun logout() {
        context.dataStore.edit { it[KEY_IS_LOGGED_IN] = false }
    }
}