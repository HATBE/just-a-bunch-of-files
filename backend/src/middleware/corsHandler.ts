import cors from 'cors';
import { Express } from 'express'

export default function corsHandler(app: Express) {
    app.use('*', cors({
        methods: ['GET', 'POST', 'DELETE', 'UPDATE', 'PUT', 'PATCH'],
        origin: process.env.URL_FRONTEND || 'https://localhost:4200'
    }));
}