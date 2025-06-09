package com.nnthienphuc.bookdownloaderapp.models;

import java.io.Serializable;


public class Book implements Serializable {
    private String id;
    private String title;
    private String author;
    private String genre;
    private String description;
    private String uploader;
    private String uploaderUid;     // <== NEW
    private int pageCount;
    private long size;
    private String thumbnailUrl;
    private String fileUrl;
    private String localPdfPath;    // <== NEW
    private boolean isDeleted;

    public Book() {} // Required for Firestore

    public Book(String id, String title, String author, String genre, String description,
                String uploader, String uploaderUid, int pageCount, long size,
                String thumbnailUrl, String fileUrl, String localPdfPath, boolean isDeleted) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.description = description;
        this.uploader = uploader;
        this.uploaderUid = uploaderUid;
        this.pageCount = pageCount;
        this.size = size;
        this.thumbnailUrl = thumbnailUrl;
        this.fileUrl = fileUrl;
        this.localPdfPath = localPdfPath;
        this.isDeleted = isDeleted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public String getUploaderUid() {
        return uploaderUid;
    }

    public void setUploaderUid(String uploaderUid) {
        this.uploaderUid = uploaderUid;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getLocalPdfPath() {
        return localPdfPath;
    }

    public void setLocalPdfPath(String localPdfPath) {
        this.localPdfPath = localPdfPath;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String toJson() {
        return new com.google.gson.Gson().toJson(this);
    }

    public static Book fromJson(String json) {
        return new com.google.gson.Gson().fromJson(json, Book.class);
    }

}
