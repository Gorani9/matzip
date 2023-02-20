CREATE DATABASE matzip_db;
CREATE USER 'matzip_admin'@'localhost' IDENTIFIED BY 'MATZIP_admin_01';
GRANT ALL PRIVILEGES ON matzip_db.* TO 'matzip_admin'@'localhost';