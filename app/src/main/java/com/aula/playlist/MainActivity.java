package com.aula.playlist;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.playlist.adapter.MusicaAdapter;
import com.aula.playlist.model.Musica;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements MusicaAdapter.OnMusicaClickListener {

    private static final String TAG = "PlayList";
    private static final String COLECAO = "playlist";

    private String meuNome;

    private CollectionReference playlistRef;
    private ListenerRegistration listenerPlaylist;
    private MusicaAdapter adapter;
    private RecyclerView listaMusicas;
    private TextView txtVazio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Carregar dados do usuario
        carregarNome();

        // Ação do botão de adicionar
        FloatingActionButton fab = findViewById(R.id.fabAdicionar);
        fab.setOnClickListener(v -> mostrarDialogoNovaMusica());


        // Adicionar SUA IMPLEMENTAÇÃO AQUI
        playlistRef = FirebaseFirestore.getInstance().collection(COLECAO);

        txtVazio = findViewById(R.id.txtVazio);
        listaMusicas = findViewById(R.id.listaMusicas);
        listaMusicas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MusicaAdapter(this);
        listaMusicas.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ouvirPlaylist();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerPlaylist != null) {
            listenerPlaylist.remove();
            listenerPlaylist = null;
        }
    }

    private void ouvirPlaylist() {
        listenerPlaylist = playlistRef
                .orderBy("votos", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, erro) -> {
                    if (erro != null) {
                        Log.e(TAG, "Erro ao ouvir a playlist", erro);
                        return;
                    }
                    if (snapshots == null) {
                        return;
                    }

                    List<Musica> musicas = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Musica musica = doc.toObject(Musica.class);
                        if (musica != null) {
                            musicas.add(musica);
                        }
                    }

                    adapter.setMusicas(musicas);
                    atualizarMensagemVazia(musicas.isEmpty());
                });
    }

    private void atualizarMensagemVazia(boolean vazia) {
        txtVazio.setVisibility(vazia ? View.VISIBLE : View.GONE);
        listaMusicas.setVisibility(vazia ? View.GONE : View.VISIBLE);
    }

    private void adicionarMusica(String titulo, String artista) {
        Musica musica = new Musica(titulo, artista, meuNome);
        playlistRef.add(musica)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao adicionar música", e);
                    Toast.makeText(this, "Erro ao adicionar música", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onVotar(Musica musica) {
        if (musica.getId() == null) {
            return;
        }
        playlistRef.document(musica.getId())
                .update("votos", FieldValue.increment(1))
                .addOnFailureListener(e -> Log.e(TAG, "Erro ao votar", e));
    }

    @Override
    public void onExcluir(Musica musica) {
        if (musica.getIndicadoPor() == null || !musica.getIndicadoPor().equals(meuNome)) {
            Toast.makeText(this, "Essa música é de " + musica.getIndicadoPor(), Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Excluir")
                .setMessage("Tirar \"" + musica.getTitulo() + "\" da playlist?")
                .setNegativeButton("Não", null)
                .setPositiveButton("Excluir", (d, w) -> excluirMusica(musica))
                .show();
    }

    private void excluirMusica(Musica musica) {
        if (musica.getId() == null) {
            return;
        }
        playlistRef.document(musica.getId())
                .delete()
                .addOnSuccessListener(unused ->
                        Snackbar.make(findViewById(R.id.main), "Música removida", Snackbar.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao excluir música", e);
                    Toast.makeText(this, "Erro ao excluir música", Toast.LENGTH_SHORT).show();
                });
    }


    private void carregarNome() {
        SharedPreferences localNome = getSharedPreferences("nomeUsuario", MODE_PRIVATE);
        meuNome = localNome.getString("nomeUsuario", null);

        if (meuNome != null && !meuNome.equals("")) {
            Toast.makeText(this, "Bem-vindo(a) " + meuNome, Toast.LENGTH_LONG).show();
            return;
        }

        // Ativar a caixa de texto para digitar o nome
        EditText campo = new EditText(this);
        campo.setHint("Seu nome");

        new AlertDialog.Builder(this)
                .setTitle("Quem é você?")
                .setMessage("Vai aparecer do lado das músicas que você indicar.")
                .setView(campo)
                .setCancelable(false)
                .setPositiveButton("Pronto",(dialogInterface, i) -> {
                    meuNome = campo.getText().toString().trim();
                    localNome.edit().putString("nomeUsuario", meuNome).apply();

                }).show();

    }


    private void mostrarDialogoNovaMusica() {
        View form = LayoutInflater.from(this).inflate(R.layout.tela_musica, null);
        EditText campoTitulo = form.findViewById(R.id.campoTitulo);
        EditText campoArtista = form.findViewById(R.id.campoArtista);

        new AlertDialog.Builder(this)
                .setTitle("Indicar música")
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Adicionar", (d, w) -> {
                    String titulo = campoTitulo.getText().toString().trim();
                    String artista = campoArtista.getText().toString().trim();

                    if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(artista)) {
                        Toast.makeText(this, "Preencha título e artista", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    adicionarMusica(titulo, artista);
                })
                .show();
    }
}
