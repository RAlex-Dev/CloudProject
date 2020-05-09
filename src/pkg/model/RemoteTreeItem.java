package pkg.model;

import javafx.scene.control.TreeItem;

public class RemoteTreeItem extends TreeItem<String> {

    private String fileName;
    private String fileId;

    public RemoteTreeItem(String fileName, String fileId) {
        this.fileName = fileName;
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileId() {
        return fileId;
    }


}
