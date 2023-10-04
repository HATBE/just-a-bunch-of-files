const mysql = require('mysql2');

const connection = mysql.createConnection({
    host: process.env.DB_HOST || '',
    user: process.env.DB_USER || '',
    password: process.env.DB_PASS || '',
    database: process.env.DB_NAME || ''
});

connection.connect(err => {
    if(err) {
        // if the server can't connect to db, exit with an error code
        console.error('[DATABASE] [ERROR] error while connecting to database:');
        console.error(err); // print error to log
        process.exit(1); // exit with the error exit code "1"
        return;
    }
    // if db conn was successfull
    console.log(`[DATABASE] [SUCCESS] successfully connected to the database! (${process.env.DB_HOST}->${process.env.DB_NAME})`);
});

module.exports = connection.promise();