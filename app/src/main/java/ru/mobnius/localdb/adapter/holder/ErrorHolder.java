package ru.mobnius.localdb.adapter.holder;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.storage.ClientErrors;

public class ErrorHolder extends RecyclerView.ViewHolder{
    private final TextView tvErrorText;
    private final Button btnSendError;
    private final Context mContext;

    public ErrorHolder(@NonNull View itemView, Context context) {
        super(itemView);
        mContext = context;
        tvErrorText = itemView.findViewById(R.id.tv_error);
        btnSendError = itemView.findViewById(R.id.btn_send_error);
    }

    public void bind(ClientErrors clientErrors) {
        String errorMessage = clientErrors.id + "\n" +
                clientErrors.date + "\n" +
                clientErrors.message;
        tvErrorText.setText(errorMessage);
        btnSendError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (Intent.ACTION_SEND);
                intent.setType ("plain/text");
                intent.putExtra (Intent.EXTRA_EMAIL, new String[] {"a-slatinin@it-serv.ru"});
                intent.putExtra (Intent.EXTRA_SUBJECT, "LocalDB error");
                intent.putExtra (Intent.EXTRA_TEXT, errorMessage); // do this so some email clients don't complain about empty body.
                mContext.startActivity (intent);
            }
        });
    }
}
