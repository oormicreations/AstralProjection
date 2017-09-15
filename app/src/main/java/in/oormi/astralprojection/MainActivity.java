package in.oormi.astralprojection;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    private LinkedHashMap<String, GroupInfo> detailsMap = new LinkedHashMap<String, GroupInfo>();
    private ArrayList<GroupInfo> allTaskList = new ArrayList<GroupInfo>();

    private CustomAdapter listAdapter;
    private ExpandableListView simpleExpandableListView;
    public DatabaseHandler db = new DatabaseHandler(this);
    public boolean saveFlag = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!loadDb()) {
            initData();
        }

        //get reference of the ExpandableListView
        simpleExpandableListView = (ExpandableListView) findViewById(R.id.listviewsession);
        // create the adapter by passing your ArrayList data
        listAdapter = new CustomAdapter(MainActivity.this, allTaskList);
        // attach the adapter to the expandable list view
        simpleExpandableListView.setAdapter(listAdapter);

        // setOnGroupClickListener listener for group heading click
        simpleExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return false;
            }
        });

        // setOnChildClickListener listener for child row click
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

        ImageButton resetButton = (ImageButton) findViewById(R.id.imageButtonReset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allTaskList.clear();
                detailsMap.clear();
                initData();
                listAdapter.notifyDataSetChanged();
            }
        });
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
        Toast.makeText(getBaseContext(), String.format("Init complete."), Toast.LENGTH_SHORT).show();
    }

    private boolean loadDb(){
        int tcount = db.getTaskCount();
        if (tcount<1) return false;
        List<GroupInfo> allTasks = db.getAllTasks();
        allTaskList.addAll(allTasks);
        for(GroupInfo task: allTaskList) detailsMap.put(task.getTask(), task);
        Toast.makeText(getBaseContext(), String.format("Db Task count = %d", tcount), Toast.LENGTH_SHORT).show();
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
        allTaskList.get(gp).getDetailsList().get(cp).isNew = true;
        saveFlag = true;
    }

    public void editGroupDialog(final int groupPos){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(params);

        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(10,10,10,10);

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
                        if (allTaskList.get(groupPos).getTask().equals(etStr1)){//new step was added
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

                    listAdapter.notifyDataSetChanged();
                }
                if (newStepAdded)setStatus(groupPos, -1);
            }
        });

        alertDialogBuilder.setNeutralButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                detailsMap.remove(allTaskList.get(groupPos).getTask());
                db.deleteTask(groupPos, allTaskList.get(groupPos));
                allTaskList.remove(groupPos);

                if (allTaskList.size()<1) {
                    addTasktoExpList("No Name", "No Action", "00:00", 0, -1);
                    db.insertData(0, allTaskList.get(0));
                }
                listAdapter.notifyDataSetChanged();
                saveFlag = true;
            }
        });

        AlertDialog edTaskDialog = alertDialogBuilder.create();
        try {
            edTaskDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void editChildDialog(final int groupPos, final int childPos){
        final ChildInfo child = allTaskList.get(groupPos).getDetailsList().get(childPos);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(params);

        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(10,10,10,10);

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
                saveFlag = true;
            }
        });

        AlertDialog edStepDialog = alertDialogBuilder.create();

        try {
            edStepDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
