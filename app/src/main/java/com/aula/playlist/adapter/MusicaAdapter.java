package com.aula.playlist.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.playlist.R;
import com.aula.playlist.model.Musica;

import java.util.ArrayList;
import java.util.List;

public class MusicaAdapter extends RecyclerView.Adapter<MusicaAdapter.MusicaViewHolder> {

    public interface OnMusicaClickListener {
        void onVotar(Musica musica);

        void onExcluir(Musica musica);
    }

    private final List<Musica> musicas = new ArrayList<>();
    private final OnMusicaClickListener listener;

    public MusicaAdapter(OnMusicaClickListener listener) {
        this.listener = listener;
    }

    public void setMusicas(List<Musica> novasMusicas) {
        musicas.clear();
        musicas.addAll(novasMusicas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MusicaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_musica, parent, false);
        return new MusicaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicaViewHolder holder, int position) {
        Musica musica = musicas.get(position);

        holder.txtTitulo.setText(musica.getTitulo());
        holder.txtArtista.setText(musica.getArtista());
        holder.txtIndicadoPor.setText("indicada por " + musica.getIndicadoPor());
        holder.txtVotos.setText(String.valueOf(musica.getVotos()));

        holder.btnVotar.setOnClickListener(v -> listener.onVotar(musica));
        holder.btnExcluir.setOnClickListener(v -> listener.onExcluir(musica));
    }

    @Override
    public int getItemCount() {
        return musicas.size();
    }

    static class MusicaViewHolder extends RecyclerView.ViewHolder {
        final TextView txtTitulo;
        final TextView txtArtista;
        final TextView txtIndicadoPor;
        final TextView txtVotos;
        final ImageButton btnVotar;
        final ImageButton btnExcluir;

        MusicaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtArtista = itemView.findViewById(R.id.txtArtista);
            txtIndicadoPor = itemView.findViewById(R.id.txtIndicadoPor);
            txtVotos = itemView.findViewById(R.id.txtVotos);
            btnVotar = itemView.findViewById(R.id.btnVotar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }
    }
}
