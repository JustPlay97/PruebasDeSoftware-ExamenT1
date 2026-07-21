package edu.pe.cibertec.taller.bdd;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import edu.pe.cibertec.taller.excepcion.HorarioOcupadoException;
import edu.pe.cibertec.taller.modelo.Cita;
import edu.pe.cibertec.taller.modelo.EstadoCita;
import edu.pe.cibertec.taller.modelo.Mecanico;
import edu.pe.cibertec.taller.modelo.TipoServicio;
import edu.pe.cibertec.taller.repositorio.RepositorioCitas;
import edu.pe.cibertec.taller.repositorio.RepositorioMecanicos;
import edu.pe.cibertec.taller.servicio.impl.ServicioCitasImpl;
import edu.pe.cibertec.taller.util.ProveedorFechaHora;
import edu.pe.cibertec.taller.util.ServicioNotificaciones;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GestionCitasSteps {

	private RepositorioMecanicos repositorioMecanicos;
	private RepositorioCitas repositorioCitas;
	private ProveedorFechaHora proveedorFechaHora;
	private ServicioNotificaciones servicioNotificaciones;
	private ServicioCitasImpl servicioCitas;
    private Cita citaResultado;
    private Exception excepcionCapturada;

	@Before
	public void inicializar() {
		repositorioMecanicos = mock(RepositorioMecanicos.class);
		repositorioCitas = mock(RepositorioCitas.class);
		proveedorFechaHora = mock(ProveedorFechaHora.class);
		servicioNotificaciones = mock(ServicioNotificaciones.class);
		servicioCitas = new ServicioCitasImpl(repositorioMecanicos, repositorioCitas,
				proveedorFechaHora, servicioNotificaciones);

        // Simulamos la fecha del sistema (un día antes)
        when(proveedorFechaHora.ahora()).thenReturn(LocalDateTime.of(2026, 9, 17, 8, 0));

        citaResultado = null;
        excepcionCapturada = null;
	}

	// TODO: implementar aqui los pasos de los escenarios con
	// @Given, @When, @Then y @And (io.cucumber.java.en)

    //TESTS PARA LA PREGUNTA 4

    @Given("que el mecanico con id {long} tiene una cita de 10:00 a 12:00 el {int} de septiembre de {int}")
    public void queElMecanicoTieneUnaCitaDe10a12(Long mecanicoId, int dia, int anio) {
        Mecanico mecanico = new Mecanico(mecanicoId, "Luis Ramos", TipoServicio.MANTENIMIENTO_LIGERO);
        when(repositorioMecanicos.findById(mecanicoId)).thenReturn(Optional.of(mecanico));

        Cita citaExistente = new Cita();
        citaExistente.setId(10L);
        citaExistente.setMecanico(mecanico);
        citaExistente.setFechaHoraInicio(LocalDateTime.of(anio, 9, dia, 10, 0));
        citaExistente.setTipoServicio(TipoServicio.MANTENIMIENTO_LIGERO);
        citaExistente.setDuracionHoras(TipoServicio.MANTENIMIENTO_LIGERO.getDuracionHoras());// dura 2 horas (10:00 a 12:00)
        citaExistente.setEstado(EstadoCita.PROGRAMADA);

        when(repositorioCitas.findByMecanicoIdAndEstado(mecanicoId, EstadoCita.PROGRAMADA))
                .thenReturn(List.of(citaExistente));
    }

    @Given("existe otro mecanico disponible con id {long} para MANTENIMIENTO_LIGERO")
    public void existeOtroMecanicoDisponible(Long mecanicoId) {
        Mecanico mecanico = new Mecanico(mecanicoId, "Romeo Santos", TipoServicio.MANTENIMIENTO_LIGERO);
        when(repositorioMecanicos.findById(mecanicoId)).thenReturn(Optional.of(mecanico));
        when(repositorioCitas.findByMecanicoIdAndEstado(mecanicoId, EstadoCita.PROGRAMADA))
                .thenReturn(Collections.emptyList());
    }

    @When("se intenta agendar un MANTENIMIENTO_LIGERO para la placa {string} con el mecanico {long} a las {int}:{int} el {int} de septiembre de {int}")
    public void seIntentaAgendarUnMantenimientoLigero(String placa, Long mecanicoId, int hora, int minutos, int dia, int anio) {
        LocalDateTime fechaCita = LocalDateTime.of(anio, 9, dia, hora, minutos);
        when(repositorioCitas.<Cita>save(any(Cita.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Cita.class));

        try {
            citaResultado = servicioCitas.agendarCita(mecanicoId, placa, TipoServicio.MANTENIMIENTO_LIGERO, fechaCita);
        } catch (Exception e) {
            excepcionCapturada = e;
        }
    }

    @Then("la cita se registra exitosamente en estado PROGRAMADA")
    public void laCitaSeRegistraExitosamente() {
        assertNotNull(citaResultado);
        assertEquals(EstadoCita.PROGRAMADA, citaResultado.getEstado());
        verify(repositorioCitas).save(any(Cita.class));
    }

    @Then("se notifica la programacion de la cita")
    public void seNotificaLaProgramacionDeLaCita() {
        verify(servicioNotificaciones).notificarCitaAgendada(any(Cita.class));
    }

    @Then("la cita falla por horario ocupado del mecanico")
    public void laCitaFallaPorHorarioOcupado() {
        assertNotNull(excepcionCapturada);
        assertTrue(excepcionCapturada instanceof HorarioOcupadoException);
    }
}

