package com.example.luistorres_examen3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;

public class ActivityEscuchar extends AppCompatActivity {

    Button btnPlay, btnRegresar;
    ImageView imageInterview;
    FirebaseFirestore mFirestore;
    String audioToPlay, id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escuchar);

        mFirestore = FirebaseFirestore.getInstance();
        String id = getIntent().getStringExtra("id_entrevista");

        btnPlay = (Button) findViewById(R.id.btn_play2);
        btnRegresar = (Button) findViewById(R.id.btnRegresar);
        imageInterview = (ImageView) findViewById(R.id.fotoEntrevista);

        //Evento para el boton Regresar
        btnRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), activity_lista_registros.class);
                startActivity(intent);
            }
        });


        getEntrevista(id);

        //Evento para el boton Play
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                reproducirAudio(view);
            }
        });

    }

    private void getEntrevista(String id) {


        mFirestore.collection("entrevista").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                // Obtener los campos necesarios
                String descripcion2 = documentSnapshot.getString("descripcion");
                String periodista2 = documentSnapshot.getString("periodista");
                String fecha2 = documentSnapshot.getString("fecha");
                String photoPersona = documentSnapshot.getString("photo");
                String audioUrl = documentSnapshot.getString("audio"); // Obtener URL del audio

                /*descripcion.setText(descripcion2);
                periodista.setText(periodista2);
                fecha.setText(fecha2);*/

                try {
                    // Cargar imagen si existe
                    if (photoPersona != null && !photoPersona.isEmpty()) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Cargando foto", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 200);
                        toast.show();

                        Glide.with(ActivityEscuchar.this)
                                .load(photoPersona)
                                //.override(150, 150)
                                .into(imageInterview);
                    } else {
                        // Manejar caso de imagen vacía o nula
                    }

                    // Guardar la URL del audio en un campo global para reproducir
                    audioToPlay = audioUrl;

                } catch (Exception e) {
                    Log.d("Exception", "e: " + e);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error al obtener los datos!", Toast.LENGTH_LONG).show();
            }
        });

    }

    // Método para reproducir el audio del registro actual
    public void reproducirAudio(View view) {
        if (audioToPlay != null && !audioToPlay.isEmpty()) {
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(audioToPlay);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.start();
            Toast.makeText(getApplicationContext(), "Reproduciendo audio", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "No hay audio para reproducir", Toast.LENGTH_SHORT).show();
        }
    }

}