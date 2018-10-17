package no.hiof.matsl.pfyll.adapter;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import no.hiof.matsl.pfyll.ProductsActivity;
import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.SingleProductActivity;
import no.hiof.matsl.pfyll.model.Product;


public class ProductRecycleViewAdapter extends RecyclerView.Adapter<ProductRecycleViewAdapter.ViewHolder> {
    private static final String TAG = "RecycleViewAdapter";

    private ArrayList<Product> products;
    private LayoutInflater inflater;
    private Context context;

    public ProductRecycleViewAdapter(Context context, ArrayList<Product> products) {
        this.products = products;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_list_products, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Product current_product = products.get(position);

        Glide.with(context)
                .asBitmap()
                .load(current_product.getBildeUrl())
                .into(holder.productImage);

        holder.productName.setText(current_product.getVarenavn());
        holder.productCountry.setText(current_product.getLand());
        holder.productPrice.setText(current_product.getPris());

        holder.setItemClickListener(new ItemClickListener(){
            @Override
            public void onClick(View view, int position, boolean isLoading) {

                //Starting single product activity
                Intent singleProductIntent = new Intent(context, SingleProductActivity.class);
                singleProductIntent.putExtra("ProductID", current_product.getFirebaseID());
                context.startActivity(singleProductIntent);

            }
        });
        Log.d(TAG, "onBindViewHolder: called." + products);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productCountry;
        private ItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productCountry = itemView.findViewById(R.id.product_country);
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
