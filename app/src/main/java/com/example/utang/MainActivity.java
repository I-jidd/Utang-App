package com.example.utang;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.utang.adapter.AddItemAdapter;
import com.example.utang.adapter.DebtAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.utang.models.*;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private TextView tvTotalOverdue, tvTotalPending;
    private EditText etSearch;
    private ImageButton btnFilter;
    private Chip chipAll, chipOverdue, chipPending, chipPaid;
    private Button btnAddUtang, btnQuickPayment;
    private RecyclerView rvDebtList, rvItems;
    private Spinner spinnerSort;

    // Add Utang Form Components
    private CardView cardAddForm;
    private TextInputEditText etCustomerName, etAmount, etDescription;
    private Button btnSelectDate, btnAddItem, btnCancel, btnSaveUtang;
    private TextView tvSelectedDate;

    // Adapters
    private DebtAdapter debtAdapter;
    private AddItemAdapter addItemAdapter;

    // Data
    private List<Debt> allDebts;
    private List<Debt> filteredDebts;
    private List<Item> itemsToAdd;
    private String selectedDueDate;

    // Filter state
    private String currentFilter = "All";
    private String currentSearchQuery = "";

    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeFormatters();
        initializeViews();
        initializeData();
        setupRecyclerViews();
        setupEventListeners();
        updateStatistics();
        updateDebtList();
    }

    private void initializeFormatters() {
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        currencyFormat.setCurrency(java.util.Currency.getInstance("PHP"));
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    private void initializeViews() {
        // Statistics
        tvTotalOverdue = findViewById(R.id.tv_total_overdue);
        tvTotalPending = findViewById(R.id.tv_total_pending);

        // Search and Filter
        etSearch = findViewById(R.id.et_search);
        btnFilter = findViewById(R.id.btn_filter);
        chipAll = findViewById(R.id.chip_all);
        chipOverdue = findViewById(R.id.chip_overdue);
        chipPending = findViewById(R.id.chip_pending);
        chipPaid = findViewById(R.id.chip_paid);

        // Action Buttons
        btnAddUtang = findViewById(R.id.btn_add_utang);
        btnQuickPayment = findViewById(R.id.btn_quick_payment);

        // RecyclerViews
        rvDebtList = findViewById(R.id.rv_debt_list);
        rvItems = findViewById(R.id.rv_items);
        spinnerSort = findViewById(R.id.spinner_sort);

        // Add Form
        cardAddForm = findViewById(R.id.card_add_form);
        etCustomerName = findViewById(R.id.et_customer_name);
        etAmount = findViewById(R.id.et_amount);
        etDescription = findViewById(R.id.et_description);
        btnSelectDate = findViewById(R.id.btn_select_date);
        btnAddItem = findViewById(R.id.btn_add_item);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSaveUtang = findViewById(R.id.btn_save_utang);

        setupSortSpinner();
    }

    private void setupSortSpinner() {
        String[] sortOptions = {"Date Added", "Due Date", "Amount", "Customer Name", "Status"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(adapter);
    }

    private void initializeData() {
        allDebts = new ArrayList<>();
        filteredDebts = new ArrayList<>();
        itemsToAdd = new ArrayList<>();

        // Sample data
        loadSampleData();
    }

    private void loadSampleData() {
        Calendar cal = Calendar.getInstance();

        // Overdue debt
        cal.add(Calendar.DAY_OF_MONTH, -5);
        allDebts.add(new Debt("1", "Juan Dela Cruz", 2500.0, "Groceries and household items",
                cal.getTime(), DebtStatus.OVERDUE));

        // Pending debt
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 10);
        allDebts.add(new Debt("2", "Maria Santos", 1750.0, "Rice and cooking oil",
                cal.getTime(), DebtStatus.PENDING));

        // Another overdue
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -2);
        allDebts.add(new Debt("3", "Pedro Garcia", 3200.0, "School supplies for kids",
                cal.getTime(), DebtStatus.OVERDUE));

        // Paid debt
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -15);
        allDebts.add(new Debt("4", "Ana Reyes", 850.0, "Medicine",
                cal.getTime(), DebtStatus.PAID));
    }

    private void setupRecyclerViews() {
        // Debt list
        debtAdapter = new DebtAdapter(filteredDebts, new DebtAdapter.OnDebtClickListener() {
            @Override
            public void onViewDetails(Debt debt) {
                // Launch DebtDetailsActivity with debt data
                Intent intent = new Intent(MainActivity.this, DebtDetailsActivity.class);
                intent.putExtra(DebtDetailsActivity.EXTRA_DEBT_ID, debt.getId());
                intent.putExtra(DebtDetailsActivity.EXTRA_CUSTOMER_NAME, debt.getCustomerName());
                intent.putExtra(DebtDetailsActivity.EXTRA_AMOUNT, debt.getAmount());
                intent.putExtra(DebtDetailsActivity.EXTRA_PAID_AMOUNT, debt.getPaidAmount());
                intent.putExtra(DebtDetailsActivity.EXTRA_DESCRIPTION, debt.getDescription());

                if (debt.getDueDate() != null) {
                    intent.putExtra(DebtDetailsActivity.EXTRA_DUE_DATE, debt.getDueDate().getTime());
                }
                if (debt.getDateAdded() != null) {
                    intent.putExtra(DebtDetailsActivity.EXTRA_DATE_ADDED, debt.getDateAdded().getTime());
                }
                intent.putExtra(DebtDetailsActivity.EXTRA_STATUS, debt.getStatus().name());

                startActivityForResult(intent, 200);
            }

            @Override
            public void onMarkPaid(Debt debt) {
                showPaymentDialog(debt);
            }
        });

        rvDebtList.setLayoutManager(new LinearLayoutManager(this));
        rvDebtList.setAdapter(debtAdapter);

        // Add items list (existing code)
        addItemAdapter = new AddItemAdapter(itemsToAdd, new AddItemAdapter.OnItemChangeListener() {
            @Override
            public void onItemRemoved(int position) {
                itemsToAdd.remove(position);
                addItemAdapter.notifyItemRemoved(position);
                updateFormTotal();
            }

            @Override
            public void onItemChanged() {
                updateFormTotal();
            }
        });

        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(addItemAdapter);
    }


    private void setupEventListeners() {
        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                filterAndUpdateList();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter chips
        chipAll.setOnClickListener(v -> selectFilterChip("All"));
        chipOverdue.setOnClickListener(v -> selectFilterChip("Overdue"));
        chipPending.setOnClickListener(v -> selectFilterChip("Pending"));
        chipPaid.setOnClickListener(v -> selectFilterChip("Paid"));

        // Action buttons
        btnAddUtang.setOnClickListener(v -> showAddForm());
        btnQuickPayment.setOnClickListener(v -> showQuickPaymentDialog());

        // Form buttons
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnAddItem.setOnClickListener(v -> addNewItem());
        btnCancel.setOnClickListener(v -> hideAddForm());
        btnSaveUtang.setOnClickListener(v -> saveNewUtang());

        // Sort spinner
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortDebts(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void selectFilterChip(String filter) {
        currentFilter = filter;

        // Update chip appearances
        resetChipStyles();
        switch (filter) {
            case "All":
                chipAll.setChipBackgroundColorResource(R.color.primary_blue);
                chipAll.setTextColor(getColor(R.color.text_white));
                break;
            case "Overdue":
                chipOverdue.setChipBackgroundColorResource(R.color.primary_blue);
                chipOverdue.setTextColor(getColor(R.color.text_white));
                break;
            case "Pending":
                chipPending.setChipBackgroundColorResource(R.color.primary_blue);
                chipPending.setTextColor(getColor(R.color.text_white));
                break;
            case "Paid":
                chipPaid.setChipBackgroundColorResource(R.color.primary_blue);
                chipPaid.setTextColor(getColor(R.color.text_white));
                break;
        }

        filterAndUpdateList();
    }

    private void resetChipStyles() {
        int defaultColor = getColor(R.color.border_light);
        int defaultTextColor = getColor(R.color.text_secondary);

        chipAll.setChipBackgroundColorResource(R.color.border_light);
        chipAll.setTextColor(defaultTextColor);
        chipOverdue.setChipBackgroundColorResource(R.color.border_light);
        chipOverdue.setTextColor(defaultTextColor);
        chipPending.setChipBackgroundColorResource(R.color.border_light);
        chipPending.setTextColor(defaultTextColor);
        chipPaid.setChipBackgroundColorResource(R.color.border_light);
        chipPaid.setTextColor(defaultTextColor);
    }

    private void filterAndUpdateList() {
        filteredDebts.clear();

        for (Debt debt : allDebts) {
            boolean matchesFilter = true;
            boolean matchesSearch = true;

            // Apply status filter
            if (!currentFilter.equals("All")) {
                DebtStatus filterStatus = DebtStatus.valueOf(currentFilter.toUpperCase());
                matchesFilter = debt.getStatus() == filterStatus;
            }

            // Apply search filter
            if (!currentSearchQuery.isEmpty()) {
                String query = currentSearchQuery.toLowerCase();
                matchesSearch = debt.getCustomerName().toLowerCase().contains(query) ||
                        debt.getDescription().toLowerCase().contains(query);
            }

            if (matchesFilter && matchesSearch) {
                filteredDebts.add(debt);
            }
        }

        debtAdapter.notifyDataSetChanged();
    }

    private void sortDebts(int sortOption) {
        Comparator<Debt> comparator;

        switch (sortOption) {
            case 0: // Date Added
                comparator = (d1, d2) -> d2.getDateAdded().compareTo(d1.getDateAdded());
                break;
            case 1: // Due Date
                comparator = (d1, d2) -> d1.getDueDate().compareTo(d2.getDueDate());
                break;
            case 2: // Amount
                comparator = (d1, d2) -> Double.compare(d2.getAmount(), d1.getAmount());
                break;
            case 3: // Customer Name
                comparator = (d1, d2) -> d1.getCustomerName().compareTo(d2.getCustomerName());
                break;
            case 4: // Status
                comparator = (d1, d2) -> d1.getStatus().compareTo(d2.getStatus());
                break;
            default:
                return;
        }

        Collections.sort(filteredDebts, comparator);
        debtAdapter.notifyDataSetChanged();
    }

    private void showAddForm() {
        cardAddForm.setVisibility(View.VISIBLE);
        clearForm();
    }

    private void hideAddForm() {
        cardAddForm.setVisibility(View.GONE);
        clearForm();
    }

    private void clearForm() {
        etCustomerName.setText("");
        etAmount.setText("");
        etDescription.setText("");
        selectedDueDate = null;
        btnSelectDate.setText("Select Date");
        itemsToAdd.clear();
        addItemAdapter.notifyDataSetChanged();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedDueDate = dateFormat.format(selectedCalendar.getTime());
                    btnSelectDate.setText(selectedDueDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void addNewItem() {
        itemsToAdd.add(new Item("", 0, 0.0, "pcs"));
        addItemAdapter.notifyItemInserted(itemsToAdd.size() - 1);
    }

    private void updateFormTotal() {
        // This would be called when items change to update the total amount
        double total = 0.0;
        for (Item item : itemsToAdd) {
            total += item.getQuantity() * item.getPrice();
        }

        if (total > 0) {
            etAmount.setText(String.valueOf(total));
        }
    }

    private void saveNewUtang() {
        String customerName = etCustomerName.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (customerName.isEmpty() || amountStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            Date dueDate = selectedDueDate != null ? dateFormat.parse(selectedDueDate) : new Date();

            Debt newDebt = new Debt(
                    String.valueOf(System.currentTimeMillis()),
                    customerName,
                    amount,
                    description,
                    dueDate,
                    DebtStatus.PENDING
            );

            // Add items if any
            if (!itemsToAdd.isEmpty()) {
                newDebt.setItems(new ArrayList<>(itemsToAdd));
            }

            allDebts.add(0, newDebt);
            hideAddForm();
            updateStatistics();
            filterAndUpdateList();

            Toast.makeText(this, "Debt saved successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPaymentDialog(Debt debt) {
        // TODO: Implement payment dialog
        // For now, just mark as paid
        debt.setStatus(DebtStatus.PAID);
        updateStatistics();
        debtAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Payment recorded for " + debt.getCustomerName(), Toast.LENGTH_SHORT).show();
    }

    private void showQuickPaymentDialog() {
        // TODO: Implement quick payment dialog
        Toast.makeText(this, "Quick payment feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void updateStatistics() {
        double totalOverdue = 0.0;
        double totalPending = 0.0;

        for (Debt debt : allDebts) {
            switch (debt.getStatus()) {
                case OVERDUE:
                    totalOverdue += debt.getAmount();
                    break;
                case PENDING:
                    totalPending += debt.getAmount();
                    break;
            }
        }

        tvTotalOverdue.setText(currencyFormat.format(totalOverdue).replace("PHP", "₱"));
        tvTotalPending.setText(currencyFormat.format(totalPending).replace("PHP", "₱"));
    }

    private void updateDebtList() {
        filteredDebts.clear();
        filteredDebts.addAll(allDebts);
        debtAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK) {
            // Debt was updated in details activity, refresh the list
            updateStatistics();
            filterAndUpdateList();
        }
    }
}