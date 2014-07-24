package com.bignerdranch.android.photogallery.model;

import lombok.*;

public class GalleryItem {
    @Getter @Setter private String caption;
    @Getter @Setter private String id;
    @Getter @Setter private String url;

    public String toString() {
        return caption;
    }
}
