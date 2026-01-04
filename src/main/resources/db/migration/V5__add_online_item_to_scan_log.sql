ALTER TABLE scan_log ADD COLUMN online_item_id BIGINT;

ALTER TABLE scan_log
ADD CONSTRAINT fk_scan_log_online_item FOREIGN KEY (online_item_id) REFERENCES online_item (id);