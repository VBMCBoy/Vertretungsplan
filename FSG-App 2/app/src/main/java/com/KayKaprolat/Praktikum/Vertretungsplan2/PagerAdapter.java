package com.KayKaprolat.Praktikum.Vertretungsplan2;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {

  int mNumOfTabs;

  public PagerAdapter(FragmentManager fm, int NumOfTabs) {
    super(fm);
    mNumOfTabs = NumOfTabs;
  }

  @Override
  public Fragment getItem(int position) {
    switch (position) {
      case 0:
        FragmentHeute tab1 = new FragmentHeute();
        return tab1;
      case 1:
        FragmentMorgen tab2 = new FragmentMorgen();
        return tab2;
      default:
        return null;
    }
  }


  @Override
  public int getCount() {
    return mNumOfTabs;
  }
}
