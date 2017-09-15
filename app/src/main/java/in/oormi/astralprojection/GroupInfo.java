package in.oormi.astralprojection;
import java.util.ArrayList;

public class GroupInfo {

    private String task;
    private int taskId;
    private ArrayList<ChildInfo> detailsList = new ArrayList<ChildInfo>();

    public String getTask() {
        return task;
    }

    public int getId() {
        return taskId;
    }

    public void setTaskId(int id) {
        this.taskId = id;
    }

    public void setTask(String name, int id) {
        this.task = name;
        this.taskId = id;
    }

    public ArrayList<ChildInfo> getDetailsList() {
        return detailsList;
    }

    public void setDetailsList(ArrayList<ChildInfo> detailsList) {
        this.detailsList = detailsList;
        reSequence();
    }

    public void reSequence(){
        for (int s=0; s<detailsList.size();s++){
            detailsList.get(s).setSequence(s);
            //child.setSequence(String.format("%02d", 1 + detailsList.indexOf(child)));
        }
    }

}