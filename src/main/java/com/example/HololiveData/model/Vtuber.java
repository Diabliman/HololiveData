package com.example.HololiveData.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class Vtuber {
    private String name;
    private String id;
    private long ytSubs;
    private String ytUrl;
    private long twitterSubs;
    private String twitterUrl;
    private Date startTime;
    private String generation;
    private String imageUrl;
    private String japName;
    private String eyeColor;
    private String hairColor;
    private String height;
}
