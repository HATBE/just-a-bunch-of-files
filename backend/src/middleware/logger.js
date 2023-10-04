const morgan = require('morgan');

function logger(app) {
    // logging (common = apache like logging)
    app.use(morgan('common')); 
}

module.exports = logger;