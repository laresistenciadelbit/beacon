package com.example.android.lighthouse;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**A dummy fragment representing a section of the app, but that simply displays dummy text.
 * This would be replaced with your application's content.*/
public class DummySectionFragment extends Fragment
{
    public static final String ARG_SECTION_NUMBER = "section_number";
    public DummySectionFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView;
        switch(getArguments().getInt(ARG_SECTION_NUMBER))
        {
            case 1:
                rootView = inflater.inflate(R.layout.layout_1,null);
                break;
            case 2:
                rootView = inflater.inflate(R.layout.layout_2,null);
                break;
            case 3:
                rootView = inflater.inflate(R.layout.layout_3,null);
                break;
            case 4:
                rootView = inflater.inflate(R.layout.layout_4,null);
                break;
            default:
                rootView = inflater.inflate(R.layout.layout_1,null);
        }
        //View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
        //TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
        //dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
        return rootView;
    }
}
