import morgan from 'morgan';
import { Express } from 'express'

export default function httpLogger(app: Express) {
    // logging (common = apache like logging)
    app.use(morgan('common')); 
}