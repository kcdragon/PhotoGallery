package com.bignerdranch.android.photogallery;

import android.os.*;
import android.support.v4.app.*;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return new PhotoGalleryFragment();
    }
}
