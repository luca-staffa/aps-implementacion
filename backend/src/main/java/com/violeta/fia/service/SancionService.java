package com.violeta.fia.service;

import com.violeta.fia.domain.Categoria;
import com.violeta.fia.domain.Sancion;
import com.violeta.fia.domain.SancionVisto;
import com.violeta.fia.domain.Usuario;
import com.violeta.fia.dto.SancionRequest;
import com.violeta.fia.dto.SancionResponse;
import com.violeta.fia.dto.VistoResponse;
import com.violeta.fia.repository.SancionRepository;
import com.violeta.fia.repository.SancionVistoRepository;
import com.violeta.fia.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SancionService {

    private final SancionRepository sancionRepository;
    private final SancionVistoRepository sancionVistoRepository;
    private final UsuarioRepository usuarioRepository;

    public SancionService(SancionRepository sancionRepository,
                          SancionVistoRepository sancionVistoRepository,
                          UsuarioRepository usuarioRepository) {
        this.sancionRepository = sancionRepository;
        this.sancionVistoRepository = sancionVistoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<SancionResponse> listar(Categoria categoria, LocalDate desde, LocalDate hasta) {
        return sancionRepository.buscarConFiltros(categoria, desde, hasta)
                .stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public SancionResponse obtener(Long id) {
        return aResponse(buscarEntidad(id));
    }

    @Transactional
    public SancionResponse crear(SancionRequest request) {
        validarParte(request);
        Sancion sancion = new Sancion();
        aplicar(sancion, request);
        return aResponse(sancionRepository.save(sancion));
    }

    @Transactional
    public SancionResponse actualizar(Long id, SancionRequest request) {
        validarParte(request);
        Sancion sancion = buscarEntidad(id);
        aplicar(sancion, request);
        return aResponse(sancionRepository.save(sancion));
    }

    @Transactional
    public void eliminar(Long id) {
        Sancion sancion = buscarEntidad(id);
        sancionRepository.delete(sancion);
    }

    @Transactional
    public VistoResponse marcarVisto(Long sancionId, String username) {
        Sancion sancion = buscarEntidad(sancionId);
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + username));

        LocalDateTime ahora = LocalDateTime.now();
        SancionVisto visto = sancionVistoRepository
                .findBySancionIdAndUsuarioId(sancionId, usuario.getId())
                .orElseGet(() -> new SancionVisto(sancion, usuario, ahora));
        visto.setFechaVisto(ahora);
        sancionVistoRepository.save(visto);

        return new VistoResponse(sancion.getId(), usuario.getId(), usuario.getUsername(), ahora, true);
    }

    private void validarParte(SancionRequest request) {
        if (request.pilotoId() == null && request.escuderiaId() == null) {
            throw new IllegalArgumentException("La sancion debe indicar un piloto o una escuderia");
        }
    }

    private void aplicar(Sancion sancion, SancionRequest request) {
        sancion.setPiloto(resolverUsuario(request.pilotoId(), "piloto"));
        sancion.setEscuderia(resolverUsuario(request.escuderiaId(), "escuderia"));
        sancion.setCategoria(request.categoria());
        sancion.setMotivo(request.motivo());
        sancion.setFecha(request.fecha());
        sancion.setTipo(request.tipo());
        sancion.setDescripcion(request.descripcion());
    }

    private Usuario resolverUsuario(Long id, String campo) {
        if (id == null) {
            return null;
        }
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el " + campo + " con id: " + id));
    }

    private Sancion buscarEntidad(Long id) {
        return sancionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sancion no encontrada con id: " + id));
    }

    private SancionResponse aResponse(Sancion sancion) {
        List<VistoResponse> vistas = sancion.getVistas().stream()
                .map(v -> new VistoResponse(
                        sancion.getId(),
                        v.getUsuario() != null ? v.getUsuario().getId() : null,
                        v.getUsuario() != null ? v.getUsuario().getUsername() : null,
                        v.getFechaVisto(),
                        true))
                .toList();

        return new SancionResponse(
                sancion.getId(),
                sancion.getPiloto() != null ? sancion.getPiloto().getId() : null,
                nombreCompleto(sancion.getPiloto()),
                sancion.getEscuderia() != null ? sancion.getEscuderia().getId() : null,
                nombreCompleto(sancion.getEscuderia()),
                sancion.getCategoria(),
                sancion.getMotivo(),
                sancion.getFecha(),
                sancion.getTipo(),
                sancion.getDescripcion(),
                vistas.size(),
                vistas);
    }

    private String nombreCompleto(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return usuario.getNombre() + " " + usuario.getApellido();
    }
}
