package com.example.mordhausoundboard;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import org.michaelbel.bottomsheet.BottomSheet;
import java.util.ArrayList;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder>{

    private ArrayList<ParentDataModel> parents;
    private Activity activity;
    private BottomSheet bottomSheet;

    RVAdapter(ArrayList<ParentDataModel> parents, Activity activity){
        this.parents = parents;
        this.activity = activity;
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_parent, parent, false);
        return new PersonViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, final int position) {
        holder.parentName.setText(parents.get(position).getName());
        if(!parents.get(position).isAllItemsDownloaded()){
            holder.isDownloaded.setVisibility(View.INVISIBLE);
        }

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, VoiceTypeActivity.class);
                intent.putExtra(Constants.ITEMNAME, parents.get(position).getName());
                activity.startActivity(intent);
            }
        });

        holder.cv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(activity, "clicked long", Toast.LENGTH_SHORT).show();

                final BottomSheet.Builder builder = new BottomSheet.Builder(activity);
                builder.setTitle(parents.get(position).getName());
                builder.setMenu(R.menu.bottomsheet_parent, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {

                            case 0:
                                Toast.makeText(activity, "downloaded", Toast.LENGTH_SHORT).show();
                                builder.dismiss();
                                break;

                            case 1:
                                Toast.makeText(activity, "deleted", Toast.LENGTH_SHORT).show();
                                builder.dismiss();

                                break;

                            case 2:
                                Toast.makeText(activity, "canceld", Toast.LENGTH_SHORT).show();
                                builder.dismiss();

                                break;
                        }

                    }
                });

                builder.show();








                return true;
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return parents.size();
    }

    static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView parentName;
        ImageView personPhoto;
        ImageView isDownloaded;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cardView);
            parentName = itemView.findViewById(R.id.person_name);
            personPhoto = itemView.findViewById(R.id.person_photo);
            isDownloaded = itemView.findViewById(R.id.isDownloaded);
        }

    }
}
