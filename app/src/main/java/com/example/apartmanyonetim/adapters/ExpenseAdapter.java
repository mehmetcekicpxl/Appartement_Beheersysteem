package com.example.apartmanyonetim.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apartmanyonetim.R;
import com.example.apartmanyonetim.models.Transaction; // Assuming Transaction model exists
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<Transaction> expenseList;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Transaction expense);
    }

    public ExpenseAdapter(List<Transaction> expenseList, OnItemClickListener listener) {
        this.expenseList = expenseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction expense = expenseList.get(position);
        
        holder.tvTitle.setText(expense.getDescription());
        holder.tvAmount.setText(String.format("%.2f TL", expense.getAmount()));
        holder.tvDate.setText(expense.getDate());
        holder.tvType.setText(expense.getCategory());
        
        holder.itemView.setOnClickListener(v -> listener.onItemClick(expense));
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvAmount;
        TextView tvDate;
        TextView tvType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvExpenseTitle);
            tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvDate = itemView.findViewById(R.id.tvExpenseDate);
            tvType = itemView.findViewById(R.id.tvExpenseType);
        }
    }
}
