package com.example.reminderapp4.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reminderapp4.R
import com.example.reminderapp4.model.Reminder
import com.example.reminderapp4.ui.addreminder.AddActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val reminders = arrayListOf<Reminder>()
    private val reminderAdapter = ReminderAdapter(reminders)

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setTitle(R.string.app_name)
        initViews()
        initViewModel()
    }

    private fun initViews() {

        // Initialize the recycler view with a linear layout manager, adapter, decorator and swipe callback.
        rvReminders.layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
        rvReminders.adapter = reminderAdapter
        rvReminders.addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
        createItemTouchHelper().attachToRecyclerView(rvReminders)

        // Clicking floating action button will call startAddActivity.
        fab.setOnClickListener {
            startAddActivity()
        }

    }


    /**
     * Create a touch helper to recognize when a user swipes an item from a recycler view.
     * An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
     * and uses callbacks to signal when a user is performing these actions.
     */
    private fun createItemTouchHelper(): ItemTouchHelper {

        // Callback which is used to create the ItemTouch helper. Only enables left swipe.
        // Use ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) to also enable right swipe.
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // Enables or Disables the ability to move items up and down.
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            // Callback triggered when a user swiped an item.
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val reminderToDelete = reminders[position]

                viewModel.deleteReminder(reminderToDelete)
            }
        }
        return ItemTouchHelper(callback)
    }

    /**
     * Start [AddActivity].
     */
    private fun startAddActivity() {
        val intent = Intent(this, AddActivity::class.java)
        startActivityForResult(
            intent,
            ADD_REMINDER_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ADD_REMINDER_REQUEST_CODE -> {
                    val reminder = data!!.getParcelableExtra<Reminder>(AddActivity.EXTRA_REMINDER)

                    viewModel.insertReminder(reminder)
                }
            }
        }
    }

    companion object {
        const val ADD_REMINDER_REQUEST_CODE = 100
    }


    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        // Observe reminders from the view model, update the list when the data is changed.
        viewModel.reminders.observe(this, Observer { reminders ->
            this@MainActivity.reminders.clear()
            this@MainActivity.reminders.addAll(reminders)
            reminderAdapter.notifyDataSetChanged()
        })
    }


}
