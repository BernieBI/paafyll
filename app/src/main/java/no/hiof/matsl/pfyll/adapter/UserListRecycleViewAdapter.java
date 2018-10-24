package no.hiof.matsl.pfyll.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.model.UserList;


public class UserListRecycleViewAdapter extends RecyclerView.Adapter<UserListRecycleViewAdapter.ViewHolder> {
    private static final String TAG = "RecycleViewAdapter";

    private ArrayList<UserList> lists;
    private LayoutInflater inflater;
    private Context context;

    public UserListRecycleViewAdapter(Context context, ArrayList<UserList> lists) {
        this.lists = lists;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_list_userlist, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final UserList current_list = lists.get(position);

        holder.listName.setText(current_list.getNavn());

        Log.d(TAG, "onBindViewHolder: called." + lists);
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView listName;
        private ItemClickListener itemClickUserListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            listName = itemView.findViewById(R.id.list_name);
            itemView.setOnClickListener(this);
        }

        public void setItemClickUserListener(ItemClickListener itemClickUserListener){
            this.itemClickUserListener = itemClickUserListener;
        }

        @Override
        public void onClick(View v) {
            itemClickUserListener.onClick(v, getAdapterPosition(), false);
        }

    }

}
