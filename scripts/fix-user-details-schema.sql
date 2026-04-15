-- Ejecutar en la DB de ms-shop (store)
-- Corrige error: Field 'id' doesn't have a default value en user_details

-- 1) Garantizar PK en id (si no existiera)
SET @has_pk := (
  SELECT COUNT(*)
  FROM information_schema.table_constraints tc
  WHERE tc.table_schema = DATABASE()
    AND tc.table_name = 'user_details'
    AND tc.constraint_type = 'PRIMARY KEY'
);

SET @sql_pk := IF(
  @has_pk = 0,
  'ALTER TABLE user_details ADD PRIMARY KEY (id)',
  'SELECT ''PK already exists'''
);
PREPARE stmt_pk FROM @sql_pk;
EXECUTE stmt_pk;
DEALLOCATE PREPARE stmt_pk;

-- 2) Forzar id como AUTO_INCREMENT
ALTER TABLE user_details
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;

-- 3) Garantizar columna user_id y unicidad (1:1)
SET @has_user_id := (
  SELECT COUNT(*)
  FROM information_schema.columns c
  WHERE c.table_schema = DATABASE()
    AND c.table_name = 'user_details'
    AND c.column_name = 'user_id'
);

SET @sql_user_id := IF(
  @has_user_id = 0,
  'ALTER TABLE user_details ADD COLUMN user_id BIGINT NOT NULL',
  'SELECT ''user_id already exists'''
);
PREPARE stmt_uid FROM @sql_user_id;
EXECUTE stmt_uid;
DEALLOCATE PREPARE stmt_uid;

SET @has_uk_uid := (
  SELECT COUNT(*)
  FROM information_schema.table_constraints tc
  WHERE tc.table_schema = DATABASE()
    AND tc.table_name = 'user_details'
    AND tc.constraint_name = 'uk_user_details_user_id'
    AND tc.constraint_type = 'UNIQUE'
);

SET @sql_uk_uid := IF(
  @has_uk_uid = 0,
  'ALTER TABLE user_details ADD CONSTRAINT uk_user_details_user_id UNIQUE (user_id)',
  'SELECT ''uk_user_details_user_id already exists'''
);
PREPARE stmt_uk FROM @sql_uk_uid;
EXECUTE stmt_uk;
DEALLOCATE PREPARE stmt_uk;

-- 4) Garantizar FK a users(id)
SET @has_fk_uid := (
  SELECT COUNT(*)
  FROM information_schema.referential_constraints rc
  WHERE rc.constraint_schema = DATABASE()
    AND rc.table_name = 'user_details'
    AND rc.constraint_name = 'fk_user_details_user'
);

SET @sql_fk_uid := IF(
  @has_fk_uid = 0,
  'ALTER TABLE user_details ADD CONSTRAINT fk_user_details_user FOREIGN KEY (user_id) REFERENCES users(id)',
  'SELECT ''fk_user_details_user already exists'''
);
PREPARE stmt_fk FROM @sql_fk_uid;
EXECUTE stmt_fk;
DEALLOCATE PREPARE stmt_fk;

SELECT 'user_details schema fixed' AS result;
