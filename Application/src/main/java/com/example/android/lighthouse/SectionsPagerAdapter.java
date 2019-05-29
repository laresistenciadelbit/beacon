package com.example.android.lighthouse;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;


public class SectionsPagerAdapter extends FragmentPagerAdapter
{

    final int tabsNumber=4;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a DummySectionFragment (defined as a static inner class below) with the page number as its lone argument.
        Fragment fragment = new DummySectionFragment();
        Bundle args = new Bundle();
        args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() { return tabsNumber; } // Show "tabsNumber" pages

    public int getIcon(int position)
    {   //mi funcion para devolver icono
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return R.drawable.menu_events;
            case 1:
                return R.drawable.menu_news;
            case 2:
                return R.drawable.menu_map;
            default:
                return R.drawable.menu_social;
        }
    }

    /**Get title for each of the pages. This will be displayed on each of the tabs.
     @Override
     public CharSequence getPageTitle(int position) {
     Locale l = Locale.getDefault();
     switch (position) {
     case 0:
     return getString(R.string.title_section1).toUpperCase(l);
     case 1:
     return getString(R.string.title_section2).toUpperCase(l);
     case 2:
     return getString(R.string.title_section3).toUpperCase(l);
     }
     return null;
     }
     */
}