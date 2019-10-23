package com.practice.todo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.practice.todo.service.LocationService;
import com.practice.todo.service.RemindService;
import com.practice.todo.storage.database.DB;
import com.practice.todo.storage.database.dao.TodoItemDao;
import com.practice.todo.storage.database.dao.TodoSubItemDao;
import com.practice.todo.storage.database.entity.TodoItem;
import com.practice.todo.storage.database.entity.TodoSubItem;
import com.practice.todo.util.FormatUtil;
import com.practice.todo.util.InMemoryCache;
import com.practice.todo.util.LinearLayoutListHelper;

import java.util.Arrays;
import java.util.Calendar;

/**
 * 编辑页面，这个页面主要有以下几个元素
 * 1、数据库dao
 * 2、一个控制子事项的LinearLayout，它是通过addView方法来添加子View的
 * 3、日期选择DatePicker
 * 4、定位权限获取
 */
public class TodoEditActivity extends AppCompatActivity {

    private static final String EXT_ITEM_ID = "item_id";
    public static final int REQ_CODE = 0x22;
    public static final int RES_CODE = 0x33;
    private static final int REQ_PERMISSION_CODE = 0x11;
    private CheckBox mCbIsDone;
    private EditText mEtTitle;
    private EditText mEtTodoDescription;
    private TextView mTvRemindTime;
    private TextView mTvLongitude;
    private TextView mTvLatitude;

    public static void start(Activity activity, int itemId) {
        Intent intent = new Intent(activity, TodoEditActivity.class);
        intent.putExtra(EXT_ITEM_ID, itemId);
        activity.startActivityForResult(intent, REQ_CODE);
    }

    private int mParentId;
    private TodoItemDao mTodoItemDao;
    private TodoSubItemDao mTodoSubItemDao;
    private TodoItem mTodoItem;
    private SubItemAdapter mSubItemAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_edit);

        mParentId = getIntent().getIntExtra(EXT_ITEM_ID, 0);
        mTodoItemDao = DB.sDb.todoItemDao();
        mTodoSubItemDao = DB.sDb.todoSubItemDao();
        initView();
        refreshView();
    }

    @Override
    public void onBackPressed() {
        setResult(RES_CODE);
        super.onBackPressed();
    }

    /**
     * 权限获取成功
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQ_PERMISSION_CODE == requestCode &&
                grantResults.length != 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            refreshView();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Authorized success");
            builder.setMessage("Whether to remind approaching of the target location which you selected?");
            builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    LocationSelActivity.start(TodoEditActivity.this);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    /**
     * 从定位界面回来设置定位提醒
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LocationSelActivity.REQ_CODE &&
                resultCode == LocationSelActivity.RES_CODE &&
                data != null) {
            LatLng targetLatLng = data.getParcelableExtra(LocationSelActivity.IE_LOCATION);
            Location targetLocation = new Location("targetLocation");
            targetLocation.setLongitude(targetLatLng.longitude);
            targetLocation.setLatitude(targetLatLng.latitude);
            prepareToLocation(targetLocation);
        }
    }

    private void initView() {
        Toolbar mToolbar = findViewById(R.id.mToolbar);
        mCbIsDone = findViewById(R.id.mCbIsDone);
        mEtTitle = findViewById(R.id.mEtTitle);
        ImageView mBtnDelete = findViewById(R.id.mBtnDelete);
        mEtTodoDescription = findViewById(R.id.mEtTodoDescription);
        LinearLayout mLlSubList = findViewById(R.id.mLlSubList);
        TextView mTvRemindByTime = findViewById(R.id.mTvRemindByTime);
        mTvRemindTime = findViewById(R.id.mTvRemindTime);
        TextView mTvRemindByLocation = findViewById(R.id.mTvRemindByLocation);
        mTvLongitude = findViewById(R.id.mTvLongitude);
        mTvLatitude = findViewById(R.id.mTvLatitude);
        FloatingActionButton mFabAdd = findViewById(R.id.mFabAdd);

        mSubItemAdapter = new SubItemAdapter(this, mLlSubList);

        // 设置toolbar返回事件
        setSupportActionBar(mToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        // 删除点击事件
        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTodoItem == null) return;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mTodoItemDao.delete(mTodoItem);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onBackPressed();
                            }
                        });
                    }
                }).start();
            }
        });
        // 编辑todo描述
        mEtTodoDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String editString = s == null ? "" : s.toString().trim();
                mTodoItem.setDescription(editString);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mTodoItemDao.update(mTodoItem);
                    }
                }).start();
            }
        });
        // 添加子事项
        mFabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new InputDialog(TodoEditActivity.this, new InputDialog.OnSendListener() {
                    @Override
                    public void onSend(final String content) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mTodoSubItemDao.insert(new TodoSubItem(mParentId, content));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshView();
                                    }
                                });
                            }
                        }).start();
                    }
                }).show();
            }
        });
        // todo事项是否已完成的标记
        mCbIsDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mTodoItem.setDone(isChecked);
                        mTodoItemDao.update(mTodoItem);
                    }
                }).start();
            }
        });
        // 编辑todo标题
        mEtTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null) return;
                final String editString = s.toString();
                if (TextUtils.isEmpty(editString)) {
                    mEtTitle.setText(mTodoItem.getTitle());
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mTodoItem.setTitle(editString);
                            mTodoItemDao.update(mTodoItem);
                        }
                    }).start();
                }
            }
        });
        // 定位按钮
        mTvRemindByLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGrantedPms()) {
                    LocationSelActivity.start(TodoEditActivity.this);
                } else {
                    ActivityCompat.requestPermissions(
                            TodoEditActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSION_CODE
                    );
                }
            }
        });
        // 点击开启日期选择
        mTvRemindByTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar currentTime = Calendar.getInstance();
                final Calendar selectedTime = Calendar.getInstance();
                final TodoItem tempItem = mTodoItem.copy();
                new DatePickerDialog(TodoEditActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedTime.set(Calendar.YEAR, year);
                        selectedTime.set(Calendar.MONTH, month);
                        selectedTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        new TimePickerDialog(TodoEditActivity.this, 0, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedTime.set(Calendar.MINUTE, minute);
                                if (selectedTime.getTimeInMillis() < System.currentTimeMillis()) {
                                    Toast.makeText(TodoEditActivity.this, "Please select a future time", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                mTvRemindTime.setText(FormatUtil.formatToTime(selectedTime.getTimeInMillis()));

                                // 选择完正确的时间，即可开启前台服务进行提醒
                                Intent serIntent = new Intent(TodoEditActivity.this, RemindService.class);
                                tempItem.setRemindTimeMillis(selectedTime.getTimeInMillis());
                                serIntent.putExtra(RemindService.INTENT_EXT_TODO_ITEM, tempItem);
                                startService(serIntent);
                            }
                        }, currentTime.get(Calendar.HOUR_OF_DAY),
                                currentTime.get(Calendar.MINUTE),
                                false).show();
                    }
                },
                        currentTime.get(Calendar.YEAR),
                        currentTime.get(Calendar.MONTH),
                        currentTime.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    // 刷新界面布局
    private void refreshView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mTodoItem = mTodoItemDao.loadById(mParentId);
                final TodoSubItem[] subItemArray = mTodoSubItemDao.loadByParentId(mTodoItem.getId());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEtTitle.setText(mTodoItem.getTitle());
                        mEtTodoDescription.setText(mTodoItem.getDescription());
                        if (InMemoryCache.RemindLocationCache.itemDbId == mTodoItem.getId()) {
                            mTvLongitude.setText(String.valueOf(InMemoryCache.RemindLocationCache.remindLocation.getLongitude()));
                            mTvLatitude.setText(String.valueOf(InMemoryCache.RemindLocationCache.remindLocation.getLatitude()));
                        }
                        if (InMemoryCache.RemindTimeCache.itemDbId == mTodoItem.getId()) {
                            mTodoItem.setRemindTimeMillis(InMemoryCache.RemindTimeCache.remindTimeMills);
                            mTvRemindTime.setText(FormatUtil.formatToTime(mTodoItem.getRemindTimeMillis()));
                        }
                        mSubItemAdapter.refreshData(Arrays.asList(subItemArray));
                        mCbIsDone.setChecked(mTodoItem.isDone());
                    }
                });
            }
        }).start();
    }

    // 开始定位提醒
    private void prepareToLocation(Location targetLocation) {
        mTvLatitude.setText(String.valueOf(targetLocation.getLatitude()));
        mTvLongitude.setText(String.valueOf(targetLocation.getLongitude()));
        Intent serIntent = new Intent(TodoEditActivity.this, LocationService.class);
        TodoItem tempItem = mTodoItem.copy();
        tempItem.setRemindLocation(targetLocation);
        serIntent.putExtra(LocationService.INTENT_EXT_TODO_ITEM, tempItem);
        startService(serIntent);
    }

    // 是否已经授权定位权限
    private boolean isGrantedPms() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    // 子事项的Adapter
    class SubItemAdapter extends LinearLayoutListHelper.SimpleLinearAdapter<ViewHolder, TodoSubItem> {

        public SubItemAdapter(Context context, LinearLayout parent) {
            super(context, parent);
        }

        @Override
        public ViewHolder onCreateViewHolder(LinearLayout parent, int type) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_todo_sub_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        protected void onSimpleBindView(final ViewHolder holder, final TodoSubItem item, int position) {
            // 局部方法，更新界面，方便下面重用
            final Runnable changeTextStyleAction = new Runnable() {
                @Override
                public void run() {
                    final int titleTextColor;
                    if (item.isDone()) {
                        titleTextColor = getColor(R.color.textColorHint);
                    } else {
                        titleTextColor = getColor(R.color.textColor);
                    }
                    holder.mEtSubTitle.setTextColor(titleTextColor);
                    if (item.isDone()) {
                        holder.mEtSubTitle.setPaintFlags(holder.mEtSubTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
                        holder.mEtSubTitle.setPaintFlags(holder.mEtSubTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                }
            };

            holder.mEtSubTitle.setText(item.getTitle());
            holder.mCbSubIsDone.setChecked(item.isDone());
            changeTextStyleAction.run();

            holder.mEtSubTitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    final String editString = s.toString().trim();
                    if (TextUtils.isEmpty(editString)) {
                        holder.mEtSubTitle.setText(item.getTitle());
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                item.setTitle(editString);
                                mTodoSubItemDao.update(item);
                            }
                        }).start();
                    }
                }
            });

            holder.mBtnSubDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mTodoSubItemDao.delete(item);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refreshView();
                                }
                            });
                        }
                    }).start();
                }
            });

            holder.mCbSubIsDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            item.setDone(isChecked);
                            mTodoSubItemDao.update(item);
                            changeTextStyleAction.run();
                        }
                    }).start();
                }
            });
        }
    }

    /**
     * ViewHolder就是代表一个ItemView的类，Item的界面可以通过它控制
     */
    static class ViewHolder extends LinearLayoutListHelper.ViewHolder {

        CheckBox mCbSubIsDone;
        EditText mEtSubTitle;
        ImageView mBtnSubDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            mCbSubIsDone = itemView.findViewById(R.id.mCbSubIsDone);
            mEtSubTitle = itemView.findViewById(R.id.mEtSubTitle);
            mBtnSubDelete = itemView.findViewById(R.id.mBtnSubDelete);
        }
    }
}
