package com.dg.checkbills;

import android.app.Fragment;

/**
 * Created by Remy on 03/11/2016.
 */
public abstract class FragmentHistorique extends Fragment
{
    protected String title;

    public String getTitle()
    {
        return this.title;
    }

    public FragmentHistorique()
    {
        super();
    }

}
