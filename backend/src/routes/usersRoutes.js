const router = require('express').Router();

const UsersController = require('../controllers/UsersController');
const usersController = new UsersController();

const mustAuthorize = require('../middleware/must-authorize')

router.get('/', usersController.getUsers);
router.get('/:id', usersController.getUser);
router.get('/:id/bikes', usersController.getUsersBikes);

module.exports = router;