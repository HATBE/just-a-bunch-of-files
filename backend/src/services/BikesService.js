const BikesRepository = require('../repositories/BikesRepository');
const bikesRepository = new BikesRepository();

const FullBikeModel = require('../models/FullBikeModel');

const Pagination = require('../utils/Pagination');

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

    async getAllBikesFromUser(id, page = 1, return_model = BikesService.MODELS.FULL_BIKE_MODEL) {
        const pagination = new Pagination(page, await bikesRepository.getBikesFromUserCount(id), 20);

        const bikes = await bikesRepository.getBikesFromUser(id, pagination.getStart(), pagination.getLimit());

        if(!bikes || bikes.length < 1) {
            return false;
        }

        let bikesModelArray = [];
        for(let bike of bikes) {
            bikesModelArray.push((new return_model(bike)));
        }

        return {bikes: bikesModelArray, pagination: pagination.getAsObject()};
    }
}

module.exports = BikesService;