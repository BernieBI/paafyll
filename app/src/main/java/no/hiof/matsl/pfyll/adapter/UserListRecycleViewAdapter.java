package no.hiof.matsl.pfyll.adapter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.SingleUserListActivity;
import no.hiof.matsl.pfyll.model.UserList;


public class UserListRecycleViewAdapter extends RecyclerView.Adapter<UserListRecycleViewAdapter.ViewHolder> {
    private static final String TAG = "ListRecycleViewAdapter";

    private ArrayList<UserList> lists;
    private LayoutInflater inflater;
    private Context context;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

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

        holder.listCount.setText( (current_list.getProducts() == null ? "0" : current_list.getProducts().size() + "" )  );

        holder.removeListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setMessage( R.string.confirm_delete_list)
                        .setPositiveButton("Slett", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                DatabaseReference userListRef = database.getReference("users/" + user.getUid() + "/userLists/" + current_list.getId());
                                userListRef.removeValue();
                                notifyDataSetChanged();
                                Log.d(TAG, userListRef.toString());
                            }
                        })
                        .setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.show();
            }
        });
        holder.setItemClickUserListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLoading) {
                if (current_list.getProducts() != null ) {
                    //Starting single product activity
                    Intent singleUserListIntent = new Intent(context, SingleUserListActivity.class);
                    singleUserListIntent.putExtra("UserListId", current_list.getId());
                    context.startActivity(singleUserListIntent);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView listName;
        TextView listCount;
        ImageButton removeListBtn;

        private ItemClickListener itemClickUserListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            listName = itemView.findViewById(R.id.list_name);
            listCount = itemView.findViewById(R.id.list_count);
            removeListBtn = itemView.findViewById(R.id.removeListBtn);
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
