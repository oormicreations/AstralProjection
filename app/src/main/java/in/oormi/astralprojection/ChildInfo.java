package in.oormi.astralprojection;

public class ChildInfo {

    private String sequence = "";
    private String description = "";
    private String delay = "";
    boolean hasError = false;
    boolean isNew = false;


    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
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