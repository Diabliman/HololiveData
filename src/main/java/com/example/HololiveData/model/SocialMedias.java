package com.example.HololiveData.model;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SocialMedias {
    private SocialMedia yt;
    private SocialMedia twitter;
}
