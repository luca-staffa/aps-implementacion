package com.violeta.fia.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sancion")
public class Sancion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "piloto_registrado_id")
    private Piloto piloto;

    @ManyToOne
    @JoinColumn(name = "escuderia_registrada_id")
    private Escuderia escuderia;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", length = 30)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "evento_id")
    private EventoCalendario evento;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "motivo", nullable = false, length = 255)
    private String motivo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "tipo", length = 80)
    private String tipo;

    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    @OneToMany(mappedBy = "sancion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SancionVisto> vistas = new ArrayList<>();

    public Sancion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Piloto getPiloto() {
        return piloto;
    }

    public void setPiloto(Piloto piloto) {
        this.piloto = piloto;
    }

    public Escuderia getEscuderia() {
        return escuderia;
    }

    public void setEscuderia(Escuderia escuderia) {
        this.escuderia = escuderia;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public EventoCalendario getEvento() {
        return evento;
    }

    public void setEvento(EventoCalendario evento) {
        this.evento = evento;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<SancionVisto> getVistas() {
        return vistas;
    }

    public void setVistas(List<SancionVisto> vistas) {
        this.vistas = vistas;
    }
}
