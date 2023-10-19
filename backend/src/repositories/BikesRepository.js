const AbstractRepository = require('./AbstractRepository');

class BikesRepository extends AbstractRepository {
    async createBike(user_id, name, make, model, year, fromYear, toYear) {
        const {res, fields} = await this.sqlQuery(
            'INSERT INTO bikes (user_id, name, make, model, year, fromYear, toYear) VALUES (?, ?, ?, ?, ?, ?, ?)', 
            [user_id, name, make, model, year, fromYear, toYear]
        );
        return res ? await this.getBikeById(res.insertId) : false;
    }

    async getBikeById(id) {
        // FullBikeModel
        const {res, fields} = await this.sqlQuery(
            'SELECT user_id, name, make, model, year, fromYear, toYear FROM bikes WHERE id LIKE ?',
            [id]
        );
        return res ? (res[0] ? res[0] : false) : false;
    }

    async getBikesFromUserCount(id) {
        const {res, fields} = await this.sqlQuery(
            'SELECT count(id) as count FROM bikes WHERE user_id LIKE ?',
            [id]
        );
        return res ? (res[0] ? res[0].count : 0) : 0;
    }

    async getBikesFromUser(id, start, limit) {
        const {res, fields} = await this.sqlQuery(
            'SELECT id, user_id, name, make, model, year, fromYear, toYear FROM bikes WHERE user_id LIKE ? ORDER BY fromYear DESC, toYear ASC LIMIT ?, ?',
            [id, start, limit]
        );
        return res ? res : false;
    }
}

module.exports = BikesRepository;