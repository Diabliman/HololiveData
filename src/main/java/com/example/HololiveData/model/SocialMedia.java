package com.example.HololiveData.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class SocialMedia {
    public String url;
    public long nbSubs;
}
