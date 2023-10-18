const BikesRepository = require('../repositories/BikesRepository');
const bikesRepository = new BikesRepository();

const FullBikeModel = require('../models/FullBikeModel');

class BikesService {
    static MODELS = {
        FULL_BIKE_MODEL: FullBikeModel
    }

    async createBike(user_id, name, make, model, year, fromYear, toYear, return_model = BikesService.MODELS.FULL_BIKE_MODEL) {
        const bike = await bikesRepository.createBike(user_id, name, make, model, year, fromYear, toYear);
        return bike ? new return_model(bike) : false;
    }

    async getBikeById(id, return_model = BikesService.MODELS.FULL_BIKE_MODEL) {
        const bike = await bikesRepository.getBikeById(id);
        return bike ? new return_model(bike) : false;
    }
}

module.exports = BikesService;