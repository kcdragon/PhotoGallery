package com.bignerdranch.android.photogallery;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.bignerdranch.android.photogallery.PhotoGalleryActivityTest \
 * com.bignerdranch.android.photogallery.tests/android.test.InstrumentationTestRunner
 */
public class PhotoGalleryActivityTest extends ActivityInstrumentationTestCase2<PhotoGalleryActivity> {

    public PhotoGalleryActivityTest() {
        super("com.bignerdranch.android.photogallery", PhotoGalleryActivity.class);
    }

}
