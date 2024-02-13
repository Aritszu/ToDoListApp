package com.example.todolistapp

import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: TaskDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var addTaskButton: FloatingActionButton
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = TaskDatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerViewTasks)
        recyclerView.layoutManager = LinearLayoutManager(this)

        addTaskButton = findViewById(R.id.fabAddTask)
        addTaskButton.setOnClickListener { showAddTaskDialog() }

        taskAdapter = TaskAdapter(arrayListOf()) { task ->
            handleTaskClick(task)
        }
        recyclerView.adapter = taskAdapter

        setupSwipeToDelete()
        prepareData() // Initial data load
    }

    private fun handleTaskClick(task: Task) {
        val updatedTask = task.copy(isCompleted = !task.isCompleted)
        dbHelper.updateTask(updatedTask)
        prepareData() // Refresh data after updating task status
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = taskAdapter.getTaskAtPosition(position)
                dbHelper.deleteTask(task.id)
                prepareData() // Refresh data after deleting a task
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    private fun prepareData() {
        val todayTasks = dbHelper.getTodayTasks() // Fetch today's tasks
        val tomorrowTasks = dbHelper.getTomorrowTasks() // Fetch tomorrow's tasks

        val items = mutableListOf<Any>().apply {
            if (todayTasks.isNotEmpty()) {
                add(Header("Today"))
                addAll(todayTasks)
            } else {
                add(Header("No Tasks for Today"))
            }
            if (tomorrowTasks.isNotEmpty()) {
                add(Header("Tomorrow"))
                addAll(tomorrowTasks)
            } else {
                add(Header("No Tasks for Tomorrow"))
            }
        }

        taskAdapter.updateItems(items)
    }

    private fun showAddTaskDialog(task: Task? = null) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
        }

        val titleEditText = EditText(this).apply {
            hint = "Enter task title"
            setText(task?.title ?: "")
        }
        layout.addView(titleEditText)

        val descriptionEditText = EditText(this).apply {
            hint = "Enter task description"
            setText(task?.description ?: "")
        }
        layout.addView(descriptionEditText)

        val datePicker = DatePicker(this)
        if (task != null) {
            val calendar = Calendar.getInstance().apply { time = task.dueDate }
            datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        }
        layout.addView(datePicker)

        AlertDialog.Builder(this)
            .setTitle(if (task == null) "Add New Task" else "Edit Task")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val title = titleEditText.text.toString()
                val description = descriptionEditText.text.toString()
                val calendar = Calendar.getInstance().apply {
                    set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                }
                val dueDate = calendar.time
                val newTask = task?.copy(
                    title = title,
                    description = description,
                    dueDate = dueDate
                ) ?: Task(
                    id = 0,
                    title = title,
                    description = description,
                    dueDate = dueDate
                )
                if (task == null) {
                    dbHelper.addTask(newTask)
                } else {
                    dbHelper.updateTask(newTask)
                }
                prepareData() // Refresh data after adding/editing a task
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}


