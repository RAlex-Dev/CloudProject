package pkg.model;

public class ScheduleItem {

  //  private final SimpleStringProperty itemPath = new SimpleStringProperty("");

    private String path;

    public ScheduleItem(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
