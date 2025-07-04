package com.example.pawpal

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pawpal.mainMenu.pets.ReminderData

data class ChatMessage(
    val id: Long = 0,
    val message: String,
    val isUser: Boolean,
    val timestamp: String
)

class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "pawpal.db", null, 4) { // Increment version to 4

    override fun onCreate(db: SQLiteDatabase) {
        // Tabel owner
        db.execSQL("""
            CREATE TABLE owner (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                phone TEXT UNIQUE,
                email TEXT UNIQUE,
                username TEXT UNIQUE,
                password TEXT
            )
        """)

        // Tabel pet
        db.execSQL("""
            CREATE TABLE pet (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                type TEXT,
                breed TEXT,
                sex TEXT,
                dob TEXT,
                photo_path TEXT,
                owner_id INTEGER,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(owner_id) REFERENCES owner(id)
            )
        """)

        // Tabel chat
        db.execSQL("""
            CREATE TABLE chat (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                message TEXT,
                isUser INTEGER,
                timestamp TEXT
            )
        """)

        // Tabel reminders - Updated with all required columns
        db.execSQL("""
            CREATE TABLE reminders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pet_id INTEGER,
                title TEXT,
                description TEXT,
                reminder_type TEXT,
                frequency TEXT,
                date TEXT,
                time TEXT,
                feed_per_day INTEGER DEFAULT 1,
                is_active INTEGER DEFAULT 1,
                is_completed INTEGER DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(pet_id) REFERENCES pet(id)
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add reminders table for version 2
            db.execSQL("""
                CREATE TABLE reminders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pet_id INTEGER,
                    title TEXT,
                    description TEXT,
                    date TEXT,
                    time TEXT,
                    is_completed INTEGER DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(pet_id) REFERENCES pet(id)
                )
            """)
        }
        if (oldVersion < 3) {
            // Add photo_path column for version 3
            db.execSQL("ALTER TABLE pet ADD COLUMN photo_path TEXT")
        }
        if (oldVersion < 4) {
            // Add missing columns for version 4
            try {
                db.execSQL("ALTER TABLE reminders ADD COLUMN reminder_type TEXT")
                db.execSQL("ALTER TABLE reminders ADD COLUMN frequency TEXT")
                db.execSQL("ALTER TABLE reminders ADD COLUMN feed_per_day INTEGER DEFAULT 1")
                db.execSQL("ALTER TABLE reminders ADD COLUMN is_active INTEGER DEFAULT 1")
            } catch (e: Exception) {
                // Handle case where columns might already exist
                e.printStackTrace()
            }
        }
    }

    // ============================
    // FUNGSI UNTUK TABEL OWNER & PET
    // ============================

    fun insertOwnerWithPet(
        name: String,
        phone: String,
        email: String,
        username: String,
        password: String,
        petName: String,
        petType: String,
        breed: String,
        sex: String,
        dob: String,
        photoPath: String? = null
    ): Long {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val ownerValues = ContentValues().apply {
                put("name", name)
                put("phone", phone)
                put("email", email)
                put("username", username)
                put("password", password)
            }
            val ownerId = db.insert("owner", null, ownerValues)
            if (ownerId == -1L) return -1L

            val petValues = ContentValues().apply {
                put("name", petName)
                put("type", petType)
                put("breed", breed)
                put("sex", sex)
                put("dob", dob)
                put("photo_path", photoPath)
                put("owner_id", ownerId)
            }
            val petResult = db.insert("pet", null, petValues)
            if (petResult == -1L) return -1L

            db.setTransactionSuccessful()
            ownerId // Return owner ID instead of boolean
        } finally {
            db.endTransaction()
        }
    }

    fun checkLogin(identifier: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT * FROM owner 
            WHERE password = ? AND 
            (username = ? OR email = ? OR phone = ?)
        """, arrayOf(password, identifier, identifier, identifier))

        val success = cursor.count > 0
        cursor.close()
        return success
    }

    fun getOwnerId(identifier: String, password: String): Int? {
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT id FROM owner 
            WHERE password = ? AND 
            (username = ? OR email = ? OR phone = ?)
        """, arrayOf(password, identifier, identifier, identifier))

        val ownerId = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        } else null

        cursor.close()
        return ownerId
    }

    fun getOwnerName(ownerId: Int): String? {
        val db = readableDatabase
        var ownerName: String? = null

        val cursor = db.query(
            "owner",  // Nama tabel sesuai dengan yang didefinisikan
            arrayOf("name"),  // Kolom name, bukan owner_name
            "id = ?",  // Kolom id, bukan owner_id
            arrayOf(ownerId.toString()),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            ownerName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
        }

        cursor.close()
        return ownerName
    }

    fun getLastPetNameForUser(ownerId: Int): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT name FROM pet WHERE owner_id = ? ORDER BY created_at DESC LIMIT 1",
            arrayOf(ownerId.toString())
        )

        val petName = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow("name"))
        } else null

        cursor.close()
        return petName
    }

    // ============================
    // FUNGSI UNTUK TABEL CHAT
    // ============================

    fun insertMessage(message: ChatMessage) {
        val values = ContentValues().apply {
            put("message", message.message)
            put("isUser", if (message.isUser) 1 else 0)
            put("timestamp", message.timestamp)
        }
        writableDatabase.insert("chat", null, values)
    }

    fun getAllMessages(): List<ChatMessage> {
        val list = mutableListOf<ChatMessage>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM chat", null)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(0)
            val message = cursor.getString(1)
            val isUser = cursor.getInt(2) == 1
            val timestamp = cursor.getString(3)
            list.add(ChatMessage(id, message, isUser, timestamp))
        }
        cursor.close()
        return list
    }

    fun deleteAllChats() {
        writableDatabase.execSQL("DELETE FROM chat")
    }

    // ============================
    // FUNGSI UNTUK TABEL REMINDERS
    // ============================

    fun getReminderCount(petId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) as count FROM reminders WHERE pet_id = ? AND is_active = 1",
            arrayOf(petId.toString())
        )

        val count = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow("count"))
        } else 0

        cursor.close()
        return count
    }

    fun getAllReminders(petId: Int): List<ReminderData> {
        val remindersList = mutableListOf<ReminderData>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """SELECT id, pet_id, title, description, reminder_type, frequency, date, time, 
               feed_per_day, is_active, is_completed, created_at 
               FROM reminders WHERE pet_id = ? AND is_active = 1 ORDER BY time ASC""",
            arrayOf(petId.toString())
        )

        while (cursor.moveToNext()) {
            val reminder =
                ReminderData(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                petId = cursor.getInt(cursor.getColumnIndexOrThrow("pet_id")),
                title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                reminderType = cursor.getString(cursor.getColumnIndexOrThrow("reminder_type")),
                frequency = cursor.getString(cursor.getColumnIndexOrThrow("frequency")),
                date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                time = cursor.getString(cursor.getColumnIndexOrThrow("time")),
                feedPerDay = cursor.getInt(cursor.getColumnIndexOrThrow("feed_per_day")),
                isActive = cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1,
                isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1,
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
            )
            remindersList.add(reminder)
        }
        cursor.close()
        return remindersList
    }

    fun updateReminderStatus(reminderId: Int, isCompleted: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("is_completed", if (isCompleted) 1 else 0)
        }
        db.update("reminders", values, "id = ?", arrayOf(reminderId.toString()))
    }

    fun deleteReminder(reminderId: Int) {
        val db = writableDatabase
        db.delete("reminders", "id = ?", arrayOf(reminderId.toString()))
    }

    fun deactivateReminder(reminderId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("is_active", 0)
        }
        db.update("reminders", values, "id = ?", arrayOf(reminderId.toString()))
    }
}