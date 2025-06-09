package com.nnthienphuc.bookdownloaderapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nnthienphuc.bookdownloaderapp.BookDetailActivity;
import com.nnthienphuc.bookdownloaderapp.R;
import com.nnthienphuc.bookdownloaderapp.models.Book;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    private final Context context;
    private final List<Book> bookList;
    private final String mode;
    private final OnBookClickListener clickListener;

    public BookAdapter(Context context, List<Book> bookList, String mode, OnBookClickListener listener) {
        this.context = context;
        this.bookList = bookList;
        this.mode = mode;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book_compact, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.title.setText(book.getTitle());
        holder.author.setText("Tác giả: " + book.getAuthor());
        holder.size.setText("Kích thước: " + formatSize(book.getSize()));
        holder.uploader.setText("Upload bởi: " + book.getUploader());

        if (book.getThumbnailUrl() != null && !book.getThumbnailUrl().isEmpty()) {
            Glide.with(context).load(book.getThumbnailUrl()).into(holder.thumbnail);
        } else {
            holder.thumbnail.setImageResource(R.drawable.ic_books);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK, book);

            // mode truyền logic: nếu adapter mode là "delete" thì vào edit mode
            if ("delete".equals(mode)) {
                intent.putExtra(BookDetailActivity.EXTRA_MODE, "delete");
            } else {
                intent.putExtra(BookDetailActivity.EXTRA_MODE, mode);
            }

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView title, author, size, uploader;
        ImageView thumbnail;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.bookTitle);
            author = itemView.findViewById(R.id.bookAuthor);
            size = itemView.findViewById(R.id.bookSize);
            uploader = itemView.findViewById(R.id.bookUploader);
            thumbnail = itemView.findViewById(R.id.bookThumbnail);
        }
    }

    private String formatSize(long bytes) {
        return String.format("%.2f MB", bytes / 1024f / 1024f);
    }
}
