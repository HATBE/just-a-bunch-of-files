const router = require('express').Router();

const authRoutes = require('./authRoutes');
const usersRoute = require('./usersRoutes');
const bikesRoute = require('./bikesRoutes');

router.use('/auth', authRoutes);
router.use('/users', usersRoute);
router.use('/bikes', bikesRoute);

module.exports = router;