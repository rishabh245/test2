package com.example.rishabh.test2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by rishabh on 3/2/18.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.myViewHolder> {


    private List<ContactModel> contacts;
    private Context context;

    public RecyclerAdapter(List<ContactModel> contacts , Context context){
        this.contacts = contacts;
        this.context = context;
    }

    @Override
    public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_layout,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(myViewHolder holder, int position) {
        holder.nameTextView.setText("Name: " + contacts.get(position).name);
        holder.phoneNumberTextView.setText("Phone number: " + contacts.get(position).mobileNumber);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder{

        TextView nameTextView,phoneNumberTextView;
        public myViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            phoneNumberTextView = itemView.findViewById(R.id.number_text_view);
        }
    }
}
