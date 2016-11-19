package com.dg.checkbills;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;


public class PhotoFragment extends Fragment {
    private ImageView imgTicket;
    private Button scanButton;
    private FragmentActivity mListener;


    // Required empty public constructor
    public PhotoFragment() {

    }


    // This method is called after the parent Activity's onCreate() method has completed.
    // Accessing the view hierarchy of the parent activity must be done in the onActivityCreated.
    // At this point, it is safe to search for activity View objects by their ID, for example.
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        scanButton = (Button) getView().findViewById(R.id.buttonScan);
        scanButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ((ProcedureTicket) getActivity()).getNext();
                return;
            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mListener = (FragmentActivity) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void putPhoto(Bitmap img) {
        imgTicket = (ImageView) getView().findViewById(R.id.imageView);
        imgTicket.setImageBitmap(img);
    }






}
