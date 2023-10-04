const cors = require('cors');

function corsHandler(app) {
    // cors
    app.use('*', cors({
        methods: ['GET', 'POST', 'DELETE', 'UPDATE', 'PUT', 'PATCH'],
        origin: process.env.URL_FRONTEND || 'https://localhost:4200'
    }));
}
module.exports = corsHandler;