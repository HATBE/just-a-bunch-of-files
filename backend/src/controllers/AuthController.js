const Validators = require('../utils/Validators');
const UsersService = require('../services/UsersService');

const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');

const usersService = new UsersService();

class AuthController {

    // get data of curretly loggedin user
    async getLogin(req, res) {
        res.status(200).json({
            status: true, 
            message:'Erfolgreich die Daten des aktuell angemeldeten Benutzers empfangen.',
            data: {
                user: req.user.getAsObject(),
                login: req.jwtclaim
            }
        });
    }

    // login to user account
    async postLogin(req, res) {
        const validation = Validators.validateMulti(req.body, [
            {
                name: 'username',
                validateFor: Validators.VALIDATORS.USERNAME
            },
            {
                name: 'password',
                validateFor: Validators.VALIDATORS.NO_VALIDATION
            }
        ]);

        if(!validation.status) {
            return res.status(400).json({status: false, message: validation.message});
        }

        let {username, password} = req.body;  

        if(!await usersService.doesUsernameExists(username)) {
            return res.status(401).json({status: false, message: `Ungültige Anmeldedaten!`});
        }

        const user = await usersService.getUserByUsername(username, UsersService.MODELS.AUTH_USER_MODEL);

        if(!user) {
            return res.status(401).json({status: false, message: `Ungültige Anmeldedaten!`});
        }

        // TODO:
        if(!await usersService.verifyPasswords(password, user.getPasswordHash())) {
            console.warn(`[LOGIN] [WARNING] User "[${user.getId()}]${user.getUsername()}/${user.getEmailAddress()}" tried to login but password was wrong!`);
            return res.status(401).json({status: false, message: 'Ungültige Anmeldedaten!'});
        }

        const tokenData = {
            id: user.getId()
        };
    
        const signedToken = jwt.sign(
            tokenData, 
            process.env.AUTH_JWT_SECRET,
            {
                expiresIn: process.env.AUTH_LOGIN_EXPIRES_IN || '1d',
            }
        ); 
    
        console.log(`[LOGIN] [SUCCESS] The user "[${user.getId()}]${user.getUsername()}/${user.getEmailAddress()}" loggedin successfully.`);

        return res.status(200).json({
            status: true, 
            message: `Erfolgreich als "${user.getUsername()}" eingeloggt.`,
            data: {
                token: signedToken,
                user: user.getAsObject()
            }, 
        });
    }

    // register a new user account
    async postRegister(req, res) {
        const validation = Validators.validateMulti(req.body, [
            {
                name: 'username',
                validateFor: Validators.VALIDATORS.USERNAME
            },
            {
                name: 'email_address',
                validateFor: Validators.VALIDATORS.EMAIL_ADDRESS
            },
            {
                name: 'password',
                validateFor: Validators.VALIDATORS.PASSWORD
            }
        ]);

        if(!validation.status) {
            return res.status(400).json({status: false, message: validation.message});
        }
      
        let {username, email_address, password} = req.body;  

        if(await usersService.doesUsernameExists(username)) {
            return res.status(400).json({status: false, message: `Der Nutzername "${username}" ist schon in verwendung!`});
        }

        if(await usersService.doesEmailAddressExists(email_address)) {
            return res.status(400).json({status: false, message: `Die E-Mail "${email_address}" ist schon in verwendung!`});
        }

        const password_hash = await bcrypt.hash(password, await bcrypt.genSalt(10));

        const user = await usersService.createUser(email_address, username, password_hash);

        if(!user) {
            console.error(`[REGISTER] [ERROR] User "${username}/${email_address}" creation failed. Unknown cause!`);
            return res.status(500).json({status: false, message: 'User creation failed!'});
        }

        console.log(`[REGISTER] [SUCCESS] The user "[${user.getId()}]${user.getUsername()}/${user.getEmailAddress()}" was created.`);

        return res.status(201).json ({
            status: true,
            message: `Der User wurde erfolgreich erstellt.`,
            data: {
                user: user.getAsObject()
            }
        });
    }
}

module.exports = AuthController;