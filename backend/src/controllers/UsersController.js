const Validators = require('../utils/Validators');
const UsersService = require('../services/UsersService');
const BikesService = require('../services/BikesService');
const UsersRepository = require('../repositories/UsersRepository');

const usersService = new UsersService();
const bikesService = new BikesService();

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

    async getUsersBikes(req, res) {
        let {id} = req.params;
        let {page} = req.query;

        if(!Validators.pageNumber(page)) {
            page = 1;
        }

        const {bikes, pagination} = await bikesService.getAllBikesFromUser(id, page, BikesService.MODELS.FULL_BIKE_MODEL);

        if(!bikes) {
            return res.status(404).json({status: false, message: 'Dieser User hat noch keine Bikes!'});
        }

        let bikesObjectArray = [];
        for(let bike of bikes) {
            bikesObjectArray.push(bike.getAsObject());
        }

        res.status(200).json({
            status: true,
            message: 'Bikedaten erfolgreich geladen.',
            data: {
                bikes: bikesObjectArray,
                pagination: pagination
            }
        });

    }

    async patchPassword(req, res) {
        let {id} = req.params;

        const validation = Validators.validateMulti(req.body, [
            {
                name: 'newpassword',
                validateFor: Validators.VALIDATORS.PASSWORD
            },
            {
                name: 'oldpassword',
                validateFor: Validators.VALIDATORS.PASSWORD
            }
        ]);

        if(!validation.status) {
            return res.status(400).json({status: false, message: validation.message});
        }

        let {newpassword, oldpassword} = req.body;

        // check if id user exists
        if(!await usersService.doesUserIdExist(id)) {
            return res.status(404).json({status: false, message: 'Dieser User wurde nicht gefunden!'});
        }

        const user = await usersService.getUserById(id, UsersService.MODELS.AUTH_USER_MODEL);

        if(!user) {
            return res.status(404).json({status: false, message: 'Dieser User wurde nicht gefunden!'});
        }

        // if you are not logged in as the user: then
        if((req.user.getId() !== user.getId())) {
            return res.status(401).json({status: false, message: 'Du bist nicht authorisiert diese aktion durchzuführen!'});
        }

        if(!await usersService.verifyPasswords(oldpassword, user.getPasswordHash())) {
            return res.status(401).json({status: false, message: 'Das alte Passwort ist nicht korrekt!'});
        }

        const change = usersService.changePassword(user.getId(), newpassword);

        if(!change) {
            return res.status(500).json({status: false, message: 'Unexpected Error!'});
        }

        res.status(200).json({
            status: true, 
            message: 'Das Passwort wurde erfolgreich geändert.',
        });
    }
}

module.exports = UsersController;