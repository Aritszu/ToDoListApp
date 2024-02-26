package com.example.todolistapp

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import android.os.Handler
import android.os.Looper


class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: TaskDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var addTaskButton: FloatingActionButton
    private lateinit var taskAdapter: TaskAdapter
    private val handler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = TaskDatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerViewTasks)
        recyclerView.layoutManager = LinearLayoutManager(this)

        addTaskButton = findViewById(R.id.fabAddTask)
        addTaskButton.setOnClickListener { showAddTaskDialog() }

        taskAdapter = TaskAdapter(arrayListOf()) { task -> handleTaskClick(task) }
        recyclerView.adapter = taskAdapter

        setupSwipeToDelete()
        prepareData() // Initial data load
    }

    private val updateTaskStatusRunnable = object : Runnable {
        override fun run() {
            updateTasksStatus()  // Update the display of tasks
            handler?.postDelayed(this, 5000)  // Reschedule the runnable every 5 seconds
        }
    }


    private fun updateTasksStatus() {
        taskAdapter.updateTaskDisplay()  // Ask the adapter to update the display of tasks
    }

    override fun onStart() {
        super.onStart()
        handler?.post(updateTaskStatusRunnable)  // Start the updates when activity starts
    }

    override fun onStop() {
        super.onStop()
        handler?.removeCallbacks(updateTaskStatusRunnable)  // Stop the updates when activity stops
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
                // Check if the item is not a Header or Spacer before attempting to delete
                when (val item = taskAdapter.getItems()[position]) {
                    is Task -> {
                        dbHelper.deleteTask(item.id)
                        prepareData() // Refresh data after deleting a task
                    }
                    else -> {
                        taskAdapter.notifyItemChanged(position)
                    }
                }
            }

            override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                // Disable swipe for Header and Spacer
                if (viewHolder.itemViewType == TaskAdapter.VIEW_TYPE_HEADER || viewHolder.itemViewType == TaskAdapter.VIEW_TYPE_SPACER) {
                    return 0
                }
                return super.getSwipeDirs(recyclerView, viewHolder)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }



    private fun prepareData() {
        val todayTasks = dbHelper.getTodayTasks() // Includes "today" header and tasks
        val tomorrowTasks = dbHelper.getTomorrowTasks() // Includes "tomorrow" header and tasks

        val items = mutableListOf<Any>().apply {
            if (todayTasks.size > 1) { // Check if there are tasks besides the header
                addAll(todayTasks)
            } else {
                add(TaskHeader("today", "No tasks for today"))
            }
            add(Spacer(height = 50)) // Add spacer between sections regardless

            if (tomorrowTasks.size > 1) { // Check if there are tasks besides the header
                addAll(tomorrowTasks)
            } else {
                add(TaskHeader("tomorrow", "No tasks for tomorrow"))
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
        layout.addView(datePicker)

        val calendar = Calendar.getInstance()
        task?.let {
            calendar.time = it.dueDate
        }
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null)

        AlertDialog.Builder(this)
            .setTitle(if (task == null) "Add New Task" else "Edit Task")
            .setView(layout)
            .setPositiveButton("Next") { _, _ ->
                val year = datePicker.year
                val month = datePicker.month
                val day = datePicker.dayOfMonth

                val timeSetListener = TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay: Int, minute: Int ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)

                    val newTask = task?.copy(
                        title = titleEditText.text.toString(),
                        description = descriptionEditText.text.toString(),
                        dueDate = calendar.time
                    ) ?: Task(
                        id = 0,
                        title = titleEditText.text.toString(),
                        description = descriptionEditText.text.toString(),
                        dueDate = calendar.time
                    )

                    if (task == null) {
                        dbHelper.addTask(newTask)
                    } else {
                        dbHelper.updateTask(newTask)
                    }
                    prepareData()
                }

                val timePickerDialog = TimePickerDialog(this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
                timePickerDialog.show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

    data class Spacer(val height: Int = 150)

