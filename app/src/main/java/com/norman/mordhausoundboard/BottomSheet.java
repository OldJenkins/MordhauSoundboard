package com.norman.mordhausoundboard;

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
    private int ActivityID;
    private int position;
    private String title_S;
    BottomSheet(int position,String title_S,int ActivityID) {
        this.position = position;
        this.title_S = title_S;
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
        title.setText(title_S);

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
