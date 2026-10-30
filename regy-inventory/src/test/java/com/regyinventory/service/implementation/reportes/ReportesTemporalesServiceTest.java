package com.regyinventory.service.implementation.reportes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.regyinventory.entities.ReporteTemporal;
import com.regyinventory.entities.Usuario;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.IReporteTemporalRepository;
import com.regyinventory.service.implementation.OperacionSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class ReportesTemporalesServiceTest {
    @TempDir Path tempDir;
    private IReporteTemporalRepository repository;
    private ReportesTemporalesService service;

    @BeforeEach
    void setup() {
        repository = mock(IReporteTemporalRepository.class);
        OperacionSupport usuarioActual = mock(OperacionSupport.class);
        Usuario usuario = new Usuario();
        usuario.setId(15L);
        when(usuarioActual.obtenerUsuarioAutenticado()).thenReturn(usuario);
        service = new ReportesTemporalesService(repository, usuarioActual);
        ReflectionTestUtils.setField(service, "directorio", tempDir.toString());
        ReflectionTestUtils.setField(service, "diasRetencion", 7L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", "token", List.of(new SimpleGrantedAuthority("PRODUCT_CREATE"))));
    }

    @AfterEach void limpiarContexto() { SecurityContextHolder.clearContext(); }

    @Test
    void guardaArchivoPrivadoYMetadatosSinBase64EnBD() throws Exception {
        String id = service.guardar("importacion-1", "PRODUCTOS", "productos.xlsx", "resultado.xlsx",
                Base64.getEncoder().encodeToString(new byte[]{1, 2, 3}));
        assertArrayEquals(new byte[]{1, 2, 3}, Files.readAllBytes(tempDir.resolve(id + ".xlsx")));
        var captor = org.mockito.ArgumentCaptor.forClass(ReporteTemporal.class);
        verify(repository).save(captor.capture());
        assertEquals(15L, captor.getValue().getUsuarioId());
        assertEquals("PRODUCTOS", captor.getValue().getTipo());
        assertEquals(7, java.time.Duration.between(captor.getValue().getCreadoEn(), captor.getValue().getVenceEn()).toDays());
    }

    @Test
    void descargaRequiereSerPropietarioYEstarVigente() {
        when(repository.findByIdAndUsuarioIdAndVenceEnAfter(eq("otro"), eq(15L), any(Instant.class)))
                .thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.descargar("otro"));
    }

    @Test
    void eliminaSoloReportesVencidos() throws Exception {
        ReporteTemporal vencido = new ReporteTemporal();
        vencido.setId("vencido");
        vencido.setVenceEn(Instant.now().minusSeconds(60));
        Files.write(tempDir.resolve("vencido.xlsx"), new byte[]{9});
        when(repository.findByVenceEnLessThanEqual(any(Instant.class))).thenReturn(List.of(vencido));
        service.limpiarVencidos();
        assertFalse(Files.exists(tempDir.resolve("vencido.xlsx")));
        verify(repository).delete(vencido);
    }
}
