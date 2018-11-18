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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;

import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.SingleProductActivity;
import no.hiof.matsl.pfyll.model.FirestoreProduct;
import no.hiof.matsl.pfyll.model.IdFilter;
import no.hiof.matsl.pfyll.model.Product;


public class ProductRecycleViewAdapter extends PagedListAdapter<FirestoreProduct, ProductRecycleViewAdapter.ViewHolder> {

    private static final String TAG = "ProductRVAdapter";
    public boolean useListLayout = true;
    private boolean isListActivity;
    private Bundle arguments;
    private  String userListID;
    private ArrayList<String> preSetProducts;

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    //firebase
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private static final DiffUtil.ItemCallback<FirestoreProduct> DIFF_CALLBACK = new DiffUtil.ItemCallback<FirestoreProduct>() {
        @Override
        public boolean areItemsTheSame(@NonNull FirestoreProduct p1, @NonNull FirestoreProduct p2) {
            return p1.getVarenummer().equals(p2.getVarenummer());
        }

        @Override
        public boolean areContentsTheSame(@NonNull FirestoreProduct p1, @NonNull FirestoreProduct p2) {
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
            preSetProducts = arguments.getStringArrayList("preSetProducts");
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Product current_product = getItem(position);
        if (current_product == null ) {
            return;
        }

        if (!isListActivity) {
            holder.removeFromListBtn.setVisibility(View.GONE);
        }else{
            holder.removeFromListBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (preSetProducts.remove(current_product.getId()+"")){

                        Log.d(TAG, "Removed from list, list: " + preSetProducts);

                        DatabaseReference userListRef = database.getReference("users/" + user.getUid() + "/userLists/" + userListID);
                        userListRef.child("products").setValue(preSetProducts);
                        Toast toast = Toast.makeText(context,  String.format("%s %s!", current_product.getVarenavn(),  context.getString(R.string.removed_from_list)), Toast.LENGTH_LONG);
                        toast.show();
                        notifyItemRemoved(position);
                    }
                }
            });
        }
        Glide.with(context)
                .asBitmap()
                .load(current_product.getBildeUrl())
                .into(holder.productImage);
        holder.productType.setText(current_product.getVaretype());
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
        TextView productName, productPrice, productCountry,productTaste, productType;
        ImageButton removeFromListBtn;

        private ItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productCountry = itemView.findViewById(R.id.product_country);
            productTaste = itemView.findViewById(R.id.product_taste);
            productType = itemView.findViewById(R.id.product_type);
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
