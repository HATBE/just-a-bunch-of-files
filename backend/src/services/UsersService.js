const UsersRepository = require('../repositories/UsersRepository');
const usersRepository = new UsersRepository();

const FullUserModel = require('../models/FullUserModel');
const ListUserModel = require('../models/ListUserModel');
const AuthUserModel = require('../models/AuthUserModel');

const Pagination = require('../utils/Pagination');

const bcrypt = require('bcryptjs');

class UsersService {
    static MODELS = {
        FULL_USER_MODEL: FullUserModel,
        LIST_USER_MODEL: ListUserModel,
        AUTH_USER_MODEL: AuthUserModel
    }

    async doesUsernameExists(username) {
        return await usersRepository.doesUsernameExists(username);
    }

    async doesEmailAddressExists(email_adress) {
        return await usersRepository.doesEmailAddressExists(email_adress);
    }

    async createUser(email_address, username, password_hash, return_model = UsersService.MODELS.FULL_USER_MODEL) {
        const creation_date = Math.round(Date.now() / 1000); // unix timestamp in seconds
        const user = await usersRepository.createUser(email_address, username, password_hash, creation_date);
        return user ? new return_model(user) : false;
    }

    async getUserById(id, return_model = UsersService.MODELS.FULL_USER_MODEL) {
        const user = await usersRepository.getUserById(id);
        return user ? new return_model(user) : false;
    }

    async getUserByEmailAddress(email_address, return_model = UsersService.MODELS.FULL_USER_MODEL) {
        const user = await usersRepository.getUserByEmailAddress(email_address);
        return user ? new return_model(user) : false;
    }

    async getUserByUsername(username, return_model = UsersService.MODELS.FULL_USER_MODEL) {
        const user = await usersRepository.getUserByUsername(username);
        return user ? new return_model(user) : false;
    }

    async verifyPasswords(password, password_hash) {
        if(await bcrypt.compare(password, password_hash)) return true;
        return false;
    }

    async getAllUsers(page = 1, return_model = UsersService.MODELS.FULL_USER_MODEL) {
        const pagination = new Pagination(page, await usersRepository.getUsersCount(), 20);

        const users = await usersRepository.getAllUsers(pagination.getStart(), pagination.getLimit());

        if(!users || users.length < 1) {
            return false;
        }

        let userModelArray = [];
        for(let user of users) {
            userModelArray.push((new return_model(user)));
        }

        return {users: userModelArray, pagination: pagination.getAsObject()};
    }
}

module.exports = UsersService;