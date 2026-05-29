package com.proptech.modelo;

public class ImagenInmueble {
    private int id;
    private String codigoInmueble;
    private String imagenBase64;
    private int orden;
    private String fechaSubida;

    public ImagenInmueble() {}

    public ImagenInmueble(String codigoInmueble, String imagenBase64, int orden, String fechaSubida) {
        this.codigoInmueble = codigoInmueble;
        this.imagenBase64 = imagenBase64;
        this.orden = orden;
        this.fechaSubida = fechaSubida;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigoInmueble() { return codigoInmueble; }
    public void setCodigoInmueble(String codigoInmueble) { this.codigoInmueble = codigoInmueble; }

    public String getImagenBase64() { return imagenBase64; }
    public void setImagenBase64(String imagenBase64) { this.imagenBase64 = imagenBase64; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }

    public String getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(String fechaSubida) { this.fechaSubida = fechaSubida; }
}
