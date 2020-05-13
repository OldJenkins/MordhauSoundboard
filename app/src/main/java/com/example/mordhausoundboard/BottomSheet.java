package com.example.mordhausoundboard;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheet extends BottomSheetDialogFragment {

    private BottomSheetListener mListener;

    private int position;
    BottomSheet(int position) {
        this.position = position;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.bottomsheet_main,container,false);

        Button cancel = v.findViewById(R.id.btn_cancel);
        Button delete = v.findViewById(R.id.btn_delete);
        Button download = v.findViewById(R.id.btn_download);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClicked(0);
                dismiss();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClicked(1);
                dismiss();
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClicked(2);
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
