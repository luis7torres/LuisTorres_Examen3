package com.example.luistorres_examen3.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.luistorres_examen3.ActivityEscuchar;
import com.example.luistorres_examen3.MainActivity;
import com.example.luistorres_examen3.R;
import com.example.luistorres_examen3.model.Entrevista;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class EntrevistaAdapter extends FirestoreRecyclerAdapter<Entrevista, EntrevistaAdapter.ViewHolder> {

    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    Activity activity;
    FragmentManager fm;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public EntrevistaAdapter(@NonNull FirestoreRecyclerOptions<Entrevista> options, Activity activity, FragmentManager fm) {
        super(options);

        this.activity = activity;
        this.fm = fm;
    }


    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Entrevista model) {

        DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
        final String id = documentSnapshot.getId();

        holder.periodista.setText(model.getPeriodista());
        holder.fecha.setText(model.getFecha());
        String photoPersona = model.getPhoto();

        try {

            if (photoPersona != null && !photoPersona.isEmpty()) {
                Glide.with(activity.getApplicationContext())
                        .load(photoPersona)
                        .override(150, 150) //Especifica el ancho y alto deseados
                        .into(holder.photo_persona);
            } else {
                // Manejar caso de imagen vacía o nula
            }
        }catch (Exception e){
            Log.d("Exception", "e: "+e);
        }


        //Evento para el boton de eliminar
        holder.btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                eliminarContacto(id);

            }
        });

        //Evento para el boton de editar
        holder.btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra("id_entrevista", id);
                activity.startActivity(intent);
            }
        });

        //Evento para el boton de Entrevista
        holder.btnEntrevista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(activity, ActivityEscuchar.class);
                intent.putExtra("id_entrevista", id);
                activity.startActivity(intent);
            }
        });


    }

    //Metodo para eliminar el contacto
    private void eliminarContacto(String id) {

        mFirestore.collection("entrevista").document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                Toast.makeText(activity, "Eliminado exitosamente!", Toast.LENGTH_LONG).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(activity, "Error al eliminar!", Toast.LENGTH_LONG).show();

            }
        });

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_lista, parent, false);
        return new ViewHolder(v);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView periodista, fecha;
        ImageView btnEliminar, btnEditar, btnEntrevista, photo_persona;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            periodista = itemView.findViewById(R.id.periodista);
            fecha = itemView.findViewById(R.id.fecha);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
            btnEditar = itemView.findViewById(R.id.btn_editar);
            btnEntrevista = itemView.findViewById(R.id.btn_entrevista);
            photo_persona = itemView.findViewById(R.id.photo);
        }
    }

}
