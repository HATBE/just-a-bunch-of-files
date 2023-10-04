require('dotenv').config(); // environment variables stored in ".ENV" file

const cors = require('./middleware/cors-handler');
const logger = require('./middleware/logger');
const rateLimiter = require('./middleware/rate-limiter');
const errorHandler = require('./middleware/error-handler');
const jwtAuth = require('./middleware/jwt-auth');

const routes = require('./routes/routes');

const express = require('express');
const app = express();

// **********
// Middleware
// **********

cors(app);
logger(app);
rateLimiter(app);

app.use(express.json());

// **********
// Routes
// **********

// root route
app.all('/', (req, res) => { 
    res.status(200).json({status: true, message: `Welcome to ${process.env.APP_NAME || 'my app'}'s api. Please read the documentation to use this api, or use the frontend located on ${process.env.URL_FRONTEND || req.hostname}`});
});

// /api/v1/* routes
app.use(process.env.API_ENDPOINT_PREFIX, jwtAuth, routes);

// default route -> if no other match (404 route)
app.all('*', (req, res) => {
    res.status(404).json({status: false, message: 'Route not found. Please read the documentation to use this api.'});
});

// **********
// Start app
// **********

errorHandler(app);

app.listen(process.env.PORT || 3000, () => {
    console.log(`[INIT] [INFO] ${process.env.APP_NAME || 'my app'} has started.`);
});