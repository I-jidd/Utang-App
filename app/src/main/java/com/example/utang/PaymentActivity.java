package com.example.utang;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {

    public static final String EXTRA_DEBT_ID = "debt_id";
    public static final String EXTRA_CUSTOMER_NAME = "customer_name";
    public static final String EXTRA_REMAINING_AMOUNT = "remaining_amount";

    private TextView tvCustomerName, tvRemainingAmount, tvNewBalance;
    private TextInputEditText etPaymentAmount;
    private Button btnRecordPayment, btnPayFull, btnCancel;

    private String debtId;
    private String customerName;
    private double remainingAmount;
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initializeFormatters();
        initializeViews();
        setupToolbar();
        loadIntentData();
        setupEventListeners();
        updateUI();
    }

    private void initializeFormatters() {
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
    }

    private void initializeViews() {
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvRemainingAmount = findViewById(R.id.tv_remaining_amount);
        tvNewBalance = findViewById(R.id.tv_new_balance);
        etPaymentAmount = findViewById(R.id.et_payment_amount);
        btnRecordPayment = findViewById(R.id.btn_record_payment);
        btnPayFull = findViewById(R.id.btn_pay_full);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Record Payment");
        }
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            debtId = intent.getStringExtra(EXTRA_DEBT_ID);
            customerName = intent.getStringExtra(EXTRA_CUSTOMER_NAME);
            remainingAmount = intent.getDoubleExtra(EXTRA_REMAINING_AMOUNT, 0.0);
        }
    }

    private void setupEventListeners() {
        etPaymentAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateNewBalance();
                updateButtonStates();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnRecordPayment.setOnClickListener(v -> recordPayment());
        btnPayFull.setOnClickListener(v -> payFullAmount());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void updateUI() {
        tvCustomerName.setText(customerName);
        String remainingText = currencyFormat.format(remainingAmount).replace("PHP", "₱");
        tvRemainingAmount.setText(remainingText);
        updateNewBalance();
        updateButtonStates();
    }

    private void updateNewBalance() {
        String paymentText = etPaymentAmount.getText().toString().trim();
        double paymentAmount = 0.0;

        try {
            if (!paymentText.isEmpty()) {
                paymentAmount = Double.parseDouble(paymentText);
            }
        } catch (NumberFormatException e) {
            paymentAmount = 0.0;
        }

        double newBalance = Math.max(0, remainingAmount - paymentAmount);
        String newBalanceText = currencyFormat.format(newBalance).replace("PHP", "₱");
        tvNewBalance.setText(newBalanceText);

        // Change color based on whether debt will be fully paid
        if (newBalance == 0 && paymentAmount > 0) {
            tvNewBalance.setTextColor(getColor(R.color.status_paid));
        } else if (paymentAmount > remainingAmount) {
            tvNewBalance.setTextColor(getColor(R.color.status_overdue));
        } else {
            tvNewBalance.setTextColor(getColor(R.color.text_primary));
        }
    }

    private void updateButtonStates() {
        String paymentText = etPaymentAmount.getText().toString().trim();
        boolean hasValidAmount = false;

        try {
            if (!paymentText.isEmpty()) {
                double amount = Double.parseDouble(paymentText);
                hasValidAmount = amount > 0 && amount <= remainingAmount;
            }
        } catch (NumberFormatException e) {
            hasValidAmount = false;
        }

        btnRecordPayment.setEnabled(hasValidAmount);
    }

    private void recordPayment() {
        String paymentText = etPaymentAmount.getText().toString().trim();

        if (paymentText.isEmpty()) {
            Toast.makeText(this, "Please enter a payment amount", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double paymentAmount = Double.parseDouble(paymentText);

            if (paymentAmount <= 0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (paymentAmount > remainingAmount) {
                Toast.makeText(this, "Payment amount cannot exceed remaining balance", Toast.LENGTH_SHORT).show();
                return;
            }

            // Record the payment
            Intent resultIntent = new Intent();
            resultIntent.putExtra("debt_id", debtId);
            resultIntent.putExtra("paid_amount", paymentAmount);
            setResult(RESULT_OK, resultIntent);

            String message = String.format("Payment of %s recorded successfully!",
                    currencyFormat.format(paymentAmount).replace("PHP", "₱"));
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    private void payFullAmount() {
        etPaymentAmount.setText(String.valueOf(remainingAmount));
        updateNewBalance();
        updateButtonStates();
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