const db = require('../utils/db');

class AbstractRepository {
    constructor() {if (this.constructor == AbstractRepository) {throw new Error('Abstract classes can\'t be instantiated.');}}

    // method to perform an prepared statement
    sqlQuery(query, params = []) {
        return db.query(
            query,
            params
        )
        .then(async ([res, fields]) => {
            return {
                res: res, 
                fields: fields
            };
        })
        .catch(() => {
            return false;
        });
    }
}

module.exports = AbstractRepository;