package com.example.utang;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.utang.adapter.DebtItemsAdapter;
import com.example.utang.models.Debt;
import com.example.utang.models.DebtStatus;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DebtDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_DEBT_ID = "debt_id";
    public static final String EXTRA_CUSTOMER_NAME = "customer_name";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_PAID_AMOUNT = "paid_amount";
    public static final String EXTRA_DESCRIPTION = "description";
    public static final String EXTRA_DUE_DATE = "due_date";
    public static final String EXTRA_DATE_ADDED = "date_added";
    public static final String EXTRA_STATUS = "status";

    private TextView tvCustomerName, tvAmount, tvPaidAmount, tvRemainingAmount;
    private TextView tvDescription, tvDueDate, tvDateAdded, tvStatus;
    private TextView tvItemsTitle, tvNoItems;
    private RecyclerView rvItems;
    private Button btnRecordPayment, btnMarkPaid, btnEdit, btnDelete;

    private Debt currentDebt;
    private DebtItemsAdapter itemsAdapter;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_debt_details);

        initializeFormatters();
        initializeViews();
        setupToolbar();
        loadDebtData();
        setupEventListeners();
        updateUI();
    }

    private void initializeFormatters() {
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    private void initializeViews() {
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvAmount = findViewById(R.id.tv_amount);
        tvPaidAmount = findViewById(R.id.tv_paid_amount);
        tvRemainingAmount = findViewById(R.id.tv_remaining_amount);
        tvDescription = findViewById(R.id.tv_description);
        tvDueDate = findViewById(R.id.tv_due_date);
        tvDateAdded = findViewById(R.id.tv_date_added);
        tvStatus = findViewById(R.id.tv_status);
        tvItemsTitle = findViewById(R.id.tv_items_title);
        tvNoItems = findViewById(R.id.tv_no_items);
        rvItems = findViewById(R.id.rv_items);
        btnRecordPayment = findViewById(R.id.btn_record_payment);
        btnMarkPaid = findViewById(R.id.btn_mark_paid);
        btnEdit = findViewById(R.id.btn_edit);
        btnDelete = findViewById(R.id.btn_delete);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Debt Details");
        }
    }

    private void loadDebtData() {
        Intent intent = getIntent();
        if (intent != null) {
            currentDebt = new Debt();
            currentDebt.setId(intent.getStringExtra(EXTRA_DEBT_ID));
            currentDebt.setCustomerName(intent.getStringExtra(EXTRA_CUSTOMER_NAME));
            currentDebt.setAmount(intent.getDoubleExtra(EXTRA_AMOUNT, 0.0));
            currentDebt.setPaidAmount(intent.getDoubleExtra(EXTRA_PAID_AMOUNT, 0.0));
            currentDebt.setDescription(intent.getStringExtra(EXTRA_DESCRIPTION));

            long dueDateMillis = intent.getLongExtra(EXTRA_DUE_DATE, 0);
            if (dueDateMillis > 0) {
                currentDebt.setDueDate(new java.util.Date(dueDateMillis));
            }

            long dateAddedMillis = intent.getLongExtra(EXTRA_DATE_ADDED, 0);
            if (dateAddedMillis > 0) {
                currentDebt.setDateAdded(new java.util.Date(dateAddedMillis));
            }

            String statusString = intent.getStringExtra(EXTRA_STATUS);
            if (statusString != null) {
                currentDebt.setStatus(DebtStatus.valueOf(statusString));
            }
        }
    }

    private void setupEventListeners() {
        btnRecordPayment.setOnClickListener(v -> openPaymentActivity());
        btnMarkPaid.setOnClickListener(v -> showMarkPaidDialog());
        btnEdit.setOnClickListener(v -> editDebt());
        btnDelete.setOnClickListener(v -> showDeleteDialog());
    }

    private void updateUI() {
        if (currentDebt == null) return;

        // Basic info
        tvCustomerName.setText(currentDebt.getCustomerName());
        tvDescription.setText(currentDebt.getDescription());

        // Amount formatting
        String amountText = currencyFormat.format(currentDebt.getAmount()).replace("PHP", "₱");
        String paidAmountText = currencyFormat.format(currentDebt.getPaidAmount()).replace("PHP", "₱");
        String remainingAmountText = currencyFormat.format(currentDebt.getRemainingAmount()).replace("PHP", "₱");

        tvAmount.setText(amountText);
        tvPaidAmount.setText(paidAmountText);
        tvRemainingAmount.setText(remainingAmountText);

        // Dates
        if (currentDebt.getDueDate() != null) {
            tvDueDate.setText(dateFormat.format(currentDebt.getDueDate()));
        }
        if (currentDebt.getDateAdded() != null) {
            tvDateAdded.setText(dateFormat.format(currentDebt.getDateAdded()));
        }

        // Status
        DebtStatus status = currentDebt.getStatus();
        tvStatus.setText(status.getDisplayName().toUpperCase());
        tvStatus.setBackgroundResource(status.getBackgroundResource());

        // Items
        setupItemsList();

        // Button states
        updateButtonStates();
    }

    private void setupItemsList() {
        if (currentDebt.getItems() != null && !currentDebt.getItems().isEmpty()) {
            tvNoItems.setVisibility(android.view.View.GONE);
            rvItems.setVisibility(android.view.View.VISIBLE);

            itemsAdapter = new DebtItemsAdapter(currentDebt.getItems());
            rvItems.setLayoutManager(new LinearLayoutManager(this));
            rvItems.setAdapter(itemsAdapter);
        } else {
            tvNoItems.setVisibility(android.view.View.VISIBLE);
            rvItems.setVisibility(android.view.View.GONE);
        }
    }

    private void updateButtonStates() {
        boolean isPaid = currentDebt.getStatus() == DebtStatus.PAID;

        btnRecordPayment.setEnabled(!isPaid);
        btnMarkPaid.setEnabled(!isPaid);

        if (isPaid) {
            btnMarkPaid.setText("Paid");
            btnMarkPaid.setBackgroundResource(R.drawable.status_paid_bg);
        } else {
            btnMarkPaid.setText("Mark as Paid");
            btnMarkPaid.setBackgroundResource(R.drawable.button_primary);
        }
    }

    private void openPaymentActivity() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PaymentActivity.EXTRA_DEBT_ID, currentDebt.getId());
        intent.putExtra(PaymentActivity.EXTRA_CUSTOMER_NAME, currentDebt.getCustomerName());
        intent.putExtra(PaymentActivity.EXTRA_REMAINING_AMOUNT, currentDebt.getRemainingAmount());
        startActivityForResult(intent, 100);
    }

    private void showMarkPaidDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Mark as Paid")
                .setMessage("Are you sure you want to mark this debt as fully paid?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    currentDebt.setPaidAmount(currentDebt.getAmount());
                    currentDebt.setStatus(DebtStatus.PAID);
                    updateUI();
                    setResult(RESULT_OK);
                    Toast.makeText(this, "Debt marked as paid", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void editDebt() {
        // TODO: Implement edit debt functionality
        Toast.makeText(this, "Edit functionality coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Debt")
                .setMessage("Are you sure you want to delete this debt? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // TODO: Implement delete functionality
                    setResult(RESULT_OK);
                    finish();
                    Toast.makeText(this, "Debt deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Payment was recorded, refresh the UI
            double paidAmount = data.getDoubleExtra("paid_amount", 0.0);
            currentDebt.setPaidAmount(currentDebt.getPaidAmount() + paidAmount);
            updateUI();
            setResult(RESULT_OK);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}