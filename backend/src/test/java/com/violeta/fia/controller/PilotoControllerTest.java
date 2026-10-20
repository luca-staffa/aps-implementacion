package com.violeta.fia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.violeta.fia.BaseIntegrationTest;
import com.violeta.fia.domain.CategoriaAtributo;
import com.violeta.fia.domain.CategoriaCatalogo;
import com.violeta.fia.domain.Escuderia;
import com.violeta.fia.domain.Piloto;
import com.violeta.fia.domain.Rol;
import com.violeta.fia.domain.RolNombre;
import com.violeta.fia.domain.Temporada;
import com.violeta.fia.domain.Usuario;
import com.violeta.fia.dto.PilotoRequest;
import com.violeta.fia.repository.CategoriaAtributoRepository;
import com.violeta.fia.repository.CategoriaCatalogoRepository;
import com.violeta.fia.repository.EscuderiaRepository;
import com.violeta.fia.repository.PilotoRepository;
import com.violeta.fia.repository.RolRepository;
import com.violeta.fia.repository.SancionRepository;
import com.violeta.fia.repository.SancionVistoRepository;
import com.violeta.fia.repository.TemporadaRepository;
import com.violeta.fia.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PilotoControllerTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoriaCatalogoRepository categoriaCatalogoRepository;

    @Autowired
    private CategoriaAtributoRepository categoriaAtributoRepository;

    @Autowired
    private TemporadaRepository temporadaRepository;

    @Autowired
    private EscuderiaRepository escuderiaRepository;

    @Autowired
    private PilotoRepository pilotoRepository;

    @Autowired
    private SancionRepository sancionRepository;

    @Autowired
    private SancionVistoRepository sancionVistoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private Escuderia escuderiaPropia;
    private Escuderia escuderiaAjena;
    private CategoriaCatalogo categoria;
    private Temporada temporada;
    private final List<Long> categoriaAtributoIdsCreados = new ArrayList<>();

    @BeforeEach
    void setUp() {
        categoria = categoriaCatalogoRepository.findByCodigo("F1")
                .orElseGet(() -> categoriaCatalogoRepository.save(new CategoriaCatalogo("F1", "Fórmula 1", true)));
        temporada = temporadaRepository.findByAnio(2026)
                .orElseGet(() -> temporadaRepository.save(new Temporada(2026, "Temporada 2026", true, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))));

        escuderiaPropia = escuderiaRepository.findByNombre("Alpine")
                .orElseGet(() -> escuderiaRepository.save(new Escuderia("Alpine", "Francia", true)));
        escuderiaAjena = escuderiaRepository.findByNombre("Ferrari")
                .orElseGet(() -> escuderiaRepository.save(new Escuderia("Ferrari", "Italia", true)));

        Usuario adminEscuderia = usuarioRepository.findByUsername("admin.escuderia")
                .orElseThrow(() -> new IllegalStateException("Usuario admin.escuderia no encontrado"));
        adminEscuderia.setEscuderia(escuderiaPropia);
        usuarioRepository.save(adminEscuderia);

        insertarAtributoLimite("max_titulares", "2");
        insertarAtributoLimite("max_suplentes", "2");
        sancionVistoRepository.deleteAll();
        sancionRepository.deleteAll();
        pilotoRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        sancionVistoRepository.deleteAll();
        sancionRepository.deleteAll();
        pilotoRepository.deleteAll();
        for (Long id : new ArrayList<>(categoriaAtributoIdsCreados)) {
            categoriaAtributoRepository.deleteById(id);
        }
        categoriaAtributoIdsCreados.clear();
    }

    private void insertarAtributoLimite(String clave, String valor) {
        CategoriaAtributo atributo = new CategoriaAtributo();
        atributo.setCategoria(categoria);
        atributo.setClave(clave);
        atributo.setValor(valor);
        atributo.setDescripcion("Límite de pilotos " + clave + " por escudería");
        CategoriaAtributo guardado = categoriaAtributoRepository.save(atributo);
        if (guardado.getId() != null) {
            categoriaAtributoIdsCreados.add(guardado.getId());
        }
    }

    @Test
    void sinTokenEnPilotosProtegidosDevuelve401() throws Exception {
        mockMvc.perform(get("/api/pilotos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEscuderiaNoPuedeVerPilotoAjeno() throws Exception {
        Piloto pilotoAjeno = guardarPiloto("Carlos", "Sainz", 55, "SAI", "TITULAR", escuderiaAjena);

        String token = obtenerToken("admin.escuderia", "Password123!");

        mockMvc.perform(get("/api/pilotos/{id}", pilotoAjeno.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicEndpointDevuelve200SinCamposSensibles() throws Exception {
        guardarPiloto("Charles", "Leclerc", 16, "LEC", "TITULAR", escuderiaPropia);

        mockMvc.perform(get("/api/pilotos/publico")
                        .param("categoriaId", categoria.getId().toString())
                        .param("temporadaId", temporada.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numeroLicenciaFia").doesNotExist())
                .andExpect(jsonPath("$[0].fechaNacimiento").doesNotExist());
    }

    @Test
    void adminFiaPuedeConsultarLaListaDePilotos() throws Exception {
        guardarPiloto("Charles", "Leclerc", 16, "LEC", "TITULAR", escuderiaPropia);

        String token = obtenerToken("admin.fia", "Password123!");

        mockMvc.perform(get("/api/pilotos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void adminFiaNoPuedeCrearPiloto() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");

        PilotoRequest request = new PilotoRequest(
                "Lando",
                "Norris",
                LocalDate.of(1999, 11, 13),
                "Britanica",
                4,
                "NOR",
                Piloto.RolPiloto.TITULAR,
                "FIA-2026-004",
                "https://example.com/foto.jpg",
                categoria.getId(),
                temporada.getId(),
                null
        );

        mockMvc.perform(post("/api/pilotos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void atributosDeReferenciaDeV3SiguenPresentes() {
        List<CategoriaAtributo> atributos = categoriaAtributoRepository.findByCategoriaIdOrderByClaveAsc(categoria.getId());

        assert atributos.stream().anyMatch(a -> "tipo_competencia".equals(a.getClave()));
        assert atributos.stream().anyMatch(a -> "max_titulares".equals(a.getClave()));
    }

    @Test
    void adminEscuderiaPuedeCrearPilotoEnSuEscuderia() throws Exception {
        String token = obtenerToken("admin.escuderia", "Password123!");

        PilotoRequest request = new PilotoRequest(
                "Lando",
                "Norris",
                LocalDate.of(1999, 11, 13),
                "Britanica",
                4,
                "NOR",
                Piloto.RolPiloto.TITULAR,
                "FIA-2026-004",
                "https://example.com/foto.jpg",
                categoria.getId(),
                temporada.getId(),
                null
        );

        mockMvc.perform(post("/api/pilotos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.escuderiaId").value(escuderiaPropia.getId()))
                .andExpect(jsonPath("$.numero").value(4));
    }

    @Test
    void adminEscuderiaNoPuedeEditarPilotoAjeno() throws Exception {
        Piloto piloto = guardarPiloto("Lando", "Norris", 4, "NOR", "TITULAR", escuderiaAjena);
        String token = obtenerToken("admin.escuderia", "Password123!");

        PilotoRequest request = new PilotoRequest(
                "Lando",
                "Norris",
                LocalDate.of(1999, 11, 13),
                "Britanica",
                7,
                "RBR",
                Piloto.RolPiloto.TITULAR,
                "FIA-2026-007",
                "https://example.com/foto-editada.jpg",
                categoria.getId(),
                temporada.getId(),
                null
        );

        mockMvc.perform(put("/api/pilotos/{id}", piloto.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEscuderiaNoPuedeEliminarPilotoAjeno() throws Exception {
        Piloto piloto = guardarPiloto("Carlos", "Sainz", 55, "SAI", "TITULAR", escuderiaAjena);
        String token = obtenerToken("admin.escuderia", "Password123!");

        mockMvc.perform(delete("/api/pilotos/{id}", piloto.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEscuderiaIgnoraEscuderiaIdAjenaEnElBody() throws Exception {
        String token = obtenerToken("admin.escuderia", "Password123!");

        PilotoRequest request = new PilotoRequest(
                "Oscar",
                "Piastri",
                LocalDate.of(2001, 4, 6),
                "Australia",
                81,
                "PIA",
                Piloto.RolPiloto.TITULAR,
                "FIA-2026-081",
                null,
                categoria.getId(),
                temporada.getId(),
                escuderiaAjena.getId()
        );

        mockMvc.perform(post("/api/pilotos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.escuderiaId").value(escuderiaPropia.getId()));
    }

    @Test
    void adminEscuderiaSoloVeSusPropiosPilotos() throws Exception {
        guardarPiloto("Charles", "Leclerc", 16, "LEC", "TITULAR", escuderiaPropia);
        guardarPiloto("Carlos", "Sainz", 55, "SAI", "TITULAR", escuderiaAjena);

        String token = obtenerToken("admin.escuderia", "Password123!");

        mockMvc.perform(get("/api/pilotos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].escuderiaId").value(escuderiaPropia.getId()));
    }

    @Test
    void adminEscuderiaPuedeEditarPilotoDeSuEscuderia() throws Exception {
        Piloto piloto = guardarPiloto("Lando", "Norris", 4, "NOR", "TITULAR", escuderiaPropia);

        String token = obtenerToken("admin.escuderia", "Password123!");

        PilotoRequest request = new PilotoRequest(
                "Lando",
                "Norris",
                LocalDate.of(1999, 11, 13),
                "Britanica",
                7,
                "RBR",
                Piloto.RolPiloto.TITULAR,
                "FIA-2026-007",
                "https://example.com/foto-editada.jpg",
                categoria.getId(),
                temporada.getId(),
                null
        );

        mockMvc.perform(put("/api/pilotos/{id}", piloto.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(piloto.getId()))
                .andExpect(jsonPath("$.numero").value(7))
                .andExpect(jsonPath("$.abreviatura").value("RBR"));
    }

    @Test
    void numeroDuplicadoDevuelve400() throws Exception {
        guardarPiloto("Fernando", "Alonso", 14, "ALO", "TITULAR", escuderiaPropia);

        String token = obtenerToken("admin.escuderia", "Password123!");

        PilotoRequest request = new PilotoRequest(
                "Esteban",
                "Ocon",
                LocalDate.of(1996, 9, 17),
                "Francesa",
                14,
                "OCO",
                Piloto.RolPiloto.TITULAR,
                "FIA-2026-014",
                null,
                categoria.getId(),
                temporada.getId(),
                null
        );

        mockMvc.perform(post("/api/pilotos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("numero")));
    }

    @Test
    void abreviaturaDuplicadaDevuelve400() throws Exception {
        guardarPiloto("Fernando", "Alonso", 14, "ALO", "TITULAR", escuderiaPropia);

        String token = obtenerToken("admin.escuderia", "Password123!");

        PilotoRequest request = new PilotoRequest(
                "Esteban",
                "Ocon",
                LocalDate.of(1996, 9, 17),
                "Francesa",
                15,
                "ALO",
                Piloto.RolPiloto.TITULAR,
                "FIA-2026-015",
                null,
                categoria.getId(),
                temporada.getId(),
                null
        );

        mockMvc.perform(post("/api/pilotos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("abreviatura")));
    }

    @Test
    void limiteTitularesExcedidoDevuelve400() throws Exception {
        Rol rol = rolRepository.findByNombre(RolNombre.ADMIN_ESCUDERIA)
                .orElseThrow();

        guardadoPilotoConRol("Piloto 1", "Uno", 1, "P1", "TITULAR", escuderiaPropia, rol);
        guardadoPilotoConRol("Piloto 2", "Dos", 2, "P2", "TITULAR", escuderiaPropia, rol);
        guardadoPilotoConRol("Piloto 3", "Tres", 3, "P3", "SUPLENTE", escuderiaPropia, rol);

        String token = obtenerToken("admin.escuderia", "Password123!");

        PilotoRequest request = new PilotoRequest(
                "Piloto",
                "Cuatro",
                LocalDate.of(1995, 5, 9),
                "Argentina",
                4,
                "PIL",
                Piloto.RolPiloto.TITULAR,
                "FIA-2026-004",
                null,
                categoria.getId(),
                temporada.getId(),
                null
        );

        mockMvc.perform(post("/api/pilotos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("titulares")));
    }

    @Test
    void bajaLogicaNoApareceEnPublico() throws Exception {
        Piloto piloto = guardarPiloto("Valtteri", "Bottas", 77, "BOT", "TITULAR", escuderiaPropia);

        String token = obtenerToken("admin.escuderia", "Password123!");

        mockMvc.perform(delete("/api/pilotos/{id}", piloto.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/pilotos/publico")
                        .param("categoriaId", categoria.getId().toString())
                        .param("temporadaId", temporada.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private Piloto guardarPiloto(String nombre, String apellido, int numero, String abreviatura, String rol, Escuderia escuderia) {
        Piloto piloto = new Piloto();
        piloto.setNombre(nombre);
        piloto.setApellido(apellido);
        piloto.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        piloto.setNacionalidad("Argentina");
        piloto.setNumero(numero);
        piloto.setAbreviatura(abreviatura);
        piloto.setRol(Piloto.RolPiloto.valueOf(rol));
        piloto.setNumeroLicenciaFia("FIA-" + numero);
        piloto.setActivo(true);
        piloto.setCategoria(categoria);
        piloto.setTemporada(temporada);
        piloto.setEscuderia(escuderia);
        return pilotoRepository.save(piloto);
    }

    private Piloto guardadoPilotoConRol(String nombre, String apellido, int numero, String abreviatura, String rol, Escuderia escuderia, Rol usuarioRol) {
        return guardarPiloto(nombre, apellido, numero, abreviatura, rol, escuderia);
    }
}
