package com.example.blottermanagementsystem.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.example.blottermanagementsystem.R;

public class LoadingDialog {
    
    private Dialog dialog;
    private TextView tvLoadingMessage, tvLoadingSubtitle;
    
    public LoadingDialog(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        dialog.setContentView(view);
        
        tvLoadingMessage = view.findViewById(R.id.tvLoadingMessage);
        tvLoadingSubtitle = view.findViewById(R.id.tvLoadingSubtitle);
        
        // Make dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        
        dialog.setCancelable(false);
    }
    
    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }
    
    public void show(String message) {
        setMessage(message);
        show();
    }
    
    public void show(String message, String subtitle) {
        setMessage(message);
        setSubtitle(subtitle);
        show();
    }
    
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    
    public void setMessage(String message) {
        if (tvLoadingMessage != null) {
            tvLoadingMessage.setText(message);
        }
    }
    
    public void setSubtitle(String subtitle) {
        if (tvLoadingSubtitle != null) {
            tvLoadingSubtitle.setText(subtitle);
            tvLoadingSubtitle.setVisibility(View.VISIBLE);
        }
    }
    
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}
