package com.example.luistorres_examen3.model;

public class Entrevista {

    String descripcion;
    String periodista;
    String fecha;
    String Record;
    String Photo;

    public Entrevista() {
    }

    public Entrevista(String descripcion, String periodista, String fecha, String record, String photo) {
        this.descripcion = descripcion;
        this.periodista = periodista;
        this.fecha = fecha;
        Record = record;
        Photo = photo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPeriodista() {
        return periodista;
    }

    public void setPeriodista(String periodista) {
        this.periodista = periodista;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getRecord() {
        return Record;
    }

    public void setRecord(String record) {
        Record = record;
    }

    public String getPhoto() {
        return Photo;
    }

    public void setPhoto(String photo) {
        Photo = photo;
    }
}
