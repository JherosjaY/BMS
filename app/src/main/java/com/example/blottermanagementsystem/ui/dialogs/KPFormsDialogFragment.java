package com.example.blottermanagementsystem.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.ui.adapters.KPFormAdapter;
import com.example.blottermanagementsystem.ui.adapters.KPFormSimpleAdapter;
import com.example.blottermanagementsystem.utils.ApiClient;
import com.example.blottermanagementsystem.utils.NetworkMonitor;

import java.util.ArrayList;
import java.util.List;

public class KPFormsDialogFragment extends DialogFragment {

    private RecyclerView recyclerForms;
    private int reportId;
    private OnFormSelectedListener listener;

    public interface OnFormSelectedListener {
        void onFormSelected(String formName, int formId);
    }

    public static KPFormsDialogFragment newInstance(int reportId, OnFormSelectedListener listener) {
        KPFormsDialogFragment fragment = new KPFormsDialogFragment();
        Bundle args = new Bundle();
        args.putInt("report_id", reportId);
        fragment.setArguments(args);
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use transparent background to show the MaterialCardView properly
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Dialog_MinWidth);
        if (getArguments() != null) {
            reportId = getArguments().getInt("report_id");
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        // Make dialog background transparent to show MaterialCardView
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Set dialog to match parent width with padding
            android.view.WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            getDialog().getWindow().setAttributes(params);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_kp_forms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
    }

    private void initViews(View view) {
        recyclerForms = view.findViewById(R.id.recyclerKPForms);
    }

    private void setupRecyclerView() {
        recyclerForms.setLayoutManager(new LinearLayoutManager(getContext()));
        
        List<KPFormItem> items = getKPForms();
        KPFormSimpleAdapter adapter = new KPFormSimpleAdapter(items, item -> {
            if (listener != null) {
                listener.onFormSelected(item.name, item.id);
            }
            dismiss();
        });
        recyclerForms.setAdapter(adapter);
    }

    private List<KPFormItem> getKPForms() {
        List<KPFormItem> forms = new ArrayList<>();
        forms.add(new KPFormItem(1, "KP Form 1", "Complaint Form"));
        forms.add(new KPFormItem(7, "KP Form 7", "Summons"));
        forms.add(new KPFormItem(16, "KP Form 16", "Amicable Settlement"));
        forms.add(new KPFormItem(0, "Certification", "Certification to File Action"));
        return forms;
    }

    public static class KPFormItem {
        public int id;
        public String name;
        public String description;

        public KPFormItem(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }
}
