const AbstractRepository = require('./AbstractRepository');
class UsersRepository extends AbstractRepository {
    async doesEmailAddressExists(email_address) {
        const {res, fields} = await this.sqlQuery(
            'SELECT count(id) as count FROM users WHERE email_address LIKE ?', 
            [email_address]
        );
        return res ? (res[0] ? res[0].count >= 1 : false) : false;
    }

    async doesUsernameExists(username) {
        const {res, fields} = await this.sqlQuery(
            'SELECT count(id) as count FROM users WHERE username LIKE ?', 
            [username]
        );
        return res ? (res[0] ? res[0].count >= 1 : false) : false;
    }

    async doesUserIdExists(user_id) {
        const {res, fields} = await this.sqlQuery(
            'SELECT count(id) as count FROM users WHERE id LIKE ?', 
            [user_id]
        );
        return res ? (res[0] ? res[0].count >= 1 : false) : false;
    }

    async createUser(email_address, username, password_hash, creation_date) {
        const {res, fields} = await this.sqlQuery(
            'INSERT INTO users (email_address, bio, username, password_hash, creation_date) VALUES (?, ?, ?, ?, ?)', 
            [email_address, 'nothing', username,  password_hash, creation_date]
        );
        return res ? await this.getUserById(res.insertId) : false;
    }

    async getUserById(id) {
        // FullUserModel
        const {res, fields} = await this.sqlQuery(
            'SELECT id, username, bio, email_address, creation_date, password_hash FROM users WHERE id LIKE ?',
            [id]
        );
        return res ? (res[0] ? res[0] : false) : false;
    }

    async getUserByEmailAddress(email_address) {
        // FullUserModel
        const {res, fields} = await this.sqlQuery(
            'SELECT id, username, bio, email_address, creation_date FROM users WHERE email_address LIKE ?',
            [email_address]
        );
        return res ? (res[0] ? res[0] : false) : false;
    }

    async getUserByUsername(username) {
        // FullUserModel
        const {res, fields} = await this.sqlQuery(
            'SELECT id, username, bio, email_address, creation_date, password_hash FROM users WHERE username LIKE ?',
            [username]
        );
        return res ? (res[0] ? res[0] : false) : false;
    }

    async getUsersCount() {
        const {res, fields} = await this.sqlQuery(
            'SELECT count(id) as count FROM users'
        );
        return res ? (res[0] ? res[0].count : 0) : 0;
    }

    async getAllUsers(start, limit) {
        const {res, fields} = await this.sqlQuery(
            'SELECT id, username, bio, email_address, creation_date FROM users ORDER BY creation_date LIMIT ?, ?',
            [start, limit]
        );
        return res ? res : false;
    }

    async changePassword(user_id, password_hash) {
        const {res, fields} = await this.sqlQuery(
            'UPDATE users SET password_hash = ? WHERE id LIKE ? ',
            [password_hash, user_id]
        );
        return res ? true : false;
    }
}

module.exports = UsersRepository;