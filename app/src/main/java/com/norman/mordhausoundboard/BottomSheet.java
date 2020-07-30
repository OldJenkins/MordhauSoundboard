package com.norman.mordhausoundboard;

/* This Bottomsheet has been made on my own because some other libs were not suitable for tablet screen sizes
 It is build mostly for the use of exactly this App so it needs to bee refactored if its going to bes used in other Applications
 */

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheet extends BottomSheetDialogFragment {

    private BottomSheetListener mListener;

    // This variable is used to determine the Activity which is calling this Bottomsheet
    private int ActivityID;
    private int position;
    private String title_txt;

    BottomSheet(int position,String title_txt,int ActivityID) {
        this.position = position;
        this.title_txt = title_txt;
        this.ActivityID = ActivityID;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.bottomsheet_grid,container,false);

        Button cancel = v.findViewById(R.id.btn_cancel);
        Button delete = v.findViewById(R.id.btn_delete);
        Button download = v.findViewById(R.id.btn_download);
        Button share = v.findViewById(R.id.btn_share);
        ImageView share_img = v.findViewById(R.id.img_share);
        TextView title = v.findViewById(R.id.txt_title);
        title.setText(title_txt);

        //if the Calling Activity is "HomeFragment", the Share functionality should not be shown
        if(ActivityID==Constants.ID_HOME){
            share.setVisibility(View.GONE);
            share_img.setVisibility(View.GONE);
        }

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClicked(0);
                dismiss();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClicked(1);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClicked(2);
                dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClicked(3);
                dismiss();
            }
        });

         return v;
    }

    int getPosition(){
        return position;
    }


    public interface BottomSheetListener{
        void onButtonClicked(int which);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (BottomSheetListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                    + "must implement BottomsheetListener");
        }
    }
}
