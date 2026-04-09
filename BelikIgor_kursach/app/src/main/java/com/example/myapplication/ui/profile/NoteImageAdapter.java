package com.example.myapplication.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class NoteImageAdapter extends RecyclerView.Adapter<NoteImageAdapter.Holder> {
    public interface Listener {
        void onImageClick(String path);
    }

    private final List<String> paths = new ArrayList<>();
    private final Listener listener;

    public NoteImageAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setPaths(List<String> list) {
        paths.clear();
        if (list != null) paths.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note_image, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        String path = paths.get(position);
        Bitmap bmp = decodeSampled(path, 280, 280);
        holder.image.setImageBitmap(bmp);
        holder.itemView.setOnClickListener(v -> listener.onImageClick(path));
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

    private static Bitmap decodeSampled(String path, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView image;

        Holder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_thumb);
        }
    }
}
