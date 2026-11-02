package com.violeta.fia.domain;

/**
 * Rol interno que un miembro puede tener dentro de una escuderia. Determina
 * que acciones puede realizar sobre la informacion de la escuderia, segun lo
 * que el administrativo de escuderia le asigne.
 */
public enum RolEscuderia {

    ADMINISTRADOR("Administrador de escuderia", true, true),
    EDITOR("Editor de escuderia", true, false),
    LECTOR("Solo lectura", false, false);

    private final String descripcion;
    private final boolean puedeActualizarInformacion;
    private final boolean puedeGestionarMiembros;

    RolEscuderia(String descripcion, boolean puedeActualizarInformacion, boolean puedeGestionarMiembros) {
        this.descripcion = descripcion;
        this.puedeActualizarInformacion = puedeActualizarInformacion;
        this.puedeGestionarMiembros = puedeGestionarMiembros;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isPuedeActualizarInformacion() {
        return puedeActualizarInformacion;
    }

    public boolean isPuedeGestionarMiembros() {
        return puedeGestionarMiembros;
    }
}
