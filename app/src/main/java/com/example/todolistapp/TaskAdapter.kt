package com.example.todolistapp

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Date
import androidx.core.content.ContextCompat
import java.util.Locale

data class Header(val title: String)

class TaskAdapter(private var items: List<Any>, private val itemClickListener: (Task) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_TASK = 1
        private const val VIEW_TYPE_PLACEHOLDER = 2
        private const val COMPLETED_COLOR = Color.GRAY
        private const val NOT_COMPLETED_COLOR = Color.BLACK
        private const val COMPLETED_TEXT_COLOR = Color.GREEN
    }

    fun getItems(): List<Any> {
        return items
    }


    fun updateItems(newItems: List<Any>) {
        items = newItems
        notifyDataSetChanged()
    }

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskTitleTextView: TextView = view.findViewById(R.id.task_title)
        val taskStatusImageView: ImageView = view.findViewById(R.id.task_status)
        val taskDueDateTextView: TextView = view.findViewById(R.id.task_due_date)
        private val context = view.context // Context to access resources

        fun bind(task: Task, itemClickListener: (Task) -> Unit, getRemainingTime: (Date) -> String) {
            taskTitleTextView.text = task.title

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
                taskDueDateTextView.text = getRemainingTime(task.dueDate)
                taskDueDateTextView.setTextColor(NOT_COMPLETED_COLOR)
            }

            itemView.setOnClickListener {
                itemClickListener.invoke(task)
            }
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerTitleTextView: TextView = view.findViewById(R.id.header_title)

        fun bind(header: Header) {
            headerTitleTextView.text = header.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val headerView = LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false)
                HeaderViewHolder(headerView)
            }
            VIEW_TYPE_TASK, VIEW_TYPE_PLACEHOLDER -> {
                val taskView = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
                TaskViewHolder(taskView)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is Task -> (holder as TaskViewHolder).bind(item, itemClickListener, ::getRemainingTime)
            is Header -> (holder as HeaderViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is Header -> VIEW_TYPE_HEADER
            is Task -> if (item.isPlaceholder) VIEW_TYPE_PLACEHOLDER else VIEW_TYPE_TASK
            else -> throw IllegalArgumentException("Unknown item type at position $position")
        }
    }

    // Moved out of the companion object to be a member function
    private fun getRemainingTime(dueDate: Date): String {
        val currentTime = System.currentTimeMillis()
        val diff = dueDate.time - currentTime
        val hours = diff / (1000 * 60 * 60) % 24
        val minutes = diff / (1000 * 60) % 60
        return String.format(Locale.getDefault(), "%02dhrs %02dmins", hours, minutes)
    }

    // Method to get a task based on position for use in MainActivity
    fun getTaskAtPosition(position: Int): Task {
        val item = items[position]
        if (item is Task) {
            return item
        } else {
            throw IllegalArgumentException("Item at position $position is not a Task.")
        }
    }
}





