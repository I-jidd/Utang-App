package com.example.utang.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.utang.R;
import com.example.utang.models.*;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DebtAdapter extends RecyclerView.Adapter<DebtAdapter.DebtViewHolder> {

    private List<Debt> debts;
    private OnDebtClickListener listener;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    public interface OnDebtClickListener {
        void onViewDetails(Debt debt);
        void onMarkPaid(Debt debt);
    }

    public DebtAdapter(List<Debt> debts, OnDebtClickListener listener) {
        this.debts = debts;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_debt, parent, false);
        return new DebtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebtViewHolder holder, int position) {
        Debt debt = debts.get(position);
        holder.bind(debt);
    }

    @Override
    public int getItemCount() {
        return debts.size();
    }

    class DebtViewHolder extends RecyclerView.ViewHolder {
        private View statusIndicator;
        private TextView tvCustomerName;
        private TextView tvDebtDescription;
        private TextView tvAmount;
        private TextView tvDueDate;
        private TextView tvStatus;
        private TextView tvItemsPreview;
        private Button btnViewDetails;
        private Button btnMarkPaid;

        public DebtViewHolder(@NonNull View itemView) {
            super(itemView);

            statusIndicator = itemView.findViewById(R.id.view_status_indicator);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvDebtDescription = itemView.findViewById(R.id.tv_debt_description);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvItemsPreview = itemView.findViewById(R.id.tv_items_preview);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnMarkPaid = itemView.findViewById(R.id.btn_mark_paid);
        }

        public void bind(Debt debt) {
            // Basic info
            tvCustomerName.setText(debt.getCustomerName());
            tvDebtDescription.setText(debt.getDescription());

            // Amount formatting
            String amountText = currencyFormat.format(debt.getRemainingAmount()).replace("PHP", "₱");
            tvAmount.setText(amountText);

            // Due date
            String dueDateText = "Due: " + dateFormat.format(debt.getDueDate());
            tvDueDate.setText(dueDateText);

            // Status
            DebtStatus status = debt.getStatus();
            tvStatus.setText(status.getDisplayName().toUpperCase());
            tvStatus.setBackgroundResource(status.getBackgroundResource());

            // Status indicator color
            int indicatorColor = itemView.getContext().getColor(status.getColorResource());
            statusIndicator.setBackgroundColor(indicatorColor);

            // Amount color based on status
            int amountColor;
            switch (status) {
                case OVERDUE:
                    amountColor = itemView.getContext().getColor(R.color.status_overdue);
                    break;
                case PAID:
                    amountColor = itemView.getContext().getColor(R.color.status_paid);
                    break;
                default:
                    amountColor = itemView.getContext().getColor(R.color.text_primary);
                    break;
            }
            tvAmount.setTextColor(amountColor);

            // Items preview
            String itemsPreview = debt.getItemsPreview();
            if (itemsPreview.isEmpty()) {
                tvItemsPreview.setVisibility(View.GONE);
            } else {
                tvItemsPreview.setVisibility(View.VISIBLE);
                tvItemsPreview.setText(itemsPreview);
            }

            // Button states
            if (debt.getStatus() == DebtStatus.PAID) {
                btnMarkPaid.setText("Paid");
                btnMarkPaid.setEnabled(false);
                btnMarkPaid.setBackgroundResource(R.drawable.status_paid_bg);
            } else {
                btnMarkPaid.setText("Mark Paid");
                btnMarkPaid.setEnabled(true);
                btnMarkPaid.setBackgroundResource(R.drawable.button_small_primary);
            }

            // Click listeners
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(debt);
                }
            });

            btnMarkPaid.setOnClickListener(v -> {
                if (listener != null && debt.getStatus() != DebtStatus.PAID) {
                    listener.onMarkPaid(debt);
                }
            });

            // Card click for details
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(debt);
                }
            });
        }
    }
}