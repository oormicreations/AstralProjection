package in.oormi.astralprojection;
import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Chronometer;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CustomAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<GroupInfo> taskList;

    public CustomAdapter(Context context, ArrayList<GroupInfo> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<ChildInfo> productList = taskList.get(groupPosition).getDetailsList();
        return productList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View view, ViewGroup parent) {

        ChildInfo detailInfo = (ChildInfo) getChild(groupPosition, childPosition);
        if (view == null) {
            LayoutInflater infalInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.child_items, null);
        }

        TextView sequence = (TextView) view.findViewById(R.id.sequence);
        sequence.setText(detailInfo.getSequence().trim() + ". ");

        Chronometer cm = (Chronometer) view.findViewById(R.id.chronometer2);
        //cm.setFormat("Time (%s)");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date mDate = null;
        Date mDate0 = null;
        try {
            mDate = sdf.parse("00:" + detailInfo.getDelay().trim());
            mDate0 = sdf.parse("00:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long timeInMilliseconds = mDate.getTime() - mDate0.getTime();
        cm.setBase(SystemClock.elapsedRealtime() - timeInMilliseconds);

        //int min = 0;
        //int sec = Integer.decode(detailInfo.getDelay().trim());
        //cm.setBase(SystemClock.elapsedRealtime() - (min * 60000 + sec * 1000));
        //cm.start();

        TextView childItem = (TextView) view.findViewById(R.id.childItem);
        childItem.setText(detailInfo.getDescription().trim());

        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        ArrayList<ChildInfo> productList = taskList.get(groupPosition).getDetailsList();
        return productList.size();

    }

    @Override
    public Object getGroup(int groupPosition) {
        return taskList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return taskList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isLastChild, View view,
                             ViewGroup parent) {

        GroupInfo headerInfo = (GroupInfo) getGroup(groupPosition);
        if (view == null) {
            LayoutInflater inf =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inf.inflate(R.layout.group_items, null);
        }

        TextView heading = (TextView) view.findViewById(R.id.heading);
        heading.setText(headerInfo.getTask().trim());

        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}