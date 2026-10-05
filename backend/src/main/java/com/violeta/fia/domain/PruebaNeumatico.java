package com.violeta.fia.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prueba_neumatico")
public class PruebaNeumatico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "circuito", length = 200)
    private String circuito;

    @Column(name = "pais", length = 100)
    private String pais;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "categorias", length = 200)
    private String categorias;

    @Column(name = "aviso_publicado", nullable = false)
    private boolean avisoPublicado;

    @OneToMany(mappedBy = "prueba", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoPrueba> resultados = new ArrayList<>();

    public PruebaNeumatico() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getCircuito() {
        return circuito;
    }

    public void setCircuito(String circuito) {
        this.circuito = circuito;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getCategorias() {
        return categorias;
    }

    public void setCategorias(String categorias) {
        this.categorias = categorias;
    }

    public boolean isAvisoPublicado() {
        return avisoPublicado;
    }

    public void setAvisoPublicado(boolean avisoPublicado) {
        this.avisoPublicado = avisoPublicado;
    }

    public List<ResultadoPrueba> getResultados() {
        return resultados;
    }

    public void setResultados(List<ResultadoPrueba> resultados) {
        this.resultados = resultados;
    }
}
