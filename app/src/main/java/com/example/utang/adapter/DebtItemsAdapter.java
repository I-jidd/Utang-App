package com.example.utang.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.utang.R;
import com.example.utang.models.Item;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DebtItemsAdapter extends RecyclerView.Adapter<DebtItemsAdapter.ItemViewHolder> {

    private List<Item> items;
    private NumberFormat currencyFormat;

    public DebtItemsAdapter(List<Item> items) {
        this.items = items;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_debt_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvItemName;
        private TextView tvQuantityUnit;
        private TextView tvUnitPrice;
        private TextView tvTotalPrice;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvQuantityUnit = itemView.findViewById(R.id.tv_quantity_unit);
            tvUnitPrice = itemView.findViewById(R.id.tv_unit_price);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
        }

        public void bind(Item item) {
            tvItemName.setText(item.getName());

            String quantityText = String.format("%.0f %s", item.getQuantity(), item.getUnit());
            tvQuantityUnit.setText(quantityText);

            String unitPriceText = currencyFormat.format(item.getPrice()).replace("PHP", "₱");
            tvUnitPrice.setText(unitPriceText + " each");

            String totalPriceText = currencyFormat.format(item.getTotalPrice()).replace("PHP", "₱");
            tvTotalPrice.setText(totalPriceText);
        }
    }
}