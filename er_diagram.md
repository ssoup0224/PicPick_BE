classDiagram
direction BT
class flyway_schema_history {
   varchar(50) version
   varchar(200) description
   varchar(20) type
   varchar(1000) script
   int checksum
   varchar(100) installed_by
   timestamp installed_on
   int execution_time
   tinyint(1) success
   int installed_rank
}
class login_log {
   bigint user_id
   datetime(6) login_at
   varchar(255) ip_address
   varchar(255) user_agent
   bigint id
}
class mart {
   varchar(255) name
   varchar(255) address
   double latitude
   double longitude
   varchar(255) document_file
   varchar(255) brn
   datetime(6) created_at
   bigint id
}
class mart_item {
   varchar(255) item_name
   int item_price
   date start_date
   date end_date
   int discount_percentage
   bigint mart_id
   bigint id
}
class online_item {
   varchar(255) naver_product_id
   varchar(255) item_Brand
   varchar(255) item_name
   int item_price
   datetime(6) last_updated
   bigint id
}
class scan_log {
   varchar(255) name
   int price
   varchar(255) description
   datetime(6) scanned_at
   bigint user_id
   bigint mart_id
   bigint online_item_id
   bigint id
}
class user {
   varchar(255) uuid
   varchar(50) role
   datetime(6) created_at
   datetime(6) last_login
   int total_scans
   double current_longitude
   double current_latitude
   bigint current_mart_id
   bigint id
}

login_log  -->  user : user_id:id
mart_item  -->  mart : mart_id:id
scan_log  -->  mart : mart_id:id
scan_log  -->  online_item : online_item_id:id
scan_log  -->  user : user_id:id
user  -->  mart : current_mart_id:id
