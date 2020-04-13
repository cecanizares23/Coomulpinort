/*
 * ContextDataResourceNames.java
 *
 * Proyecto: Coomulpinort Puntos
 * Cliente:  Coomulpinort
 * Copyright 2020 by Ing. Carlos Cañizares
 * All rights reserved
 */

package co.IngCarlos.Coomulpinort.mvc.dto;

import co.IngCarlos.Coomulpinort.common.util.Generales;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;

/**
 *
 * @author carlos
 */
public class TipoUsuarioDTO implements Serializable {
    
    String id = Generales.EMPTYSTRING;
    String descripcion = Generales.EMPTYSTRING;
    String estado = Generales.EMPTYSTRING;
    String registradoPor = Generales.EMPTYSTRING;
    String fechaRegistro = Generales.EMPTYSTRING;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(String registradoPor) {
        this.registradoPor = registradoPor;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public String toStringJson() {
        String dtoJsonString = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            dtoJsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (Exception e) {
        }
        return dtoJsonString;
    }
    
}
