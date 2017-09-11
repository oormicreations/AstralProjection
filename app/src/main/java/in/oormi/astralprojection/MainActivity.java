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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    private LinkedHashMap<String, GroupInfo> detailsMap = new LinkedHashMap<String, GroupInfo>();
    private ArrayList<GroupInfo> allTaskList = new ArrayList<GroupInfo>();

    private CustomAdapter listAdapter;
    private ExpandableListView simpleExpandableListView;
    public DatabaseHandler db = new DatabaseHandler(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // add data for displaying in expandable list view
        initData();

        //get reference of the ExpandableListView
        simpleExpandableListView = (ExpandableListView) findViewById(R.id.listviewsession);
        // create the adapter by passing your ArrayList data
        listAdapter = new CustomAdapter(MainActivity.this, allTaskList);
        // attach the adapter to the expandable list view
        simpleExpandableListView.setAdapter(listAdapter);

        //expand all the Groups
        //expandAll();

        // setOnGroupClickListener listener for group heading click
        simpleExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                GroupInfo headerInfo = allTaskList.get(groupPosition);
                //display it or do something with it
                //Toast.makeText(getBaseContext(), " Header is :: " + headerInfo.getTask(),
                //Toast.LENGTH_LONG).show();
                //editDialog();
                return false;
            }
        });

        // setOnChildClickListener listener for child row click
        simpleExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                GroupInfo headerInfo = allTaskList.get(groupPosition);
                //get the child info
                ChildInfo detailInfo =  headerInfo.getDetailsList().get(childPosition);
                //display it or do something with it
                //Toast.makeText(getBaseContext(), " Clicked on :: " + headerInfo.getTask()
                        //+ "/" + detailInfo.getDescription(), Toast.LENGTH_LONG).show();
                return false;
            }
        });


/*
        simpleExpandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                View getElement1 = ((ViewGroup)view).getChildAt(0);
                //Toast.makeText(getBaseContext(),String.valueOf(position) + " id=" + String.valueOf(id),Toast.LENGTH_SHORT).show();
                Toast.makeText(getBaseContext(), simpleExpandableListView.getItemAtPosition(position).toString(), Toast.LENGTH_LONG).show();
                return false;
            }
        });
*/
        simpleExpandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int itemType = ExpandableListView.getPackedPositionType(id);

                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int childPosition = ExpandableListView.getPackedPositionChild(id);
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    editDialog(groupPosition, childPosition);
                    return true; //true if we consumed the click, false if not

                } else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    editDialog(groupPosition, -1);
                    return true; //true if we consumed the click, false if not

                } else {
                    // null item; we don't consume the click
                    return false;
                }
            }
        });
    }

    //method to expand all groups
    private void expandAll() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++){
            simpleExpandableListView.expandGroup(i);
        }
    }

    //method to collapse all groups
    private void collapseAll() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++){
            simpleExpandableListView.collapseGroup(i);
        }
    }

    private void initData(){

        String [] defaultTasks = getResources().getStringArray(R.array.defaultTasksStringArray);
        String [] defTimes = getResources().getStringArray(R.array.defaultTimeArray);

        int [] idArray = {  R.array.defaultDetStringArray0, R.array.defaultDetStringArray1,
                            R.array.defaultDetStringArray2, R.array.defaultDetStringArray3,
                            R.array.defaultDetStringArray4, R.array.defaultDetStringArray5,
                            R.array.defaultDetStringArray6, R.array.defaultDetStringArray7,
                            R.array.defaultDetStringArray8, R.array.defaultDetStringArray9};

        ArrayList<String[]> defDetailsAll = new ArrayList<>();
        for (int id = 0; id < idArray.length; id++) {
            defDetailsAll.add(getResources().getStringArray(idArray[id]));
        }

        int ndelay = 0;
        //populate explist before writing to db
        for (int ntask = 0; ntask < defaultTasks.length; ntask++){
            if (ntask < defDetailsAll.size()) {
                for (int ntaskdet = 0; ntaskdet < defDetailsAll.get(ntask).length; ntaskdet++) {
                    addTasktoExpList(defaultTasks[ntask], defDetailsAll.get(ntask)[ntaskdet],
                            defTimes[ndelay], -1, -1);
                    ndelay++;
                }
            }
        }

        //write to db
       // GroupInfo headerInfo = detailsMap.get(defaultTasks[0]);
        try {
            db.resetDB(); //if needed, remove after testing app
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //db.addData(headerInfo);
        for (int ntask = 0; ntask < detailsMap.size(); ntask++) {
            db.addData(detailsMap.get(defaultTasks[ntask]));
        }

        int tcount = db.getTaskCount();
        Toast.makeText(getBaseContext(), String.format("Task count = %d", tcount), Toast.LENGTH_SHORT).show();

        //test read
/*
        List<GroupInfo> allTasks = db.getAllTasks();
        for (GroupInfo t : allTasks) {
            String det = "none";
            ArrayList<ChildInfo> detailsList = t.getDetailsList();
            if(!detailsList.isEmpty())
            {
                for(int d=0; d<detailsList.size(); d++) {
                    det = detailsList.get(d).getDescription();
                    addTasktoExpList(defaultTasks[2], det, delay);
                }
            }
            String log = "Task: " + t.getTask() + " ,Details: " + det;
            Toast.makeText(getBaseContext(), log, Toast.LENGTH_LONG).show();
        }
*/

    }

    private void addTasktoExpList(String taskName, String taskDetail, String delay, int at1, int at2){

        //int groupPosition = 0;

        //check the hash map if the group already exists
        GroupInfo headerInfo = detailsMap.get(taskName);
        //add the group if doesn't exists
        if(headerInfo == null){
            headerInfo = new GroupInfo();
            headerInfo.setTask(taskName);
            detailsMap.put(taskName, headerInfo);
            if (at1>=0) allTaskList.add(at1, headerInfo);
            else  allTaskList.add(headerInfo);
        }

        //get the children for the group
        ArrayList<ChildInfo> detailsList = headerInfo.getDetailsList();
        //int listSize = detailsList.size();
        //add to the counter
        //listSize++;

        //create a new child and add that to the group
        ChildInfo detailInfo = new ChildInfo();
        //detailInfo.setSequence(String.format("%02d", listSize));
        detailInfo.setDescription(taskDetail);
        detailInfo.setDelay(delay);
        if (at2>=0) detailsList.add(at2, detailInfo);
        else detailsList.add(detailInfo);
        //for (ChildInfo d : detailsList) {
            //d.setSequence(String.format("%02d", 1 + detailsList.indexOf(d)));
        //}
        headerInfo.setDetailsList(detailsList);
        //headerInfo.reSequence();

        //find the group position inside the list
        //groupPosition = allTaskList.indexOf(headerInfo);
        //return groupPosition;
    }

    public void editDialog(final int groupPos, int childPos){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(params);

        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(10,10,10,10);

        String title = getString(R.string.editTitle);
        if (childPos>0) title = getString(R.string.editDetail);
        final TextView tv = new TextView(this);
        tv.setText(title);
        tv.setPadding(40, 40, 40, 40);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);

        final CheckBox chNew = new CheckBox(this);
        chNew.setText(R.string.checkboxAdd);
        layout.addView(chNew);

        final EditText etGroupTitle = new EditText(this);
        if (childPos>=0) etGroupTitle.setEnabled(false);
        etGroupTitle.setHint(R.string.hintTaskName);
        etGroupTitle.setText(allTaskList.get(groupPos).getTask());
        //etGroupTitle.setGravity(Gravity.CENTER);
        layout.addView(etGroupTitle);

        String hintChild = getString(R.string.hintDetail);
        if (childPos<0) hintChild = getString(R.string.hintNewStep);
        final EditText etDetail = new EditText(this);
        etDetail.setHint(hintChild);
        if (childPos>=0) {
            etDetail.setText(allTaskList.get(groupPos).getDetailsList().get(childPos).getDescription());
        }
        layout.addView(etDetail);

        final EditText etTime = new EditText(this);
        etTime.setHint(R.string.hintTime);
        etTime.setInputType(InputType.TYPE_DATETIME_VARIATION_TIME | InputType.TYPE_CLASS_DATETIME);
        if (childPos>=0) {
            etTime.setText(allTaskList.get(groupPos).getDetailsList().get(childPos).getDelay());
        }
        //etTime.setGravity(Gravity.CENTER);
        layout.addView(etTime);

/*
        etTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //if (s.length()==2) etTime.setText(etTime.getText() + ":");
                tv.setText(s);
            }
        });
*/

        final int fgroupPos = groupPos;
        final int fchildPos = childPos;

        chNew.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (fchildPos < 0) etGroupTitle.setText(null);
                    else {
                        etDetail.setText(null);
                        etTime.setText(null);
                    }
                } else {
                    if (fchildPos < 0) etGroupTitle.setText(allTaskList.get(fgroupPos).getTask());
                    else {
                        etDetail.setText(allTaskList.get(fgroupPos).getDetailsList().get(fchildPos).getDescription());
                        etTime.setText(allTaskList.get(fgroupPos).getDetailsList().get(fchildPos).getDelay());
                    }

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

                if (!checkTime(etStr3)) {
                    etStr3 = "00:00";
                    setStatus(true, 1, fgroupPos, fchildPos);
                    //allTaskList.get(fgroupPos).getDetailsList().get(fchildPos).hasError = true;
                } else setStatus(false, 1, fgroupPos, fchildPos); //allTaskList.get(fgroupPos).getDetailsList().get(fchildPos).hasError = false;

                if (etStr2.length() < 1) {
                    etStr2 = "No Action";
                    setStatus(true, 1, fgroupPos, fchildPos);
                }

                if (etStr1.length() > 0) {
                    if (chNew.isChecked()) {
                        addTasktoExpList(etStr1, etStr2, etStr3, fgroupPos, fchildPos);
                        if (etStr2.length() > 0) setStatus(true, 2, fgroupPos, fchildPos);
                        else setStatus(false, 2, fgroupPos, fchildPos);
                    } else {
                        detailsMap.put(etStr1, detailsMap.remove(allTaskList.get(fgroupPos).getTask()));
                        allTaskList.get(fgroupPos).setTask(etStr1);
                        listAdapter.notifyDataSetChanged();

                        if (fchildPos >= 0) {
                            allTaskList.get(fgroupPos).getDetailsList().get(fchildPos).setDescription(etStr2);
                            allTaskList.get(fgroupPos).getDetailsList().get(fchildPos).setDelay(etStr3);
                            setStatus(true, 2, fgroupPos, fchildPos); //allTaskList.get(fgroupPos).getDetailsList().get(fchildPos).isNew = true;
                        } else {
                            addTasktoExpList(etStr1, etStr2, etStr3, fgroupPos, fchildPos);
                            if (etStr3.equals("No Action")) setStatus(true, 1, fgroupPos, fchildPos + 1);
                        }
                    }

                    listAdapter.notifyDataSetChanged();
                }
            }
        });

        alertDialogBuilder.setNeutralButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (fchildPos < 0) {
                    detailsMap.remove(allTaskList.get(fgroupPos).getTask());
                    allTaskList.remove(fgroupPos);
                }
                else {
                    allTaskList.get(fgroupPos).getDetailsList().remove(fchildPos);
                    allTaskList.get(fgroupPos).reSequence();
                }
                listAdapter.notifyDataSetChanged();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkTime(String tstr){
        StringTokenizer st = new StringTokenizer(tstr, ":");
        if( (tstr.length()<1) || (tstr.length()>8) ||
                (!tstr.contains(":")) || (st.countTokens()>2)) return false;
        return true;
    }

    private void setStatus(boolean status, int stype, int gp, int cp){
        if (cp<0) return;
        if (stype==1) allTaskList.get(gp).getDetailsList().get(cp).hasError = status;
        if (stype==2) {
            allTaskList.get(gp).getDetailsList().get(cp).isNew = status &&
                    allTaskList.get(gp).getDetailsList().get(cp).hasError;
        }
    }

}
