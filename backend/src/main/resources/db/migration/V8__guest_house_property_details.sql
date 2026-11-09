-- Preserve room and facility identifiers used by existing reservations.
UPDATE rooms AS room
SET room_number = inventory.room_number, location = inventory.location
FROM (VALUES
    ('101', 'GF 1', 'Ground floor'),
    ('102', 'GF 2', 'Ground floor'),
    ('103', 'GF 3', 'Ground floor'),
    ('104', 'GF 4', 'Ground floor'),
    ('105', 'FF 5', 'First floor'),
    ('106', 'FF 6', 'First floor'),
    ('107', 'FF 7', 'First floor'),
    ('108', 'FF 8', 'First floor'),
    ('201', '201', 'First floor'),
    ('202', '202', 'First floor')
) AS inventory(original_number, room_number, location)
WHERE room.id = ('00000000-0000-0000-0000-000000000' || inventory.original_number)::uuid;

UPDATE facilities SET name = 'Conference Hall' WHERE type = 'CONVENTION';
