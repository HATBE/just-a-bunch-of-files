import dotenv from 'dotenv';
import express, { Express, Request, Response } from 'express';
import ip from 'ip';

import corsHandler from './middleware/corsHandler.js';
import httpLogger from './middleware/httpLogger.js';
import rateLimiter from './middleware/rateLimmiter.js';
import errorHandler from './middleware/errorHandler.js';

dotenv.config();

const app: Express = express();

const appPort: number = +process.env.PORT || 3000;

// **********
// Middleware
// **********

corsHandler(app);
httpLogger(app);
rateLimiter(app);

app.use(express.json());

// **********
// Routes
// **********

// root route
app.all('/', (req: Request, res: Response) => { 
    res.status(200).json({status: true, message: `Welcome to ${process.env.APP_NAME || 'my app'}'s api. Please read the documentation to use this api, or use the frontend located on ${process.env.URL_FRONTEND || req.hostname}`});
});

// /api/v1/* routes
/*app.use(process.env.API_ENDPOINT_PREFIX, jwtAuth, routes);*/

// default route -> if no other match (404 route)
app.all('*', (req: Request, res: Response) => {
    res.status(404).json({status: false, message: 'Route not found. Please read the documentation to use this api.'});
});

// **********
// Start app
// **********

errorHandler(app);

app.listen(appPort, () => {
    console.clear();
    console.info(`[STARTUP] [INFO] "${process.env.APP_NAME || 'my app'}" has started`);
    console.info(`[STARTUP] [INFO] Backend runs on "${ip.address()}:${appPort}"`);
});