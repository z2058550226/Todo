package com.practice.todo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.maps.model.LatLng
import com.practice.todo.base.CoroutineActivity
import com.practice.todo.service.LocationService
import com.practice.todo.service.RemindService
import com.practice.todo.storage.database.db
import com.practice.todo.storage.database.entity.TodoItem
import com.practice.todo.storage.database.entity.TodoSubItem
import com.practice.todo.util.InMemoryCache
import com.practice.todo.util.LinearLayoutListHelper
import com.practice.todo.util.bindView
import com.practice.todo.util.formatToTime
import kotlinx.android.synthetic.main.activity_todo_edit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import java.util.*

class TodoEditActivity : CoroutineActivity() {

    companion object {
        private const val EXT_ITEM_ID = "item_id"
        const val REQ_CODE = 0x22
        const val RES_CODE = 0x33
        const val REQ_PERMISSION_CODE = 0x11

        fun start(activity: Activity, itemId: Int) {
            val intent = Intent(activity, TodoEditActivity::class.java)
            intent.putExtra(EXT_ITEM_ID, itemId)
            activity.startActivityForResult(intent, REQ_CODE)
        }
    }

    private val mParentId by lazy { intent.getIntExtra(EXT_ITEM_ID, 0) }
    private val mTodoItemDao by lazy { db.todoItemDao() }
    private val mTodoSubItemDao by lazy { db.todoSubItemDao() }
    private lateinit var mTodoItem: TodoItem
    private lateinit var mSubAdapter: SubItemAdapter
    private val isGrantedPms: Boolean
        get() = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_edit)

        initView()
        refreshView()
    }

    override fun onBackPressed() {
        setResult(RES_CODE)
        super.onBackPressed()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (REQ_PERMISSION_CODE == requestCode &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            refreshView()
            alert {
                title = "Authorized success"
                message = "Whether to remind approaching of the target location which you selected?"
                positiveButton("yes") {
                    LocationSelActivity.start(this@TodoEditActivity)
                    it.dismiss()
                }
                negativeButton("no", DialogInterface::dismiss)
            }.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LocationSelActivity.REQ_CODE &&
            resultCode == LocationSelActivity.RES_CODE &&
            data != null
        ) {
            val targetLatLng = data.getParcelableExtra<LatLng>(LocationSelActivity.IE_LOCATION)
            val targetLocation = Location("targetLocation")
            targetLocation.longitude = targetLatLng.longitude
            targetLocation.latitude = targetLatLng.latitude
            prepareToLocation(targetLocation)
        }
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun initView() {
        mSubAdapter = SubItemAdapter(mLlSubList)

        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        mToolbar.setNavigationOnClickListener { finish() }
        mBtnDelete.setOnClickListener {
            if (this::mTodoItem.isInitialized.not()) return@setOnClickListener
            launch {
                withContext(Dispatchers.IO) {
                    mTodoItemDao.delete(mTodoItem)
                    setResult(RES_CODE)
                    finish()
                }
            }
        }
        mEtTodoDescription.addTextChangedListener {
            val editString = it?.toString()?.trim() ?: ""
            launch {
                mTodoItem.description = editString
                withContext(Dispatchers.IO) {
                    mTodoItemDao.update(mTodoItem)
                }
            }
        }
        mFabAdd.setOnClickListener {
            InputDialog(this) { content ->
                launch {
                    withContext(Dispatchers.IO) {
                        mTodoSubItemDao.insert(TodoSubItem(mParentId, content))
                    }
                    refreshView()
                }
            }.show()
        }
        mCbIsDone.setOnCheckedChangeListener { _, isChecked ->
            launch {
                mTodoItem.isDone = isChecked
                withContext(Dispatchers.IO) {
                    mTodoItemDao.update(mTodoItem)
                }
            }
        }
        mEtTitle.addTextChangedListener { et ->
            val editString = et?.toString()
            if (editString.isNullOrEmpty()) {
                mEtTitle.setText(mTodoItem.title)
            } else {
                launch {
                    withContext(Dispatchers.IO) {
                        mTodoItem.title = editString
                        mTodoItemDao.update(mTodoItem)
                    }
                }
            }
        }
        mTvRemindByLocation.setOnClickListener {
            if (isGrantedPms) {
                LocationSelActivity.start(this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQ_PERMISSION_CODE
                )
            }
        }
        mTvRemindByTime.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val selectedTime = Calendar.getInstance()
            val tempItem = mTodoItem.copy()
            DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    selectedTime.set(Calendar.YEAR, year)
                    selectedTime.set(Calendar.MONTH, month)
                    selectedTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    TimePickerDialog(
                        this,
                        0,
                        TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            selectedTime.set(Calendar.MINUTE, minute)
                            if (selectedTime.timeInMillis < System.currentTimeMillis()) {
                                toast("Please select a future time")
                                return@OnTimeSetListener
                            }
                            mTvRemindTime.text = selectedTime.timeInMillis.formatToTime()
                            val serIntent = Intent(this, RemindService::class.java)
                            tempItem.remindTimeMillis = selectedTime.timeInMillis
                            serIntent.putExtra(RemindService.INTENT_EXT_TODO_ITEM, tempItem)
                            startService(serIntent)
                        },
                        currentTime.get(Calendar.HOUR_OF_DAY),
                        currentTime.get(Calendar.MINUTE),
                        false
                    ).show()
                },
                currentTime.get(Calendar.YEAR),
                currentTime.get(Calendar.MONTH),
                currentTime.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun prepareToLocation(targetLocation: Location) {
        mTvLatitude.text = targetLocation.latitude.toString()
        mTvLongitude.text = targetLocation.longitude.toString()
        val serIntent = Intent(this, LocationService::class.java)
        val tempItem = mTodoItem.copy()
        tempItem.remindLocation = targetLocation
        serIntent.putExtra(LocationService.INTENT_EXT_TODO_ITEM, tempItem)
        startService(serIntent)
    }

    @SuppressLint("SetTextI18n")
    private fun refreshView() = launch {
        mTodoItem = withContext(Dispatchers.IO) {
            mTodoItemDao.loadById(mParentId)
        }
        mEtTitle.setText(mTodoItem.title)
        mEtTodoDescription.setText(mTodoItem.description)
        if (InMemoryCache.RemindLocationCache.itemDbId == mTodoItem.id) {
            mTvLongitude.text =
                InMemoryCache.RemindLocationCache.remindLocation?.longitude?.toString() ?: ""
            mTvLatitude.text =
                InMemoryCache.RemindLocationCache.remindLocation?.longitude?.toString() ?: ""
        }
        if (InMemoryCache.RemindTimeCache.itemDbId == mTodoItem.id) {
            mTodoItem.remindTimeMillis = InMemoryCache.RemindTimeCache.remindTimeMills
            mTvRemindTime.text = mTodoItem.remindTimeMillis.formatToTime()
        }

        val subItemArray = withContext(Dispatchers.IO) {
            mTodoSubItemDao.loadByParentId(mTodoItem.id)
        }

        mSubAdapter.refreshData(subItemArray.toMutableList())
        mCbIsDone.isChecked = mTodoItem.isDone
    }

    inner class SubItemAdapter(parent: LinearLayout) :
        LinearLayoutListHelper.SimpleLinearAdapter<ViewHolder, TodoSubItem>(parent) {

        override fun onCreateViewHolder(parent: LinearLayout, type: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_todo_sub_list, parent, false)
            return ViewHolder(view)
        }

        override fun onSimpleBindView(holder: ViewHolder, item: TodoSubItem, position: Int) {
            val changeTextStyleAction = fun() {
                val titleTextColor =
                    if (item.isDone) getColor(R.color.textColorHint) else getColor(R.color.textColor)
                holder.mEtSubTitle.setTextColor(titleTextColor)
                if (item.isDone) {
                    holder.mEtSubTitle.paintFlags =
                        holder.mEtSubTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    holder.mEtSubTitle.paintFlags =
                        holder.mEtSubTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }

            holder.mEtSubTitle.setText(item.title)
            holder.mCbSubIsDone.isChecked = item.isDone
            changeTextStyleAction()

            holder.mEtSubTitle.addTextChangedListener { et ->
                val editString = et?.toString()
                if (editString.isNullOrEmpty()) {
                    holder.mEtSubTitle.setText(item.title)
                } else {
                    launch {
                        withContext(Dispatchers.IO) {
                            item.title = editString
                            mTodoSubItemDao.update(item)
                        }
                    }
                }
            }
            holder.mBtnSubDelete.setOnClickListener {
                launch {
                    withContext(Dispatchers.IO) {
                        mTodoSubItemDao.delete(item)
                    }
                    refreshView()
                }
            }
            holder.mCbSubIsDone.setOnCheckedChangeListener { _, isChecked ->
                launch {
                    item.isDone = isChecked
                    withContext(Dispatchers.IO) {
                        mTodoSubItemDao.update(item)
                    }
                    changeTextStyleAction()
                }
            }
        }
    }

    class ViewHolder(itemView: View) : LinearLayoutListHelper.ViewHolder(itemView) {
        val mCbSubIsDone: CheckBox by bindView(R.id.mCbSubIsDone)
        val mEtSubTitle: EditText by bindView(R.id.mEtSubTitle)
        val mBtnSubDelete: ImageView by bindView(R.id.mBtnSubDelete)
    }
}