package com.example.todolistapp

import java.util.Date

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val dueDate: Date,
    val isCompleted: Boolean = false,
    val isForToday: Boolean = false,
    val isForTomorrow: Boolean = false,
    val isPlaceholder: Boolean = false
)







