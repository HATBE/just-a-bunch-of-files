const router = require('express').Router();

const AuthController = require('../controllers/AuthController');
const authController = new AuthController();

const mustAuthorize = require('../middleware/must-authorize')

router.get('/login', mustAuthorize, authController.getLogin);
router.post('/login', authController.postLogin);
router.post('/register', authController.postRegister);

module.exports = router;