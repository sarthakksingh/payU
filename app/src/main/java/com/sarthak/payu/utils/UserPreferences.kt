package com.sarthak.payu.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sarthak.payu.data.model.PaymentMethod
import com.sarthak.payu.data.model.PaymentMethodType
import com.sarthak.payu.data.model.SavedProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
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
        val KEY_PAYMENT_METHODS = stringPreferencesKey("payment_methods") // JSON array
        val KEY_REGISTERED_PROFILES = stringPreferencesKey("registered_profiles") // JSON array
    }

    val userName: Flow<String> = context.dataStore.data.map { it[KEY_USER_NAME] ?: "" }
    val userEmail: Flow<String> = context.dataStore.data.map { it[KEY_USER_EMAIL] ?: "" }
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_DARK_MODE] ?: true }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_LOGGED_IN] ?: false }
    val totalBalance: Flow<Double> = context.dataStore.data.map { it[KEY_TOTAL_BALANCE] ?: 0.0 }
    val registeredProfiles: Flow<List<SavedProfile>> = context.dataStore.data.map { prefs ->
        parseRegisteredProfiles(prefs[KEY_REGISTERED_PROFILES] ?: "[]")
    }

    val paymentMethods: Flow<List<PaymentMethod>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_PAYMENT_METHODS] ?: return@map emptyList()
        parsePaymentMethods(json)
    }

    val primaryPaymentMethod: Flow<PaymentMethod?> = paymentMethods.map { list ->
        list.firstOrNull { it.isPrimary } ?: list.firstOrNull()
    }

    suspend fun saveUser(name: String, email: String) {
        context.dataStore.edit { prefs ->
            val currentProfiles = parseRegisteredProfiles(prefs[KEY_REGISTERED_PROFILES] ?: "[]").toMutableList()
            val existingIndex = currentProfiles.indexOfFirst { it.email.equals(email, ignoreCase = true) }
            val normalized = SavedProfile(name = name.trim().ifBlank { email.substringBefore("@") }, email = email.trim())
            if (existingIndex >= 0) {
                currentProfiles[existingIndex] = normalized
            } else {
                currentProfiles.add(normalized)
            }
            prefs[KEY_REGISTERED_PROFILES] = serializeRegisteredProfiles(currentProfiles)
            prefs[KEY_USER_NAME] = normalized.name
            prefs[KEY_USER_EMAIL] = normalized.email
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

    // ── Payment Method CRUD ────────────────────────────────────

    suspend fun addPaymentMethod(method: PaymentMethod) {
        context.dataStore.edit { prefs ->
            val current = parsePaymentMethods(prefs[KEY_PAYMENT_METHODS] ?: "[]").toMutableList()
            if (current.size >= 5) return@edit // max 5
            // If this is first one, make it primary
            val toAdd = if (current.isEmpty()) method.copy(isPrimary = true) else method
            current.add(toAdd)
            prefs[KEY_PAYMENT_METHODS] = serializePaymentMethods(current)
        }
    }

    suspend fun removePaymentMethod(id: String) {
        context.dataStore.edit { prefs ->
            val current = parsePaymentMethods(prefs[KEY_PAYMENT_METHODS] ?: "[]").toMutableList()
            val removed = current.find { it.id == id }
            current.removeAll { it.id == id }
            // If removed was primary and list not empty, set first as primary
            if (removed?.isPrimary == true && current.isNotEmpty()) {
                current[0] = current[0].copy(isPrimary = true)
            }
            prefs[KEY_PAYMENT_METHODS] = serializePaymentMethods(current)
        }
    }

    suspend fun setPrimaryPaymentMethod(id: String) {
        context.dataStore.edit { prefs ->
            val current = parsePaymentMethods(prefs[KEY_PAYMENT_METHODS] ?: "[]")
            val updated = current.map { it.copy(isPrimary = it.id == id) }
            prefs[KEY_PAYMENT_METHODS] = serializePaymentMethods(updated)
        }
    }

    suspend fun updatePaymentMethodBalance(id: String, balance: Double) {
        context.dataStore.edit { prefs ->
            val current = parsePaymentMethods(prefs[KEY_PAYMENT_METHODS] ?: "[]")
            val updated = current.map { method ->
                if (method.id == id) method.copy(balance = balance) else method
            }
            prefs[KEY_PAYMENT_METHODS] = serializePaymentMethods(updated)
        }
    }

    suspend fun updatePaymentMethod(method: PaymentMethod) {
        context.dataStore.edit { prefs ->
            val current = parsePaymentMethods(prefs[KEY_PAYMENT_METHODS] ?: "[]")
            val updated = current.map {
                if (it.id == method.id) method else it
            }
            prefs[KEY_PAYMENT_METHODS] = serializePaymentMethods(updated)
        }
    }

    suspend fun adjustPaymentMethodBalance(id: String, delta: Double) {
        context.dataStore.edit { prefs ->
            val current = parsePaymentMethods(prefs[KEY_PAYMENT_METHODS] ?: "[]")
            val updated = current.map { method ->
                if (method.id == id) method.copy(balance = method.balance + delta) else method
            }
            prefs[KEY_PAYMENT_METHODS] = serializePaymentMethods(updated)
        }
    }

    suspend fun getRegisteredProfile(email: String): SavedProfile? {
        val prefs = context.dataStore.data.first()
        return parseRegisteredProfiles(prefs[KEY_REGISTERED_PROFILES] ?: "[]")
            .firstOrNull { it.email.equals(email, ignoreCase = true) }
    }

    suspend fun getKnownProfile(email: String): SavedProfile? {
        val current = context.dataStore.data.first()
        val currentEmail = current[KEY_USER_EMAIL]?.trim().orEmpty()
        val currentName = current[KEY_USER_NAME]?.trim().orEmpty()
        if (currentEmail.equals(email.trim(), ignoreCase = true) && currentName.isNotBlank()) {
            return SavedProfile(name = currentName, email = currentEmail)
        }
        return parseRegisteredProfiles(current[KEY_REGISTERED_PROFILES] ?: "[]")
            .firstOrNull { it.email.equals(email, ignoreCase = true) }
    }

    // ── JSON helpers ───────────────────────────────────────────

    private fun serializePaymentMethods(list: List<PaymentMethod>): String {
        val arr = JSONArray()
        list.forEach { m ->
            arr.put(JSONObject().apply {
                put("id", m.id)
                put("bankName", m.bankName)
                put("lastDigits", m.lastDigits)
                put("balance", m.balance)
                put("isPrimary", m.isPrimary)
                put("type", m.type.name)
                put("accountNumber", m.accountNumber)
                put("cardNumber", m.cardNumber)
                put("cvv", m.cvv)
                put("expiryDate", m.expiryDate)
            })
        }
        return arr.toString()
    }

    private fun parsePaymentMethods(json: String): List<PaymentMethod> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                PaymentMethod(
                    id = obj.getString("id"),
                    bankName = obj.getString("bankName"),
                    lastDigits = obj.getString("lastDigits"),
                    balance = obj.optDouble("balance", 0.0),
                    isPrimary = obj.getBoolean("isPrimary"),
                    type = PaymentMethodType.valueOf(obj.optString("type", "BANK")),
                    accountNumber = obj.optString("accountNumber", "xxxx xxxx xxxx xxxx"),
                    cardNumber = obj.optString("cardNumber", "xxxx xxxx xxxx xxxx"),
                    cvv = obj.optString("cvv", "xxx"),
                    expiryDate = obj.optString("expiryDate", "xx/xx")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeRegisteredProfiles(list: List<SavedProfile>): String {
        val arr = JSONArray()
        list.forEach { profile ->
            arr.put(JSONObject().apply {
                put("name", profile.name)
                put("email", profile.email)
            })
        }
        return arr.toString()
    }

    private fun parseRegisteredProfiles(json: String): List<SavedProfile> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                SavedProfile(
                    name = obj.optString("name", ""),
                    email = obj.optString("email", "")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun generateId(): String = UUID.randomUUID().toString()
}
