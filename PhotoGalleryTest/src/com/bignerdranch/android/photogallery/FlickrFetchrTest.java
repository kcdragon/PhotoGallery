package com.bignerdranch.android.photogallery;

import java.util.ArrayList;

import static org.junit.Assert.*;
import org.junit.*;

import com.bignerdranch.android.photogallery.model.*;

public class FlickrFetchrTest {

    @Test
    public void testFetchItems() {
        ArrayList<GalleryItem> items = new FlickrFetchr().fetchItems();
        assertEquals(100, items.size());
    }
}
