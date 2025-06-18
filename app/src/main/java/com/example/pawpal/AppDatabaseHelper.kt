package com.example.pawpal

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class ChatMessage(
    val id: Long = 0,
    val message: String,
    val isUser: Boolean,
    val timestamp: String
)

class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "pawpal.db", null, 1) {

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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS chat")
        db.execSQL("DROP TABLE IF EXISTS pet")
        db.execSQL("DROP TABLE IF EXISTS owner")
        onCreate(db)
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
        dob: String
    ): Boolean {
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
            if (ownerId == -1L) return false

            val petValues = ContentValues().apply {
                put("name", petName)
                put("type", petType)
                put("breed", breed)
                put("sex", sex)
                put("dob", dob)
                put("owner_id", ownerId)
            }
            val petResult = db.insert("pet", null, petValues)
            if (petResult == -1L) return false

            db.setTransactionSuccessful()
            true
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
}