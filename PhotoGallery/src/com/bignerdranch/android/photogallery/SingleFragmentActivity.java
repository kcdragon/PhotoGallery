package com.bignerdranch.android.photogallery;

import android.os.*;
import android.support.v4.app.*;

public abstract class SingleFragmentActivity extends FragmentActivity {
    protected abstract Fragment createFragment();

    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

	final FragmentManager fm = getSupportFragmentManager();
	Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

	if (fragment == null) {
	    fragment = createFragment();
	    fm.beginTransaction()
		.add(R.id.fragmentContainer, fragment)
		.commit();
	}
    }
}
