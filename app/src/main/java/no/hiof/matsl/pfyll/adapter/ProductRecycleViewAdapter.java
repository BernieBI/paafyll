package no.hiof.matsl.pfyll.adapter;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.SingleProductActivity;
import no.hiof.matsl.pfyll.model.Product;


public class ProductRecycleViewAdapter extends PagedListAdapter<Product, ProductRecycleViewAdapter.ViewHolder> {

    private static final String TAG = "ProductRVAdapter";
    public boolean useListLayout = false;
    private boolean isListActivity;
    private Bundle arguments;
    private  String userListID;
    ArrayList<String> productsInList;
    //firebase
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private static final DiffUtil.ItemCallback<Product> DIFF_CALLBACK = new DiffUtil.ItemCallback<Product>() {
        @Override
        public boolean areItemsTheSame(@NonNull Product p1, @NonNull Product p2) {
            return p1.getVarenummer().equals(p2.getVarenummer());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product p1, @NonNull Product p2) {
            return p1.getDatotid().equals(p2.getDatotid());
        }
    };

    private LayoutInflater inflater;
    private Context context;


    public ProductRecycleViewAdapter(Context context, Bundle arguments) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.arguments = arguments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = inflater.inflate(viewType, parent, false);

        if (arguments != null) {
            if (arguments.getString("userListId") != null){
                isListActivity = true;
                userListID = arguments.getString("userListId");
            }
            productsInList = arguments.getStringArrayList("preSetProducts");

        }
        return new ViewHolder(view);

    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Product current_product = getItem(position);
        if (current_product == null) {
            return;
        }

        if (!isListActivity) {
            holder.removeFromListBtn.setVisibility(View.GONE);
        }else{
            holder.removeFromListBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (productsInList.remove(current_product.getId()+"")){
                        Log.d(TAG, "Removed from list, list: " + productsInList);
                        DatabaseReference userListRef = database.getReference("userLists");
                        userListRef.child(userListID).child("products").setValue(productsInList);
                        Toast toast = Toast.makeText(context,  String.format("%s %s!", current_product.getVarenavn(),  context.getString(R.string.removed_from_list)), Toast.LENGTH_LONG);
                        toast.show();
                        holder.itemView.setVisibility(View.GONE);
                        //notifyItemRemoved(position);
                    }

                }
            });
        }
        Glide.with(context)
                .asBitmap()
                .load(current_product.getBildeUrl())
                .into(holder.productImage);

        holder.productName.setText(current_product.getVarenavn());
        holder.productCountry.setText(current_product.getLand());
        holder.productPrice.setText(String.format("Kr %s",current_product.getPris()));
        if (getItemViewType(position) == R.layout.layout_list_product_alt){
            holder.productTaste.setText(current_product.getSmak());
        }

        holder.setItemClickListener(new ItemClickListener(){
            @Override
            public void onClick(View view, int position, boolean isLoading) {

                //Starting single product activity
                Intent singleProductIntent = new Intent(context, SingleProductActivity.class);
                singleProductIntent.putExtra("ProductID", current_product.getId());
                context.startActivity(singleProductIntent);

            }
        });
        Log.d(TAG, "onBindViewHolder: called.");

    }

    @Override
    public int getItemViewType(final int position) {
        return useListLayout ? R.layout.layout_list_product_alt : R.layout.layout_list_product;
    }

    public void setLayout(Boolean useListLayout){
        this.useListLayout = useListLayout;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productCountry;
        TextView productTaste;
        ImageButton removeFromListBtn;

        private ItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productCountry = itemView.findViewById(R.id.product_country);
            productTaste = itemView.findViewById(R.id.product_taste);
            removeFromListBtn = itemView.findViewById(R.id.removeFromListBtn);
            itemView.setOnClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener){
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition(), false);
        }

    }



}
