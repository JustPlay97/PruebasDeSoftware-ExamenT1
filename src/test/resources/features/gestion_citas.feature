Feature: Gestion de Citas del Taller

  Scenario: Registro exitoso de mantenimiento ligero con otro mecanico
    Given que el mecanico con id 1 tiene una cita de 10:00 a 12:00 el 18 de septiembre de 2026
    And existe otro mecanico disponible con id 2 para MANTENIMIENTO_LIGERO
    When se intenta agendar un MANTENIMIENTO_LIGERO para la placa "RAM-778" con el mecanico 2 a las 10:00 el 18 de septiembre de 2026
    Then la cita se registra exitosamente en estado PROGRAMADA
    And se notifica la programacion de la cita

  Scenario: Intento de registro con mecanico ocupado iniciando a las 11:00
    Given que el mecanico con id 1 tiene una cita de 10:00 a 12:00 el 18 de septiembre de 2026
    When se intenta agendar un MANTENIMIENTO_LIGERO para la placa "RAM-778" con el mecanico 1 a las 11:00 el 18 de septiembre de 2026
    Then la cita falla por horario ocupado del mecanico

  Scenario: Intento de registro con mecanico iniciando a las 12:00
    Given que el mecanico con id 1 tiene una cita de 10:00 a 12:00 el 18 de septiembre de 2026
    When se intenta agendar un MANTENIMIENTO_LIGERO para la placa "RAM-778" con el mecanico 1 a las 12:00 el 18 de septiembre de 2026
    Then la cita se registra exitosamente en estado PROGRAMADA
