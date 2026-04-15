package com.example.studentprofile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.studentprofile.R;
import com.example.studentprofile.model.Note;

import java.util.ArrayList;
import java.util.Locale;

public class NoteAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final ArrayList<Note> notes;

    public NoteAdapter(Context context, ArrayList<Note> notes) {
        this.inflater = LayoutInflater.from(context);
        this.notes = notes;
    }

    @Override
    public int getCount() {
        return notes.size();
    }

    @Override
    public Object getItem(int position) {
        return notes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_note, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Note note = notes.get(position);
        holder.tvSubject.setText(note.getSubject());
        holder.tvScore.setText(String.format(Locale.getDefault(), "%.1f / 20", note.getScore()));
        holder.ivStatus.setImageResource(note.isGood()
                ? android.R.drawable.presence_online
                : android.R.drawable.presence_busy);

        return convertView;
    }

    private static class ViewHolder {
        final ImageView ivStatus;
        final TextView tvSubject;
        final TextView tvScore;

        ViewHolder(View root) {
            ivStatus = root.findViewById(R.id.ivStatus);
            tvSubject = root.findViewById(R.id.tvSubject);
            tvScore = root.findViewById(R.id.tvScore);
        }
    }
}
