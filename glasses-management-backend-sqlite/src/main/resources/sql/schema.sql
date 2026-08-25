CREATE TABLE IF NOT EXISTS sys_user (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL,
  phone TEXT DEFAULT NULL,
  password TEXT NOT NULL,
  real_name TEXT DEFAULT NULL,
  role TEXT NOT NULL DEFAULT 'merchant',
  must_change_password INTEGER NOT NULL DEFAULT 0,
  disabled INTEGER NOT NULL DEFAULT 0,
  disabled_time TEXT DEFAULT NULL,
  deleted INTEGER NOT NULL DEFAULT 0,
  deleted_time TEXT DEFAULT NULL,
  force_reset_time TEXT DEFAULT NULL,
  create_time TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
  UNIQUE (username),
  UNIQUE (phone)
);

CREATE TABLE IF NOT EXISTS customer (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  phone TEXT NOT NULL,
  gender INTEGER DEFAULT 0,
  birthday TEXT DEFAULT NULL,
  remark TEXT DEFAULT '',
  deleted INTEGER NOT NULL DEFAULT 0,
  deleted_time TEXT DEFAULT NULL,
  deleted_by INTEGER DEFAULT NULL,
  create_time TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
  update_time TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
  UNIQUE (phone)
);

CREATE TABLE IF NOT EXISTS optometry_record (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  customer_id INTEGER NOT NULL,
  od_sph NUMERIC DEFAULT NULL,
  od_cyl NUMERIC DEFAULT NULL,
  od_axis INTEGER DEFAULT NULL,
  od_va TEXT DEFAULT NULL,
  os_sph NUMERIC DEFAULT NULL,
  os_cyl NUMERIC DEFAULT NULL,
  os_axis INTEGER DEFAULT NULL,
  os_va TEXT DEFAULT NULL,
  od_pd NUMERIC DEFAULT NULL,
  os_pd NUMERIC DEFAULT NULL,
  od_ph NUMERIC DEFAULT NULL,
  os_ph NUMERIC DEFAULT NULL,
  pd_far NUMERIC DEFAULT NULL,
  pd_near NUMERIC DEFAULT NULL,
  add_power NUMERIC DEFAULT NULL,
  optometrist_name TEXT DEFAULT NULL,
  exam_date TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
  create_time TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
  deleted INTEGER NOT NULL DEFAULT 0,
  deleted_time TEXT DEFAULT NULL,
  deleted_by INTEGER DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS sales_record (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  record_no TEXT NOT NULL,
  customer_id INTEGER NOT NULL,
  optometry_id INTEGER DEFAULT NULL,
  frame_brand TEXT DEFAULT NULL,
  frame_model TEXT DEFAULT NULL,
  frame_quantity INTEGER NOT NULL DEFAULT 1,
  frame_price NUMERIC DEFAULT 0.00,
  lens_brand TEXT DEFAULT NULL,
  lens_params TEXT DEFAULT NULL,
  lens_quantity INTEGER NOT NULL DEFAULT 1,
  lens_price NUMERIC DEFAULT 0.00,
  total_amount NUMERIC NOT NULL DEFAULT 0.00,
  sales_date TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
  operator_id INTEGER DEFAULT NULL,
  create_time TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
  update_time TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
  deleted INTEGER NOT NULL DEFAULT 0,
  deleted_time TEXT DEFAULT NULL,
  deleted_by INTEGER DEFAULT NULL,
  frame_retail_price NUMERIC DEFAULT NULL,
  lens_retail_price NUMERIC DEFAULT NULL,
  total_retail_price NUMERIC DEFAULT NULL,
  remark TEXT DEFAULT '',
  UNIQUE (record_no)
);

CREATE TABLE IF NOT EXISTS operation_log (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  operator_id INTEGER DEFAULT NULL,
  operator_name TEXT DEFAULT NULL,
  module TEXT NOT NULL,
  action TEXT NOT NULL,
  method TEXT NOT NULL,
  uri TEXT NOT NULL,
  description TEXT DEFAULT NULL,
  params TEXT DEFAULT NULL,
  status INTEGER NOT NULL DEFAULT 200,
  message TEXT DEFAULT NULL,
  cost_ms INTEGER DEFAULT NULL,
  ip TEXT DEFAULT NULL,
  create_time TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime'))
);

CREATE INDEX IF NOT EXISTS idx_sys_user_deleted ON sys_user(deleted);
CREATE INDEX IF NOT EXISTS idx_sys_user_deleted_time ON sys_user(deleted_time);
CREATE INDEX IF NOT EXISTS idx_customer_deleted ON customer(deleted);
CREATE INDEX IF NOT EXISTS idx_customer_deleted_time ON customer(deleted_time);
CREATE INDEX IF NOT EXISTS idx_customer_id_opto ON optometry_record(customer_id);
CREATE INDEX IF NOT EXISTS idx_opto_deleted ON optometry_record(deleted);
CREATE INDEX IF NOT EXISTS idx_opto_deleted_time ON optometry_record(deleted_time);
CREATE INDEX IF NOT EXISTS idx_customer_id_sales ON sales_record(customer_id);
CREATE INDEX IF NOT EXISTS idx_sales_deleted ON sales_record(deleted);
CREATE INDEX IF NOT EXISTS idx_sales_deleted_time ON sales_record(deleted_time);
CREATE INDEX IF NOT EXISTS idx_operation_log_operator_id ON operation_log(operator_id);
CREATE INDEX IF NOT EXISTS idx_operation_log_create_time ON operation_log(create_time);
