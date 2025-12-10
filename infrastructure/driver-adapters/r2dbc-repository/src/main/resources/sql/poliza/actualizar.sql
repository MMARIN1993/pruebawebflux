UPDATE poliza
SET tipo = :tipo,
    fecha_inicio = :fecha_inicio,
    valor = :valor
WHERE policy_id = :policy_id;

