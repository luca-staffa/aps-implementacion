package com.violeta.fia.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "sancion_visto")
public class SancionVisto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sancion_id", nullable = false)
    private Sancion sancion;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_visto")
    private LocalDateTime fechaVisto;

    public SancionVisto() {
    }

    public SancionVisto(Sancion sancion, Usuario usuario, LocalDateTime fechaVisto) {
        this.sancion = sancion;
        this.usuario = usuario;
        this.fechaVisto = fechaVisto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sancion getSancion() {
        return sancion;
    }

    public void setSancion(Sancion sancion) {
        this.sancion = sancion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaVisto() {
        return fechaVisto;
    }

    public void setFechaVisto(LocalDateTime fechaVisto) {
        this.fechaVisto = fechaVisto;
    }
}
