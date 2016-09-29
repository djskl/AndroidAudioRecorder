package cnic.sdc.androidaudiorecorder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cnic.sdc.androidaudiorecorder.Audios.AudioItem;

public class MyAudioItemRecyclerViewAdapter extends RecyclerView.Adapter<MyAudioItemRecyclerViewAdapter.ViewHolder> {

    private final List<AudioItem> mValues;
    private final AudioItemFragment.OnListFragmentInteractionListener mListener;

    public MyAudioItemRecyclerViewAdapter(List<AudioItem> items, AudioItemFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_audioitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mNameView.setText(mValues.get(position).audio.getName());
        holder.mCtimeView.setText(mValues.get(position).getCtime());
        holder.mDuartionView.setText(mValues.get(position).getDuration());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mCtimeView;
        public final TextView mDuartionView;

        public AudioItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.audio_name);
            mCtimeView = (TextView) view.findViewById(R.id.audio_ctime);
            mDuartionView = (TextView) view.findViewById(R.id.audio_duration);
        }
    }
}
