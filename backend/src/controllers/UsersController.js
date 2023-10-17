const Validators = require('../utils/Validators');
const UsersService = require('../services/UsersService');

const usersService = new UsersService();

class UsersController {
    async getUsers(req, res) {
        let {page} = req.query;

        if(!Validators.pageNumber(page)) {
            page = 1;
        }

        const {users, pagination} = await usersService.getAllUsers(page, UsersService.MODELS.LIST_USER_MODEL);

        if(!users) {
            return res.status(404).json({status: false, message: 'Es wurden keine User gefunden!'});
        }

        let userObjectArray = [];
        for(let user of users) {
            userObjectArray.push(user.getAsObject());
        }

        res.status(200).json({
            status: true,
            message: 'Userdaten erfolgreich geladen.',
            data: {
                users: userObjectArray,
                pagination: pagination
            }
        });
    }

    async getUser(req, res) {
        let {id} = req.params;

        const user = await usersService.getUserById(id);
        
        if(!user) {
            return res.status(404).json({status: false, message: 'Ein User mit dieser ID wurde nicht gefunden!'});
        }

        res.status(200).json({
            status: true,
            message: 'Userdaten erfolgreich geladen.',
            data: {
                user: user.getAsObject(),
            }
        });
    }
}

module.exports = UsersController;