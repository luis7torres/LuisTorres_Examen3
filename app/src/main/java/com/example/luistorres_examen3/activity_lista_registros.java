package com.example.luistorres_examen3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.SearchView;

import com.example.luistorres_examen3.adapter.EntrevistaAdapter;
import com.example.luistorres_examen3.model.Entrevista;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class activity_lista_registros extends AppCompatActivity {

    RecyclerView recyclerView;
    EntrevistaAdapter mAdpater;
    FirebaseFirestore mFirestore;
    SearchView searchView;
    Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_registros);

        mFirestore = FirebaseFirestore.getInstance();
        searchView = (SearchView) findViewById(R.id.search);

        setUpRecyclerView();
        search_view();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setUpRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        query = mFirestore.collection("entrevista");

        FirestoreRecyclerOptions<Entrevista> firestoreRecyclerOptions =
                new FirestoreRecyclerOptions.Builder<Entrevista>().setQuery(query, Entrevista.class).build();

        mAdpater = new EntrevistaAdapter(firestoreRecyclerOptions, this, getSupportFragmentManager());
        mAdpater.notifyDataSetChanged();
        recyclerView .setAdapter(mAdpater);
    }

    //Metodo para buscar registros
    private void search_view() {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                textSearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                textSearch(s);
                return false;
            }
        });
    }

    public void textSearch(String s){
        //Query query = mFirestore.collection( "contacto");
        FirestoreRecyclerOptions<Entrevista> firestoreRecyclerOptions =
                new FirestoreRecyclerOptions.Builder<Entrevista>()
                        .setQuery(query.orderBy("periodista")
                                .startAt(s).endAt(s+"~"), Entrevista.class).build();
        mAdpater = new EntrevistaAdapter(firestoreRecyclerOptions, this, getSupportFragmentManager());
        mAdpater.startListening();
        recyclerView.setAdapter(mAdpater);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAdpater.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdpater.stopListening();
    }
}