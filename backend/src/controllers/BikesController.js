const Validators = require('../utils/Validators');
const BikesService = require('../services/BikesService');

const bikesService = new BikesService();

class BikesController {
    async postBike(req, res) {
        const validation = Validators.validateMulti(req.body, [
            {
                name: 'name',
                validateFor: Validators.VALIDATORS.NAME
            },
            {
                name: 'make',
                validateFor: Validators.VALIDATORS.NO_VALIDATION // TODO:
            },
            {
                name: 'model',
                validateFor: Validators.VALIDATORS.NO_VALIDATION // TODO:
            },
            {
                name: 'year',
                validateFor: Validators.VALIDATORS.NO_VALIDATION // TODO:
            },
            {
                name: 'fromYear',
                validateFor: Validators.VALIDATORS.NO_VALIDATION // TODO:
            },
            {
                name: 'toYear',
                validateFor: Validators.VALIDATORS.NO_VALIDATION // TODO:
            }
        ]);

        if(!validation.status) {
            return res.status(400).json({status: false, message: validation.message});
        }
        
        let {name, make, model, year, fromYear, toYear} = req.body;

        const user_id = req.user.getId();

        const bike = await bikesService.createBike(user_id, name, make, model, year, fromYear, toYear);

        if(!bike) {
            return res.status(500).json({status: false, message: 'Bike creation failed!'});
        }

        return res.status(201).json ({
            status: true,
            message: `Das Bike wurde erfolgreich erstellt.`,
            data: {
                user: bike.getAsObject()
            }
        });
    }
}

module.exports = BikesController;