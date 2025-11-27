package com.example.blottermanagementsystem.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.Hearing;
import com.example.blottermanagementsystem.ui.adapters.HearingListAdapter;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ViewHearingsDialogFragment extends DialogFragment {
    
    private int reportId;
    private RecyclerView rvHearingList;
    private TextView tvEmptyState;
    private MaterialCardView cardContainer;
    private HearingListAdapter adapter;
    private List<Hearing> hearingList = new ArrayList<>();
    
    public static ViewHearingsDialogFragment newInstance(int reportId) {
        ViewHearingsDialogFragment fragment = new ViewHearingsDialogFragment();
        Bundle args = new Bundle();
        args.putInt("report_id", reportId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Dialog_MinWidth);
        if (getArguments() != null) {
            reportId = getArguments().getInt("report_id");
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_view_hearings, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        loadHearings();
    }
    
    private void initViews(View view) {
        cardContainer = view.findViewById(R.id.cardHearingContainer);
        rvHearingList = view.findViewById(R.id.rvHearingList);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        
        rvHearingList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HearingListAdapter(hearingList, true); // true = read-only
        rvHearingList.setAdapter(adapter);
    }
    
    private void loadHearings() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                BlotterDatabase database = BlotterDatabase.getDatabase(getContext());
                if (database != null) {
                    hearingList.clear();
                    hearingList.addAll(database.hearingDao().getHearingsByReport(reportId));
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (hearingList.isEmpty()) {
                                tvEmptyState.setVisibility(View.VISIBLE);
                                rvHearingList.setVisibility(View.GONE);
                            } else {
                                tvEmptyState.setVisibility(View.GONE);
                                rvHearingList.setVisibility(View.VISIBLE);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("ViewHearings", "Error loading hearings: " + e.getMessage());
            }
        });
    }
}
