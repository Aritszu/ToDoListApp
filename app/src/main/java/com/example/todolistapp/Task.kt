package com.example.todolistapp

import java.util.Date

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val dueDate: Date, // This should include both date and time
    val isCompleted: Boolean = false
)







