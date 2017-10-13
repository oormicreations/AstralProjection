package in.oormi.apguide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.InputType;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    private static final int CURRENT_DB_VER = 2;
    private static final String APG_PRESET_VERSION = "1";

    private LinkedHashMap<String, GroupInfo> detailsMap = new LinkedHashMap<String, GroupInfo>();
    private ArrayList<GroupInfo> allTaskList = new ArrayList<GroupInfo>();

    private CustomAdapter listAdapter;
    private ExpandableListView simpleExpandableListView;
    public DatabaseHandler db = new DatabaseHandler(this);
    boolean animrunning = false;
    boolean allExp = true;
    long startTime = 0;
    int gnum = 0;
    int cnum = 0;
    TextToSpeech tts;
    float speechRate = 0.85f;
    Locale locale = Locale.getDefault();
    boolean doneSpeaking = true;
    float sessionSpeed = 1.0f;

    private static final int READ_REQUEST_CODE = 1042;
    private static final int WRITE_REQUEST_CODE = 1043;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {

            if (!doneSpeaking){
                //Toast.makeText(getBaseContext(), "Not Done", Toast.LENGTH_SHORT).show();
                timerHandler.postDelayed(this, 2000); //wait 2 sec
                return;
            }
            speakInstruction();

            boolean carryOn = false;
            if (cnum < allTaskList.get(gnum).getDetailsList().size() - 1) {
                cnum++;
                //timerHandler.postDelayed(this, next);
                carryOn = true;
            } else {
                if (gnum < allTaskList.size() - 1) {
                    cnum = 0;
                    gnum++;
                    //timerHandler.postDelayed(this, next);
                    carryOn = true;
                }
            }
            if (carryOn){
                timerHandler.postDelayed(this, getNextDelay());
            }
            else stopTimer(true);

        }

    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        final ImageView ivRay = (ImageView) findViewById(R.id.imageViewRay);
        ivRay.setVisibility(View.INVISIBLE);


        if(!loadDb()) initData();
        Toast toast = Toast.makeText(this,getString(R.string.longpress), Toast.LENGTH_SHORT );
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(locale);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(getBaseContext(), R.string.ttserr1,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        tts.setSpeechRate(speechRate);
                        tts.speak(getString(R.string.welcomeMsg),
                                TextToSpeech.QUEUE_FLUSH, null,
                                TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                    }
                } else
                    Toast.makeText(getBaseContext(), R.string.ttserr2,
                            Toast.LENGTH_SHORT).show();
            }
        });

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP, "APAppTag");
        wl.acquire();

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onDone(String utteranceId) {
                doneSpeaking = true;
            }

            @Override
            public void onError(String utteranceId) {

            }
        });

        simpleExpandableListView = (ExpandableListView) findViewById(R.id.listviewsession);
        listAdapter = new CustomAdapter(MainActivity.this, allTaskList);
        simpleExpandableListView.setAdapter(listAdapter);

        //  listener for group heading click
        simpleExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return false;
            }
        });

        //  listener for child row click
        simpleExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return false;
            }
        });


        simpleExpandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int itemType = ExpandableListView.getPackedPositionType(id);

                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int childPosition = ExpandableListView.getPackedPositionChild(id);
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    editChildDialog(groupPosition, childPosition);
                    return true; //true if we consumed the click, false if not

                } else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    editGroupDialog(groupPosition);
                    return true; //true if we consumed the click, false if not

                } else {
                    // null item; we don't consume the click
                    return false;
                }
            }
        });

        final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButtonStartStop);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    animrunning = false;
                    startTimer();
                } else {
                    stopTimer(true);
                    final Animation animation = AnimationUtils.loadAnimation(MainActivity.this,
                            R.anim.scaledn);
                    toggle.startAnimation(animation);
                }
            }
        });

        final ImageButton resetButton = (ImageButton) findViewById(R.id.imageButtonReset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                stopTimer(true);
                                toggle.setChecked(false);
                                backupDB();

                                allTaskList.clear();
                                detailsMap.clear();
                                initData();
                                listAdapter.notifyDataSetChanged();

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Confirm Data Reset");
                builder.setMessage("All changes will be lost. Are you sure?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
                resetButton.startAnimation(AnimationUtils.
                        loadAnimation(MainActivity.this, R.anim.buttonpress));

            }
        });

        final ImageButton mbuttonSet = (ImageButton) findViewById(R.id.imageButtonSet);
        mbuttonSet.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick (View view){
                        stopTimer(true);
                        toggle.setChecked(false);
                        editSettingDialog();
                        mbuttonSet.startAnimation(AnimationUtils.
                                loadAnimation(MainActivity.this, R.anim.buttonpress));
                    }
                });

        final ImageButton mbuttonJournal = (ImageButton) findViewById(R.id.imageButtonJournal);
        mbuttonJournal.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick (View view){
                        stopTimer(true);
                        toggle.setChecked(false);
                        mbuttonJournal.startAnimation(AnimationUtils.
                                loadAnimation(MainActivity.this, R.anim.buttonpress));
                        launchJournal();
                    }
                });

        final ImageButton mbuttonEC = (ImageButton) findViewById(R.id.imageButtonEC);
        mbuttonEC.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick (View view){
                        if (allExp) {expandAll(); allExp = false;}
                        else {collapseAll(); allExp = true;}
                        mbuttonEC.startAnimation(AnimationUtils.
                                loadAnimation(MainActivity.this, R.anim.buttonpress));
                    }
                });

        mbuttonSet.setColorFilter(Color.argb(60, 200, 0, 200));
        mbuttonJournal.setColorFilter(Color.argb(60, 200, 0, 200));
        mbuttonEC.setColorFilter(Color.argb(60, 200, 0, 200));
        resetButton.setColorFilter(Color.argb(60, 200, 0, 200));

    }

    private void editSettingDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(params);

        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(10,10,10,10);
        layout.setBackgroundColor(ContextCompat.getColor(getBaseContext(),
                R.color.colorDialogLayout));

        final TextView tv = new TextView(this);
        tv.setText(R.string.settings);
        tv.setPadding(40, 40, 40, 40);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);

        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setCustomTitle(tv);

        final TextView tvSesSpeed = new TextView(this);
        tvSesSpeed.setText(R.string.sspeed);
        tvSesSpeed.setPadding(80,10,10,10);
        layout.addView(tvSesSpeed);

        final SeekBar sessionSpeedBar = new SeekBar(this);
        sessionSpeedBar.setMax(200);
        sessionSpeedBar.setProgress(100);
        layout.addView(sessionSpeedBar);

        final TextView tvSpeechRate = new TextView(this);
        tvSpeechRate.setText(R.string.srate);
        tvSpeechRate.setPadding(80,10,10,10);
        layout.addView(tvSpeechRate);

        final SeekBar speechRateBar = new SeekBar(this);
        speechRateBar.setMax(200);
        speechRateBar.setProgress(85);
        layout.addView(speechRateBar);

        final TextView tvVoice = new TextView(this);
        tvVoice.setText(R.string.loc);
        tvVoice.setPadding(80,10,10,10);
        layout.addView(tvVoice);

        final RadioButton rb1 = new RadioButton(this);
        rb1.setText(R.string.locdef);
        rb1.setChecked(false);
        final RadioButton rb2 = new RadioButton(this);
        rb2.setText("EN-US");
        rb2.setChecked(false);
        final RadioButton rb3 = new RadioButton(this);
        rb3.setText("EN-UK");
        rb3.setChecked(false);
        final RadioButton rb4 = new RadioButton(this);
        rb4.setText("EN-IN");
        rb4.setChecked(false);

        final RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.addView(rb1);
        radioGroup.addView(rb2);
        radioGroup.addView(rb3);
        radioGroup.addView(rb4);
        radioGroup.setOrientation(RadioGroup.HORIZONTAL);
        //radioGroup.setPadding(60,10,10,10);
        radioGroup.setGravity(Gravity.CENTER);
        radioGroup.check(rb1.getId());
        layout.addView(radioGroup);


        alertDialogBuilder.setNegativeButton(R.string.editCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        alertDialogBuilder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sessionSpeed = (float)sessionSpeedBar.getProgress()/100.f;
                speechRate = (float)speechRateBar.getProgress()/100.f;
                if (sessionSpeed<0.1f) sessionSpeed = 0.1f;
                if (speechRate<0.1f) speechRate = 0.1f;

                int loc = radioGroup.getCheckedRadioButtonId();
                if (loc == rb1.getId()) locale = Locale.getDefault();
                if (loc == rb2.getId()) locale = Locale.US;
                if (loc == rb3.getId()) locale = Locale.UK;
                if (loc == rb4.getId()) locale = Locale.ENGLISH;//find out

                tts.setSpeechRate(speechRate);
                tts.setLanguage(locale);
            }
        });

        sessionSpeedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvSesSpeed.setText(getString(R.string.sspeedval) + String.valueOf(progress) + " %");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        speechRateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvSpeechRate.setText(getString(R.string.srateval) + String.valueOf(progress) + " %");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        AlertDialog edSetDialog = alertDialogBuilder.create();
        edSetDialog.getWindow().setBackgroundDrawableResource(R.color.colorDialog);
        sessionSpeedBar.setProgress((int) (sessionSpeed * 100.0f));
        speechRateBar.setProgress((int) (speechRate * 100.0f));
        tvSpeechRate.setText(getString(R.string.srateval) + String.valueOf(speechRateBar.getProgress()) + " %");
        tvSesSpeed.setText(getString(R.string.sspeedval) + String.valueOf(sessionSpeedBar.getProgress()) + " %");
        if (locale == Locale.getDefault()) radioGroup.check(rb1.getId());
        if (locale == Locale.US) radioGroup.check(rb2.getId());
        if (locale == Locale.UK) radioGroup.check(rb3.getId());
        if (locale == Locale.ENGLISH) radioGroup.check(rb4.getId());

        try {
            edSetDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startTimer() {
        startTime = System.currentTimeMillis();
        gnum = 0;
        cnum = 0;
        doneSpeaking = true;
        timerHandler.postDelayed(timerRunnable, getNextDelay());

        if (!animrunning) {
            final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButtonStartStop);
            final Animation animation = AnimationUtils.loadAnimation(MainActivity.this,
                    R.anim.scaleup);
            toggle.startAnimation(animation);
            animrunning = true;

            final ImageView ivRay = (ImageView) findViewById(R.id.imageViewRay);
            ivRay.setVisibility(View.VISIBLE);
            final Animation ivAnim = AnimationUtils.loadAnimation(MainActivity.this,
                    R.anim.rotate_around_center_point);
            ivRay.startAnimation(ivAnim);

        }

    }

    private void stopTimer(boolean stopanim) {
        tts.stop();
        timerHandler.removeCallbacks(timerRunnable);
        if (stopanim) {
            final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButtonStartStop);
            final Animation animation = AnimationUtils.loadAnimation(MainActivity.this,
                    R.anim.scaledn);
            if (toggle.isChecked()) {
                toggle.startAnimation(animation);
                toggle.setChecked(false);
            }
            final ImageView ivRay = (ImageView) findViewById(R.id.imageViewRay);
            ivRay.setVisibility(View.INVISIBLE);
            ivRay.clearAnimation();

        }
    }

    private void speakInstruction(){
/*      //useful for checking if uttered on right time
        long millis = System.currentTimeMillis() - startTime;
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;

        String str = String.format("%02d:%02d, g=%d, c=%d", minutes, seconds, gnum, cnum);
        str = str + "\n" + allTaskList.get(gnum).getTask() + ":: " + child.getDescription();
        Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();
*/
        ChildInfo child = allTaskList.get(gnum).getDetailsList().get(cnum);
        doneSpeaking = !child.getEnabled();
        if (child.getEnabled()) {
            tts.speak(child.getDescription(), TextToSpeech.QUEUE_FLUSH, null,
                    TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        }
    }

    private long getNextDelay() {
        ChildInfo child = allTaskList.get(gnum).getDetailsList().get(cnum);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date mDate = null;
        Date mDate0 = null;
        try {
            mDate = sdf.parse("00:" + child.getDelay().trim());
            mDate0 = sdf.parse("00:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        float next = (float)(mDate.getTime() - mDate0.getTime())/sessionSpeed;
        //long kk = (long)next;
        return (long) next;
    }

    private void expandAll() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++){
            simpleExpandableListView.expandGroup(i);
        }
    }

    private void collapseAll() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++){
            simpleExpandableListView.collapseGroup(i);
        }
    }

    private void initData() {

        String[] defaultTasks = getResources().getStringArray(R.array.defaultTasksStringArray);
        String[] defTimes = getResources().getStringArray(R.array.defaultTimeArray);

        int[] idArray = {R.array.defaultDetStringArray0, R.array.defaultDetStringArray1,
                R.array.defaultDetStringArray2, R.array.defaultDetStringArray3,
                R.array.defaultDetStringArray4, R.array.defaultDetStringArray5,
                R.array.defaultDetStringArray6, R.array.defaultDetStringArray7,
                R.array.defaultDetStringArray8, R.array.defaultDetStringArray9};

        ArrayList<String[]> defDetailsAll = new ArrayList<>();
        for (int id = 0; id < idArray.length; id++) {
            defDetailsAll.add(getResources().getStringArray(idArray[id]));
        }

        int ndelay = 0;

        for (int ntask = 0; ntask < defaultTasks.length; ntask++) {
            if (ntask < defDetailsAll.size()) {
                for (int ntaskdet = 0; ntaskdet < defDetailsAll.get(ntask).length; ntaskdet++) {
                    addTasktoExpList(defaultTasks[ntask], defDetailsAll.get(ntask)[ntaskdet],
                            defTimes[ndelay], ntask, -1);
                    ndelay++;
                }
            }
        }

        try {
            db.resetDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (int ntask = 0; ntask < allTaskList.size(); ntask++) {
            db.addData(allTaskList.get(ntask));
        }
        Toast toast = Toast.makeText(this,getString(R.string.initmsg), Toast.LENGTH_SHORT );
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    private boolean loadDb(){
        if (db.getVer() != CURRENT_DB_VER) return false;
        int tcount = db.getTaskCount();
        if (tcount<1) return false;
        List<GroupInfo> allTasks = db.getAllTasks();
        allTaskList.addAll(allTasks);
        for(GroupInfo task: allTaskList) detailsMap.put(task.getTask(), task);

        Toast toast = Toast.makeText(this,getString(R.string.dbmsg), Toast.LENGTH_SHORT );
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();

        return true;
    }

    private void addTasktoExpList(String taskName, String taskDetail, String delay, int at1, int at2){
        //check the hash map if the group already exists
        GroupInfo task = detailsMap.get(taskName); //todo - get from db, remove the map
        //add the group if doesn't exists
        if(task == null){
            task = new GroupInfo();
            task.setTask(taskName, at1);
            detailsMap.put(taskName, task);
            if (at1>=0) allTaskList.add(at1, task);
            else allTaskList.add(task);
            for (GroupInfo t: allTaskList){
                t.setTaskId(allTaskList.indexOf(t));
            }
        }

        //get the children for the group
        ArrayList<ChildInfo> detailsList = task.getDetailsList();

        //create a new child and add that to the group
        ChildInfo detailInfo = new ChildInfo();
        detailInfo.setDescription(taskDetail);
        detailInfo.setDelay(delay);
        if (at2>=0) detailsList.add(at2, detailInfo);
        else detailsList.add(detailInfo);
        task.setDetailsList(detailsList);
    }

    public boolean checkTime(String tstr){
        StringTokenizer st = new StringTokenizer(tstr, ":");
        if( (tstr.length()<1) || (tstr.length()>8) ||
                (!tstr.contains(":")) || (st.countTokens()>2)) return false;
        return true;
    }

    private void setStatus(int gp, int cp){
        for(ChildInfo ch : allTaskList.get(gp).getDetailsList()){
            ch.hasError = false;
            ch.isNew = false;
            if (ch.getDelay().equals("00:00")) ch.hasError = true;
            if (ch.getDescription().equals("No Action")) ch.hasError = true;
        }

        if (cp<0) cp = allTaskList.get(gp).getDetailsList().size() - 1;
        boolean enabled = allTaskList.get(gp).getDetailsList().get(cp).getEnabled();
        allTaskList.get(gp).getDetailsList().get(cp).isNew = enabled;
    }

    private void editGroupDialog(final int groupPos){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(params);

        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(40,10,40,10);
        layout.setBackgroundColor(ContextCompat.getColor(getBaseContext(),
                R.color.colorDialogLayout));

        final TextView tv = new TextView(this);
        tv.setText(getString(R.string.editGroupDialogTitle));
        tv.setPadding(40, 40, 40, 40);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);

        final CheckBox chNew = new CheckBox(this);
        chNew.setText(R.string.checkboxAdd);
        layout.addView(chNew);

        final EditText etTaskName = new EditText(this);
        etTaskName.setHint(R.string.hintTaskName);
        etTaskName.setText(allTaskList.get(groupPos).getTask());
        layout.addView(etTaskName);

        final EditText etNewStep = new EditText(this);
        etNewStep.setHint(getString(R.string.hintNewStep));
        layout.addView(etNewStep);

        final EditText etTime = new EditText(this);
        etTime.setHint(R.string.hintTime);
        etTime.setInputType(InputType.TYPE_DATETIME_VARIATION_TIME | InputType.TYPE_CLASS_DATETIME);
        layout.addView(etTime);

        final RadioButton rb1 = new RadioButton(this);
        rb1.setText("As is");
        rb1.setChecked(true);
        final RadioButton rb2 = new RadioButton(this);
        rb2.setText("UnMute");
        rb2.setChecked(false);
        final RadioButton rb3 = new RadioButton(this);
        rb3.setText("Mute");
        rb3.setChecked(false);
        final RadioButton rb4 = new RadioButton(this);
        rb4.setText("Unmute Everything");
        rb4.setChecked(false);

        final RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.addView(rb1);
        radioGroup.addView(rb2);
        radioGroup.addView(rb3);
        radioGroup.addView(rb4);
        radioGroup.setOrientation(RadioGroup.HORIZONTAL);
        //radioGroup.setPadding(60,10,10,10);
        radioGroup.setGravity(Gravity.CENTER);
        radioGroup.check(rb1.getId());
        layout.addView(radioGroup);

        chNew.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etTaskName.setText(null);
                } else {
                    etTaskName.setText(allTaskList.get(groupPos).getTask());
                }
            }
        });

        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setCustomTitle(tv);

        alertDialogBuilder.setNegativeButton(R.string.editCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        alertDialogBuilder.setPositiveButton(R.string.editOk, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                String etStr1 = etTaskName.getText().toString();
                String etStr2 = etNewStep.getText().toString();
                String etStr3 = etTime.getText().toString();
                boolean newStepAdded = false;
                String oldName = allTaskList.get(groupPos).getTask();

                if (!checkTime(etStr3)) etStr3 = "00:00";
                if (etStr2.length() < 1) etStr2 = "No Action";

                if (etStr1.length() > 0) {
                    if (chNew.isChecked()) {
                        addTasktoExpList(etStr1, etStr2, etStr3, groupPos, -1);
                        db.insertData(groupPos, allTaskList.get(groupPos));
                    } else {
                        if (allTaskList.get(groupPos).getTask().equals(etStr1) &&
                                (!etStr2.equals("No Action"))){     //new step was added
                            addTasktoExpList(etStr1, etStr2, etStr3, groupPos, -1);
                            newStepAdded = true;
                            db.insertStep(allTaskList.get(groupPos),
                                    allTaskList.get(groupPos).getDetailsList().size() - 1);
                        }
                        else {//only name changed
                            detailsMap.put(etStr1,
                                    detailsMap.remove(allTaskList.get(groupPos).getTask()));
                            allTaskList.get(groupPos).setTask(etStr1, groupPos);
                            db.updateTask(allTaskList.get(groupPos));
                        }
                        listAdapter.notifyDataSetChanged();
                    }

                    int res = radioGroup.getCheckedRadioButtonId();
                    if (res != rb1.getId()) {
                        setGroupMute(groupPos, res == rb2.getId(), res == rb3.getId(), res == rb4.getId());
                    }

                    listAdapter.notifyDataSetChanged();
                }
                if (newStepAdded)setStatus(groupPos, -1);
            }
        });

        alertDialogBuilder.setNeutralButton(R.string.editRemove, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                detailsMap.remove(allTaskList.get(groupPos).getTask());
                db.deleteTask(groupPos, allTaskList.get(groupPos));
                allTaskList.remove(groupPos);
                for (int s=0; s<allTaskList.size();s++) {
                    allTaskList.get(s).setTaskId(s);
                }

                if (allTaskList.size()<1) {
                    addTasktoExpList("No Name", "No Action", "00:00", 0, -1);
                    db.insertData(0, allTaskList.get(0));
                }
                listAdapter.notifyDataSetChanged();
            }
        });

        AlertDialog edTaskDialog = alertDialogBuilder.create();
        edTaskDialog.getWindow().setBackgroundDrawableResource(R.color.colorDialog);
        try {
            edTaskDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopTimer(true);
    }

    private void setGroupMute(int groupPos, boolean a1, boolean a2, boolean a3) {
        int count = a3?allTaskList.size():groupPos+1;
        int from = a3?0:groupPos;
        for (int i=from; i<count; i++) {
            for (ChildInfo child : allTaskList.get(i).getDetailsList()) {
                child.setEnabled((a1 && (!a2)) || a3);
                child.isNew = false;
                child.hasError = false;
                db.updateStep(allTaskList.get(i), child.getSequence());
            }
        }
    }

    private void editChildDialog(final int groupPos, final int childPos){
        final ChildInfo child = allTaskList.get(groupPos).getDetailsList().get(childPos);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(params);

        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(40,10,40,10);
        layout.setBackgroundColor(ContextCompat.getColor(getBaseContext(),
                R.color.colorDialogLayout));

        final TextView tv = new TextView(this);
        tv.setText(getString(R.string.editDetail));
        tv.setPadding(40, 40, 40, 40);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);

        final CheckBox chNew = new CheckBox(this);
        chNew.setText(R.string.checkboxAdd);
        layout.addView(chNew);

        final EditText etGroupTitle = new EditText(this);
        etGroupTitle.setEnabled(false);
        etGroupTitle.setText(allTaskList.get(groupPos).getTask());
        layout.addView(etGroupTitle);

        final EditText etDetail = new EditText(this);
        etDetail.setHint(getString(R.string.hintNewStep));
        etDetail.setText(allTaskList.get(groupPos).getDetailsList().get(childPos).getDescription());
        layout.addView(etDetail);

        final EditText etTime = new EditText(this);
        etTime.setHint(R.string.hintTime);
        etTime.setInputType(InputType.TYPE_DATETIME_VARIATION_TIME | InputType.TYPE_CLASS_DATETIME);
        etTime.setText(allTaskList.get(groupPos).getDetailsList().get(childPos).getDelay());
        layout.addView(etTime);

        final CheckBox chMute = new CheckBox(this);
        chMute.setText(R.string.checkboxMute);
        chMute.setChecked(!child.getEnabled());
        layout.addView(chMute);


        chNew.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etDetail.setText(null);
                    etTime.setText(null);
                } else {
                    etDetail.setText(child.getDescription());
                    etTime.setText(child.getDelay());
                }
            }
        });

        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setCustomTitle(tv);


        alertDialogBuilder.setNegativeButton(R.string.editCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        alertDialogBuilder.setPositiveButton(R.string.editOk, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {


                String etStr1 = etGroupTitle.getText().toString();
                String etStr2 = etDetail.getText().toString();
                String etStr3 = etTime.getText().toString();

                if (!checkTime(etStr3)) etStr3 = "00:00";
                if (etStr2.length() < 1) etStr2 = "No Action";

                if (chNew.isChecked()) {
                    addTasktoExpList(etStr1, etStr2, etStr3, groupPos, childPos);
                    db.insertStep(allTaskList.get(groupPos), childPos);
                } else {
                    child.setDescription(etStr2);
                    child.setDelay(etStr3);
                    child.setEnabled(!chMute.isChecked());
                    db.updateStep(allTaskList.get(groupPos), childPos);
                }
                listAdapter.notifyDataSetChanged();
                setStatus(groupPos, childPos);
            }
        });

        alertDialogBuilder.setNeutralButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.deleteStep(allTaskList.get(groupPos), childPos);
                allTaskList.get(groupPos).getDetailsList().remove(childPos);

                allTaskList.get(groupPos).reSequence();
                if (allTaskList.get(groupPos).getDetailsList().size()<1) {
                    addTasktoExpList(etGroupTitle.getText().toString(),
                            "No Action", "00:00", groupPos, -1);
                    db.insertStep(allTaskList.get(groupPos), 0);
                }
                listAdapter.notifyDataSetChanged();
            }
        });

        AlertDialog edStepDialog = alertDialogBuilder.create();
        edStepDialog.getWindow().setBackgroundDrawableResource(R.color.colorDialog);

        try {
            edStepDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopTimer(true);
    }
    protected void onPause()
    {
        //stopTimer(false);
        //tts.shutdown();
        super.onPause();
    }

    private ShareActionProvider mShareActionProvider;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.infomenu, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider)  MenuItemCompat.getActionProvider(item);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=in.oormi.apguide");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this app!");
        setShareIntent(shareIntent);
        return true;
    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info:
                stopTimer(true);
                final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButtonStartStop);
                toggle.setChecked(false);

                Intent i = new Intent(this, ResourceShow.class);
                startActivity(i);
                break;

            case R.id.savePreset:
                String suf = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
                createExFile("text/plain", "AP_Preset_" + suf + ".xml");
                break;

            case R.id.loadPreset:
                getImFile();
                break;
        }
        return true;
    }

    public void launchJournal() {
        stopTimer(true);
        final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButtonStartStop);
        toggle.setChecked(false);
        Intent i = new Intent(this, JournalActivity.class);
        startActivity(i);
    }

    public void backupDB(){ //may not work
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//in.oormi.apguide//databases//tasksManager";
                String backupDBPath = "tasksManager";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getBaseContext(), backupDB.toString() + " backed up.", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "Backup DB Failed. " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    private void createExFile(String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested MIME type.
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    private void getImFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("xmlHelperTAG", "Uri Read: " + uri.toString());
                readXml(uri);
            }
        }

        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("xmlHelperTAG", "Uri Write: " + uri.toString());
                writeXml(uri);
            }
        }
    }

    public void writeXml(Uri uri) {
        OutputStream outs = null;
        try {
            outs = getContentResolver().openOutputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        XmlSerializer serializer = Xml.newSerializer();

        try {
            serializer.setOutput(outs, "UTF-8");
            serializer.startDocument(null, true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            serializer.startTag(null, "tasklist");
            serializer.attribute(null, "ver", APG_PRESET_VERSION);

            for (int n=0;n<allTaskList.size();n++) {
                serializer.startTag(null, "task");
                GroupInfo g = allTaskList.get(n);
                serializer.attribute(null, "task", g.getTask());
                serializer.attribute(null, "taskid", String.valueOf(g.getId()));

                for (int m=0;m<g.getDetailsList().size();m++) {
                    serializer.startTag(null, "step");
                    ChildInfo c = g.getDetailsList().get(m);
                    serializer.attribute(null, "seq", String.valueOf(c.getSequence()));
                    serializer.attribute(null, "enabled", String.valueOf(c.getEnabled()));
                    serializer.attribute(null, "delay", c.getDelay());
                    serializer.attribute(null, "desc", c.getDescription());
                    serializer.endTag(null, "step");
                }

                serializer.endTag(null, "task");
            }

            serializer.endTag(null, "tasklist");
            serializer.endDocument();
            serializer.flush();
            if (outs != null) {
                outs.close();
            }

        } catch (Exception e) {
            Log.e("Exception", "Exception occurred in writing");
        }
    }


    public void readXml(Uri uri) {
        InputStream inputStream = null;
        String task = "Task";
        String taskid = "0";
        String seq = "0";
        String delay = "00:00";
        String enabled = "true";
        String desc = "Step";

        try {
            inputStream = getContentResolver().openInputStream(uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();

            parser.setInput(inputStream, null);

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                String tagname = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagname.equalsIgnoreCase("tasklist")) {
                            if (parser.getAttributeCount() > 0) {
                                String ver = parser.getAttributeValue(null, "ver");
                                if (ver.equals("1")) {
                                    allTaskList.clear();
                                    detailsMap.clear();
                                } else {
                                    Toast.makeText(this, "Preset version mismatch",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }

                        }
                        if (tagname.equalsIgnoreCase("task")) {
                            if (parser.getAttributeCount() > 1) {
                                task = parser.getAttributeValue(null, "task");
                                taskid = parser.getAttributeValue(null, "taskid");
                            }
                        }

                        if (tagname.equalsIgnoreCase("step")) {
                            if (parser.getAttributeCount() > 3) {
                                seq = parser.getAttributeValue(null, "seq");
                                delay = parser.getAttributeValue(null, "delay");
                                enabled = parser.getAttributeValue(null, "enabled");
                                desc = parser.getAttributeValue(null, "desc");
                            }
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        if (tagname.equalsIgnoreCase("step")) {
                            addtoList(task, desc, delay, seq, enabled);
                        }
                        break;

                    default:
                        break;
                }
                try {
                    eventType = parser.next();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        listAdapter.notifyDataSetChanged();

    }

    private void addtoList(String task, String desc, String delay, String seq, String enabled) {
        addTasktoExpList(task, desc, delay, -1, -1);
        GroupInfo g = allTaskList.get(allTaskList.size()-1);
        ChildInfo c = g.getDetailsList().get(g.getDetailsList().size()-1);
        c.setEnabled(Boolean.parseBoolean(enabled));
        c.setSequence(Integer.parseInt(seq));
    }

}
