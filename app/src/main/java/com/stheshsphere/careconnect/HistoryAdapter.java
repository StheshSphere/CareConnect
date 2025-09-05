package com.stheshsphere.careconnect;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HistoryItem> historyItems;

    public HistoryAdapter(List<HistoryItem> historyItems) {
        this.historyItems = historyItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = historyItems.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvDate.setText(item.getDate());
        holder.tvStatus.setText(item.getStatus());
        holder.ivIcon.setImageResource(item.getIconResId());

        // Set different status colors
        if ("Completed".equals(item.getStatus())) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.completed_green));
        } else {
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.in_progress_yellow));
        }

        holder.itemView.setOnClickListener(v -> {
            // Handle item click - show details
            Toast.makeText(v.getContext(), "Showing details for: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvDate;
        TextView tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivHistoryType);
            tvTitle = itemView.findViewById(R.id.tvHistoryTitle);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvStatus = itemView.findViewById(R.id.tvHistoryStatus);
        }
    }
}