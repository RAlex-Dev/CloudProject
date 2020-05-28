package pkg.model;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;


public class FilePathTreeItem extends TreeItem<String> {

    private String fullPath;
    private boolean isDirectory;
    private String fileName;
    private String fileId;
    private ContextMenu contextMenu = new ContextMenu();
    private String accessLevel;

    // public static Image image = new Image("CloudProject/pkg/model/user.png");
    //  ImageView imageView = new ImageView(image);

    public FilePathTreeItem(Path file) {
        // check call to super class
        super(file.toString());
        this.fullPath = file.toString();
        this.setAccessLevel("BRONZE"); // default access level

        if (Files.isDirectory(file)) {
            this.isDirectory = true;
            //    this.setGraphic(imageView);
            // set the graphic a closed folder image
        } else {
            this.isDirectory = false;
            //   this.setGraphic(imageView);
        }

        if (!fullPath.endsWith(File.separator)) {

            String value = file.toString();
            int indexOf = value.lastIndexOf(File.separator);
            if (indexOf > 0) {
                this.setValue(value.substring(indexOf + 1));
            } else {
                this.setValue(value);
            }
        }

        this.addEventHandler(TreeItem.branchExpandedEvent(), new EventHandler() {
            @Override
            public void handle(javafx.event.Event event) {
                FilePathTreeItem source = (FilePathTreeItem) event.getSource();
                if (source.isDirectory() && source.isExpanded()) {
                    // set Image
                }
                try {
                    if (source.getChildren().isEmpty()) {
                        Path path = Paths.get(source.getFullPath());
                        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                        if (attributes.isDirectory()) {
                            DirectoryStream<Path> dir = Files.newDirectoryStream(path);
                            for (Path file : dir) {
                                FilePathTreeItem treeItem = new FilePathTreeItem(file);
                                source.getChildren().add(treeItem);
                            }
                        }
                    } else {
                        // directory can be rescanned for changes here

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        this.addEventHandler(TreeItem.branchCollapsedEvent(), new EventHandler() {
            @Override
            public void handle(Event event) {
                FilePathTreeItem source = (FilePathTreeItem) event.getSource();
                if (source.isDirectory() && !source.isExpanded()) {
                    // set image of collapsed folder
                }
            }
        });
    }

    public FilePathTreeItem(String fileName, String fileId, Path file) {
        // check call to super class
        super(file.toString());
        this.fullPath = file.toString();
        this.fileName = fileName;
        this.fileId = fileId;
        this.setAccessLevel("BRONZE");

        // USE LATER FOR DIR UPLOAD

        /*
        if (Files.isDirectory(file)) {
            this.isDirectory = true;
            //     this.setGraphic(imageView);
            // set the graphic a closed folder image
        } else {
            this.isDirectory = false;
            // set the graphic for a file image
        }
        */

        if (!fullPath.endsWith(File.separator)) {

            String value = file.toString();
            int indexOf = value.lastIndexOf(File.separator);
            if (indexOf > 0) {
                this.setValue(value.substring(indexOf + 1));
            } else {
                this.setValue(this.fileName);
            }
        }

        this.addEventHandler(TreeItem.branchExpandedEvent(), new EventHandler() {
            @Override
            public void handle(javafx.event.Event event) {
                FilePathTreeItem source = (FilePathTreeItem) event.getSource();
                if (source.isDirectory() && source.isExpanded()) {
                    // set Image
                }
            }
        });

        this.addEventHandler(TreeItem.branchCollapsedEvent(), new EventHandler() {
            @Override
            public void handle(Event event) {
                FilePathTreeItem source = (FilePathTreeItem) event.getSource();
                if (source.isDirectory() && !source.isExpanded()) {
                    // set image of collapsed folder
                }
            }
        });
    }

    public String getFileId() {
        return fileId;
    }

    @Override
    public boolean isLeaf() {
        return !isDirectory;
    }

    public String getFullPath() {
        return this.fullPath;
    }

    public boolean isDirectory() {
        return this.isDirectory;
    }

    public String getFileName() {
        return fileName;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }
}


