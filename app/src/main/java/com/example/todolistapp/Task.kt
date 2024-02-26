package com.example.todolistapp

import java.util.Date

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val dueDate: Date,
    var isCompleted: Boolean = false,
    var isCurrently: Boolean = false
)








