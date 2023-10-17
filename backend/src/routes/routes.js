const router = require('express').Router();

const authRoutes = require('./authRoutes');
const usersRoute = require('./usersRoutes');

router.use('/auth', authRoutes);
router.use('/users', usersRoute);

module.exports = router;