package com.violeta.fia.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "resultado_prueba")
public class ResultadoPrueba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prueba_id", nullable = false)
    private PruebaNeumatico prueba;

    @Column(name = "escuderia", length = 150)
    private String escuderia;

    @Column(name = "piloto", length = 150)
    private String piloto;

    @Column(name = "mejor_tiempo", length = 50)
    private String mejorTiempo;

    @Column(name = "vueltas")
    private Integer vueltas;

    @Column(name = "posicion")
    private Integer posicion;

    public ResultadoPrueba() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PruebaNeumatico getPrueba() {
        return prueba;
    }

    public void setPrueba(PruebaNeumatico prueba) {
        this.prueba = prueba;
    }

    public String getEscuderia() {
        return escuderia;
    }

    public void setEscuderia(String escuderia) {
        this.escuderia = escuderia;
    }

    public String getPiloto() {
        return piloto;
    }

    public void setPiloto(String piloto) {
        this.piloto = piloto;
    }

    public String getMejorTiempo() {
        return mejorTiempo;
    }

    public void setMejorTiempo(String mejorTiempo) {
        this.mejorTiempo = mejorTiempo;
    }

    public Integer getVueltas() {
        return vueltas;
    }

    public void setVueltas(Integer vueltas) {
        this.vueltas = vueltas;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }
}
