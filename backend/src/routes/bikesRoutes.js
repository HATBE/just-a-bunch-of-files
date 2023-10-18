const router = require('express').Router();

const BikesController = require('../controllers/BikesController');
const bikesController = new BikesController();

const mustAuthorize = require('../middleware/must-authorize')

router.post('/', mustAuthorize, bikesController.postBike);

module.exports = router;