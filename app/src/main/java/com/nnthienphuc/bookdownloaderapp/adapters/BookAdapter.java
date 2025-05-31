package com.nnthienphuc.bookdownloaderapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nnthienphuc.bookdownloaderapp.BookDetailActivity;
import com.nnthienphuc.bookdownloaderapp.R;
import com.nnthienphuc.bookdownloaderapp.models.Book;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final Context context;
    private final List<Book> bookList;
    private final String mode; // "download", "read", "delete"

    public BookAdapter(Context context, List<Book> bookList, String mode) {
        this.context = context;
        this.bookList = bookList;
        this.mode = mode;
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

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK, book);
            intent.putExtra(BookDetailActivity.EXTRA_MODE, mode);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView title, author, size, uploader;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.bookTitle);
            author = itemView.findViewById(R.id.bookAuthor);
            size = itemView.findViewById(R.id.bookSize);
            uploader = itemView.findViewById(R.id.bookUploader);

        }
    }

    private String formatSize(long bytes) {
        return String.format("%.2f MB", bytes / 1024f / 1024f);
    }
}
