package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.example.blottermanagementsystem.R;

/**
 * Utility class to programmatically create empty state UI
 * Builds empty state content entirely in Java without XML dependency
 */
public class EmptyStateBuilder {

    private Context context;
    private ViewGroup container;
    private CardView emptyStateCard;
    private LinearLayout emptyStateContent;
    private ImageView emptyStateIcon;
    private TextView emptyStateTitle;
    private TextView emptyStateMessage;

    public EmptyStateBuilder(Context context, android.view.View containerView) {
        this.context = context;
        this.container = (ViewGroup) containerView;
    }

    /**
     * Build empty state UI programmatically
     */
    public void buildEmptyState(String title, String message, int iconDrawableId) {
        if (container == null) return;

        // Clear existing content
        container.removeAllViews();

        // Create CardView as main container
        emptyStateCard = new CardView(context);
        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        cardParams.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        emptyStateCard.setLayoutParams(cardParams);
        emptyStateCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background));
        emptyStateCard.setRadius(dpToPx(24));
        emptyStateCard.setCardElevation(dpToPx(8));

        // Create FrameLayout as inner container
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setLayoutParams(new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.MATCH_PARENT
        ));

        // Create LinearLayout for empty state content
        emptyStateContent = new LinearLayout(context);
        emptyStateContent.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        emptyStateContent.setOrientation(LinearLayout.VERTICAL);
        emptyStateContent.setGravity(android.view.Gravity.CENTER);
        emptyStateContent.setPadding(
                dpToPx(48), dpToPx(48),
                dpToPx(48), dpToPx(48)
        );
        emptyStateContent.setVisibility(View.VISIBLE);

        // Create CardView for icon background
        CardView iconCard = new CardView(context);
        LinearLayout.LayoutParams iconCardParams = new LinearLayout.LayoutParams(
                dpToPx(120), dpToPx(120)
        );
        iconCardParams.setMargins(0, dpToPx(20), 0, 0);
        iconCard.setLayoutParams(iconCardParams);
        iconCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.electric_blue));
        iconCard.setRadius(dpToPx(60));
        iconCard.setCardElevation(dpToPx(8));

        // Create ImageView for icon
        emptyStateIcon = new ImageView(context);
        emptyStateIcon.setLayoutParams(new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.MATCH_PARENT
        ));
        emptyStateIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        emptyStateIcon.setPadding(dpToPx(30), dpToPx(30), dpToPx(30), dpToPx(30));
        emptyStateIcon.setImageResource(iconDrawableId);
        iconCard.addView(emptyStateIcon);

        emptyStateContent.addView(iconCard);

        // Create title TextView
        emptyStateTitle = new TextView(context);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, dpToPx(24), 0, 0);
        emptyStateTitle.setLayoutParams(titleParams);
        emptyStateTitle.setText(title);
        emptyStateTitle.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        emptyStateTitle.setTextSize(22);
        emptyStateTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        emptyStateContent.addView(emptyStateTitle);

        // Create message TextView
        emptyStateMessage = new TextView(context);
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        messageParams.setMargins(0, dpToPx(8), 0, 0);
        emptyStateMessage.setLayoutParams(messageParams);
        emptyStateMessage.setText(message);
        emptyStateMessage.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        emptyStateMessage.setTextSize(14);
        emptyStateMessage.setGravity(android.view.Gravity.CENTER);
        emptyStateMessage.setLineSpacing(dpToPx(4), 1.0f);
        emptyStateContent.addView(emptyStateMessage);

        // Create decorative line
        View decorativeLine = new View(context);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
                dpToPx(60), dpToPx(4)
        );
        lineParams.setMargins(0, dpToPx(20), 0, 0);
        decorativeLine.setLayoutParams(lineParams);
        decorativeLine.setBackgroundColor(ContextCompat.getColor(context, R.color.electric_blue));
        decorativeLine.setAlpha(0.8f);
        emptyStateContent.addView(decorativeLine);

        // Add content to frame layout
        frameLayout.addView(emptyStateContent);

        // Add frame layout to card view
        emptyStateCard.addView(frameLayout);

        // Add card view to container
        container.addView(emptyStateCard);
    }

    /**
     * Show empty state
     */
    public void show() {
        if (emptyStateCard != null) {
            emptyStateCard.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hide empty state
     */
    public void hide() {
        if (emptyStateCard != null) {
            emptyStateCard.setVisibility(View.GONE);
        }
    }

    /**
     * Update title and message
     */
    public void updateContent(String title, String message) {
        if (emptyStateTitle != null) {
            emptyStateTitle.setText(title);
        }
        if (emptyStateMessage != null) {
            emptyStateMessage.setText(message);
        }
    }

    /**
     * Convert dp to pixels
     */
    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}
