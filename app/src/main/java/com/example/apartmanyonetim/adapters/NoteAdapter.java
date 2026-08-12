package com.example.apartmanyonetim.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apartmanyonetim.R;
import com.example.apartmanyonetim.models.Note;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    private List<Note> noteList;
    private OnNoteDeleteListener deleteListener;

    public interface OnNoteDeleteListener {
        void onDelete(Note note);
    }

    public NoteAdapter(List<Note> noteList, OnNoteDeleteListener deleteListener) {
        this.noteList = noteList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = noteList.get(position);
        
        holder.tvContent.setText(note.getContent());
        
        // Datum converteren van timestamp
        try {
            long timestamp = Long.parseLong(note.getDate());
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            holder.tvDate.setText(sdf.format(new Date(timestamp)));
        } catch (NumberFormatException e) {
            holder.tvDate.setText(note.getDate());
        }

        holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(note));
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        TextView tvDate;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvNoteContent);
            tvDate = itemView.findViewById(R.id.tvNoteDate);
            btnDelete = itemView.findViewById(R.id.btnDeleteNote);
        }
    }
}
