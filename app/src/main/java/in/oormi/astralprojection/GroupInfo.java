package in.oormi.astralprojection;
import java.util.ArrayList;

public class GroupInfo {

    private String task;
    private ArrayList<ChildInfo> detailsList = new ArrayList<ChildInfo>();

    public String getTask() {
        return task;
    }

    public void setTask(String name) {
        this.task = name;
    }

    public ArrayList<ChildInfo> getDetailsList() {
        return detailsList;
    }

    public void setDetailsList(ArrayList<ChildInfo> detailsList) {
        this.detailsList = detailsList;
        reSequence();
    }

    public void reSequence(){
        for (ChildInfo child : detailsList){
            child.setSequence(String.format("%02d", 1 + detailsList.indexOf(child)));
        }
    }

}