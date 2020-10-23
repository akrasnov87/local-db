package ru.mobnius.localdb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.adapter.holder.ErrorHolder;
import ru.mobnius.localdb.storage.ClientErrors;

public class ErrorsAdapter extends RecyclerView.Adapter<ErrorHolder>  {

    private final List<ClientErrors> mErrorsList;

    public ErrorsAdapter(List<ClientErrors> clientErrors){
        mErrorsList = clientErrors;
    }

    @NonNull
    @Override
    public ErrorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_error, parent, false);
        return new ErrorHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull ErrorHolder holder, int position) {
        holder.bind(mErrorsList.get(position));
    }

    @Override
    public int getItemCount() {
        return mErrorsList.size();
    }
}
