package com.example.utang.adapter;


import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.utang.models.*;
import com.example.utang.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AddItemAdapter extends RecyclerView.Adapter<AddItemAdapter.AddItemViewHolder> {

    private List<Item> items;
    private OnItemChangeListener listener;
    private NumberFormat currencyFormat;
    private String[] units = {"pcs", "kg", "g", "L", "mL", "pack", "box", "bottle", "can", "bag"};

    public interface OnItemChangeListener {
        void onItemRemoved(int position);
        void onItemChanged();
    }

    public AddItemAdapter(List<Item> items, OnItemChangeListener listener) {
        this.items = items;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
    }

    @NonNull
    @Override
    public AddItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_add_item, parent, false);
        return new AddItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddItemViewHolder holder, int position) {
        Item item = items.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class AddItemViewHolder extends RecyclerView.ViewHolder {
        private EditText etItemName;
        private EditText etQuantity;
        private EditText etPrice;
        private Spinner spinnerUnit;
        private ImageButton btnRemoveItem;
        private TextView tvItemTotal;

        public AddItemViewHolder(@NonNull View itemView) {
            super(itemView);

            etItemName = itemView.findViewById(R.id.et_item_name);
            etQuantity = itemView.findViewById(R.id.et_quantity);
            etPrice = itemView.findViewById(R.id.et_price);
            spinnerUnit = itemView.findViewById(R.id.spinner_unit);
            btnRemoveItem = itemView.findViewById(R.id.btn_remove_item);
            tvItemTotal = itemView.findViewById(R.id.tv_item_total);

            setupUnitSpinner();
        }

        private void setupUnitSpinner() {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(itemView.getContext(),
                    android.R.layout.simple_spinner_item, units);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerUnit.setAdapter(adapter);
        }

        public void bind(Item item, int position) {
            // Set initial values
            etItemName.setText(item.getName());
            if (item.getQuantity() > 0) {
                etQuantity.setText(String.valueOf(item.getQuantity()));
            }
            if (item.getPrice() > 0) {
                etPrice.setText(String.valueOf(item.getPrice()));
            }

            // Set unit spinner
            for (int i = 0; i < units.length; i++) {
                if (units[i].equals(item.getUnit())) {
                    spinnerUnit.setSelection(i);
                    break;
                }
            }

            updateTotal(item);

            // Text watchers for real-time updates
            etItemName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    item.setName(s.toString());
                    notifyListener();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            etQuantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        double quantity = s.toString().isEmpty() ? 0 : Double.parseDouble(s.toString());
                        item.setQuantity(quantity);
                        updateTotal(item);
                        notifyListener();
                    } catch (NumberFormatException e) {
                        item.setQuantity(0);
                        updateTotal(item);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            etPrice.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        double price = s.toString().isEmpty() ? 0 : Double.parseDouble(s.toString());
                        item.setPrice(price);
                        updateTotal(item);
                        notifyListener();
                    } catch (NumberFormatException e) {
                        item.setPrice(0);
                        updateTotal(item);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // Unit spinner listener
            spinnerUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    item.setUnit(units[position]);
                    notifyListener();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            // Remove button
            btnRemoveItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemRemoved(position);
                }
            });
        }

        private void updateTotal(Item item) {
            double total = item.getTotalPrice();
            String totalText = currencyFormat.format(total).replace("PHP", "₱");
            tvItemTotal.setText(totalText);
        }

        private void notifyListener() {
            if (listener != null) {
                listener.onItemChanged();
            }
        }
    }
}