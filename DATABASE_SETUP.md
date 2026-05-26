# Database setup

This project uses MariaDB/MySQL.

## 1. Create the database

Run the SQL script in `database/schema.sql` in your own MariaDB/MySQL server.

Example:

```bash
mysql -u root -p < database/schema.sql
```

## 2. Configure the app

The app reads the database connection from environment variables:

```text
DB_URL=jdbc:mariadb://localhost:3306/agency_db
DB_USER=root
DB_PASSWORD=your_password
```

If these variables are not set, the app uses:

```text
DB_URL=jdbc:mariadb://localhost:3306/agency_db
DB_USER=root
DB_PASSWORD=
```

You can also pass them as Java system properties:

```bash
mvnw.cmd javafx:run -DDB_URL=jdbc:mariadb://localhost:3306/agency_db -DDB_USER=root -DDB_PASSWORD=your_password
```
