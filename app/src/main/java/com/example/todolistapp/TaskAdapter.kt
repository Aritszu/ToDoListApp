package com.example.todolistapp

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import androidx.core.content.ContextCompat
import java.util.Locale
import java.util.Calendar




class TaskAdapter(private var items: List<Any>, private val itemClickListener: (Task) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_TASK = 1
        const val VIEW_TYPE_PLACEHOLDER = 2
        const val VIEW_TYPE_SPACER = 3
        private const val NOT_COMPLETED_COLOR = Color.BLACK
    }

    fun getItems(): List<Any> = items

    fun updateItems(newItems: List<Any>) {
        items = newItems
        notifyDataSetChanged()
    }

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskTitleTextView: TextView = view.findViewById(R.id.task_title)
        val taskStatusImageView: ImageView = view.findViewById(R.id.task_status)
        val taskDueDateTextView: TextView = view.findViewById(R.id.task_due_date)
        private val context = view.context

        fun bind(task: Task, itemClickListener: (Task) -> Unit) {
            taskTitleTextView.text = task.title

            val currentDateTime = Calendar.getInstance().time
            val isCurrentlyActive = !task.isCompleted && currentDateTime.after(task.dueDate)

            if (task.isCompleted) {
                taskTitleTextView.paintFlags = taskTitleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                taskTitleTextView.setTextColor(ContextCompat.getColor(context, R.color.gray))
                taskStatusImageView.setImageResource(R.drawable.component_6__1)
                taskDueDateTextView.text = "Completed"
                taskDueDateTextView.setTextColor(ContextCompat.getColor(context, R.color.green))
            } else {
                taskTitleTextView.paintFlags = taskTitleTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                taskTitleTextView.setTextColor(NOT_COMPLETED_COLOR)
                taskStatusImageView.setImageResource(R.drawable.rectangle_3)
                taskDueDateTextView.setTextColor(NOT_COMPLETED_COLOR)

                // Get current time and format it
                val currentCalendar = Calendar.getInstance()
                val taskCalendar = Calendar.getInstance().apply { time = task.dueDate }

                // Check if the current time is past the task's due time
                if (!task.isCompleted && currentCalendar.after(taskCalendar)) {
                    taskDueDateTextView.text = "Currently"
                    taskDueDateTextView.setTextColor(ContextCompat.getColor(context, R.color.blue))
                } else {
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val timeText = "At ${timeFormat.format(task.dueDate)}"
                    taskDueDateTextView.text = timeText
                    taskDueDateTextView.setTextColor(NOT_COMPLETED_COLOR)
                }
            }
            // Set up click listener for the entire task view
            itemView.setOnClickListener { itemClickListener(task) }
        }

    }
    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerTitleTextView: TextView = view.findViewById(R.id.header_title)
        val headerDateInfoTextView: TextView = view.findViewById(R.id.header_date_info)

        fun bind(header: TaskHeader) {
            headerTitleTextView.text = header.title
            headerDateInfoTextView.text = header.dateInfo
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        VIEW_TYPE_HEADER -> HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false))
        VIEW_TYPE_TASK, VIEW_TYPE_PLACEHOLDER -> TaskViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false))
        VIEW_TYPE_SPACER -> SpacerViewHolder(View(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50)
        })
        else -> throw IllegalArgumentException("Invalid view type")
    }

    class SpacerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(spacer: Spacer) {
            itemView.layoutParams.height = spacer.height
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (val item = items[position]) {
        is Task -> (holder as TaskViewHolder).bind(item, itemClickListener)
        is TaskHeader -> (holder as HeaderViewHolder).bind(item)
        is Spacer -> (holder as SpacerViewHolder).bind(item)
        else -> throw IllegalArgumentException("Unknown item type at position $position")
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is TaskHeader -> VIEW_TYPE_HEADER
        is Task -> VIEW_TYPE_TASK
        is Spacer -> VIEW_TYPE_SPACER
        else -> throw IllegalArgumentException("Unknown item type at position $position")
    }

    fun updateTaskDisplay() {
        val currentTime = Calendar.getInstance().time
        items.forEach { item ->
            if (item is Task) {
                // Check if the current time is after the task's due date and the task is not completed
                item.isCurrently = currentTime.after(item.dueDate) && !item.isCompleted
            }
        }
        notifyDataSetChanged() // Notify the adapter to rebind the data to reflect any changes
    }
}







