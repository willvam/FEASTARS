package com.example.feastarfeed;

public class Video {
    private final String videoUrl;
    private final String title;
    private final String address;
    private final String date;
    private final String price ;
    private final Long id;
    private final String uploader;
    private final String profileImageUrl;
    private final String videoPic;


    public Video(String videoUrl, String title, String address, String date, String price, long id, String uploader, String profileImageUrl, String videoPic) {

        if (videoUrl == null) {
            throw new IllegalArgumentException("VideoUrl cannot be null");
        }
        if (title == null) {
            throw new IllegalArgumentException("title cannot be null");
        }
        if (address == null) {
            throw new IllegalArgumentException("address cannot be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("date cannot be null");
        }
        if (price == null) {
            throw new IllegalArgumentException("price cannot be null");
        }
        if (uploader == null) {
            throw new IllegalArgumentException("uploader cannot be null");
        }
        if (profileImageUrl == null) {
            throw new IllegalArgumentException("profileImageUrl cannot be null");
        }

        this.videoUrl = videoUrl;
        this.title = title;
        this.address = address;
        this.price = price;
        this.date = date;
        this.id = id;
        this.uploader = uploader;
        this.profileImageUrl = profileImageUrl;
        this.videoPic = videoPic;

    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getAddress() {
        return address;
    }

    public String getDate() { return date; }

    public String getPrice() { return price; }

    public Long getId() { return id; }

    public String getUploader(){return uploader;}
    public String getprofileImageUrl(){return profileImageUrl;}

    public String getVideoPic() {return videoPic;}


}
