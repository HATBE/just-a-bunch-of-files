import { Express, Request, Response, Error, Next } from 'express'

export default function errorHandler(app: Express) {
    app.use((err: Error, req: Request, res: Response, next: Next) => {
        const errorMessage = err.message || err.name || 'Unexpected error!';
        console.error(`[ERROR] ${errorMessage}`)
        return res.status(err.statusCode || 500).json({status: false, message: errorMessage, data: {error: err.name || 'Unexpected error!'}});
    });
}