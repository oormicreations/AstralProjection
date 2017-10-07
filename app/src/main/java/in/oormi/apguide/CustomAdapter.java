package in.oormi.apguide;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
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

        //if (view == null) { //do not reuse views, it messes up colored backgrounds
            LayoutInflater infalInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.child_items, null);
        //}

        TextView sequence = (TextView) view.findViewById(R.id.sequence);
        sequence.setText(String.format("%02d. ", 1 + detailInfo.getSequence()));

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
        cm.setTextColor(Color.rgb(0,127,0));

        TextView childItem = (TextView) view.findViewById(R.id.childItem);
        childItem.setText(detailInfo.getDescription().trim());

       if (detailInfo.hasError) {
            ObjectAnimator colorFade = ObjectAnimator.ofObject(view, "backgroundColor",
                    new ArgbEvaluator(), Color.argb(255, 255, 255, 255), 0xffffeecc);
            colorFade.setDuration(2000);
            colorFade.start();
        }
        else {
            if (detailInfo.isNew) {

                ObjectAnimator colorFade1 = ObjectAnimator.ofObject(view, "backgroundColor",
                        new ArgbEvaluator(), Color.argb(255, 255, 255, 255), 0xFFFDEBFF);
                colorFade1.setDuration(2000);
                colorFade1.start();

            }
        }
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
        //if (view == null) {
            LayoutInflater inf =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inf.inflate(R.layout.group_items, null);
        //}

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