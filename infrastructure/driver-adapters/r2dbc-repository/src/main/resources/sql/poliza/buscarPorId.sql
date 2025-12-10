SELECT uuid, policy_id, tipo, fecha_inicio, valor
FROM poliza
WHERE policy_id = :policy_id;

