package com.example.apartmanyonetim.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apartmanyonetim.R;
import com.example.apartmanyonetim.models.FileItem;
import java.util.List;

// Adapter voor de lijst met bestanden
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private List<FileItem> fileList;
    private OnItemClickListener listener;
    private OnItemDeleteListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(FileItem fileItem);
    }
    
    public interface OnItemDeleteListener {
        void onItemDelete(FileItem fileItem);
    }

    public FileAdapter(List<FileItem> fileList, OnItemClickListener listener, OnItemDeleteListener deleteListener) {
        this.fileList = fileList;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem item = fileList.get(position);
        holder.tvFileName.setText(item.getName() != null ? item.getName() : "Dosya");
        
        // Icon logic (simpel)
        if (item.getType() != null && item.getType().contains("pdf")) {
            holder.ivFileIcon.setImageResource(android.R.drawable.ic_menu_agenda); // Placeholder PDF icon
        } else if (item.getType() != null && item.getType().contains("image")) {
             holder.ivFileIcon.setImageResource(android.R.drawable.ic_menu_gallery); // Placeholder Image icon
             // Eventueel echte image laden als URI werkt
             try {
                 holder.ivFileIcon.setImageURI(android.net.Uri.parse(item.getUri()));
             } catch (Exception e) {}
        } else {
             holder.ivFileIcon.setImageResource(android.R.drawable.ic_menu_upload);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onItemDelete(item));
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName;
        ImageView ivFileIcon;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            ivFileIcon = itemView.findViewById(R.id.ivFileIcon);
            btnDelete = itemView.findViewById(R.id.btnDeleteFile);
        }
    }
}
