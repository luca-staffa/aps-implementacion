-- Datos de demostracion para que el calendario muestre actividad en las cuatro categorias.

INSERT INTO evento_calendario (categoria, nombre, circuito, pais, fecha_inicio, fecha_fin, tipo) VALUES
    ('F1', 'Gran Premio de Bahrein', 'Circuito Internacional de Bahrein', 'Bahrein', DATE '2026-03-06', DATE '2026-03-08', 'CARRERA'),
    ('F1', 'Gran Premio de Monaco', 'Circuit de Monaco', 'Monaco', DATE '2026-05-22', DATE '2026-05-24', 'CARRERA'),
    ('F1', 'Gran Premio de Gran Bretana', 'Silverstone Circuit', 'Reino Unido', DATE '2026-07-03', DATE '2026-07-05', 'CARRERA'),
    ('F1', 'Gran Premio de Belgica', 'Circuit de Spa-Francorchamps', 'Belgica', DATE '2026-07-24', DATE '2026-07-26', 'CARRERA'),
    ('F1', 'Gran Premio de Japon', 'Suzuka International Racing Course', 'Japon', DATE '2026-10-09', DATE '2026-10-11', 'CARRERA'),

    ('F2', 'Ronda de Arabia Saudita', 'Jeddah Corniche Circuit', 'Arabia Saudita', DATE '2026-04-10', DATE '2026-04-12', 'CARRERA'),
    ('F2', 'Ronda de Imola', 'Autodromo Enzo e Dino Ferrari', 'Italia', DATE '2026-05-15', DATE '2026-05-17', 'CARRERA'),
    ('F2', 'Ronda de Barcelona', 'Circuit de Barcelona-Catalunya', 'Espana', DATE '2026-06-05', DATE '2026-06-07', 'CARRERA'),

    ('F3', 'Ronda de Austria', 'Red Bull Ring', 'Austria', DATE '2026-06-26', DATE '2026-06-28', 'CARRERA'),
    ('F3', 'Ronda de Hungria', 'Hungaroring', 'Hungria', DATE '2026-07-17', DATE '2026-07-19', 'CARRERA'),

    ('F1_ACADEMY', 'Ronda de Zandvoort', 'Circuit Zandvoort', 'Paises Bajos', DATE '2026-08-21', DATE '2026-08-23', 'CARRERA'),
    ('F1_ACADEMY', 'Ronda de Singapur', 'Marina Bay Street Circuit', 'Singapur', DATE '2026-10-02', DATE '2026-10-04', 'CARRERA');
