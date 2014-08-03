package com.bignerdranch.android.photogallery.model;

import java.util.ArrayList;

import lombok.*;

public class GalleryItemCollection {

    @Getter @Setter private ArrayList<GalleryItem> items;
    @Getter @Setter private int total;

    public GalleryItemCollection() {
        this.items = new ArrayList<GalleryItem>();
        this.total = total;
    }
}
