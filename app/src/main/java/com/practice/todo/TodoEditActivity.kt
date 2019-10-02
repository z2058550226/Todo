package com.practice.todo

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.practice.todo.base.BaseActivity
import com.practice.todo.storage.database.db
import com.practice.todo.storage.database.entity.TodoItem
import com.practice.todo.storage.database.entity.TodoSubItem
import com.practice.todo.util.LinearLayoutListHelper
import com.practice.todo.util.NotificationUtil
import com.practice.todo.util.bindView
import kotlinx.android.synthetic.main.activity_todo_edit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*

class TodoEditActivity : BaseActivity() {

    companion object {
        private const val EXT_ITEM_ID = "item_id"
        const val REQ_CODE = 0x22
        const val RES_CODE = 0x33
        const val REQ_PERMISSON_CODE = 0x11

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
    private val mLatitude: String get() = mEtLatitude.text.trim().toString()
    private val mLongitude: String get() = mEtLongitude.text.trim().toString()

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
        if (REQ_PERMISSON_CODE == requestCode &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            prepareToLocation()
        }
    }

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
        mFabAdd.setOnClickListener {
            InputDialog.show(this) { title ->
                launch {
                    withContext(Dispatchers.IO) {
                        mTodoSubItemDao.insert(TodoSubItem(parentId = mParentId, title = title))
                    }
                    refreshView()
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
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQ_PERMISSON_CODE
                )
            } else {
                prepareToLocation()
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
                            Handler(Looper.getMainLooper()).postDelayed(
                                { NotificationUtil.notification(tempItem) },
                                selectedTime.timeInMillis - currentTime.timeInMillis
                            )
                            toast(
                                "it will notify you at ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                                    Date(selectedTime.timeInMillis)
                                )}"
                            )
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

    private fun prepareToLocation() {
        if (mLongitude.isEmpty() || mLatitude.isEmpty()) {
            toast("empty input")
            return
        }
        val targetLongitude = mLongitude.toDouble()
        val targetLatitude = mLatitude.toDouble()
        if (targetLongitude > 180 || targetLongitude < -180) {
            toast("illegal longitude")
            return
        }
        if (targetLatitude > 90 || targetLatitude < -90) {
            toast("illegal latitude")
            return
        }
        LocationTask.startLocation(targetLongitude, targetLatitude,this) { curLocation, isNotify ->
            mTvCurLatitude.text = curLocation.latitude.toString()
            mTvCurLongitude.text = curLocation.longitude.toString()
            if (isNotify) {
                NotificationUtil.notification(mTodoItem)
            }
        }

    }

    private fun refreshView() = launch {
        mTodoItem = withContext(Dispatchers.IO) {
            mTodoItemDao.loadById(mParentId)
        }
        mEtTitle.setText(mTodoItem.title)
        val subItemArray = withContext(Dispatchers.IO) {
            mTodoSubItemDao.loadByParentId(mTodoItem.id)
        }
        mSubAdapter.refreshData(subItemArray.toMutableList())
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