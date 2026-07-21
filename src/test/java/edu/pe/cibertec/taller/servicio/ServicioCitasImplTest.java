package edu.pe.cibertec.taller.servicio;

import edu.pe.cibertec.taller.excepcion.CitaNoCancelableException;
import edu.pe.cibertec.taller.excepcion.EspecialidadIncorrectaException;
import edu.pe.cibertec.taller.excepcion.HorarioNoPermitidoException;
import edu.pe.cibertec.taller.excepcion.MecanicoNoEncontradoException;
import edu.pe.cibertec.taller.modelo.*;
import edu.pe.cibertec.taller.repositorio.RepositorioCitas;
import edu.pe.cibertec.taller.repositorio.RepositorioMecanicos;
import edu.pe.cibertec.taller.servicio.impl.ServicioCitasImpl;
import edu.pe.cibertec.taller.util.ProveedorFechaHora;
import edu.pe.cibertec.taller.util.ServicioNotificaciones;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioCitasImplTest {

	@Mock
	private RepositorioMecanicos repositorioMecanicos;

	@Mock
	private RepositorioCitas repositorioCitas;

	@Mock
	private ProveedorFechaHora proveedorFechaHora;

	@Mock
	private ServicioNotificaciones servicioNotificaciones;

	private ServicioCitasImpl servicioCitas;

	@BeforeEach
	void inicializar() {
		servicioCitas = new ServicioCitasImpl(repositorioMecanicos, repositorioCitas,
				proveedorFechaHora, servicioNotificaciones);
		// TODO: crear aqui los datos comunes que necesiten los tests

	}

    //TESTS PARA LA PREGUNTA 1
	@Test
	@DisplayName("Agendar una cita valida la guarda, notifica y la retorna en estado PROGRAMADA")
	void agendarCitaExitosa() {
        // Arrange
        String placa = "RAM-778";
        Long mecanicoId = 1L;
        LocalDateTime fechaCita = LocalDateTime.of(2026, 9, 18, 10, 0);
        TipoServicio tipoServicio = TipoServicio.CAMBIO_ACEITE;

        Mecanico mecanico = new Mecanico(mecanicoId,"Luis Ramos", TipoServicio.CAMBIO_ACEITE);

        when(proveedorFechaHora.ahora()).thenReturn(LocalDateTime.of(2026, 9, 17, 8, 0));
        when(repositorioMecanicos.findById(mecanicoId)).thenReturn(Optional.of(mecanico));
        when(repositorioCitas.findByMecanicoIdAndEstado(mecanicoId, EstadoCita.PROGRAMADA)).thenReturn(Collections.emptyList());
        when(repositorioCitas.<Cita>save(any(Cita.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Cita.class));

        // Act
        Cita citaCreada = servicioCitas.agendarCita(mecanicoId,placa, tipoServicio, fechaCita);

        // Assert
        assertNotNull(citaCreada);
        assertEquals(EstadoCita.PROGRAMADA, citaCreada.getEstado());
        assertEquals(TipoServicio.CAMBIO_ACEITE.getDuracionHoras(), citaCreada.getDuracionHoras());

        verify(repositorioCitas).save(any(Cita.class));
        verify(servicioNotificaciones).notificarCitaAgendada(any(Cita.class));
	}

	@Test
	@DisplayName("Agendar con un mecanico inexistente lanza MecanicoNoEncontradoException")
	void agendarConMecanicoInexistente() {
		// Arrange
		// TODO
        String placa = "RAM-778";
        Long mecanicoIdInexistente = 99L;
        LocalDateTime fechaCita = LocalDateTime.of(2026, 9, 18, 10, 0);
        TipoServicio tipoServicio = TipoServicio.CAMBIO_ACEITE;

        when(repositorioMecanicos.findById(mecanicoIdInexistente)).thenReturn(Optional.empty());

		// Act y Assert
		// TODO
        assertThrows(MecanicoNoEncontradoException.class, () ->
                servicioCitas.agendarCita(mecanicoIdInexistente, placa, tipoServicio, fechaCita)
        );

        verify(repositorioCitas, never()).save(any(Cita.class));
        verify(servicioNotificaciones, never()).notificarCitaAgendada(any(Cita.class));
	}

	@Test
	@DisplayName("Agendar cuando la especialidad no coincide lanza EspecialidadIncorrectaException")
	void agendarConEspecialidadIncorrecta() {
		// Arrange
		// TODO
        String placa = "RAM-778";
        Long mecanicoId = 1L;
        LocalDateTime fechaCita = LocalDateTime.of(2026, 9, 18, 10, 0);
        TipoServicio servicioSolicitado = TipoServicio.REPARACION_MOTOR;

        Mecanico mecanico = new Mecanico(mecanicoId, "Luis Ramos", TipoServicio.CAMBIO_ACEITE);

        when(repositorioMecanicos.findById(mecanicoId)).thenReturn(Optional.of(mecanico));

		// Act y Assert
		// TODO
        assertThrows(EspecialidadIncorrectaException.class, () ->
                servicioCitas.agendarCita(mecanicoId, placa, servicioSolicitado, fechaCita)
        );

        verify(repositorioCitas, never()).save(any(Cita.class));
        verify(servicioNotificaciones, never()).notificarCitaAgendada(any(Cita.class));
	}

    //TESTS PARA LA PREGUNTA 2
	@Test
	@DisplayName("Un servicio pesado a las 7:00 se rechaza con HorarioNoPermitidoException")
	void agendarServicioALas7() {
		// Arrange
		// TODO
        String placa = "RAM-778";
        Long mecanicoId = 1L;
        LocalDateTime fechaCita = LocalDateTime.of(2026, 9, 18, 7, 0);
        TipoServicio tipoServicio = TipoServicio.REPARACION_MOTOR;

        Mecanico mecanico = new Mecanico(mecanicoId, "Luis Ramos", TipoServicio.REPARACION_MOTOR);
        when(repositorioMecanicos.findById(mecanicoId)).thenReturn(Optional.of(mecanico));

		// Act y Assert
		// TODO
        assertThrows(HorarioNoPermitidoException.class, () ->
                servicioCitas.agendarCita(mecanicoId, placa, tipoServicio, fechaCita)
        );

        verify(repositorioCitas, never()).save(any(Cita.class));
        verify(servicioNotificaciones, never()).notificarCitaAgendada(any(Cita.class));
	}

	@Test
	@DisplayName("Un servicio pesado a las 08:00 se acepta y se guarda")
	void agendarServicioPesadoALas8() {
		// Arrange
		// TODO
        String placa = "RAM-778";
        Long mecanicoId = 1L;
        LocalDateTime fechaCita = LocalDateTime.of(2026, 9, 18, 8, 0);
        TipoServicio tipoServicio = TipoServicio.REPARACION_MOTOR;

        Mecanico mecanico = new Mecanico(mecanicoId, "Luis Ramos", TipoServicio.REPARACION_MOTOR);

        when(proveedorFechaHora.ahora()).thenReturn(LocalDateTime.of(2026, 9, 17, 8, 0));
        when(repositorioMecanicos.findById(mecanicoId)).thenReturn(Optional.of(mecanico));
        when(repositorioCitas.findByMecanicoIdAndEstado(mecanicoId, EstadoCita.PROGRAMADA))
                .thenReturn(Collections.emptyList());
        when(repositorioCitas.<Cita>save(any(Cita.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Cita.class));

		// Act
		// TODO
        Cita citaCreada = servicioCitas.agendarCita(mecanicoId, placa, tipoServicio, fechaCita);

		// Assert
		// TODO
        assertNotNull(citaCreada);
        assertEquals(EstadoCita.PROGRAMADA, citaCreada.getEstado());
        assertEquals(TipoServicio.REPARACION_MOTOR.getDuracionHoras(), citaCreada.getDuracionHoras());

        verify(repositorioCitas).save(any(Cita.class));
        verify(servicioNotificaciones).notificarCitaAgendada(any(Cita.class));
	}

    @Test
    @DisplayName("Un servicio pesado a las 11:00 se acepta y se guarda")
    void agendarServicioPesadoALas11() {
        // Arrange
        // TODO
        String placa = "RAM-778";
        Long mecanicoId = 1L;
        LocalDateTime fechaCita = LocalDateTime.of(2026, 9, 18, 11, 0);
        TipoServicio tipoServicio = TipoServicio.REPARACION_MOTOR;

        Mecanico mecanico = new Mecanico(mecanicoId, "Luis Ramos", TipoServicio.REPARACION_MOTOR);

        when(proveedorFechaHora.ahora()).thenReturn(LocalDateTime.of(2026, 9, 17, 8, 0));
        when(repositorioMecanicos.findById(mecanicoId)).thenReturn(Optional.of(mecanico));
        when(repositorioCitas.findByMecanicoIdAndEstado(mecanicoId, EstadoCita.PROGRAMADA))
                .thenReturn(Collections.emptyList());
        when(repositorioCitas.<Cita>save(any(Cita.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Cita.class));

        // Act
        // TODO
        Cita citaCreada = servicioCitas.agendarCita(mecanicoId, placa, tipoServicio, fechaCita);

        // Assert
        // TODO
        assertNotNull(citaCreada);
        assertEquals(EstadoCita.PROGRAMADA, citaCreada.getEstado());
        assertEquals(TipoServicio.REPARACION_MOTOR.getDuracionHoras(), citaCreada.getDuracionHoras());

        verify(repositorioCitas).save(any(Cita.class));
        verify(servicioNotificaciones).notificarCitaAgendada(any(Cita.class));
    }

    @Test
    @DisplayName("Un servicio pesado a las 12:00 se rechaza con HorarioNoPermitidoException")
    void agendarServicioALas12() {
        // Arrange
        // TODO
        String placa = "RAM-778";
        Long mecanicoId = 1L;
        LocalDateTime fechaCita = LocalDateTime.of(2026, 9, 18, 12, 0);
        TipoServicio tipoServicio = TipoServicio.REPARACION_MOTOR;

        Mecanico mecanico = new Mecanico(mecanicoId, "Luis Ramos", TipoServicio.REPARACION_MOTOR);
        when(repositorioMecanicos.findById(mecanicoId)).thenReturn(Optional.of(mecanico));

        // Act y Assert
        // TODO
        assertThrows(HorarioNoPermitidoException.class, () ->
                servicioCitas.agendarCita(mecanicoId, placa, tipoServicio, fechaCita)
        );

        verify(repositorioCitas, never()).save(any(Cita.class));
        verify(servicioNotificaciones, never()).notificarCitaAgendada(any(Cita.class));
    }

    //TESTS PARA LA PREGUNTA 3
	@Test
	@DisplayName("Cancelar con 24 horas o mas de anticipacion no genera penalidad")
	void cancelarConAnticipacionSuficiente() {
		// Arrange
		// TODO
        Long citaId = 100L;
        LocalDateTime fechaCita = LocalDateTime.of(2026, 9, 18, 10, 0);
        when(proveedorFechaHora.ahora()).thenReturn(LocalDateTime.of(2026, 9, 17, 10, 0));
        Cita cita = new Cita();
        cita.setId(citaId);
        cita.setPlacaVehiculo("RAM-778");
        cita.setTipoServicio(TipoServicio.CAMBIO_ACEITE);
        cita.setFechaHoraInicio(fechaCita);
        cita.setEstado(EstadoCita.PROGRAMADA);

        when(repositorioCitas.findById(citaId)).thenReturn(Optional.of(cita));
        when(repositorioCitas.<Cita>save(any(Cita.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Cita.class));

		// Act
		// TODO
        ResultadoCancelacion resultado = servicioCitas.cancelarCita(citaId);

		// Assert
		// TODO: penalidad 0, estado CANCELADA, notificacion
        assertNotNull(resultado);
        assertEquals(0.0, resultado.getMontoPenalidad());
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());

        verify(repositorioCitas).save(cita);
        verify(servicioNotificaciones).notificarCitaCancelada(any(Cita.class));
	}

	@Test
	@DisplayName("Cancelar con menos de 24 horas aplica una penalidad de 50.00")
	void cancelarConAvisoTardio() {
		// Arrange
		// TODO
        Long citaId = 101L;
        LocalDateTime fechaCita = LocalDateTime.of(2026, 9, 18, 10, 0);
        when(proveedorFechaHora.ahora()).thenReturn(LocalDateTime.of(2026, 9, 18, 8, 0));

        Cita cita = new Cita();
        cita.setId(citaId);
        cita.setPlacaVehiculo("RAM-778");
        cita.setTipoServicio(TipoServicio.CAMBIO_ACEITE);
        cita.setFechaHoraInicio(fechaCita);
        cita.setEstado(EstadoCita.PROGRAMADA);

        when(repositorioCitas.findById(citaId)).thenReturn(Optional.of(cita));
        when(repositorioCitas.<Cita>save(any(Cita.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Cita.class));

		// Act
		// TODO
        ResultadoCancelacion resultado = servicioCitas.cancelarCita(citaId);

		// Assert
		// TODO
        assertNotNull(resultado);
        assertEquals(50.0, resultado.getMontoPenalidad());
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());

        verify(repositorioCitas).save(cita);
        verify(servicioNotificaciones).notificarCitaCancelada(any(Cita.class));
	}

    @Test
    @DisplayName("Intentar cancelar una cita ya atendida lanza CitaNoCancelableException")
    void cancelarCitaAtendidaLanzaExcepcion() {
        // Arrange
        Long citaId = 102L;

        Cita cita = new Cita();
        cita.setId(citaId);
        cita.setPlacaVehiculo("RAM-778");
        cita.setTipoServicio(TipoServicio.CAMBIO_ACEITE);
        cita.setFechaHoraInicio(LocalDateTime.of(2026, 9, 18, 10, 0));
        cita.setEstado(EstadoCita.ATENDIDA); // Cita ya atendida / completada

        when(repositorioCitas.findById(citaId)).thenReturn(Optional.of(cita));

        // Act y Assert
        assertThrows(CitaNoCancelableException.class, () ->
                servicioCitas.cancelarCita(citaId)
        );

        verify(repositorioCitas, never()).save(any(Cita.class));
        verify(servicioNotificaciones, never()).notificarCitaCancelada(any(Cita.class));
    }

}
