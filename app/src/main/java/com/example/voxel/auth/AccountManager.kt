package com.example.voxel.auth

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

/**
 * Manages player authentication, account registration, profiles, stats, and centralized persistence.
 */
class AccountManager(private val context: Context) {

    private val dbFile = File(context.filesDir, "voxel_accounts_db.json")
    private val sessionFile = File(context.filesDir, "active_session.json")

    private val accounts = mutableMapOf<String, PlayerAccount>()
    var currentAccount: PlayerAccount? = null
        private set

    init {
        loadDatabase()
        loadActiveSession()
        if (accounts.isEmpty()) {
            // Seed a default miner account
            register("Player1", "miner@voxelcraft.io", "123456")
        }
    }

    fun register(username: String, email: String, pass: String): Result<PlayerAccount> {
        val trimmedUser = username.trim()
        val trimmedEmail = email.trim().lowercase()

        if (trimmedUser.length < 3) return Result.failure(Exception("Username must be at least 3 characters"))
        if (!trimmedEmail.contains("@")) return Result.failure(Exception("Invalid email address"))
        if (pass.length < 4) return Result.failure(Exception("Password must be at least 4 characters"))

        if (accounts.values.any { it.username.equals(trimmedUser, ignoreCase = true) }) {
            return Result.failure(Exception("Username is already taken"))
        }
        if (accounts.values.any { it.email.equals(trimmedEmail, ignoreCase = true) }) {
            return Result.failure(Exception("Email is already registered"))
        }

        val id = "usr_" + System.currentTimeMillis()
        val passHash = hashPassword(pass)
        val acc = PlayerAccount(
            id = id,
            username = trimmedUser,
            email = trimmedEmail,
            passwordHash = passHash,
            skin = PlayerSkin.STEVE
        )

        accounts[id] = acc
        currentAccount = acc
        saveDatabase()
        saveActiveSession(id)
        return Result.success(acc)
    }

    fun login(userOrEmail: String, pass: String): Result<PlayerAccount> {
        val query = userOrEmail.trim()
        val hash = hashPassword(pass)

        val acc = accounts.values.find {
            (it.username.equals(query, ignoreCase = true) || it.email.equals(query, ignoreCase = true)) &&
                    it.passwordHash == hash
        }

        return if (acc != null) {
            acc.lastLoginAt = System.currentTimeMillis()
            currentAccount = acc
            saveDatabase()
            saveActiveSession(acc.id)
            Result.success(acc)
        } else {
            Result.failure(Exception("Invalid username/email or password"))
        }
    }

    fun logout() {
        currentAccount = null
        if (sessionFile.exists()) sessionFile.delete()
    }

    fun updateSkin(skin: PlayerSkin) {
        val acc = currentAccount ?: return
        acc.skin = skin
        saveDatabase()
    }

    fun recordBlockMined() {
        val acc = currentAccount ?: return
        acc.blocksMined++
        checkLevelUp(acc)
        saveDatabase()
    }

    fun recordBlockPlaced() {
        val acc = currentAccount ?: return
        acc.blocksPlaced++
        checkLevelUp(acc)
        saveDatabase()
    }

    fun recordMobDefeated() {
        val acc = currentAccount ?: return
        acc.mobsDefeated++
        checkLevelUp(acc)
        saveDatabase()
    }

    private fun checkLevelUp(acc: PlayerAccount) {
        val totalActivity = acc.blocksMined + acc.blocksPlaced + acc.mobsDefeated * 3
        acc.level = (1 + totalActivity / 40).coerceAtLeast(1)
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun saveDatabase() {
        try {
            val root = JSONObject()
            val arr = JSONArray()
            for (acc in accounts.values) {
                val obj = JSONObject().apply {
                    put("id", acc.id)
                    put("username", acc.username)
                    put("email", acc.email)
                    put("passwordHash", acc.passwordHash)
                    put("skin", acc.skin.name)
                    put("level", acc.level)
                    put("blocksMined", acc.blocksMined)
                    put("blocksPlaced", acc.blocksPlaced)
                    put("mobsDefeated", acc.mobsDefeated)
                    put("diamondsCollected", acc.diamondsCollected)
                    put("createdAt", acc.createdAt)
                    put("lastLoginAt", acc.lastLoginAt)
                }
                arr.put(obj)
            }
            root.put("accounts", arr)
            dbFile.writeText(root.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadDatabase() {
        try {
            if (!dbFile.exists()) return
            val root = JSONObject(dbFile.readText())
            val arr = root.optJSONArray("accounts") ?: return
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val skinName = obj.optString("skin", "STEVE")
                val skin = try { PlayerSkin.valueOf(skinName) } catch (_: Exception) { PlayerSkin.STEVE }
                val acc = PlayerAccount(
                    id = obj.getString("id"),
                    username = obj.getString("username"),
                    email = obj.getString("email"),
                    passwordHash = obj.getString("passwordHash"),
                    skin = skin,
                    level = obj.optInt("level", 1),
                    blocksMined = obj.optInt("blocksMined", 0),
                    blocksPlaced = obj.optInt("blocksPlaced", 0),
                    mobsDefeated = obj.optInt("mobsDefeated", 0),
                    diamondsCollected = obj.optInt("diamondsCollected", 0),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    lastLoginAt = obj.optLong("lastLoginAt", System.currentTimeMillis())
                )
                accounts[acc.id] = acc
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveActiveSession(accountId: String) {
        try {
            val obj = JSONObject().apply { put("activeAccountId", accountId) }
            sessionFile.writeText(obj.toString())
        } catch (_: Exception) {}
    }

    private fun loadActiveSession() {
        try {
            if (!sessionFile.exists()) return
            val obj = JSONObject(sessionFile.readText())
            val id = obj.optString("activeAccountId")
            currentAccount = accounts[id]
        } catch (_: Exception) {}
    }
}
