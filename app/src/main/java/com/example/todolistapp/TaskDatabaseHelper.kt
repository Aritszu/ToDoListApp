package com.example.todolistapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class TaskDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSQL = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                dueDate INTEGER,
                isCompleted INTEGER DEFAULT 0
            );
        """.trimIndent()
        db.execSQL(createTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS tasks")
        onCreate(db)
    }

    fun addTask(task: Task): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("title", task.title)
            put("description", task.description)
            put("dueDate", task.dueDate.time)
            put("isCompleted", if (task.isCompleted) 1 else 0)
        }
        val id = db.insert("tasks", null, values)
        db.close()
        return id
    }

    fun getTasksForDate(date: Date): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = this.readableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(date)

        val selection = "strftime('%Y-%m-%d', dueDate / 1000, 'unixepoch', 'localtime') = ?"
        val selectionArgs = arrayOf(dateString)
        val cursor = db.query("tasks", null, selection, selectionArgs, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val dueDateMillis = cursor.getLong(cursor.getColumnIndexOrThrow("dueDate"))
                val isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("isCompleted")) == 1
                val dueDate = Date(dueDateMillis)
                tasks.add(Task(id, title, description, dueDate, isCompleted))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return tasks
    }

    fun getTodayTasks(): List<Task> {
        val calendar = Calendar.getInstance()
        return getTasksForDate(calendar.time)
    }

    fun getTomorrowTasks(): List<Task> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return getTasksForDate(calendar.time)
    }

    fun updateTask(task: Task): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("title", task.title)
            put("description", task.description)
            put("dueDate", task.dueDate.time)
            put("isCompleted", if (task.isCompleted) 1 else 0)
        }
        val success = db.update("tasks", values, "id = ?", arrayOf(task.id.toString()))
        db.close()
        return success
    }

    fun deleteTask(taskId: Int): Int {
        val db = this.writableDatabase
        val success = db.delete("tasks", "id = ?", arrayOf(taskId.toString()))
        db.close()
        return success
    }

    companion object {
        private const val DATABASE_NAME = "taskmanager.db"
        private const val DATABASE_VERSION = 1
    }
}



