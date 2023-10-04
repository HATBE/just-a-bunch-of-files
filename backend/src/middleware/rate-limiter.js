const rateLimit = require('express-rate-limit');

function rateLimiter(app) {
    // rate limit
    app.use(rateLimit({
        windowMs: (process.env.RATE_LIMIT_WINDOW_MINUTES || 15) * 60 * 1000, // value in mins
        max: process.env.RATE_LIMIT_MAX || 100,
        standardHeaders: true,
        legacyHeaders: false, 
        message: {status: false, message: 'Too many requests, please try again later.'}
    }));
}

module.exports = rateLimiter;