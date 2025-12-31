-- Move location fields from scan_log to user
ALTER TABLE user ADD COLUMN current_longitude DOUBLE;

ALTER TABLE user ADD COLUMN current_latitude DOUBLE;

ALTER TABLE scan_log DROP COLUMN current_longitude;

ALTER TABLE scan_log DROP COLUMN current_latitude;