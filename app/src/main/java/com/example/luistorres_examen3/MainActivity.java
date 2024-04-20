package com.example.luistorres_examen3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //Variables para el funcionamiento de Firebase
    private FirebaseFirestore mfirestore;
    private Uri image_url;
    StorageReference storageReference;
    private FirebaseStorage storage;
    String id, image;
    String storage_path = "imagenes/*";
    String storage_path2 = "grabaciones/*";
    String idd;
    String photo = "photo";
    String record = "record";

    //Varibles para los objetos de la app
    ImageView imageView;

    Button btnImgTomarFoto, guardar, registros;
    EditText descripcion, periodista, fecha;

    //Variables para el uso de la camara
    static final  int REQUEST_IMAGE = 101;
    static final  int PETICION_ACCESS_CAM = 201;
    String currentPhotoPath;

    //Variables para la grabacion de audio
    private MediaRecorder grabacion;
    private String archivoSalida = null;
    private Button btn_recorder;


    private boolean estado = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String id = getIntent().getStringExtra("id_entrevista");

        //Obtener la instancia de Firebase Firestore (AQUI ESTAMOS APUNTANDO A LA BASE DE DATOS)
        mfirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        //Inicializacion de cada variable
        descripcion = (EditText) findViewById(R.id.txtDescripcion);
        periodista = (EditText) findViewById(R.id.txtPeriodista);
        fecha = (EditText) findViewById(R.id.txtFecha);
        imageView = (ImageView) findViewById(R.id.fotoView);
        btnImgTomarFoto = (Button) findViewById(R.id.btnFoto);
        guardar = (Button) findViewById(R.id.btnGuardar);
        registros = (Button) findViewById(R.id.btnRegistros);
        btn_recorder = (Button)findViewById(R.id.btn_rec);


        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.RECORD_AUDIO}, 1000);
        }

        //Evento del boton Foto
        btnImgTomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permisos();
            }
        });


        //Evento para el boton Guardar
        if(id == null || id == ""){

            //Evento para el boton Guardar
            guardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String descripcionEntrevista = descripcion.getText().toString().trim();
                    String nombrePeriodista = periodista.getText().toString().trim();
                    String fechaEntrevista = fecha.getText().toString().trim();

                    //Validacion de campos vacios
                    if(descripcionEntrevista.isEmpty() || nombrePeriodista.isEmpty() || fechaEntrevista.isEmpty()){

                        Toast.makeText(getApplicationContext(), "Campos Vacios!", Toast.LENGTH_LONG).show();

                    }else{

                        postEntrevista(descripcionEntrevista, nombrePeriodista, fechaEntrevista);
                        clear();
                    }
                }
            });

        }else{

            idd = id;

            //Actuaizar el nombre del boton
            guardar.setText("Actualizar");

            //Llamamos al metodo de obtener datos
            getEntrevista(id);

            guardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String descripcionEntrevista = descripcion.getText().toString().trim();
                    String nombrePeriodista = periodista.getText().toString().trim();
                    String fechaEntrevista = fecha.getText().toString().trim();

                    //Validacion de campos vacios
                    if(descripcionEntrevista.isEmpty() || nombrePeriodista.isEmpty() || fechaEntrevista.isEmpty()){

                        Toast.makeText(getApplicationContext(), "Campos Vacios!", Toast.LENGTH_LONG).show();

                    }else{

                        updateEntrevista(descripcionEntrevista, nombrePeriodista, fechaEntrevista, id);

                        clear();

                    }
                }
            });
        }


        //Evento del boton Registros
        registros.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), activity_lista_registros.class);
                startActivity(intent);

            }
        });
    }

    //METODOS PARA INGRESAR DATOS A LA BASE DE DATOS EN FIREBASE

    //Metodo para insertar los datos en la base de datos
    private void postEntrevista(String descripcionEntrevista, String nombrePeriodista, String fechaEntrevista) {

        // Verificar si hay una imagen o un audio para subir
        if (image_url != null || archivoSalida != null) {
            // Crear una referencia para la imagen (si existe)
            final StorageReference imageRef = (image_url != null) ? storageReference.child("imagenes/" + System.currentTimeMillis() + ".jpg") : null;
            // Crear una referencia para el audio (si existe)
            final StorageReference audioRef = (archivoSalida != null) ? storageReference.child("grabaciones/" + System.currentTimeMillis() + ".mp3") : null;

            // Lista para almacenar tareas de subida de archivos
            List<Task<Uri>> tasks = new ArrayList<>();

            // Subir la imagen si existe
            if (imageRef != null) {
                tasks.add(imageRef.putFile(image_url).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        return imageRef.getDownloadUrl();
                    }
                }));
            }

            // Subir el audio si existe
            if (audioRef != null) {
                tasks.add(audioRef.putFile(Uri.fromFile(new File(archivoSalida))).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        return audioRef.getDownloadUrl();
                    }
                }));
            }

            // Combinar todas las tareas de subida
            Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                @Override
                public void onSuccess(List<Object> list) {
                    // Obtener las URL de descarga de imagen y audio (si existen)
                    String imageUrl = (imageRef != null && list.get(0) instanceof Uri) ? ((Uri) list.get(0)).toString() : "";
                    String audioUrl = (audioRef != null && list.size() > 1 && list.get(1) instanceof Uri) ? ((Uri) list.get(1)).toString() : "";

                    // Agregar datos de la entrevista a Firestore
                    Map<String, Object> map = new HashMap<>();
                    map.put("descripcion", descripcionEntrevista);
                    map.put("periodista", nombrePeriodista);
                    map.put("fecha", fechaEntrevista);
                    map.put("photo", imageUrl);
                    map.put("audio", audioUrl);

                    mfirestore.collection("entrevista").add(map)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(getApplicationContext(), "Registro Exitoso!", Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Error al ingresar", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        } else {
            Toast.makeText(this, "Debe tomar una fotografía o grabar un audio", Toast.LENGTH_SHORT).show();
        }

    }


    //Metodo para subir la foto

    private void subirPhoto(Uri image_url) {
        String rute_storage_photo = storage_path + "" + photo + "" + idd;
        StorageReference reference = storageReference.child(rute_storage_photo);
        reference.putFile(image_url)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        if (uriTask.isSuccessful()) {
                            uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String download_uri = uri.toString();
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("photo", download_uri);
                                    mfirestore.collection("entrevista").document(idd).update(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(MainActivity.this, "Foto actualizada", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(MainActivity.this, "Error al cargar foto", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error al cargar foto", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    //Metodo para actualizar los datos en la base de datos
    private void updateEntrevista(String descripcionEntrevista, String  nombrePeriodista, String fechaEntrevista, String id) {

        Map<String, Object> map = new HashMap<>();
        map.put("descripcion", descripcionEntrevista );
        map.put("periodista", nombrePeriodista);
        map.put("fecha", fechaEntrevista);

        mfirestore.collection("entrevista").document(id).update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                Toast.makeText(getApplicationContext(), "Registro actualizado!", Toast.LENGTH_LONG).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getApplicationContext(), "Error al actualizar!", Toast.LENGTH_LONG).show();

            }
        });
    }


    //Metodo para obtener toda la informacion al momento de actualizarla
    private void getEntrevista(String id){

        estado = true;

        mfirestore.collection("entrevista").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String descripcion2 = documentSnapshot.getString("descripcion");
                String periodista2 = documentSnapshot.getString("periodista");
                String fecha2 = documentSnapshot.getString("fecha");
                String photoPersona = documentSnapshot.getString("photo");

                descripcion.setText(descripcion2);
                periodista.setText(periodista2);
                fecha.setText(fecha2);

                try {

                    if (photoPersona != null && !photoPersona.isEmpty()) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Cargando foto", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP,0,200);
                        toast.show();

                        Glide.with(MainActivity.this)
                                .load(photoPersona)
                                .override(150, 150) //Especifica el ancho y alto deseados
                                .into(imageView);
                    } else {
                        // Manejar caso de imagen vacía o nula
                    }



                }catch (Exception e){
                    Log.d("Exception", "e: "+e);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getApplicationContext(), "Error al obtener los datos!", Toast.LENGTH_LONG).show();

            }
        });

    }


    //METODOS PARA EL FUNCIONAMIENTO DE LA CAMARA
    private void permisos(){
        // METODO PARA LOS PERMISOS DE LA APLICACION
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},PETICION_ACCESS_CAM);
        }
        else
        {
            //estado = false;
            dispatchTakePictureIntent();
            //TomarFoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode ==  PETICION_ACCESS_CAM){
            if (grantResults.length>0&&grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }{
                Toast.makeText(getApplicationContext(),"Se necesita permiso de la camara",Toast.LENGTH_LONG);
            }
        }
    }

    //METODO ORIGINAL
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            try {
                File foto = new File(currentPhotoPath);
                image_url = Uri.fromFile(foto);
                //subirPhoto(image_url); // Si necesitas subir la foto, hazlo aquí
                imageView.setImageURI(image_url);

                // Verificamos si image_url no es nulo antes de llamar a subirPhoto
                // Si isUpdating es true y image_url no es nula, llamamos a subirPhoto
                if (estado && image_url != null) {
                    subirPhoto(image_url);
                } else {
                    // Esto es útil para depuración, muestra un mensaje si la condición no se cumple
                    //Toast.makeText(this, "isUpdating: " + estado + ", image_url: " + (image_url != null), Toast.LENGTH_SHORT).show();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.toString();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.luistorres_examen3",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE);
            }
        }
    }

    private String convertImage64(String path){
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] imagearray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imagearray,Base64.DEFAULT);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use  with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    //METODOS PARA LA GRABACION DE AUDIO
    public void Recorder(View view){
        if(grabacion == null){
            //archivoSalida = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Grabacion.mp3";
            archivoSalida = getExternalFilesDir(null).getAbsolutePath() + "/Grabacion.mp3";
            grabacion = new MediaRecorder();
            grabacion.setAudioSource(MediaRecorder.AudioSource.MIC);
            grabacion.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            //grabacion.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            grabacion.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            grabacion.setOutputFile(archivoSalida);

            try{
                grabacion.prepare();
                grabacion.start();
            } catch (IOException e){
            }

            btn_recorder.setBackgroundResource(R.drawable.rec);
            Toast.makeText(getApplicationContext(), "Grabando...", Toast.LENGTH_SHORT).show();
        } else if(grabacion != null){
            grabacion.stop();
            grabacion.release();
            grabacion = null;
            btn_recorder.setBackgroundResource(R.drawable.stop_rec);
            Toast.makeText(getApplicationContext(), "Grabación finalizada", Toast.LENGTH_SHORT).show();
        }
    }

    public void reproducir(View view) {

        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(archivoSalida);
            mediaPlayer.prepare();
        } catch (IOException e){
        }

        mediaPlayer.start();
        Toast.makeText(getApplicationContext(), "Reproduciendo audio", Toast.LENGTH_SHORT).show();
    }


    //Metodo para limpiar las cajas de texto
    private void clear(){
        descripcion.setText("");
        periodista.setText("");
        fecha.setText("");
        imageView.setImageResource(R.drawable.ic_contacto2);
    }


}