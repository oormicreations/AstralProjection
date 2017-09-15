package in.oormi.astralprojection;

public class ChildInfo {

    private int sequence = 0;
    private String description = "";
    private String delay = "";
    boolean hasError = false;
    boolean isNew = false;


    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        //hasError = false;
        //isNew = false;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

}