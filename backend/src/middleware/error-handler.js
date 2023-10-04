function errorHandler(app) {
    app.use((err, req, res, next) => {
        const errorMessage = err.message || err.name || 'Unexpected error!';
        console.error(`[ERROR] ${errorMessage}`)
        return res.status(err.statusCode || 500).json({status: false, message: errorMessage, data: {error: err.name || 'Unexpected error!'}});
    });
}

module.exports = errorHandler;