package com.example.Cards;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Cards.Utils.CardDetail;

import java.util.ArrayList;

public class CardsRecyclerAdapter extends RecyclerView.Adapter<CardsRecyclerAdapter.CardHolder> {

    ArrayList<CardDetail> cards;
    Context context;
    OnItemClickListener mListener;

    public CardsRecyclerAdapter(ArrayList<CardDetail> cards, Context context) {
        this.cards = cards;
        this.context = context;
    }

    @NonNull
    @Override
    public CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater li =LayoutInflater.from(context);
        View view =li.inflate(R.layout.recycler_layout, parent, false);
        return new CardHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CardHolder holder, int position) {
        final CardDetail c = cards.get(position);
        holder.cardImage.setImageResource(c.getCardImage());
        /*holder.cardImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, c.getCardName(), Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    class CardHolder extends RecyclerView.ViewHolder {

        ImageView cardImage;

        public CardHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            cardImage = itemView.findViewById(R.id.cardImage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.OnItemClick(position);
                            Log.d("Return", "onClick: returned "+position);
                        }
                    }
                }
            });
        }
    }

    public interface OnItemClickListener{
        void OnItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

}
