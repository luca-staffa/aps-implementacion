-- V11: compuesto de neumatico utilizado en cada resultado de prueba.
-- El compuesto (blando, medio, duro, etc.) forma parte de los datos que se
-- consultan en el detalle de una prueba de neumaticos.

ALTER TABLE resultado_prueba ADD COLUMN compuesto VARCHAR(50);

UPDATE resultado_prueba
SET compuesto = CASE WHEN posicion = 1 THEN 'Blando' ELSE 'Medio' END
WHERE compuesto IS NULL;
